package com.ihandy.a2014011290.mode.categories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ihandy.a2014011290.mode.JsonURL;
import com.ihandy.a2014011290.mode.MySQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class Categories
{
    private String urlStr;
    private String jsonStr;
    private Context context;
    public Categories(Context context, String timestamp)
    {
        this.context = context;
        this.urlStr = "http://assignment.crazz.cn/news/en/category?timestamp="+timestamp;
    }

   public void addCategoriesToDatabase()
    {
        jsonStr = JsonURL.getJsonContent(urlStr);
        if(jsonStr.equals("") || jsonStr==null)return;
        JSONObject jsonObj;
        MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = myHelper.getWritableDatabase();
        try
        {
            jsonObj = new JSONObject(jsonStr);
            JSONObject data = jsonObj.optJSONObject("data");
            JSONObject categories = data.optJSONObject("categories");
            Iterator iterator = categories.keys();
            while(iterator.hasNext())
            {
                String key = (String)iterator.next();
                ContentValues cv = new ContentValues();
                cv.put("key",key);
                cv.put("value",categories.optString(key));
                Cursor cursor = db.rawQuery("select * from categories where key=?",new String[]{key});
                cursor.moveToFirst();
                int count = cursor.getCount();
                if(count == 0)
                {
                    cv.put("show",1);
                    db.insert("categories","id",cv);
                }
                else
                {
                    db.update("categories",cv,"key=?",new String[]{key});

                }
            }
        }
        catch (JSONException e) {e.printStackTrace();}
        return;
    }
}
