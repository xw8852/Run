package baidumapsdk.demo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MapFragmentDemo extends FragmentActivity {
	private static final String LTAG = MapFragmentDemo.class.getSimpleName();
	SupportMapFragment map;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
         * 使用地图sdk前需先初始化BMapManager.
         * BMapManager是全局的，可为多个MapView共用，它需要地图模块创建前创建，
         * 并在地图地图模块销毁后销毁，只要还有地图模块在使用，BMapManager就不应该销毁
         */
        DemoApplication app = (DemoApplication)this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(getApplicationContext());
            /**
             * 如果BMapManager没有初始化则初始化BMapManager
             */
            app.mBMapManager.init(new DemoApplication.MyGeneralListener());
        }
		Log.d(LTAG, "onCreate");
		setContentView(R.layout.activity_fragment);
		map = SupportMapFragment.newInstance();
		FragmentManager manager = getSupportFragmentManager();
		manager.beginTransaction().add(R.id.map, map, "map_fragment").commit();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d(LTAG, "onRestoreInstanceState");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(LTAG, "onRestart");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(LTAG, "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(LTAG, "onResume");
		MapController controller = map.getMapView().getController();
		controller.setCenter(new GeoPoint((int)(39.945 * 1E6), (int)(116.404 * 1E6)));
		controller.setZoom(13);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(LTAG, "onSaveInstanceState");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(LTAG, "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(LTAG, "onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(LTAG, "onDestory");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(LTAG, "onConfigurationChanged");
	}

}
