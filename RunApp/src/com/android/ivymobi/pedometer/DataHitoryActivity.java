package com.android.ivymobi.pedometer;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.ivymobi.runapp.R;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;

@InjectActivity(id = R.layout.history_activity)
public class DataHitoryActivity extends BaseActivity {
    @InjectView(id = R.id.dates)
    RadioGroup mGroup;
    @InjectView(id = R.id.title_left)
    View mLeftTitle;
    @InjectView(id = R.id.title)
    TextView mTitleView;
    @InjectView(id = R.id.title_right)
    View mRightTitle;
    @InjectView(id = R.id.speed)
    TextView mSpeed;
    @InjectView(id = R.id.distance)
    TextView mDistance;
    @InjectView(id = R.id.maxSpeed)
    TextView mMaxSpeed;
    @InjectView(id = R.id.times)
    TextView mTimes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Inject.inject(this);
       
    }

}
