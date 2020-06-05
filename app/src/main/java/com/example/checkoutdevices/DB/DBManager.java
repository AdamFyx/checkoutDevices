package com.example.checkoutdevices.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.checkoutdevices.Bean.Devices;

import java.util.ArrayList;
import java.util.HashMap;

public class DBManager {
    String TAG = "DBManger";
    DBHelper helper;
    SQLiteDatabase sqLiteDatabase;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        sqLiteDatabase = helper.getReadableDatabase();
    }

    /**
     * execSql()方法可以执行 Insert,update,delete语句
     * 实现对数据库的增删改功能
     * sql为操作语句 bindArgs为操作传递参数
     */
    public boolean updateSQLite(String sql, Object[] bindArgs) {
        boolean isSuccess = false;
        try {
            sqLiteDatabase.execSQL(sql, bindArgs);
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if ((sqLiteDatabase != null)) {
                sqLiteDatabase.close();
            }
        }
        return isSuccess;
    }

    /**
     * rawQuery()方法可以执行select语句
     * 实现查询功能
     * sql为操作语句，bingArgs为操作传递参数
     */
    public ArrayList<HashMap<String, String>> querySQLite(String sql, String[] bindArgs) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        //Cursor是结果集游标，使用Cursou.moveToNext()方法可以从当前行移动到下一行
        Cursor cursor = sqLiteDatabase.rawQuery(sql, bindArgs);
        int clos_len = cursor.getColumnCount(); //获取数据所有列数
        Log.d("DBManager", "querySQLite中获得的总列数是" + clos_len);
        boolean isfals = cursor.moveToNext();
        Log.d("DBManager", "isfals值为" + isfals);
        //循环表格中的每一行
        while (cursor.moveToNext()) {
            Log.d(TAG, "进入到while循环中");
            HashMap<String, String> map = new HashMap<>();
            for (int i = 0; i < clos_len; i++) {
                String clos_name = cursor.getColumnName(i);
                //返回指定的名称，没有就返回-1
                String clos_value = cursor.getString(cursor.getColumnIndex(clos_name));
                if (clos_value == null) {
                    clos_value = "";
                }
                Log.d(TAG, "while循环下面的for循环拿到的数据clos_value为：" + cursor.getString(cursor.getColumnIndex(clos_name)));
                map.put(clos_name, clos_value);
            }
            list.add(map);
        }
        return list;
    }

    /**
     * 查询全部数据
     */
    public ArrayList<Devices> queryAllDevicesData() {
        //查询全部数据
        Cursor cursor = sqLiteDatabase.query("devices", null, null, null, null, null, null, null);
        ArrayList<Devices> list = new ArrayList<Devices>();
        if (cursor.getCount() > 0) {
            //移动到首位
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String d_id = cursor.getString(cursor.getColumnIndex("zkid"));
                String ping = cursor.getString(cursor.getColumnIndex("pin"));
                String version = cursor.getString(cursor.getColumnIndex("zkversion"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String operator = cursor.getString(cursor.getColumnIndex("operator"));

                String type=cursor.getString(cursor.getColumnIndex("zktype"));
                String firm =cursor.getString(cursor.getColumnIndex("zkfactory"));
                String issynchr=cursor.getString(cursor.getColumnIndex("issynchr"));
                String qrcode=cursor.getString(cursor.getColumnIndex("qrcode"));

                Devices devices = new Devices();
                devices.setZkId(d_id);
                devices.setPin(ping);
                devices.setZkVersion(version);
                devices.setTime(time);
                devices.setOperator(operator);
                devices.setZkType(type);
                devices.setZkFactory(firm);
                devices.setIssynchr(issynchr);
                devices.setQrcode(qrcode);
                list.add(devices);
                //移动到下一位
                cursor.moveToNext();
            }
        }
        cursor.close();
        return list;
    }

    /**
     * 查询没有被同步的网关数据
     */
    public ArrayList<Devices> selectDevicesIsNotSyn(String  issynchr) {
        String[]columns={"zkid","pin","zkversion","time","operator","zktype","zkfactory"};
        String [] selectionArgs={issynchr};
        Cursor cursor = sqLiteDatabase.query("devices", columns, "issynchr"+"=?", selectionArgs, null, null, null, null);
        ArrayList<Devices> list = new ArrayList<Devices>();
        if (cursor.getCount() > 0) {
            //移动到首位
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String d_id = cursor.getString(cursor.getColumnIndex("zkid"));
                String ping = cursor.getString(cursor.getColumnIndex("pin"));
                String version = cursor.getString(cursor.getColumnIndex("zkversion"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String operator = cursor.getString(cursor.getColumnIndex("operator"));
                Devices devices = new Devices();
                devices.setZkId(d_id);
                devices.setPin(ping);
                devices.setZkVersion(version);
                devices.setTime(time);
                devices.setOperator(operator);
                list.add(devices);
                //移动到下一位
                cursor.moveToNext();
            }
        }
        cursor.close();
        return list;
    }

    /**
     * 查询是否被同步的是否关联过二维码的网关数据
     */
    public ArrayList<Devices> selectDevicesIsnotSynAndlinkQrcode(String  where1,String where2) {
        String[]columns={"zkid","pin","zkversion","time","operator","zktype","zkfactory","issynchr","qrcode"};
        String [] selectionArgs={where1,where2};
        Cursor cursor = sqLiteDatabase.query("devices", columns, "issynchr"+"=?"+"and isqrcode =?", selectionArgs, null, null, null, null);
        ArrayList<Devices> list = new ArrayList<Devices>();
        if (cursor.getCount() > 0) {
            //移动到首位
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String d_id = cursor.getString(cursor.getColumnIndex("zkid"));
                String ping = cursor.getString(cursor.getColumnIndex("pin"));
                String version = cursor.getString(cursor.getColumnIndex("zkversion"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String operator = cursor.getString(cursor.getColumnIndex("operator"));
                String issynchr=cursor.getString(cursor.getColumnIndex("issynchr"));
                String qrcode=cursor.getString(cursor.getColumnIndex("qrcode"));
                Devices devices = new Devices();
                devices.setZkId(d_id);
                devices.setPin(ping);
                devices.setZkVersion(version);
                devices.setTime(time);
                devices.setOperator(operator);
                devices.setIssynchr(issynchr);
                devices.setQrcode(qrcode);
                list.add(devices);
                //移动到下一位
                cursor.moveToNext();
            }
        }
        cursor.close();
        return list;
    }

}
