package com.nld.starpos.wxtrade.bean.scan_pay;


import com.nld.starpos.wxtrade.bean.scan_common.BaseRsp;

public class ScanPayRsp extends BaseRsp {

    //扫码支付返回结果
    private String type;       //接口类型
    private String result;     //返回结果
    private String orderNo; //订单号
    private String payTime; //支付时间
    private String payChannel; //支付渠道
    private String payType;//支付类型
    private String hmac; //签名结果
    private String signType;   //签名类型
    private String version;    //版本
    private String merchantId ; //商户号
    private String returnCode; //返回码
    private String message ; //信息
    private Long amount; //金额
    private Long total_amount; //总金额
    private  String feeAmt;  //交易手续费
    private String refAmt; //最大可退款金额
    private String tranDtTm; //交易日期
    private String trmNo; //总端号
    private String orderId; //流水号
    private String txnSts;//交易状态
    private Long cashFee;//金额

    private String batchNo; //批次号
    private String logNo; //内部订单号
    private  String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    //扫Pos返回的其他字段
    private String qc_Code; //二维码信息地址

    public Long getCashFee() {
        return cashFee;
    }

    public void setCashFee(Long cashFee) {
        this.cashFee = cashFee;
    }

    public String getTxnSts() {
        return txnSts;
    }

    public void setTxnSts(String txnSts) {
        this.txnSts = txnSts;
    }

    public String getFeeAmt() {
        return feeAmt;
    }

    public void setFeeAmt(String feeAmt) {
        this.feeAmt = feeAmt;
    }

    public String getRefAmt() {
        return refAmt;
    }

    public void setRefAmt(String refAmt) {
        this.refAmt = refAmt;
    }

    public String getTranDtTm() {
        return tranDtTm;
    }

    public void setTranDtTm(String tranDtTm) {
        this.tranDtTm = tranDtTm;
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

    public String getQc_Code() {
        return qc_Code;
    }

    public void setQc_Code(String qc_Code) {
        this.qc_Code = qc_Code;
    }

    public Long getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Long total_amount) {
        this.total_amount = total_amount;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getLogNo() {
        return logNo;
    }

    public void setLogNo(String logNo) {
        this.logNo = logNo;
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

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    @Override
    public String getReturnCode() {
        return returnCode;
    }

    @Override
    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

}