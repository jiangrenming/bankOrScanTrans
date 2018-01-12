package com.nld.cloudpos.payment.controller;

/**
 * Created by jiangrenming on 2017/9/15.
 * 网络请求交易时，判断界面的展示的常量
 */

public class TransConts {

    //交易时transHandler使用的常量
    public final static int TRANS_RESULT_CODE_SUCCESS=100;//交易接口成功返回
    public final static int TRANS_RESULT_CODE_FAILD=101;//交易失败
    public final static int TRANS_RESULT_CODE_START=102;//开始网络访问
    public final static int TRANS_RESULT_CODE_SHOW_TIP=103;//提示信息变化
    public final static int TRANS_RESULT_CODE_SHOW_DIALOG=104;//显示对话框
    public static final  int DIALOG_TYPE_AID_SELECT=201;//多应用选择
    public static final  int DIALOG_TYPE_ECASH_TIP=202;//电子现金确认
}
