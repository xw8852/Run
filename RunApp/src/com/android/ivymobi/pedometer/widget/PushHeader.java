package com.android.ivymobi.pedometer.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.ivymobi.runapp.R;

/**
 * 
 * @author msx7 <br/>
 *         1、必须在构造函数或者{@link #setOnRefreshListener(OnRefreshListener)} 添加下拉刷新的
 *         监听，否则下拉刷新不起作用<br/>
 *         2、请通过{@link #getHeader()}获得添加至
 *         {@link ListView#addHeaderView(View, Object, boolean)} 或者添加至
 *         {@link GridView}的上方， 3、下拉刷新结束请务必调用{@link #onRefreshComplete()}
 *         4、你也主动迫使产生下拉刷新时间通过函数{@link #onRefresh()}
 * 
 */
public final class PushHeader extends AbstractHeader implements OnTouchListener {

    protected View mHeaderView;
    protected AdapterView<?> mAdapterView;
    protected OnRefreshListener mOnRefreshListener;

    private RotateAnimation mAnimation;
    private RotateAnimation mReverseAnimation;
    private TextView mTipsTextview;
    private TextView mLastUpdatedTextView;
    private ImageView mArrowImageView;
    private ProgressBar mProgressBar;
    private int headContentHeight;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm ");

    public PushHeader(ListView listView) {
        this(listView, null);
    }

    public PushHeader(ListView listView, OnRefreshListener l) {
        super(listView, true);
        this.mAdapterView = listView;
        this.mOnRefreshListener = l;
        if (listView == null)
            throw new IllegalArgumentException(" the 'listView' must be not null!");
        mAdapterView = listView;

        mHeaderView = LayoutInflater.from(getContext()).inflate(R.layout.head, null);
        measureView(mHeaderView);
        headContentHeight = mHeaderView.getMeasuredHeight();
        listView.addHeaderView(getHeader(), null, false);
        mArrowImageView = (ImageView) getHeader().findViewById(R.id.head_arrowImageView);
        mArrowImageView.setMinimumWidth(70);
        mArrowImageView.setMinimumHeight(50);
        mProgressBar = (ProgressBar) getHeader().findViewById(R.id.head_progressBar);
        mTipsTextview = (TextView) getHeader().findViewById(R.id.head_tipsTextView);
        mLastUpdatedTextView = (TextView) getHeader().findViewById(R.id.head_lastUpdatedTextView);
        mLastUpdatedTextView.setText("还没有刷新");
        mAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setDuration(250);
        mAnimation.setFillAfter(true);

        mReverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseAnimation.setInterpolator(new LinearInterpolator());
        mReverseAnimation.setDuration(200);
        mReverseAnimation.setFillAfter(true);
        mState = DONE;
        changeHeaderViewByState(mState);
    }

    public View getHeader() {
        return mHeaderView;
    }

    public Context getContext() {
        return mAdapterView.getContext();
    }

    /**
     * 你可以主动迫使生成下拉刷新事件，不一定成功，谨慎使用
     */
    public void onRefresh() {
        if (mOnRefreshListener == null || mState == REFRESHING || mState == LOADING)
            return;
        mState = REFRESHING;
        changeHeaderViewByState(mState);
        mOnRefreshListener.onRefresh();
    }

    /**
     * 结束刷新事件，将view还原至初始状态
     */
    public void onRefreshComplete() {
        mState = DONE;
        mLastUpdatedTextView.setText(sdf.format(new Date()));
        changeHeaderViewByState(mState);
    }

    public OnRefreshListener getOnRefreshListener() {
        return mOnRefreshListener;
    }

    public void setOnRefreshListener(OnRefreshListener l) {
        this.mOnRefreshListener = l;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mOnRefreshListener == null)
            return false;
        super.onTouch(v, event);
        return false;
    }

    protected void changeHeaderViewByState(int state) {
        switch (state) {
        case RELEASE_To_REFRESH:
            mArrowImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mTipsTextview.setVisibility(View.VISIBLE);
            mLastUpdatedTextView.setVisibility(View.VISIBLE);
            mArrowImageView.clearAnimation();
            mArrowImageView.startAnimation(mAnimation);
            mTipsTextview.setText("请释放刷新");
            break;
        case PULL_To_REFRESH:
            mProgressBar.setVisibility(View.GONE);
            mTipsTextview.setVisibility(View.VISIBLE);
            mLastUpdatedTextView.setVisibility(View.VISIBLE);
            mArrowImageView.clearAnimation();
            mArrowImageView.setVisibility(View.VISIBLE);
            mArrowImageView.clearAnimation();
            mArrowImageView.startAnimation(mReverseAnimation);
            mTipsTextview.setText("请下拉刷新");
            break;
        case REFRESHING:
            getHeader().setPadding(0, 0, 0, 0);
            mProgressBar.setVisibility(View.VISIBLE);
            mArrowImageView.clearAnimation();
            mArrowImageView.setVisibility(View.GONE);
            mTipsTextview.setText("正在加载中 ...");
            mLastUpdatedTextView.setVisibility(View.VISIBLE);
            if (mOnRefreshListener != null)
                mOnRefreshListener.onRefresh();
            break;
        case DONE:
            getHeader().setPadding(0, -1 * headContentHeight, 0, 0);
            mProgressBar.setVisibility(View.GONE);
            mArrowImageView.setVisibility(View.GONE);
            mArrowImageView.clearAnimation();
            mArrowImageView.setImageResource(R.drawable.arrow);
            mTipsTextview.setText("已经加载完毕 ");
            mLastUpdatedTextView.setVisibility(View.VISIBLE);
            break;
        }
    }

    public static interface OnRefreshListener {
        public void onRefresh();
    }

    @Override
    protected int getVisiableHeight() {
        return headContentHeight;
    }
}
