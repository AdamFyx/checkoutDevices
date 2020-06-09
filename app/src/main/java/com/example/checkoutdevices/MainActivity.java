package com.example.checkoutdevices;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.checkoutdevices.Activity.ManagerActivity;
import com.example.checkoutdevices.Bean.User;
import com.example.checkoutdevices.Utils.NetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String jsessionid;
    public static List<String> setCookies;
    EditText nameText;
    EditText pwdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button loginBtn = findViewById(R.id.loginbtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                nameText = findViewById(R.id.username);
                pwdText = findViewById(R.id.userpwd);
                if (nameText.getText().toString().equals("")) {
                    nameText.setHint("请输入账号");
                    return;
                }
                if (pwdText.getText().toString().equals("")) {
                    pwdText.setHint("请输入密码");
                    return;
                }
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        init(nameText.getText().toString(), pwdText.getText().toString());
                    }
                });
                t.start();
            }
        });
    }

    private void init(String name, String pwd) {
        String urlPath = "http://192.168.0.109:8080/user/login";
        URL url;
        int id = 0;
        try {
            url = new URL(urlPath);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", name);//把参数put到json串里
            jsonObject.put("password", pwd);
            //json串转string类型
            String content = String.valueOf(jsonObject);
            //开启连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("ser-Agent", "Fidder");
            conn.setRequestProperty("Content-Type", "application/json");
            //写输出流，将要转的参数写入流里
            OutputStream os = conn.getOutputStream();
            os.write(content.getBytes());//字符串写入二进流
            os.close();
            ;
            int code = conn.getResponseCode();
            if (code == 200) {//与后台交互成功返回200
                //读取返回的json数据
                // Toast.makeText (getApplicationContext(),"成功", Toast.LENGTH_LONG ).show();
                Log.d("MainActivity", "数据提交成功");
                InputStream inputStream = conn.getInputStream();
                //session
                Map<String, List<String>> cookies = conn.getHeaderFields();
                setCookies = cookies.get("Set-Cookie");

                //调用自已写的NetUtils将流转成String 类型
                String json = NetUtils.readString(inputStream);
                JSONObject jsonObject1 = new JSONObject(json);
                String msg = jsonObject1.getString("msg");
                if (msg.equals("success")) {
                    pwdText.setText("");
                    nameText.setText("");


                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ManagerActivity.class);
                    intent.putExtra("username", name);
                    intent.putExtra("password", pwd);
                    intent.putExtra("jsessionid", jsessionid);
                    startActivity(intent);
                } else {
                    AlertDialog alertDialog2 = new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("账号或者密码错误请联系请重新输入")
                            .setIcon(R.mipmap.ic_launcher)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create();
                    alertDialog2.show();
                }

            } else {
                // Toast.makeText (getApplicationContext(),"数据提交失败", Toast.LENGTH_LONG ).show();
                Log.d("MainActivity", "数据提交失败");
            }
        } catch (MalformedURLException | JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
