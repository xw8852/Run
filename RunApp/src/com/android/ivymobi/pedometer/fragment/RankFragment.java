package com.android.ivymobi.pedometer.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.ivymobi.pedometer.Config;
import com.android.ivymobi.pedometer.FilterActivity;
import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.data.Mine;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.pedometer.widget.AbstractAdapter;
import com.android.ivymobi.pedometer.widget.PageFooter;
import com.android.ivymobi.pedometer.widget.PageFooter.Page;
import com.android.ivymobi.pedometer.widget.PageFooter.PageLoader;
import com.android.ivymobi.pedometer.widget.PushHeader;
import com.android.ivymobi.runapp.R;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectView;
import com.msx7.core.Manager;
import com.msx7.core.command.ErrorCode;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;
import com.msx7.image.AsyncImageLoad;

public class RankFragment extends LinearLayout implements PushHeader.OnRefreshListener, IViewStatus, PageLoader {
    @InjectView(id = R.id.rank_item_mine)
    View mMine;
    @InjectView(id = R.id.right_title)
    TextView mFilter;
    @InjectView(id = R.id.title)
    TextView mTitle;
    @InjectView(id = R.id.listView1)
    ListView mListView;
    @InjectView(id = R.id.topBar)
    RadioGroup mGroup;
    PushHeader header;
    Page page = new Page(20, 1);
    RankAdapter rankAdapter;
    PageFooter footer;
    String url;
    View headMine;

    public RankFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RankFragment(Context context) {
        super(context);
        initView();
    }

