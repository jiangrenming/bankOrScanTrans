package com.nld.starpos.wxtrade.bean.scan_common;

import com.nld.starpos.wxtrade.local.db.bean.ScanTransRecord;

import java.io.Serializable;

/**
 * @description 交易结果状态
 * @author Xrh
 */
public class ResultStatus implements Serializable{

	private String retCode;
	private boolean isSucess;
	private String transCode;
	private ScanTransRecord record;

	public ScanTransRecord getRecord() {
		return record;
	}

	public void setRecord(ScanTransRecord record) {
		this.record = record;
	}

	public String getTransCode() {
		return transCode;
	}

	public void setTransCode(String transCode) {
		this.transCode = transCode;
	}


	public boolean isSucess() {
		return isSucess;
	}

	public void setSucess(boolean sucess) {
		isSucess = sucess;
	}

	public String getRetCode() {
		return this.retCode;
	}

	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}

	// 名称：错误代码描述
	// 必选：必填选项
	// 长度：128
	// 说明：
	private String errMsg;

	public String getErrMsg() {
		return this.errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	// 名称：返回结果对象
	// 必选：必填选项
	// 长度：
	// 说明：
	private byte[] retData;

	public byte[] getRetData() {
		return this.retData;
	}

	public void setRetData(byte[] retData) {
		this.retData = retData;
	}

	// 清空数据
	public void clear() {
		retCode = null;
		errMsg = null;
		retData = null;
	}
}
