package com.nld.starpos.wxtrade.debug.bean.scan_query;

import java.io.Serializable;

/**
 * Created by jiangrenming on 2017/9/29.
 * 扫码查单的返回数据
 */

public class ScanQueryWater implements Serializable{

    private String logNo; //系统内部号
    private String merchantId;//商户号

    private String type;  //接口
    private String returnCode; //返回码
    private String message;  //信息
    private long amount;   //金额
    private String orderNo;  //订单号
    private long feeAmt;   //手续费
    private long refAmt; //最大可退金额
    private String tranDtTm; //交易日期
    private String mercNm; //商户名称
    private String batNo;  //批次号
    private String trmNo;  //终端号
    private String orderId;  //流水号
    private String payChannel; //支付渠道
    private String posOperId;  //操作员
    private String txnSts;  //交易状态
    private String txnTyp;  //交易类型
    private String TXN_CD;  //交易码
    private long ordFee;   //手续费
    /***暂时不用**/
    private int MAX_REF_AMT; //最大交易额
    private String channelid; //订单号

    public long getOrdFee() {
        return ordFee;
    }

    public void setOrdFee(long ordFee) {
        this.ordFee = ordFee;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    private String terminalNo;//终端号


    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public int getMAX_REF_AMT() {
        return MAX_REF_AMT;
    }

    public void setMAX_REF_AMT(int MAX_REF_AMT) {
        this.MAX_REF_AMT = MAX_REF_AMT;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getLogNo() {
        return logNo;
    }

    public void setLogNo(String logNo) {
        this.logNo = logNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public long getFeeAmt() {
        return feeAmt;
    }

    public void setFeeAmt(long feeAmt) {
        this.feeAmt = feeAmt;
    }

    public long getRefAmt() {
        return refAmt;
    }

    public void setRefAmt(long refAmt) {
        this.refAmt = refAmt;
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

    public String getBatNo() {
        return batNo;
    }

    public void setBatNo(String batNo) {
        this.batNo = batNo;
    }

    public String getTrmNo() {
        return trmNo;
    }

    public void setTrmNo(String trmNo) {
        this.trmNo = trmNo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public String getPosOperId() {
        return posOperId;
    }

    public void setPosOperId(String posOperId) {
        this.posOperId = posOperId;
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
}
