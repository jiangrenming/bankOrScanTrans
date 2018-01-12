package com.nld.starpos.wxtrade.local.db.bean;

import java.io.Serializable;

/**
 * Created by jiangrenming on 2017/9/25.
 * 扫码流水表类
 */

public class ScanTransRecord implements Serializable {

    public int id;//主键
    public int transType; //交易类型
    public int oldTransType; //原交易类型
    public String transamount; //交易金额
    public String scanTime;//交易时间
    public String scanDate;//交易日期
    public String scanYear; //交易年份
    public String respcode;//应答码
    public String memberId;//商户号
    public String memberName;//商户名称
    public String systraceno;//POS流水号
    public String logNo; //系统流水号
    public String payType; //支付类型
    public String batchbillno; //批次号
    public String orderNo; //订单号
    public String terminalId; //终端号
    public String statuscode; //交易结果状态码  00交易成功 01撤销成功 -1交易失败
    public String payChannel; //支付方式
    public String settledata; //结算信息
    public String oper; //操作员
    public String transcurrcode;//交易货币代码
    public String isrevoke; //交易查询时使用，判断是否被撤销
    public String adddataword; //附加信息
    public String transprocode;//交易处理码  (消费和撤销消费)
    public String totalAmount; //交易总金额
    public String authCode; //授权码
    private String member;// 会员信息

    private String amount;//显示金额

    public String getScanYear() {
        return scanYear;
    }

    public void setScanYear(String scanYear) {
        this.scanYear = scanYear;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getLogNo() {
        return logNo;
    }

    public void setLogNo(String logNo) {
        this.logNo = logNo;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTransType() {
        return transType;
    }

    public void setTransType(int transType) {
        this.transType = transType;
    }

    public int getOldTransType() {
        return oldTransType;
    }

    public void setOldTransType(int oldTransType) {
        this.oldTransType = oldTransType;
    }

    public String getTransamount() {
        return transamount;
    }

    public void setTransamount(String transamount) {
        this.transamount = transamount;
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    public String getScanDate() {
        return scanDate;
    }

    public void setScanDate(String scanDate) {
        this.scanDate = scanDate;
    }

    public String getRespcode() {
        return respcode;
    }

    public void setRespcode(String respcode) {
        this.respcode = respcode;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getSystraceno() {
        return systraceno;
    }

    public void setSystraceno(String systraceno) {
        this.systraceno = systraceno;
    }

    public String getBatchbillno() {
        return batchbillno;
    }

    public void setBatchbillno(String batchbillno) {
        this.batchbillno = batchbillno;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getStatuscode() {
        return statuscode;
    }

    public void setStatuscode(String statuscode) {
        this.statuscode = statuscode;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public String getSettledata() {
        return settledata;
    }

    public void setSettledata(String settledata) {
        this.settledata = settledata;
    }

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    public String getTranscurrcode() {
        return transcurrcode;
    }

    public void setTranscurrcode(String transcurrcode) {
        this.transcurrcode = transcurrcode;
    }

    public String getIsrevoke() {
        return isrevoke;
    }

    public void setIsrevoke(String isrevoke) {
        this.isrevoke = isrevoke;
    }

    public String getAdddataword() {
        return adddataword;
    }

    public void setAdddataword(String adddataword) {
        this.adddataword = adddataword;
    }

    public String getTransprocode() {
        return transprocode;
    }

    public void setTransprocode(String transprocode) {
        this.transprocode = transprocode;
    }
}
