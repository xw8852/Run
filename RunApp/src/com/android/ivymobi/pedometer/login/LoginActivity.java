package com.android.ivymobi.pedometer.login;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.ivymobi.pedometer.BaseActivity;
import com.android.ivymobi.pedometer.Config;
import com.android.ivymobi.pedometer.MainActivity;
import com.android.ivymobi.pedometer.SyncMine;
import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.util.MD5Util;
import com.android.ivymobi.pedometer.util.RegUtil;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Inject.inject(this);
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
        String email = mViewEmail.getText().toString();
        if (email == null || "".equals(email) || "".equals(email.trim())) {
            ToastUtil.showLongToast("邮箱不能为空");
            return;
        }
        if (!RegUtil.isEmail(email)) {
            ToastUtil.showLongToast("邮箱格式不正确");
            return;
        }
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
