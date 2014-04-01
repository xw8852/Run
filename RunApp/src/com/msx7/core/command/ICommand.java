package com.msx7.core.command;

import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;



public interface ICommand {
	Request getRequest();

	void setRequest(Request request);

	Response getResponse();

	void setResponse(Response response);

	void execute();

	IResponseListener getResponseListener();

	void setResponseListener(IResponseListener listener);

	void setTerminated(boolean terminated);

	boolean isTerminated();

}
