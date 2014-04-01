package com.msx7.image;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.msx7.core.Controller;
import com.msx7.core.Manager;
import com.msx7.core.command.ICommand;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.impl.HttpGetCommand;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;

public class ImageLoader {

    public static final int IO_BUFFER_SIZE_BYTES = 1024;

    private static final int DISK_CACHE_INDEX = 0;
    private static final int HTTP_CACHE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final String HTTP_CACHE_DIR = "http";
    ExecutorService mPools;
    DiskLruCache mDiskLruCache;
    ImageCache mMemoryCache;
    int mImageWidth;
    int mImageHeight;
    private static final ImageLoader loader=new ImageLoader();

    protected ImageLoader() {
        mPools = Executors.newFixedThreadPool(3);
        mMemoryCache = new ImageCache(Controller.getApplication());
        DisplayMetrics displayMetrics = Controller.getApplication().getResources().getDisplayMetrics();
        mImageHeight = displayMetrics.heightPixels;
        mImageWidth = displayMetrics.widthPixels;
        try {
            File file=ImageCache.getDiskCacheDir(Controller.getApplication(), HTTP_CACHE_DIR);
            if(file!=null){
                if(!file.exists()){
                    file.mkdirs();
                }
                mDiskLruCache = DiskLruCache.open(file, 1, 1, HTTP_CACHE_SIZE);
            }
           
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final ImageLoader getInstance(){
        return loader;
    }
    public void loadThumbnailImage(String key, ImageView imageView, int resId) {
        loadThumbnailImage(key, imageView, BitmapFactory.decodeResource(Controller.getApplication().getResources(), resId));
    }

    public void loadThumbnailImage(String key, ImageView imageView, Bitmap loadingBitmap) {
        if (loadingBitmap != null) {
            imageView.setImageBitmap(loadingBitmap);
        }
        ImageData data = new ImageData(key, ImageData.IMAGE_TYPE_THUMBNAIL);
        loadImage(data, imageView);
    }

    public void loadThumbnailImage(String key, ImageView imageView) {
        loadThumbnailImage(key, imageView, null);
    }

    public void loadImage(String key, ImageView imageView, Bitmap loadingBitmap) {
        if (loadingBitmap != null) {
            imageView.setImageBitmap(loadingBitmap);
        }
        ImageData data = new ImageData(key, ImageData.IMAGE_TYPE_NORMAL);
        loadImage(data, imageView);
    }

    public void loadImage(String key, ImageView imageView, int resId) {
        loadImage(key, imageView, BitmapFactory.decodeResource(Controller.getApplication().getResources(), resId));
    }

    public void loadImage(String key, ImageView imageView) {
        loadImage(key, imageView, null);
    }

    public void loadImage(ImageData data, ImageView imageView) {
        String key = String.valueOf(data);
        imageView.setTag(key);
        Bitmap bitmap = null;
        bitmap = mMemoryCache.getBitmapFromMemCache(key);
        if (bitmap == null) {
            bitmap = getBitmapFromDisk(key);
            if (bitmap != null)
                mMemoryCache.addBitmapToCache(key, bitmap);
        }
        if (bitmap != null) {
            imageView.post(new ImageRunnable(imageView, key, bitmap));
        } else {
            DownBitmapFromUrl(data, imageView);
        }
    }

    protected void DownBitmapFromUrl(ImageData data, ImageView imageView) {
        ICommand cmd = new HttpGetCommand();
        Request request = new Request();
        request.url = data.mKey;
        cmd.setRequest(request);
        IResponseListener listener = new ImageResponseListener(data.mKey, imageView);
        cmd.setResponseListener(listener);
        mPools.execute(new Manager.ThreadCall(cmd));
    }

    private class ImageResponseListener implements IResponseListener {
        String key;
        ImageView mImageView;

        public ImageResponseListener(String key, ImageView mImageView) {
            super();
            this.key = key;
            this.mImageView = mImageView;
        }

        @Override
        public void onSuccess(Response response) {
            Log.d("MSG", " onSuccess  url:"+key);
            if (response == null)
                return;
            Log.d("MSG", " onSuccess   response url:"+key);
            Bitmap bitmap = null;
            Object data = response.getData();
            DiskLruCache.Snapshot snapshot;
            if (data == null || !(data instanceof InputStream))
                return;
            InputStream in = (InputStream) data;

            try {
                if (mDiskLruCache != null) {
                    snapshot = mDiskLruCache.get(key.hashCode()+"");
                    if (snapshot == null) {
                        DiskLruCache.Editor editor = mDiskLruCache.edit(key.hashCode()+"");
                        if (editor != null) {
                            if (downloadUrlToStream(in, editor.newOutputStream(DISK_CACHE_INDEX))) {
                                editor.commit();
                            } else
                                editor.abort();
                        }
                    }
                } else {
                    bitmap = BitmapFactory.decodeStream(in);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
            if(bitmap==null)bitmap = getBitmapFromDisk(key);
            if (bitmap != null) {
                mMemoryCache.addBitmapToCache(key, bitmap);
                mImageView.post(new ImageRunnable(mImageView, key, bitmap));
            }

        }

        @Override
        public void onError(Response response) {
            Log.d("MSG", " onError  url:"+key);
        }

    }

    public Bitmap getBitmapFromDisk(String key) {
        if (mDiskLruCache == null)
            return null;
        DiskLruCache.Snapshot snapshot;
        Bitmap bitmap = null;
        try {
            snapshot = mDiskLruCache.get(key.hashCode()+"");
            if (snapshot == null)
                return null;
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fd = fileInputStream.getFD();
            if (fd == null && fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                bitmap = decodeSampledBitmapFromDescriptor(fd, mImageWidth, mImageHeight);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Download a bitmap from a URL and write the content to an output stream.
     * 
     * @param urlString
     *            The URL to fetch
     * @param outputStream
     *            The outputStream to write to
     * @return true if successful, false otherwise
     */
    public boolean downloadUrlToStream(InputStream is, OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            in = new BufferedInputStream(is, IO_BUFFER_SIZE_BYTES);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE_BYTES);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            e.printStackTrace();
            // LOGE(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
            }
        }
        return false;
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and
     * height.
     * 
     * @param filename
     *            The full path of the file to decode
     * @param reqWidth
     *            The requested width of the resulting bitmap
     * @param reqHeight
     *            The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect
     *         ratio and dimensions that are equal to or greater than the
     *         requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested
     * width and height.
     * 
     * @param fileDescriptor
     *            The file descriptor to read from
     * @param reqWidth
     *            The requested width of the resulting bitmap
     * @param reqHeight
     *            The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect
     *         ratio and dimensions that are equal to or greater than the
     *         requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    /**
     * Calculate an inSampleSize for use in a
     * {@link android.graphics.BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This
     * implementation calculates the closest inSampleSize that will result in
     * the final decoded bitmap having a width and height equal to or larger
     * than the requested width and height. This implementation does not ensure
     * a power of 2 is returned for inSampleSize which can be faster when
     * decoding but results in a larger bitmap which isn't as useful for caching
     * purposes.
     * 
     * @param options
     *            An options object with out* params already populated (run
     *            through a decode* method with inJustDecodeBounds==true
     * @param reqWidth
     *            The requested width of the resulting bitmap
     * @param reqHeight
     *            The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    public  static class ImageData {
        public static final int IMAGE_TYPE_THUMBNAIL = 0;
        public static final int IMAGE_TYPE_NORMAL = 1;
        public String mKey;
        public int mType;

        public int width;
        public int height;

        public ImageData(String key, int type) {
            mKey = key;
            mType = type;
        }

        public ImageData(String mKey, int mType, int width, int height) {
            super();
            this.mKey = mKey;
            this.mType = mType;
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return mKey;
        }
    }

    private class ImageRunnable implements Runnable {
        ImageView mImageView;
        String key;
        Bitmap bitmap;

        public ImageRunnable(ImageView mImageView, String key, Bitmap bitmap) {
            super();
            this.mImageView = mImageView;
            this.key = key;
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            if (key.equals(mImageView.getTag())) {
                Rect outRect = new Rect();
                mImageView.getHitRect(outRect);
                int width = outRect.width();
                int height = bitmap.getHeight() * width / bitmap.getWidth();
                if (mImageView.getLayoutParams() != null && width > 10 && height > 10) {
                    ViewGroup.LayoutParams params = mImageView.getLayoutParams();
                    params.height = height;
                    params.width = width;
                    mImageView.setLayoutParams(params);
                }
                mImageView.setImageBitmap(bitmap);
            }
        }

    }

    public void clearCache() {
        mMemoryCache.clearCache();
    }
}
