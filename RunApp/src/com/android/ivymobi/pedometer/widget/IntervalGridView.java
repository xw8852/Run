package com.android.ivymobi.pedometer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.GridView;

public class IntervalGridView extends GridView {

    public IntervalGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntervalGridView(Context context) {
        super(context);
    }

    public IntervalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int count = getChildCount();
        int width = getWidth();
        int y = 1;
        for (int i = 0; i < count; i = i + 4) {
            Rect rect = new Rect(0, getChildAt(i).getTop(), width, getChildAt(i).getBottom());
            Paint paint = new Paint();
            if (y % 2 != 0) {
                paint.setColor(0xf0f0f0);
            } else
                paint.setColor(Color.TRANSPARENT);
            canvas.drawRect(rect, paint);
            y++;
        }

        super.dispatchDraw(canvas);
    }
}
