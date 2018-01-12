package com.nld.starpos.banktrade.utils;

/**
 * Created by jiangrenming on 2017/9/18.
 * 所有交易和参数key 对应的键值 常量放置地
 */

public class TransParams {


    //签到后的值
    public static  class  SingValue{
        public static  final  String UnSingedValue =  "0";
        public static  final  String SingedValue =  "1";
        public static  final  String Singed_UPDATA_CODE =  "0";
    }
    //应用设置中交易设置
    public static  class  SettingTransValue{
        public static  final  int LY_CARD_TYPE =  1; //银行卡
        public static  final  int QC_CARD_TYPE =  2; //二维码
    }


    //重拨次数,默认是3次
    public static  final  String RECONN_TIMES  ="reconntimes";
    //超时时间
    public final static String PARAMS_KEY_COMMUNICATION_OUT_TIME = "dealtimeout";
    /**
     * 电话号码1
     */
    public static final String PARAMS_KEY_DIAL_PHONE1 = "phoneOne";
    /**
     * 电话号码2
     */
    public static final String PARAMS_KEY_DIAL_PHONE2 = "phoneTwo";
    /**
     * 电话号码3
     */
    public static final String PARAMS_KEY_DIAL_PHONE3 = "phoneThree";

    /**
     * 管理电话号码
     */
    public static final String PARAMS_KEY_MANAGE_PHONE = "phoneManager";
    /**
     * 是否支持手动输入卡号 1 —支持， 0—不支持
     */
    public static final String PARAMS_KEY_IS_CARD_INPUT = "input_mode";

}
