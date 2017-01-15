package com.ihandy.a2014011290;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ihandy.a2014011290.mode.MySQLiteOpenHelper;
import com.ihandy.a2014011290.mode.categories.Categories;
import com.ihandy.a2014011290.mode.categories.Category;
import com.ihandy.a2014011290.mode.news.NewsOfOneCategory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private List<Category> categories;
    private Context context = this;
    private ViewPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TextView loadText;
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //模板自带  实现侧边栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //从数据库获取分类，若第一次登录，则从服务器获取分类和部分新闻
        initTab();//从数据库获取分类
        loadText = (TextView) findViewById(R.id.connectionError);//用于在未连接服务器导致无信息显示时作为提示
        if(categories.size()==0)
            firstUpdate();//从服务器获取分类和部分新闻
        if(categories.size()==0)
            loadText.setVisibility(View.VISIBLE);

        new CategoryRefresh().execute();//异步加载最新分类
        new NewsRefresh().execute();//异步加载最新新闻
        //选项卡
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setTabMode (TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);

        //用于在分类变动时使显示正确
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
    //模板自带
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        //根据侧边栏选项启动对应的activity
        if (id == R.id.nav_favourite) {
            Intent intent = new Intent(context,FavouriteActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_category_management) {
            Intent intent = new Intent(context,TabManagerActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about_me) {
            Intent intent = new Intent(context,AboutMeAcitivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_search) {
            Intent intent = new Intent(context,SearchActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onResume() {
        super.onResume();
        //分类管理后更新数据（管理后写入数据库，此处重新从数据库读取）
        initTab();
        pagerAdapter.notifyDataSetChanged();

    }
    //初始化分类（从数据库读取）
    private void initTab() {
        categories = new ArrayList<Category>();
        MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from categories where show=?",new String[]{"1"});
        if(cursor.moveToFirst())
        {
            int count = cursor.getCount();
            int keyIdx = cursor.getColumnIndex("key");
            int valueIdx = cursor.getColumnIndex("value");
            int showIdx = cursor.getColumnIndex("show");
            for(int i=0;i<count;i++)
            {
                cursor.moveToPosition(i);
                Category category = new Category();
                category.key = cursor.getString(keyIdx);
                category.value = cursor.getString(valueIdx);
                category.show = cursor.getInt(showIdx);
                categories.add(category);
            }
        }
        db.close();
    }
    //内部类（直接对父类数据操作，使pagerAdapter.notifyDataSetChanged()能正确执行）
    private class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //getItem 根据当前分类生成对应新闻的fragment
        @Override
        public Fragment getItem(int position) {
            return new PageFragment().init(context,categories.get(position).key);
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        //以value值作为用于显示的新闻分类
        @Override
        public CharSequence getPageTitle(int position)
        {
            return categories.get(position).value;
        }

        //对ItemId进行哈希 同时使ItemPosition返回POSITION_NONE，使得不存在的页面及时销毁
        @Override
        public long getItemId(int position) {
            // 获取当前数据的hashCode
            int hashCode = categories.get(position).hashCode();
            return hashCode;
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }
    //第一次登录时获取数据（多个线程运行，且都要运行完，较耗时）
    private void firstUpdate() {
        Thread thread1;
        (thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                new Categories(context,String.valueOf(System.currentTimeMillis())).addCategoriesToDatabase();
            }})).start();
        try
        {
            thread1.join();
        }catch(InterruptedException e){}

        initTab();

        List<Thread> threadList = new ArrayList<Thread>();
        for(int i=0;i<categories.size();i++)
        {
            final String category = categories.get(i).key;
            Thread thread;
            (thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    new NewsOfOneCategory(context,category).addNewsToDatabase();
                }})).start();
            threadList.add(thread);
        }
        for(int i=0;i<threadList.size();i++)
        {
            try{threadList.get(i).join();}
            catch (InterruptedException e){}
        }

    }
    //异步加载最新分类
    private class CategoryRefresh extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            new Categories(context,String.valueOf(System.currentTimeMillis())).addCategoriesToDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            pagerAdapter.notifyDataSetChanged();
        }
    }
    //异步加载所有分类的最新新闻
    private class NewsRefresh extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            for(int i=0;i<categories.size();i++)
            {
                final String category = categories.get(i).key;
                new NewsOfOneCategory(context,category).addNewsToDatabase();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            pagerAdapter.notifyDataSetChanged();
        }
    }
    //实现按两次后退键退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }
}
