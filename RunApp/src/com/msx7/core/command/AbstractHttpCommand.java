package com.msx7.core.command;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;

import com.msx7.NetUtils;
import com.msx7.core.Controller;
import com.msx7.core.command.model.Response;

/**
 * <pre>
 * Steps:
 * 1. Prepare - Initialize headers. Sub-classes can override this method parse
 * request etc
 * 2. onBeforeExecute:
 * 2.1. Get the URI
 * 2.2. Get HTTP command
 * 2.3. Add request to headers
 * 2.4. onBeforeExecute({@link HttpRequestBase}) -- Can add body and other
 * headers if need be etc
 * 3. go:
 * 3.1. Instantiate {@link HttpClient} 3.2. Execute the client using
 * http-request
 * 3.3. Validate the response (200)
 * 3.4. If valid, {@link #getSuccessResponse(HttpResponse)} or else
 * {@link #getErrorResponse(Exception)} or
 * {@link #getErrorResponse(HttpResponse)} 
 * 3.5. Set the response
 * 4. onAfterExecute:
 * Nothing special to be done. And also, don't override
 * {@link #notifyListener(boolean)} - let notification happen
 * </pre>
 * 
 * @author Gaurav Vaish
 * 
 */
public abstract class AbstractHttpCommand extends AbstractCommand {

	private HashMap<String, String> headers = new HashMap<String, String>();
	private URI uri;
	private HttpRequestBase request;

	protected AbstractHttpCommand() {
	}

	protected void prepare() {
		initializeHeaders();
	}

	protected final void onBeforeExecute() {
		request = getHttpRequest();

		addHeadersToRequest();
		String ctype = getContentType();
		if (ctype != null) {
			request.addHeader(HTTP.CONTENT_TYPE, ctype);
		}

		onBeforeExecute(request);
	}

	public void go() {
		HttpClient client = new DefaultHttpClient();
		Object responseData = null;
		Response response = new Response();
		client.getParams().setIntParameter("http.connection.timeout", 20000);
		client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
		        20000);
		if(!NetUtils.isNetworkWell(Controller.getApplication())){
			response.errorCode=ErrorCode.ERROR_NET;
			response.setError(true);
			setResponse(response);
			return;
		}
		try {
			HttpResponse rawResponse = client.execute(request);

			if (rawResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// If All-Iz-Well, give the sub-class the raw-response to
				// process and generate Response-data
				responseData = getSuccessResponse(rawResponse);
				response.setError(false);
			} else {
				// If something's wrong with the response, let the sub-class
				// create appropriate Response-data
				responseData = getErrorResponse(rawResponse);
				response.setError(true);
				response.errorCode = ErrorCode.ERROR_HTTP_STATUS;
			}
		} catch (Exception e) {
			e.printStackTrace();
			responseData = getErrorResponse(e);
			response.setError(true);
			if (e instanceof SocketTimeoutException
					|| e instanceof ConnectTimeoutException) {
				response.errorCode = ErrorCode.ERROR_TIME_OUT;
			} else if (e instanceof java.net.ConnectException) {
				response.errorCode = ErrorCode.ERROR_CONNECT_SERVER;
			} else if (e instanceof java.io.FileNotFoundException) {
				response.errorCode = ErrorCode.ERROR_NOT_FOUND_FILE_NET;
			} else
				response.errorCode = ErrorCode.ERROR_NET_UNKOWN;

		}

		response.setData(responseData);
		setResponse(response);
	}

	protected void onBeforeExecute(HttpRequestBase request) {
	}

	protected Object getErrorResponse(HttpResponse response) {
		return null;
	}

	protected Object getErrorResponse(Exception error) {
		return error;
	}

	protected Object getSuccessResponse(HttpResponse response) {
		InputStream payload = null;
		try {
			payload = response.getEntity().getContent();
		} catch (Exception e) {
			e.printStackTrace();
			return getErrorResponse(e);
		}
		return payload;
	}

	private void addHeadersToRequest() {
		for (String name : headers.keySet()) {
			request.addHeader(name, headers.get(name));
		}
	}

	protected abstract byte[] getBody();

	/**
	 * 
	 * 添加http中content-type头的值(MIME类型) exp: HTTP.CONTENT_TYPE, "application/json"
	 * 
	 */
	protected abstract String getContentType();

	protected HttpRequestBase getHttpRequest() {
		if (getBody() != null) {
			return new HttpPost(getURI());
		} else {
			return new HttpGet(getURI());
		}
	}

	public void setURI(URI uri) {
		this.uri = uri;
	}

	public URI getURI() {
		return uri==null?URI.create(getRequest().url):uri;
	}

	/**
	 * you can add some default headers like cookie, agent etc override and call
	 * addHeader
	 */
	protected void initializeHeaders() {
		addHeader(HTTP.CONN_CLOSE, "close");

	}

	/**
	 * use this method to add header for httpRequest
	 * 
	 * @param name
	 * @param value
	 */
	protected final void addHeader(String name, String value) {
		headers.put(name, value);
	}

}
