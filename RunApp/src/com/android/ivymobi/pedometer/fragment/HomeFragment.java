package com.android.ivymobi.pedometer.fragment;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.android.ivymobi.pedometer.gps.BaiduGPSServer;
import com.android.ivymobi.pedometer.listener.PedometerSettings;
import com.android.ivymobi.pedometer.listener.StepService;
import com.android.ivymobi.pedometer.util.DateUtil;
import com.android.ivymobi.pedometer.util.PUtils;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.pedometer.widget.SportView;
import com.android.ivymobi.runapp.R;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.MyLocationOverlay.LocationMode;
import com.baidu.mapapi.map.Symbol;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.google.gson.Gson;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectView;

public class HomeFragment extends RelativeLayout implements IViewStatus {
    @InjectView(id = R.id.sportView1)
    SportView mSportView;
    @InjectView(id = R.id.btn_walk)
    View btn_walk;
    @InjectView(id = R.id.btn_run)
    View btn_run;

    @InjectView(id = R.id.bmapView)
    MapView mMapView;

    LocationClient mLocClient = null;
    LocationData locData = null;
    MyLocationOverlay myLocationOverlay;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MapController mMapController = null;
    boolean isRequest = false;// 是否手动触发请求定位
    boolean isFirstLoc = true;// 是否首次定位
    GraphicsOverlay graphicsOverlay = null;
    private Timer timer;

    public HomeFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public HomeFragment(Context context) {
        super(context);
        initView();

    }

