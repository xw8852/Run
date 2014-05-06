package com.android.ivymobi.pedometer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.ivymobi.runapp.R;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

public class AboutActivity extends BaseActivity implements OnClickListener {

	private Button update;
	private Button pro;
	private Button comments;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about_main);
		update = (Button) findViewById(R.id.update);
		update.setOnClickListener(this);
		pro = (Button) findViewById(R.id.pro);
		pro.setOnClickListener(this);
		comments = (Button) findViewById(R.id.comments);
		comments.setOnClickListener(this);

		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(R.string.about_title);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == update) {
			// 版本升级
			UmengUpdateAgent.forceUpdate(AboutActivity.this);
		} else if (v == pro) {
			// 功能介绍
			String url1 = Config.SEVER_URL + "static/page/feature.html";
			String title1 = "关于HRMObile";
			Intent intent1 = new Intent(this, WebActivity.class);
			intent1.putExtra("url", url1);
			intent1.putExtra("title", title1);
			startActivity(intent1);
		} else if (v == comments) {
			// 友盟评论
			FeedbackAgent agent = new FeedbackAgent(AboutActivity.this);
			agent.startFeedbackActivity();
		}

	}

}
