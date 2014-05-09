package com.android.ivymobi.pedometer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.ivymobi.pedometer.SyncMetaData.ISyncMeta;
import com.android.ivymobi.pedometer.SyncMetaData.MetaData;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.pedometer.widget.AbstractAdapter;
import com.android.ivymobi.runapp.R;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.Controller;

@InjectActivity(id = R.layout.activity_filter)
public class FilterActivity extends BaseActivity implements OnClickListener {
    @InjectView(id = R.id.title)
    TextView mTitle;
    @InjectView(id = R.id.content)
    View mView;
    @InjectView(id = R.id.check)
    ImageView mCheckAll;
    @InjectView(id = R.id.depart)
    View mdepartment;
    @InjectView(id = R.id.city)
    View location;
    @InjectView(id = R.id.sex)
    View sex;
    @InjectView(id = R.id.year)
    View year;
    @InjectView(id = R.id.listView1)
    ListView mListView;
    @InjectView(id = R.id.button1)
    Button button;
    @InjectView(id = R.id.contentT)
    View root;
    @InjectView(id = R.id._depart)
    TextView _mdepart;
    @InjectView(id = R.id._city)
    TextView _mcity;
    @InjectView(id = R.id._sex)
    TextView _msex;
    @InjectView(id = R.id._year)
    TextView _myear;
    boolean isAll;
    List<CheckInfo> departs = new ArrayList<FilterActivity.CheckInfo>();
    List<CheckInfo> cities = new ArrayList<FilterActivity.CheckInfo>();
    List<CheckInfo> sexes = new ArrayList<FilterActivity.CheckInfo>();
    List<CheckInfo> years = new ArrayList<FilterActivity.CheckInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Inject.inject(this);
        mTitle.setText("筛选");
        mView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mCheckAll.isSelected()) {
                    setCheckAll(true);
                } else {
                    setCheckAll(false);
                }
            }
        });
        mdepartment.setOnClickListener(this);
        location.setOnClickListener(this);
        sex.setOnClickListener(this);
        button.setOnClickListener(this);
        year.setOnClickListener(this);
        MetaData data = UserUtil.getMetaData();
        if (data == null) {
            SyncMetaData.SyncMetaData(new ISyncMeta() {

                @Override
                public void syncMetaData() {
                    initdata();
                }
            });
        } else {
            initdata();
        }
        mAdapter = new CheckedAdapter(new ArrayList<FilterActivity.CheckInfo>(), this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(itemClickListener);
    }

    public void setCheckAll(boolean flag) {
        if (flag) {   
            _msex.setText("");
            _myear.setText("");
            _mcity.setText("");
            _mdepart.setText("");
            mCheckAll.setSelected(true);
            ;
        } else {
            mCheckAll.setSelected(false);
        }
    }

    CheckedAdapter mAdapter;

    void initdata() {
        String[] arr = null;
        if(getConfig()==null){
          arr=new String[]{null,null,null,null};
        }else
            arr=  getConfig().split("0x00X0");
        MetaData data = UserUtil.getMetaData();
        if (data == null)
            return;
        for (String name : data.department) {
            if (!TextUtils.isEmpty(arr[0]) && name.equals(arr[0])) {
                departs.add(new CheckInfo(name, true));
                _depart=name;
                _mdepart.setText(name);
            } else
                departs.add(new CheckInfo(name, false));
        }
        for (String name : data.location) {
            if (!TextUtils.isEmpty(arr[1]) && name.equals(arr[1])) {
                cities.add(new CheckInfo(name, true));
                _location=name;
                _mcity.setText(name);
            } else
                cities.add(new CheckInfo(name, false));
        }
        if (!TextUtils.isEmpty(arr[2]) && "male".equals(arr[2])) {
            sexes.add(new CheckInfo("男", true, "male"));
            sexes.add(new CheckInfo("女", false, "female"));
            _msex.setText("男");
            _sex="male";
        } else if (!TextUtils.isEmpty(arr[2]) && "female".equals(arr[2])) {
            sexes.add(new CheckInfo("男", false, "male"));
            sexes.add(new CheckInfo("女", true, "female"));
            _sex="female";
            _msex.setText("女");
        } else {
            sexes.add(new CheckInfo("男", false, "male"));
            sexes.add(new CheckInfo("女", false, "female"));
        }
        years.add(new CheckInfo("20以下", false, "0~20"));
        years.add(new CheckInfo("20~29", false));
        years.add(new CheckInfo("30~39", false));
        years.add(new CheckInfo("40~49", false));
        years.add(new CheckInfo("50~59", false));
        years.add(new CheckInfo("60~69", false));
        for (CheckInfo info : years) {
            if (!TextUtils.isEmpty(arr[3]) && info.value.equals(arr[3])) {
                info.isChecked = true;
                _year=info.value;
                _myear.setText(info.name);
            }
        }

    }

    OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (state) {
            case 1:
                _depart = mAdapter.getItem(position).value;
                for (CheckInfo info : departs) {
                    if (info.name.equals(mAdapter.getItem(position).name)) {
                        _mdepart.setText(info.name + "部门");
                        info.isChecked = true;
                    } else {
                        info.isChecked = false;
                    }
                    mAdapter.notifyDataSetChanged();
                }
                setCheckAll(false);
                break;
            case 2:
                _location = mAdapter.getItem(position).value;
                for (CheckInfo info : cities) {
                    if (info.name.equals(mAdapter.getItem(position).name)) {
                        _mcity.setText(info.name);
                        info.isChecked = true;
                    } else
                        info.isChecked = false;
                    setCheckAll(false);
                }
                break;
            case 3:
                _sex = mAdapter.getItem(position).value;
                for (CheckInfo info : sexes) {
                    if (info.name.equals(mAdapter.getItem(position).name)) {
                        _msex.setText(info.name);
                        info.isChecked = true;
                    } else
                        info.isChecked = false;
                    setCheckAll(false);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case 4:
                _year = mAdapter.getItem(position).value;
                for (CheckInfo info : years) {
                    if (info.name.equals(mAdapter.getItem(position).name)) {
                        _myear.setText(info.name);
                        info.isChecked = true;
                    } else
                        info.isChecked = false;
                    setCheckAll(false);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            }
            mListView.setVisibility(View.GONE);
        }
    };
    String _depart;
    String _location;
    String _sex;
    String _year;
    int state = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.depart:
            state = 1;
            mAdapter.changeData(departs);
            mListView.setVisibility(View.VISIBLE);

            break;
        case R.id.city:
            state = 2;
            mAdapter.changeData(cities);
            mListView.setVisibility(View.VISIBLE);
            break;
        case R.id.sex:
            state = 3;
            mAdapter.changeData(sexes);
            mListView.setVisibility(View.VISIBLE);
            break;
        case R.id.year:
            state = 4;
            mAdapter.changeData(years);
            mListView.setVisibility(View.VISIBLE);
            break;
        case R.id.button1:
            Intent intent = new Intent();
            if (!mCheckAll.isSelected()) {
                if (!TextUtils.isEmpty(_depart))
                    intent.putExtra("depart", _depart);
                if (!TextUtils.isEmpty(_location))
                    intent.putExtra("city", _location);
                if (!TextUtils.isEmpty(_sex))
                    intent.putExtra("sex", _sex);
                if (!TextUtils.isEmpty(_year))
                    intent.putExtra("year", _year);
            }
            saveConfig(_depart + "0x00X0" + _location + "0x00X0" + _sex + "0x00X0" + _year);
            setResult(RESULT_OK, intent);
            finish();
            break;

        default:
            break;
        }
    }

    public static final void saveConfig(String filter) {
        SharedPreferences preferences = Controller.getApplication().getSharedPreferences("filter", 0);
        preferences.edit().putString("filter", filter).commit();
    }

    public static final String getConfig() {
        SharedPreferences preferences = Controller.getApplication().getSharedPreferences("filter", 0);
        return preferences.getString("filter", null);
    }

    public static final void clearConfig() {
        SharedPreferences preferences = Controller.getApplication().getSharedPreferences("filter", 0);
        preferences.edit().clear().commit();
    }

    @Override
    public void onBackPressed() {
        if (mListView.getVisibility() == View.VISIBLE) {
            mListView.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }

    public static class CheckInfo {
        String name;
        boolean isChecked;
        String value;

        public CheckInfo(String name, boolean isChecked) {
            super();
            this.name = name;
            this.isChecked = isChecked;
            this.value = name;
        }

        public CheckInfo(String name, boolean isChecked, String value) {
            super();
            this.name = name;
            this.isChecked = isChecked;
            this.value = value;
        }

        public CheckInfo() {
            super();
        }

    }

    public class CheckedAdapter extends AbstractAdapter<CheckInfo> {

        public CheckedAdapter(List<CheckInfo> data, Context context) {
            super(data, context);
        }

        @Override
        public View createView(int position, View convertView, ViewGroup parent, LayoutInflater inflater) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_checked, null);
            }
            TextView content = (TextView) convertView.findViewById(R.id.content);
            ImageView check = (ImageView) convertView.findViewById(R.id.check);
            content.setText(getItem(position).name);
            if (mCheckAll.isSelected()) {
                check.setSelected(false);
            } else
                check.setSelected(getItem(position).isChecked);
            return convertView;
        }

    }

}
