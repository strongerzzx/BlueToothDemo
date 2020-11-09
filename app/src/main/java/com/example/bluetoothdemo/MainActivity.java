package com.example.bluetoothdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_BLUE_ENABLE = 0;
    private static final int REQUEST_PERMITE_CODE = 1;
    private String[] premits ={Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN
            ,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
    private List<String> mCurrentPermits = new ArrayList<>();
    private BlueControl mBlueControl;
    private BlueRvAdapter mAdapter;

    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private Set<BluetoothDevice> mDviceSet = new HashSet<>();
    private RecyclerView mBlueRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPrmits();

        initView();

        //监听蓝牙开+关的广播
        IntentFilter stateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED); //蓝牙状态监听广播
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//监听设备绑定状态
        stateFilter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
        registerReceiver(blueStateReceiver,stateFilter);

        //搜索 + 可见广播
        IntentFilter discoverFilter = new IntentFilter();
        discoverFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始搜索
        discoverFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//结束搜索
        discoverFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//设备扫描模式改变 --> 被可见了有广播  无可见了又有个广播
        discoverFilter.addAction(BluetoothDevice.ACTION_FOUND); //传统蓝牙扫描到的数据
        registerReceiver(blueDiscovery,discoverFilter);

        mBlueControl = new BlueControl();

        initBoundBlue();

    }

    private void initBoundBlue() {
        mAdapter.setOnBlueDeviceClickListener(new BlueRvAdapter.onBlueDeviceClickListener() {
            @Override
            public void onBlueDeviceClick(BluetoothDevice device) {
                boolean bond = device.createBond();
                Toast.makeText(MainActivity.this, "绑定状态 --> "+bond, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {
        mAdapter = new BlueRvAdapter();
        mBlueRv = findViewById(R.id.blue_rv);
        mBlueRv.setLayoutManager(new LinearLayoutManager(this));
        mBlueRv.setAdapter(mAdapter);

        mBlueRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top=5;
                outRect.bottom=5;
            }
        });
    }


    private void initPrmits() {
        mCurrentPermits.clear();
        for (int i = 0; i < premits.length; i++) {
            if (ContextCompat.checkSelfPermission(this,premits[i])!=PackageManager.PERMISSION_GRANTED){
                mCurrentPermits.add(premits[i]);
            }
        }
        if (mCurrentPermits.size()>0) {
            ActivityCompat.requestPermissions(this,premits,REQUEST_PERMITE_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMITE_CODE:
                if (grantResults.length>0) {
                    for (int result : grantResults) {
                        if (result== PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG,"权限申请成功");
                        }else{
                            Toast.makeText(this, "权限申请失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_BLUE_ENABLE:
                if (resultCode==RESULT_OK){
                    Log.d(TAG,"打开成功");
                }else {
                    Log.d(TAG,"打开失败");
                }
                break;
        }
    }

    public void enableBlue(View view) {
        boolean enable = mBlueControl.isEnable();
        Log.d(TAG,"是否支持蓝牙 --->"+enable);
    }

    public void startBlue(View view) {
        mBlueControl.startBlue(this,REQUEST_BLUE_ENABLE);
    }

    public void stopBlue(View view) {
        mBlueControl.closeBlue();
    }

    public void getBluePedui(View view) {
        Set<BluetoothDevice> blueDevice = mBlueControl.getBlueDevice();
        for (BluetoothDevice device : blueDevice) {
            Log.d(TAG,device.getName()+"--> "+device.getAddress());
        }
    }

    public void tranditionalDiscoverBlue(View view) { //传统蓝牙扫描 需要配合广播  --> 需要把设备设置为可见才行
        boolean search = mBlueControl.tranditionalDiscoverBlue();
        Log.d(TAG,"是否搜索到蓝牙 --> "+search);
    }

    public void getBlueState(View view) {
        boolean blueState = mBlueControl.getBlueState();
        Log.d(TAG,"蓝牙状态 -->"+blueState);
    }

    public void openSeeBlue(View view) {
        mBlueControl.openSeeBlue(this,200);
    }

    /*
      写个广播  --> 监听蓝牙状态
     */
    private BroadcastReceiver blueStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取蓝牙状态
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d(TAG,"蓝牙正在关闭");
                    break;
                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG,"蓝牙已经关闭");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d(TAG,"蓝牙正在打开");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG,"蓝牙已打开");
                    break;
                default:
                    Log.d(TAG,"；蓝牙出现异常");
                    break;
            }
            int boundState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            switch (boundState) {
                case BluetoothDevice.BOND_BONDED:
                    Toast.makeText(context, "绑定成功", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"绑定成功");
                    break;
                case BluetoothDevice.BOND_NONE:
                    Toast.makeText(context, "未绑定", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothDevice.BOND_BONDING:
                    Toast.makeText(context, "正在绑定", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, "绑定出现错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    /*
       传统蓝牙广播
     */
    private BroadcastReceiver blueDiscovery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                setProgressBarIndeterminate(true);
                Log.d(TAG,"正在搜索");
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                setProgressBarIndeterminate(false);
                Log.d(TAG,"搜索结束");
            }else if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); //传统蓝牙的参数
                mDeviceList.add(device);
                mDviceSet.add(device);
                mAdapter.setData(mDeviceList);
                Log.d(TAG,"传统蓝牙搜索到的蓝牙 --> "+device.getName()+":"+device.getAddress());
            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)){ //这个action里包含 2个 ， 可见+不可见
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                if (mode==BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    setProgressBarIndeterminate(true);
                    Log.d(TAG,"可发现的");
                }else{
                    setProgressBarIndeterminate(false);
                    Log.d(TAG,"发现结束");
                }
            }
        }
    };

    //开启BLE扫描  必须先让蓝牙可见 --> 扫描
    public void bleDiscoverBlue(View view) {
        mBlueControl.bleDiscoveryBlue();
    }

    //结束Ble扫描
    public void stopBleDiscoverBlue(View view) {
        mBlueControl.stopBlueDiscovery();
    }

    public void stopTranditionalDiscoverBlue(View view) {
        mBlueControl.stopTranditonalBlueSearch();
    }

    public void realeaseBondBlue(View view) {

        mAdapter.setOnBlueDeviceClickListener(new BlueRvAdapter.onBlueDeviceClickListener() {
            @Override
            public void onBlueDeviceClick(BluetoothDevice device) {
                boolean isRelease = mBlueControl.releaseBoundBlue(device);
                Log.d(TAG,"是否断开连接--> "+isRelease);
            }
        });
    }
}