package com.android.ivymobi.pedometer.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.ivymobi.pedometer.BaseActivity;
import com.android.ivymobi.pedometer.Config;
import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.util.RegUtil;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.runapp.R;
import com.google.gson.Gson;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.command.ErrorCode;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.DefaultMapRequest;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;

@InjectActivity(id = R.layout.activity_forgetpwd)
public class ForgetPWD extends BaseActivity implements View.OnClickListener,IResponseListener {

    @InjectView(id = R.id.email)
    TextView mViewEmail;
    @InjectView(id = R.id.sendEmail)
    Button mBtnLogin;
    @InjectView(id = R.id.regist)
    View mForgetPWD;
    @InjectView(id = R.id.login)
    View mRegist;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Inject.inject(this);
        mRegist.setOnClickListener(this);
        mForgetPWD.setOnClickListener(this);
        mBtnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.regist:
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            break;

        case R.id.login:
            onBackPressed();
            break;
        case R.id.sendEmail:
            String email = mViewEmail.getText().toString();
            if (email == null || "".equals(email) || "".equals(email.trim())) {
                ToastUtil.showLongToast("邮箱不能为空");
                return;
            }
            if (!RegUtil.isEmail(email)) {
                ToastUtil.showLongToast("邮箱格式不正确");
                return;
            }
            showLoadingDialog(R.string.loadingData);
            Request request = new DefaultMapRequest(Config.SEVER_REGISTER, "email", email);
            goPost(request, this);
            break;
        default:
            break;
        }
    }
    @Override
    public void onSuccess(Response response) {
        dismissLoadingDialog();
        String dataString = response.getData().toString();
        BaseModel model=new Gson().fromJson(dataString, BaseModel.class);
        ToastUtil.showLongToast(model.message);

    }

    @Override
    public void onError(Response response) {
        dismissLoadingDialog();
        ToastUtil.showLongToast(ErrorCode.getErrorCodeString(response.errorCode));
    }

}
