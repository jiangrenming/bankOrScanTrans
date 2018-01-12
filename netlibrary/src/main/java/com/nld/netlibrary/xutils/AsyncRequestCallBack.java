package com.nld.netlibrary.xutils;
import android.text.TextUtils;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;

import java.net.URLDecoder;

public class AsyncRequestCallBack<T> extends RequestCallBack<String> {


    @Override
    public void onFailure(HttpException httpException, String errorMsg) {
        LogUtils.i("error="+errorMsg);
    }

    @Override
    public void onSuccess(ResponseInfo<String> responseInfo) {
        try {
            responseInfo.result = URLDecoder.decode(responseInfo.result, "UTF-8");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLoading(long total, long current, boolean isUploading) {
        super.onLoading(total, current, isUploading);
    }
}