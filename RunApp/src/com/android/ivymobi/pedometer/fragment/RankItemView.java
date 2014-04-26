package com.android.ivymobi.pedometer.fragment;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.ivymobi.pedometer.widget.RoundImageView;
import com.android.ivymobi.runapp.R;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectView;
import com.msx7.image.AsyncImageLoad;

public class RankItemView extends LinearLayout {
    @InjectView(id = R.id.imageView2)
    View me;
    @InjectView(id = R.id.portrait)
    RoundImageView portrait;
    @InjectView(id = R.id.user_name)
    TextView userName;
    @InjectView(id = R.id.score)
    TextView score;
    @InjectView(id = R.id.num)
    TextView num;

    public RankItemView(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.rank_item_mine, this);
        Inject.inject(this, this);
    }

    public void setShowMe(boolean isShow) {
        me.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    public void setData(RankFragment.RankUserInfo info, int num) {
        userName.setText(info.name);
        userName.setTextColor(Color.BLACK);
        score.setTextColor(Color.BLACK);
        ((TextView) findViewById(R.id.textView4)).setTextColor(Color.BLACK);
        this.num.setTextColor(Color.BLACK);
        score.setText((String.valueOf(info.sum_credits > info.sum_miles ? info.sum_credits : info.sum_miles)));
        this.num.setText(String.valueOf(info.rank));
        portrait.setScaleType(ScaleType.CENTER_CROP);
        portrait.setImageResource(R.drawable.ic_user_default);
        AsyncImageLoad.getIntance().loadImage(info.avatar, portrait, null);

    }
}
