package com.nld.starpos.wxtrade.utils.params;

/**
 * Created by jiangrenming on 2017/10/30.
 */

public class ScanTransFlagUtil {
    public final static String TRANS_CODE_SIGN = "002308";//签到请求
    public final static String TRANS_CODE_BATCHNO = "300000";//同步扫码批次号
    public final static String TRANS_CODE_PARAMS = "300001";//业务参数请求
    public final static String TRANS_CODE_WX_PAY = "660000";//扫码支付
    public final static String TRANS_CODE_WX_QUERY = "670000";//微信查询
    public final static String TRANS_CODE_WX_CX = "680000";//微信撤销
    public final static String TRANS_CODE_WX_TH = "690000";//微信退货
    public final static String TRANS_CODE_WX_TRANS_QUERY = "700000";//扫码支付主动查询
    public final static String TRANS_CODE_WX_LOCAL_TRANS_QUERY = "700001";//扫码本地交易查询
    public final static String TRANS_CODE_WX_SETTLE = "700002";//扫码结算
    public final static String TRANS_CODE_MERNOINFO = "300002";//同步商终信息

    //密码相关
    public final static String PASSWORD_CHANGE = "800000";//管理员密码修改
    public final static String OPER_PASSWORD_CHANGE = "800002";//操作员密码重置
    public final static String OPER_PASSWORD_EXIT = "800003";//密码验证
}
