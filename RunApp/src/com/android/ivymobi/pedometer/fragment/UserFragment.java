package com.android.ivymobi.pedometer.fragment;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.ivymobi.pedometer.AchiActivity;
import com.android.ivymobi.pedometer.DataHitoryActivity;
import com.android.ivymobi.pedometer.ScoreActivity;
import com.android.ivymobi.pedometer.SyncMine;
import com.android.ivymobi.pedometer.data.Mine;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.runapp.R;
import com.msx7.image.ImageLoader;

public class UserFragment extends LinearLayout implements IViewStatus, OnClickListener {
    ImageView imageView;
    TextView userName;

    public UserFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.user_home_main, this);
    }

    public UserFragment(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.user_home_main, this);
        imageView = (ImageView) findViewById(R.id.roundImageView1);
        userName = (TextView) findViewById(R.id.textView1);
        setBackgroundResource(R.drawable.user_home_bg);
        LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        }
        params.gravity = Gravity.CENTER_HORIZONTAL;
        setLayoutParams(params);
        setOrientation(VERTICAL);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        if (UserUtil.getMine() == null) {
            SyncMine.getInstance().syncMine(new SyncMine.ISyncMineFinish() {

                @Override
                public void syncFinish() {
                    Mine _mine = UserUtil.getMine();
                    if (_mine != null) {
                        ImageLoader.getInstance().loadImage(_mine.avatar_url, imageView);
                        userName.setText(_mine.nickname);
                    }

                }
            });
        } else {
            Mine _mine = UserUtil.getMine();
            if (_mine != null) {
                ImageLoader.getInstance().loadImage(_mine.avatar_url, imageView);
                userName.setText(_mine.nickname);
            }
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button1:
            /** 积分 */
            getContext().startActivity(new Intent(getContext(), ScoreActivity.class));
            break;
        case R.id.button2:
            /** 成就 */
            getContext().startActivity(new Intent(getContext(), AchiActivity.class));
            break;
        case R.id.button3:
            /** 历史统计 */
            getContext().startActivity(new Intent(getContext(), DataHitoryActivity.class));
            break;
        }
    }

}
