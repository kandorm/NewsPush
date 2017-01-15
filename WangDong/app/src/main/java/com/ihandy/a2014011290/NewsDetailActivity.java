package com.ihandy.a2014011290;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ihandy.a2014011290.mode.MySQLiteOpenHelper;

/**
 * Created by ebc5 on 2016/9/2.
 */
public class NewsDetailActivity extends Activity {

    private WebView webView;
    private Toolbar toolbar;
    private ImageButton shareButton;
    private ImageButton loveButton;

    private String category;
    private byte[] images;
    private String news_id;
    private String origin;
    private String source_url;
    private String title;

    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail);

        webView = (WebView) findViewById(R.id.webView);

        //接收fragment传来的信息
        Intent intent = getIntent();
        category = intent.getStringExtra("category");
        images = intent.getByteArrayExtra("images");
        news_id = intent.getStringExtra("news_id");
        origin = intent.getStringExtra("origin");
        source_url = intent.getStringExtra("source_url");
        title = intent.getStringExtra("title");
        //source为null
        if(source_url==null || source_url.equals(""))
            Toast.makeText(this, "Can't connect to the news source! Please connect to the Internet or restart!", Toast.LENGTH_LONG).show();

        //显示新闻详情
        webView.loadUrl(source_url);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        //设置退出键（只有通过退出键和后退键退出才能保存对新闻的收藏信息）
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.back_page);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //退出时更新数据库
                MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(NewsDetailActivity.this);
                SQLiteDatabase db = myHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("category",category);
                cv.put("img",images);
                cv.put("news_id",news_id);
                cv.put("origin",origin);
                cv.put("source_url",source_url);
                cv.put("title",title);
                Cursor cursor = db.rawQuery("select * from favourites where news_id=?",new String[]{news_id});
                cursor.moveToFirst();
                int count = cursor.getCount();
                if(count == 0 && flag)//没有需添加
                {
                    db.insert("favourites","id",cv);
                }
                else if(!flag)//取消（无论有没有）
                {
                    db.delete("favourites", "news_id=?", new String[]{news_id});
                }
                else//数据库有同时这次没变动，更新数据库
                {
                    db.update("favourites",cv,"news_id=?",new String[]{news_id});
                }
                //销毁activity（退出）
                finish();
            }
        });
        //分享按钮（系统自带分享）
        shareButton = (ImageButton) findViewById(R.id.sharebar);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, title);
                intent.putExtra(Intent.EXTRA_TEXT, source_url);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getTitle()));
            }
        });
        //收藏按钮（只修改flag，不修改数据库）
        loveButton = (ImageButton) findViewById(R.id.lovebar);
        loveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!flag)
                {
                    flag = true;
                    loveButton.setImageResource(R.drawable.love_bar_click);
                }
                else
                {
                    flag = false;
                    loveButton.setImageResource(R.drawable.love_bar);
                }
            }
        });
        //初始化收藏按钮，使其正确显示
        initData();

    }
    //初始化收藏按钮，使其正确显示
    private void initData() {
        MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(NewsDetailActivity.this);
        SQLiteDatabase db = myHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from favourites where news_id=?",new String[]{news_id});
        cursor.moveToFirst();
        int count = cursor.getCount();
        if(count == 0)
        {
            flag = false;
            loveButton.setImageResource(R.drawable.love_bar);
        }
        else
        {
            flag = true;
            loveButton.setImageResource(R.drawable.love_bar_click);
        }
    }

    @Override
    //设置回退 回到新闻上一层或退出（同时更新数据库）
    //覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(webView.canGoBack())
                webView.goBack(); //goBack()表示返回WebView的上一页面
            else
            {
                //更新数据库
                MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(NewsDetailActivity.this);
                SQLiteDatabase db = myHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("category",category);
                cv.put("img",images);
                cv.put("news_id",news_id);
                cv.put("origin",origin);
                cv.put("source_url",source_url);
                cv.put("title",title);

                Cursor cursor = db.rawQuery("select * from favourites where news_id=?",new String[]{news_id});
                cursor.moveToFirst();
                int count = cursor.getCount();
                if(count == 0 && flag)
                {
                    db.insert("favourites","id",cv);
                }
                else if(!flag)
                {
                    db.delete("favourites", "news_id=?", new String[]{news_id});
                }
                else
                {
                    db.update("favourites",cv,"news_id=?",new String[]{news_id});
                }
                //销毁activity（退出）
                finish();
            }
            return true;
        }
        return false;
    }
}
