package com.example.gebilaohu.ludengctr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gebilaohu on 2020/3/8.
 */

public class MyOpenHelper extends SQLiteOpenHelper {

    public MyOpenHelper(Context context) {

        super(context, "pm25_time.db", null, 1); //创建sqlite数据库
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table pm25_time(_id integer primary key autoincrement,pm25 text,time text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
