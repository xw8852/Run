package com.msx7.core.command;

import com.msx7.core.command.model.Response;


/**
 * <p>
 * Steps for a general command:
 * </p>
 * <ol>
 * <li>Prepare: Initialize the command</li>
 * <li>onBeforeExecute: About to execute</li>
 * <li>go: Actual execution</li>
 * <li>onAfterExecute: Just executed. Notify the response listener.</li>
 * </ol>
 * 
 * @author Gaurav Vaish
 */
public abstract class AbstractCommand extends AbstractBaseCommand
{
	public final void execute()
	{
		prepare();
		onBeforeExecute();
		go();
		onAfterExecute();

		Response response = getResponse();

		if(response != null)
		{
			notifyListener(!response.isError());
		}
	}
	/**
	 * Initialize the command
	 */
	protected void prepare()
	{
	}
	/**
	 * Actual to execute
	 */
	protected abstract void go();
	
	/**
	 * About to execute
	 */
	protected void onBeforeExecute()
	{
	}
	/**
	 * About manage the executive result
	 */
	protected void onAfterExecute()
	{
	}
	

}
