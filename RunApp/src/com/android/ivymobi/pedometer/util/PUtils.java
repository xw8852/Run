package com.android.ivymobi.pedometer.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.msx7.core.Controller;

public class PUtils {
    /**
     * 运动状态 -1:未开始 0：跑步 1：走路 2:暂停 3:结束
     */
    public static final void saveSportState(int state) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Controller.getApplication());
        preferences.edit().putInt("pedometer.sport.state", state).commit();
    }

    /**
     * 运动状态 -1:未开始 0：跑步 1：走路 2:暂停 3:结束
     */
    public static final int getSportState() {
        return PreferenceManager.getDefaultSharedPreferences(Controller.getApplication()).getInt("pedometer.sport.state", -1);
    }

    public static final void saveStartTime(long time) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Controller.getApplication());
        preferences.edit().putLong("pedometer.sport.StartTime", time).commit();
    }

    public static final void saveEndTime(long time) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Controller.getApplication());
        preferences.edit().putLong("pedometer.sport.EndTime", time).commit();

    }

    public static final long getStartTime() {
        return PreferenceManager.getDefaultSharedPreferences(Controller.getApplication()).getLong("pedometer.sport.StartTime", 0);
    }

    public static final long getEndTime() {
        return PreferenceManager.getDefaultSharedPreferences(Controller.getApplication()).getLong("pedometer.sport.EndTime", 0);
    }

    public static final void savePauseDate(long date) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Controller.getApplication());
        preferences.edit().putLong("pedometer.sport.PauseDate", date).commit();
    }

    public static final long getPauseDate() {
        return PreferenceManager.getDefaultSharedPreferences(Controller.getApplication()).getLong("pedometer.sport.PauseDate", 0);
    }

    public static final void saveLastTime(long date) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Controller.getApplication());
        preferences.edit().putLong("pedometer.sport.PauseTime", date).commit();
    }

    public static final long getLastTime() {
        return PreferenceManager.getDefaultSharedPreferences(Controller.getApplication()).getLong("pedometer.sport.PauseTime", 0);
    }

    public static final String getNum2(float num) {
        num = num * 100;
        return String.valueOf(Math.round(num) / 100.0f);
    }

    public static final String getNum3(float num) {
        num = num * 1000;
        return String.valueOf(Math.round(num) / 1000.0f);
    }

    public static final String getNum2(double num) {
        num = num * 100;
        return String.valueOf(Math.round(num) / 100.0);
    }

    public static final String getNum3(double num) {
        num = num * 1000;
        return String.valueOf(Math.round(num) / 1000.0);
    }

    public static final void clearStepData() {
        SharedPreferences preferences = Controller.getApplication().getSharedPreferences("state", 0);
        preferences.edit().clear().commit();
    }
}
