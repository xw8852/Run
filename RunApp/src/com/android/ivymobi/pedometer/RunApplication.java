package com.android.ivymobi.pedometer;

import android.content.Context;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.msx7.core.Controller;

public class RunApplication extends Controller {
    public BMapManager mBMapManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        initEngineManager(this);
        
    }

    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(new MyGeneralListener())) {
            Toast.makeText(Controller.getApplication().getApplicationContext(), "BMapManager  初始化错误!",
                    Toast.LENGTH_LONG).show();
        }
    }

    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    static class MyGeneralListener implements MKGeneralListener {

        @Override
        public void onGetNetworkState(int iError) {
            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
                Toast.makeText(Controller.getApplication().getApplicationContext(), "您的网络出错啦！", Toast.LENGTH_LONG)
                        .show();
            } else if (iError == MKEvent.ERROR_NETWORK_DATA) {
                Toast.makeText(Controller.getApplication().getApplicationContext(), "输入正确的检索条件！", Toast.LENGTH_LONG)
                        .show();
            }
            // ...
        }

        @Override
        public void onGetPermissionState(int iError) {
            // //非零值表示key验证未通过
            // if (iError != 0) {
            // //授权Key错误：
            // Toast.makeText(Controller.getApplication.getApplicationContext(),
            // "请在 DemoApplication.java文件输入正确的授权Key,并检查您的网络连接是否正常！error: "+iError,
            // Toast.LENGTH_LONG).show();
            // DemoApplication.getInstance().m_bKeyRight = false;
            // }
            // else{
            // DemoApplication.getInstance().m_bKeyRight = true;
            // Toast.makeText(DemoApplication.getInstance().getApplicationContext(),
            // "key认证成功", Toast.LENGTH_LONG).show();
            // }
        }
    }
}
