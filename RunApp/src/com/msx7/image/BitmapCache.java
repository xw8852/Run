package com.msx7.image;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;


/***
 * 地图切片缓存
 * 
 * @author maple
 * 
 */
public class BitmapCache  {

	// 开辟8M硬缓存空间
	private final int HARD_CACHED_SIZE = 8 * 1024 * 1024;
	private static final boolean DEBUG_SWITCH = true;
	private static final String TAG = BitmapCache.class.getSimpleName();
	private static BitmapCache instance = new BitmapCache();

	private BitmapCache() {
	}

	public static final BitmapCache getInstance() {
		return instance;
	}

	private final LruCache<String, Bitmap> sHardBitmapCache = new LruCache<String, Bitmap>(
			HARD_CACHED_SIZE) {
		protected int sizeOf(String key, Bitmap value) {
			return value.getRowBytes() * value.getHeight();
		};

		protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
			// 硬引用缓存区满，将一个最不经常使用的oldvalue推入到软引用缓存区
			sSoftBitmapCahe.put(key, new SoftReference<Bitmap>(oldValue));
		};
	};

	// 软引用
	private static final int SOFT_CACHE_CAPACITY = 40;
	@SuppressWarnings("serial")
	private static final LinkedHashMap<String, SoftReference<Bitmap>> sSoftBitmapCahe = new LinkedHashMap<String, SoftReference<Bitmap>>(
			SOFT_CACHE_CAPACITY, 0.75f, true) {
		public SoftReference<Bitmap> put(String key, SoftReference<Bitmap> value) {
			return super.put(key, value);
		};

		protected boolean removeEldestEntry(Entry<String, SoftReference<Bitmap>> eldest) {
			if (size() > SOFT_CACHE_CAPACITY) {
				return true;
			}
			return false;
		};
	};

	/***
	 * 缓存Bitmap
	 * 
	 * @param key
	 * @param bitmap
	 * @return
	 */
	public boolean putBitmap(String key, Bitmap bitmap) {
		if (bitmap == null) { return false; }
		synchronized (sHardBitmapCache) {
			if (sHardBitmapCache.get(key) == null) {
				sHardBitmapCache.put(key, bitmap);
			}
		}
		return true;
	}

	/***
	 * 从缓存中获取bitmap
	 * 
	 * @param key
	 * @return
	 */
	public Bitmap getBitmap(String key) {
		Bitmap bitmap = null;
		synchronized (sHardBitmapCache) {
			bitmap = sHardBitmapCache.get(key);
			if (bitmap != null) { return bitmap; }
		}
		// 硬引用缓存区间中读取失败，从软引用缓存区间读取
		synchronized (sSoftBitmapCahe) {
			SoftReference<Bitmap> bitmapReference = sSoftBitmapCahe.get(key);
			if (bitmapReference == null) { return null; }
			bitmap = bitmapReference.get();
			if (bitmap != null) { return bitmap; }
			sSoftBitmapCahe.remove(key);
		}
		return bitmap;
	}

	public boolean getDebugSwitch() {
		return DEBUG_SWITCH;
	};
}
