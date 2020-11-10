package com.example.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    public  void doConnection(BluetoothDevice device) {
        //绑定之后 建立Socket
        OutputStream os = null;
        InputStream is = null;
        try {
            BluetoothSocket client = device.createRfcommSocketToServiceRecord(MainActivity.CONNECTION_UUID);
            boolean connected = client.isConnected();
            Log.d(TAG,"传统蓝牙是否成功建立通信 --> "+connected);
            os = client.getOutputStream();
            Log.d(TAG,":"+os);
            os.write("客户端蓝牙发来的问候".getBytes());
            os.flush();

            //读取服务端 会写的数据
            is = client.getInputStream();
            byte[] bytes = new byte[1024];
            int len=0;
            StringBuffer sb =new StringBuffer();
            while ((len=is.read(bytes))!=-1){
                String s = new String(bytes, 0, len);
                sb.append(s);
            }
            Log.d(TAG,"蓝牙服务器 返回的数据 --> "+sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
//            if (os != null) {
//                try {
//                    os.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

    private Set<BluetoothDevice> mBleSet = new HashSet<BluetoothDevice>();
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            mBleSet.add(device);

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

    /*
       让别人能搜索到你 配合搜索
     */
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

    private List<BluetoothDevice> mBleList = new ArrayList<>();
    public List<BluetoothDevice> getBleDevice() {
        mBleList.addAll(mBleSet);
        return mBleList;
    }

    public void startService() {
        while (true){
            try {
                BluetoothServerSocket serverSocket = mAdapter.listenUsingRfcommWithServiceRecord("蓝牙服务器", MainActivity.CONNECTION_UUID);
                Log.d(TAG,"服务端socket --> "+  serverSocket.toString()+":");
                BluetoothSocket accept = serverSocket.accept();
                boolean connected = accept.isConnected();
                Log.d(TAG,"服务端socket --> 是否建立成功"+connected);
                BluetoothDevice remoteDevice = accept.getRemoteDevice();
                String name = remoteDevice.getName();
                Log.d(TAG,"与服务端建立连接的蓝牙 --> "+name);

                InputStream is = accept.getInputStream();
                byte[] bytes = new byte[1024];
                int len=0;
                StringBuffer sb =new StringBuffer();
                while ((len=is.read(bytes))!=-1){
                    String s = new String(bytes, 0, len);
                    sb.append(s);
                }
                Log.d(TAG,"服务器读取的数据 --> "+sb.toString());


                //服务器写给蓝牙客户端的数据
                OutputStream os = accept.getOutputStream();
                os.write("服务器已收到".getBytes());
                os.flush();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

