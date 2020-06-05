package com.example.checkoutdevices.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="devices.db";//数据库名字
    private static final int DATABASE_VERSION=1;//数据库版本


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 创建数据库表：devicesinfo
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS devices(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                +
               "zkid VARCHAR,pin VARCHAR,zkversion VARCHAR," +
                "time VARCHAR,operator VARCHAR," +
                "zktype VARCHAR,zkfactory VARCHAR,issynchr VARCHAR," +
                "qrcode VARCHAR,isqrcode VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
