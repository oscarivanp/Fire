package com.rmasc.fireroad.Entities;

/**
 * Created by ADMIN on 15/02/2016.
 */
public class DeviceBluetooth {

    public String Name;
    public String Mac;

    public DeviceData DataReceived;

    public DeviceBluetooth()
    {
        Name = Mac = "";
        DataReceived = new DeviceData();
    }
}
