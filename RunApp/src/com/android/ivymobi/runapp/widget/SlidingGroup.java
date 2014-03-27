package com.android.ivymobi.runapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.android.ivymobi.runapp.R;


public class SlidingGroup extends ViewGroup {
    private static final float NANOTIME_DIV = 1000000000.0f;
    private static final float SMOOTHING_SPEED = 0.75f;
    private static final float SMOOTHING_CONSTANT = (float) (0.016 / Math.log(SMOOTHING_SPEED));
    private int offset = 60;
    private float mSmoothingTime;
    private float mTouchX;
    private SlidingOvershootInterpolator mScrollInterpolator;
    private Scroller mScroller;
    private int mDefaultScreen = 0;

    private int mCurrentScreen;
    private int mNextScreen;
    private boolean mFirstLayout = true;
    private static final float BASELINE_FLING_VELOCITY = 2500.f;
    private static final float FLING_VELOCITY_INFLUENCE = 0.4f;
    public static final int INVALID_SCREEN = -1999;
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;
    // 横竖屏
    private boolean mPortrait;
    private ISildingListener listener;

    public ISildingListener getListener() {
        return listener;
    }
    public void setListener(ISildingListener listener) {
        this.listener = listener;
    }

    public SlidingGroup(Context context) {
        super(context);
        initWorkspace();
    }

