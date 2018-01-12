package com.nld.starpos.wxtrade.bean.scan_pay;


import com.nld.starpos.wxtrade.bean.scan_common.CommonBean;

/**
 * Created by jiangrenming on 2017/9/22.
 * 扫码支付需要的一些常量
 */

public class ScanPayBean extends CommonBean {


    //扫码结束后交易需要的一些参数
    private String transName ; //交易名称
    private int type; //交易类型
    private int oldType; //原交易类型
    private String transNo;  //流水号
    private String posId;  //终端号
    private String messageID; //消息类型码
    private String processID; //《应答码》
    private String serverCode; // 服务点条件码
    private String shopID; //扫码商户号
    private String merchantId;  //商户编号
    private Long amount; //金额
    private String currency; //币种
    private Integer format; //金额的格式
    private String userNo;  //操作员号
    private String txnCnl;    // 交易渠道,I-智能POS T-台牌扫码
    private String payChannel;    //支付渠道,ALIPAY,WXPAY,YLPAY
    private String batchNo; //批次号
    private String authCode;    //扫码支付授权码，设备读取用户微信或支付宝中的条码或者二维码信息
    private String sn;    //序列SN号
    private String terminalNo ;//终端号
    private Long TotalAmount; //订单总金额
    private String merchantName; //商户名称
    private String addInfos; //附加信息
    private String settleDate; //结算日期
    private String transCode; //交易处理码

    //交易返回一部分需要用到的数据
    private String orderNo ; //交易订单号
    private String date;  //日期
    private String time; //时间
    private String year; //年份
    private String payType; //支付类型
    private String logNo ; //系统流水号

    private String oldRequestId;

    //查询参数
    private int pageNo;
    private int pageNumber;
    private String dateTime;
    private String qryTyp; //查询类型


    //扫码批结
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
    private String refundCount;
    private String refundAmount;


    private boolean isSucess;
    //扫码退货请求需要的参数
    private String orderId; //终端流水号
    private Long total_amount;
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

    //密码相关
    private String passWd;
    private String managerNo;  //主管密码
    private String oldPassword;
    private String newPassword;

    public String getPassWd() {
        return passWd;
    }

    public void setPassWd(String passWd) {
        this.passWd = passWd;
    }

    public String getManagerNo() {
        return managerNo;
    }

    public void setManagerNo(String managerNo) {
        this.managerNo = managerNo;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
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

    public Long getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Long total_amount) {
        this.total_amount = total_amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


    @Override
    public boolean isSucess() {
        return isSucess;
    }

    @Override
    public void setSucess(boolean sucess) {
        isSucess = sucess;
    }

    public String getOldRequestId() {
        return oldRequestId;
    }

    public void setOldRequestId(String oldRequestId) {
        this.oldRequestId = oldRequestId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
    public String getQryTyp() {
        return qryTyp;
    }

    public void setQryTyp(String qryTyp) {
        this.qryTyp = qryTyp;
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

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getLogNo() {
        return logNo;
    }

    public void setLogNo(String logNo) {
        this.logNo = logNo;
    }

    public String getTransName() {
        return transName;
    }

    public void setTransName(String transName) {
        this.transName = transName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getOldType() {
        return oldType;
    }

    public void setOldType(int oldType) {
        this.oldType = oldType;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(String settleDate) {
        this.settleDate = settleDate;
    }

    public String getAddInfos() {
        return addInfos;
    }

    public void setAddInfos(String addInfos) {
        this.addInfos = addInfos;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    public Long getTotalAmount() {
        return TotalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        TotalAmount = totalAmount;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getTransNo() {
        return transNo;
    }

    public void setTransNo(String transNo) {
        this.transNo = transNo;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
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

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getServerCode() {
        return serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }

    @Override
    public String getMerchantId() {
        return merchantId;
    }

    @Override
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    @Override
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getFormat() {
        return format;
    }

    public void setFormat(Integer format) {
        this.format = format;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getTxnCnl() {
        return txnCnl;
    }

    public void setTxnCnl(String txnCnl) {
        this.txnCnl = txnCnl;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

}
