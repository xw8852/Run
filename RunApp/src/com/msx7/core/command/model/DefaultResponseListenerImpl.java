/**   
 * @Title: ResponseListenerImpl.java 
 * @Package com.yhiker.playmate.core.common 
 * @Description: TODO
 * @author xiaowei   
 * @date 2012-7-23 下午4:17:48 
 * @version V1.0   
 */
package com.msx7.core.command.model;

import android.os.Handler;

import com.msx7.core.command.IResponseListener;

/**
 * 
 * @author 作者 xiaowei
 * @创建时间 2012-7-23 下午4:17:48<br/>
 *       Function： 将结果转发至{@link #mHandler}所在的线程执行
 * 
 */
public class DefaultResponseListenerImpl implements IResponseListener {

	Handler mHandler;
	IResponseListener listener;
	Response data;

	public DefaultResponseListenerImpl(Handler mHandler, IResponseListener listener) {
		super();
		this.mHandler = mHandler;
		this.listener = listener;
	}

	public Response getData() {
		return data;
	}

	public void setData(Response data) {
		this.data = data;
	}

	@Override
	public void onSuccess(Response response) {
		setData(response);
		if (listener == null) return;
		if (mHandler == null) {
			listener.onSuccess(response);
			return;
		}
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				listener.onSuccess(getData());

			}
		});
	}

	@Override
	public void onError(Response response) {
		setData(response);
		if (listener == null) return;
		if (mHandler == null) {
			listener.onError(response);
			return;
		}
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				listener.onError(getData());
			}
		});
	}

}
