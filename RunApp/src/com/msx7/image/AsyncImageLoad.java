package com.msx7.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.ivymobi.pedometer.util.MD5Util;
import com.msx7.core.Controller;
import com.msx7.core.Manager;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.impl.HttpGetCommand;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;

/**
 * 
 * @author 作者 xiaowei
 * @创建时间 2012-7-16 下午5:13:12 类说明
 * 
 */
public class AsyncImageLoad {

    public BitmapCache cache = BitmapCache.getInstance();

    static AsyncImageLoad load;

    private static final int LOADING_THREADS = 3;
    private Lock locker = new ReentrantLock();

    static ExecutorService threadPool = Executors.newFixedThreadPool(LOADING_THREADS);
    java.util.concurrent.ConcurrentHashMap<String, List<ImageView>> queue = new ConcurrentHashMap<String, List<ImageView>>();

    public static AsyncImageLoad getIntance() {
        return load == null ? load = new AsyncImageLoad() : load;
    }

    /**
     * 
     * @Title: loadImage
     * @Description: 加载图片
     * @param url
     *            图片的地址
     * @param imageView
     * @param callBack
     *            void 返回类型
     * @author xiaowei
     * @date 2012-7-16 下午5:40:49
     */
    public void loadImage(String url, ImageView mView, ImageCallBack callBack) {
        loadImageType(url, mView, new ImageData(callBack, ImageData.TYPE_NONE, url));
    }

    public void loadImageType(String url, ImageView mView, ImageData data) {
        if (mView == null)
            return;
        if (data == null) {
            data = new ImageData(null, ImageData.TYPE_NONE, url);
        }
        if (TextUtils.isEmpty(url))
            return;
        mView.setTag(data);
        if (cached(url, mView, data)) {
            return;
        } else if (hasDown(url, mView, data)) {
            return;
        } else
            down(url, mView, data);
    }

    /** 图片是否已经下载过 */
    private boolean hasDown(String url, ImageView mView, ImageData data) {
        String path = getPath(url);
        File file = new File(path);
        if (!file.exists() && file.length() < 1)
            return false;
        if (!TextUtils.isEmpty(url) && url.trim().startsWith("http://")) {
            path = null;
            return false;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        cache.putBitmap(url, bitmap);
        cached(url, mView, data);
        return true;
    }

    /** 图片是否已经缓存 */
    private boolean cached(String url, ImageView mView, ImageData data) {
        Bitmap bitmap = cache.getBitmap(url);
        if (bitmap == null)
            return false;
        if (mView != null) {
            ImageRunable runable = new ImageRunable(mView, bitmap, data);
            mView.post(runable);
        } else if (data != null && data.callBack != null) {
            data.callBack.callback(bitmap);
        }
        return true;
    }

    public String getPath(String url) {
        if (isExternalable()) {
            File file = getExternalCacheDir(Controller.getApplication());
            return file.getPath() + File.separator + MD5Util.getMD5String(url) + ".png";
        }
        return null;
    }

    boolean isExternalable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Get the external app cache directory.
     * 
     * @param context
     *            The context to use
     * @return The external cache dir
     */
    @TargetApi(8)
    public static File getExternalCacheDir(Context context) {
        File file = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            file = context.getExternalCacheDir();
        }
        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        if (file == null) {
            file = new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
        }
        return file;
    }

    /** 图片下载 */
    private void down(String url, ImageView mView, ImageData data) {
        locker.lock();
        try {
            Request request = new Request();
            request.url = url;
            String path = getPath(url);
            if (TextUtils.isEmpty(url) || !isExternalable()) {
                path = null;
            }
            if (pushImage(url, mView)) {
                return;
            }
            DownRunable run = new DownRunable(url, path, data, mView);
            ImageDownListener listener = new ImageDownListener(run);

            HttpGetCommand getCommand = new HttpGetCommand();

            getCommand.setRequest(request);
            getCommand.setResponseListener(listener);
            threadPool.execute(new Manager.ThreadCall(getCommand));
            queue.put(url, new ArrayList<ImageView>());
        } finally {
            locker.unlock();
        }
    }

    boolean pushImage(String key, ImageView imageView) {

        if (queue.containsKey(key)) {

            List<ImageView> list = queue.get(key);
            if (imageView != null)
                list.add(imageView);
            return true;
        }
        return false;

    }

    List<ImageView> pullImage(String key) {

        if (!queue.containsKey(key))
            return null;
        return queue.remove(key);

    }

    /** 图片下载监听 */
    class ImageDownListener implements IResponseListener {
        DownRunable runable;

        public ImageDownListener(DownRunable runable) {
            super();
            this.runable = runable;
        }

