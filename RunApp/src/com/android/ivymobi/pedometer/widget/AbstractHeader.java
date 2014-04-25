package com.android.ivymobi.pedometer.widget;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;

public abstract class AbstractHeader implements OnTouchListener {
    public final static int RELEASE_To_REFRESH = 0;
    public final static int PULL_To_REFRESH = 1;
    public final static int REFRESHING = 2;// 正在刷新
    public final static int DONE = 3;// 刷新完成
    public final static int LOADING = 4;
    protected static final int RATIO = 3;

    protected AdapterView<?> mAdapterView;
    protected boolean isDragOut;
    protected int mStartY;
    protected int mState;
    protected boolean isRecored;

    public AbstractHeader(AdapterView<?> adapterView, boolean isDragOut) {
        super();
        this.mAdapterView = adapterView;
        this.isDragOut = isDragOut;
        if (adapterView == null)
            throw new IllegalArgumentException(" the 'adapterView' must be not null!");
        mAdapterView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int firstItemIndex = mAdapterView.getFirstVisiblePosition();
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (firstItemIndex == 0 && !isRecored) {
                isRecored = true;
                mStartY = (int) event.getY();
            }
            break;

        case MotionEvent.ACTION_UP:
            if (mState != REFRESHING && mState != LOADING) {
                if (mState == PULL_To_REFRESH) {
                    mState = DONE;
                    changeHeaderViewByState(mState);
                } else if (mState == RELEASE_To_REFRESH) {
                    mState = REFRESHING;
                    changeHeaderViewByState(mState);
                }
            }
            isRecored = false;
            break;

        case MotionEvent.ACTION_MOVE:
            int tempy = (int) event.getY();
            if (!isRecored && firstItemIndex == 0) {
                isRecored = true;
                mStartY = tempy;
            }
            if (mStartY != REFRESHING && isRecored && mStartY != LOADING) {
                if (mState == RELEASE_To_REFRESH) {
                    mAdapterView.setSelection(0);
                    if (((tempy - mStartY) / RATIO < getVisiableHeight()) && (tempy - mStartY) > 0) {
                        mState = PULL_To_REFRESH;
                        changeHeaderViewByState(mState);
                    } else if (tempy - mStartY <= 0) {
                        mState = DONE;
                        changeHeaderViewByState(mState);
                    }
                }
                if (mState == PULL_To_REFRESH) {
                    mAdapterView.setSelection(0);
                    if ((tempy - mStartY) / RATIO >= getVisiableHeight()) {
                        mState = RELEASE_To_REFRESH;
                        changeHeaderViewByState(mState);
                    } else if (tempy - mStartY <= 0) {
                        mState = DONE;
                        changeHeaderViewByState(mState);
                    }
                }
                if (mState == DONE) {
                    if (tempy - mStartY > 0) {
                        mState = PULL_To_REFRESH;
                        changeHeaderViewByState(mState);
                    }
                }
                if (mState == PULL_To_REFRESH) {
                    int paddingTop = -1 * getVisiableHeight() + (tempy - mStartY) / RATIO;
                    if (!isDragOut)
                        paddingTop = Math.min(paddingTop, 0);
                    getHeader().setPadding(0, paddingTop, 0, 0);
                }
                if (mState == RELEASE_To_REFRESH) {
                    int paddingTop = (tempy - mStartY) / RATIO - getVisiableHeight();
                    getHeader().setPadding(0, paddingTop, 0, 0);
                }
            }
            break;

        }
        return false;
    }

    protected void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    protected abstract int getVisiableHeight();

    public abstract View getHeader();

    protected abstract void changeHeaderViewByState(int state);

}
