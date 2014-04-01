package com.msx7.core;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.msx7.core.command.AbstractHttpCommand;
import com.msx7.core.command.ICommand;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.impl.HttpGetCommand;
import com.msx7.core.command.impl.HttpJsonPostCommand;
import com.msx7.core.command.model.DefaultResponseListenerImpl;
import com.msx7.core.command.model.Request;

/***
 * <p>
 * 关于Manager的使用操作步骤
 * </p>
 * <ol>
 * <li>RegisterCommand:将自定义继承自{@link ICommand}的Command 注册</li>
 * <li>Execute:将需要执行的CMD添加进执行列队</li>
 * </ol>
 * 
 * @author Josn
 * 
 */
public class Manager {
	/**
	 * 类型为cmdId 此值对应 {@link HttpJsonPostCommand}
	 */
	public static final int CMD_JSON_POST = 0x00001;
	/**
	 * 类型为cmdId 此值对应 {@link HttpGetCommand}
	 */
	public static final int CMD_GET = CMD_JSON_POST << CMD_JSON_POST;
	private static final Manager instance = new Manager();
	private HashMap<String, Class<? extends ICommand>> maps = new HashMap<String, Class<? extends ICommand>>();
	private ExecutorService mPools;

	private Manager() {
		mPools = Executors.newCachedThreadPool();
		registerCommand(CMD_JSON_POST, HttpJsonPostCommand.class);
		registerCommand(CMD_GET, HttpGetCommand.class);
	}

	public static final Manager getInstance() {
		return instance;
	}

	public void registerCommand(int cmdId, Class<? extends ICommand> cls) {
		if (cls == null)
			return;
		maps.put(Integer.toString(cmdId), cls);
	}

	/**
	 * 
	 * @param request
	 * @param listener
	 *            listener will be running in the UI thread
	 */
	public void executePoset(Request request, IResponseListener listener) {
		execute(CMD_JSON_POST, request, listener);
	}

	/**
	 * 
	 * @param cmdId
	 *            {@link Manager#CMD_JSON_POST}
	 *            {@link #registerCommand(int, Class)}
	 * @param request
	 * @param listener
	 *            listener will be running in the UI thread
	 */
	public void execute(int cmdId, Request request, IResponseListener listener) {
		execute(cmdId, request, listener, true);
	}

	/**
	 * 
	 * @param cmdId
	 *            {@link Manager#CMD_JSON_POST}
	 *            {@link #registerCommand(int, Class)}
	 * @param request
	 * @param listener
	 * @param isUIThread
	 *            true : the listener will be executed in UI Thread;
	 */
	public void execute(int cmdId, Request request, IResponseListener listener,
			boolean isUIThread) {
		if (isUIThread) {
			listener = new DefaultResponseListenerImpl(Controller
					.getApplication().getHandler(), listener);
		}
		Class<? extends ICommand> cmd_class = maps.get(Integer.toString(cmdId));
		if (cmd_class == null)
			return;
		int modifiers = cmd_class.getModifiers();
		if ((modifiers & Modifier.ABSTRACT) != 0)
			return;
		if ((modifiers & Modifier.INTERFACE) != 0)
			return;
		try {
			ICommand cmd = cmd_class.newInstance();
			cmd.setRequest(request);
			cmd.setResponseListener(listener);
			if (cmd instanceof AbstractHttpCommand) {
				((AbstractHttpCommand) cmd).setURI(URI.create(request.url));
			}
			mPools.execute(new ThreadCall(cmd));
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static  class ThreadCall implements Runnable {
		ICommand cmd;

		public ThreadCall(ICommand cmd) {
			super();
			this.cmd = cmd;
		}

		@Override
		public void run() {
			cmd.execute();
		}
	}

}
