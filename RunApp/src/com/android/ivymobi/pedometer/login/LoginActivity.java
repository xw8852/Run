package com.android.ivymobi.pedometer.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.ivymobi.pedometer.BaseActivity;
import com.android.ivymobi.runapp.R;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;

@InjectActivity(id = R.layout.activity_login)
public class LoginActivity extends BaseActivity implements View.OnClickListener {
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

            break;
        default:
            break;
        }
    }

}
