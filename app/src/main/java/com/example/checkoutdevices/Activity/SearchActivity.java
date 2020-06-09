package com.example.checkoutdevices.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boneylink.sxiotsdk.DataClient;
import com.boneylink.sxiotsdk.DeviceClient;
import com.boneylink.sxiotsdk.SXIoTSDK;
import com.boneylink.sxiotsdk.database.beans.SXCacheGateway;
import com.boneylink.sxiotsdk.database.beans.SXDeviceInfo;
import com.boneylink.sxiotsdk.database.beans.SXGateWayInfo;
import com.boneylink.sxiotsdk.database.beans.SXRoomInfo;
import com.example.checkoutdevices.Bean.Devices;
import com.example.checkoutdevices.Bean.EventDevsStatusChange;
import com.example.checkoutdevices.Bean.EventGatewayChange;
import com.example.checkoutdevices.Bean.EventSearchGateway;
import com.example.checkoutdevices.Bean.UIDeviceBean;
import com.example.checkoutdevices.DB.SQLFunction;
import com.example.checkoutdevices.R;
import com.example.checkoutdevices.Utils.OnSDKStatusChange;
import com.google.gson.Gson;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;

public class SearchActivity extends AppCompatActivity {
    RecyclerView deviceListContainer;
    List<UIDeviceBean> dataList = new ArrayList<>();
    List<SXDeviceInfo> deviceInfoList = new ArrayList<>();
    String opearator = "";
    Button searchGateway;
    ListView lv;
    List<SXGateWayInfo> list = new ArrayList<SXGateWayInfo>();
    String zkid = "";

    MyListAdapter myListAdapter = new MyListAdapter();
    SQLFunction sqlFunction = new SQLFunction();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.search);

        lv = findViewById(R.id.searchlist);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        opearator = bundle.getString("username");

        searchGateway = findViewById(R.id.search_gateway);
        searchGateway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceClient.searchGatewayLocal();
            }
        });
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        List<SXDeviceInfo> sensorDevList = DataClient.getSensorDevList();
        Log.d(SearchActivity.class.getSimpleName(), "有" + sensorDevList.size() + "个传感器");
        List<SXRoomInfo> roomInfoList = DataClient.getRoomInfoList();
        Log.d(SearchActivity.class.getSimpleName(), "有" + roomInfoList.size() + "个房间");
        dataList.clear();
        for (SXRoomInfo roomInfo : roomInfoList) {
            List<SXDeviceInfo> roomDevInfoList = DataClient.getDevListByRoomId(roomInfo.getRoomId());
            Log.d(SearchActivity.class.getSimpleName(), roomInfo.getRoomId() + "房间有" + roomDevInfoList.size() + "个设备");

            deviceInfoList.addAll(roomDevInfoList);
            for (SXDeviceInfo deviceInfo : roomDevInfoList) {
                dataList.add(new UIDeviceBean(deviceInfo, DeviceClient.getStatusCacheById(deviceInfo.getDevice_id())));
            }
        }

        super.onResume();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevStatusChange(EventDevsStatusChange event) {
        LongSparseArray<Map<String, Object>> statusParams = event.statusMap;
        int index = 0;
        for (UIDeviceBean uiDeviceBean : dataList) {
            long deviceId = uiDeviceBean.deviceInfo.getDevice_id();
            if (statusParams.indexOfKey(deviceId) >= 0) {
                uiDeviceBean.devStatus = statusParams.get(deviceId);
            }
            index++;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGatewayChange(EventGatewayChange event) {
        //当网关连接上来后，调用查询接口主动查询设备状态
        SXCacheGateway cacheGateway = DeviceClient.getGatewayStatCache(event.zkId);
        if (cacheGateway.isOnLine()) {
            DeviceClient.queryDeviceStatusByDevList(deviceInfoList);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGatewaySearch(EventSearchGateway event) {
        List<SXGateWayInfo> gatewayInfoList = event.list;
        System.out.println(new Gson().toJson(gatewayInfoList));
        Log.d("SerarchActivity", gatewayInfoList.size() + "");
        list = gatewayInfoList;
        if (gatewayInfoList.size() > 0) {
            for (int i = 0; i < gatewayInfoList.size(); i++) {
                ArrayList<Devices> deviceList = sqlFunction.selectDevicesByzkId(SearchActivity.this, gatewayInfoList.get(i).zkId);
                if (deviceList.size() == 0) {
                    Object[] data = {gatewayInfoList.get(i).zkId, gatewayInfoList.get(i).pin, gatewayInfoList.get(i).zkVersion,
                            getTime(), opearator, gatewayInfoList.get(i).zkType, gatewayInfoList.get(i).zkFactory, "0", "0", "0"};
                    sqlFunction.insert(SearchActivity.this, data);
                } else {
                }
            }
        }
        if (list.size() != 0) {
            Toast.makeText(this, "找到" + list.size() + "个网关", Toast.LENGTH_LONG).show();
            lv.setAdapter(myListAdapter);
            //点击事件
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

        } else {
            Toast.makeText(this, "找到0个网关", Toast.LENGTH_LONG).show();
        }
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

    // 开始扫码
    private void startQrCode() {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(SearchActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(SearchActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(SearchActivity.this, CaptureActivity.class);
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
            Log.d("我是回调我执行到了", "我是回调我执行到了");
            //添加二维码
            sqlFunction.updateQrcodeByZkId(SearchActivity.this, qrdate);
            //修改二维码状态信息
            sqlFunction.updateIsQrcodeBydid(SearchActivity.this, isqrdate);
            //回调扫描的方法
            DeviceClient.searchGatewayLocal();
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
                    Toast.makeText(SearchActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case Constant.REQ_PERM_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 文件读写权限申请
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(SearchActivity.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        return time;
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void startLog() {
    }

    long exitTimestamp;

    @Override
    public void onBackPressed() {
        SearchActivity.this.finish();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.i("ManagerActivity", "返回的view对象，position" + position);
            //得到某个位置对应的person对象
            SXGateWayInfo info = list.get(position);
            //视图填充器：inflate ；这个为内部类，要使用上下文则为  类.this
            View view = View.inflate(SearchActivity.this, R.layout.adapter, null);
            //修改单个单元格的底色
            String qrcode = "";
            ArrayList<Devices> des = sqlFunction.selectDevicesByzkId(SearchActivity.this, info.zkId);
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
            TextView de_ping = (TextView) view.findViewById(R.id.de_ping);
            TextView de_type = (TextView) view.findViewById(R.id.de_type);
            TextView de_version = (TextView) view.findViewById(R.id.de_version);
            TextView de_time = (TextView) view.findViewById(R.id.de_time);
            TextView de_operatoe = (TextView) view.findViewById(R.id.de_operator);
            TextView de_qrcode = (TextView) view.findViewById(R.id.de_qrcode);
            de_id.setText("网关id:" + info.getZkId());
            de_firm.setText("网关厂商:" + info.getZkFactory());
            de_ping.setText("网关ping值:" + info.getPin());
            de_type.setText("网关类型:" + info.getZkType());
            de_version.setText("网关版本号:" + info.getZkVersion());
            de_time.setText("网关录入时间:");
            de_operatoe.setText("网关录入操作员:");
            de_qrcode.setText("我是二维码信息:" + qrcode);

            return view;
        }
    }
}

