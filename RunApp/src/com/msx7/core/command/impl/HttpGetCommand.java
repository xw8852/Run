package com.msx7.core.command.impl;

import org.apache.http.HttpResponse;

import com.msx7.core.command.AbstractHttpCommand;

public class HttpGetCommand extends AbstractHttpCommand{

	@Override
	protected byte[] getBody() {
		return null;
	}

	@Override
	protected String getContentType() {
		return null;
	}
	
	protected Object getErrorResponse(HttpResponse response) {
		return "HTTP ERROR C0DE : "+response.getStatusLine().getStatusCode();
	}

}
