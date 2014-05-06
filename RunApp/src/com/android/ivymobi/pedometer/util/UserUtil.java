package com.android.ivymobi.pedometer.util;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.android.ivymobi.pedometer.data.Mine;
import com.google.gson.Gson;
import com.msx7.core.Controller;

public class UserUtil {

	public static final void saveEmail(String email) {
		SharedPreferences preferences = Controller.getApplication()
				.getSharedPreferences("User", 0);
		preferences.edit().putString("u_login_email", email).commit();
	}

	public static final String getEmail() {
		return Controller.getApplication().getSharedPreferences("User", 0)
				.getString("u_login_email", null);
	}

	public static final void saveSession(String sessoin) {
		SharedPreferences preferences = Controller.getApplication()
				.getSharedPreferences("User", 0);
		preferences.edit().putString("u_login_sessoin", sessoin).commit();
	}

	public static final String getSession() {
		return Controller.getApplication().getSharedPreferences("User", 0)
				.getString("u_login_sessoin", null);
	}

	public static final Mine getMine() {
		String json = Controller.getApplication()
				.getSharedPreferences("User", 0).getString("u_Mine", null);
		if (TextUtils.isEmpty(json) || json == null)
			return null;
		return new Gson().fromJson(json, Mine.class);
	}

	public static final void saveMine(Mine mine) {
		SharedPreferences preferences = Controller.getApplication()
				.getSharedPreferences("User", 0);
		preferences.edit().putString("u_Mine", new Gson().toJson(mine))
				.commit();
	}

	public static final void clearMine() {
		SharedPreferences preferences = Controller.getApplication()
				.getSharedPreferences("User", 0);
		preferences.edit().remove("u_Mine").remove("u_login_sessoin").commit();
	}
}
