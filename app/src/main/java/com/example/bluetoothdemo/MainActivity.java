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
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

//    public static final UUID CONNECTION_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final UUID CONNECTION_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "MainActivity";
    private static final int REQUEST_BLUE_ENABLE = 0;
    private static final int REQUEST_PERMITE_CODE = 1;
    private String[] premits ={Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN
            ,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
    private List<String> mCurrentPermits = new ArrayList<>();
    private BlueControl mBlueControl;
    private BlueRvAdapter mAdapter;


    private Handler mHandler = new Handler();
    private BluetoothDevice mBoundDevice ;
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private Set<BluetoothDevice> mDviceSet = new HashSet<>();
    private RecyclerView mBlueRv;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.tranditon_connect:
                //TODO:作为客户端 --> 建立连接

                break;
            case R.id.trandition_send:
                break;
            case R.id.trandition_duan_kai:

                break;
            case R.id.trandition_server:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mBlueControl.startService();
                    }
                }).start();

                break;
            case R.id.trandition_stop_listener:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPrmits();

        initView();

        //监听蓝牙开+关的广播
        IntentFilter stateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED); //蓝牙状态监听广播
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//监听设备绑定状态
//        stateFilter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
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


                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mBlueControl.doConnection(device);
                                Log.d(TAG,"要连接的设备 --> "+device.getName()+":"+device.getAddress());
                            }
                        }).start();
                    }
                },8000);

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


    //Ble蓝牙配对
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState== BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG,"Ble 蓝牙连接成功");

                //Ble连接后  --> 开始扫描服务  -->回调ServiceDiscovered
                boolean isService = gatt.discoverServices();
                Log.d(TAG,"是否开始扫描服务 --> "+isService);


            }else if (newState == BluetoothGatt.STATE_DISCONNECTED){
                Log.d(TAG,"连接断开");
            }
            Log.d(TAG,"newState --> "+newState+":"+status);
        }

        /*
                 0:00001801-0000-1000-8000-00805f9b34fb
                 0:00001800-0000-1000-8000-00805f9b34fb
                 0:0000180a-0000-1000-8000-00805f9b34fb
                 0:0000180f-0000-1000-8000-00805f9b34fb
                 0:00001812-0000-1000-8000-00805f9b34fb
                 0:6e40ff01-b5a3-f393-e0a9-e50e24dcca9e */

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                Log.d(TAG,"service --> "+service.getType()+":"+service.getUuid());
            }

            //开始通信 先获取UUID对应的服务
            BluetoothGattService serviceByUid = gatt.getService(UUID.fromString("00001801-0000-1000-8000-00805f9b34fb"));
            //对蓝牙进行读写操作 --> 通过获取characteris的对应的Uuid  --> 回调给
            BluetoothGattCharacteristic notifyCharcter = serviceByUid.getCharacteristic(UUID.fromString("notify uuid"));
            BluetoothGattCharacteristic writeCharcter = serviceByUid.getCharacteristic(UUID.fromString("write uuid"));

            //开始监听数据  通过descriptor --> 回调给onDescriptorWrite
            gatt.setCharacteristicNotification(notifyCharcter,true);
            //获取charcter中的描述 --> 不知道是否通过这个角色
            BluetoothGattDescriptor descriptor = notifyCharcter.getDescriptor(UUID.fromString("00001801-0000-1000-8000-00805f9b34fb"));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE); //标志 启用通知

            //开始写数据 --> 回调给 onCharacteristicWrite
            writeCharcter.setValue("测试数据");//其中的内容由协议规定
            gatt.writeCharacteristic(writeCharcter);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status==BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG,"发送成功");
            }
            super.onCharacteristicWrite(gatt,characteristic,status);
        }

        //监听数据成功 --> 则会回调这里
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status== BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG,"开启监听成功");
            }

        }

        //服务器返回的数据
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();
            Log.d(TAG,"回复的数据 ---> "+new String(value,0,value.length));

            //断开连接
            gatt.disconnect();
            gatt.close();
        }
    };


    //开启BLE扫描  必须先让蓝牙可见 --> 扫描
    public void bleDiscoverBlue(View view) {
        mBlueControl.bleDiscoveryBlue();

        List<BluetoothDevice> bleDevice = mBlueControl.getBleDevice();
        mAdapter.setData(bleDevice);

        //绑定Ble蓝牙
        mAdapter.setOnBlueDeviceClickListener(new BlueRvAdapter.onBlueDeviceClickListener() {
            @Override
            public void onBlueDeviceClick(BluetoothDevice device) {
                device.connectGatt(MainActivity.this,false,mGattCallback);
                Toast.makeText(MainActivity.this, "点击了Ble设备", Toast.LENGTH_SHORT).show();
            }
        });
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