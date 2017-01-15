package com.ihandy.a2014011290;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ihandy.a2014011290.mode.MySQLiteOpenHelper;
import com.ihandy.a2014011290.mode.news.News;
import com.ihandy.a2014011290.mode.news.NewsOfOneCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PageFragment extends Fragment {

    private static final String ARGS_NAME = "category";
    private String category;
    private List<News> newsList = new ArrayList<News>();
    private PullToRefreshListView listView;
    private ListViewAdapter adapter;
    private static Context myContext;
    private FloatingActionButton fab;
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        if(getArguments()!=null)
        {
            category = getArguments().getString(ARGS_NAME);
            //根据分类从数据库导入数据
            importData();
        }

        //选项卡
        listView = (PullToRefreshListView) view.findViewById(R.id.listView);
        adapter = new ListViewAdapter(this.getActivity());
        listView.setAdapter(adapter);
        listView.setMode(PullToRefreshBase.Mode.BOTH);

        //初始化上拉刷新与下拉刷新显示的文字
        init();

        //添加监听器以启动NewsDetailActivity来显示对应当前的新闻详情
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),NewsDetailActivity.class);
                intent.putExtra("category",newsList.get(position-1).category);
                intent.putExtra("images",newsList.get(position-1).img);
                intent.putExtra("news_id",newsList.get(position-1).news_id);
                intent.putExtra("origin",newsList.get(position-1).origin);
                intent.putExtra("source_url",newsList.get(position-1).source_url);
                intent.putExtra("title",newsList.get(position-1).title);
                startActivity(intent);
            }
        });

        //添加上拉刷新与下拉刷新监听器
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>(){
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                //异步加载最新新闻
                new PullDownRefresh().execute();
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                //异步加载更老新闻
                new PullUpRefresh().execute();
            }
        });

        //回到顶部的按钮
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView lvShow = listView.getRefreshableView();
                lvShow.smoothScrollToPosition(0);//平滑的回到顶部
            }
        });

        //在listview向下滑过两个item时，回到顶部按钮才会显示，否则会隐藏
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

        return view;
    }
    //初始化下拉刷新和上拉刷新的显示文字
    private void init() {
        ILoadingLayout startLabels = listView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("下拉刷新...");// 刚下拉时，显示的提示
        startLabels.setRefreshingLabel("正在载入...");// 刷新时
        startLabels.setReleaseLabel("放开刷新...");// 下来达到一定距离时，显示的提示

        ILoadingLayout endLabels = listView.getLoadingLayoutProxy(false, true);
        endLabels.setPullLabel("上拉刷新...");// 刚下拉时，显示的提示
        endLabels.setRefreshingLabel("正在载入...");// 刷新时
        endLabels.setReleaseLabel("放开刷新...");// 下来达到一定距离时，显示的提示
    }
    //初始化当前分类
    public static PageFragment init(Context context,String category) {
        myContext = context;
        PageFragment pageFragment = new PageFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_NAME,category);
        pageFragment.setArguments(args);
        return pageFragment;
    }
    //初始化列表，从数据库导入最多10条数据
    private void importData() {
        newsList = new ArrayList<News>();
        MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(this.getActivity());
        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from news where category=? order by news_id desc",new String[]{category});
        if(cursor.moveToFirst())
        {
            int count = cursor.getCount();
            int imgIdx = cursor.getColumnIndex("img");
            int newsIDIdx = cursor.getColumnIndex("news_id");
            int originIdx = cursor.getColumnIndex("origin");
            int sourceURLIdx = cursor.getColumnIndex("source_url");
            int titleIdx = cursor.getColumnIndex("title");

            count = Math.min(count,10);
            for(int i=0;i<count;i++)
            {
                cursor.moveToPosition(i);
                News news = new News();
                news.category = category;
                news.img = cursor.getBlob(imgIdx);;
                news.news_id = cursor.getString(newsIDIdx);
                news.origin = cursor.getString(originIdx);
                news.source_url = cursor.getString(sourceURLIdx);
                news.title = cursor.getString(titleIdx);
                newsList.add(news);
            }
            Comparator comp = new SortComparator();
            Collections.sort(newsList,comp);
        }
        db.close();

    }
    //加载最新数据
    private void refreshNewData(String news_id) {
        MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(this.getActivity());
        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from news where category=? and news_id>? order by news_id desc",new String[]{category,news_id});
        if(cursor.moveToFirst())
        {
            int count = cursor.getCount();
            int imgIdx = cursor.getColumnIndex("img");
            int newsIDIdx = cursor.getColumnIndex("news_id");
            int originIdx = cursor.getColumnIndex("origin");
            int sourceURLIdx = cursor.getColumnIndex("source_url");
            int titleIdx = cursor.getColumnIndex("title");
            count = Math.min(count,10);
            for(int i=0;i<count;i++)
            {
                cursor.moveToPosition(i);
                News news = new News();
                news.category = category;
                news.img = cursor.getBlob(imgIdx);;
                news.news_id = cursor.getString(newsIDIdx);
                news.origin = cursor.getString(originIdx);
                news.source_url = cursor.getString(sourceURLIdx);
                news.title = cursor.getString(titleIdx);
                newsList.add(news);
            }
            Comparator comp = new SortComparator();
            Collections.sort(newsList,comp);
        }

    }
    //加载更老数据
    private void refreshOldData(String news_id) {
        MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(this.getActivity());
        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from news where category=? and news_id<? order by news_id desc",new String[]{category,news_id});
        if(cursor.moveToFirst())
        {
            int count = cursor.getCount();
            int imgIdx = cursor.getColumnIndex("img");
            int newsIDIdx = cursor.getColumnIndex("news_id");
            int originIdx = cursor.getColumnIndex("origin");
            int sourceURLIdx = cursor.getColumnIndex("source_url");
            int titleIdx = cursor.getColumnIndex("title");
            count = Math.min(count,10);
            for(int i=0;i<count;i++)
            {
                cursor.moveToPosition(i);
                News news = new News();
                news.category = category;
                news.img = cursor.getBlob(imgIdx);;
                news.news_id = cursor.getString(newsIDIdx);
                news.origin = cursor.getString(originIdx);
                news.source_url = cursor.getString(sourceURLIdx);
                news.title = cursor.getString(titleIdx);
                newsList.add(news);
            }
            Comparator comp = new SortComparator();
            Collections.sort(newsList,comp);
        }

    }
    //用于加载数据后排序
    private class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            News a = (News) lhs;
            News b = (News) rhs;

            return new Long(b.news_id).compareTo(new Long(a.news_id));
        }
    }
    //内部类（直接对父类数据操作，使pagerAdapter.notifyDataSetChanged()能正确执行）
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

        //使得holder得到重复利用
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
            //将byte[]类型的图片信息转化为bitmap类型，同时压缩图片，避免内存溢出
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 2;
            Bitmap image = BitmapFactory.decodeByteArray(newsList.get(position).img, 0, newsList.get(position).img.length,opts);

            holder.imageView.setImageBitmap(image);
            holder.titleTextView.setText(newsList.get(position).title);
            holder.originTextView.setText(newsList.get(position).origin);
            return convertView;
        }
    }
    //异步加载最新数据同时更新列表
    private class PullDownRefresh extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            new NewsOfOneCategory(myContext,category).addNewsToDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            int count1 = newsList.size();
            if(newsList.size()==0)
                importData();
            else
                refreshNewData(newsList.get(0).news_id);
            int count2 = newsList.size();
            if(count2 > count1)
                Toast.makeText(myContext, "刷新成功", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(myContext, "刷新失败", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
            listView.onRefreshComplete();
        }
    }
    //异步加载更老数据同时更新列表
    private class PullUpRefresh extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            new NewsOfOneCategory(myContext,category,newsList.get(newsList.size()-1).news_id).addNewsToDatabase();
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e){}
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            int count1 = newsList.size();
            if(newsList.size()==0)
                importData();
            else
                refreshOldData(newsList.get(newsList.size()-1).news_id+"");
            int count2 = newsList.size();
            if(count2 > count1)
                Toast.makeText(myContext, "刷新成功", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(myContext, "刷新失败", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
            listView.onRefreshComplete();
        }
    }
}
