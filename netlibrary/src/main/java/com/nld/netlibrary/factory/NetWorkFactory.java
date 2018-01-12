package com.nld.netlibrary.factory;

import android.content.Context;

import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.netlibrary.xutils.AsyHttpXUtilsHelper;


/**
 *
 * @author jiangrenming
 * @date 2017/12/8
 * 配置初始化相关配置
 */

public class NetWorkFactory {

    private static class InnerNetWorkFactory{
        private static  final NetWorkFactory NET_WORK = new NetWorkFactory();
    }
    public static  NetWorkFactory getInstance(){
        return InnerNetWorkFactory.NET_WORK;
    }

    public  void initFactory(Context context){
        HttpConnetionHelper.init(context);
        AsyHttpXUtilsHelper.getInstance();
    }
}