        @Override
        public void onSuccess(Response response) {
            runable.setInputStream((InputStream) response.getData());
            new Thread(runable).start();
        }

        @Override
        public void onError(Response response) {

        }

    }

    Object obj = new Object();

    /** 下载线程 */
    class DownRunable implements Runnable {
        String url;
        String path;
        ImageData data;
        ImageView mView;

        InputStream entity;

        public void setInputStream(InputStream entity) {
            this.entity = entity;
        }

        public DownRunable(String url, String path, ImageData data, ImageView mView) {
            this.url = url;
            this.path = path;
            this.data = data;
            this.mView = mView;
        }

        @Override
        public synchronized void run() {
            Bitmap bitmap = null;
            if (entity == null) {
                return;
            }
            bitmap = BitmapFactory.decodeStream(entity);
            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                    OutputStream out = new FileOutputStream(file);
                    bitmap.compress(CompressFormat.PNG, 100, out);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            cache.putBitmap(url, bitmap);
            ImageData _dataData = (ImageData) mView.getTag();
            if (!url.equals(_dataData.url))
                return;
            ImageRunable runable = new ImageRunable(mView, bitmap, data);
            mView.post(runable);
        }

        // 复制文件
        public void copy(InputStream sourceFileIs, File targetFile) throws IOException {
            FileOutputStream fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = sourceFileIs.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            sourceFileIs.close();
        }
    }

    /** 用户线程执行 */
    class ImageRunable implements Runnable {

        ImageView mView;
        Bitmap bitmap;
        ImageData data;

        public ImageRunable(ImageView mView, Bitmap bitmap, ImageData data) {
            super();
            this.mView = mView;
            this.bitmap = bitmap;
            this.data = data;
        }

        @Override
        public void run() {
            locker.lock();
            try {
                int type = 0;
                int width = -1;
                int height = -1;
                List<ImageView> views = new ArrayList<ImageView>();
                views = pullImage(data.url);
                if (views == null) {
                    views = new ArrayList<ImageView>();
                }
                views.add(mView);
                for (ImageView _View : views) {
                    ImageData _data = (ImageData) _View.getTag();
                    ViewGroup.LayoutParams aa;
                    switch (_data.Type) {
                    case ImageData.TYPE_FIT_BITMAP:
                        height = bitmap.getWidth();
                        width = bitmap.getHeight();
                        break;
                    case ImageData.TYPE_FIT_IMAGEVIEW_HEIGHT:
                        height = Math.max(_View.getMeasuredHeight(), _View.getHeight());
                        width = bitmap.getWidth() * height / bitmap.getHeight();
                        break;
                    case ImageData.TYPE_FIT_IMAGEVIEW_WIDTH:
                        width = Math.max(_View.getMeasuredWidth(), _View.getWidth());
                        height = bitmap.getHeight() * width / bitmap.getWidth();
                        break;
                    default:
                        break;
                    }

                    aa = _View.getLayoutParams();
                    if (aa != null && height > 0 && width > 0) {
                        aa.height = height;
                        aa.width = width;
                        _View.setLayoutParams(aa);
                    }
                    if (obj != null) {

                    }
                    if (_data.Type == ImageData.TYPE_GREY) {
                        _View.setImageBitmap(grey(bitmap));
                    } else
                        _View.setImageBitmap(bitmap);
                    _View.postInvalidate();
                    if (_data.callBack != null)
                        _data.callBack.callback(bitmap);
                }
            } finally {
                locker.unlock();
            }
        }

    }

    public static final Bitmap grey(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap faceIconGreyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(faceIconGreyBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return faceIconGreyBitmap;
    }

    public static class ImageData {
        /* 不作任何处理 */
        public static final int TYPE_NONE = 1;
        /* VIEW适应图片 */
        public static final int TYPE_FIT_BITMAP = TYPE_NONE << 1;
        /* 图片适应view，以view的宽度为基准，保持图片的纵横比 */
        public static final int TYPE_FIT_IMAGEVIEW_WIDTH = TYPE_NONE << 2;
        /* 图片适应view，以view的高度为基准，保持图片的纵横比 */
        public static final int TYPE_FIT_IMAGEVIEW_HEIGHT = TYPE_NONE << 3;
        public static final int TYPE_GREY = TYPE_NONE << 4;
        public ImageCallBack callBack;
        public int Type;
        public String url;

        public ImageData(ImageCallBack callBack, int type, String url) {
            super();
            this.callBack = callBack;
            Type = type;
            this.url = url;
        }

    }

    public static void cancelAllTasks() {
        threadPool.shutdownNow();
        threadPool = Executors.newFixedThreadPool(LOADING_THREADS);
    }
}
