package com.android.ivymobi.pedometer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.ivymobi.runapp.R;

public class RoundImageView extends ImageView {
    private int gap = 0;

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGap(4);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView, defStyle, 0);
        gap = a.getDimensionPixelSize(R.styleable.RoundImageView_gap, 0);
        a.recycle();
    }

    public RoundImageView(Context context) {
        super(context);
    }
    Path clip;
    Paint paint;
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawColor(Color.TRANSPARENT);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Drawable drawable = getDrawable();
        if (drawable != null) {
            clip = configureCircle();
            canvas.clipPath(clip);
        }
        paint = new Paint();
        paint.setAntiAlias(true);  
        paint.setFilterBitmap(true);  
        paint.setDither(true); 
        paint.setStrokeWidth(4);
        paint.setColor(Color.WHITE);
        paint.setStyle(Style.FILL);
        canvas.drawPath(clip, paint);
        super.onDraw(canvas);
        

    }

    /**
     * 根据scaleType 设置的src，来确定圆圈的大小，中心位置
     * 
     * @return
     */
    private Path configureCircle() {
        Path path = new Path();
        float r = 0;
        float centerx = (getRight() - getLeft()) / 2;
        float centery = (getBottom() - getTop()) / 2;
        if (getDrawable() == null) {
            return path;
        }

        int dwidth = getDrawable().getIntrinsicWidth();
        int dheight = getDrawable().getIntrinsicHeight();

        int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int vheight = getHeight() - getPaddingTop() - getPaddingBottom();
        centerx = centerx + (getPaddingLeft() - getPaddingRight()) / 2;
        centery = centery + (getPaddingTop() - getPaddingBottom()) / 2;

        boolean fits = (dwidth < 0 || vwidth == dwidth) && (dheight < 0 || vheight == dheight);

        if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == getScaleType()) {

            r = Math.min(vwidth, vheight) / 2;
            path.addCircle(centerx, centery, r, Direction.CCW);

        } else {

            r = Math.min(dwidth, dheight) / 2;
            path.addCircle(centerx, centery, r, Direction.CCW);
            if (ScaleType.MATRIX == getScaleType() || fits || ScaleType.CENTER == getScaleType()) {
                path.reset();
                r = Math.min(vwidth, vheight) / 2;
                r = Math.min(r, Math.min(dheight, dwidth) / 2);

                path.addCircle(centerx, centery, r, Direction.CCW);
            } else if (ScaleType.CENTER_CROP == getScaleType()) {

                float scale;

                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                }
                r = Math.min(dheight * scale, dwidth * scale) / 2;
                r = Math.min(r, Math.min(vheight, vwidth) / 2);
                path.reset();
                path.addCircle(centerx, centery, r, Direction.CCW);

            } else if (ScaleType.CENTER_INSIDE == getScaleType()) {
                float scale;

                if (dwidth <= vwidth && dheight <= vheight) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) vwidth / (float) dwidth, (float) vheight / (float) dheight);
                }

                r = Math.min(dheight * scale, dwidth * scale) / 2;
                r = Math.min(r, Math.min(vheight, vwidth) / 2);
                path.reset();
                path.addCircle(centerx, centery, r, Direction.CCW);
            } else {
                RectF mTempSrc = new RectF();
                RectF mTempDst = new RectF();

                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);

                Matrix mDrawMatrix = new Matrix();
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst, scaleTypeToScaleToFit(getScaleType()));
                float[] values = new float[9];
                mDrawMatrix.getValues(values);
                centerx = getPaddingLeft() + values[2] + dwidth * values[0] / 2.0f;
                centery = getPaddingTop() + values[5] + dheight * values[4] / 2.0f;
                path.reset();
                r = Math.min(dwidth * values[0], dheight * values[4]) / 2.0f;
                r = Math.min(r, Math.min(vheight, vwidth) / 2);
                path.addCircle(centerx, centery, r, Direction.CCW);
            }

        }
        return path;
    }

    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st) {
        if (st == ScaleType.FIT_XY) {
            return Matrix.ScaleToFit.FILL;
        } else if (st == ScaleType.FIT_START) {
            return Matrix.ScaleToFit.START;
        } else if (st == ScaleType.FIT_CENTER) {
            return Matrix.ScaleToFit.CENTER;
        }
        return Matrix.ScaleToFit.END;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

}
