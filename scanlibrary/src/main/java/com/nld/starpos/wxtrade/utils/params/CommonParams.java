package com.nld.starpos.wxtrade.utils.params;

/**
 * Created by jiangrenming on 2017/10/28.
 */

public class CommonParams {


    public static final String CHARACTER_SET = "characterSet";      //编码只能取以下枚举值 00-GBK，01-GB2312，02-UTF-8，默认00-GBK
    public static final String IP_ADDRESS = "ipAddress";      //IP地址
    public static final String REQEUST_ID = "requestId";       //商户请求号,商户请求的交易流水号唯一
    public static final String SIGN_TYPE = "signType";       //签名方式，只能是MD5登陆设置绑定不做验签
    public static final String VERSION = "version";       //版本号
    public static final String H_MAC = "hmac";       //签名方式，只能是MD5登陆设置绑定不做验签
    public static final String TYPE = "type";               //接口类型
    public static final String SN = "sn";                //序列号SN号
    public static final String POSSn = "posSn";                //终端SN号
    public static final String TERMINAL_NO = "terminalNo";  //设备号
    public static final String TRM_NO = "trmNo";  //终端设备号
    public static final String MD5_KEY = "md5Key";      //MD5密钥
    public static final String MERCHANT_ID = "merchantId";      //商户编号
    public static final String MERC_NM = "mercNm";      //商户名称
    public static final String STL_ACT = "stlAct";      //结算账号
    public static final String STL_NM = "stlNm";      //结账户名
    public static final String TXN_CNL = "txnCnl";      //C-PC收银端  I- 智能POS  T-台牌扫码
    public static final String OPR_ID = "oprId";      //管理员账号
    public static final String TRANS_DATE = "chlDate";      //交易日期
    public static final String OLDTRANS_DATE = "oldChlDate";      //原交易日期
    public static final String TRANS_CURRENCY = "currency";      //交易币种

    public static final String PAY_CHANNEL = "payChannel";       //支付渠道
    public static final String AMOUNT = "amount";       //订单金额，以分为单位，如1元表示为100
    public static final String AUTH_CODE = "authCode";       //二维码
    public static final String ORDER_ID = "orderId";       //终端流水
    public static final String BATCHNO = "batchNo";       //批次号
    public static final String QUERY_BATCHNO = "batNo";       //批次号
    public static final String TOTAL_AMOUNT = "total_amount";       //订单总金额
    public static final String REF_AMT = "refAmt";       //退款金额
    public static final String LOG_TYPE = "logTyp";       //退款金额
    public static final String DLD_TXN_LOGID = "oldTxnLogId";       //原交易流水号
    public static final String OLD_ORDER_NO = "oldOrderNo";       //原订单号
    public static final String OLD_REQUEST_ID = "oldRequestId";       //原请求商户号
    public static final String QUR_TYP = "qryTyp";       //查询类型
    public static final String LOG_NO = "logNo";       //内部订单号(系统流水号)
    public static final String ORDER_NO = "orderNo";       //订单号
    public static final String PAG_NO = "pagNo";       //当前页码
    public static final String PAG_NUM = "pagNum";       //每页记录数
    public static final String START_DATE = "startDate";       //交易日期起
    public static final String END_DATE = "endDate";       //交易日期止

    public static final String PHWEIXINAMT = "phWeiXinAmt";       //微信扫手机
    public static final String PHWEIXINCNT = "phWeiXinCnt";       //微信扫手机总条数
    public static final String POSWEIXINAMT = "posWeiXinAmt";       //扫pos
    public static final String POSWEIXINCNT = "posWeiXinCnt";       //扫pos总条数

    public static final String PHAPLIYNAMT = "phAlipayAmt";
    public static final String PHAPLIYNCNT = "phAlipayCnt";
    public static final String POSAPLIYAMT = "posAlipayAmt";
    public static final String POSAPLIYCNT = "posAlipayCnt";

    public static final String PHUnionpayAMT = "phUnionpayAmt";
    public static final String PHUnionpayCNT = "phUnionpayCnt";
    public static final String POSUnionpayAMT = "posUnionpayAmt";
    public static final String POSUnionpayCNT = "posUnionpayCnt";

    public static final String WX_PAYCHANNEL = "WXPAY";       //微信支付
    public static final String ALI_PAYCHANNEL = "ALIPAY";       //支付宝支付

    public static final String PASS_WD = "passWd";      //管理员密码
    public static final String OPR_ID1 = "oprId1";      //操作员账号
    public static final String OLDPASSWD = "oldPassWd";//旧密码
    public static final String NEWPASSWD = "newPassWd";//新密码
    public static final String USER_NO = "userNo";      //操作员
    public static final String OPER_USER_NO = "oprUserNo";      //主管操作操作员
}