    void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.sport_home_main, this);
        Inject.inject(this, this);
        // 初始化地图
        mMapController = mMapView.getController();

        mMapView.getController().setZoom(17);
        mMapView.getController().enableClick(true);
        graphicsOverlay = new GraphicsOverlay(mMapView);

        // mLocClient = new LocationClient(getContext());
        locData = new LocationData();
        // mLocClient.registerLocationListener(myListener);
        // LocationClientOption option = new LocationClientOption();
        // option.setOpenGps(true);// 打开gps
        // option.setCoorType("bd09ll"); // 设置坐标类型
        // option.setScanSpan(10000);
        // mLocClient.setLocOption(option);
        // mLocClient.start();

        // 定位图层初始化
        myLocationOverlay = new MyLocationOverlay(mMapView);
        mMapView.getOverlays().add(graphicsOverlay);
        // 设置定位数据
        myLocationOverlay.setData(locData);
        // 添加定位图层
        mMapView.getOverlays().add(myLocationOverlay);
        myLocationOverlay.enableCompass();
        // 修改定位数据后刷新图层生效
        mMapView.refresh();
        btn_run.setOnClickListener(mRunOrWalkListener);
        btn_walk.setOnClickListener(mRunOrWalkListener);
        mSportView.getButton().setOnClickListener(mPauseORContinueClickListener);
    }

    @Override
    public void onResume() {
        int state = PUtils.getSportState();
        if (state > -1 && state < 3) {
            mSportView.setDate(DateUtil.getDate(System.currentTimeMillis()));
            mSportView.setDTime(DateUtil.getTime(System.currentTimeMillis()) + " - ~ ");
            SharedPreferences mState = getContext().getSharedPreferences("state", 0);

            float distance = mState.getFloat("distance", 0);
            System.out.println(distance+"-----");
            mSportView.setDistance(PUtils.getNum3(distance));

            mSportView.setCals(PUtils.getNum3(mState.getFloat("calories", 0)));
            int _state = new PedometerSettings(PreferenceManager.getDefaultSharedPreferences(getContext())).isRunning() ? 0 : 1;
            if (_state == 1) {
                mSportView.setSpeed(mState.getInt("steps", 0) + "");
            } else if (_state == 2) {
                mSportView.setSpeed(PUtils.getNum3(mState.getFloat("speed", 0)) + " km/h");
            }

            if (state == 2) {
                mSportView.setButton(true);
                return;
            }
            mSportView.setButton(false);
            // mLocClient.stop();
            getContext().startService(new Intent(BaiduGPSServer.ACTION));
            startTimer();
            getContext().startService(new Intent(getContext(), StepService.class));
            findViewById(R.id.choise_sport).setVisibility(View.GONE);

        }
        if (!isRegistBroadCast) {
            isRegistBroadCast = true;
            getContext().registerReceiver(baiduGpsReceiver, new IntentFilter(BaiduGPSServer.ACTION_GPS));
            getContext().registerReceiver(stepBroadcastReceiver, new IntentFilter(StepService.ACTION_STEP));
        }
    }

    @Override
    public void onPause() {

    }

    boolean isRegistBroadCast;

    @Override
    public void onFinish() {
        if (isRegistBroadCast) {
            getContext().unregisterReceiver(baiduGpsReceiver);
            getContext().unregisterReceiver(stepBroadcastReceiver);
            isRegistBroadCast = false;
        }

        if (timer != null)
            timer.cancel();
        onPauseStep();
    }

    public void startTimer() {
        timer = new Timer();
        final Handler handler = new Handler();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO:设置时长
                        int state = PUtils.getSportState();
                        if ((state == 0 || state == 1) && mSportView != null) {
                            long time = System.currentTimeMillis() - (PUtils.getPauseDate() == 0 ? PUtils.getStartTime() : PUtils.getPauseDate())
                                    + PUtils.getLastTime();
                            mSportView.setTime(DateUtil.convertTime(time));
                            mSportView.setDTime(DateUtil.getTime(PUtils.getStartTime()) + " -  " + DateUtil.getTime(System.currentTimeMillis()));
                        }
                    }
                });
            }
        }, 1000, 1000);
    }

    public void onPauseStep() {
        if (PUtils.getSportState() != 3 && PUtils.getSportState() != -1)
            PUtils.saveSportState(2);
        getContext().stopService(new Intent(getContext(), StepService.class));
    }

    public void onStartStep() {
        PUtils.savePauseDate(System.currentTimeMillis());
        PUtils.saveSportState(new PedometerSettings(PreferenceManager.getDefaultSharedPreferences(getContext())).isRunning() ? 0 : 1);
        getContext().startService(new Intent(getContext(), StepService.class));
    }

    BroadcastReceiver baiduGpsReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            myListener.onReceiveLocation(new Gson().fromJson(intent.getStringExtra(BaiduGPSServer.ACTION_GPS), BDLocation.class));
        }
    };

    BroadcastReceiver stepBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mSportView == null)
                return;
            int state = PUtils.getSportState();
            if (intent.hasExtra("distance")) {
                float distance = intent.getFloatExtra("distance", 0f);
                mSportView.setDistance(PUtils.getNum3(distance));
            }
            if (intent.hasExtra("cal")) {
                float cal = intent.getFloatExtra("cal", 0f);
                mSportView.setCals(PUtils.getNum3(cal));
            }
            if (intent.hasExtra("step") && state == 1) {
                mSportView.setSpeed(intent.getIntExtra("step", 0) + "");
            }
            if (intent.hasExtra("speed") && state == 0) {
                float speed = intent.getFloatExtra("speed", 0f);
                mSportView.setSpeed(PUtils.getNum3(speed) + " km/h");
            }
        }
    };

    View.OnClickListener mRunOrWalkListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            Editor editor = preferences.edit();
            if (v.getId() == R.id.btn_run) {
                editor.putString("exercise_type", "running");
                mSportView.setSportStae(true);
                PUtils.saveSportState(0);
            } else if (v.getId() == R.id.btn_walk) {
                mSportView.setSportStae(false);
                editor.putString("exercise_type", "walking");
                PUtils.saveSportState(1);
            }
            mSportView.setButton(false);
            if (!isRegistBroadCast) {
                isRegistBroadCast = true;
                getContext().registerReceiver(baiduGpsReceiver, new IntentFilter(BaiduGPSServer.ACTION_GPS));
                getContext().registerReceiver(stepBroadcastReceiver, new IntentFilter(StepService.ACTION_STEP));
            }
            editor.commit();
            long time = System.currentTimeMillis();
            PUtils.saveStartTime(time);
            PUtils.saveEndTime(0);
            PUtils.saveLastTime(0);
            PUtils.savePauseDate(0);
            mSportView.setDate(DateUtil.getDate(time));
            mSportView.setDTime(DateUtil.getTime(time) + " -  " + DateUtil.getTime(time));
            mSportView.setButton(false);
            startTimer();
            getContext().startService(new Intent(BaiduGPSServer.ACTION));
            getContext().startService(new Intent(v.getContext(), StepService.class));
            findViewById(R.id.choise_sport).setVisibility(View.GONE);
        }
    };
    PopupWindow popupWindow;
    View.OnClickListener mPauseORContinueClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mSportView.getButton().isChecked()) {
                mSportView.setButton(false);
                onStartStep();
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                // TODO:继续
            } else {
                mSportView.setButton(true);
                onPauseStep();
                DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                popupWindow = new PopupWindow(metrics.widthPixels, metrics.heightPixels);
                View popView = LayoutInflater.from(getContext()).inflate(R.layout.sport_pause_menu, null);
                setPopupListener(popView);
                popupWindow.setContentView(popView);
                popupWindow.showAtLocation(v.getRootView(), Gravity.CENTER, 0, 0);
                // TODO:暂停
            }
        }
    };

    void setPopupListener(final View popView) {
        popView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
        popView.findViewById(R.id.button0).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mSportView.setButton(false);
                onStartStep();
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }

        });
        popView.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PUtils.saveSportState(3);
                onFinish();
                mSportView.reset();
                findViewById(R.id.choise_sport).setVisibility(View.VISIBLE);
                ToastUtil.showShortToast("放弃");
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                PUtils.clearStepData();
            }

        });
        popView.findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PUtils.saveSportState(3);
                onFinish();
                mSportView.reset();
                ToastUtil.showShortToast("结束");
                findViewById(R.id.choise_sport).setVisibility(View.VISIBLE);
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                PUtils.clearStepData();
            }

        });
    }

    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    BDLocation _lastLocation;

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;

            locData.latitude = location.getLatitude();
            locData.longitude = location.getLongitude();
            // 如果不显示定位精度圈，将accuracy赋值为0即可
            locData.accuracy = location.getRadius();
            // 此处可以设置 locData的方向信息, 如果定位 SDK 未返回方向信息，用户可以自己实现罗盘功能添加方向信息。
            locData.direction = location.getDerect();
            // 更新定位数据
            myLocationOverlay.setData(locData);
            // 更新图层数据执行刷新后生效
            mMapView.refresh();
            // 是手动触发请求或首次定位时，移动到定位点
            if (isRequest || isFirstLoc) {
                // 移动地图到定位点
                Log.d("LocationOverlay", "receive location, animate to it");
                mMapController.animateTo(new GeoPoint((int) (locData.latitude * 1e6), (int) (locData.longitude * 1e6)));
                isRequest = true;
                isFirstLoc = false;
                myLocationOverlay.setLocationMode(LocationMode.FOLLOWING);
                // requestLocButton.setText("跟随");
                // mCurBtnType = E_BUTTON_TYPE.FOLLOW;
            }
            // 首次定位完成
            isFirstLoc = false;

            if (PUtils.getSportState() != 1 && PUtils.getSportState() != 0) {
                _lastLocation = location;
                return;
            }
            if (_lastLocation == null) {
                _lastLocation = location;
            } else {
                // if(location.getLatitude()!=_lastLocation.getLatitude()&&
                // location.getLongitude()!=_lastLocation.getLongitude()){

                double mLat = _lastLocation.getLatitude();
                double mLon = _lastLocation.getLongitude();

                int lat = (int) (mLat * 1E6);
                int lon = (int) (mLon * 1E6);
                GeoPoint pt1 = new GeoPoint(lat, lon);

                mLat = location.getLatitude();
                mLon = location.getLongitude();
                lat = (int) (mLat * 1E6);
                lon = (int) (mLon * 1E6);

                GeoPoint pt2 = new GeoPoint(lat, lon);
                double distance = DistanceUtil.getDistance(pt1, pt2);
                if (distance > 5) {
                    // ToastUtil.showLongToast("距离：" + distance);
                }
                // 构建线
                Geometry lineGeometry = new Geometry();
                // 设定折线点坐标
                GeoPoint[] linePoints = new GeoPoint[2];
                linePoints[0] = pt1;
                linePoints[1] = pt2;
                lineGeometry.setPolyLine(linePoints);
                // 设定样式
                Symbol lineSymbol = new Symbol();
                Symbol.Color lineColor = lineSymbol.new Color();
                lineColor.red = 255;
                lineColor.green = 0;
                lineColor.blue = 0;
                lineColor.alpha = 255;
                lineSymbol.setLineSymbol(lineColor, 5);
                // 生成Graphic对象
                Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);

                graphicsOverlay.setData(lineGraphic);
                mMapView.refresh();
                _lastLocation = location;
                // }
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }
    }
}
