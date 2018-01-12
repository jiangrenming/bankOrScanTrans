package com.nld.netlibrary.https;

import java.security.cert.X509Certificate;

/**
 * Created by jiangrenming on 2017/12/6.
 * 配置文件构造器
 */

public class BankConfigBuilder {

    //重试次数
    public int mRetryTimes;
    public X509Certificate mCacert;
    public  int connectTimes;

    public BankConfigBuilder(){
        mRetryTimes = 3;
        connectTimes = 60;

    }
    public int getmRetryTimes() {
        return mRetryTimes;
    }

    public X509Certificate getmCacert() {
        return mCacert;
    }

    public int getConnectTimes() {
        return connectTimes;
    }

    /**
     * 构建器
     */
    public static class Builder {
        private BankConfigBuilder config;
        public Builder() {
            config = new BankConfigBuilder();
        }
        public BankConfigBuilder build() {
            return config;
        }
        public Builder setBankCacert( X509Certificate mCacert) {
            config.mCacert = mCacert;
            return this;
        }
        public Builder setRetryTimes(int retryTimes) {
            config.mRetryTimes = retryTimes;
            return this;
        }
        public  Builder setConnectTimes(int connectTimes){
            config.connectTimes = connectTimes;
            return this;
        }
    }
}
