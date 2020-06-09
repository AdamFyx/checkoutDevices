package com.example.checkoutdevices.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    /**
     * 保存对象
     * @param context
     * @param key
     * @param value
     */
    public static  void  saveStr(Context context, String key, String value){
        //得到SharedPreferences对象
        SharedPreferences cookie = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        //得到Editor对象
        SharedPreferences.Editor edit = cookie.edit();
        //放入数据
        edit.putString(key,value);
        edit.commit();

    }
    /**
     * 获取对象
     * @param context
     * @param key
     * @return
     */
    public static String getStr(Context context,String key){
        //得到SharedPreferences对象
        SharedPreferences cookie = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        String cookieString = cookie.getString(key, "");
        return cookieString;
    }
}