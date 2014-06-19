package com.android.ivymobi.pedometer;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.pedometer.widget.AbstractAdapter;
import com.android.ivymobi.pedometer.widget.HorizontalListView;
import com.android.ivymobi.runapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.Manager;
import com.msx7.core.command.ErrorCode;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;
import com.msx7.image.AsyncImageLoad;
import com.msx7.image.AsyncImageLoad.ImageData;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.sso.UMWXHandler;

@InjectActivity(id = R.layout.activity_achi)
public class AchiActivity extends BaseActivity {
    @InjectView(id = R.id.title_left)
    View mLeftTitle;
    @InjectView(id = R.id.title)
    TextView mTitleView;
    @InjectView(id = R.id.root)
    LinearLayout root;
    @InjectView(id = R.id.title_right)
    ImageView mRightTitle;

    final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share", RequestType.SOCIAL);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Inject.inject(this);
        showLoadingDialog(R.string.loadingData);
        mTitleView.setText("成就");
        mLeftTitle.setVisibility(View.VISIBLE);
        mRightTitle.setVisibility(View.VISIBLE);
        mRightTitle.setImageResource(R.drawable.share);
        Request request = new Request();
        request.url = Config.SEVER_ACHIEVEMENT_LIST + "?session_id=" + UserUtil.getSession();
        Manager.getInstance().execute(Manager.CMD_GET_STRING, request, listener);

        mLeftTitle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }

        });

        // 设置分享内容
        mController.setShareContent("友盟社会化组件（SDK）让移动应用快速整合社交分享功能，http://www.umeng.com/social");
        // 设置分享图片, 参数2为图片的url地址
        mController.setShareMedia(new UMImage(this, "http://www.umeng.com/images/pic/banner_module_social.png"));

     // wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
        String appID = "wx967daebe835fbeac";
        // 微信图文分享必须设置一个url 
        String contentUrl = "http://www.umeng.com/social";
        // 添加微信平台，参数1为当前Activity, 参数2为用户申请的AppID, 参数3为点击分享内容跳转到的目标url
        UMWXHandler wxHandler = mController.getConfig().supportWXPlatform(this,appID, contentUrl);
        //设置分享标题
        wxHandler.setWXTitle("友盟社会化组件很不错");
        // 支持微信朋友圈
        UMWXHandler circleHandler = mController.getConfig().supportWXCirclePlatform(this,appID, contentUrl) ;
        circleHandler.setCircleTitle("友盟社会化组件还不错...");
        mController.getConfig().setSsoHandler(new SinaSsoHandler());


        /**
        // 设置分享到微信的内容, 图片类型
       UMImage mUMImgBitmap = new UMImage(getActivity(),
                       "http://www.umeng.com/images/pic/banner_module_social.png");
       WeiXinShareContent weixinContent = new WeiXinShareContent(mUMImgBitmap);
       weixinContent.setShareContent("来自友盟社会化组件（SDK）让移动应用快速整合社交分享功能，微信");
       mController.setShareMedia(weixinContent);

       // 设置朋友圈分享的内容
       CircleShareContent circleMedia = new CircleShareContent(new UMImage(getActivity(),
                       "http://www.umeng.com/images/pic/social/chart_1.png"));
       circleMedia.setShareContent("来自友盟社会化组件（SDK）让移动应用快速整合社交分享功能，朋友圈");
       mController.setShareMedia(circleMedia);
        */
        
        mRightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.getConfig().removePlatform(SHARE_MEDIA.QZONE, SHARE_MEDIA.QQ, SHARE_MEDIA.RENREN, SHARE_MEDIA.TENCENT,
                        SHARE_MEDIA.DOUBAN, SHARE_MEDIA.SMS, SHARE_MEDIA.EMAIL, SHARE_MEDIA.GOOGLEPLUS, SHARE_MEDIA.FACEBOOK, SHARE_MEDIA.TWITTER,
                        SHARE_MEDIA.LAIWANG, SHARE_MEDIA.LAIWANG_DYNAMIC, SHARE_MEDIA.YIXIN, SHARE_MEDIA.YIXIN_CIRCLE, SHARE_MEDIA.INSTAGRAM,
                        SHARE_MEDIA.GENERIC);
                mController.getConfig().setPlatformOrder(SHARE_MEDIA.SINA, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE);
                mController.openShare(AchiActivity.this, false);
            }
        });
    }
    
    @Override 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**使用SSO授权必须添加如下代码 */
        UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode) ;
        if(ssoHandler != null){
           ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    IResponseListener listener = new IResponseListener() {

        @Override
        public void onSuccess(Response response) {
            dismissLoadingDialog();
            String json = String.valueOf(response.getData());
            BaseModel<AchiInfo[][]> baseModel = new Gson().fromJson(json, new TypeToken<BaseModel<AchiInfo[][]>>() {
            }.getType());
            AchiInfo[][] infos = baseModel.data;
            int length = infos.length;
            for (int i = 0; i < length; i++) {
                HorizontalListView gallery = new HorizontalListView(AchiActivity.this);
                gallery.setBackgroundColor(i % 2 == 0 ? 0xf0f0f0 : Color.TRANSPARENT);
                gallery.setAdapter(new AchiAdapter(Arrays.asList(infos[i]), AchiActivity.this));
                gallery.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
                gallery.setPadding(10, 10, 10, 10);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                // params.weight = 1.0f;
                gallery.setLayoutParams(params);
                root.addView(gallery, params);

            }
        }

        @Override
        public void onError(Response response) {
            dismissLoadingDialog();
            ToastUtil.showLongToast(ErrorCode.getErrorCodeString(response.errorCode));
        }

    };

    class AchiAdapter extends AbstractAdapter<AchiInfo> {

        public AchiAdapter(List<AchiInfo> data, Context context) {
            super(data, context);
        }

        @Override
        public View createView(int position, View convertView, ViewGroup parent, LayoutInflater inflater) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.achi_item, null);

            }
            ImageView view = (ImageView) convertView.findViewById(R.id.icon);
            TextView view2 = (TextView) convertView.findViewById(R.id.text1);
            view2.setTextColor(Color.BLACK);
            view2.setText(getItem(position).name);
            ImageData data = null;
            if (getItem(position).achieved == 0) {
                data = new ImageData(null, ImageData.TYPE_GREY, getItem(position).icon);
            } else {
                data = new ImageData(null, ImageData.TYPE_NONE, getItem(position).icon);
            }
            AsyncImageLoad.getIntance().loadImageType(getItem(position).icon, view, data);
            convertView.setPadding(10, 0, 10, 0);
            if ((position / 4) % 2 == 0) {
                convertView.setBackgroundColor(0xf0f0f0);
            } else
                convertView.setBackgroundColor(Color.TRANSPARENT);
            return convertView;
        }

    }

    public static class AchiInfo {
        String id;
        String name;
        String icon;
        String group;
        // 1：表示已获得
        int achieved;
    }
}
