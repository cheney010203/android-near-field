package com.example.bluetoothhiddemo.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.bluetoothhiddemo.tools.HidConsts;

import java.util.concurrent.Executors;

public class HidUtils {
    public static final String TAG = "Hid-Utils";
    public static boolean _connected = false;
    public static ConnectionStateChangeListener connectionStateChangeListener;

    static BluetoothProfile bluetoothProfile;
    public static BluetoothDevice mDevice;
    static BluetoothHidDevice mHidDevice;

    private static BluetoothAdapter mBluetoothAdapter = null;

    private static String mStrBtAddress = null;

    public interface ConnectionStateChangeListener {
        void onConnecting();

        void onConnected();

        void onDisConnected();
    }

    public static void registerApp(Context context) {

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.setName("ATC HID BT");
            bluetoothAdapter.getProfileProxy(context, mProfileServiceListener, BluetoothProfile.HID_DEVICE);

    }

    public static void setStrBtAddress(String btAddress){
        mStrBtAddress = btAddress;
    }

    public static void setBluetoothAdapter(BluetoothAdapter bluetoothManager){
        mBluetoothAdapter = bluetoothManager;
    }

    public static boolean isConnected() {
        return HidUtils._connected;
    }

    private static void isConnected(boolean _connected) {
        HidUtils._connected = _connected;
    }

    public static BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceDisconnected(int profile) {
            Log.e(TAG, "hid onServiceDisconnected");
            if (profile == BluetoothProfile.HID_DEVICE) {
                mHidDevice.unregisterApp();
            }
        }

        @SuppressLint("NewApi")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.e(TAG, "hid onServiceConnected");
            bluetoothProfile = proxy;
            if (profile == BluetoothProfile.HID_DEVICE) {
                mHidDevice = (BluetoothHidDevice) proxy;
                HidConsts.HidDevice = mHidDevice;
                BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(HidConsts.NAME, HidConsts.DESCRIPTION, HidConsts.PROVIDER, BluetoothHidDevice.SUBCLASS1_COMBO, HidConsts.Descriptor);
                mHidDevice.registerApp(sdp, null, null, Executors.newCachedThreadPool(), mCallback);
            }
        }
    };
    public static final BluetoothHidDevice.Callback mCallback = new BluetoothHidDevice.Callback() {
        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            // 注册成功回调，开启蓝牙连接
            Log.e(TAG, "onAppStatusChanged: " + registered);
            if (!TextUtils.isEmpty(mStrBtAddress)&&mBluetoothAdapter!=null) {
                mHidDevice.connect(mBluetoothAdapter.getRemoteDevice(mStrBtAddress));
            }
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            Log.e(TAG, "onConnectionStateChanged:" + state);
            if (state == BluetoothProfile.STATE_DISCONNECTED) {
                HidUtils.isConnected(false);
                if (connectionStateChangeListener != null) {
                    connectionStateChangeListener.onDisConnected();
                    mDevice = null;
                }
            } else if (state == BluetoothProfile.STATE_CONNECTED) {
                HidUtils.isConnected(true);
                mDevice = device;
                if (connectionStateChangeListener != null) {
                    connectionStateChangeListener.onConnected();
                }
            } else if (state == BluetoothProfile.STATE_CONNECTING) {
                if (connectionStateChangeListener != null) {
                    connectionStateChangeListener.onConnecting();
                }
            }
        }
    };
}
