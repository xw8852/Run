/**   
 * @Title: Response.java 
 * @Package com.yhiker.playmate.core.common 
 * @Description: TODO
 * @author xiaowei   
 * @date 2012-7-23 下午4:10:28 
 * @version V1.0   
 */
package com.msx7.core.command.model;

import java.io.InputStream;

import com.msx7.core.command.ErrorCode;
import com.msx7.core.command.impl.HttpGetCommand;
import com.msx7.core.command.impl.HttpJsonPostCommand;

/**
 * 
 * @author 作者 xiaowei
 * @创建时间 2012-7-23 下午4:10:28 类说明
 *       <ol>
 *       <li>
 *       {@link Response#errorCode} 错误代码，参考 {@link ErrorCode}</li>
 *       <li>
 *       {@link Response#result}
 *       <table>
 *       <tr>
 *       <td>
 *       {@link HttpJsonPostCommand}中正常返回类型为String</td>
 *       </tr>
 *       <tr>
 *       <td>
 *       {@link HttpGetCommand}中正常返回类型为{@link InputStream}</td>
 *       </tr>
 *       <tr>
 *       <td>
 *       {@link Response#errorCode}的值为{@link ErrorCode#ERROR_HTTP_STATUS}
 *       时，值为string字符串 否则为{@link Exception}</td>
 *       </tr>
 *       </table>
 *       </ol>
 */
public class Response {


	public Object result;
	public boolean error;
	public int errorCode;

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public Object getData() {
		return result;
	}

	public void setData(Object data) {
		this.result = data;
	}
}
