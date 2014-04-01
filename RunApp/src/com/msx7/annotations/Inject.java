package com.msx7.annotations;

import java.lang.reflect.Field;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

public class Inject {

    public static final void inject(Activity activity) {
        if (activity == null)
            return;
        InjectActivity mActivityLayout = activity.getClass().getAnnotation(InjectActivity.class);
        if (mActivityLayout != null) {
            activity.setContentView(mActivityLayout.id());
        }
        Field[] mFields = activity.getClass().getDeclaredFields();
        for (Field field : mFields) {
            InjectView mViewId = field.getAnnotation(InjectView.class);
            if (mViewId == null)
                continue;
            if (mViewId.id() == -1)
                continue;
            field.setAccessible(true);
            try {
                field.set(activity, activity.findViewById(mViewId.id()));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public  static final  View inject(Fragment fragment) {
        if (fragment == null)
            return null;
        InjectActivity mActivityLayout = fragment.getClass().getAnnotation(InjectActivity.class);
        View view = null;
        if (mActivityLayout != null) {
            view = fragment.getLayoutInflater(null).inflate(mActivityLayout.id(), null);
        } else {
            view = fragment.getView();
        }
       inject(fragment, view);
        return view;
    }
    
    public static final void inject(Object obj,View rootView){
    	if(rootView==null||obj==null)return ;
    	 Field[] mFields = obj.getClass().getDeclaredFields();
         for (Field field : mFields) {
             InjectView mViewId = field.getAnnotation(InjectView.class);
             if (mViewId == null)
                 continue;
             if (mViewId.id() == -1)
                 continue;
             field.setAccessible(true);
             try {
                 field.set(obj, rootView.findViewById(mViewId.id()));
             } catch (IllegalArgumentException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             }
         }
    }
}
