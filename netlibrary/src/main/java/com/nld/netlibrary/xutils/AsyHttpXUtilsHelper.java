package com.nld.netlibrary.xutils;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;

import java.util.Map;
import java.util.Set;

/**
 * Created by jiangrenming on 2017/12/8.
 * xutils网络请求
 */

public class AsyHttpXUtilsHelper {

    public  static HttpUtils client ;

    private  static  class InnerXutils{
        private static final AsyHttpXUtilsHelper HELPER = new AsyHttpXUtilsHelper();
    }
    public  static AsyHttpXUtilsHelper getInstance(){
        return  InnerXutils.HELPER;
    }

    public AsyHttpXUtilsHelper(){
        client = new HttpUtils();
        client.configCurrentHttpCacheExpiry(10);
        client.configTimeout(1000 * 10);
        client.configResponseTextCharset("UTF-8");
    }

    /**
     * 网络请求回调
     * @param url
     * @param params
     * @param callBack
     */
    public void asyXUtils(String url, Map<String ,String> params,AsyncRequestCallBack<String> callBack){
        if (client == null){
            new AsyHttpXUtilsHelper();
        }
        client.configResponseTextCharset("GBK");
        RequestParams requestParams = new RequestParams();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            requestParams.addBodyParameter(key, params.get(key));
        }
        LogUtils.i("content="+requestParams.toString());
        client.send(HttpRequest.HttpMethod.POST,url,requestParams,callBack);
    }

}
