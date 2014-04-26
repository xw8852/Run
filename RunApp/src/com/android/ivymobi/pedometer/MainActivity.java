package com.android.ivymobi.pedometer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.ivymobi.pedometer.fragment.HomeFragment;
import com.android.ivymobi.pedometer.fragment.RankFragment;
import com.android.ivymobi.pedometer.fragment.UserFragment;
import com.android.ivymobi.runapp.R;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.Controller;

@InjectActivity(id = R.layout.activity_main)
public class MainActivity extends Activity implements View.OnClickListener {
    /** 最小化线的距离，单位米 */
    public static final int MIN_DISTANCE = 5;
    @InjectView(id = R.id.content)
    LinearLayout mContent;

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
        mBtnRun.setOnClickListener(this);
        mBtnRank.setOnClickListener(this);
        mBtnUser.setOnClickListener(this);
        onClick(mBtnRun);
        Controller.getApplication().addActivityHistory(this);
    }

    UserFragment userFragment;
    HomeFragment fragment;
    RankFragment fragment2;

    @Override
    public void onClick(View v) {
        if (v.isSelected())
            return;
        mBtnRank.setSelected(false);
        mBtnRun.setSelected(false);
        mBtnUser.setSelected(false);
        v.setSelected(true);
        switch (v.getId()) {
        case R.id.btn_user:
            if (userFragment == null) {
                userFragment = new UserFragment(this);
                mContent.addView(userFragment);
                userFragment.onResume();
            }
            showView(userFragment);
            break;
        case R.id.btn_run:
            if (fragment == null) {
                fragment = new HomeFragment(this);
                mContent.addView(fragment);
                fragment.onResume();
            }
            showView(fragment);
            break;
        case R.id.btn_rank:
            if (fragment2 == null) {
                fragment2 = new RankFragment(this);
                mContent.addView(fragment2);
                fragment2.onResume();
            }
            showView(fragment2);
            break;
        }
    }

    void showView(View v) {
        if (userFragment != null)
            userFragment.setVisibility(View.GONE);
        if (fragment != null)
            fragment.setVisibility(View.GONE);
        if (fragment2 != null)
            fragment2.setVisibility(View.GONE);
        v.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (fragment != null && fragment.getPopupWindow() != null && fragment.getPopupWindow().isShowing()) {
            fragment.getPopupWindow().dismiss();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();

    }

}
