package com.msx7.core;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

import com.msx7.image.ImageLoader;

public class Controller extends Application {
    private static Controller instance;
    private Handler mHandler;
    public static final boolean DEBUG = true;
    
    protected  ImageLoader   mLoader;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mLoader=ImageLoader.getInstance();
    }

    public static final Controller getApplication() {
        return instance;
    }

    public Handler getHandler() {
        if (mHandler == null)
            mHandler = new Handler();
        return mHandler;
    }
    
    public void clearImageCache(){
        mLoader.clearCache();
    }
    
    public void loadThumbnailImage(String key, ImageView imageView, Bitmap loadingBitmap) {
        mLoader.loadThumbnailImage(key, imageView, loadingBitmap);
    }

    public void loadThumbnailImage(String key, ImageView imageView, int resId) {
        mLoader.loadThumbnailImage(key, imageView, resId);
    }

    public void loadThumbnailImage(String key, ImageView imageView) {
        mLoader.loadThumbnailImage(key, imageView);
    }

    public void loadImage(String key, ImageView imageView, Bitmap loadingBitmap) {
        mLoader.loadImage(key, imageView, loadingBitmap);
    }

    public void loadImage(String key, ImageView imageView, int resId) {
        mLoader.loadImage(key, imageView, resId);
    }

    public void loadImage(String key, ImageView imageView) {
        mLoader.loadImage(key, imageView);
    }
}
