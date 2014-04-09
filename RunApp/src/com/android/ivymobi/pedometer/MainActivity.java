package com.android.ivymobi.pedometer;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ivymobi.pedometer.gps.BaiduGPSServer;
import com.android.ivymobi.pedometer.listener.PedometerSettings;
import com.android.ivymobi.pedometer.listener.StepService;
import com.android.ivymobi.pedometer.util.DateUtil;
import com.android.ivymobi.pedometer.util.PUtils;
import com.android.ivymobi.pedometer.widget.SportView;
import com.android.ivymobi.runapp.R;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.BMapManager;
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
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;

@InjectActivity(id = R.layout.activity_main)
public class MainActivity extends Activity implements View.OnClickListener {
    /** 最小化线的距离，单位米 */
    public static final int MIN_DISTANCE = 5;
    @InjectView(id = R.id.content)
    LinearLayout mContent;
    @InjectView(id = R.id.titleBar)
    View titleBar;
    @InjectView(id = R.id.title)
    TextView titleView;
    @InjectView(id = R.id.bmapView)
    MapView mMapView;
    /** 用户按钮 */
    @InjectView(id = R.id.btn_user)
    ImageView mBtnUser;
    /** 运动按钮 */
    @InjectView(id = R.id.btn_run)
    /**排行榜按钮*/
    ImageView mBtnRun;
    @InjectView(id = R.id.btn_rank)
    ImageView mBtnRank;
    LocationClient mLocClient = null;
    LocationData locData = null;
    MyLocationOverlay myLocationOverlay;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MapController mMapController = null;
    boolean isRequest = false;// 是否手动触发请求定位
    boolean isFirstLoc = true;// 是否首次定位
    GraphicsOverlay graphicsOverlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        RunApplication app = (RunApplication) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(getApplicationContext());
            /**
             * 如果BMapManager没有初始化则初始化BMapManager
             */
            app.mBMapManager.init(new RunApplication.MyGeneralListener());
        }
        Inject.inject(this);
        titleBar.setVisibility(View.GONE);
         mBtnRank.setOnClickListener(this);
        mBtnRun.setOnClickListener(this);
         mBtnUser.setOnClickListener(this);
        // 初始化地图
        mMapController = mMapView.getController();

        mMapView.getController().setZoom(17);
        mMapView.getController().enableClick(true);
        graphicsOverlay = new GraphicsOverlay(mMapView);

        // mLocClient = new LocationClient(this);
        locData = new LocationData();
        // mLocClient.registerLocationListener(myListener);
        // LocationClientOption option = new LocationClientOption();
        // option.setOpenGps(true);// 打开gps
        // option.setCoorType("bd09ll"); // 设置坐标类型
        // option.setScanSpan(5000);
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
        onClick(mBtnRun);

    }

    @Override
    public void onClick(View v) {
        if(v.isSelected())return;
        mBtnRank.setSelected(false);
        mBtnRun.setSelected(false);
        mBtnUser.setSelected(false);
        v.setSelected(true);
        switch (v.getId()) {
        case R.id.btn_user:
            mContent.removeAllViews();
            titleBar.setVisibility(View.GONE);
            getLayoutInflater().inflate(R.layout.user_home_main, mContent);
            break;
        case R.id.btn_run:
            titleView.setText("选择运动");
            titleBar.setVisibility(View.VISIBLE);
            mContent.removeAllViews();
            getLayoutInflater().inflate(R.layout.run_walk, mContent);
            mContent.getChildAt(0).findViewById(R.id.btn_run).setOnClickListener(mRunOrWalkListener);
            mContent.getChildAt(0).findViewById(R.id.btn_walk).setOnClickListener(mRunOrWalkListener);
            break;
        case R.id.btn_rank:
            titleBar.setVisibility(View.GONE);
            mContent.removeAllViews();
            break;
        }
    }

    SportView mSportView;
    View.OnClickListener mRunOrWalkListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
            Editor editor = preferences.edit();
            mSportView = new SportView(MainActivity.this);
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
            mSportView.getButton().setOnClickListener(mPauseORContinueClickListener);
            editor.commit();
            mContent.removeAllViews();
            long time = System.currentTimeMillis();
            PUtils.saveStartTime(time);
            PUtils.saveEndTime(0);
            PUtils.saveLastTime(0);
            PUtils.savePauseDate(0);
            mSportView.setDate(DateUtil.getDate(time));
            mSportView.setDTime(DateUtil.getTime(time) + " - ~ ");
            mContent.addView(mSportView);
            startService(new Intent(v.getContext(), StepService.class));
            titleBar.setVisibility(View.GONE);
        }
    };
    View.OnClickListener mPauseORContinueClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mSportView.getButton().getText().equals("继续")) {
                mSportView.setButton(false);
                onStartStep();
                // TODO:继续
            } else {
                mSportView.setButton(true);
                onPauseStep();
                // TODO:暂停
            }
        }
    };

    BroadcastReceiver baiduGpsReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            myListener.onReceiveLocation(new Gson().fromJson(intent.getStringExtra(BaiduGPSServer.ACTION_GPS),
                    BDLocation.class));
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
                mSportView.setSpeed(PUtils.getNum3(speed) + " km");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // 定位初始化
        startService(new Intent(BaiduGPSServer.ACTION));
        registerReceiver(baiduGpsReceiver, new IntentFilter(BaiduGPSServer.ACTION_GPS));
        registerReceiver(stepBroadcastReceiver, new IntentFilter(StepService.ACTION_STEP));
        startTimer();

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
                            long time = System.currentTimeMillis()
                                    - (PUtils.getPauseDate() == 0 ? PUtils.getStartTime() : PUtils.getPauseDate())
                                    + PUtils.getLastTime();
                            mSportView.setTime(DateUtil.convertTime(time));
                        }
                    }
                });
            }
        }, 1000, 1000);
    }

    @Override
    public void finish() {
        super.finish();
        PUtils.saveStartTime(0);
        PUtils.saveEndTime(0);
        PUtils.saveLastTime(0);
        PUtils.savePauseDate(0);
        PUtils.saveSportState(3);
        unregisterReceiver(baiduGpsReceiver);
        unregisterReceiver(stepBroadcastReceiver);
        timer.cancel();
    }

    Timer timer;

    public void onPauseStep() {
        PUtils.saveSportState(2);
        stopService(new Intent(this, StepService.class));
    }

    public void onStartStep() {
        PUtils.savePauseDate(System.currentTimeMillis());
        PUtils.saveSportState(new PedometerSettings(PreferenceManager.getDefaultSharedPreferences(this)).isRunning() ? 0
                : 1);
        startService(new Intent(this, StepService.class));
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
            
            if (PUtils.getSportState() != 1 && PUtils.getSportState() != 0){
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
                if(distance>5){
                    Toast.makeText(getApplication(), "距离："+distance, Toast.LENGTH_LONG).show();
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
