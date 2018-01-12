package com.nld.netlibrary.https;

import android.content.Context;

import java.security.cert.X509Certificate;


/**
 * Created by jiangrenming on 2017/12/6.
 * 网络请求封装类
 */

public class HttpConnetionHelper {

    private  static  BankConfigBuilder mConfig;
    public  static HttpClient httpClient;
    public  static  Context mContext;

    private HttpConnetionHelper() {
       throw new RuntimeException("HttpConnetionHelper cannot be initialized!");

    }

    public static  void  init(Context context){
        mContext = context;
        mConfig = new BankConfigBuilder.Builder().build();
        //在这里初始化网络的类
        httpClient = new HttpClient();
    }
    /**
     * 设置配置参数
     */
    public static void setConfig(BankConfigBuilder config) {
        mConfig = config;
    }
    //获取银联证书
    public  static X509Certificate getX509CertFicated(){
        return mConfig.getmCacert();
    }
    //获取重试的次数
    public  static  int getRetryTimes(){
        return  mConfig.getmRetryTimes();
    }
    //获取超时时间
    public  static  int getConnectTimes(){
        return  mConfig.getConnectTimes();
    }

    public static Context getmContext() {
        return mContext;
    }

}
