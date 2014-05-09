package com.android.ivymobi.pedometer;

import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.data.Mine;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msx7.core.Manager;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;

public final class SyncMine {

    private static SyncMine sMine;

    private SyncMine() {

    }

    static {
        sMine = new SyncMine();
    }

    public static final synchronized SyncMine getInstance() {
        return sMine;
    }

    public void syncMine(ISyncMineFinish syncMineFinish) {
       
        Request request = new Request(Config.SEVER_USER_INFO + "?session_id=" + UserUtil.getSession());
        Manager.getInstance().execute(Manager.CMD_GET_STRING, request, new ResponseListner(syncMineFinish));
    }

    class ResponseListner implements IResponseListener {
        ISyncMineFinish finish;

        public ResponseListner(ISyncMineFinish finish) {
            super();
            this.finish = finish;
        }

        @Override
        public void onSuccess(Response response) {
            String dataString = response.getData().toString();
            BaseModel<Mine> data = new Gson().fromJson(dataString, new TypeToken<BaseModel<Mine>>() {
            }.getType());
            /**
             * edit by abel
             */
            if (data.data != null) {
                UserUtil.saveMine(data.data);
            }
            if (finish != null)
                finish.syncFinish();
           
            /**
             * end
             */
        }

        @Override
        public void onError(Response response) {
            if (finish != null)
                finish.syncFinish();
        }

    }

   public static interface ISyncMineFinish {
        public void syncFinish();
    }
}
