package com.example.checkoutdevices.DB;

import android.content.Context;
import android.util.Log;

import com.example.checkoutdevices.Bean.Devices;

import java.net.CookieHandler;
import java.util.ArrayList;
import java.util.HashMap;

public class SQLFunction {

    static DBHelper helper;

    public static void initTable(Context context) {
        helper = new DBHelper(context);
        helper.getReadableDatabase();
    }

    /**
     * 插入数据
     *
     * @param context
     * @param data
     */
    public static void insert(Context context, Object[] data) {
        Log.d("SQLFunction", "插入到devices表中的：" + data.toString());
        DBManager sqlManager = new DBManager(context);
        helper = new DBHelper(context);
        helper.getWritableDatabase();
        String sql = "insert into devices (zkid,pin,zkversion,time,operator,zktype,zkfactory,issynchr,qrcode,isqrcode) values(?,?,?,?,?,?,?,?,?,?)";
        Object[] bindArgs = data;
        sqlManager.updateSQLite(sql, bindArgs);
    }

    /*
    查询所有的网关信息    */
    public static ArrayList<Devices> selectAllDevices(Context context) {
        DBManager sqlManager = new DBManager(context);
        ArrayList<Devices> list = new ArrayList<>();
        String sql = "select * from devices ";
        list = sqlManager.queryAllDevicesData();
        return list;
    }

    /*
    查询已同步的网关信息    */
    public static ArrayList<Devices> syschrAlready(Context context) {
        DBManager sqlManager = new DBManager(context);
        ArrayList<Devices> list = new ArrayList<Devices>();
        list = sqlManager.selectDevicesIsNotSyn("1");
        return list;
    }

    /*
        查询所有没有被同步的网关信息    */
    public static ArrayList<Devices> selectDevicesIsNotSyn(Context context, String where) {
        DBManager sqlManager = new DBManager(context);
        ArrayList<Devices> list = new ArrayList<>();
        list = sqlManager.selectDevicesIsNotSyn(where);
        return list;
    }

    /**
     * 查询是否绑定二维码是否被同步的数据haveornoQrSyschr
     *
     * @return
     */
    public static ArrayList<Devices> haveornoQrSyschr(Context context, String where1, String where2) {
        DBManager sqlManager = new DBManager(context);
        ArrayList<Devices> list = new ArrayList<>();
        list = sqlManager.selectDevicesIsnotSynAndlinkQrcode(where1, where2);
        return list;
    }

    /*
         查询所有没有被同步的关联过二维码的网关信息    */
    public static ArrayList<Devices> selectDevicesIsnotSynAndlinkQrcode(Context context, String where1, String where2) {
        DBManager sqlManager = new DBManager(context);
        ArrayList<Devices> list = new ArrayList<>();
        list = sqlManager.selectDevicesIsnotSynAndlinkQrcode(where1, where2);
        return list;
    }

    /**
     * 删除数据
     */
    public static void delete(Context context, Object[] data) {
        DBManager sqlManager = new DBManager(context);
        String sql = "delete from devices where _id= ? ";
        sqlManager.updateSQLite(sql, data);
    }

    /**
     * 更新数据把未同步改成已同步
     */
    public static void updateIssynchrBydid(Context context, Object[] data) {
        helper = new DBHelper(context);
        helper.getReadableDatabase();
        DBManager sqlManager = new DBManager(context);
        String sql = "update devices set issynchr=1 where zkid=?";
        sqlManager.updateSQLite(sql, data);
    }

    /**
     * 更新数据未绑定改成已绑定二维码
     */
    public static void updateIsQrcodeBydid(Context context, Object[] data) {
        helper = new DBHelper(context);
        helper.getReadableDatabase();
        DBManager sqlManager = new DBManager(context);
        String sql = "update devices set isqrcode=1 where zkid=?";
        sqlManager.updateSQLite(sql, data);
    }

    /**
     * 根据zkid修改qrcode信息
     */
    public static void updateQrcodeByZkId(Context context, Object[] data) {
        Log.d("SQLFunction", "根据zkid修改qrcode信息：" + data.toString());
        DBManager sqlManager = new DBManager(context);
        helper = new DBHelper(context);
        helper.getWritableDatabase();
        String sql = "update devices set  qrcode=? where zkid=?";
        Object[] bindArgs = data;
        sqlManager.updateSQLite(sql, bindArgs);
    }

    /**
     * 根据id查询网关信息
     */
    public ArrayList<Devices> selectDevicesByzkId(Context context, String zkid) {
        DBManager sqlManager = new DBManager(context);
        helper = new DBHelper(context);
        helper.getWritableDatabase();
        ArrayList<Devices> list = sqlManager.selectDevicesByzkId(zkid);
        return list;
    }

}
