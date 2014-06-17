/**   
 * @Title: AbstractAdapter.java 
 * @Description: TODO
 * @author xiaowei   
 * @date 2012-9-10 上午11:19:17 
 * @version V1.0   
 */
package com.android.ivymobi.pedometer.widget;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 
 * @author 作者 xiaowei
 * @创建时间 2012-9-10 上午11:19:17 类说明
 * 
 */
public abstract class AbstractAdapter<T> extends BaseAdapter {
	protected List<T> data;
	protected Context context;
	LayoutInflater mInflater;

	public AbstractAdapter(List<T> data, Context context) {
		super();
		this.data = data;
		this.context = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	public Resources getResources() {
		return context.getResources();
	}

	@Override
	public T getItem(int position) {
		if (data == null || position < 0 || position >= data.size())
			return null;
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public Context getContext() {
		return this.context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (data == null || position < 0 || position >= data.size())
			return null;
		return createView(position, convertView, parent, mInflater);
	}

	public abstract View createView(int position, View convertView,
			ViewGroup parent, LayoutInflater inflater);

	public void addMore(List<T> data) {
		this.data.addAll(data);
		this.notifyDataSetChanged();
	}

	public void changeData(List<T> data) {
		this.data = data;
		this.notifyDataSetChanged();
	}

	public boolean hasContent(T t) {
		return data.contains(t);
	}

	public boolean remove(T t) {
		return data.remove(t);
	}

	public void clear() {
		if (data == null) {
			return;
		}
		data.clear();
		notifyDataSetChanged();
	}

	public List<T> getList() {
		return data;
	}
}
