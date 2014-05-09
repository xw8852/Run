package com.android.ivymobi.pedometer;

import java.util.ArrayList;
import java.util.List;

import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msx7.core.Manager;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;

public class SyncMetaData {

    public static final void SyncMetaData(ISyncMeta syncMeta) {
        Manager.getInstance().execute(Manager.CMD_GET_STRING, new Request(Config.SEVER_METADATA), new ResponseListener(syncMeta));
    }

    public static class ResponseListener implements IResponseListener {
        ISyncMeta meta;

        public ResponseListener(ISyncMeta meta) {
            super();
            this.meta = meta;
        }

        @Override
        public void onSuccess(Response response) {
            String dataString = response.getData().toString();
            BaseModel<MetaData> data = new Gson().fromJson(dataString, new TypeToken<BaseModel<MetaData>>() {
            }.getType());
            if (data.data != null) {
                if (data.data.domain != null) {
                    ArrayList<String> _data = new ArrayList<String>();
                    for (String str : data.data.domain) {
                        _data.add(str.startsWith("@") ? str : "@" + str);
                    }
                    data.data.domain = _data;
                }
                
            }
            MetaData data2 = UserUtil.getMetaData();
            if (data2 == null || !data2.version.equals(data.data.version)) {
                UserUtil.saveMetaData(data.data);
                if (meta != null)
                    meta.syncMetaData();
            }
          
        }

        @Override
        public void onError(Response response) {
            if (meta != null)
                meta.syncMetaData();
        }

    }

    public interface ISyncMeta {
        public void syncMetaData();
    }

    public static class MetaData {
        public String version;
        public List<String> department;
        public List<String> location;
        public List<String> domain;
    }

}
