package com.rmasc.fireroad.BluetoothLe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by ADMIN on 23/12/2015.
 */
public class BluetoothLE extends Activity {

    public static UUID SERVICE_BATTERY = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static UUID CHARACTERISTIC_BATTERY = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static UUID CHARACTERISTIC_SERIAL = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bleAdapter;
    private BluetoothAdapter.LeScanCallback bleScanCallback;
    public BluetoothGattCallback bleGattCallback;
    public BluetoothGatt bleGatt;
    public ArrayList<BluetoothGattCharacteristic> bleGattCharasteristicList = new ArrayList<BluetoothGattCharacteristic>();
    public BroadcastReceiver bleBroadcastReceiver;
    public ArrayList<BluetoothDevice> bleDevices;
    public ArrayList<BluetoothGattService> bluetoothGattServiceList = new ArrayList<BluetoothGattService>();
    public boolean mScanning;
    private android.os.Handler mHandler = new android.os.Handler();
    public ArrayAdapter mAdapter;
    public String DeviceStatus;


    public BluetoothLE(Context context)
    {
        this.bleAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mAdapter = new ArrayAdapter(context, android.R.layout.simple_selectable_list_item);
        this.bleDevices = new ArrayList<BluetoothDevice>();
        this.bleScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //bleDevices = new ArrayList<BluetoothDevice>();
                        bleDevices.add(device);
                        //mAdapter.clear();
                        mAdapter.add(device.getName());
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        this.bleBroadcastReceiver = new BroadcastReceiver() {
            @Override
                public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                switch (action)
                {
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        DeviceStatus = "Connected";
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        DeviceStatus = "Disconnected";
                        break;
                    default:
                        break;
                }

                if (action.equals("2"))
                    invalidateOptionsMenu();
                else if (action.equals("0"))
                {
                    invalidateOptionsMenu();
                }
            }
        };
    }

    public boolean isCompatible()
    {
        if ( BluetoothAdapter.getDefaultAdapter() == null )
            return false;
        else
            return true;
    }

    public boolean isEnabled()
    {
        if ( bleAdapter != null && !bleAdapter.isEnabled() )
            return false;
        else
            return true;
    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bleAdapter.stopLeScan(bleScanCallback);
                }
            }, 5000);
            mScanning = true;
            bleAdapter.startLeScan(bleScanCallback);
        } else {
            mScanning = false;
            bleAdapter.stopLeScan(bleScanCallback);
        }
    }

    public void  scanLeDevice()
    {
    }

    public void ConnectToGattServer(BluetoothDevice device, boolean autoConnect)
    {
        bleGatt = device.connectGatt(this, autoConnect, bleGattCallback);
        //bleGatt.discoverServices();
    }

}
