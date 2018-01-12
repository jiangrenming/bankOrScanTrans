package com.nld.starpos.wxtrade.utils.params;

/**
 * Created by jiangrenming on 2017/10/28.
 */

public class TransParamsValue {


    //蚂蚁企服接口类型
    public static  class  AntCompanyInterfaceType{

        public static  final String  BIND_POS = "TermBind";    //终端绑定
        public static final String  BIND_POS_TYPE = "bindMerc"; //终端绑定接口类型
        public static final String  LOGIN_POS_TYPE = "chkOprPassWd"; //终端登录接口
        public static final String  SCAN_SHOP_ID = "TermBindQry"; //终端绑定查询
        public static  final String  WXPOS = "BarcodePay";    //扫码手机
        public static  final String  SCAN_POS_QUERY= "QryBarcodePay";    //扫码查询接口
        public static  final String  SCAN_QC_POS= "BarcodePosPay";    //客户扫pos接口
        public static  final String  SCAN_REFUND= "RefundBarcodePay";    //扫码退货接口
        public static final String CHECK_OPERATOR_PASS_WORD = "ChkOprPssWd";    //操作员验密接口
        public static final String UPDATE_PASS_WORD = "UpOprPssWd";    //主管密码修改
        public static  final String  OPER_NO_UPDATE= "resetPssWd";    //操作员密码重置
        public static  final String  SCAN_QUER= "TranDetails";    //交易明细查询接口
        public static  final String  TRANS_QUER= "TranListQuery";    //交易列表查询
        public static  final String  SCAN_POST_BATCH_CHK= "PosBatchChk";    //扫码批结
        public static  final String  POSPARMSET= "PosParmSet";    //业务参数接口
        public static  final String  SNYBATCHNO= "batnosynize";    //同步扫码批次号接口
    }

    //澳门项目接口类型的值
    public static  class  InterfaceType{

        public static  final String  BIND_POS = "TermBind";    //终端绑定
        public static final String  BIND_POS_TYPE = "bindMerc"; //终端绑定接口类型
        public static final String  SCAN_SHOP_ID = "TermBindQry"; //终端绑定查询
        public static  final String  WXPOS = "BarcodePay";    //扫码手机
        public static  final String  SCAN_POS_QUERY= "QryBarcodePay";    //扫码查询接口
        public static  final String  SCAN_QC_POS= "BarcodePosPay";    //客户扫pos接口
        public static  final String  SCAN_REFUND= "RefundBarcodePay";    //扫码退货接口
        public static final String CHECK_OPERATOR_PASS_WORD = "ChkOprPssWd";    //操作员验密接口
        public static  final String  SCAN_QUER= "TranDetails";    //交易明细查询接口
        public static  final String  TRANS_QUER= "TranListQuery";    //交易列表查询
        public static  final String  SCAN_POST_BATCH_CHK= "PosBatchChk";    //扫码批结
        public static  final String  POSPARMSET= "PosParmSet";    //业务参数接口
        public static  final String  SNYBATCHNO= "batnosynize";    //同步扫码批次号接口
    }

    /**
     * 交易号与批次号
     */
    public static class TransParamsContns{
        //扫码使用批次号key常量
        public static  final String SCAN_TYANS_BATCHNO = "scan_batchno";
        //扫码使用交易流水号key常量
        public static  final String SCAN_SYSTRANCE_NO = "scan_systraceno";
    }

    //终端绑定扫码数据的key常量<跟数据库对应的>
    public static class BindParamsContns{
        //MD5
        public static  final String MD5_KEY= "md5_key";
        //终端号
        public static final String PARAMS_KEY_BASE_POSID = "termid";
        //商户名称
        public static final String PARAMS_KEY_BASE_MERCHANTNAME = "mchntname";
        //扫码商户号
        public static final String PARAMS_KEY_BASE_SCAN_MERCHANTID = "scan_merid";
        //二维码结算账号
        public static final String PARAMS_KEY_QR_CODE_ACCOUNT = "scan_account";
        //门店号
        public static final String PARAMS_SHOP_ID = "shop_id";
    }

    /**
     * 有关扫码结算相关的常量
     */
    public  static  class SettleConts{

