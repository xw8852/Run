package com.android.ivymobi.pedometer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.data.Mine;
import com.android.ivymobi.pedometer.login.LoginActivity;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.runapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.Controller;
import com.msx7.core.command.ErrorCode;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.DefaultMapRequest;
import com.msx7.core.command.model.IParams;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;
import com.msx7.image.AsyncImageLoad;

@InjectActivity(id = R.layout.activity_userinfo)
public class UserInfoActivity extends BaseActivity implements View.OnClickListener, IResponseListener {
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
    @InjectView(id = R.id.edit_pwd)
    LinearLayout editPwdLayout;
    @InjectView(id = R.id.about)
    TextView aboutView;
    @InjectView(id = R.id.weight)
    TextView weight;
    @InjectView(id = R.id.height)
    TextView height;
    @InjectView(id = R.id.bmi_num)
    TextView bmi;
    @InjectView(id = R.id.line)
    View line;
    @InjectView(id = R.id.img_ll)
    LinearLayout headImg;

    private PopupWindow popupWindow;
    private Button takePhoto;
    private Button takePic;
    private Button cancel;
    private String resultFilepath;
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_GALLERY_IMAGE = 3;
    private static final int REQUEST_MODIFY_FINISH = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Inject.inject(this);
        mTitleView.setText("设置");
        findViewById(R.id.about).setOnClickListener(this);
        findViewById(R.id.img_ll).setOnClickListener(this);
        findViewById(R.id.edit_pwd).setOnClickListener(this);
        findViewById(R.id.loginOut).setOnClickListener(this);
        findViewById(R.id.modifyInfo).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cancelSync) {
            cancelSync = false;
            return;
        }
        showLoadingDialog(R.string.loadingData);
        Mine _mine = UserUtil.getMine();
        if (_mine != null) {
            AsyncImageLoad.getIntance().loadImage(_mine.avatar_url, portail, null);
            showMine(_mine);
        }
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
        height.setText(mine.height + " cm");
        weight.setText(mine.weight + " kg");
        bmi.setText(String.valueOf(mine.BMI));
        Rect rect = new Rect();
        bmi.getHitRect(rect);
        Rect rect2 = new Rect();
        line.getHitRect(rect2);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bmi.getLayoutParams();
        if (params == null) {
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        }
        int dx = new Float((Math.max(Math.min(mine.BMI, 35f), 20f) - 20f) * (rect2.width() / 15.0f)).intValue();
        params.leftMargin = rect2.left - rect.width() / 2 + dx;
        bmi.setLayoutParams(params);
        bmi.requestLayout();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.loginOut:
            HashMap<String, String> maps = new HashMap<String, String>();
            maps.put("session_id", UserUtil.getSession());
            showLoadingDialog(R.string.loadingData);
            Request request = new DefaultMapRequest(Config.SEVER_LOGOUT, maps);
            goPost(request, this);

            break;
        case R.id.modifyInfo:
            String url = Config.SEVER_URL + "web/profile/edit?session_id=" + UserUtil.getSession();
            String title = "修改资料";
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("title", title);
            startActivity(intent);
            break;
        case R.id.edit_pwd:
            startActivity(new Intent(UserInfoActivity.this, EditPwdActivity.class));
            break;
        case R.id.about:
            startActivity(new Intent(UserInfoActivity.this, AboutActivity.class));
            break;
        case R.id.img_ll:
            popupWindow = makePopupWindow(UserInfoActivity.this);
            popupWindow.showAtLocation(this.findViewById(R.id.userinfo_layout), Gravity.CENTER, 0, 0);
            break;
        case R.id.uploading_b1:
            dismissPopupWindow();
            doTakePhoto();
            break;
        case R.id.uploading_b2:
            dismissPopupWindow();
            doPickPhotoFromGallery();
            break;
        case R.id.uploading_b3:
            dismissPopupWindow();
            break;

        }
    }

    @Override
    public void onSuccess(Response response) {
        // TODO Auto-generated method stub
        dismissLoadingDialog();
        String dataString = response.getData().toString();
        BaseModel<LogoutResult> data = new Gson().fromJson(dataString, new TypeToken<BaseModel<LogoutResult>>() {
        }.getType());
        if ("fail".equals(data.status)) {
            ToastUtil.showLongToast(data.message);
        } else {
            ToastUtil.showLongToast(R.string.logoutSuccess);
            Controller.getApplication().finishAllHistory();
            UserUtil.clearMine();
            startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onError(Response response) {
        // TODO Auto-generated method stub
        dismissLoadingDialog();
        ToastUtil.showLongToast(ErrorCode.getErrorCodeString(response.errorCode));
    }

    class LogoutResult {

    }

    private PopupWindow makePopupWindow(Context context) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.img_uploading_pop, null);
        PopupWindow window = new PopupWindow(contentView, WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
        takePhoto = (Button) contentView.findViewById(R.id.uploading_b1);
        takePhoto.setOnClickListener(this);
        takePic = (Button) contentView.findViewById(R.id.uploading_b2);
        takePic.setOnClickListener(this);
        cancel = (Button) contentView.findViewById(R.id.uploading_b3);
        cancel.setOnClickListener(this);

        // 设置PopupWindow外部区域是否可触摸
        window.setFocusable(true); // 设置PopupWindow可获得焦点
        window.setTouchable(true); // 设置PopupWindow可触摸
        window.setOutsideTouchable(false); // 设置非PopupWindow区域可触摸
        return window;
    }

    private void dismissPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private void doPickPhotoFromGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            cancelSync = true;

            // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            // intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY_IMAGE);
        } catch (ActivityNotFoundException e) {
            ToastUtil.showShortToast("图片集为空");
        }
    }

    private void doTakePhoto() {
        try {
            cancelSync = true;
            resultFilepath = Environment.getExternalStorageDirectory() + "/com.android.ivymobi.pedometer/" + getNormalTime() + ".jpg";
            File mCurrentPhotoFile = new File(resultFilepath);// 给新照的照片文件命名
            if (!mCurrentPhotoFile.getParentFile().exists())
                mCurrentPhotoFile.getParentFile().mkdirs();
            Intent intent = getTakePickIntent(mCurrentPhotoFile);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        } catch (ActivityNotFoundException e) {
            ToastUtil.showShortToast("摄像头启动失败");
        }
    }

    public String getNormalTime() {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        String date = simpleFormat.format(System.currentTimeMillis());
        return date;
    }

    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    boolean cancelSync;
