package com.msx7.core;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;

public class Controller extends Application {
    private static Controller instance;
    private Handler mHandler;
    public static final boolean DEBUG = true;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static final Controller getApplication() {
        return instance;
    }

    public Handler getHandler() {
        if (mHandler == null)
            mHandler = new Handler();
        return mHandler;
    }

    List<Activity> activities = new ArrayList<Activity>();

    public void addActivityHistory(Activity activity) {
        activities.add(activity);
    }

    public void finishAllHistory() {
        for (Activity activity : activities) {
            if (activity != null)
                activity.finish();
        }
    }
    // public void clearImageCache(){
    // mLoader.clearCache();
    // }
    //
    // public void loadThumbnailImage(String key, ImageView imageView, Bitmap
    // loadingBitmap) {
    // mLoader.loadThumbnailImage(key, imageView, loadingBitmap);
    // }
    //
    // public void loadThumbnailImage(String key, ImageView imageView, int
    // resId) {
    // mLoader.loadThumbnailImage(key, imageView, resId);
    // }
    //
    // public void loadThumbnailImage(String key, ImageView imageView) {
    // mLoader.loadThumbnailImage(key, imageView);
    // }
    //
    // public void loadImage(String key, ImageView imageView, Bitmap
    // loadingBitmap) {
    // mLoader.loadImage(key, imageView, loadingBitmap);
    // }
    //
    // public void loadImage(String key, ImageView imageView, int resId) {
    // mLoader.loadImage(key, imageView, resId);
    // }
    //
    // public void loadImage(String key, ImageView imageView) {
    // mLoader.loadImage(key, imageView);
    // }
}
