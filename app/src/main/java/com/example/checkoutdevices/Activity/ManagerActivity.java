package com.example.checkoutdevices.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.boneylink.sxiotsdk.DeviceClient;
import com.boneylink.sxiotsdk.SXIoTSDK;
import com.boneylink.sxiotsdk.database.beans.SXGateWayInfo;
import com.example.checkoutdevices.Bean.Devices;
import com.example.checkoutdevices.Bean.EventSearchGateway;
import com.example.checkoutdevices.DB.SQLFunction;
import com.example.checkoutdevices.MainActivity;
import com.example.checkoutdevices.R;
import com.example.checkoutdevices.Utils.NetUtils;
import com.example.checkoutdevices.Utils.OnSDKStatusChange;
import com.google.gson.Gson;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.activity.SYSActivity;
import com.google.zxing.util.Constant;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ManagerActivity extends AppCompatActivity {
    private TextView view;
    private Spinner spinner;
    private ArrayAdapter adapter;
    private ArrayList<Devices> list;
    ListView lv;
    MyListAdapter myListAdapter = new MyListAdapter();
    private String clickBtn;
    SQLFunction sqlFunction = new SQLFunction();
    String name = "";
    String pwd = "";
    String zkid = "";
    String jsessionid = "";
    String setCookiesInfo = "";
    String flag;
    long exitTimestamp;

    @Override
    public void onBackPressed() {
        int exitOffset = 2000;
        if (exitTimestamp == 0 || (System.currentTimeMillis() - exitTimestamp) > exitOffset) {
            exitTimestamp = System.currentTimeMillis();
            Toast.makeText(this, "再返回一次退出应用", Toast.LENGTH_LONG).show();
        } else {
            System.exit(0);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        //初始化sdk信息
        SXIoTSDK.init(this, "appId123123", "appsecret123123");
        SXIoTSDK.setOnStatusChangeListener(new OnSDKStatusChange());
        /*
        初始化数据库
         */
        SQLFunction.initTable(ManagerActivity.this);

        lv = findViewById(R.id.listview);
        noQrSyschr();
        lv.setAdapter(myListAdapter);
          /*
          初始化信息
         */
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        name = bundle.getString("username");
        jsessionid = bundle.getString("jsessionid");
        pwd = bundle.getString("password");
        TextView nameText = findViewById(R.id.welcome);
        nameText.setTextSize(20);
        nameText.setText("欢迎登陆：" + name);
         /*
         下拉框选择查询什么样的数据
          */
        spinner = (Spinner) findViewById(R.id.spinner);
        view = (TextView) findViewById(R.id.spinnerText);
        //将可选内容与ArrayAdapter连接起来
        adapter = ArrayAdapter.createFromResource(this, R.array.plantes, android.R.layout.simple_spinner_item);
        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter2 添加到spinner中
        spinner.setAdapter(adapter);
        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new SpinnerXMLSelectedListener());
        //设置默认值
        spinner.setVisibility(View.VISIBLE);

        //首页登记页面切换
        Button home = findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout homeLinearLayout = findViewById(R.id.homepage);
                LinearLayout enterLinearLayout = findViewById(R.id.enterpage);
                LinearLayout devicesLinearLayout = findViewById(R.id.devices);
                homeLinearLayout.setVisibility(View.VISIBLE);
                enterLinearLayout.setVisibility(View.GONE);
                devicesLinearLayout.setVisibility(View.GONE);
            }
        });
        Button enter = findViewById(R.id.enter);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("d", "出库登记被点击到了");
                LinearLayout homeLinearLayout = findViewById(R.id.homepage);
                LinearLayout enterLinearLayout = findViewById(R.id.enterpage);
                LinearLayout devicesLinearLayout = findViewById(R.id.devices);
                homeLinearLayout.setVisibility(View.GONE);
                enterLinearLayout.setVisibility(View.VISIBLE);
                devicesLinearLayout.setVisibility(View.VISIBLE);
            }
        });
        /*
        /登出操作
         */
        Button logoutBtn = findViewById(R.id.quit);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        logout();
                    }
                });
                t.start();
            }
        });
        /*
        搜索新网关
         */
        Button search = findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("username", name);
                intent.setClass(ManagerActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        //同步操作
        Button synchr = findViewById(R.id.synchr);
        synchr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //查询本地数据库没有被同步的数据
                final ArrayList<Devices> list = selectDevicesIsnotSyn();
                final ArrayList<Devices> qrlist = selectDevicesIsnotSynAndlinkQrcode();
                if (list.size() == 0) {
                    AlertDialog alertDialog = new AlertDialog.Builder(ManagerActivity.this)
                            .setTitle("提示")
                            .setMessage("当前没有未同步的数据")
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
                    alertDialog.show();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(ManagerActivity.this)
                            .setTitle("提示")
                            .setMessage("搜索到" + list.size() + "条未同步的数据" + "其中有" + qrlist.size() + "条关联过二维码的数据，是否上传至服务器")
                            .setIcon(R.mipmap.ic_launcher)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Thread t = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //将网关数据信息上传至服务器
                                            upload(qrlist);

                                        }
                                    });
                                    t.start();
                                    haveQrSyschr();
                                    myListAdapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            }
        });
        //根据下拉框的不同选择 查询不同的数据
        Button select = findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ManagerActivity", "啊‘’‘’‘’‘’" + clickBtn);
                if (clickBtn.equals("已同步")) {
                    syschrAlready();
                    flag="1";
                    myListAdapter.notifyDataSetChanged();
                }
                if (clickBtn.equals("已绑二维码未同步")) {
                    haveQrSyschr();
                    flag="2";
                    myListAdapter.notifyDataSetChanged();
                }
                if (clickBtn.equals("未绑二维码未同步")) {
                    noQrSyschr();
                    flag="3";
                    myListAdapter.notifyDataSetChanged();
                }
            }
        });
        //listview的单击事件
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("ManagerActivity i的值", i + "");
                lv.showContextMenu();
            }
        });
        //设置ContextMenu,长按listitem时触发
        lv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                //menu.setHeaderTitle("选择操作");
                menu.add(0, 0, 0, "扫一扫");
            }

        });
    }

    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //获取点击的item的id
        int position = info.position;
        switch (item.getItemId()) {
            case 0:
                startQrCode();
                Log.d("我是被点到id", list.get(position).getZkId());
                zkid = list.get(position).getZkId();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    //使用XML形式操作 搜索时的条件
    class SpinnerXMLSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, final int arg2,
                                   long arg3) {
            clickBtn = adapter.getItem(arg2).toString();
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }


    // 开始扫码
    private void startQrCode() {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(ManagerActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(ManagerActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(ManagerActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    //扫码回调结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            final String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            Object[] qrdate = {scanResult, zkid};
            Object[] isqrdate = {zkid};
            //添加二维码
            sqlFunction.updateQrcodeByZkId(ManagerActivity.this, qrdate);
            //修改二维码状态信息
            sqlFunction.updateIsQrcodeBydid(ManagerActivity.this, isqrdate);
            if(flag.equals("1")){
                syschrAlready();
            }
            if(flag.equals("2")){
                haveQrSyschr();
            }
            if(flag.equals("3")){
                noQrSyschr();
            }
            myListAdapter.notifyDataSetChanged();

        }
    }

    //扫码权限设置
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(ManagerActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case Constant.REQ_PERM_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 文件读写权限申请
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(ManagerActivity.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    // 查询所有的网关信息
    public void selectAllDevices() {
        list = new ArrayList<Devices>();
        list = sqlFunction.selectAllDevices(ManagerActivity.this);
        Log.d("LISTSIZE", list.size() + "");
    }

    //已经同步的所有信息
    public void syschrAlready() {
        list = new ArrayList<Devices>();
        list = sqlFunction.syschrAlready(ManagerActivity.this);
        Log.d("LISTSIZE", list.size() + "");
    }

    //已绑二维码未同步
    public void haveQrSyschr() {
        list = new ArrayList<Devices>();
        list = sqlFunction.haveornoQrSyschr(ManagerActivity.this, "0", "1");
        Log.d("LISTSIZE", list.size() + "");
    }

    //未绑二维码未同步
    public void noQrSyschr() {
        list = new ArrayList<Devices>();
        list = sqlFunction.haveornoQrSyschr(ManagerActivity.this, "0", "0");
        Log.d("LISTSIZE", list.size() + "");
    }

    //查询没有被同步的信息
    public ArrayList<Devices> selectDevicesIsnotSyn() {
        String where = "0";
        ArrayList<Devices> list = new ArrayList<Devices>();
        list = sqlFunction.selectDevicesIsNotSyn(ManagerActivity.this, where);
        Log.d("ManagerActivity", list.size() + "");
        return list;
    }

    //查询没有被同步且绑定二维码的信息
    public ArrayList<Devices> selectDevicesIsnotSynAndlinkQrcode() {
        String where1 = "0";
        String where2 = "1";
        ArrayList<Devices> list = new ArrayList<Devices>();
        list = sqlFunction.selectDevicesIsnotSynAndlinkQrcode(ManagerActivity.this, where1, where2);
        Log.d("ManagerActivity", list.size() + "");
        return list;
    }

    //将网关数据上传至服务器
    private void upload(ArrayList<Devices> list) {
        String urlPath = "http://192.168.0.109:8080/gateway/info/upload";
        URL url;
        try {
            url = new URL(urlPath);
            //开启连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("ser-Agent", "Fidder");
            conn.setRequestProperty("Content-Type", "application/json");

            //操作时带上session登录
            List<String> setCookies = MainActivity.setCookies;
            for (int i = 0; i < setCookies.size(); i++) {
                setCookiesInfo = setCookies.get(0).toString();
            }
            conn.setRequestProperty("Cookie", setCookiesInfo);


            JSONObject jsonObject = new JSONObject();
            //json串转string类型
            JSONArray jsonArray = new JSONArray();
            ArrayList<String> array = new ArrayList<String>();
            for (int i = 0; i < list.size(); i++) {
                jsonObject.put("firmwareVersion", list.get(i).getZkVersion());//把参数put到json串里
                jsonObject.put("pinCode", list.get(i).getPin());
                jsonObject.put("qrCode", list.get(i).getQrcode());
                jsonObject.put("zkId", list.get(i).getZkId());
                jsonArray.put(jsonObject);
            }

            String content = String.valueOf(jsonArray);
            //写输出流，将要转的参数写入流里
            OutputStream os = conn.getOutputStream();
            os.write(content.getBytes());//字符串写入二进流
            os.close();
            int code = conn.getResponseCode();
            Log.d("我是上传的code", code + "");
            if (code == 200) {//与后台交互成功返回200
                //读取返回的json数据
                // Toast.makeText (getApplicationContext(),"成功", Toast.LENGTH_LONG ).show();
                Log.d("MainActivity", "数据提交成功");
                InputStream inputStream = conn.getInputStream();
                //调用自已写的NetUtils将流转成String 类型
                String json = NetUtils.readString(inputStream);
                JSONObject jsonObject1 = new JSONObject(json);
                Log.d("managereActivity  info", jsonObject1.toString());
                String msg = jsonObject1.getString("msg");
                JSONArray jsonArray1 = jsonObject1.getJSONArray("data");
                if (msg.equals("success")) {
                    for (int i = 0; i < jsonArray1.length(); i++) {
                        String json1 = jsonArray1.getString(i);
                        Object[] data = {json1};
                        sqlFunction.updateIssynchrBydid(ManagerActivity.this, data);

                    }
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

    /**
     * 登出操作
     */
    private void logout() {
        String urlPath = "http://192.168.0.109:8080/user/logout";
        URL url;
        try {
            url = new URL(urlPath);
            //开启连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");


            //操作时带上session登录
            List<String> setCookies = MainActivity.setCookies;
            for (int i = 0; i < setCookies.size(); i++) {
                setCookiesInfo = setCookies.get(0).toString();
            }
            conn.setRequestProperty("Cookie", setCookiesInfo);

            int code = conn.getResponseCode();
            if (code == 200) {//与后台交互成功返回200
                //读取返回的json数据
                // Toast.makeText (getApplicationContext(),"成功", Toast.LENGTH_LONG ).show();
                Log.d("MainActivity", "数据提交成功");
                InputStream inputStream = conn.getInputStream();
                //调用自已写的NetUtils将流转成String 类型
                String json = NetUtils.readString(inputStream);
                JSONObject jsonObject1 = new JSONObject(json);
                String msg = jsonObject1.getString("msg");
                if (msg.equals("success")) {
                    Log.d("MainActivity", "我退出成功了");
                    System.exit(0);
                } else {
                    Log.d("MainActivity", "我退出失败了");
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

    //适配器
    private class MyListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            int count = list.size(); //条目个数=集合的size
            return count;
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //getView控制每个条目显示的内容,依据position来控制，传进来的位置是什么就把此位置的view对象给他
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.i("ManagerActivity", "返回的view对象，position" + position);
            //得到某个位置对应的person对象
            Devices info = list.get(position);
            Log.d("info",info.toString());
            //视图填充器：inflate ；这个为内部类，要使用上下文则为  类.this
            View view = View.inflate(ManagerActivity.this, R.layout.adapter, null);

            //修改单个单元格的底色
            String qrcode = "";
            ArrayList<Devices> des = sqlFunction.selectDevicesByzkId(ManagerActivity.this, info.getZkId());
            Log.d("des", des.toString());
            for (int i = 0; i < des.size(); i++) {
                qrcode = des.get(0).getQrcode();
            }
            if (qrcode == null || qrcode.equals("0")) {
                Log.d("我是第一个", "我被执行到了");
                view.setBackgroundColor(Color.parseColor("#90FFFFFF"));
            } else {
                Log.d("我是第er个", "我被执行到了");
                view.setBackgroundColor(Color.parseColor("#60000000"));
            }
            //一定要在view对象里面寻找孩子的id
            TextView de_id = (TextView) view.findViewById(R.id.de_id);
            TextView de_firm = (TextView) view.findViewById(R.id.de_firm);
            TextView de_operator = (TextView) view.findViewById(R.id.de_operator);
            TextView de_ping = (TextView) view.findViewById(R.id.de_ping);
            TextView de_type = (TextView) view.findViewById(R.id.de_type);
            TextView de_version = (TextView) view.findViewById(R.id.de_version);
            TextView de_time = (TextView) view.findViewById(R.id.de_time);
            TextView de_qrcode = (TextView) view.findViewById(R.id.de_qrcode);


            de_id.setText("网关id:" + info.getZkId());
            de_firm.setText("网关厂商" + info.getZkFactory());
            de_operator.setText("信息操作员" + info.getOperator());
            de_ping.setText("网关ping值" + info.getPin());
            de_type.setText("网关类型" + info.getZkType());
            de_version.setText("网关版本号" + info.getZkVersion());
            de_time.setText("录入时间" + info.getTime());
            de_qrcode.setText("网关绑定二维码" + info.getQrcode());

            return view;


        }
    }
}
