package com.example.checkoutdevices.Bean;

import com.boneylink.sxiotsdk.database.beans.SXDeviceInfo;

import java.util.Map;

public class UIDeviceBean {

    public UIDeviceBean() {
    }

    public UIDeviceBean(SXDeviceInfo deviceInfo, Map<String, Object> devStatus) {
        this.deviceInfo = deviceInfo;
        this.devStatus = devStatus;
    }

    public SXDeviceInfo deviceInfo;
    public Map<String, Object> devStatus;

}
