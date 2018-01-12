package com.nld.starpos.wxtrade.bean;

import com.nld.starpos.wxtrade.bean.scan_common.BaseRsp;

public class TranDetailsByOrderRsp extends BaseRsp {


    /**
     * merchantId : 888290059490001
     * signType : MD5
     * version : 1.0.0
     * type : TranDetailsByOrder
     * amount : 1
     * ordFee : 0
     * tranDtTm : 20170208190806
     * mercNm : 弗兰克及按实际弗利萨借方看到了
     * terminalNo : 21312414140
     * orderId : 123456789
     * orderNo : 0000006198
     * orderSts : S
     * payChannel : WXPAY
     * hmac : 8F4C4CACB101B81F118216A36591A7B6
     */

    private String merchantId;
    private String signType;
    private String version;
    private String type;
    private String amount;
    private String ordFee;
    private String tranDtTm;
    private String mercNm;
    private String terminalNo;
    private String orderId;
    private String orderNo;
    private String orderSts;
    private String payChannel;
    private String hmac;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOrdFee() {
        return ordFee;
    }

    public void setOrdFee(String ordFee) {
        this.ordFee = ordFee;
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

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderSts() {
        return orderSts;
    }

    public void setOrderSts(String orderSts) {
        this.orderSts = orderSts;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }
}