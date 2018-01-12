/*
package com.nld.starpos.wxtrade.bean.scan_refund;


import com.nld.starpos.wxtrade.bean.scan_common.CommonBean;

*/
/**
 * Created by jiangrenming on 2017/9/28.
 * 扫码退货的类
 *//*


public class ScanRefundBean extends CommonBean {

    private boolean isSucess;
    //扫码退货请求需要的参数
    private String sn; //序列号
    private String terminalNo ;//终端号
    private String txnCnl ;//交易渠道
    private String orderId; //终端流水号
    private String batchNo ; //批次号
    private String orderNo ; //订单号
    private int type; //交易类型
    private Long amount; //金额
    private String year;//年份
    private String date;//日期
    private String time; //时间
    private String currency; //币种
    private String userNo;  //操作员号
    private String payChannel;
    private String logNo;
    private Long total_amount;
    private String payType;
    private String transCode;
    private String addtionInfo;
    private String settleData;
    private String processId;

    //扫码查单 需要的数据
    private String returnCode; //返回码
    private String message;  //信息
    private String channelid;  //订单号
    private long ordFee;   //手续费
    private long MAX_REF_AMT; //最大可退金额
    private String tranDtTm; //交易日期
    private String mercNm; //商户名称
    private String txnSts;  //交易状态
    private String txnTyp;  //交易类型
    private String TXN_CD;  //交易码
    private String oldChlDate ;// 原交易日期
    private String oldRequestId;

    public String getOldRequestId() {
        return oldRequestId;
    }

    public void setOldRequestId(String oldRequestId) {
        this.oldRequestId = oldRequestId;
    }

    private String qryTyp;//类型

    public String getQryTyp() {
        return qryTyp;
    }

    public void setQryTyp(String qryTyp) {
        this.qryTyp = qryTyp;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getOldChlDate() {
        return oldChlDate;
    }

    public void setOldChlDate(String oldChlDate) {
        this.oldChlDate = oldChlDate;
    }
    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public long getOrdFee() {
        return ordFee;
    }

    public void setOrdFee(long ordFee) {
        this.ordFee = ordFee;
    }

    public long getMAX_REF_AMT() {
        return MAX_REF_AMT;
    }

    public void setMAX_REF_AMT(long MAX_REF_AMT) {
        this.MAX_REF_AMT = MAX_REF_AMT;
    }

    public String getTranDtTm() {
        return tranDtTm;
    }

    public void setTranDtTm(String tranDtTm) {
        this.tranDtTm = tranDtTm;
    }

    public String getMercNm() {
        return mercNm;
    }

    public void setMercNm(String mercNm) {
        this.mercNm = mercNm;
    }

    public String getTxnSts() {
        return txnSts;
    }

    public void setTxnSts(String txnSts) {
        this.txnSts = txnSts;
    }

    public String getTxnTyp() {
        return txnTyp;
    }

    public void setTxnTyp(String txnTyp) {
        this.txnTyp = txnTyp;
    }

    public String getTXN_CD() {
        return TXN_CD;
    }

    public void setTXN_CD(String TXN_CD) {
        this.TXN_CD = TXN_CD;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getAddtionInfo() {
        return addtionInfo;
    }

    public void setAddtionInfo(String addtionInfo) {
        this.addtionInfo = addtionInfo;
    }

    public String getSettleData() {
        return settleData;
    }

    public void setSettleData(String settleData) {
        this.settleData = settleData;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public Long getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Long total_amount) {
        this.total_amount = total_amount;
    }

    public String getLogNo() {
        return logNo;
    }

    public void setLogNo(String logNo) {
        this.logNo = logNo;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    @Override
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getTxnCnl() {
        return txnCnl;
    }

    public void setTxnCnl(String txnCnl) {
        this.txnCnl = txnCnl;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    @Override
    public boolean isSucess() {
        return isSucess;
    }

    @Override
    public void setSucess(boolean sucess) {
        isSucess = sucess;
    }
}
*/
