package com.nld.starpos.wxtrade.debug.bean.scan_settle;

import java.io.Serializable;

/**
 * Created by jiangrenming on 2017/10/10.
 * 扫码批结返回的数据
 */

public class ScanSettleRes implements Serializable {

    private String returnCode;
    private String message;
    private String sysLog;
    private String phWeiXinAmt;
    private String phWeiXinCnt;
    private String posWeiXinAmt;
    private String posWeiXinCnt;
    private String phUnionpayAmt;
    private String phUnionpayCnt;
    private String posUnionpayAmt;
    private String posUnionpayCnt;
    private String phAlipayAmt;
    private String phAlipayCnt;
    private String posAlipayAmt;
    private String posAlipayCnt;
    private String cashbookCnt;
    private String cashbookAmt;
    private String chkRspCod;
    private String transType;


    private String refundCount;
    private String refundAmount;
    private String WeiXinAmt;//微信金额
    private String WeiXinCnt;//微信笔数
    private String AliPayAmt;//支付宝金额
    private String AliPayCnt;//支付宝笔数
    private String TotalAmt;//总计金额
    private String TotalCnt;//总计笔数

    private String isSettleEok1EqualFlag; //扫码对帐平
    private String isSettleEok1NotEqualFlag;//扫码对帐不平
    private String isSettleEok1ErrFlag;//扫码对帐错

    private String merchantName;
    private String shopId;
    private String terminalId;
    private String operId;
    private String batchNo;
    private String dateTime;
    private String requestURL;
    private String projectType;

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getOperId() {
        return operId;
    }

    public void setOperId(String operId) {
        this.operId = operId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getIsSettleEok1EqualFlag() {
        return isSettleEok1EqualFlag;
    }

    public void setIsSettleEok1EqualFlag(String isSettleEok1EqualFlag) {
        this.isSettleEok1EqualFlag = isSettleEok1EqualFlag;
    }

    public String getIsSettleEok1NotEqualFlag() {
        return isSettleEok1NotEqualFlag;
    }

    public void setIsSettleEok1NotEqualFlag(String isSettleEok1NotEqualFlag) {
        this.isSettleEok1NotEqualFlag = isSettleEok1NotEqualFlag;
    }

    public String getIsSettleEok1ErrFlag() {
        return isSettleEok1ErrFlag;
    }

    public void setIsSettleEok1ErrFlag(String isSettleEok1ErrFlag) {
        this.isSettleEok1ErrFlag = isSettleEok1ErrFlag;
    }

    public String getWeiXinAmt() {
        return WeiXinAmt;
    }

    public void setWeiXinAmt(String weiXinAmt) {
        WeiXinAmt = weiXinAmt;
    }

    public String getWeiXinCnt() {
        return WeiXinCnt;
    }

    public void setWeiXinCnt(String weiXinCnt) {
        WeiXinCnt = weiXinCnt;
    }

    public String getAliPayAmt() {
        return AliPayAmt;
    }

    public void setAliPayAmt(String aliPayAmt) {
        AliPayAmt = aliPayAmt;
    }

    public String getAliPayCnt() {
        return AliPayCnt;
    }

    public void setAliPayCnt(String aliPayCnt) {
        AliPayCnt = aliPayCnt;
    }

    public String getTotalAmt() {
        return TotalAmt;
    }

    public void setTotalAmt(String totalAmt) {
        TotalAmt = totalAmt;
    }

    public String getTotalCnt() {
        return TotalCnt;
    }

    public void setTotalCnt(String totalCnt) {
        TotalCnt = totalCnt;
    }

    public String getRefundCount() {
        return refundCount;
    }

    public void setRefundCount(String refundCount) {
        this.refundCount = refundCount;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
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

    public String getSysLog() {
        return sysLog;
    }

    public void setSysLog(String sysLog) {
        this.sysLog = sysLog;
    }

    public String getPhWeiXinAmt() {
        return phWeiXinAmt;
    }

    public void setPhWeiXinAmt(String phWeiXinAmt) {
        this.phWeiXinAmt = phWeiXinAmt;
    }

    public String getPhWeiXinCnt() {
        return phWeiXinCnt;
    }

    public void setPhWeiXinCnt(String phWeiXinCnt) {
        this.phWeiXinCnt = phWeiXinCnt;
    }

    public String getPosWeiXinAmt() {
        return posWeiXinAmt;
    }

    public void setPosWeiXinAmt(String posWeiXinAmt) {
        this.posWeiXinAmt = posWeiXinAmt;
    }

    public String getPosWeiXinCnt() {
        return posWeiXinCnt;
    }

    public void setPosWeiXinCnt(String posWeiXinCnt) {
        this.posWeiXinCnt = posWeiXinCnt;
    }

    public String getPhUnionpayAmt() {
        return phUnionpayAmt;
    }

    public void setPhUnionpayAmt(String phUnionpayAmt) {
        this.phUnionpayAmt = phUnionpayAmt;
    }

    public String getPhUnionpayCnt() {
        return phUnionpayCnt;
    }

    public void setPhUnionpayCnt(String phUnionpayCnt) {
        this.phUnionpayCnt = phUnionpayCnt;
    }

    public String getPosUnionpayAmt() {
        return posUnionpayAmt;
    }

    public void setPosUnionpayAmt(String posUnionpayAmt) {
        this.posUnionpayAmt = posUnionpayAmt;
    }

    public String getPosUnionpayCnt() {
        return posUnionpayCnt;
    }

    public void setPosUnionpayCnt(String posUnionpayCnt) {
        this.posUnionpayCnt = posUnionpayCnt;
    }

    public String getPhAlipayAmt() {
        return phAlipayAmt;
    }

    public void setPhAlipayAmt(String phAlipayAmt) {
        this.phAlipayAmt = phAlipayAmt;
    }

    public String getPhAlipayCnt() {
        return phAlipayCnt;
    }

    public void setPhAlipayCnt(String phAlipayCnt) {
        this.phAlipayCnt = phAlipayCnt;
    }

    public String getPosAlipayAmt() {
        return posAlipayAmt;
    }

    public void setPosAlipayAmt(String posAlipayAmt) {
        this.posAlipayAmt = posAlipayAmt;
    }

    public String getPosAlipayCnt() {
        return posAlipayCnt;
    }

    public void setPosAlipayCnt(String posAlipayCnt) {
        this.posAlipayCnt = posAlipayCnt;
    }

    public String getCashbookCnt() {
        return cashbookCnt;
    }

    public void setCashbookCnt(String cashbookCnt) {
        this.cashbookCnt = cashbookCnt;
    }

    public String getCashbookAmt() {
        return cashbookAmt;
    }

    public void setCashbookAmt(String cashbookAmt) {
        this.cashbookAmt = cashbookAmt;
    }

    public String getChkRspCod() {
        return chkRspCod;
    }

    public void setChkRspCod(String chkRspCod) {
        this.chkRspCod = chkRspCod;
    }
}
