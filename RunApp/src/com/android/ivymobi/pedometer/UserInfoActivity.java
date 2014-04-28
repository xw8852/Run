package com.android.ivymobi.pedometer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ivymobi.pedometer.data.Mine;
import com.android.ivymobi.pedometer.login.LoginActivity;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.runapp.R;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.Controller;
import com.msx7.image.AsyncImageLoad;

@InjectActivity(id = R.layout.activity_userinfo)
public class UserInfoActivity extends BaseActivity implements View.OnClickListener {
    @InjectView(id = R.id.usrname)
    TextView userName;
    @InjectView(id = R.id.ic_portail)
    ImageView portail;
    @InjectView(id = R.id.location)
    TextView location;
    @InjectView(id = R.id.department)
    TextView department;
    @InjectView(id = R.id.sex)
    TextView sex;
    @InjectView(id = R.id.title)
    TextView mTitleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Inject.inject(this);
        mTitleView.setText("设置");
        findViewById(R.id.loginOut).setOnClickListener(this);

        findViewById(R.id.modifyInfo).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingDialog(R.string.loadingData);
        SyncMine.getInstance().syncMine(new SyncMine.ISyncMineFinish() {

            @Override
            public void syncFinish() {
                dismissLoadingDialog();
                Mine _mine = UserUtil.getMine();
                if (_mine != null) {
                    AsyncImageLoad.getIntance().loadImage(_mine.avatar_url, portail, null);
                    showMine(_mine);
                }

            }
        });
    }

    void showMine(Mine mine) {
        if (mine == null) {
            return;
        }
        AsyncImageLoad.getIntance().loadImage(mine.avatar_url, portail, null);
        userName.setText(mine.nickname);
        location.setText(mine.location);
        department.setText(mine.department);
        sex.setText("female".equals(mine.sexual) ? "女" : "男");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.loginOut:
            Controller.getApplication().finishAllHistory();
            UserUtil.clearMine();
            startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));
            finish();
            break;
        case R.id.modifyInfo:
            String url = Config.SEVER_URL + "web/profile/edit?session_id=" + UserUtil.getSession();
            String title = "修改资料";
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("title", title);
            startActivity(intent);
            break;

        }
    }
}
