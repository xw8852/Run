package com.android.ivymobi.pedometer.gps;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;

public class BaiduGPSServer extends Service {

    public static final String ACTION = "com.android.ivymobi.pedometer.gps.BaiduGPSServer";
    public static final String ACTION_PAUSE = ACTION + "_PAUSE";
    public static final String ACTION_RESUME = ACTION + "_RESUME";
    public static final String ACTION_GPS = ACTION + "_BAIDUGPS";
    LocationClient mLocClient = null;
    public MyLocationListenner myListener = new MyLocationListenner();
    baiduGPSBroadcastReceiver mBroadcastReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_RESUME);
        mBroadcastReceiver = new baiduGPSBroadcastReceiver();
        registerReceiver(mBroadcastReceiver, filter);
        startBaiduGPS();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    void startBaiduGPS() {
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    float step=0.0f;
    class baiduGPSBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_PAUSE.equals(intent.getAction())) {
                if (mLocClient != null)
                    mLocClient.stop();
            } else if (ACTION_RESUME.equals(intent.getAction())) {
                if (mLocClient == null)
                    startBaiduGPS();
                else
                    mLocClient.start();
            }
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;
//            step+=0.0001f;
//            location.setLongitude(location.getLongitude()+step);
            Intent intent = new Intent(ACTION_GPS);
            intent.putExtra(ACTION_GPS, new Gson().toJson(location));
            sendBroadcast(intent);
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }
    }
}
