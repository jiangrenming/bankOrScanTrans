package com.nld.thirdlibrary;

/**
 *
 * @author jiangrenming
 * @date 2017/12/27
 * 第三方调用字段
 */

public class Constants {


    public final static String APPID = "appid";

    public final static String MERCHANTNO = "merid";

    public final static String TERMINALNO = "termid";

    public final static String MSGTYPE = "msg_tp";// 报文类型

    public final static String PAYTYPE = "pay_tp";

    public final static String TRANSTYPE = "proc_tp";// 交易类型

    public final static String TRANSCODE = "proc_cd";// 交易处理码

    public final static String TAG_AMOUNT = "amt";

    public final static String REFERNUM = "refernumber";// 检索参考号

    public final static String ORDERNUM = "order_no";

    public static final String SYSTRACENO = "systraceno"; // 凭证号，仅限消费撤销使用

    public final static String BATCHTRACENO = "batchbillno";// 批次流水号

    public final static String TGTIMESTAMP = "time_stamp";

    public final static String TGORDERINFO = "order_info";

    public final static String TGPRINTINFO = "print_info";

    public final static String TGTRTURNTP = "return_type";// 是否关闭打印界面 1-关闭

    public final static String TGADDWORD = "adddataword";

    public final static String TGRESERVE = "reserve";

    public final static String TGREASON = "reason";// 应答码

    public final static String TGTXNDETAIL = "txndetail";// TXN详情

    /**
     * 扫码支付
     */
    public final static int TRANS_SCAN_PAY = 85;
    /**
     * 空交易类型
     */
    public final static int TRANS_NULL = 86;

    /**
     * 支付宝扫码支付(扫手机)
     */
    public final static int TRANS_SCAN_ALIPAY = 68;

    /**
     * 微信扫码支付(扫手机)
     */
    public final static int TRANS_SCAN_WEIXIN = 66;

    /** 消费 */
    public final static int TRANS_SALE = 101;
}
