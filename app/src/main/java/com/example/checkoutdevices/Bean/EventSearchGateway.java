package com.example.checkoutdevices.Bean;

import com.boneylink.sxiotsdk.database.beans.SXGateWayInfo;

import java.util.List;

public class EventSearchGateway {

    public List<SXGateWayInfo> list;
    public EventSearchGateway(List<SXGateWayInfo> list) {
        this.list = list;
    }

}
