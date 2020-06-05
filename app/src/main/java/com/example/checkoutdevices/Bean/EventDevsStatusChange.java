package com.example.checkoutdevices.Bean;

import android.util.LongSparseArray;

import com.boneylink.sxiotsdk.database.beans.SXDeviceInfo;

import java.util.List;
import java.util.Map;

public class EventDevsStatusChange {

    public EventDevsStatusChange() {
    }
    public EventDevsStatusChange(List<SXDeviceInfo> devList, LongSparseArray<Map<String, Object>> statusMap) {
        this.devList = devList;
        this.statusMap = statusMap;
    }

    public List<SXDeviceInfo> devList;
    public LongSparseArray<Map<String, Object>> statusMap;

}
