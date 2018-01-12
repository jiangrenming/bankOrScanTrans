package com.nld.starpos.banktrade.db.bean;

import java.io.Serializable;

public class TransRecord implements Serializable {
    private static final long serialVersionUID = -4538218186281265962L;


	private int id;//主键
	private String transType;// 交易类型
	private String transState;//交易状态  : 1、交易成功  2、已撤销
	private String priaccount;//主账号
	private String transprocode;//交易处理码  (消费和撤销消费)
	private String transamount;//交易金额
	private String systraceno;//POS流水号
	private String translocaltime;//交易时间
	private String translocaldate;//交易日期
	private String expireddate;//卡有效期
//	private String settledate;//清算日期
	private String entrymode;//POS输入方式码
	private String seqnumber;//卡序列号
	private String conditionmode;//服务条件码
//	private String capturecode;//服务点PIN获取码
	private String updatecode;//更新标识码
//	public String insidcode;//受理方标识码
	private String track2data;//2磁道数据
	private String track3data;//3磁道数据
	private String refernumber;//检索参考号(系统参考号)
	private String idrespcode;//授权标识应答码
	private String respcode;//应答码
	private String terminalid;//受卡机终端标识码
	private String acceptoridcode;//受卡方标识码
	private String acceptoridname;//商户名称
	private String addrespkey; //附加响应-密钥数据
	private String adddataword; //附加数据-文字信息
	private String transcurrcode;//交易货币代码
	private String pindata;//个人标识数据
	private String secctrlinfo;//安全控制信息
	private String balanceamount;//余额
	private String icdata;//IC卡数据域
	private String adddatapri;//附加数据-私有
	private String pbocdata;//PBOC电子钱包标准的交易信息
	private String loadparams;//下装参数
	private String cardholderid; //持卡人身份证
	private String batchbillno; //批次号票据号(凭证号)
	private String settledata; //结算信息
	private String mesauthcode;//消息认证码
	private String statuscode; //交易结果状态码  00交易成功 01撤销成功 -1交易失败
	private String reversetimes; //冲正次数，默认3
	private String reserve1;	//预留信息1，交易应答的消息类型；
	private String reserve2;	//预留信息2, 附加响应数据
	private String reserve3;	//预留信息3, CT、AAC、ARPC上送报文中F55
	private String reserve4;	//预留信息4
	private String reserve5;	//预留信息5
	private String isrevoke; //交易查询时使用，判断是否被撤销
	public String getIsrevoke() {
        return isrevoke;
    }
    public void setIsrevoke(String isrevoke) {
        this.isrevoke = isrevoke;
    }
    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPriaccount() {
		return priaccount;
	}
	public void setPriaccount(String priaccount) {
		this.priaccount = priaccount;
	}
	public String getTransprocode() {
		return transprocode;
	}
	public void setTransprocode(String transprocode) {
		this.transprocode = transprocode;
	}
	public String getTransamount() {
		return transamount;
	}
	public void setTransamount(String transamount) {
		this.transamount = transamount;
	}
	public String getSystraceno() {
		return systraceno;
	}
	public void setSystraceno(String systraceno) {
		this.systraceno = systraceno;
	}
	public String getTranslocaltime() {
		return translocaltime;
	}
	public void setTranslocaltime(String translocaltime) {
		this.translocaltime = translocaltime;
	}
	public String getTranslocaldate() {
		return translocaldate;
	}
	public void setTranslocaldate(String translocaldate) {
		this.translocaldate = translocaldate;
	}
	public String getExpireddate() {
		return expireddate;
	}
	public void setExpireddate(String expireddate) {
		this.expireddate = expireddate;
	}
	public String getEntrymode() {
		return entrymode;
	}
	public void setEntrymode(String entrymode) {
		this.entrymode = entrymode;
	}
	public String getSeqnumber() {
		return seqnumber;
	}
	public void setSeqnumber(String seqnumber) {
		this.seqnumber = seqnumber;
	}
	public String getConditionmode() {
		return conditionmode;
	}
	public void setConditionmode(String conditionmode) {
		this.conditionmode = conditionmode;
	}
	public String getUpdatecode() {
		return updatecode;
	}
	public void setUpdatecode(String updatecode) {
		this.updatecode = updatecode;
	}
	public String getTrack2data() {
		return track2data;
	}
	public void setTrack2data(String track2data) {
		this.track2data = track2data;
	}
	public String getTrack3data() {
		return track3data;
	}
	public void setTrack3data(String track3data) {
		this.track3data = track3data;
	}
	public String getRefernumber() {
		return refernumber;
	}
	public void setRefernumber(String refernumber) {
		this.refernumber = refernumber;
	}
	public String getIdrespcode() {
		return idrespcode;
	}
	public void setIdrespcode(String idrespcode) {
		this.idrespcode = idrespcode;
	}
	public String getRespcode() {
		return respcode;
	}
	public void setRespcode(String respcode) {
		this.respcode = respcode;
	}
	public String getTerminalid() {
		return terminalid;
	}
	public void setTerminalid(String terminalid) {
		this.terminalid = terminalid;
	}
	public String getAcceptoridcode() {
		return acceptoridcode;
	}
	public void setAcceptoridcode(String acceptoridcode) {
		this.acceptoridcode = acceptoridcode;
	}
	public String getAcceptoridname() {
		return acceptoridname;
	}
	public void setAcceptoridname(String acceptoridname) {
		this.acceptoridname = acceptoridname;
	}
	public String getAddrespkey() {
		return addrespkey;
	}
	public void setAddrespkey(String addrespkey) {
		this.addrespkey = addrespkey;
	}
	public String getAdddataword() {
		return adddataword;
	}
	public void setAdddataword(String adddataword) {
		this.adddataword = adddataword;
	}
	public String getTranscurrcode() {
		return transcurrcode;
	}
	public void setTranscurrcode(String transcurrcode) {
		this.transcurrcode = transcurrcode;
	}
	public String getPindata() {
		return pindata;
	}
	public void setPindata(String pindata) {
		this.pindata = pindata;
	}
	public String getSecctrlinfo() {
		return secctrlinfo;
	}
	public void setSecctrlinfo(String secctrlinfo) {
		this.secctrlinfo = secctrlinfo;
	}
	public String getBalanceamount() {
		return balanceamount;
	}
	public void setBalanceamount(String balanceamount) {
		this.balanceamount = balanceamount;
	}
	public String getIcdata() {
		return icdata;
	}
	public void setIcdata(String icdata) {
		this.icdata = icdata;
	}
	public String getAdddatapri() {
		return adddatapri;
	}
	public void setAdddatapri(String adddatapri) {
		this.adddatapri = adddatapri;
	}
	public String getPbocdata() {
		return pbocdata;
	}
	public void setPbocdata(String pbocdata) {
		this.pbocdata = pbocdata;
	}
	public String getLoadparams() {
		return loadparams;
	}
	public void setLoadparams(String loadparams) {
		this.loadparams = loadparams;
	}
	public String getCardholderid() {
		return cardholderid;
	}
	public void setCardholderid(String cardholderid) {
		this.cardholderid = cardholderid;
	}
	public String getBatchbillno() {
		return batchbillno;
	}
	public void setBatchbillno(String batchbillno) {
		this.batchbillno = batchbillno;
	}
	public String getSettledata() {
		return settledata;
	}
	public void setSettledata(String settledata) {
		this.settledata = settledata;
	}
	public String getMesauthcode() {
		return mesauthcode;
	}
	public void setMesauthcode(String mesauthcode) {
		this.mesauthcode = mesauthcode;
	}
	public String getStatuscode() {
		return statuscode;
	}
	public void setStatuscode(String statuscode) {
		this.statuscode = statuscode;
	}
	public String getReversetimes() {
		return reversetimes==null?"0":reversetimes;
	}
	public void setReversetimes(String reversetimes) {
		this.reversetimes = reversetimes;
	}
	public String getReserve1() {
		return reserve1;
	}
	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}
	public String getReserve2() {
		return reserve2;
	}
	public void setReserve2(String reserve2) {
		this.reserve2 = reserve2;
	}
	public String getReserve3() {
		return reserve3;
	}
	public void setReserve3(String reserve3) {
		this.reserve3 = reserve3;
	}
	public String getReserve4() {
		return reserve4;
	}
	public void setReserve4(String reserve4) {
		this.reserve4 = reserve4;
	}
	public String getReserve5() {
		return reserve5;
	}
	public void setReserve5(String reserve5) {
		this.reserve5 = reserve5;
	}

	public String getTransType() {
		return transType;
	}

	public void setTransType(String transType) {
		this.transType = transType;
	}

	public String getTransState() {
		return transState;
	}

	public void setTransState(String transState) {
		this.transState = transState;
	}

	@Override
    public String toString() {
        
        return this.getPriaccount()+this.getTransprocode()+this.getTransamount()+this.getSystraceno()+this.getTranslocaltime()+
                this.getTranslocaldate()+this.getExpireddate()+this.getEntrymode()+this.getSeqnumber()+this.getConditionmode()+
                this.getUpdatecode()+this.getTrack2data()+this.getTrack3data()+this.getRefernumber()+this.getIdrespcode()+
                this.getRespcode()+this.getTerminalid()+this.getAcceptoridcode()+this.getAcceptoridname()+this.getAddrespkey()+
                this.getAdddataword()+this.getTranscurrcode()+this.getPindata()+this.getSecctrlinfo()+this.getBalanceamount()+
                this.getIcdata()+this.getAdddatapri()+this.getPbocdata()+this.getLoadparams()+this.getCardholderid()+
                this.getBatchbillno()+this.getSettledata()+this.getMesauthcode()+this.getStatuscode()+this.getReversetimes()+
                this.getReserve1()+this.getReserve2()+this.getReserve3()+this.getReserve4()+this.getReserve5();
    }
	
}
