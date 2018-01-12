package com.nld.starpos.wxtrade.bean.scan_pay;


import java.io.Serializable;

/**
 * 信息提示
 * @author shipy
 * 2015-05-11
 */
public class MessageTipBean implements Serializable {

	private static final long serialVersionUID = -9202463934421849457L;
	private String projectType;
	/**标题*/
	private String title;
	
	/**内容*/
	private String content;
	
	/**蜂鸣次数*/
	private int beep;
	
	/**超时时间*/
	private int timeOut;
	
	/**输出*/
	private boolean result;
	
	/**能否取消*/
	private boolean cancelable;

	// 是否显示右上角的X关闭按钮。
	private boolean isShowIcon;
	private  boolean isInputPwd;

	private  int oldTransType; //原交易类型
	private String oldBabtchNo;   //原交易流水号
	private String terminalNo; //终端号
	private String oldOrderNo; //上一步订单号
    private String currency; //币种
    private String authCode; //二维码信息
	private String batchNo; //批次号
	private String oldChlDate; //原交易日期
	private String requestId;
	private String reQuestURL;
	private Long amount;
	private String date;

	public String getProjectType() {
		return projectType;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public String getReQuestURL() {
		return reQuestURL;
	}

	public void setReQuestURL(String reQuestURL) {
		this.reQuestURL = reQuestURL;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getOldChlDate() {
		return oldChlDate;
	}

	public void setOldChlDate(String oldChlDate) {
		this.oldChlDate = oldChlDate;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOldOrderNo() {
        return oldOrderNo;
    }

    public void setOldOrderNo(String oldOrderNo) {
        this.oldOrderNo = oldOrderNo;
    }

    public String getTerminalNo() {
		return terminalNo;
	}

	public void setTerminalNo(String terminalNo) {
		this.terminalNo = terminalNo;
	}

	public int getOldTransType() {
		return oldTransType;
	}

	public void setOldTransType(int oldTransType) {
		this.oldTransType = oldTransType;
	}

	public String getOldBabtchNo() {
		return oldBabtchNo;
	}

	public void setOldBabtchNo(String oldBabtchNo) {
		this.oldBabtchNo = oldBabtchNo;
	}

	/**
	 * 初始化一个新创建的MessageTipBean对象
	 */
	public MessageTipBean(){
		this.timeOut = 60;
	}
	
	/**
	 * 获取标题
	 * @return 标题
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 设置标题
	 * @param title 标题
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 获取提示信息
	 * @return 提示信息
	 */
	public String getContent() {
		return content;
	}

	/**
	 * 设置提示信息
	 * @param content 提示信息
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 获取蜂鸣次数
	 * @return 蜂鸣次数
	 */
	public int getBeep() {
		return beep;
	}

	/**
	 * 设置蜂鸣次数
	 * @param beep 蜂鸣次数
	 */
	public void setBeep(int beep) {
		this.beep = beep;
	}

	/**
	 * 获取超时时间
	 * @return 超时时间，单位为秒，默认60秒
	 */
	public int getTimeOut() {
		return timeOut;
	}

	/**
	 * 设置超时时间
	 * @param timeOut 超时时间，单位为秒
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * 获取输出结果
	 * @return 
	 * true-成功，false-失败
	 */
	public boolean getResult() {
		return result;
	}

	/**
	 * 设置输出结果
	 * @param result
	 * true-成功，false-失败
	 */
	public void setResult(boolean result) {
		this.result = result;
	}

	/**
	 * 获取能否取消
	 * @return
	 * true-可以取消，false-不可以取消
	 */
	public boolean isCancelable() {
		return cancelable;
	}

	/**
	 * 设置能否取消
	 * @param cancelable
	 * true-可以取消，false-不可以取消
	 */
	public void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}

	public boolean isShowIcon() {
		return isShowIcon;
	}

	public void setShowIcon(boolean showIcon) {
		isShowIcon = showIcon;
	}

	public boolean isResult() {
		return result;
	}

	public boolean isInputPwd() {
		return isInputPwd;
	}

	public void setInputPwd(boolean inputPwd) {
		isInputPwd = inputPwd;
	}
}
