package com.example.checkoutdevices.Utils;

import android.util.LongSparseArray;

import com.boneylink.sxiotsdk.database.beans.SXCacheGateway;
import com.boneylink.sxiotsdk.database.beans.SXDeviceInfo;
import com.boneylink.sxiotsdk.database.beans.SXGateWayInfo;
import com.boneylink.sxiotsdk.listener.SXOnStatusChangeListener;
import com.example.checkoutdevices.Bean.EventDevsStatusChange;
import com.example.checkoutdevices.Bean.EventGatewayChange;
import com.example.checkoutdevices.Bean.EventSearchGateway;
import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

public class OnSDKStatusChange implements SXOnStatusChangeListener {

    private static final String TAG = OnSDKStatusChange.class.getSimpleName();
//
//    @Override
//    public void handleGatewayStateChange(CacheGateway cacheGateway) {
//        //网关状态变化
//        EventBus.getDefault().post(new EventGatewayChange(cacheGateway.getDid()));
//    }
//
//    @Override
//    public void onScanedLocalGateway(List<GateWayInfo> list) {
//        //内网搜索网关回调
//    }
//
//    @Override
//    public void onDevsStatusChange(List<DeviceInfo> list, LongSparseArray<Map<String, Object>> params) {
//        //设备状态变化
//        EventBus.getDefault().post(new EventDevsStatusChange(list, params));
//    }
//
//    @Override
//    public void onDevCtrlError(DeviceInfo deviceInfo, String resultCode, String resultMsg) {
//        //设备控制异常
//    }

    @Override
    public void handleGatewayStateChange(SXCacheGateway sxCacheGateway) {
        EventBus.getDefault().post(new EventGatewayChange(sxCacheGateway.getDid()));
    }

    @Override
    public void onScanedLocalGateway(List<SXGateWayInfo> list) {
        EventBus.getDefault().post(new EventSearchGateway(list));
    }

    @Override
    public void onDevsStatusChange(List<SXDeviceInfo> list, LongSparseArray<Map<String, Object>> params) {
        EventBus.getDefault().post(new EventDevsStatusChange(list, params));
    }

    @Override
    public void onDevCtrlError(SXDeviceInfo sxDeviceInfo, String s, String s1) {
        //设备控制异常
    }
}
