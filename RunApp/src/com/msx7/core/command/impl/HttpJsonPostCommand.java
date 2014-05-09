package com.msx7.core.command.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;
import android.util.Log;

import com.msx7.core.command.AbstractHttpCommand;

public class HttpJsonPostCommand extends AbstractHttpCommand {

    public HttpJsonPostCommand() {
        super();
    }

    protected void onBeforeExecute(HttpRequestBase request) {
        try {
            HttpPost post = (HttpPost) request;
            boolean flag=getRequest().Params != null&&getRequest().Params.toParams()!=null&&!TextUtils.isEmpty(getRequest().Params.toParams());
            if (getRequest().Params != null && flag) {
                StringEntity entity = new StringEntity(getRequest().Params.toParams(), HTTP.UTF_8);
                entity.setContentType("application/json");
                post.setEntity(entity);
            }else if(getRequest().Params != null){
                post.setEntity(getRequest().Params.getEntity());
                
            }
          
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Object getSuccessResponse(HttpResponse response) {
        String result = null;
        try {
            result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected Object getErrorResponse(HttpResponse response) {
        return "HTTP ERROR C0DE : " + response.getStatusLine().getStatusCode();
    }

    protected Object getErrorResponse(Exception error) {
        return error;
    }

    @Override
    protected byte[] getBody() {
        return null;
    }

    @Override
    protected HttpRequestBase getHttpRequest() {
        return new HttpPost(getURI());
    }

    @Override
    protected void initializeHeaders() {
        super.initializeHeaders();
//        addHeader("Accept", "application/json");
    }

    @Override
    protected String getContentType() {
        return null;
    }
}