    public SlidingGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingGroup, defStyle, 0);
        mDefaultScreen = a.getInt(R.styleable.SlidingGroup_defaultPostion, 0);
        offset = a.getDimensionPixelSize(R.styleable.SlidingGroup_offset, 60);
        a.recycle();
        initWorkspace();
    }

    public SlidingGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Initializes various states for this workspace.
     */
    private void initWorkspace() {
        Context context = getContext();
        mScrollInterpolator = new SlidingOvershootInterpolator();
        mScroller = new Scroller(context, mScrollInterpolator);
        mCurrentScreen = mDefaultScreen;

    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        int childLeft = 0;
        final int count = getChildCount();
        if (count != 2) { throw new IllegalStateException(
                "The sliding must contain only 2 childview"); }
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = i == mDefaultScreen ? child.getMeasuredWidth() : child
                        .getMeasuredWidth() - offset;

                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        if (heightSpecSize > widthSpecSize != mPortrait && !mFirstLayout) {
            mPortrait = heightSpecSize > widthSpecSize;
            setHorizontalScrollBarEnabled(false);
            if (isDefault()) {
                scrollTo(getMeasuredWidth() - offset, 0);
            }else {
                scrollTo(0, 0);
            }
            setHorizontalScrollBarEnabled(true);
        }
        if (mFirstLayout) {
            setHorizontalScrollBarEnabled(false);
            snapToScreen(mDefaultScreen, 0, false);
            setHorizontalScrollBarEnabled(true);
            mFirstLayout = false;
        }

    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mTouchX = x;
        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
    }

    /**
     * 设置默认屏
     * 
     * @param screen
     */
    public void setDefaultScreen(int screen) {
        if (screen >= 0 && screen < getChildCount()) this.mDefaultScreen = screen;

    }

    /**
     * 滑动到默认屏
     */
    public void snapToDefault() {
        snapToScreen(mDefaultScreen, 0, true);
    }

    /**
     * 加载的View与当前的default View是同一类型（即同类），会失败 不能在显示默认屏的时候，改变默认屏的显示
     * 
     * @param view
     * @return
     */
    public boolean changeDefaultView(View view) {
        if (view == null
                || isDefault()
                || view.getClass().getName()
                        .equals(getChildAt(mDefaultScreen).getClass().getName())) { return false; }
        Log.d("BUG", view.getClass().getName());
        this.removeViewAt(mDefaultScreen);
        addView(view, mDefaultScreen);
        postInvalidate();
        return true;
    }

    /**
     * 滑动到下一屏
     */
    public void snapToNext() {
        int which = ++mCurrentScreen % 2;
        snapToScreen(which, 0, true);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
            mTouchX = mScroller.getCurrX();
            super.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            if (mNextScreen == -1) {
                mCurrentScreen = getChildCount() - 1;
                scrollTo(mCurrentScreen * getWidth(), getScrollY());
            } else if (mNextScreen == getChildCount()) {
                mCurrentScreen = 0;
                scrollTo(0, getScrollY());
            } else
                mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            // Launcher.setScreen(mCurrentScreen);
            mNextScreen = INVALID_SCREEN;
        } else if (mTouchState == TOUCH_STATE_SCROLLING) {
            final float now = System.nanoTime() / NANOTIME_DIV;
            final float e = (float) Math.exp((now - mSmoothingTime) / SMOOTHING_CONSTANT);
            final float dx = mTouchX - getScrollX();

            final int scrolltoX = getScrollX() + (int) (dx * e);
            super.scrollTo(scrolltoX, getScrollY());
            mSmoothingTime = now;

            // Keep generating points as long as we're more than 1px away from
            // the target
            if (dx > 1.f || dx < -1.f) {
                postInvalidate();
            }
        }
    }

    /**
     * 
     * @param whichScreen
     *            目的地
     * @param velocity
     *            滑动的速率 0：为默认值，负数则取其绝对值
     * @param settle
     *            是否启动动画插入器
     */
    private void snapToScreen(int whichScreen, int velocity, boolean settle) {
        // if (!mScroller.isFinished()) return;

        whichScreen = Math.max(-1, Math.min(whichScreen, getChildCount() - 0));
        mNextScreen = whichScreen;

        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurrentScreen
                && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }

        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
        final int tmp_offset = whichScreen * getMeasuredWidth() - offset;
        final int tmp_width = whichScreen * getMeasuredWidth();
        final int newX = whichScreen > mDefaultScreen ? tmp_offset
                : (whichScreen < mDefaultScreen ? tmp_width
                        : (mDefaultScreen > 0 && whichScreen == mDefaultScreen) ? tmp_offset
                                : tmp_width);
        final int delta = newX - getScrollX();
        int duration = (screenDelta + 1) * 100;

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        if (settle) {
            mScrollInterpolator.setDistance(screenDelta);
        } else {
            mScrollInterpolator.disableSettle();
        }

        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration += (duration / (velocity / BASELINE_FLING_VELOCITY))
                    * FLING_VELOCITY_INFLUENCE;
        } else {
            duration += 100;
        }

        awakenScrollBars(duration);
        mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
        invalidate();
        if(listener!=null){
            if(isDefault()){
                listener.changeDefault();
            }else {
                listener.changeSetting();
            }
        }
    }

    /**
     * 动画插入器
     * 
     * @author xiaowei
     * 
     */
    private static class SlidingOvershootInterpolator implements Interpolator {
        private static final float DEFAULT_TENSION = 1.3f;
        private float mTension;

        public SlidingOvershootInterpolator() {
            mTension = DEFAULT_TENSION;
        }

        public void setDistance(int distance) {
            mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;
        }

        public void disableSettle() {
            mTension = 0.f;
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mCurrentScreen == mDefaultScreen) return super.dispatchTouchEvent(ev);
        float x = ev.getX();
        float y = ev.getY();
        Rect rect = new Rect();
        getHitRect(rect);
        Rect rect2 = new Rect(rect.left, rect.top, rect.right - offset, rect.bottom);
        if (mDefaultScreen == 0) {
            rect2 = new Rect(rect.left + offset, rect.top, rect.right, rect.bottom);
        }
        if (rect2.contains((int) x, (int) y)) { return super.dispatchTouchEvent(ev); }
        snapToDefault();
        return true;

    }

    public int getCurrent() {
        return mCurrentScreen;
    }

    public boolean isDefault() {
        return mCurrentScreen == mDefaultScreen;
    }
    /**   

    /**
     * 
     * @author 作者 msx
     * @创建时间 2012-7-17 上午10:32:32 类说明
     * 
     */
    public static interface ISildingListener {
        /**滑动到设置页面，触发此监听*/
        public void changeSetting();
        /**滑动到默认页面，触发此监听*/
        public void changeDefault();
    }

}
