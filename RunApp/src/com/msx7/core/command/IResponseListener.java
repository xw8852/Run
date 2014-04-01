package com.msx7.core.command;

import com.msx7.core.command.model.Response;


public interface IResponseListener {
	
	public void onSuccess(Response response);

	public void onError(Response response);
	
}