        //批结交易key
        public  static  final  String SETTLE_FLAG = "settle_flag";
        /**
         * 结算中断,总标志,true-需要处理
         */
        public  static  final  String SETTLE_ALL_FLAG = "PARAMS_IS_SETTLT_HALT";
        /**
         * 二维码结算中断
         */
        public final static String PARAMS_IS_SCAN_SETTLT_HALT = "PARAMS_IS_SCAN_SETTLT_HALT";
        /**
         * 打印结算单中断,true-需要处理
         */
        public final static String PARAMS_IS_PRINT_SETTLE_HALT = "PARAMS_IS_PRINT_SETTLE_HALT";
        /**
         * 打印明细中断,true-需要处理
         */
        public final static String PARAMS_IS_PRINT_ALLWATER_HALT = "PARAMS_IS_PRINT_ALLWATER_HALT";
        /**
         * 清除结算数据中断,true-需要处理
         */
        public final static String PARAMS_IS_CLEAR_SETTLT_HLAT = "PARAMS_IS_CLEAR_SETTLT_HLAT";
        /**
         * 扫码交易对账标志, "1"-对账平, "2"-对账不平, "3"-对账错
         */
        public final static String PARAMS_FLAG_SCAN_ACCOUNT_CHECKING = "PARAMS_FLAG_SCAN_ACCOUNT_CHECKING";
        /**
         * 结算时间
         */
        public final static String PARAMS_SETTLE_TIME = "PARAMS_SETTLE_TIME";
        /**
         * 目的：存储批结时的数据
         */
        public final static String PARAMS_SETTLE_DATA = "PARAMS_SETTLE_DATA";
    }

    /**
     * 对账情况
     */
    public static class AccountStatus {
        /**
         * 初始状态
         */
        public static final String INIT = "0";
        /**
         * 对账平
         */
        public static final String EQUAL = "1";
        /**
         * 对账不平
         */
        public static final String UNEQUAL = "2";
        /**
         * 对账错
         */
        public static final String ERROR = "3";

    }

    /**
     * 二维码开关
     */
    public static final String PARAMS_KEY_TRANS_SCAN_PHONE_WEIXIN = "TRANS_SCAN_PHONE_WEIXIN";
    public static final String PARAMS_KEY_TRANS_SCAN_PHONE_ALIPAY = "TRANS_SCAN_PHONE_ALIPAY";
    public static final String PARAMS_KEY_TRANS_SCAN_PHONE_YLPAY = "TRANS_SCAN_PHONE_YLPAY";
    public static final String PARAMS_KEY_TRANS_SCAN_POS_WEIXIN = "TRANS_SCAN_POS_WEIXIN";
    public static final String PARAMS_KEY_TRANS_SCAN_POS_ALIPAY = "TRANS_SCAN_POS_ALIPAY";
    public static final String PARAMS_KEY_TRANS_SCAN_POS_YLPAY = "TRANS_SCAN_POS_YLPAY";
    public static final String PARAMS_KEY_TRANS_SCAN_REFUND = "TRANS_SCAN_REFUND";

    /**
     * 使用 0前/1后置扫描头
     */
    public static final String PARAMS_KEY_SCANNER = "SCANNER";
    /**
     * 0扫手机/1扫POS
     */
    public static final String PARAMS_KEY_SCAN_POS_PHONE = "SCAN_POS_PHONE";
    /**
     * 参数传递,true-需要处理
     */
    public final static String PARAMS_IS_PARAM_DOWN = "PARAMS_IS_PARAM_DOWN";
    /**
     * 业务参数同步日期
     */
    public static final String PARAMS_TRANSMIT_DATE = "PARAMS_TRANSMIT_DATE";

    /**
     * 是否是第一次启动
     */
    public static final String PARAMS_KEY_IS_FIRST = "PARAMS_KEY_IS_FIRST";
    /**
     * 业务参数
     */
    public final static String PARAMS_IS_PARAM = "PARAMS_IS_PARAM";

    /**
     * 扫码设置张数 0代表2联，1代表1联
     */
    public  final  static  String SCAN_PRINTER_STATUE = "scan_printer";
    public  final  static  String SCAN_PRINTER_TWO_PAPER = "2";
    public  final  static  String SCAN_PRINTER_ONE_PAPER = "1";


}