    void initView() {
        url = Config.SEVER_RANKING_CREDIT;
        LayoutInflater.from(getContext()).inflate(R.layout.activity_rank, this);
        Inject.inject(this, this);
        mMine.setBackgroundColor(0x66000000);
        headMine = LayoutInflater.from(getContext()).inflate(R.layout.rank_item_mine, null);
        mTitle.setText("排行榜");
        // header = new PushHeader(mListView);
        // header.setOnRefreshListener(this);
        footer = new PageFooter(mListView, this, page);
        footer.getView().setPadding(0, 0, 0, 20);
        mFilter.setVisibility(View.VISIBLE);
        mFilter.setText("筛选");
        mFilter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((Activity) getContext()).startActivityForResult(new Intent(getContext(), FilterActivity.class), 4);
                ;
            }
        });
        rankAdapter = new RankAdapter(new ArrayList<RankFragment.RankUserInfo>(), getContext());
        mGroup.setOnCheckedChangeListener(mCheckedChangeListener);
        mListView.addHeaderView(headMine);
        headMine.setVisibility(View.INVISIBLE);
        mListView.setAdapter(rankAdapter);
    }

    String subffix = "";

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 4 && resultCode == Activity.RESULT_OK) {
            subffix = "";
            if (data.hasExtra("depart")) {
                String aa = data.getStringExtra("depart");
                try {
                    aa = URLEncoder.encode(aa, "utf-8").replaceAll("\\+", "%20");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                subffix = "&" + subffix + "filter_department=" + aa;
            }
            if (data.hasExtra("city"))
                subffix = "&" + subffix + "filter_location=" + data.getStringExtra("city");
            if (data.hasExtra("sex"))
                subffix = "&" + subffix + "filter_sexual=" + data.getStringExtra("sex");
            if (data.hasExtra("year"))
                subffix = "&" + subffix + "filter_age=" + data.getStringExtra("year");
            onRefresh();
        }
    }

    RadioGroup.OnCheckedChangeListener mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group.getCheckedRadioButtonId() == R.id.credit) {
                url = Config.SEVER_RANKING_CREDIT;
            } else {
                url = Config.SEVER_RANKING_MILE;
            }
            FilterActivity.clearConfig();
            subffix="";
            rankAdapter.clear();
            onRefresh();
        }

    };

    @Override
    public void onRefresh() {
        mListView.setSelection(0);
        Request request = new Request();
        footer.reset();
        mMine.setVisibility(View.GONE);

        footer.showFooter(false);
        request.url = url + "?session_id=" + UserUtil.getSession() + "&pr=" + page.mAVECount + "&pn=" + page.mStartPage + subffix;

        Manager.getInstance().execute(Manager.CMD_GET_STRING, request, listener);
    }

    @Override
    public void loadPage(int pageNumber) {
        Request request = new Request();
        request.url = url + "?session_id=" + UserUtil.getSession() + "&pr=" + page.mAVECount + "&pn=" + pageNumber + subffix;

        Manager.getInstance().execute(Manager.CMD_GET_STRING, request, listener);
    }

    @Override
    public void onResume() {
        onRefresh();
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onFinish() {

    }

    IResponseListener listener = new IResponseListener() {

        @Override
        public void onSuccess(Response response) {
            String str = response.getData().toString();
            BaseModel<RankInfo> baseModel = new Gson().fromJson(str, new TypeToken<BaseModel<RankInfo>>() {
            }.getType());
            List<RankUserInfo> infos = baseModel.data.ranklist;
            if (infos.size() > 0) {
                RankUserInfo info = infos.get(0);
                if (mGroup.getCheckedRadioButtonId() == R.id.credit && info.sum_miles > info.sum_credits) {
                    return;
                } else if (mGroup.getCheckedRadioButtonId() == R.id.miles && info.sum_miles < info.sum_credits) {
                    return;
                }
            }
            showMine(mGroup.getCheckedRadioButtonId() == R.id.miles, baseModel.data.mine);
            mMine.setVisibility(View.VISIBLE);
            headMine.setVisibility(View.VISIBLE);

            if (infos.size() < page.mAVECount) {
                footer.showFooter(false);
            } else {
                footer.showFooter(true);
            }
            if (page.mCurPage == 0) {
                rankAdapter.changeData(infos);
            } else
                rankAdapter.addMore(infos);
            footer.onPageLoaderFinish();
        }

        @Override
        public void onError(Response response) {
            ToastUtil.showLongToast(ErrorCode.getErrorCodeString(response.errorCode));
            footer.onPageLoaderFinish();
        }
    };

    void showMine(boolean isMiles, Mine mine) {
        ImageView mImageView = (ImageView) mMine.findViewById(R.id.portrait);
        TextView userName = (TextView) mMine.findViewById(R.id.user_name);
        TextView score = (TextView) mMine.findViewById(R.id.score);
        TextView rank = (TextView) mMine.findViewById(R.id.num);
        if (UserUtil.getMine() != null) {
            AsyncImageLoad.getIntance().loadImage(UserUtil.getMine().avatar_url, mImageView, null);
        } else
            AsyncImageLoad.getIntance().loadImage(mine.avatar_url, mImageView, null);
        userName.setText(mine.nickname);
        score.setText(isMiles ? String.valueOf(mine.sum_miles) : String.valueOf(mine.sum_credits));
        rank.setText(String.valueOf(mine.rank));

    }

    class RankAdapter extends AbstractAdapter<RankUserInfo> {

        public RankAdapter(List<RankUserInfo> data, Context context) {
            super(data, context);
        }

        @Override
        public View createView(int position, View convertView, ViewGroup parent, LayoutInflater inflater) {
            if (convertView == null) {
                convertView = new RankItemView(getContext());
            }
            RankItemView mView = (RankItemView) convertView;
            mView.setShowMe(false);
            mView.setData(getItem(position), position + 1);
            return mView;
        }

    }

    public class RankInfo {
        Mine mine;
        List<RankUserInfo> ranklist;
    }

    public static class RankUserInfo {
        String employ_id;
        String realname;
        @SerializedName("nickname")
        String name;
        @SerializedName("avatar_url")
        String avatar;
        int sum_credits;
        int sum_miles;
        int rank;

    }

}
