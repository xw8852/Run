/**   
 * @Title: Request.java 
 * @Package com.yhiker.playmate.core.common 
 * @Description: TODO
 * @author xiaowei   
 * @date 2012-7-23 下午4:10:20 
 * @version V1.0   
 */
package com.msx7.core.command.model;

import com.msx7.core.Manager;
import com.msx7.core.command.impl.HttpJsonPostCommand;

/**
 * 
 * @author 作者 xiaowei
 * @创建时间 2012-7-23 下午4:10:20 类说明 <br/>
 *       <ul>
 *       <li> {@link #url} httprequest url</li>
 *       <li> {@link #Params} 默认在 {@link HttpJsonPostCommand}
 *       {@link Manager#CMD_JSON_POST} 中调用 {@link IParams#toParams()}
 *       作为json化的参数使用</li>
 *       <li> {@link #object} 临时参数</li>
 *       </ul>
 */
public class Request {
	public String url;
	public IParams Params;
	public Object object;
}
