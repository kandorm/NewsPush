package com.ihandy.a2014011290.mode.news;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ihandy.a2014011290.mode.JsonURL;
import com.ihandy.a2014011290.mode.MySQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NewsOfOneCategory {
    private String urlStr=null;
    private String jsonStr=null;
    private String category;
    private Context context;
    public NewsOfOneCategory(Context context,String category)
    {
        this.context = context;
        this.category = category;
        urlStr = "http://assignment.crazz.cn/news/query?locale=en&category="+category;
    }
    public NewsOfOneCategory(Context context,String category,String max_news_id)
    {
        this.context = context;
        this.category = category;
        urlStr = "http://assignment.crazz.cn/news/query?locale=en&category="+category+"&max_news_id="+max_news_id;
    }

    public void addNewsToDatabase()
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
            if(data==null)return;
            JSONArray news = data.optJSONArray("news");
            if(news==null)return;
            int length = Math.min(news.length(),10);
           for(int i=0;i<length;i++)
            {
                JSONObject ob = (JSONObject)news.get(i);
                ContentValues cv = new ContentValues();

                byte[] blob=null;
                String news_id=null;
                String source_name=null;
                String source_url=null;

                JSONArray imgs = ob.optJSONArray("imgs");
                if(imgs!=null)
                {
                    JSONObject img = imgs.optJSONObject(0);
                    String imgURL=null;
                    if(img!=null)
                        imgURL = img.optString("url");
                    blob = getPicture(imgURL);
                }
                news_id = ob.optString("news_id");
                JSONObject source = ob.optJSONObject("source");
                if(source!=null)
                {
                    source_name = source.optString("name");
                    source_url = source.optString("url");
                }
                cv.put("source_name",source_name);
                cv.put("source_url",source_url);
                cv.put("img",blob);
                cv.put("category",category);
                cv.put("title",ob.optString("title"));
                cv.put("origin",ob.optString("origin"));
                cv.put("news_id",news_id);

                Cursor cursor = db.rawQuery("select * from news where news_id=?",new String[]{news_id});
                cursor.moveToFirst();
                int count = cursor.getCount();
                if(count == 0)
                    db.insert("news","id",cv);
                else
                    db.update("news",cv,"news_id=?",new String[]{news_id});
            }
        }
        catch (JSONException e) {e.printStackTrace();}
        finally {
            db.close();
        }
    }
    private byte[] getPicture(String urlStr)
    {
        URL url;
        HttpURLConnection httpConn;
        InputStream inputStream = null;
        ByteArrayOutputStream out = null;
        try
        {
            url = new URL(urlStr);
            httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setConnectTimeout(3000);
            httpConn.setDoInput(true);
            int respCode = httpConn.getResponseCode();
            if (respCode == 200)
            {
                inputStream = httpConn.getInputStream();
                out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len = 0;
                try
                {
                    while ((len = inputStream.read(buffer, 0, buffer.length)) != -1)
                    {
                        out.write(buffer, 0, len);
                    }
                }
                catch (IOException e) {e.printStackTrace();}
                finally {
                    if(inputStream!=null)
                        inputStream.close();
                }
                return out.toByteArray();
            }
        }
        catch (MalformedURLException e){e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}
        return new byte[1024];
    }

}
