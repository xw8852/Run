package baidumapsdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.PoiOverlay;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class PanoramaPoiSelectorActivity extends FragmentActivity {

    MKSearch mSearch = null;
    MapView mMapView = null;

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
		setContentView(R.layout.activity_panorama_poi_selector);
        initMap();
        initSearcher();
	}

    private void initMap(){
        mMapView = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMapView();
        GeoPoint p = new GeoPoint((int)(39.945 * 1E6), (int)(116.404 * 1E6));
        mMapView.getController().setCenter(p);
        mMapView.getController().setZoom(13);
    }

    private void initSearcher(){
        mSearch = new MKSearch();
        mSearch.init(((DemoApplication) this.getApplication()).mBMapManager, new MKSearchListener() {
            @Override
            public void onGetPoiResult(MKPoiResult res, int type, int iError) {
                if (iError != 0){
                    Toast.makeText(PanoramaPoiSelectorActivity.this,
                            "抱歉，未能找到结果",Toast.LENGTH_SHORT).show();
                    return ;
                }
                if (res.getCurrentNumPois() > 0) {
                    // 将poi结果显示到地图上
                    SelectPoiOverlay poiOverlay = new SelectPoiOverlay(PanoramaPoiSelectorActivity.this, mMapView);
                    poiOverlay.setData(res.getAllPoi());
                    mMapView.getOverlays().clear();
                    mMapView.getOverlays().add(poiOverlay);
                    mMapView.refresh();
                    //当ePoiType为2（公交线路）或4（地铁线路）时， poi坐标为空
                    for( MKPoiInfo info : res.getAllPoi() ){
                        if ( info.pt != null ){
                            mMapView.getController().animateTo(info.pt);
                            break;
                        }
                    }
                }

            }

            @Override
            public void onGetTransitRouteResult(MKTransitRouteResult result, int iError) {
            }

            @Override
            public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
            }

            @Override
            public void onGetWalkingRouteResult(MKWalkingRouteResult result, int iError) {
            }

            @Override
            public void onGetAddrResult(MKAddrInfo result, int iError) {
            }

            @Override
            public void onGetBusDetailResult(MKBusLineResult result, int iError) {
            }

            @Override
            public void onGetSuggestionResult(MKSuggestionResult result, int iError) {
            }

            @Override
            public void onGetPoiDetailSearchResult(int type, int iError) {
            }

            @Override
            public void onGetShareUrlResult(MKShareUrlResult result, int type, int error) {

            }
        });
    }

    public void doPoiSearch(View v) {
        mSearch.poiSearchInCity(((EditText) findViewById(R.id.city)).getText().toString(),
                ((EditText) findViewById(R.id.key)).getText().toString());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
       if (mSearch!=null){
           mSearch.destory();
           mSearch = null;
       }
    }

    private class SelectPoiOverlay extends PoiOverlay {


        public SelectPoiOverlay(Activity activity, MapView mapView) {
            super(activity, mapView);
        }

        @Override
        protected boolean onTap(int i) {
            super.onTap(i);
            MKPoiInfo info = getPoi(i);
            if (!info.isPano) {
               Toast.makeText(PanoramaPoiSelectorActivity.this,
                       "当前POI当不包含全景信息",Toast.LENGTH_SHORT).show();
            }
            else{
                Intent intent = new Intent();
                intent.setClass(PanoramaPoiSelectorActivity.this,
                        PanoramaDemoActivityMain.class);
                intent.putExtra("uid",info.uid);
                startActivity(intent);
            }
            return true;
        }
    }

}
