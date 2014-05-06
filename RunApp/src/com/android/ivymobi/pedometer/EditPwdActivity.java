package com.android.ivymobi.pedometer;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.util.MD5Util;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.runapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msx7.core.command.ErrorCode;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.DefaultMapRequest;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;

public class EditPwdActivity extends BaseActivity implements
		View.OnClickListener, IResponseListener {

	private EditText old_pwd;
	private EditText new_pwd;
	private EditText again_pwd;
	private int mState = STATE_RESET;

	private final static int STATE_RESET = 0;
	private final static int STATE_AUTO = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_pwd);
		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(R.string.modified_info);

		old_pwd = (EditText) findViewById(R.id.old_pwd);
		new_pwd = (EditText) findViewById(R.id.new_pwd);
		again_pwd = (EditText) findViewById(R.id.again_pwd);

		Button submitBtn = (Button) findViewById(R.id.submit);
		submitBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String old_pwd_value = old_pwd.getText().toString();
				if (old_pwd_value == null || "".equals(old_pwd_value)
						|| "".equals(old_pwd_value.trim())) {
					ToastUtil.showLongToast("密码不能为空");
					return;
				}

				String new_pwd_value = new_pwd.getText().toString();
				if (new_pwd_value == null || "".equals(new_pwd_value)
						|| "".equals(new_pwd_value.trim())) {
					ToastUtil.showLongToast("密码不能为空");
					return;
				}

				String again_pwd_value = again_pwd.getText().toString();
				if (again_pwd_value == null || "".equals(again_pwd_value)
						|| "".equals(again_pwd_value.trim())) {
					ToastUtil.showLongToast("密码不能为空");
					return;
				}
				mState = STATE_RESET;
				HashMap<String, String> maps = new HashMap<String, String>();
				maps.put("session_id", UserUtil.getSession());
				String mEmail = UserUtil.getEmail();
				maps.put("password",
						MD5Util.getMD5String(mEmail + old_pwd_value));
				maps.put("new_password",
						MD5Util.getMD5String(mEmail + new_pwd_value));
				maps.put("new_password2",
						MD5Util.getMD5String(mEmail + again_pwd_value));
				showLoadingDialog(R.string.loadingData);
				Request request = new DefaultMapRequest(Config.SEVER_RESETPSD,
						maps);
				goPost(request, EditPwdActivity.this);

			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			startActivity(new Intent(EditPwdActivity.this,
					UserInfoActivity.class));
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	public void onSuccess(Response response) {
		// TODO Auto-generated method stub

		if (mState == STATE_RESET) {
			String dataString = response.getData().toString();
			BaseModel<ResetResult> data = new Gson().fromJson(dataString,
					new TypeToken<BaseModel<ResetResult>>() {
					}.getType());
			if ("fail".equals(data.status)) {
				dismissLoadingDialog();
				ToastUtil.showLongToast(data.message);
			} else {
				mState = STATE_AUTO;
				HashMap<String, String> maps = new HashMap<String, String>();
				String mEmail = UserUtil.getEmail();
				maps.put("email", mEmail);
				String passwd = new_pwd.getText().toString();
				maps.put("password", MD5Util.getMD5String(mEmail + passwd));
				Request request = new DefaultMapRequest(Config.SEVER_LOGIN,
						maps);
				goPost(request, this);

			}
		} else {
			dismissLoadingDialog();
			String dataString = response.getData().toString();
			BaseModel<LoginResult> data = new Gson().fromJson(dataString,
					new TypeToken<BaseModel<LoginResult>>() {
					}.getType());
			if ("fail".equals(data.status)) {
				ToastUtil.showLongToast(data.message);
			} else {
				ToastUtil.showLongToast(R.string.resetSuccess);
				UserUtil.saveSession(data.data.session_id);
				SyncMine.getInstance().syncMine(null);
				finish();
			}
		}
	}

	@Override
	public void onError(Response response) {
		// TODO Auto-generated method stub
		dismissLoadingDialog();
		ToastUtil.showLongToast(ErrorCode
				.getErrorCodeString(response.errorCode));
	}

	class ResetResult {

	}

	class LoginResult {
		String session_id;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
