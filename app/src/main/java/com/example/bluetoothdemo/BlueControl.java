package com.example.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public
/**
 * 作者：zzx on 2020/11/8 13:40
 *  作用： xxxx
 */
class BlueControl {

    private static final String TAG = "BlueControl";
    private final BluetoothAdapter mAdapter;
    private final BluetoothLeScanner mScanner;
    private Map<String,String> mBleScanMap = new HashMap<>();


    public BlueControl() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanner = mAdapter.getBluetoothLeScanner(); //Ble蓝牙扫描对象
    }

    public boolean isEnable(){
        return mAdapter==null?false:true; //空不支持 不空则支持
    }

    public void startBlue(Activity activity,int requestCode){
        Intent startIntent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//启动蓝牙
        activity.startActivityForResult(startIntent,requestCode);
    }

    public void closeBlue(){
        mAdapter.disable();
    }

    public Set<BluetoothDevice> getBlueDevice(){
        return mAdapter.getBondedDevices();
    }

    public boolean tranditionalDiscoverBlue(){  //设备发现  --> 可见 传统蓝牙扫描  --> 无法响应Ble的广播
        return mAdapter.startDiscovery(); //true 找到了  false找不到
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String name = result.getDevice().getName();
            String address = result.getDevice().getAddress();
            mBleScanMap.put(address,name);
           // Log.d(TAG,"new ble --> "+address+"--> "+name);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG,"old size --> "+results.size());
            for (ScanResult result : results) {
                Log.d(TAG,"old result --> "+result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    //低功耗蓝牙扫描
    public void bleDiscoveryBlue(){
        ScanSettings.Builder scanSettings = new ScanSettings.Builder();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                   scanSettings .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                    .setReportDelay(0);
        }
        ScanSettings build = scanSettings.build();
        mScanner.startScan(null,build, mScanCallback);

        Iterator<Map.Entry<String, String>> iterator = mBleScanMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> next = iterator.next();
            String key = next.getKey();
            String value = next.getValue();
            Log.d(TAG,key+"--> "+value);
        }


    }

    public void openSeeBlue(Context context,int durtion){
        Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);//设置蓝牙可见 --> 让别的设备能够搜索到
        discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,durtion);//设置可见性的时间
        context.startActivity(discoverIntent);

    }



    public boolean getBlueState(){
        return mAdapter.isEnabled();
    }

    public void stopBlueDiscovery() {
        mScanner.stopScan(mScanCallback); //开始和停止必须callback一样 否则无法停止
    }

    public void stopTranditonalBlueSearch() {
        mAdapter.cancelDiscovery();
    }

    /*
      有问题
     */
    public boolean releaseBoundBlue(BluetoothDevice device) {
        Class<BluetoothDevice> bluetoothDeviceClass = BluetoothDevice.class;
        Method removeBond = null;
        try {
            removeBond = bluetoothDeviceClass.getMethod("removeBond");
            removeBond.setAccessible(true);
            return (boolean)removeBond.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}

