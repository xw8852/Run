package com.msx7.core.command.impl;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import com.msx7.core.command.AbstractHttpCommand;

public class HttpGetStringCommand extends AbstractHttpCommand{

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

    @Override
    protected Object getSuccessResponse(HttpResponse response) {
        
        try {
            return EntityUtils.toString(response.getEntity());
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.getSuccessResponse(response);
    }

}
