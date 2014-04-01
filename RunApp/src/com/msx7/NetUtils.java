/**
 * Project Name:Msx7
 * File Name:NetUtils.java
 * Package Name:com.msx7.util
 * Date: 2013-2-26 下午12:39:31
 * Copyright (c) 2013
 * Company:www.msx7.com
 *
*/

package com.msx7;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * ClassName:NetUtils <br/>
 * @Description:
 * Date:     2013-2-26 下午12:39:31 <br/>
 * @author   xiaowei
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class NetUtils {

    /**
     * 
     * isNetworkWell:检测网络连接是否开启. <br/>
     * 包含wifi、gprs. <br/>
     * @author xiaowei
     * @param ctx
     * @return
     * @since JDK 1.6
     */
    public static boolean isNetworkWell(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnected()) { return true; }
        }
        return false;
    }
}

