package com.nld.starpos.wxtrade.debug.bean.scan_pay;

import java.io.Serializable;

/**
 * Created by jiangrenming on 2017/9/27.
 */

public class QC_Pay_Rsp implements Serializable{


    //生成二维码的接口返回的字段
    private String qc_Code; //二维码信息地址
    private String type;       //接口类型
    private String result;
    private String  orderNo;  //订单号
    private Long total_amount;  //金额
    private  String attach;  //附加数据
    private String returnCode; //返回码
    private String message ; //信息


    //查询返回其他的字段
    private Long amount;
    private String payTime;
    private String payType;
    private String payChannel;
    private String logNo;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Long total_amount) {
        this.total_amount = total_amount;
    }

    public String getQc_Code() {
        return qc_Code;
    }

    public void setQc_Code(String qc_Code) {
        this.qc_Code = qc_Code;
    }
}
