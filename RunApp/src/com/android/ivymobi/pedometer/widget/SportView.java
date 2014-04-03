package com.android.ivymobi.pedometer.widget;

import com.android.ivymobi.runapp.R;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SportView extends LinearLayout {
    /** 运动状态，走路or跑步 */
    @InjectView(id = R.id.imageView1)
    ImageView mSporState;
    /** 公里数 */
    @InjectView(id = R.id.textView3)
    TextView mDistance;
    /** 设置日期 */
    @InjectView(id = R.id.textView5)
    TextView mDate;
    /** 时长 */
    @InjectView(id = R.id.textView7)
    TextView mTime;
    /** 起始时间-结束时间 */
    @InjectView(id = R.id.textView8)
    TextView mDTime;
    /** 消耗的卡路里 */
    @InjectView(id = R.id.textView10)
    TextView mCal;
    /** 运动对应的单位： 跑步：时速，走路：步数 */
    @InjectView(id = R.id.textView11)
    TextView mSpeedUnit;
    /** 速度： 跑步：时速，走路：步数 */
    @InjectView(id = R.id.textView12)
    TextView mSpeed;
    @InjectView(id = R.id.button1)
    Button mState;

    boolean isRunning;

    public SportView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SportView(Context context) {
        super(context);
        initView();
    }

    void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.run_state, this);
        Inject.inject(this, this);
    }

    /** 设置运动状态 */
    public void setSportStae(boolean isRun) {
        this.isRunning = isRun;
        mSporState.setImageResource(isRun ? R.drawable.ic_state_run : R.drawable.ic_state_walk);
        mSpeedUnit.setText(isRun ? "时速" : "步数");
    }

    public void setButton(boolean isPause) {
        if (isPause) {
            mState.setText("继续");
        } else {
            mState.setText("暂停");
        }
    }

    public Button getButton() {
        return mState;
    }

    /** 设置公里数 */
    public void setDistance(String distance) {
        mDistance.setText(distance + " km");
    }

    /** 设置日期 */
    public void setDate(String date) {
        mDate.setText(date);
    }

    /** 设置花费时间 */
    public void setTime(String time) {
        mTime.setText(time);
    }

    /** 设置起止时间 13:00-14:35 */
    public void setDTime(String date) {
        mDTime.setText(date);
    }

    /** 设置cal */
    public void setCals(String cals) {
        mCal.setText(cals + " cal");
    }

    /** 设置速度 */
    public void setSpeed(String date) {
        mSpeed.setText(date);
    }

}
