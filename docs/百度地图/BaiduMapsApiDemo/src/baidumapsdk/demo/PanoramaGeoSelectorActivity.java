package baidumapsdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKMapTouchListener;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class PanoramaGeoSelectorActivity extends FragmentActivity {

    private MapView mMapView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
        /**
         * 由于MapView在setContentView()中初始化,所以它需要在BMapManager初始化之后
         */
        setContentView(R.layout.activity_panorama_geo_selector);
        initMap();
    }

    private void initMap(){
        mMapView = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMapView();
        GeoPoint p = new GeoPoint((int)(39.945 * 1E6), (int)(116.404 * 1E6));
        mMapView.getController().setCenter(p);
        mMapView.getController().setZoom(13);
        mMapView.regMapTouchListner(new MKMapTouchListener() {
            @Override
            public void onMapClick(GeoPoint point) {
               updateUI(point);
            }

            @Override
            public void onMapDoubleClick(GeoPoint point) {

            }

            @Override
            public void onMapLongClick(GeoPoint point) {

            }
        });
    }

    public void startPanorama(View v){
        float lat = Float.parseFloat(((EditText)findViewById(R.id.lat)).getText().toString());
        float lon = Float.parseFloat(((EditText)findViewById(R.id.lon)).getText().toString());
        Intent intent = new Intent();
        intent.setClass(PanoramaGeoSelectorActivity.this,
                PanoramaDemoActivityMain.class);
        intent.putExtra("lon", (int) (lon * 1E6));
        intent.putExtra("lat",(int)(lat * 1E6));
        startActivity(intent);
    }

    private void updateUI(GeoPoint p){
        ((EditText)findViewById(R.id.lat)).setText(String.format("%.3f",p.getLatitudeE6()*1E-6));
        ((EditText)findViewById(R.id.lon)).setText(String.format("%.3f",p.getLongitudeE6()*1E-6));
    }

    public void onDestroy(){
        super.onDestroy();
    }



}
