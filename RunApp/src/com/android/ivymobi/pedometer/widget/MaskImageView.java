package com.android.ivymobi.pedometer.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MaskImageView extends ImageView {
    Rect rect;
    Path clip;
    Paint paint;

    public MaskImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public MaskImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public MaskImageView(Context context) {
        super(context);

    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        
        canvas.save();
        
        canvas.drawColor(Color.TRANSPARENT);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        rect = new Rect();
        getFocusedRect(rect);
        clip = new Path();
        clip.addCircle(rect.centerX(), rect.centerY(), Math.min(rect.width() / 2, rect.height() / 2), Direction.CW);
        canvas.clipPath(clip);
        super.onDraw(canvas);
        paint = new Paint();
        paint.setAntiAlias(true);  
        paint.setFilterBitmap(true);  
        paint.setDither(true); 
        paint.setColor(Color.WHITE);
        paint.setStyle(Style.FILL);
        canvas.drawPath(clip, paint);
        
    }
}
