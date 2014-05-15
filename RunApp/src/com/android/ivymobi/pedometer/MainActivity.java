package com.android.ivymobi.pedometer;

import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.fragment.HomeFragment;
import com.android.ivymobi.pedometer.fragment.RankFragment;
import com.android.ivymobi.pedometer.fragment.UserFragment;
import com.android.ivymobi.pedometer.util.PUtils;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.runapp.R;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.Controller;
import com.msx7.core.Manager;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.DefaultMapRequest;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;
import com.umeng.analytics.MobclickAgent;

@InjectActivity(id = R.layout.activity_main)
public class MainActivity extends BaseActivity implements View.OnClickListener {
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
        FilterActivity.clearConfig();
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
        sendRunData();
    }

    void sendRunData() {
        String data = PUtils.getRunDate();
        if (TextUtils.isEmpty(data))
            return;
        Map<String, Object> map = new Gson().fromJson(data, new TypeToken<Map<String, Object>>() {
        }.getType());
        Request request = new DefaultMapRequest(Config.SEVER_WORK_SYNC + "?session_id=" + UserUtil.getSession(), map);
        Manager.getInstance().execute(Manager.CMD_JSON_POST, request, new IResponseListener() {

            @Override
            public void onSuccess(Response response) {
                dismissLoadingDialog();
                String dataString = response.getData().toString();
                BaseModel data = new Gson().fromJson(dataString, new TypeToken<BaseModel>() {
                }.getType());
                if ("ok".equals(data.status)) {
                    ToastUtil.showLongToast("上传运动数据成功");

                    PUtils.clearRunDate();
                    ToastUtil.showShortToast("结束");

                } else {
                    ToastUtil.showLongToast("上传运动数据失败,下次启动时自动发送");

                }
            }

            @Override
            public void onError(Response response) {
                dismissLoadingDialog();
                ToastUtil.showLongToast("上传运动数据失败,下次启动时自动发送");

            }

        });
    }

    UserFragment userFragment;
    HomeFragment fragment;
    RankFragment fragment2;
    String screanPage = "HomeFragment";

    @Override
    public void onClick(View v) {
        int state = PUtils.getSportState();
        if (state > -1 && state < 3)
            return;
        if (v.isSelected())
            return;
        MobclickAgent.onPageEnd(screanPage);
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
            screanPage = "UserFragment";
            showView(userFragment);
            break;
        case R.id.btn_run:
            if (fragment == null) {
                fragment = new HomeFragment(this);
                mContent.addView(fragment);
                fragment.onResume();
            }
            screanPage = "HomeFragment";
            showView(fragment);
            break;
        case R.id.btn_rank:
            if (fragment2 == null) {
                fragment2 = new RankFragment(this);
                mContent.addView(fragment2);
                fragment2.onResume();
            }
            screanPage = "RankFragment";
            showView(fragment2);
            break;
        }
        MobclickAgent.onPageStart(screanPage);
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
    public void onResume() {
        super.onResume();
        if (userFragment != null)
            userFragment.onResume();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragment2.onActivityResult(requestCode, resultCode, data);
    }

}
