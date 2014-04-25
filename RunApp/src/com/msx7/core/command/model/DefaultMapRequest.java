package com.msx7.core.command.model;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class DefaultMapRequest extends Request {

    public DefaultMapRequest(String url, Map<String, ? extends Object> maps) {
        super();
        this.url=url;
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Map.Entry<String, ? extends Object> entry : maps.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
        }
        this.Params = new IParams() {

            @Override
            public String toParams() {
                return null;
            }

            @Override
            public HttpEntity getEntity() {
                try {
                    return new UrlEncodedFormEntity(params, HTTP.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public DefaultMapRequest(String url, String key, String value) {
        this.url=url;
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(key, value));
        this.Params = new IParams() {

            @Override
            public String toParams() {
                return null;
            }

            @Override
            public HttpEntity getEntity() {
                try {
                    return new UrlEncodedFormEntity(params, HTTP.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
}
