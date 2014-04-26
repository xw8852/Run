package com.android.ivymobi.pedometer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.android.ivymobi.pedometer.login.LoginActivity;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.runapp.R;
import com.msx7.core.Controller;

public class UserInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_userinfo);
        findViewById(R.id.loginOut).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Controller.getApplication().finishAllHistory();
                UserUtil.clearMine();
                startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

}
