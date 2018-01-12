package com.nld.starpos.banktrade.utils;

/**
 * Created by jiangrenming on 2017/12/7.
 */

public class TransConstans {

    public final static String INPUT_TYPE_SG_PIN = "011";//手工输入有pin
    public final static String INPUT_TYPE_SG_NO_PIN = "012";//手工输入无pin
    public final static String INPUT_TYPE_MAG_PIN = "021";//磁卡有pin
    public final static String INPUT_TYPE_FALLBACK_PIN = "801";//降级输入有PIN
    public final static String INPUT_TYPE_MAG_NO_PIN = "022";//磁卡无pin
    public final static String INPUT_TYPE_IC_PIN = "051";//IC卡输入有PIN
    public final static String INPUT_TYPE_IC_NO_PIN = "052";//IC卡输入无PIN
    public final static String INPUT_TYPE_QPBOC_PIN = "071";//电子现金输入有PIN
    public final static String INPUT_TYPE_QPBOC_NO_PIN = "072";//电子现金输入无PIN
    public final static String INPUT_TYPE_FALLBACK_NO_PIN = "802";//降级输入无PIN


    public final static String TRANS_CODE_QUERY_BALANCE = "002301";//余额查询请求
    public final static String TRANS_CODE_CONSUME = "002302";//消费操作请求
    public final static String TRANS_CODE_CONSUME_CX = "002303";//消费撤销操作请求
    public final static String TRANS_CODE_REVERSE = "002304";//冲正操作请求
    public final static String TRANS_CODE_BATCHNO = "300000";//同步扫码批次号
    public final static String TRANS_CODE_PARAMS = "300001";//业务参数请求
    public final static String TRANS_CODE_MERNOINFO = "300002";//同步商终信息
    public final static String TRANS_CODE_SIGN = "002308";//签到请求
    public final static String TRANS_CODE_SIGN_JS = "002309";//签退(结算)请求
    public final static String TRANS_CODE_PRE = "002313";//预授权
    public final static String TRANS_CODE_PRE_COMPLET = "002314";//预授权完成
    public final static String TRANS_CODE_PRE_COMPLET_CX = "002315";//预授权完成撤销
    public final static String TRANS_CODE_PRE_CX = "002316";//预授权撤销
    public final static String TRANS_CODE_LJTH = "002317";//联机退货
    public final static String TRANS_CODE_UPLOAD_SCRIPT = "002318";//脚本结果通知操作请求
    public final static String TRANS_CODE_IC_KEY_QUERY = "002329";//IC卡公钥查询
    public final static String TRANS_CODE_IC_PARAM_QUERY = "002331";//IC卡参数查询
    public final static String TRANS_CODE_IC_KEY_DOWN = "002330";//IC卡公钥下载
    public final static String TRANS_CODE_IC_PARAM_DOWN = "002332";//IC卡参数下载
    public final static String TRANS_CODE_PARAM_DOWN = "002333";//参数下载
    public final static String TRANS_CODE_DZXJCZ = "002321";//电子现金充值
    public final static String TRANS_CODE_QC_ZD = "002322";//指定账户圈存
    public final static String TRANS_CODE_QC_FZD = "002323";//非指定账户圈存
    public final static String TRANS_CODE_DZXJCZ_CX = "002324";//电子现金充值撤销
    public final static String TRANS_CODE_OFF_TH = "002325";//脱机退货（校验）
    public final static String TRANS_CODE_UPLOAD_OFF_CONSUME = "002326";//脱机消费上送（脱机退货）
    public final static String TRANS_CODE_UPLOAD_TC = "002327";//交易证书（TC）上送
    public final static String TRANS_CODE_UPLOAD_AAC_ARPC = "002328";//交易信息（AAC、ARPC）上送
    public final static String TRANS_CODE_DZXJ_OFFLINE = "002330";//电子现金脱机消费
    public final static String TRANS_CODE_BATCH_SEND_START = "002335";//批上送开始交易
    public final static String TRANS_CODE_BATCH_SEND_END = "002336";//批上送金融交易、批上送结束
    public final static String TRANS_CODE_BATCH_SEND_IC = "002337";//批上送IC卡
    //以下不需要联机的本地交易代码
    public final static String TRANS_CODE_DZXJ_BALANCE_QUERY = "200001";//电子现金余额查询
    public final static String TRANS_CODE_DZXJ_TRANS_QUERY = "200002";//电子现金交易记录
    public final static String TRANS_CODE_DZXJ_TRANS_QUICK_PAY = "200003";//电子现金快速支付
    public final static String TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER = "200004";//电子现金普通消费

    //原收单常量
    public final static String OLD_PAYMENT_PACKAGE_NAME = "com.nld.cloudpos.bankline";//原收单包名
    public final static String OLD_PAYMENT_LAUNCHER_PAGE = "com.nld.cloudpos.bankline.activity.LauncherActivity";//原收单启动页

    public final static String SETTING_PASSWORD = "800001";//应用设置
    public final static String TRANS_CODE_WX_PAY = "660000";//扫码支付
    public final static String TRANS_CODE_WX_QUERY = "670000";//微信查询
    public final static String TRANS_CODE_REPRINT_LAST = "700004";//重打印上一笔交易
    public final static String TRANS_CODE_WX_TH = "690000";//微信退货
}
