package baidumapsdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class PanoramaDemo extends Activity {
    //通过全景ID打开全景是使用的默认ID，全景ID可以使用PanoramaService查询得到
    public static final String DEFAULT_PANORAMA_ID ="0100220000130817164838355J5";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_panorama_demo);
	}

    //通过poi uid 打开全景
    public void startPoiSelector(View v){
        Intent intent = new Intent();
        intent.setClass(this, PanoramaPoiSelectorActivity.class);
        startActivity(intent);
    }

    //通过经纬度坐标开启全景
    public void startGeoSelector(View v){
        Intent intent = new Intent();
        intent.setClass(this, PanoramaGeoSelectorActivity.class);
        startActivity(intent);
    }
    //通过全景ID开启全景
    public void startIDSelector(View v){
        Intent intent = new Intent();
        intent.setClass(this, PanoramaDemoActivityMain.class);
        intent.putExtra("pid",DEFAULT_PANORAMA_ID);
        startActivity(intent);
    }


}
