package com.nld.starpos.banktrade.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import com.nld.cloudpos.aidl.emv.PCardTransLog;
import com.nld.starpos.banktrade.bean.ResultStatus;
import com.nld.starpos.banktrade.db.bean.TransRecord;

import java.util.Map;

import common.StringUtil;

/**
 * Cache
 * 
 * @description 缓存数据
 */
public class Cache {

	private static Cache instance;
	private String thirdPrintInfo;//第三方打印信息
	private boolean isSucessScriptResult = false;
	private boolean isSucessReserveResult = false;
	private Cache() {

	}

	public synchronized static Cache getInstance() {
		if (null == instance) {
			instance = new Cache();
		}
		return instance;
	}


	public synchronized void clearAllData() {
		transCode = null;
		transMoney = null; // 交易金额
		TxnSeqNo = null;// 交易流水号,武汉通
		serialNo = null; // 交易流水号,收付易
		serInputCode = null;// 服务点输入方式
		CardNo = null;// 武汉通卡片
		CardPrtNo = null; // 卡印刻号
		CrdPhyNo = null;// 物理卡号
		CrdBal = null;// 当前卡余额
		TxnDate = null;// 交易日期
		TxnTime = null;// 交易时间

		PosBatch = null; // POS交易批次号
		TrnSerial = null;// 检索参考号
		UniMchNo = null;// 银联POS商户号
		PosNo = null; // Pos终端号

		transType = null;// 交易类型
		masterAcct = null;// 交易主账号
		cardSeqNo = null;// 卡序列号
		tlvTag55 = null;// 55域
		invalidDate = null; // 卡有效期
		track_2_data = null;// 二磁
		track_3_data = null;// 三磁
		batchNo = null;// 批次号
		pinBlock = null;// PinBlock

		errCode = null; // 错误代码
		errDesc = null; // 错误描述
		thirdPrintInfo = null;

		ReqType = 0x01;// 联机圈存
		// stlvList.clear();
		// lvList.clear();

		retry = false;
		
		printIcData=null;//打印时IC内核数据
		printArqc=null;//ARQC数据
		
		transRecord=null;//交易记录对象
		oldBatchBillno=null;//原交易参考号
		transDate=null;//交易日期
		adddataword=null;//附加数据
		settleData=null;
		resultMap=null;
		bundle=null;//第三方应用调用时如果未签到，在签到前先保存传入的Bundle，等签到成功后使用
		isECash=false;
		field61=null;
		setTransLog(null);
		isThree=false;
		ext_txn_type = null;
		Log.i(Cache.class.getSimpleName(), "清除缓存-->clearAllData()");
	}

	public boolean isSucessScriptResult() {
		return isSucessScriptResult;
	}

	public void setSucessScriptResult(boolean sucessScriptResult) {
		isSucessScriptResult = sucessScriptResult;
	}

	public boolean isSucessReserveResult() {
		return isSucessReserveResult;
	}

	public void setSucessReserveResult(boolean sucessReserveResult) {
		isSucessReserveResult = sucessReserveResult;
	}

	// 撤销类型
	// 01:用户主动撤销
	// 02:POS轮询超时
	private String ext_txn_type;
    public String getExt_txn_type() {
		return ext_txn_type;
	}

	public void setExt_txn_type(String ext_txn_type) {
		this.ext_txn_type = ext_txn_type;
	}
	
	private String settleData;
	
    public String getSettleData() {
        return settleData;
    }

    public void setSettleData(String settleData) {
        this.settleData = settleData;
    }


    private String adddataword;//附加数据48域
	private String printIcData;//打印时用到的IC内核数据
	private String printArqc;
	private TransRecord transRecord;//交易记录对象
	private String oldBatchBillno;//原交易参考号
	private String transDate;//交易日期MMDD格式
	private Bundle bundle;//第三方应用调用时如果未签到，在签到前先保存传入的Bundle，等签到成功后使用
	private String field61;
	private boolean isECash=false;//是否使用电子现金，消费时标识用，如果走电子现金流程，当转联机时，走完整pboc流程。
	private boolean isThree=false;//是否为第三方调用
	

