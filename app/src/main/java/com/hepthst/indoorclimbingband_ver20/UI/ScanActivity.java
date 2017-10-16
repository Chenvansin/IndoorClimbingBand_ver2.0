package com.hepthst.indoorclimbingband_ver20.UI;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.hepthst.indoorclimbingband_ver20.Action.LeService;
import com.hepthst.indoorclimbingband_ver20.Model.UserInfo;
import com.hepthst.indoorclimbingband_ver20.R;

import static android.content.ContentValues.TAG;

/**
 * Created by hepthSt on 2017/10/12.
 */

public class ScanActivity extends Activity{

    private Intent serviceIntent;
    private ImageButton mScan;
    private Button mBond;
    private Button mConfig;
    private TextView mStepTV;
    private TextView mBatteryInfoTV;
    private TextView mDeviceTV;
    private TextView mDisplayStateTV;
    private TextView mHeartRateTV;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver onReceive");
            if (intent.getAction().equals("state"))
            {
                //get Intent name used for next operation of value
                if (intent.getStringExtra("state").equals("0")) {//断开连接
                    //get value of intent
                    mDisplayStateTV.append("断开连接\n");
                    updateConnectionStateUI(false);
                }
                if (intent.getStringExtra("state").equals("1")) {//连接成功
                    mDisplayStateTV.append("连接到目标设备\n");
                    updateConnectionStateUI(true);
                } else if (intent.getStringExtra("state").equals("3")) {//扫描超时
                    mDisplayStateTV.append("扫描超时，重新扫描\n");
                } else if (intent.getStringExtra("state").equals("4")) {//开启实时计步通知
                    mDisplayStateTV.append("开始计步\n");
                } else if (intent.getStringExtra("state").equals("6")) {//开启已配对通知
                    mDisplayStateTV.append("目标设备已配对\n");
                } else if (intent.getStringExtra("state").equals("8")) {//开启已配对通知
                    mDisplayStateTV.append("开始心率扫描...\n");
                }  else if (intent.getStringExtra("state").equals("9")) {//开启已配对通知
                    mDisplayStateTV.append("心率扫描结束...\n");
                } else if (intent.getStringExtra("state").equals("10")) {//开启已配对通知
//                    mDisplayStateTV.append("用户信息确认结束...\n");
//                    mDisplayStateTV.append("请开启心率监听...\n");
                    Toast.makeText(ScanActivity.this,"登录完成",Toast.LENGTH_SHORT).show();
                } else if (intent.getStringExtra("state").equals("11")) {//开启已配对通知
                    mDisplayStateTV.append("心率监听已开启...\n");
                } else if (intent.getStringExtra("state").equals("7")) {//开启Notify后使用scan
//                    mDisplayStateTV.append("心率扫描前，请录入用户信息\n");
                }
                else {
                    String deviceAddress = intent.getStringExtra("state");
                    mDisplayStateTV.append("扫描到目标设备： " + deviceAddress + "\n");
                    //Bond Device...
                        int result = mService.bondTarget();
                        if (result == 1) {
                            mDisplayStateTV.append("目标设备已绑定 " + "\n");
                            mService.connectToGatt();
                            Toast.makeText(ScanActivity.this,"设备已连接...",Toast.LENGTH_SHORT).show();
                        } else if (result == -1) {
                            mDisplayStateTV.append("绑定失败 " + "\n");
                        }
                }
            }
            else if (intent.getAction().equals("step")) {
                mStepTV.setText(intent.getStringExtra("step"));
            }
            else if (intent.getAction().equals("battery")) {
                mBatteryInfoTV.setText(intent.getStringExtra("battery"));
            }
            else if (intent.getAction().equals("heartbeats")) {
                Toast.makeText(ScanActivity.this,"心率测试完成",Toast.LENGTH_SHORT).show();
                mHeartRateTV.setText(intent.getStringExtra("heartbeats"));}
            else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1 ) == BluetoothDevice.BOND_BONDED){
                    mDisplayStateTV.append("绑定目标设备 " + "\n");
                }
            }
        }
    };


    LeService.LocalBinder mService;
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mService = (LeService.LocalBinder) service;
            if (mService != null) {
                initBluetooth();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mScan = (ImageButton) findViewById(R.id.BtnConfig);
        mDisplayStateTV = (TextView) findViewById(R.id.tvDisplayConnect);
        mBatteryInfoTV = (TextView) findViewById(R.id.tvBattery_Detail);
        mStepTV =(TextView) findViewById(R.id.tvRealTimeStep_Detail);
        mDeviceTV = (TextView) findViewById(R.id.tvDevice_Detail);
        mDisplayStateTV.setMovementMethod(new ScrollingMovementMethod());
        mHeartRateTV = (TextView) findViewById(R.id.tvHeartRate_Detail);
        serviceIntent = new Intent(this, LeService.class);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
        protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void HandleClickEvent(View v){
        if (v.getId() == R.id.BtnConfig){
            Toast.makeText(ScanActivity.this,"开始BLE设置...",Toast.LENGTH_SHORT).show();
            mService.startLeScan();
        }

        if (v.getId() == R.id.BtnLogin){
            Toast.makeText(ScanActivity.this,"正在登录...",Toast.LENGTH_SHORT).show();
            UserInfo userInfo = new UserInfo(20271234, 1, 32, 160, 40, "Ychepth", 0);
            mService.setUserInfo(userInfo);
        }

        if (v.getId() == R.id.BtnHeartRate){
            Toast.makeText(ScanActivity.this,"心率测试中...",Toast.LENGTH_LONG).show();
            mService.setHeartRateScanListener();
            mService.startHeartRateScan();
        }

        if (v.getId() == R.id.BtnAction){
            startActivity(new Intent(ScanActivity.this,DisplayActivity.class));
        }
    }

    private void initBluetooth() {
        boolean bluetoothStatte = mService.initBluetooth();
        if (bluetoothStatte == false)
        {
            mDisplayStateTV.append("您的设备不支持蓝牙！\n");
        } else {
            boolean leScannerState = mService.initLeScanner();
            if (leScannerState == true) {
                mDisplayStateTV.append("蓝牙已就绪！\n");
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("state");
        intentFilter.addAction("step");
        intentFilter.addAction("battery");
        intentFilter.addAction("heartbeats");
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        return intentFilter;
    }

    private void updateConnectionStateUI(boolean enable) {

        String deviceName = enable ? ("MI1S") : ("未连接");
        mDeviceTV.setText(deviceName);
        mBatteryInfoTV.setText("0|100");
        mStepTV.setText("0");
        mHeartRateTV.setText("0");
    }

}
