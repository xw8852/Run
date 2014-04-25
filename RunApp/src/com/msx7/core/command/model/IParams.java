package com.msx7.core.command.model;

import org.apache.http.HttpEntity;

public interface IParams {
	public String toParams();
	public HttpEntity getEntity();
}
