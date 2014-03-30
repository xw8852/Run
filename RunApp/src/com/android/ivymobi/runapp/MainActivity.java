package com.android.ivymobi.runapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.ivymobi.runapp.fragment.FriendFragment;
import com.android.ivymobi.runapp.fragment.HomeFragment;
import com.android.ivymobi.runapp.fragment.MessageFragment;
import com.android.ivymobi.runapp.fragment.RankFragment;
import com.android.ivymobi.runapp.widget.SlidingGroup;

public class MainActivity extends FragmentActivity {
    View msetView;
    SlidingGroup slidingGroup;
    RadioGroup radioGroup;
    TextView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        msetView = findViewById(R.id.titleBar).findViewById(R.id.imageView1);
        slidingGroup = (SlidingGroup) findViewById(R.id.slidingGroup1);
        msetView.setOnClickListener(mSettingClickListener);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        titleView = (TextView) findViewById(R.id.title);
        radioGroup.setOnCheckedChangeListener(mCheckedChangeListener);
        changeFragment(-1);
    }

    View.OnClickListener mSettingClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            slidingGroup.snapToNext();
        }
    };
    RadioGroup.OnCheckedChangeListener mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            changeFragment(checkedId);
        }

    };

    @Override
    public void onBackPressed() {
        if (!slidingGroup.isDefault())
            slidingGroup.snapToNext();
        else
            super.onBackPressed();
    }

    void changeFragment(int id) {
        Fragment fragment = null;
        switch (id) {
        default:
        case R.id.radio0:
            titleView.setText("主页");
            fragment = new HomeFragment();
            break;
        case R.id.radio1:
            titleView.setText("附件排行");
            fragment = new RankFragment();
            break;
        case R.id.radio2:
            titleView.setText("关注好友");
            fragment = new FriendFragment();
            break;
        case R.id.radio3:
            titleView.setText("消息");
            fragment = new MessageFragment();
            break;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.root, fragment);
        ft.show(fragment);
        ft.commit();
        if (!slidingGroup.isDefault())
            slidingGroup.snapToDefault();
    }
}
