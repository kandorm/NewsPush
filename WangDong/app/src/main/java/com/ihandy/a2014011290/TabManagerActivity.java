package com.ihandy.a2014011290;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ihandy.a2014011290.mode.MySQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ebc5 on 2016/9/2.
 */
public class TabManagerActivity extends Activity {
    private List<String> watched;
    private List<String> unwatched;
    private ListView listView;
    private MyAdapter adapter;
    private Toolbar toolbar;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_manager);
        //从数据库加载数据
        importData();
        //选项卡
        listView = (ListView) findViewById(R.id.tabmanagerlistview);
        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //实现点击调整两个列表的数据同时更新整个列表的显示
                if(position == 0 || position == watched.size()+1)
                {
                }
                else if(position <= watched.size()) {
                    unwatched.add(watched.remove(position - 1));
                }
                else
                {
                    watched.add(unwatched.remove(position-watched.size()-2));
                }
                adapter.notifyDataSetChanged();
            }
        });
        //工具栏（点击后退按钮实现数据库修改）
        toolbar = (Toolbar) findViewById(R.id.tabmanagertoolbar);
        toolbar.setNavigationIcon(R.drawable.back_page);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(context);
                SQLiteDatabase db = myHelper.getWritableDatabase();
                for(int i=0;i<watched.size();i++)
                {
                    ContentValues cv = new ContentValues();
                    cv.put("show",1);
                    db.update("categories",cv,"value=?",new String[]{watched.get(i)});
                }
                for(int i=0;i<unwatched.size();i++)
                {
                    ContentValues cv = new ContentValues();
                    cv.put("show",0);
                    db.update("categories",cv,"value=?",new String[]{unwatched.get(i)});
                }
                finish();
            }
        });
        toolbar.setTitle("Category Management");
    }
    //从数据库获取显示与不显示的两个列表
    private void importData() {
        watched = new ArrayList<String>();
        unwatched = new ArrayList<String>();
        MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(this);
        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from categories",null);
        if(cursor.moveToFirst())
        {
            int count = cursor.getCount();
            int valueIdx = cursor.getColumnIndex("value");
            int showIdx = cursor.getColumnIndex("show");
            for(int i=0;i<count;i++)
            {
                cursor.moveToPosition(i);
                String value = cursor.getString(valueIdx);
                int show = cursor.getInt(showIdx);
                if(show==0)
                    unwatched.add(value);
                else
                    watched.add(value);
            }
        }
    }

    //根据对应位置加载数据
    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        Cursor cursor;
        MyAdapter(Context context){mInflater = LayoutInflater.from(context);}
        @Override
        public int getCount()
        {
            return watched.size()+unwatched.size()+2;
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }
        class ViewHolder
        {
            public TextView categoryTextView;
            public ImageView imageView;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            if(convertView == null)
            {
                convertView = mInflater.inflate(R.layout.tab_manager_item,null);
                holder = new ViewHolder();
                holder.imageView = (ImageView)convertView.findViewById(R.id.moveImageView);
                holder.categoryTextView = (TextView) convertView.findViewById(R.id.categoryTextView);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            if(position == 0)
            {
                holder.categoryTextView.setText("Watched");
                holder.imageView.setVisibility(View.INVISIBLE);
            }
            else if(position <= watched.size())
            {
                holder.imageView.setImageResource(R.drawable.tab_down);
                holder.categoryTextView.setText(watched.get(position-1));
                holder.imageView.setVisibility(View.VISIBLE);
            }
            else if(position == watched.size()+1)
            {
                holder.categoryTextView.setText("Unwatched");
                holder.imageView.setVisibility(View.INVISIBLE);
            }
            else
            {
                holder.imageView.setImageResource(R.drawable.tab_up);
                holder.categoryTextView.setText(unwatched.get(position-watched.size()-2));
                holder.imageView.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }
    @Override
    //设置回退（修改数据库数据）
    //覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(context);
            SQLiteDatabase db = myHelper.getWritableDatabase();
            for(int i=0;i<watched.size();i++)
            {
                ContentValues cv = new ContentValues();
                cv.put("show",1);
                db.update("categories",cv,"value=?",new String[]{watched.get(i)});
            }
            for(int i=0;i<unwatched.size();i++)
            {
                ContentValues cv = new ContentValues();
                cv.put("show",0);
                db.update("categories",cv,"value=?",new String[]{unwatched.get(i)});
            }
            //销毁Activity（退出）
            finish();
            return true;
        }
        return false;
    }
}
