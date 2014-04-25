package com.android.ivymobi.pedometer;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.pedometer.widget.AbstractAdapter;
import com.android.ivymobi.pedometer.widget.HorizontalListView;
import com.android.ivymobi.runapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.Manager;
import com.msx7.core.command.ErrorCode;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;
import com.msx7.image.ImageLoader;
import com.msx7.image.ImageLoader.ImageData;

@InjectActivity(id = R.layout.activity_achi)
public class AchiActivity extends BaseActivity {
    @InjectView(id = R.id.title_left)
    View mLeftTitle;
    @InjectView(id = R.id.title)
    TextView mTitleView;
    @InjectView(id = R.id.root)
    LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Inject.inject(this);
        showLoadingDialog(R.string.loadingData);
        mTitleView.setText("成就");
        mLeftTitle.setVisibility(View.VISIBLE);
        Request request = new Request();
        request.url = Config.SEVER_ACHIEVEMENT_LIST + "?session_id=" + UserUtil.getSession();
        Manager.getInstance().execute(Manager.CMD_GET_STRING, request, listener);
        mLeftTitle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }

        });
    }

    IResponseListener listener = new IResponseListener() {

        @Override
        public void onSuccess(Response response) {
            dismissLoadingDialog();
            String json = String.valueOf(response.getData());
            BaseModel<AchiInfo[][]> baseModel = new Gson().fromJson(json, new TypeToken<BaseModel<AchiInfo[][]>>() {
            }.getType());
            AchiInfo[][] infos = baseModel.data;
            int length = infos.length;
            for (int i = 0; i < length; i++) {
                HorizontalListView gallery = new HorizontalListView(AchiActivity.this);
                gallery.setBackgroundColor(i % 2 == 0 ? 0xf0f0f0 : Color.TRANSPARENT);
                gallery.setAdapter(new AchiAdapter(Arrays.asList(infos[i]), AchiActivity.this));
                gallery.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
//                params.weight = 1.0f;
                gallery.setLayoutParams(params);
                root.addView(gallery,params);

            }
        }

        @Override
        public void onError(Response response) {
            dismissLoadingDialog();
            ToastUtil.showLongToast(ErrorCode.getErrorCodeString(response.errorCode));
        }

    };

    class AchiAdapter extends AbstractAdapter<AchiInfo> {

        public AchiAdapter(List<AchiInfo> data, Context context) {
            super(data, context);
        }

        @Override
        public View createView(int position, View convertView, ViewGroup parent, LayoutInflater inflater) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.achi_item, null);

            }
            ImageView view = (ImageView) convertView.findViewById(R.id.icon);
            TextView view2 = (TextView) convertView.findViewById(R.id.text1);
            view2.setTextColor(Color.BLACK);
            view2.setText(getItem(position).name);
            ImageData data = null;
            if (getItem(position).achieved == 1) {
                data = new ImageData(getItem(position).icon, ImageData.IMAGE_GRAY);
            } else {
                data = new ImageData(getItem(position).icon, ImageData.IMAGE_TYPE_NORMAL);
            }
            ImageLoader.getInstance().loadImage(data, view);
            convertView.setPadding(10, 0, 10, 0);
            if ((position / 4) % 2 == 0) {
                convertView.setBackgroundColor(0xf0f0f0);
            } else
                convertView.setBackgroundColor(Color.TRANSPARENT);
            return convertView;
        }

    }

    public static class AchiInfo {
        String id;
        String name;
        String icon;
        String group;
        // 1：表示已获得
        int achieved;
    }
}
