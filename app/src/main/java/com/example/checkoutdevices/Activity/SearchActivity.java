package com.example.checkoutdevices.Activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boneylink.sxiotsdk.DataClient;
import com.boneylink.sxiotsdk.DeviceClient;
import com.boneylink.sxiotsdk.SXIoTSDK;
import com.boneylink.sxiotsdk.database.beans.SXCacheGateway;
import com.boneylink.sxiotsdk.database.beans.SXDeviceInfo;
import com.boneylink.sxiotsdk.database.beans.SXGateWayInfo;
import com.boneylink.sxiotsdk.database.beans.SXRoomInfo;
import com.example.checkoutdevices.Bean.EventDevsStatusChange;
import com.example.checkoutdevices.Bean.EventGatewayChange;
import com.example.checkoutdevices.Bean.EventSearchGateway;
import com.example.checkoutdevices.Bean.UIDeviceBean;
import com.example.checkoutdevices.R;
import com.example.checkoutdevices.Utils.OnSDKStatusChange;
import com.google.gson.Gson;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;

public class SearchActivity extends AppCompatActivity {
    //CommonTitleBar titleBar;
    RecyclerView deviceListContainer;
    List<UIDeviceBean> dataList = new ArrayList<>();
    List<SXDeviceInfo> deviceInfoList = new ArrayList<>();

    Button searchGateway;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.search);
        SXIoTSDK.init(this, "appId123123", "appsecret123123");
        SXIoTSDK.setOnStatusChangeListener(new OnSDKStatusChange());




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
        for (SXRoomInfo roomInfo: roomInfoList) {
            List<SXDeviceInfo> roomDevInfoList = DataClient.getDevListByRoomId(roomInfo.getRoomId());
            Log.d(SearchActivity.class.getSimpleName(), roomInfo.getRoomId() + "房间有" + roomDevInfoList.size() + "个设备");

            deviceInfoList.addAll(roomDevInfoList);
            for (SXDeviceInfo deviceInfo: roomDevInfoList) {
                dataList.add(new UIDeviceBean(deviceInfo, DeviceClient.getStatusCacheById(deviceInfo.getDevice_id())));
            }
        }

        super.onResume();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevStatusChange(EventDevsStatusChange event) {
        LongSparseArray<Map<String, Object>> statusParams = event.statusMap;
        int index = 0;
        for (UIDeviceBean uiDeviceBean: dataList) {
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
        Toast.makeText(this, "找到" + gatewayInfoList.size() + "个网关", Toast.LENGTH_LONG).show();

    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void startLog() {
    }

    //对这个权限的解释
//    @OnShowRationale({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
//    void showRationaleForCamera(final PermissionRequest request) {
//        new AlertDialog.Builder(this)
//                .setMessage("应用需要访问存储空间")
//                .setPositiveButton("允许", (dialog, button) -> request.proceed())
//                .setNegativeButton("拒绝", (dialog, button) -> request.cancel())
//                .show();
//    }

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
    }

