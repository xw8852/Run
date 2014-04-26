package com.android.ivymobi.pedometer.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.ivymobi.pedometer.Config;
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
    @InjectView(id = R.id.title_right)
    View mReload;
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

    public RankFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RankFragment(Context context) {
        super(context);
        initView();
    }

    Animation anim;
    boolean animFinish;

    void initView() {
        url = Config.SEVER_RANKING_CREDIT;
        LayoutInflater.from(getContext()).inflate(R.layout.activity_rank, this);
        Inject.inject(this, this);
        mMine.setBackgroundColor(0x66000000);
        
        mTitle.setText("排行榜");
        // header = new PushHeader(mListView);
        // header.setOnRefreshListener(this);
        footer = new PageFooter(mListView, this, page);
        footer.getView().setPadding(0, 0, 0, 20);
        mReload.setVisibility(View.VISIBLE);
        mReload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!animFinish)
                    return;

                onRefresh();
            }
        });
        rankAdapter = new RankAdapter(new ArrayList<RankFragment.RankUserInfo>(), getContext());
        mGroup.setOnCheckedChangeListener(mCheckedChangeListener);
        mListView.setAdapter(rankAdapter);
        anim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
    }

    RadioGroup.OnCheckedChangeListener mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group.getCheckedRadioButtonId() == R.id.credit) {
                url = Config.SEVER_RANKING_CREDIT;
            } else {
                url = Config.SEVER_RANKING_MILE;
            }
           rankAdapter.clear();
            onRefresh();
        }

    };

    @Override
    public void onRefresh() {
        mListView.setSelection(0);
        mReload.startAnimation(anim);
        Request request = new Request();
        footer.reset();
        mMine.setVisibility(View.GONE);
        animFinish = false;
        footer.showFooter(false);
        request.url = url + "?session_id=" + UserUtil.getSession() + "&pr=" + page.mAVECount + "&pn=" + page.mStartPage;
        Manager.getInstance().execute(Manager.CMD_GET_STRING, request, listener);
    }

    @Override
    public void loadPage(int pageNumber) {
        mReload.startAnimation(anim);
        Request request = new Request();
        request.url = url+ "?session_id=" + UserUtil.getSession() + "&pr=" + page.mAVECount + "&pn=" + pageNumber;
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
            if(infos.size()>0){
                RankUserInfo info=infos.get(0);
                if(mGroup.getCheckedRadioButtonId()==R.id.credit&&info.sum_miles>info.sum_credits){
                    return;
                }else if(mGroup.getCheckedRadioButtonId()==R.id.miles&&info.sum_miles<info.sum_credits){
                    return;
                }
            }
            showMine(mGroup.getCheckedRadioButtonId()==R.id.miles, baseModel.data.mine);
            mMine.setVisibility(View.VISIBLE);
            anim.cancel();
            anim.reset();
            animFinish = true;
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
            anim.cancel();
            animFinish = true;
            anim.reset();
            footer.onPageLoaderFinish();
        }
    };
    void showMine(boolean isMiles,Mine mine){
        ImageView mImageView=(ImageView)mMine.findViewById(R.id.portrait);
        TextView userName=(TextView)mMine.findViewById(R.id.user_name);
        TextView score=(TextView)mMine.findViewById(R.id.score);
        TextView rank=(TextView)mMine.findViewById(R.id.num);
        AsyncImageLoad.getIntance().loadImage(mine.avatar_url, mImageView,null);
        userName.setText(mine.nickname);
        score.setText(isMiles?String.valueOf(mine.sum_miles):String.valueOf(mine.sum_credits));
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
