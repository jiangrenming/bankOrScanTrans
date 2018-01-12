package com.nld.starpos.wxtrade.debug.utils.params;

/**
 * Created by jiangrenming on 2017/9/15.
 * 项目中需要用到的类型type 的常量放置地<一般都是不变的类型，只是一个类型有好几种情况></>
 */

public class TransType {

    //快速支付类型
    public static  final int QUK_PAY = 0;
    //电子现金类型
    public static  final int Normal_PAY = 1;


    //签到类别
    public static class SingType{
        //银联签到类型
        public static  final String YLPAY_SIGN_TYPE = "07";
    }


    //银联，微信扫码，支付宝扫码支付的区别码
    public static  class MainTransType{
        public  static  final int  BRUSH = 1001;
        public  static  final int  WECHAT = 1002;
        public  static  final int  ALIPAY = 1003;
        public  static  final Double  PAY_MAX_VALUE = 99999999.99;
    }



    //应用设置中的交易类型
    public static  class  SettingTransType{
        public  static  final String YL_QC_CARD_TYPE = "setType"; //银行卡<1>和二维码<2>
    }

    public static class ScanTransType{
        /**
         * 微信扫码支付(扫手机)
         */
        public final static int TRANS_SCAN_WEIXIN = 66;
        /**
         * 微信扫码支付(扫POS机)
         */
        public final static int TRANS_QR_WEIXIN = 67;
        /**
         * 支付宝扫码支付(扫手机)
         */
        public final static int TRANS_SCAN_ALIPAY = 68;
        /**
         * 支付宝扫码支付(扫POS机)
         */
        public final static int TRANS_QR_ALIPAY = 69;
        /**
         * 扫码退款
         */
        public final static int TRANS_SCAN_REFUND = 73;
        /**
         * 扫码支付(扫POS机)结果查询
         */
        public final static int TRANS_SCAN_POS_CHECK = 75;
        /**
         * 交易列表查询
         */
        public final static int TRANS_QUERY_LIST = 76;
        /**
         * 扫码查单
         */
        public final static int TRANS_QUERY_DETAIL = 77;
        /**
         * 扫码交易批结算
         */
        public final static int TRANS_SCAN_SETTLE = 81;

        //微信支付渠道
        public final static String  TRANS_SCAN_WX_CHANNCLE ="WXPAY";
        //支付宝支付渠道
        public final static String  TRANS_SCAN_ALIPAY_CHANNCLE ="ALIPAY";
        //退款渠道
        public final static String  TRANS_SCAN_REFUND_CHANNCLE ="REFUND";
        //智能pos交易渠道
        public final static  String TRANS_SCAN_PAY_I ="I";
        //app扫码交易渠道
        public final static  String TRANS_SCAN_PAY_A ="A";
        //收银交易渠道
        public final static  String TRANS_SCAN_PAY_C ="C";
        //台牌扫码交易渠道
        public final static  String TRANS_SCAN_PAY_T ="T";


        /**
         * 微信扫POS
         */
        public static final String WEIXIN_PAY_SCANPOS = "weixin_pay_scanpos";
        /**
         * 支付宝扫POS
         */
        public static final String ALIPAY_PAY_SCANPOS = "alipay_pay_scanpos";
        /**
         * 扫码退货
         */
        public static final String SCAN_REFUND = "scan_refund";
        /**
         * 微信扫手机
         */
        public static final String WEIXIN_PAY_SCANPHONE = "weixin_pay_scanphone";
        /**
         * 支付宝扫手机
         */
        public static final String ALIPAY_PAY_SCANPHONE = "alipay_pay_scanphone";
    }

    public static class QueryNet {
        // 支付宝
        public static final String ALIPAY_NUMBER = "1";
        public static final String ALIPAY_STRING = "ALIPAY";
        // 微信
        public static final String WEICHAT_NUMBER = "2";
        public static final String WEICHAT_STRING = "WXPAY";
        // 银联
        public static final String YLPAY_NUMBER = "9";
        public static final String YLPAY_STRING = "YLPAY";
        // 扫码退货
        public static final String SCANREFUND = "4433005";
        // 扫手机
        public static final String SCAN_PHONE = "4433001";
        // 扫POS
        public static final String SCAN_POS = "4433002";
        // 多商户扫手机
        public static final String MORE_SCAN_PHONE = "4433011";
    }

    public static  class SccriptReverseType{
        //脚本类型
        public static  final int SCRIPT =1;
        //冲正类型
        public static  final int REVERSE =2;

    }

    public static  class TransStatueType{
        /**
         * 交易状态
         * <li>0-初始状态</li>
         * <li>1-已撤销</li>
         * <li>2-已调整</li>
         * <li>3-已退货</li>
         * <li>4-上送后被调整</li>
         *
         */
        /**
         * 正常
         */
        public static final int NORMAL = 0;
        /**
         * 已撤销
         */
        public static final int REV = 1;
        /**
         * 部分退款
         */
        public static final int REBATE = 5;
        /**
         * 已全额退货
         */
        public static final int RETURN = 3;

        /**
         * 已调整
         */
        public static final int ADJUST = 2;
        /**
         * 上送后被调整
         */
        public static final int SEND_AND_ADJ = 4;

    }

    /**以下code码是用于调用时回调所用**/
    public static  final  int  SCAN_PAY_CALLBACK_CODE =100;
    public static  final  int  SCAN_PAY_WAIT_CODE =0;
    public static  final  int  SCAN_SETTLE_WAIT_CODE =1;
    public static  final  int  SCAN_PAY_QUERY_CODE =101;
    public static  final  int  SCAN_PAY_REFUND_CODE =102;
    public static  final  int  SCAN_POS_CODE =103;
    public static  final  int  SCAN_SETTLE_CODE =104;
    public static  final  int  ASY_PARAMS_CODE =105;
    public static  final  int  ASY_BATCHNO_CODE =106;
    public static  final  int  PWD_UPDATE_CODE =107;
    public static  final  int  PWD_UPDATE_EXIT =108;
}
