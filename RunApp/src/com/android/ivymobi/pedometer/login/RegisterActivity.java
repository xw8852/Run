package com.android.ivymobi.pedometer.login;

import android.os.Bundle;

import com.android.ivymobi.pedometer.BaseActivity;
import com.android.ivymobi.runapp.R;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;

@InjectActivity(id = R.layout.activity_register)
public class RegisterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Inject.inject(this);
    }

}
