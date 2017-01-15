package com.ihandy.a2014011290.mode;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ebc5 on 2016/8/31.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "news.db";
    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    public MySQLiteOpenHelper(Context context, String name){
        this(context,name, null,1);
    }
    public MySQLiteOpenHelper(Context context){
        this(context,DATABASE_NAME, null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table if not exists categories(" +
                "id integer primary key autoincrement," +
                "key varchar," +
                "value varchar," +
                "show INTEGER)");
        sqLiteDatabase.execSQL("create table if not exists news(" +
                "id integer primary key autoincrement," +
                "category varchar," +
                "img blob," +
                "news_id varchar," +
                "origin varchar," +
                "source_name varchar," +
                "source_url varchar," +
                "title varchar)");
        sqLiteDatabase.execSQL("create table if not exists favourites(" +
                "id integer primary key autoincrement," +
                "category varchar," +
                "img blob," +
                "news_id varchar," +
                "origin varchar," +
                "source_name varchar," +
                "source_url varchar," +
                "title varchar)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