String path;
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
        case REQUEST_TAKE_PHOTO:

            if (resultFilepath != null && resultFilepath.length() > 0 && new File(resultFilepath).exists()) {
                File f = new File(resultFilepath);
                path=resultFilepath;
                cropImageUri(Uri.fromFile(f), 200 , 200, REQUEST_MODIFY_FINISH);
//                Intent intent = new Intent(UserInfoActivity.this, CropImageActivity.class);
//                intent.putExtra("path", f.getAbsolutePath());
//                startActivityForResult(intent, REQUEST_MODIFY_FINISH);
                cancelSync = true;
                return;
            }
            break;
        case REQUEST_GALLERY_IMAGE:
            if (null == data) {
                return;
            }
            Uri _uri = data.getData();
            if (_uri != null) {
                Cursor cursor = UserInfoActivity.this.getContentResolver().query(_uri, null, null, null, null);
                cursor.moveToFirst();
                resultFilepath = cursor.getString(1); // 返回图片的地址
                path=resultFilepath;
                cursor.close();
                File f = new File(resultFilepath);
                cropImageUri(Uri.fromFile(f), 200 , 200, REQUEST_MODIFY_FINISH);
//                Intent intent = new Intent(UserInfoActivity.this, CropImageActivity.class);
//                intent.putExtra("path", f.getAbsolutePath());
                cancelSync = true;
//                startActivityForResult(intent, REQUEST_MODIFY_FINISH);
            }
            break;
        case REQUEST_MODIFY_FINISH:
            if (data != null) {

                resultFilepath =path;
                updateImg();
                Request request = new Request(Config.SEVER_UPDATE_AVATAR + "?session_id=" + UserUtil.getSession(), new ImgUpdateIparam(
                        resultFilepath, UserUtil.getSession()));
                goPost(request, new IResponseListener() {

                    @Override
                    public void onSuccess(Response response) {
                        dismissLoadingDialog();
                        String dataString = response.getData().toString();
                        BaseModel<LogoutResult> data = new Gson().fromJson(dataString, new TypeToken<BaseModel<LogoutResult>>() {
                        }.getType());
                        if ("fail".equals(data.status)) {
                            ToastUtil.showLongToast(data.message);
                        } else {
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
                    }

                    @Override
                    public void onError(Response response) {
                        dismissLoadingDialog();
                        ToastUtil.showLongToast(ErrorCode.getErrorCodeString(response.errorCode));
                    }
                });
            }
            break;
        }
    }

    private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, requestCode);
       }
    private void updateImg() {
        dismissPopupWindow();
        if (resultFilepath != null && resultFilepath.length() > 0) {
            // ArrayList<String> mPath = new ArrayList<String>();
            // mPath.add(resultFilepath);
            Drawable drawable = filePathToDrawable(resultFilepath);
            portail.setImageDrawable(drawable);
        }

    }

    class ImgUpdateIparam implements IParams {
        String filePath;
        String session;

        public ImgUpdateIparam(String filePath, String session) {
            super();
            this.filePath = filePath;
            this.session = session;
        }

        @Override
        public String toParams() {
            return null;
        }

        @Override
        public HttpEntity getEntity() {
            MultipartEntity entity = new MultipartEntity();
            try {
                FileBody fileBody = new FileBody(new File(filePath));
                entity.addPart("session_id", new StringBody(session));
                entity.addPart("avatar_data", fileBody);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return entity;
        }

    }

    public static Drawable filePathToDrawable(String umdFilepath) {
        try {
            File fileboot = new File(umdFilepath);
            if (fileboot.exists()) {
                return new BitmapDrawable(fileboot.getPath());
            }
        } catch (Exception e) {

        }
        return null;
    }

}
