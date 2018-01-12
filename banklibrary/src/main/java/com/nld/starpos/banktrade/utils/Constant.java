package com.nld.starpos.banktrade.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangrenming
 */
public class Constant {

    /*******************************数据库的key****************************************/
    public final static String FIELD_NEW_MKEY_ID="newmkeyid";
    public final static String FIELD_NEW_PIK_ID="newpikid";
    public final static String FIELD_NEW_TDK_ID="newtdkid";
    public final static String FIELD_NEW_MAK_ID="newmakid";
    public final static String FIELD_WX_NEW_MKEY_ID="wxnewmkeyid";
    public final static String FIELD_WX_NEW_PIK_ID="wxnewpikid";
    public final static String FIELD_WX_NEW_TDK_ID="wxnewtdkid";
    public final static String FIELD_WX_NEW_MAK_ID="wxnewmakid";

    /******************************消费类型数据库字段************************************************/
    public static final String CONSUME = "CONSUME"; // 消费
    public static final String CONSUME_REVOCATION = "CONSUME_REVOCATION"; // 消费撤销
    public static final String PRE_CONSUME = "PRE_CONSUME"; // 预授权
    public static final String PRE_CONSUME_COMPLETE = "PRE_CONSUME_COMPLETE"; // 预授权完成
    public static final String PRE_CONSUME_COMPLETE_RESERVE = "PRE_CONSUME_COMPLETE_RESERVE"; // 预授权完成撤销
    public static final String PRE_CONSUME_RESERVE = "PRE_CONSUME_RESERVE"; // 预授权撤销
    /**
     * 数据库中type字段值对应内容
     * @param s
     * @return
     */
    public static String transType2Value(String s) {
        if (CONSUME.equals(s)) {
            return "消费";
        } else if (CONSUME_REVOCATION.equals(s)) {
            return "消费撤销";
        } else if (PRE_CONSUME.equals(s)){
            return "预授权";
        }else if (PRE_CONSUME_COMPLETE.equals(s)){
            return "预授权完成";
        } else if (PRE_CONSUME_RESERVE.equals(s)){
            return "预授权撤销";
        } else if (PRE_CONSUME_COMPLETE_RESERVE.equals(s)){
            return "预授权完成撤销";
        } else {
            return "未知";
        }
    }

    /*************************************预授权相关常量************************************************/
    public static final int TYPE_CARD_EXPENSE = 1001;
    public static final int TYPE_CARD_PREAUTHORIZATION = 1002;
    //========= Broadcast message define ==========//
    public final static String MESSAGE_GOTO_CASH_PAGE = "com.nld.cloudpos.bankline.broadcast.MESSAGE_GOTO_CASH_PAGE";

    /*************************国密与AIDL连接action*****************************************/
    public static final boolean IS_SM = false;
    public static final String USDK_NAME_ACTION = "nld_cloudpos_device_service";
    /******************************** 测试常量******************************************/
    public static final String MER_NO = "302482059495003"; // 商户号 蚂蚁企服
    public static final String TERM_ID = "60022395"; // 终端号 蚂蚁企服
    public static final String TMK = "04459DEF38E39762AEBA04CD9498194C";//主密钥
    public static final String MD5_KEY = "pD0huCAFHbAbcNOUlccccc0WvqIgfTJAFFdEyfgdtKuzUqzAjM2rrhObTSsb80GS";//MD5 key 值
    public static final String POS = "NLP200012295";  //终端序列号

 /***************************************相关msg消息*****************************************************/
    public static final class msg {
        public final static String msg = "msg";
}
    public static final class config {
        public static final String tag_service_ip = "Service_ip";
        public static final String tag_service_port = "Service_port";
        public static final String tag_pswd_max_length = "pswd_max_length";
        public static final Map<String, String> defaultValueMap = new HashMap();
        static {
            defaultValueMap.put(tag_service_ip, "1.202.150.4");
            defaultValueMap.put(tag_service_port, "8980");
            defaultValueMap.put(tag_pswd_max_length, "6");
        }

    }

    /**************************************密码相关回调code*******************************************/

    public final static int ADMIN_PWD_CHECK_RESULT_OK = 0;
    public final static int ADMIN_PWD_CHECK_RESULT_NO = 1;
    public final static int ADMIN_PWD_CHECK_REQ_CODE = 3;
    /***************************************银行卡批结算终端标志*******************************************************/
    //存储批结时的数据
    public final static String PARAMS_SETTLE_DATA = "BANK_PARAMS_SETTLE_DATA";
    //银行批结中断标志
    public static final String BATCH_SETTLE_INTERRUPT_FLAG = "BATCH_SETTLE_INTERRUPT_FLAG";
    public static final String BATCH_SETTLE_INTERRUPT_STEP1 = "1"; // 对账不平，批上送中断
    public static final String BATCH_SETTLE_INTERRUPT_STEP2 = "2"; // 打印明细中断
    public static final String BATCH_SETTLE_INTERRUPT_STEP3 = "3"; // 清除本地数据失败中断
    public static final String BATCH_SETTLE_INTERRUPT_STEP4 = "0"; // 批上送完成

    /******************************银行卡回调code码****************************************************/

    // 1.消费相关
    public static  final  int  COSUME_PIN = 1000;
    public static  final  int  COSUME_NO_PIN = 1001;
    //2.余额查询
    public static  final  int  QUERY_BALANCE = 1002;
    //消费撤销
    public static  final  int  COSUME_REVERSE = 1003;
    //预授权完成撤销撤销
    public static  final  int  PRE_COMPLETED_REVERSE = 1004;
    //签到
    public static  final  int  BANK_SIGN= 1005;
    //预授权
    public static  final  int  PRE_COSUME = 1006;
    //预授权完成
    public static  final  int  PRE_COSUME_COMPLETE = 1007;
    //预授权撤销
    public static  final  int  PRE_COSUME_REVERSE = 1008;
    //ic卡参数查询
    public static  final  int  PARAMS_DOWNLOAD_DATA= 1009;
    //批结算
    public static  final  int  BANK_SETTLE= 1010;
    //批上送
    public static  final  int  BANK_SEND_SETTLE= 1011;
    //联机退货
    public static  final  int  BANK_REVERSE_ONLINE= 1012;
    //脱机退货
    public static  final  int  BANK_REVERSE_OFFLINE= 1013;
    //非指定账户圈存
    public static  final  int  BANK_NO_QUAN_CUN= 1014;
    //指定账户圈存
    public static  final  int  BANK_QUAN_CUN= 1015;
    //电子现金普通消费
    public static  final  int  ELECTRI_NOMARAL_COSUME= 1016;
    //电子现金快速消费
    public static  final  int  ELECTRI_QC_PAY= 1017;

}
