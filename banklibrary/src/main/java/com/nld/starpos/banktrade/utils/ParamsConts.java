package com.nld.starpos.banktrade.utils;

/**
 * Created by jiangrenming on 2017/9/18.
 * 项目中所有参数和交易的key值常量
 */

public class ParamsConts {

    //银联请求接口key
    public static  final  String UNIONPAY_TRANS_URL  ="unionpay_trans_url";
    //银行卡所需的tdup
    public static  final  String UNIONPAY_TDUP  ="unionpay_tpdu";
    //联网模式 0：3G专网, 1：公网, 2：其他  默认是3g
    public static  final  String CONNECT_MODE  ="connect_mode";
    //扫码签到下发数据的时间
    public final static String PARAMS_SCAN_RUN_DATE = "params_scan_run_date";
    //签到的key常量
    public static  final String SIGN_SYMBOL = "signsymbol";
    //签到获取的相关索引key
    public static  final String BATHC_BILNO= "batchbillno";
    //更新签到码key
    public static  final String UPDATA_CODE= "updatecode";
    //密码键盘的key常量
    public static  final String PINPAD_TYPE = "pinpadType";
    //是否免密消费 0为需要输入密码，1代表不需要输入密码
    public static  final String PINPAD_PIN = "neednopin";
    //金额上限不免密 > 300
    public static  final String PINPAD_AMOUNT = "nopinamount";

    //交易类型key常量
    public static  final String TYANS_TYPE = "transtype";
    public static class TransParamsContns{
        //交易批次号key常量
        public static  final String TYANS_BATCHNO = "batchno";
        //交易流水号key常量
        public static  final String SYSTRANCE_NO = "systraceno";
    }

    //终端绑定数据的key常量<跟数据库对应的>
    public static class BindParamsContns{
        //银行商户号
        public static final String  PARAMS_KEY_BASE_MERCHANTID= "unionpay_merid";
        //银联终端号
        public static final String UNIONPAY_TERMID = "unionpay_termid";
        //银行卡结账号
        public static final String PARAMS_KEY_CARD_ACCOUNT = "unionpay_card_account";
        //IC卡公钥版本
        public static final String PARAMS_CAVERSION = "caversion";
        //IC卡参数版本
        public static final String PARAMS_PARAMVERSION = "paramversion";
        //更新状态
        public static final String PARAMS_UPDATASTATUS = "updatastatus";

    }

    //更新code的key常量
    public static  final String UPDATA_STATUS = "updatastatus";

    /**
     * 缺省交易类型 0-预授权，1-消费
     */
    public static final String PARAMS_KEY_DEFAULT_TRANS_TYPE = "DEFAULT_TRANS_TYPE";
    /**
     * 消费撤销刷卡
     */
    public static final String PARAMS_KEY_TRANS_VOID_SWIPE = "IS_VOIDSALE_STRIP";
    /**
     * 授权完成刷卡
     */
    public static final String PARAMS_KEY_AUTH_SALE_SWIPE = "IS_AUTHSALE_STRIP";

    /**
     * 授权完成撤销刷卡
     */
    public static final String PARAMS_KEY_AUTH_SALE_VOID_SWIPE = "IS_VOIDAUTHSALE_STRIP";
    /**
     * 消费撤销是否输入密码
     */
    public static final String PARAMS_KEY_IS_INPUT_TRANS_VOID = "IS_VOIDSALE_PIN";
    /**
     * 授权撤销是否输入密码
     */
    public static final String PARAMS_KEY_IS_INPUT_AUTH_VOID = "IS_VOIDAUTH_PIN";

    /**
     * 授权完成撤销是否输入密码
     */
    public static final String PARAMS_KEY_IS_AUTH_SALE_VOID_PIN = "IS_VOIDAUTHSALE_PIN";
    /**
     * 授权完成请求是否输入密码
     */
    public static final String PARAMS_KEY_IS_AUTH_SALE_PIN = "IS_AUTHSALE_PIN";

    /**
     * 冲正是否超过15s
     */
    public static final String PARAMS_KEY_IS_RESERVE_TIME = "IS_RESERVE_TIME";

    /**
     * 初始化对账标志
     */
    public  static  final  String SETTLE_ACCOUNT = " ";

    public static class TransDeal {
        /**
         * 交易正常处理
         */
        public final static int NORMAL = 0;

        /**
         * 交易特殊处理
         */
        public final static int SPECIAL = 1;

    }
    /**
     * 签到时间
     */
    public final static String PARAMS_RUN_LOGIN_DATE = "translocaldate";
    /**
     * 签到成功与失败标志
     */
   // public final static String PARAMS_SIGN_SUCESS = "signSucess";
    /**
     * 银行卡批结成功与否
     */
    public final static String PARAMS_CARD_SETTLE_SUCESS = "card_settle_Sucess";

    /**
     * 是否需要结算mac，00代表不需要，01代表需要
     */
    public  final  static  String NO_MAC_PARAMS = "00";
    public  final  static  String MAC_PARAMS = "01";

    /**
     * 银行卡开头<刷卡，插卡，挥卡></>
     */
    public final  static  String INSERT_CARD = "02";
    public final  static  String SWING_CARD = "05";
    public final  static  String SWIP_CARD = "07";
    public final  static  String SWING_ALL_CARD = "98";

    /**
     * 免密的标志
     */
    public  final static  String NO_PIN = "1";
    public  final static  String NEED_PIN = "0";

}
