package com.android.ivymobi.pedometer.util;

import com.msx7.core.Controller;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    private static final void makeText(Context context, String text, boolean isLong) {
        Toast.makeText(context, text, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static final void showToast(String text, boolean isLong) {
        makeText(Controller.getApplication(), text, isLong);
    }

    public static final void showLongToast(String text) {
        showToast(text, true);
    }

    public static final void showShortToast(String text) {
        showToast(text, false);
    }

    public static final void showToast(int res, boolean isLong) {
        makeText(Controller.getApplication(), Controller.getApplication().getString(res), isLong);
    }

    public static final void showLongToast(int res) {
        showToast(res, true);
    }

    public static final void showShortToast(int res) {
        showToast(res, false);
    }

}
