package com.msx7.core.command;

import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;



public abstract class AbstractBaseCommand implements ICommand {
	private Request request;
	private Response response;
	private IResponseListener responseListener;
	private boolean terminated;

	public AbstractBaseCommand() {
		super();
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public IResponseListener getResponseListener() {
		return responseListener;
	}

	public void setResponseListener(IResponseListener responseListener) {
		this.responseListener = responseListener;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}
	
	/**
	 * notify the {#IResponseListener}
	 * @param success
	 */
	protected void notifyListener(boolean success)
	{
		IResponseListener responseListener = getResponseListener();
		if(responseListener != null)
		{
			if(success)
			{
				responseListener.onSuccess(getResponse());
			} else
			{
				responseListener.onError(getResponse());
			}
		}
	}
	

}
