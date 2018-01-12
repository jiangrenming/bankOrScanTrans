package com.nld.thirdlibrary;

import java.io.Serializable;

/**
 *
 * @author jiangrenming
 * @date 2017/12/27
 */

public class ThirdBean implements Serializable{


    private String appId; // 应用包名
    private String msgType;
    private String payType;
    private String orderNo;
    private String transCode;
    private String referenceNo;
    private String marchantNo;
    private String transTime;
    private String closePrintUI;
    private String terminalNo;
    private String batchTraceNo;
    // 凭证号，仅限消费撤销使用
    private String systraceno;
    // 打印信息
    private String printInfo;

    private String amount;
    private String discountAmount;
    private String actualAmount;
    private String merName;
    private int type; //--->第三方的调取方式
    private int transType;  //--->支付方式

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getMarchantNo() {
        return marchantNo;
    }

    public void setMarchantNo(String marchantNo) {
        this.marchantNo = marchantNo;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getClosePrintUI() {
        return closePrintUI;
    }

    public void setClosePrintUI(String closePrintUI) {
        this.closePrintUI = closePrintUI;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getBatchTraceNo() {
        return batchTraceNo;
    }

    public void setBatchTraceNo(String batchTraceNo) {
        this.batchTraceNo = batchTraceNo;
    }

    public String getSystraceno() {
        return systraceno;
    }

    public void setSystraceno(String systraceno) {
        this.systraceno = systraceno;
    }

    public String getPrintInfo() {
        return printInfo;
    }

    public void setPrintInfo(String printInfo) {
        this.printInfo = printInfo;
    }

    public int getTransType() {
        return transType;
    }

    public void setTransType(int transType) {
        this.transType = transType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(String discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(String actualAmount) {
        this.actualAmount = actualAmount;
    }

    public String getMerName() {
        return merName;
    }

    public void setMerName(String merName) {
        this.merName = merName;
    }
}
