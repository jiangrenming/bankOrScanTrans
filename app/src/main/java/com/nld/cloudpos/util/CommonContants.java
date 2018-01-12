package com.nld.cloudpos.util;

import com.nld.cloudpos.bankline.BuildConfig;
import com.nld.cloudpos.data.PrinterConstant;

import java.io.File;

import common.FileUtils;

/**
 * Created by jiangrenming on 2017/12/9.
 * 公用参数
 */

public class CommonContants {

    //需要提供给扫码使用
    public  static String url = BuildConfig.POST_URL;

    //提供给日志使用
    public static final int FONT_TYPE = PrinterConstant.FontType.FONTTYPE_N;
    public final static String LOG_LOCAL_PAHT = FileUtils.getSdcardPath()
            + File.separator + "mtms" + File.separator + "log" + File.separator + "com.newland.bzDirect";

    public static final String OPERATOR = "001";  //操作员
    public static final String USER_NO = "100";  //默认主管密码
}
