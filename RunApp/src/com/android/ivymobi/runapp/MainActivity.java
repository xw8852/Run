package com.android.ivymobi.runapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.MyLocationOverlay.LocationMode;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;

@InjectActivity(id = R.layout.activity_main)
public class MainActivity extends Activity implements View.OnClickListener {
    @InjectView(id=R.id.content)
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

        // 定位初始化
        mLocClient = new LocationClient(this);
        locData = new LocationData();
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        // 定位图层初始化
        myLocationOverlay = new MyLocationOverlay(mMapView);
        // 设置定位数据
        myLocationOverlay.setData(locData);
        // 添加定位图层
        mMapView.getOverlays().add(myLocationOverlay);
        myLocationOverlay.enableCompass();
        // 修改定位数据后刷新图层生效
        mMapView.refresh();
    }

    @Override
    public void onClick(View v) {
        mBtnRank.setSelected(false);
        mBtnRun.setSelected(false);
        mBtnUser.setSelected(false);
        v.setSelected(true);
        switch (v.getId()) {
        case R.id.btn_user:
            mContent.removeAllViews();
            break;
        case R.id.btn_run:
            titleView.setText("选择运动");
            titleBar.setVisibility(View.VISIBLE);
            mContent.removeAllViews();
            getLayoutInflater().inflate(R.layout.run_walk, mContent);
            break;
        case R.id.btn_rank:
            break;
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
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }
    }
}
