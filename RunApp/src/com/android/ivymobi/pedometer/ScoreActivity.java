package com.android.ivymobi.pedometer;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.ivymobi.pedometer.SyncMine.ISyncMineFinish;
import com.android.ivymobi.pedometer.data.Mine;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.pedometer.widget.AbstractAdapter;
import com.android.ivymobi.runapp.R;
import com.google.gson.Gson;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.command.ErrorCode;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;
import com.msx7.image.ImageLoader;

@InjectActivity(id = R.layout.activity_score)
public class ScoreActivity extends BaseActivity {
    @InjectView(id = R.id.title_left)
    View mLeftTitle;
    @InjectView(id = R.id.title)
    TextView mTitleView;
    @InjectView(id = R.id.title_right)
    View mReload;
    @InjectView(id = R.id.include1)
    View mMine;
    @InjectView(id = R.id.listView1)
    ListView mListView;
    TextView view;
    @InjectView(id = R.id.menu)
    View menu;
    ImageView mMineImage;
    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Inject.inject(this);
        mReload.setVisibility(View.VISIBLE);
        mLeftTitle.setVisibility(View.VISIBLE);
        mTitleView.setText("积分");
        mMineImage = (ImageView) mMine.findViewById(R.id.portrait);
        if (UserUtil.getMine() == null) {
            SyncMine.getInstance().syncMine(new ISyncMineFinish() {

                @Override
                public void syncFinish() {
                    Mine _mine = UserUtil.getMine();
                    if (_mine != null)
                        ImageLoader.getInstance().loadImage(_mine.avatar_url, mMineImage);
                }
            });
        } else {
            Mine _mine = UserUtil.getMine();
            if (_mine != null)
                ImageLoader.getInstance().loadImage(_mine.avatar_url, mMineImage);
        }

        view = (TextView) mMine.findViewById(R.id.num);
        mLeftTitle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }

        });
        mReload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (animation != null)
                    return;
                loadData();
            }

        });
        mMine.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                menu.setVisibility(View.VISIBLE);
                menu.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        menu.setVisibility(View.GONE);
                    }
                });
            }

        });
        loadData();
    }

    @Override
    public void onBackPressed() {
        if (View.VISIBLE == menu.getVisibility()) {
            menu.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();

    }

    void loadData() {
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        mReload.startAnimation(animation);
        Request request = new Request();
        request.url = Config.SEVER_CREDIT_MINE + "?session_id=" + UserUtil.getSession();
        goGet(request, listener);
        showLoadingDialog(R.string.loadingData);
    }

    IResponseListener listener = new IResponseListener() {

        @Override
        public void onSuccess(Response response) {
            dismissLoadingDialog();
            mReload.clearAnimation();
            animation = null;
            String json = String.valueOf(response.getData());
            Data baseModel = new Gson().fromJson(json, Data.class);
            view.setText(String.valueOf(baseModel.data.credits));
            mListView.setAdapter(new ScoreAdapter(baseModel.data.history, ScoreActivity.this));
        }

        @Override
        public void onError(Response response) {
            dismissLoadingDialog();
            mReload.clearAnimation();
            animation = null;
            ToastUtil.showLongToast(ErrorCode.getErrorCodeString(response.errorCode));
        }

    };

    class ScoreAdapter extends AbstractAdapter<ScoreHistoryInfo> {

        public ScoreAdapter(List<ScoreHistoryInfo> data, Context context) {
            super(data, context);
        }

        @Override
        public View createView(int position, View convertView, ViewGroup parent, LayoutInflater inflater) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.score_item_mine, null);

            }
            ScoreItemMine mine = new ScoreItemMine(convertView);
            mine.setScoreItem(getItem(position));
            return convertView;
        }

    }

    class Data {
        public String status;
        public ScoreInfo data;

    }

    class ScoreInfo {
        int credits;
        int history_total_num;
        List<ScoreHistoryInfo> history;
    }

    class ScoreHistoryInfo {
        int credits;
        String via;
        long timestamp;
        String credit_id;
        String uuid;
        long id;
        /**
         * "id": 64058, "uuid": "47cf61adc363abd73299adab75ebbf3f", "credit_id":
         * "CreditsAward_5", "credits": 500, "timestamp": 1402667400, "via":
         * "workout"
         */
    }

    class ScoreItemMine {
        View root;
        View portrait;
        TextView userName;
        TextView num;

        public ScoreItemMine(ScoreItemMine mine) {

        }

        public ScoreItemMine(View view) {
            if (view.getTag() != null && view.getTag() instanceof ScoreItemMine) {
                ScoreItemMine mine = (ScoreItemMine) view.getTag();
                this.root = mine.root;
                this.portrait = mine.portrait;
                this.userName = mine.userName;
                this.num = mine.num;
                return;
            }
            root = view;
            portrait = view.findViewById(R.id.portrait);
            userName = (TextView) view.findViewById(R.id.user_name);
            num = (TextView) view.findViewById(R.id.num);
            view.setTag(this);
        }

        public void setMineScore(int num) {
            this.num.setText(String.valueOf(num));
        }

        public void setScoreItem(ScoreHistoryInfo info) {
            portrait.setVisibility(View.GONE);
            root.setBackgroundColor(Color.TRANSPARENT);
            userName.setTextColor(Color.BLACK);
            userName.setText(info.credit_id);
            num.setTextColor(Color.BLACK);
            num.setText(String.valueOf(info.credits));
        }
    }
}
