package com.nld.starpos.wxtrade.bean.scan_refund;

import com.nld.starpos.wxtrade.bean.scan_common.BaseRsp;

/*
扫码撤销
add by branker @20170120
 */
public class ReverseRsp extends BaseRsp {
    private String type;
    private String result;//S-撤销成功 F-撤销失败 Z-交易未知

    private String requestId;
    private String merchantId;
    private String version;
    private String refAmt;
    private String chlDate;
    private String cashFee;
    private String orderNo;
    private  String amount;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getCashFee() {
        return cashFee;
    }

    public void setCashFee(String cashFee) {
        this.cashFee = cashFee;
    }

    public String getRefAmt() {
        return refAmt;
    }

    public void setRefAmt(String refAmt) {
        this.refAmt = refAmt;
    }

    public String getChlDate() {
        return chlDate;
    }

    public void setChlDate(String chlDate) {
        this.chlDate = chlDate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
