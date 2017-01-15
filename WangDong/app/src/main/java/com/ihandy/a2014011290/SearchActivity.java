package com.ihandy.a2014011290;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ihandy.a2014011290.mode.MySQLiteOpenHelper;
import com.ihandy.a2014011290.mode.news.News;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ebc5 on 2016/9/9.
 */
public class SearchActivity extends Activity {
    private ImageButton searchButton;
    private TextView searchText;
    private String keyWord;
    private ListView listView;
    private List<News> newsList = new ArrayList<News>();
    private ListViewAdapter adapter;
    private Context context = this;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        //搜索框
        searchButton = (ImageButton) findViewById(R.id.searchButton);
        searchText = (TextView) findViewById(R.id.searchText);

        //选项卡
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ListViewAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context,NewsDetailActivity.class);
                intent.putExtra("category",newsList.get(position).category);
                intent.putExtra("images",newsList.get(position).img);
                intent.putExtra("news_id",newsList.get(position).news_id);
                intent.putExtra("origin",newsList.get(position).origin);
                intent.putExtra("source_url",newsList.get(position).source_url);
                intent.putExtra("title",newsList.get(position).title);
                startActivity(intent);
            }
        });
        //对新闻标题模糊搜索
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyWord = searchText.getText().toString();
                newsList = new ArrayList<News>();
                adapter.notifyDataSetChanged();
                if(keyWord==null || keyWord.equals(""))return;

                MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(context);
                SQLiteDatabase db = myHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from news where title like '%"+keyWord+"%' order by news_id desc",null);
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
                        news.img = cursor.getBlob(imgIdx);
                        news.news_id = cursor.getString(newsIDIdx);
                        news.origin = cursor.getString(originIdx);
                        news.source_url = cursor.getString(sourceURLIdx);
                        news.title = cursor.getString(titleIdx);
                        newsList.add(news);
                    }
                }
                Comparator comp = new SortComparator();
                Collections.sort(newsList,comp);
                adapter.notifyDataSetChanged();
                if(newsList.size()==0)
                    Toast.makeText(context,"No such news",Toast.LENGTH_SHORT).show();
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
        toolbar.setTitle("Search");
        //回到顶部按钮
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.smoothScrollToPosition(0);
            }
        });
        fab.setVisibility(View.INVISIBLE);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem > 1)
                    fab.setVisibility(View.VISIBLE);
                else
                    fab.setVisibility(View.INVISIBLE);

            }
        });
    }
    //比较（用于排序）
    private class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            News a = (News) lhs;
            News b = (News) rhs;

            return new Long(b.news_id).compareTo(new Long(a.news_id));
        }
    }

    private class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        Cursor cursor;
        ListViewAdapter(Context context){mInflater = LayoutInflater.from(context);}
        @Override
        public int getCount()
        {
            return newsList.size();
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
            public ImageView imageView;
            public TextView titleTextView;
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
            Bitmap image = BitmapFactory.decodeByteArray(newsList.get(position).img, 0, newsList.get(position).img.length,opts);
            holder.imageView.setImageBitmap(image);
            holder.titleTextView.setText(newsList.get(position).title);
            holder.originTextView.setText(newsList.get(position).origin);
            return convertView;
        }
    }
}
