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

import android.app.Activity;
import android.app.ProgressDialog;

import com.msx7.core.Manager;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.Request;

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
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
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

}
