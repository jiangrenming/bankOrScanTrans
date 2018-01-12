package com.nld.starpos.banktrade.pinUtils;

import android.content.Context;
import android.os.RemoteException;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.emv.AidlCheckCardListener;
import com.nld.cloudpos.aidl.emv.AidlPboc;
import com.nld.cloudpos.aidl.emv.AidlPbocStartListener;
import com.nld.cloudpos.aidl.emv.EmvTransData;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.utils.TransConstans;

public class PbocDev {
	private AidlPboc mDev = null;
	private AidlDeviceService mDeviceService;
	private static PbocDev mInstance = null;
	private Context mContext;

	/**
	 * 开始PBOC流程
	 * 
	 * @param type
	 * @param requestAmtPosition
	 *            请求输入金额位置 0x01 显示卡号前 0x02后
	 * @param listener
	 */
	public void startProc(final byte type, final byte requestAmtPosition, AidlPbocStartListener listener) {

		LogUtils.d(" cashup_startProc called..................");
		EmvTransData transData = new EmvTransData(type// 交易类型0x00消费
				, requestAmtPosition// 请求输入金额位置 0x01 显示卡号前 0x02后
				, false// 是否支持电子现金
				, false// 是否支持国密算法
				, false// 是否强制联机
				, (byte) 0x01// 0x01PBOC 0x02QPBOC
				, (byte) 0x00 // 界面类型：0x00接触 0x01非接
				, new byte[] { 0x00, 0x00, 0x00 });
		try {
			mDev.processPBOC(transData, listener);
			LogUtils.d("PBOC交易开始");
		} catch (RemoteException e) {
			try {
				listener.onError(0x99);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

	}

	/**
	 * 开始非接PBOC流程
	 * 
	 * @param type
	 * @param requestAmtPosition
	 *            请求输入金额位置 0x01 显示卡号前 0x02后
	 * @param listener
	 */
	public void startRfProc(final byte type, final byte requestAmtPosition, AidlPbocStartListener listener) {

		LogUtils.d(" cashup_startProc called..................");
		EmvTransData transData = new EmvTransData(type// 交易类型0x00消费
				, requestAmtPosition// 请求输入金额位置 0x01 显示卡号前 0x02后
				, true// 是否支持电子现金
				, false// 是否支持国密算法
				, false// 是否强制联机
				, (byte) 0x01// 0x01PBOC 0x02QPBOC
				, (byte) 0x01 // 界面类型：0x00接触 0x01非接
				, new byte[] { 0x00, 0x00, 0x00 });
		try {
			mDev.processPBOC(transData, listener);
			LogUtils.d("PBOC交易开始");
		} catch (RemoteException e) {
			try {
				listener.onError(0x99);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

	}

	/**
	 * 获取启动PBOC参数对象
	 * 
	 * @param transCode
	 *            交易代码例 消费：002302
	 * 
	 * @return
	 */
	public static EmvTransData getPbocEmvTransData(String transCode) {

		/*
		 * * @param transType transtype:交易类型，定义如下： 消费 0x00 查询 0x31 预授权 0x03
		 * 指定账户圈存 0x60 非指定账户圈存 0x62 现金圈存 0x63 现金充值撤销 0x17 退货 0x20 消费撤销 0x20
		 * 非指定账户圈存读转入卡 0xF1 卡片余额查询 0xF2 卡片交易日志查询 0xF3 卡片圈存日志查询 0xF4
		 * 
		 * @param moneyPos 请求输入金额位置 0x01:显示卡号前 0x02： 显示卡号后
		 */
		EmvTransData transData = new EmvTransData((byte) 0x00 // 交易类型0x00消费
		, (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
		, true// 是否支持电子现金
				, false// 是否支持国密算法
				, false// 是否强制联机
				, (byte) 0x01// 0x01PBOC 0x02QPBOC
				, (byte) 0x00 // 界面类型：0x00接触 0x01非接
				, new byte[] { 0x00, 0x00, 0x00 });
		if (TransConstans.TRANS_CODE_CONSUME.equals(transCode)) {// 消费
			transData.setTranstype((byte) 0x00);
			transData.setRequestAmtPosition((byte) 0x01);
		} else if (TransConstans.TRANS_CODE_PRE.equals(transCode)) {// 预授权
			transData.setTranstype((byte) 0x03);
			transData.setRequestAmtPosition((byte) 0x02);
			transData.setEcashEnable(false);
		} else if (TransConstans.TRANS_CODE_QC_ZD.equals(transCode)) {// 指定账户圈存
			LogUtils.d("指定账户圈存" + transCode);
			transData.setTranstype((byte) 0x60);
			transData.setEcashEnable(false);
			transData.setRequestAmtPosition((byte) 0x02);
		} else if (TransConstans.TRANS_CODE_QC_FZD.equals(transCode)) {// 非指定账户圈存
			LogUtils.d("非指定账户圈存" + transCode);
			transData.setTranstype((byte) 0xF1);
			transData.setEcashEnable(false);
			transData.setRequestAmtPosition((byte) 0x02);
		} else if (TransConstans.TRANS_CODE_QUERY_BALANCE.equals(transCode)) {// 余额查询
			LogUtils.d("余额查询PBOC流程" + transCode);
			transData.setTranstype((byte) 0x31);
			transData.setRequestAmtPosition((byte) 0x02);
            transData.setEcashEnable(false);
		} else if (TransConstans.TRANS_CODE_CONSUME_CX.equals(transCode)) {// 消费撤销
			LogUtils.d("PBOC简化流程" + transCode);
			transData.setTranstype((byte) 0x20);
			transData.setRequestAmtPosition((byte) 0x02);
            transData.setEcashEnable(false);
		} else if (TransConstans.TRANS_CODE_DZXJ_TRANS_QUERY.equals(transCode)) {
			// 电子现金交易查询
			LogUtils.d("电子现金交易查询获取非接EMV启动参数");
			transData = new EmvTransData((byte) 0xF3 // 交易类型0x00消费
			, (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
			, true// 是否支持电子现金
					, false// 是否支持国密算法
					, false// 是否强制联机
					, (byte) 0x01// 0x01PBOC 0x02QPBOC
					, (byte) 0x00 // 界面类型：0x00接触 0x01非接
					, new byte[] { 0x00, 0x00, 0x00 });
		} else if (TransConstans.TRANS_CODE_DZXJ_BALANCE_QUERY.equals(transCode)) {
			// 电子现金余额查询
			LogUtils.d("电子现金余额查询获取插卡EMV启动参数");
			transData = new EmvTransData((byte) 0xF2 // 交易类型0x00消费
			, (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
			, true// 是否支持电子现金
					, false// 是否支持国密算法
					, false// 是否强制联机
					, (byte) 0x01// 0x01PBOC 0x02QPBOC
					, (byte) 0x00 // 界面类型：0x00接触 0x01非接
					, new byte[] { 0x00, 0x00, 0x00 });
		} else if (TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER.equals(transCode)) {
			// 电子现金普通消费
			LogUtils.d("电子现金普通消费插卡EMV启动参数");
			transData = new EmvTransData((byte) 0x00 // 交易类型0x00消费
			, (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
			, true// 是否支持电子现金
					, false// 是否支持国密算法
					, false// 是否强制联机
					, (byte) 0x01// 0x01PBOC 0x02QPBOC
					, (byte) 0x00 // 界面类型：0x00接触 0x01非接
					, new byte[] { 0x00, 0x00, 0x00 });
		} else {// pboc简化流程，主要是为了读取IC卡卡号
			LogUtils.d("PBOC简化流程" + transCode);
			transData.setTranstype((byte) 0x20);
			transData.setRequestAmtPosition((byte) 0x02);
		}
		LogUtils.d("PBOC简化流程" + transData.getTranstype());
		return transData;
	}
	/**
	 * 获取启动PBOC参数对象 非接Q联机 消费强制联机设置
	 * 
	 * @param transCode
	 *            交易代码例 消费：002302
	 * 
	 * @return
	 */
	public static EmvTransData getPbocEmvTransData2(String transCode) {

		/*
		 * * @param transType transtype:交易类型，定义如下： 消费 0x00 查询 0x31 预授权 0x03
		 * 指定账户圈存 0x60 非指定账户圈存 0x62 现金圈存 0x63 现金充值撤销 0x17 退货 0x20 消费撤销 0x20
		 * 非指定账户圈存读转入卡 0xF1 卡片余额查询 0xF2 卡片交易日志查询 0xF3 卡片圈存日志查询 0xF4
		 * 
		 * @param moneyPos 请求输入金额位置 0x01:显示卡号前 0x02： 显示卡号后
		 */
		EmvTransData transData = new EmvTransData((byte) 0x00 // 交易类型0x00消费
		, (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
		, true// 是否支持电子现金
				, false// 是否支持国密算法
				, true// 是否强制联机
				, (byte) 0x02// 0x01PBOC 0x02QPBOC
				, (byte) 0x01 // 界面类型：0x00接触 0x01非接
				, new byte[] { 0x00, 0x00, 0x00 });
		if (TransConstans.TRANS_CODE_CONSUME.equals(transCode)) {  // 消费
			transData.setTranstype((byte) 0x00);
			transData.setRequestAmtPosition((byte) 0x01);
		}else if (TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY.equals(transCode)){  //电子现金快速支付
			transData.setTranstype((byte) 0x00);
			transData.setRequestAmtPosition((byte) 0x01);
			transData.setForceOnline(false);
		}
		LogUtils.d("PBOC简化流程" + transData.getTranstype());
		return transData;
	}
	public static PbocDev getInstance(Context context, AidlDeviceService service) throws Exception {
		if (null == service) {
			throw new Exception("服务未绑定无法启动EMV");
		}
		if(null==mInstance){
			mInstance = new PbocDev(context, service);
		}
			
		if (null == mInstance) {
			throw new Exception("获取PBOC设备实例失败");
		}
		return mInstance;
	}

	private PbocDev(Context context, AidlDeviceService service) {
		mContext = context;
		mDeviceService = service;
		if (null == service) {
			LogUtils.e("EMV设备获取失败，服务未绑定");
			return;
		}
		try {
			mDev = AidlPboc.Stub.asInterface(service.getEMVL2());
		} catch (RemoteException e) {
			LogUtils.e("EMV设备获取失败");
			e.printStackTrace();
		}
	}
	
	/**
	 * 中断PBOC
	 */
	public void abortPboc(){
	    try {
            mDev.abortPBOC();
        } catch (RemoteException e) {
            LogUtils.e("中断PBOC异常");
            e.printStackTrace();
        }
	}

	/**
	 * 开始检卡
	 * 
	 * @param supportMag
	 * @param supportIC
	 * @param supportRF
	 * @param timeout
	 * @param listener
	 * @return
	 */
	public boolean checkCard(boolean supportMag, boolean supportIC, boolean supportRF, int timeout,
			AidlCheckCardListener listener) {
		try {
			mDev.checkCard(supportMag, supportIC, supportRF, timeout, listener);
		} catch (RemoteException e) {
			LogUtils.e("PBOC检卡失败", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 导入交易金额
	 * 
	 * @param money
	 * @return
	 */
	public boolean importMoney(String money) {
		try {
			mDev.importAmount(money);
		} catch (RemoteException e) {
			LogUtils.e("金额导入失败", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean stopCheckCard() {
		try {
            mDev.cancelCheckCard();
		} catch (RemoteException e) {
			LogUtils.e("停止PBOC失败");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public AidlPboc getOriginalDev() {
		return mDev;
	}

	public boolean importConfirmCardInfoRes(boolean confirm) {
		try {
			mDev.importConfirmCardInfoRes(confirm);
		} catch (RemoteException e) {

			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 获取启动非接QPBOC参数对象
	 * 
	 * @param transCode
	 *            交易代码例 消费：002302
	 * 
	 * @return
	 */
	public static EmvTransData getRFPbocEmvTransData(String transCode) {

		/*
		 * * @param transType transtype:交易类型，定义如下： 消费 0x00 查询 0x31 预授权 0x03
		 * 指定账户圈存 0x60 非指定账户圈存 0x62 现金圈存 0x63 现金充值撤销 0x17 退货 0x20 消费撤销 0x20
		 * 非指定账户圈存读转入卡 0xF1 卡片余额查询 0xF2 卡片交易日志查询 0xF3 卡片圈存日志查询 0xF4
		 * 
		 * @param moneyPos 请求输入金额位置 0x01:显示卡号前 0x02： 显示卡号后
		 */
		EmvTransData transData = new EmvTransData((byte) 0x00 // 交易类型0x00消费
		, (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
		, true// 是否支持电子现金
				, false// 是否支持国密算法
				, false// 是否强制联机
				, (byte) 0x01// 0x01PBOC 0x02QPBOC
				, (byte) 0x00 // 界面类型：0x00接触 0x01非接
				, new byte[] { 0x00, 0x00, 0x00 });
		if (TransConstans.TRANS_CODE_CONSUME.equals(transCode)) {// 消费
			transData.setTranstype((byte) 0x00);
			transData.setRequestAmtPosition((byte) 0x01);
		} else if (TransConstans.TRANS_CODE_PRE.equals(transCode)) {// 预授权
			transData.setTranstype((byte) 0x03);
			transData.setRequestAmtPosition((byte) 0x02);
		} else if (TransConstans.TRANS_CODE_CONSUME_CX.equals(transCode)) {// 消费撤销
			LogUtils.d("PBOC简化流程" + transCode);
			transData.setTranstype((byte) 0x03);
			transData.setRequestAmtPosition((byte) 0x02);
		} else if (TransConstans.TRANS_CODE_DZXJ_TRANS_QUERY.equals(transCode)) {
			// 电子现金交易查询
			LogUtils.d("电子现金交易查询获取非接EMV启动参数");
			transData = new EmvTransData((byte) 0xF3 // 交易类型0x00消费
			, (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
			, true// 是否支持电子现金
					, false// 是否支持国密算法
					, false// 是否强制联机
					, (byte) 0x02// 0x01PBOC 0x02QPBOC
					, (byte) 0x01 // 界面类型：0x00接触 0x01非接
					, new byte[] { 0x00, 0x00, 0x00 });
		} else if (TransConstans.TRANS_CODE_DZXJ_BALANCE_QUERY.equals(transCode)) {
			// 电子现金余额查询
			LogUtils.d("电子现金余额查询获取非接EMV启动参数");
			transData = new EmvTransData((byte) 0xF2 // 交易类型0x00消费
			, (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
			, true// 是否支持电子现金
					, false// 是否支持国密算法
					, false// 是否强制联机
					, (byte) 0x02// 0x01PBOC 0x02QPBOC
					, (byte) 0x01 // 界面类型：0x00接触 0x01非接
					, new byte[] { 0x00, 0x00, 0x00 });
		} else if (TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY.equals(transCode)) {
			// 电子现金快速支付
			LogUtils.d("快速支付获取非接EMV启动参数");
			transData = new EmvTransData((byte) 0x00 // 交易类型0x00消费
			, (byte) 0x02// 请求输入金额位置 0x01 显示卡号前 0x02后
			, true// 是否支持电子现金
					, false// 是否支持国密算法
					, false// 是否强制联机
					, (byte) 0x02// 0x01PBOC 0x02QPBOC
					, (byte) 0x01 // 界面类型：0x00接触 0x01非接
					, new byte[] { 0x00, 0x00, 0x00 });
		} else {// pboc简化流程，主要是为了读取IC卡卡号
			LogUtils.d("PBOC简化流程" + transCode);
			transData.setTranstype((byte) 0x03);
			transData.setRequestAmtPosition((byte) 0x02);
		}

		LogUtils.d("PBOC简化流程" + transData.getTranstype());
		return transData;
	}

	/**
	 * 获取PBOC读取内核55域数据的TLV byte数组
	 * 
	 * @param transCode
	 * @return
	 * @throws Exception
	 */
	public static String[] getKernalTag(String transCode) throws Exception {
		if (transCode.equals(TransConstans.TRANS_CODE_QC_ZD)
				|| transCode.equals(TransConstans.TRANS_CODE_QC_FZD)
				|| transCode.equals(TransConstans.TRANS_CODE_DZXJCZ)) {
			return EMVTAGStr.getLakalaTransferF55Tag();
		} else if (transCode.equals(TransConstans.TRANS_CODE_PRE)
				|| transCode.equals(TransConstans.TRANS_CODE_QUERY_BALANCE)
				|| transCode.equals(TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER)) {
			return EMVTAGStr.getLakalaF55UseModeOne();
		} else if (transCode.equals(TransConstans.TRANS_CODE_CONSUME)) {
			return EMVTAGStr.getLakalaF55UseModeOneForOnlineSale();
		} else if (transCode.equals(TransConstans.TRANS_CODE_DZXJCZ_CX)) {
			return EMVTAGStr.getLakalaCashValueVoidF55Tag();
		} else {
			return EMVTAGStr.getLakalaF55UseModeOne();
		}
	}
	
	/**
	 * @设置TLV
	 * @param tag
	 * @param value
	 * @throws RemoteException 
	 */
	public void setTlv(String tag, byte[] value) throws RemoteException{
		try {
			mDev.setTlv(tag, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
