package com.android.ivymobi.pedometer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ivymobi.pedometer.widget.AbstractAdapter;
import com.android.ivymobi.pedometer.widget.CircleFlowIndicator;
import com.android.ivymobi.pedometer.widget.ViewFlow;
import com.android.ivymobi.runapp.R;
import com.baidu.a.a.a.a.c;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectView;
import com.msx7.image.ImageLoader;

public class AwardActivity extends BaseActivity {
    private ViewFlow viewFlow;

    List<Detail> model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.awardactivity);

        String json = getIntent().getStringExtra("param");
        List<Data> data = new Gson().fromJson(json, new TypeToken<ArrayList<Data>>() {
        }.getType());
        model = new ArrayList<Detail>();
        List<Detail> credits = new ArrayList<Detail>();
        List<Detail> achievement = new ArrayList<Detail>();
        for (int i = 0; i < data.size(); i++) {
            // Data data1 = data.get(i);
            // Data data2 = data.get(++i);
            // Detail _data = new Detail();
            // _data.id = data1.data.name;
            // _data.credits = data1.data.credits;
            // _data.name = data2.data.name;
            // _data.group = data2.data.group;
            // _data.icon = data2.data.icon;\
            if ("achievement".equals(data.get(i).type))
                achievement.add(data.get(i).data);
            else {
                credits.add(data.get(i).data);
            }
        }
        System.out.println(new Gson().toJson(achievement));
        System.out.println(new Gson().toJson(credits));
        for (int i = 0; i < achievement.size(); i++) {
            for (int j = 0; j < credits.size(); j++) {
                Detail cDetail = credits.get(j);
                Detail aDetail = achievement.get(i);
                if (cDetail.name.replace("CreditsAward_", "").equals(cDetail.name.replace("Achievement", ""))) {
                    aDetail.credits = cDetail.credits;
                    break;
                }
            }
        }
        System.out.println(new Gson().toJson(achievement));
        viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        viewFlow.setAdapter(new AwardAdapter(achievement, this), 0);
        CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
        viewFlow.setFlowIndicator(indic);
        viewFlow.setSelection(0);
        ((TextView) findViewById(R.id.title)).setText("成就");
        ((TextView) findViewById(R.id.right_title)).setText("完成");
        findViewById(R.id.right_title).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(AwardActivity.this, AchiActivity.class));
                finish();
            }

        });
    }

    class AwardAdapter extends AbstractAdapter<Detail> {

        public AwardAdapter(List<Detail> data, Context context) {
            super(data, context);
        }

        @Override
        public View createView(int position, View convertView, ViewGroup parent, LayoutInflater inflater) {
            Holder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_award, null);
                holder = new Holder(convertView);
                convertView.setTag(holder);
            }
            holder = holder == null ? (Holder) convertView.getTag() : holder;
            holder.achi.setText(getItem(position).name);
            holder.achi2.setText(getItem(position).name);
            holder.credits.setText(getItem(position).credits + "");
            ImageLoader.getInstance().loadImage(getItem(position).icon, holder.icon);
            return convertView;
        }

        class Holder {
            @InjectView(id = R.id.icon)
            ImageView icon;
            @InjectView(id = R.id.achi)
            TextView achi;
            @InjectView(id = R.id.achi2)
            TextView achi2;
            @InjectView(id = R.id.achi3)
            TextView credits;

            public Holder(View v) {
                Inject.inject(this, v);
            }
        }
    }

    public class Data {
        String type;
        Detail data;
    }

    public class Detail {
        String id;
        String name;
        int credits;
        String group;
        String icon;
    }

}
