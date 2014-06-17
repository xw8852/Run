/**
 * Project Name:HaiYou
 * File Name:BaseActivity.java
 * Package Name:com.hiker.onebyone.ui
 * Date:2013-2-1下午1:42:34
 * Copyright (c) 2013
 * Company:苏州海客科技有限公司
 *
 */

package com.android.ivymobi.pedometer;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;

import com.msx7.core.Manager;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.Request;
import com.umeng.analytics.MobclickAgent;

/**
 * ClassName:BaseActivity <br/>
 * 
 * @Description: Date: 2013-2-1 下午1:42:34 <br/>
 * @author maple
 * @version
 * @since JDK 1.6
 * @see
 */
public abstract class BaseActivity extends Activity {

    private ProgressDialog mProgressDialog;

    /**
     * 
     * showLoadingDialog:显示数据加载框. <br/>
     * 
     * @author maple
     * @since JDK 1.6
     */
    protected void showLoadingDialog(int msgId) {
        dismissLoadingDialog();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(msgId));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    /***
     * 
     * dismissDialog:关闭数据加载弹出框. <br/>
     * 
     * @author maple
     * @since JDK 1.6
     */
    protected void dismissLoadingDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    protected void goPost(Request request, IResponseListener listener) {
        Manager.getInstance().execute(Manager.CMD_JSON_POST, request, listener);
    }

    protected void goGet(Request request, IResponseListener listener) {
        Manager.getInstance().execute(Manager.CMD_GET_STRING, request, listener);
    }

    public static int jsonToInt(JSONObject jsonObject, String key)
			throws Exception {
		try {
			if (!jsonObject.isNull(key))
				return jsonObject.getInt(key);
		} catch (Exception e) {

		}
		return 0;
	}

	public static String jsonToString(JSONObject jsonObject, String key)
			throws Exception {
		try {
			if (!jsonObject.isNull(key))
				return jsonObject.getString(key);
		} catch (Exception e) {

		}
		return "";
	}

	public static long jsonToLong(JSONObject jsonObject, String key)
			throws Exception {
		try {
			if (!jsonObject.isNull(key))
				return jsonObject.getLong(key);
		} catch (Exception e) {

		}
		return 0;
	}
	
	public static float jsonToFloat(JSONObject jsonObject, String key)
			throws Exception {
		try {
			if (!jsonObject.isNull(key)){
				String value = jsonToString(jsonObject, "key");
				if (value != null && value.length() > 0) {
					return Float.parseFloat(value);
				}
			}
		} catch (Exception e) {

		}
		return 0;
	}
	
	
	
	public static boolean jsonToBoolean(JSONObject jsonObject, String key)
			throws Exception {
		try {
			if (!jsonObject.isNull(key))
				return jsonObject.getBoolean(key);
		} catch (Exception e) {

		}
		return false;
	}

	public static JSONArray jsonToArray(JSONObject jsonObject, String key)
			throws Exception {
		if (!jsonObject.isNull(key))
			return jsonObject.getJSONArray(key);
		return new JSONArray();
	}

	public static JSONObject jsonToJSON(JSONObject jsonObject, String key)
			throws Exception {
		if (!jsonObject.isNull(key))
			return jsonObject.getJSONObject(key);
		return null;
	}
    
	
	
}
