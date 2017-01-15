package com.ihandy.a2014011290;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ihandy.a2014011290.mode.MySQLiteOpenHelper;
import com.ihandy.a2014011290.mode.news.News;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ebc5 on 2016/9/3.
 */
public class FavouriteActivity extends Activity {
    private ListView listView;
    private List<News> favouriteList;
    private Context context = this;
    private Toolbar toolbar;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourites);
        //从数据库导入数据
        importData();
        //选项卡
        listView = (ListView) findViewById(R.id.listView);
        adapter = new MyAdapter(context);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context,NewsDetailActivity.class);
                intent.putExtra("category",favouriteList.get(position).category);
                intent.putExtra("images",favouriteList.get(position).img);
                intent.putExtra("news_id",favouriteList.get(position).news_id);
                intent.putExtra("origin",favouriteList.get(position).origin);
                intent.putExtra("source_url",favouriteList.get(position).source_url);
                intent.putExtra("title",favouriteList.get(position).title);
                startActivity(intent);
            }
        });
        //工具栏
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.back_page);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setTitle("Favourite");

    }
    //内部类（直接对父类数据操作，使adapter.notifyDataSetChanged()能正确执行）
    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        Cursor cursor;
        MyAdapter(Context context){mInflater = LayoutInflater.from(context);}
        @Override
        public int getCount()
        {
            return favouriteList.size();
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
            public TextView titleTextView;
            public ImageView imageView;
            public TextView originTextView;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            if(convertView == null)
            {
                convertView = mInflater.inflate(R.layout.list_item,null);
                holder = new ViewHolder();
                holder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
                holder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
                holder.originTextView = (TextView) convertView.findViewById(R.id.originTextView);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 3;
            Bitmap image = BitmapFactory.decodeByteArray(favouriteList.get(position).img, 0, favouriteList.get(position).img.length,opts);
            holder.imageView.setImageBitmap(image);
            holder.titleTextView.setText(favouriteList.get(position).title);
            holder.originTextView.setText(favouriteList.get(position).origin);
            return convertView;
        }
    }
    //从数据库读取数据
    private void importData() {
        favouriteList = new ArrayList<News>();
        MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from favourites",null);
        if(cursor.moveToFirst())
        {
            int count = cursor.getCount();
            int categoryIdx = cursor.getColumnIndex("category");
            int imgIdx = cursor.getColumnIndex("img");
            int newsIDIdx = cursor.getColumnIndex("news_id");
            int originIdx = cursor.getColumnIndex("origin");
            int sourceURLIdx = cursor.getColumnIndex("source_url");
            int titleIdx = cursor.getColumnIndex("title");

            for(int i=0;i<count;i++)
            {
                cursor.moveToPosition(i);
                News news = new News();
                news.category = cursor.getString(categoryIdx);
                news.img = cursor.getBlob(imgIdx);;
                news.news_id = cursor.getString(newsIDIdx);
                news.origin = cursor.getString(originIdx);
                news.source_url = cursor.getString(sourceURLIdx);
                news.title = cursor.getString(titleIdx);
                favouriteList.add(news);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        importData();
        adapter.notifyDataSetChanged();

    }
}