    public boolean isThree() {
        return isThree;
    }

    public void setThree(boolean isThree) {
        this.isThree = isThree;
    }
    public String getField61() {
        return field61;
    }

    public void setField61(String field61) {
        this.field61 = field61;
    }

    public boolean isECash() {
        return isECash;
    }

    public void setECash(boolean isECash) {
        this.isECash = isECash;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public String getAdddataword() {
        return adddataword;
    }

    public void setAdddataword(String adddataword) {
        this.adddataword = adddataword;
    }

	public String getTransDate() {
        return transDate;
    }
	/**
	 * 交易日期
	 * @param transDate 格式MMDD
	 */
    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public String getOldBatchBillno() {
        return oldBatchBillno;
    }

    public void setOldBatchBillno(String oldBatchBillno) {
        this.oldBatchBillno = oldBatchBillno;
    }

    public TransRecord getTransRecord() {
        return transRecord;
    }

    public void setTransRecord(TransRecord transRecord) {
        this.transRecord = transRecord;
    }

    public String getPrintIcData() {
        return printIcData;
    }

    public void setPrintIcData(String printIcData) {
        this.printIcData = printIcData;
    }

    public String getPrintArqc() {
        return printArqc;
    }

    public void setPrintArqc(String printArqc) {
        this.printArqc = printArqc;
    }

    public synchronized int getVersionCode() {
		int versionCode = 1;
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionCode = pi.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	// 应用上下文
	private Context context;

	/**
	 * 交易码
	 */
	private String transCode = "";

	public String getTransCode() {
		return transCode;
	}

	public void setTransCode(String transCode) {
		this.transCode = transCode;
	}

	// 名称：交易金额
	// 长度：10
	// 说明：
	private String transMoney = "";

	public synchronized String getTransMoney() {
		return transMoney;
	}

	public synchronized void setTransMoney(String transMoney) {
		this.transMoney = transMoney;
	}

	// 武汉通交易流水号
	// 长度：4
	// 说明：由空中业务平台产生，唯一标识交易流水。
	private String TxnSeqNo;

	public synchronized String getTxnSeqNo() {
		return TxnSeqNo;
	}

	public synchronized void setTxnSeqNo(String txnSeqNo) {
		TxnSeqNo = txnSeqNo;
	}

	// 服务点输入方式
	// 长度：4
	private String serInputCode;
	private boolean hasPin;

	/**
	 * 获取POS输入方式，获取时会根据hasPin获取值。
	 * 
	 * @return
	 */
	public synchronized String getSerInputCode() {
		return serInputCode;
	}

	/**
	 * 设置时只在获取卡号时设置，设置的值都是 XXX无PIN，然后在输密界面会设置一个标志，hasPin，
	 * 最终组包时，只要通过getSerInputCode（）获取就可以得到最总的值。 输入方式</p> 011 手工方式，且带PIN</p> 012
	 * 手工方式，且不带PIN</p> 021 磁条读入，且带PIN</p> 022 磁条读入, 且不带PIN</p> 031 磁条读入,
	 * 微信被扫带PIN</p> 032 磁条读入, 微信被扫不带PIN</p> 05x IC卡输入, 且磁条信息可靠</p> 80x
	 * Fallback磁条卡</p> 07x qPBOC快速支付</p> 91x 非接触式磁条读入(CUPS MSD)</p> 98x
	 * 标准PBOC借/贷记IC卡读入(非接触式)</p>
	 * 
	 * @param serInputCode
	 */
	public synchronized void setSerInputCode(String serInputCode) {
		this.serInputCode = serInputCode;
	}

	public synchronized void setHasPin(boolean hasPin) {
		this.hasPin = hasPin;
	}

	public synchronized boolean getHasPin() {
		return hasPin;
	}

	// 请求类型
	// 长度：1
	// 说明：0x01 圈存 卡片联机圈存
	// 0x02 兼容性校验 判断卡片和操作终端是否兼容
	// 0x03 圈存失败校验 判断写卡失败是否合法
	private byte ReqType = 0x01;

	public synchronized byte getReqType() {
		return ReqType;
	}

	public synchronized void setReqType(byte reqType) {
		ReqType = reqType;
	}

	// 卡号
	private String CardNo;

	public synchronized String getCardNo() {
		return CardNo;
	}

	public synchronized void setCardNo(String cardNo) {
		CardNo = cardNo;
//		CardNo="6227000130960089157";
	}

	// 卡印刻号
	// 长度：5
	// 说明：武汉通卡片的逻辑卡号。
	private String CardPrtNo;

	public synchronized String getCardPrtNo() {
		return CardPrtNo;
	}

	public synchronized void setCardPrtNo(String cardPrtNo) {
		CardPrtNo = cardPrtNo;
	}

	// 物理卡号
	// 长度：4
	// 说明：卡片的唯一物理标识。
	private String CrdPhyNo;

	public synchronized String getCrdPhyNo() {
		return CrdPhyNo;
	}

	public synchronized void setCrdPhyNo(String crdPhyNo) {
		CrdPhyNo = crdPhyNo;
	}

	// 卡类型
	// 长度：2
	// 说明：武汉通区别卡种类所使用的类型编码。
	private String CardKind;

	public synchronized String getCardKind() {
		return CardKind;
	}

	public synchronized void setCardKind(String cardKind) {
		CardKind = cardKind;
	}

	// 卡片联机计数器
	// 长度：2
	// 说明:联机交易所产生的卡片计数器。
	private String CrdOnCnt;

	public synchronized String getCrdOnCnt() {
		return CrdOnCnt;
	}

	public synchronized void setCrdOnCnt(String crdOnCnt) {
		CrdOnCnt = crdOnCnt;
	}

	// 卡片脱机计数器
	// 长度：2
	// 说明:脱机交易所产生的卡片计数器。
	private String CrdOffCnt;

	public synchronized String getCrdOffCnt() {
		return CrdOffCnt;
	}

	public synchronized void setCrdOffCnt(String crdOffCnt) {
		CrdOffCnt = crdOffCnt;
	}

	// 交易金额
	// 长度：4
	// 本次交易产生的金额，单位到分
	private String TxnAmt;

	public synchronized String getTxnAmt() {
		return TxnAmt;
	}

	public synchronized void setTxnAmt(String txnAmt) {
		TxnAmt = txnAmt;
	}

	// 授权码
	// 长度：6
	// 本次预授权授权码
	private String authCode;

	public synchronized String getAuthCode() {
		return authCode;
	}

	public synchronized void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	// 当前卡余额
	// 长度：4
	// 当前卡片电子钱包的余额，单位到分。
	private String CrdBal;

	public synchronized String getCrdBal() {
		return CrdBal;
	}

	public synchronized void setCrdBal(String crdBal) {
		CrdBal = crdBal;
	}

	// 交易日期
	// 长度：4
	// 由平台在交易产生时生成，未生成时默认为0
	private String TxnDate;

	public synchronized String getTxnDate() {
		return TxnDate;
	}

	public synchronized void setTxnDate(String txnDate) {
		TxnDate = txnDate;
	}

	// 交易时间
	// 长度：3
	// 由平台在交易产生时生成, 未生成时默认为0。
	private String TxnTime;

	public synchronized String getTxnTime() {
		return TxnTime;
	}

	public synchronized void setTxnTime(String txnTime) {
		TxnTime = txnTime;
	}

	// POS交易批次号
	private String PosBatch;
	// 检索参考号，POS中心系统流水号，可唯一确定一笔交易
	private String TrnSerial;
	// 银联POS商户号
	private String UniMchNo;
	// Pos终端号
	private String PosNo;

	public String getPosBatch() {
		return PosBatch;
	}

	public void setPosBatch(String posBatch) {
		PosBatch = posBatch;
	}

	public String getTrnSerial() {
		return TrnSerial;
	}

	public void setTrnSerial(String trnSerial) {
		TrnSerial = trnSerial;
	}

	public String getUniMchNo() {
		return UniMchNo;
	}

	public void setUniMchNo(String uniMchNo) {
		UniMchNo = uniMchNo;
	}

	public String getPosNo() {
		return PosNo;
	}

	public void setPosNo(String posNo) {
		PosNo = posNo;
	}

	// 交易主账号,field2
	private String masterAcct;

	// 卡序列号,field23
	private String cardSeqNo;
	// 55域,field55
	private String tlvTag55;

	// 卡有效期,field14
	private String invalidDate;

	// 二磁,field35
	private String track_2_data;
	// 三磁,field36
	private String track_3_data;
	// 批次号
	private String batchNo;
	// 流水号,field11
	private String serialNo;
	// 交易类型
	private String transType;

	// PinBlock
	private String pinBlock;
	private PCardTransLog[] transLog;

	public String getInvalidDate() {
		return invalidDate;
	}

	public void setInvalidDate(String invalidDate) {
		this.invalidDate = invalidDate;
	}

	public synchronized String getPinBlock() {
		return pinBlock;
	}

	public synchronized void setPinBlock(String pinBlock) {
		this.pinBlock = pinBlock;
	}

	public synchronized String getMasterAcct() {
		return masterAcct;
	}

	public synchronized void setMasterAcct(String masterAcct) {
		this.masterAcct = masterAcct;
	}

	public synchronized String getBatchNo() {
		return batchNo;
	}

	public synchronized void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public synchronized String getCardSeqNo() {
		return cardSeqNo;
	}

	public synchronized void setCardSeqNo(String cardSeqNo) {
	    if(StringUtil.isEmpty(cardSeqNo)){
	        this.cardSeqNo="00";
	    }else{
	        this.cardSeqNo = cardSeqNo;
	    }
	}

	public synchronized String getTlvTag55() {
		return tlvTag55;
	}

	public synchronized void setTlvTag55(String tlvTag55) {
		this.tlvTag55 = tlvTag55;
	}

	public synchronized String getTrack_2_data() {
		return track_2_data;
	}

	public synchronized void setTrack_2_data(String track_2_data) {
		this.track_2_data = track_2_data;
	}

	public synchronized String getTrack_3_data() {
		return track_3_data;
	}

	public synchronized void setTrack_3_data(String track_3_data) {
		this.track_3_data = track_3_data;
	}

	public synchronized String getSerialNo() {
		return serialNo;
	}

	public synchronized void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public synchronized String getTransType() {
		return transType;
	}

	public synchronized void setTransType(String transType) {
		this.transType = transType;
	}

	public synchronized Context getContext() {
		return context;
	}

	public synchronized void setContext(Context context) {
		this.context = context;
	}

	// ---------------------

	// 下达ADPU 指令序列
	// 长度：n
	// 采用STLV 变长记录格式如下。
	// private List<ApduStlv> stlvList = new ArrayList<ApduStlv>();
	//
	// public synchronized List<ApduStlv> getStlvList() {
	// return stlvList;
	// }
	//
	// public synchronized void setStlvList(List<ApduStlv> stlvList) {
	// this.stlvList = stlvList;
	// }
	//
	// // 响应ADPU 指令序列
	// // 长度：n
	// // 采用LV 变长记录格式。
	// private List<ApduStlv> lvList = new ArrayList<ApduStlv>();
	//
	// public synchronized List<ApduStlv> getLvList() {
	// return lvList;
	// }
	//
	// public synchronized void setLvList(List<ApduStlv> lvList) {
	// this.lvList = lvList;
	// }

	// 错误代码
	private String errCode;
	// 错误描述
	private String errDesc;
	// 交易结果Map
	private Map<String, String> resultMap;
	private String reserverCode;

	// 补圈重试
	private boolean retry = false;

	public synchronized String getErrCode() {
		return errCode;
	}

	public synchronized void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public synchronized String getErrDesc() {
		return errDesc;
	}

	public synchronized void setErrDesc(String errDesc) {
		this.errDesc = errDesc;
	}

	public boolean isRetry() {
		return retry;
	}

	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	// ------------------------------------
	// 结果数据
	private ResultStatus restatus;

	public ResultStatus getRestatus() {
		return restatus;
	}

	public void setRestatus(ResultStatus restatus) {
		this.restatus = restatus;
	}

	// ---微信支付内容
	// 二维码
	private String dimensionCode;

	public String getDimensionCode() {
		return dimensionCode;
	}

	public void setDimensionCode(String dimensionCode) {
		this.dimensionCode = dimensionCode;
	}

	public Map<String, String> getResultMap() {
		return resultMap;
	}

	public void setResultMap(Map<String, String> resultMap) {
		this.resultMap = resultMap;
	}
	public PCardTransLog[] getTransLog() {
		return transLog;
	}

	public void setTransLog(PCardTransLog[] transLog) {
		this.transLog = transLog;
	}
	public String getThirdPrintInfo() {
		return thirdPrintInfo;
	}

	public void setThirdPrintInfo(String thirdPrintInfo) {
		this.thirdPrintInfo = thirdPrintInfo;
	}

	private String oprId;  //操作员号

	private String payChannel;  //支付渠道

	private String orderId;     //流水号

	private String terminalNo;  //终端号

	private String txnCnl;		//交易渠道

	private String startDate;		//交易日期起

	private String endDate;		//交易日期止

	private String pagNo;		//当前页码

	private String pagNum;		//每页记录数

	private String orderNo;		//订单号

	private String amount;		//交易金额

	private String oprTyp;		//统计对象

	private String logNo;		//内部订单号(系统流水号)

	public String getLogNo() {
		return logNo;
	}

	public void setLogNo(String logNo) {
		this.logNo = logNo;
	}

	public String getOprTyp() {
		return oprTyp;
	}

	public void setOprTyp(String oprTyp) {
		this.oprTyp = oprTyp;
	}

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

	public String getPagNum() {
		return pagNum;
	}

	public void setPagNum(String pagNum) {
		this.pagNum = pagNum;
	}

	public String getPagNo() {
		return pagNo;
	}

	public void setPagNo(String pagNo) {
		this.pagNo = pagNo;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	private String merchantId;  //商户号

	private String txnLogNo;    //支付宝或者微信交易单号

	private String coreSeqNo;   //微信或者支付宝的扫码号

	public String getCoreSeqNo() {
		return coreSeqNo;
	}

	public void setCoreSeqNo(String coreSeqNo) {
		this.coreSeqNo = coreSeqNo;
	}

	public String getTxnLogNo() {
		return txnLogNo;
	}

	public void setTxnLogNo(String txnLogNo) {
		this.txnLogNo = txnLogNo;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getTxnCnl() {
		return txnCnl;
	}

	public void setTxnCnl(String txnCnl) {
		this.txnCnl = txnCnl;
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

	public String getPayChannel() {
		return payChannel;
	}

	public void setPayChannel(String payChannel) {
		this.payChannel = payChannel;
	}

	public String getOprId() {
		return oprId;
	}

	public void setOprId(String oprId) {
		this.oprId = oprId;
	}

	private int payType;

	public int getPayType() {
		return payType;
	}

	public void setPayType(int payType) {
		this.payType = payType;
	}

	public String getReserverCode() {
		return reserverCode;
	}

	public void setReserverCode(String reserverCode) {
		this.reserverCode = reserverCode;
	}
}
