package baidumapsdk.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.panorama.Panorama;
import com.baidu.mapapi.panorama.PanoramaLink;
import com.baidu.mapapi.panorama.PanoramaMarker;
import com.baidu.mapapi.panorama.PanoramaOverlay;
import com.baidu.mapapi.panorama.PanoramaService;
import com.baidu.mapapi.panorama.PanoramaService.PanoramaServiceCallback;
import com.baidu.mapapi.panorama.PanoramaView;
import com.baidu.mapapi.panorama.PanoramaViewCamera;
import com.baidu.mapapi.panorama.PanoramaViewListener;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/**
 * 全景Demo主Activity
 */
public class PanoramaDemoActivityMain extends Activity implements PanoramaViewListener{
    @SuppressWarnings("unused")
    private static final String LTAG = PanoramaDemoActivityMain.class.getSimpleName();
    private PanoramaView mPanoramaView;
    private PanoramaService mService;
    private PanoramaServiceCallback mCallback;
    private MyOverlay mOverlay = null;
    private int mSrcType = -1;
    private Button mBtn = null;
    private boolean isShowOverlay = true;
    private ProgressDialog pd;
    private TextView mRoadName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //先初始化BMapManager
        DemoApplication app = (DemoApplication) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(this);

            app.mBMapManager.init(
                    new DemoApplication.MyGeneralListener());
        }
        setContentView(R.layout.activity_panorama_main);
        mPanoramaView = (PanoramaView) findViewById(R.id.panorama);
        //UI初始化
        mBtn = (Button) findViewById(R.id.button);
        mBtn.setVisibility(View.INVISIBLE);
        //道路名
        mRoadName = (TextView)findViewById(R.id.road);
        mRoadName.setVisibility(View.VISIBLE);
        mRoadName.setText("百度全景");
        mRoadName.setBackgroundColor(Color.argb(200, 5, 5, 5));  //背景透明度
        mRoadName.setTextColor(Color.argb(255, 250, 250, 250));  //文字透明度
        mRoadName.setTextSize(22);
        //跳转进度条
        pd = new ProgressDialog(PanoramaDemoActivityMain.this);
        pd.setMessage("跳转中……");   
        pd.setCancelable(true);//设置进度条是否可以按退回键取消 

        //初始化Searvice
        mService = PanoramaService.getInstance(getApplicationContext());
        mCallback = new PanoramaServiceCallback() {
            public void onGetPanorama(Panorama p, int error) {
                //使用pid进入时添加标注
                if ( error != 0){
                    Toast.makeText(PanoramaDemoActivityMain.this,
                            "抱歉，未能检索到全景数据",Toast.LENGTH_LONG).show();
                }
                if (p != null) {
                    mPanoramaView.setPanorama(p);
                    mRoadName.setText(p.getStreetName());
                }
            }
        };
        //设置全景图监听
        mPanoramaView.setPanoramaViewListener(this);
                
        //解析输入
        parseInput();
        
    }

    private void parseInput() {
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (intent.hasExtra("uid")) {
            mSrcType = 1;
            mService.requestPanoramaByPoi(b.getString("uid"), mCallback);
            return;
        }
        if (intent.hasExtra("lat") && intent.hasExtra("lon")) {
            mSrcType = 2;
            mService.requestPanoramaByGeoPoint(new GeoPoint(b.getInt("lat"), b.getInt("lon")), mCallback);
            return;
        }
        if (intent.hasExtra("pid")) {
            mSrcType = 3;
            mService.requestPanoramaById(b.getString("pid"), mCallback);

        }
    }

    //处理button点击
    public void onButtonClick(View v) {
        if (isShowOverlay) {
            addOverlay();
            mBtn.setText("删除标注");
        } else {
            removeOverlay();
            mBtn.setText("添加标注");
        }
        isShowOverlay = !isShowOverlay;

    }

    //添加标注
    private void addOverlay() {
        //天安门坐标
        GeoPoint p = new GeoPoint(39914195,116403928);
        mOverlay = new MyOverlay(getResources().getDrawable(R.drawable.icon_marka),
                mPanoramaView);
        PanoramaMarker item = new PanoramaMarker(p);
        item.setMarker(getResources().getDrawable(R.drawable.icon_marka));
        mOverlay.addMarker(item);
        mPanoramaView.getOverlays().add(mOverlay);
        mPanoramaView.refresh();
    }

    //删除标注
    private void removeOverlay() {
        if (mOverlay != null) {
            mPanoramaView.getOverlays().remove(mOverlay);
            mPanoramaView.refresh();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mPanoramaView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPanoramaView.onResume();
        if (mSrcType == 3){
            mBtn.setVisibility(View.VISIBLE);
            ((ViewGroup)findViewById(R.id.layout))
                    .bringChildToFront(mBtn);
        }
    }

    @Override
    protected void onDestroy() {
        mPanoramaView.destroy();
        mService.destroy();
        super.onDestroy();
    }

    public class MyOverlay extends PanoramaOverlay {

        public MyOverlay(Drawable defaultMarker, PanoramaView mapView) {
            super(defaultMarker, mapView);
        }

        @Override
        public boolean onTap(int index) {
            Toast.makeText(PanoramaDemoActivityMain.this,
                    "标注已被点击", Toast.LENGTH_SHORT).show();
            return true;
        }

    }

    @Override
    public void beforeMoveToPanorama(String pId) {
        // TODO Auto-generated method stub
        //启动进度条
        pd.show();
    }

    @Override
    public void afterMovetoPanorama(String pId) {
        // TODO Auto-generated method stub
        //隐藏进度条
        pd.dismiss();
    }

    @Override
    public void onPanoramaCameraChange(PanoramaViewCamera camera) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClickPanoramaLink(PanoramaLink link) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPanoramaMoveStart() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPanoramaMoveFinish() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPanoramaAnimationStart() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPanoramaAnimationEnd() {
        // TODO Auto-generated method stub
        
    }

}
