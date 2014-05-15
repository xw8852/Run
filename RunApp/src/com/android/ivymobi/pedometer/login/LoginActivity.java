package com.android.ivymobi.pedometer.login;

import java.util.HashMap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.ivymobi.pedometer.BaseActivity;
import com.android.ivymobi.pedometer.Config;
import com.android.ivymobi.pedometer.MainActivity;
import com.android.ivymobi.pedometer.SyncMetaData;
import com.android.ivymobi.pedometer.SyncMetaData.MetaData;
import com.android.ivymobi.pedometer.SyncMine;
import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.util.MD5Util;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.runapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.command.ErrorCode;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.DefaultMapRequest;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

@InjectActivity(id = R.layout.activity_login)
public class LoginActivity extends BaseActivity implements View.OnClickListener, IResponseListener {
    @InjectView(id = R.id.passwd)
    TextView mViewPWD;
    @InjectView(id = R.id.email)
    TextView mViewEmail;
    @InjectView(id = R.id.login)
    Button mBtnLogin;
    @InjectView(id = R.id.ForPwd)
    View mForgetPWD;
    @InjectView(id = R.id.regist)
    View mRegist;
    @InjectView(id = R.id.suffix)
    EditText emailSuffix;
    @InjectView(id = R.id.down)
    ImageView down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // MobclickAgent.setReportPolicy(ReportPolicy.REALTIME);
        UmengUpdateAgent.update(this);
        MobclickAgent.setDebugMode( true );

        Inject.inject(this);

        // SDK在统计Fragment时，需要关闭Activity自带的页面统计，
        // 然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
        // MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.updateOnlineConfig(this);
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);
        mBtnLogin.setOnClickListener(this);
        mForgetPWD.setOnClickListener(this);
        mRegist.setOnClickListener(this);
        if (!TextUtils.isEmpty(UserUtil.getSession())) {
            SyncMine.getInstance().syncMine(null);
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        UserUtil.clearMine();
        // showSpinner();
        SyncMetaData.SyncMetaData(null);
        /**
         * new SyncMetaData.ISyncMeta() {
         * 
         * @Override public void syncMetaData() { showSpinner(); }
         * 
         *           }
         */
        down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showSpinner();
            }

        });
    }

    String[] mItems = null;
    PopupWindow popupWindow;

    void showSpinner() {
        MetaData data = UserUtil.getMetaData();
        if (data == null || data.domain == null || data.domain.size() < 1) {
            mItems = getResources().getStringArray(R.array.domain);
        } else {
            // 建立数据源
            mItems = data.domain.toArray(new String[data.domain.size()]);
        }

        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> _Adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1, mItems);
        popupWindow = new PopupWindow();
        ListView mListView = new ListView(this);
        mListView.setBackgroundColor(Color.WHITE);
        mListView.setCacheColorHint(Color.TRANSPARENT);
        mListView.setAdapter(_Adapter);
        mListView.measure(0, 0);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                emailSuffix.setText(mItems[position]);
                popupWindow.dismiss();
            }

        });
        popupWindow.setWidth(getResources().getDisplayMetrics().widthPixels);
        popupWindow.setHeight(mListView.getMeasuredHeight()*3);
        popupWindow.setContentView(mListView);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        Rect rect = new Rect();
        emailSuffix.getGlobalVisibleRect(rect);
        popupWindow.showAtLocation(down, Gravity.TOP | Gravity.LEFT, 0, rect.bottom);
    }

    @Override
    public void onBackPressed() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.ForPwd:
            startActivity(new Intent(this, ForgetPWD.class));
            break;
        case R.id.regist:
            startActivity(new Intent(this, RegisterActivity.class));
            break;
        case R.id.login:
            login();
            // ToastUtil.showLongToast("登陆成功，暂时假登陆");

            break;
        default:
            break;
        }
    }

    void login() {
        String email = mViewEmail.getText().toString() + emailSuffix.getText().toString();
        if (email == null || "".equals(email) || "".equals(email.trim())) {
            ToastUtil.showLongToast("邮箱不能为空");
            return;
        }
        // if (!RegUtil.isEmail(email)) {
        // ToastUtil.showLongToast("邮箱格式不正确");
        // return;
        // }
        String passwd = mViewPWD.getText().toString();
        if (passwd == null || "".equals(passwd) || "".equals(passwd.trim())) {
            ToastUtil.showLongToast("密码不能为空");
            return;
        }
        HashMap<String, String> maps = new HashMap<String, String>();
        maps.put("email", email);
        maps.put("password", MD5Util.getMD5String(email + passwd));
        showLoadingDialog(R.string.loadingData);
        Request request = new DefaultMapRequest(Config.SEVER_LOGIN, maps);
        goPost(request, this);
    }

    @Override
    public void onSuccess(Response response) {
        dismissLoadingDialog();
        String dataString = response.getData().toString();
        BaseModel<LoginResult> data = new Gson().fromJson(dataString, new TypeToken<BaseModel<LoginResult>>() {
        }.getType());
        if ("fail".equals(data.status)) {
            ToastUtil.showLongToast(data.message);
        } else {
            ToastUtil.showLongToast(R.string.loginSuccess);
            UserUtil.saveEmail(mViewEmail.getText().toString());
            UserUtil.saveSession(data.data.session_id);
            SyncMine.getInstance().syncMine(null);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }

    @Override
    public void onError(Response response) {
        dismissLoadingDialog();
        ToastUtil.showLongToast(ErrorCode.getErrorCodeString(response.errorCode));
    }

    class LoginResult {
        String session_id;
    }
}
