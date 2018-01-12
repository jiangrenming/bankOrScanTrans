package com.nld.cloudpos.ecash.thread;

import android.content.Context;
import android.os.RemoteException;

import com.centerm.iso8583.util.DataConverter;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.emv.AidlPboc;
import com.nld.cloudpos.aidl.emv.AidlPbocStartListener;
import com.nld.cloudpos.aidl.emv.CardInfo;
import com.nld.cloudpos.aidl.emv.EmvTransData;
import com.nld.cloudpos.aidl.emv.PCardLoadLog;
import com.nld.cloudpos.aidl.emv.PCardTransLog;
import com.nld.cloudpos.payment.base.BaseAbstractThread;
import com.nld.cloudpos.payment.socket.TransHandler;
import com.nld.cloudpos.payment.socket.TransHttpHelper;
import com.nld.cloudpos.util.MyLog;
import com.nld.cloudpos.util.TlvUtil;
import com.nld.starpos.banktrade.db.ReverseDao;
import com.nld.starpos.banktrade.db.ScriptNotityDao;
import com.nld.starpos.banktrade.db.SettleDataDao;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.bean.Reverse;
import com.nld.starpos.banktrade.db.bean.ScriptNotity;
import com.nld.starpos.banktrade.db.bean.SettleData;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.ReverseDaoImpl;
import com.nld.starpos.banktrade.db.local.ScriptNotityDaoImpl;
import com.nld.starpos.banktrade.db.local.SettleDataDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.pinUtils.EMVTAGStr;
import com.nld.starpos.banktrade.pinUtils.PbocDev;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.ParseDataUtils;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransactionPackageUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.HexUtil;
import common.StringUtil;

/**
 * @description 指定账户圈存/非指定
 * @date 2015-11-9 17:13:30
 * @author Xrh
 */
public class AppointLoadThread extends BaseAbstractThread {

	private final MyLog logger = MyLog.getLogger(getClass());
	protected TransHandler handler;
	protected Context context;
	protected TransHttpHelper httpRequest;
	protected Map<String, String> dataMap = null;
	protected AidlDeviceService deviceService;
	private String transCode = "";
	private AidlPboc mDev;

	public void setHandler(TransHandler handler) {
		this.handler = handler;
	}

	public AppointLoadThread(Context context, TransHandler handler,
                             AidlDeviceService deviceService) {
		this.context = context;
		this.handler = handler;
		this.deviceService = deviceService;
		try {
			httpRequest = new TransHttpHelper(context, handler);
		} catch (Exception e) {
			e.printStackTrace();
			logger.e("网络访问失败，创建网络请求对象失败。", e);
			handler.messageSendProgressFaild(NldException.ERR_NET_NETREQUEST_E110, false);
		}
		try {
			mDev = PbocDev.getInstance(context, deviceService).getOriginalDev();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean stopThread() {
		return false;
	}

	@Override
	public void run() {
		transCode = Cache.getInstance().getTransCode();
		startPboc();
		super.run();
	}

	public void startPboc() {
		EmvTransData transData = new EmvTransData((byte) 0x60 // 交易类型0x00消费
				, (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
				, true// 是否支持电子现金
				, false// 是否支持国密算法
				, false// 是否强制联机
				, (byte) 0x01// 0x01PBOC 0x02QPBOC
				, (byte) 0x00 // 界面类型：0x00接触 0x01非接
				, new byte[] { 0x00, 0x00, 0x00 });
		
		if (transCode.equals(TransConstans.TRANS_CODE_QC_FZD)) {
			transData = PbocDev.getPbocEmvTransData(transCode);
		}
		
		logger.log(MyLog.LogType.DEBUG, "PBOC开始交易");
		try {
			mDev.processPBOC(transData, pbocTransListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	AidlPbocStartListener pbocTransListener = new AidlPbocStartListener.Stub() {

		@Override
		public void onConfirmCardInfo(CardInfo arg0) throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC,请求卡信息确认");
			mDev.importConfirmCardInfoRes(true);
		}

		@Override
		public void onError(int arg0) throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "第二次PBOC,onError: " + arg0);
			if (null != dataMap) {
				saveReverseRecord(dataMap);
			}
			//交易失败
			handler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_FAILD_E304, false);
			mDev.endPBOC();
		}

		@Override
		public void onReadCardLoadLog(String arg0, String arg1,
                                      PCardLoadLog[] arg2) throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC,请求读取卡交易日志");
		}

		@Override
		public void onReadCardOffLineBalance(String arg0, String arg1,
                                             String arg2, String arg3) throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC,请求读取卡脱机账户信息确认");
		}

		@Override
		public void onReadCardTransLog(PCardTransLog[] arg0)
				throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC,请求读取卡交易日志");
		}

		@Override
		public void onRequestOnline() throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC，请求联机");

			byte[] results = new byte[1024];
			try {
				int count = 0;
				count = mDev.readKernelData(PbocDev.getKernalTag(Cache
						.getInstance().getTransCode()), results);
				logger.log(MyLog.LogType.INFO,
						"读取内核数据结果：" + DataConverter.bytesToHexString(results)
								+ "长度：" + count);

				byte[] datas = null;
				if (count > 0) {
					datas = new byte[count];
					System.arraycopy(results, 0, datas, 0, count);
					logger.log(MyLog.LogType.INFO,
							"field55 =" + HexUtil.bcd2str(datas));
				}
				String tag55 = HexUtil.bcd2str(datas);
				Cache.getInstance().setTlvTag55(tag55);
				logger.log(MyLog.LogType.INFO, "55域数据：" + tag55);
				// 获取磁道、卡片序列号、ARQC数据
				getTraceAndArqc();
			} catch (Exception e) {
				logger.e("55域获取失败", e);
				e.printStackTrace();
			}
			// 此处重新获取交易报文Map是因为PBOC流程中有更新参数
			dataMap = getTransMap();
			dataMap.put("statuscode", "-1");
			// 交易前保存交易数据
			saveOrUpdateTransRecord(dataMap, null);
			// 交易前保存冲正，防止交易过程中出现异常情况需要进行冲正。
			saveReverseRecord(dataMap);
			logger.log(MyLog.LogType.DEBUG, "磁条卡或IC卡降级交易");
			handler.messageSendTipChange("联机请求...");
			// 交易发起联机请求
			int code = httpRequest.transactionRequest(deviceService, transCode,
					dataMap, getTransName());
			if (code != TransHttpHelper.TRANS_SUCCESS) {
				// 交易只要不是成功“00”，中断PBOC
				try {
					mDev.abortPBOC();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			switch (code) {
			case TransHttpHelper.TRANS_SUCCESS:
				boolean transResult = true;
				String resIcdata = "";// 55域数据
				Map<String, String> map = Cache.getInstance().getResultMap();
				String resultCode = map.get("respcode");
				if (!Cache.getInstance().getSerInputCode().startsWith("05")) {// 非IC卡交易
					// 保存数据到结算明细表
					handler.messageSendProgressSuccess("交易成功");
					break; // 标记有交易打印状态
				}
				resIcdata = map.get("icdata");
				logger.log(MyLog.LogType.INFO, "交易结果55域数据：" + resIcdata);
				if (StringUtil.isEmpty(resIcdata)) {
					resIcdata = "";
				}
				boolean pbocResult = false;
				try {
					logger.log(MyLog.LogType.INFO, "交易结果导入联机返回IC卡数据：" + resIcdata
							+ "\n交易结果码：" + resultCode);
					pbocResult = mDev.importOnlineResp(transResult, resultCode,
							resIcdata);
				} catch (RemoteException e) {
					logger.e("IC卡交易结果导入失败，设备获取失败", e);
					e.printStackTrace();
				} catch (Exception e) {
					logger.e("IC卡交易结果导入失败", e);
					e.printStackTrace();
				}
				logger.log(MyLog.LogType.INFO, "ic卡交易结果：" + pbocResult);
				if (!pbocResult) {
					handler.messageSendProgressFaild(NldException.ERR_DEV_INPUT_KERNELE305, false);
					try {
						mDev.endPBOC();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				break;
			case TransHttpHelper.TRANS_FAILD:
				// 删除交易前保存的冲正记录
				deleteReverseRecord(dataMap.get("batchbillno"));
				deleteTransRecord(dataMap.get("batchbillno"));
				logger.log(MyLog.LogType.INFO, "交易失败");
				break;
			case TransHttpHelper.TRANS_REVERSE:
				logger.log(MyLog.LogType.INFO, "交易失败需要冲正");
				// 冲正数据在交易前已经保存，在此无须再保存
				// 冲正处理结束，通知页面跳转至结果页
				deleteTransRecord(dataMap.get("batchbillno"));
				handler.messageSendProgressFaild(Cache.getInstance()
						.getErrCode(), Cache.getInstance().getErrDesc(), true);
				break;
			}
		}

		@Override
		public void onTransResult(int arg0) throws RemoteException {
			logger.log(MyLog.LogType.INFO, "PBOC，交易结果：" + arg0);
			switch (arg0) {
			case 0x01:// 交易批准
			case 0x02:// 交易拒绝
				try {
					byte[] temp = new byte[1024];
					int count;
					count = mDev.readKernelData(
							EMVTAGStr.getLakalaScriptResultTag(), temp);
					if (count <= 0) {
						//成功导入交易结果后，读取内核失败
						handler.messageSendProgressFaild(NldException.ERR_DEV_READ_KERNEL_E303, false);
						break;
					}
					byte[] datas = new byte[count];
					System.arraycopy(temp, 0, datas, 0, count);
					// 读取内核数据，脚本处理结果
					String scriptExeResponse = HexUtil.bcd2str(datas);
					logger.log(MyLog.LogType.DEBUG, "读取脚本上送数据 == "
							+ scriptExeResponse);
					String tagDF31 = TlvUtil.tlvToMap(scriptExeResponse).get(
							"DF31");
					logger.log(MyLog.LogType.DEBUG, "发卡行脚本结果 Tag DF31：" + tagDF31);
					String transCode = Cache.getInstance().getTransCode();
					if (transCode.equals("002322")
							|| transCode.equals("002323")
							|| transCode.equals("002321")) {
						if (tagDF31 == null || "".equals(tagDF31)) { // 无脚本下发
							logger.e("无发卡行脚本执行结果，引发冲正");
							
							handler.messageSendProgressFaild(NldException.ERR_DEV_NO_ISSUE_SCRIPT_E306, true);
							mDev.endPBOC();
							break;
						}
					}

					String ic55data = Cache.getInstance().getResultMap()
							.get("icdata");
					boolean hasScriptData = haveScriptData(ic55data);
					logger.log(MyLog.LogType.INFO, "返回55域是否有脚本处理结果：" + hasScriptData);
					// 保存发卡行脚本结果
					// 返回结果包含有发卡行脚本时时才执行脚本结果上送
					if (hasScriptData) { // 交易联机成功（指拉卡拉平台F39返回00）
						logger.log(MyLog.LogType.INFO, "返回55域含脚本处理结果，保存结果待上送");
						Map<String, String> resultMap = Cache.getInstance()
								.getResultMap();
						ScriptNotity record = ParseDataUtils.mapObjToScriptNotity(
								dataMap, resultMap, scriptExeResponse);
						ScriptNotityDao mScriptNotitiesDao = new ScriptNotityDaoImpl();
						mScriptNotitiesDao.save(record);
					}

					// 读取内核数据，F55用法一，用于CT或AAC、ARPC上送
					logger.log(MyLog.LogType.INFO, "读取内核55域结果");
					byte[] temp2 = new byte[1024];
					int count2 = mDev.readKernelData(
							EMVTAGStr.getLakalaF55UseModeOne(), temp2);
					logger.log(MyLog.LogType.INFO, "F55域内核长度：" + count2);
					if (count2 <= 0) {
						logger.e("成功导入交易结果后，读取内核失败");
						handler.messageSendProgressFaild(NldException.ERR_DEV_READ_KERNEL_E303, false);
						break;
					}
					byte[] datas2 = new byte[count2];
					System.arraycopy(temp2, 0, datas2, 0, count2);
					String f55Data = HexUtil.bcd2str(datas2);
					logger.log(MyLog.LogType.INFO, "读取内核 F55Data == " + f55Data);
					dataMap.put("reserve3", f55Data);

					// 圈存交易需验证脚本是否执行成功，若脚本执行失败，交易则为失败
					String tag95 = TlvUtil.tlvToMap(scriptExeResponse)
							.get("95");
					logger.log(MyLog.LogType.INFO, "脚本处理结果TVR Tag 95：" + tag95);
					if (0x01 == arg0 && transferScriptSuc(tag95)) { // 交易接受
																	// 判断圈存交易是否脚本执行成功
						logger.log(MyLog.LogType.INFO, "交易接受");
						// IC卡交易接受，结算时需上送TC或者ARPC
						// 发卡行认证是否执行成功Tag 95 第5字节b7=1，结算时上送ARPC
						if (!arpcSucess(tag95)) { // ARPC（发卡行认证）执行失败,需上送ARPC
							logger.log(MyLog.LogType.INFO, "发卡行认证执行失败");
							dataMap.put("statuscode", "AR");
						} else { // 上送TC
							logger.log(MyLog.LogType.INFO, "发卡行认证执行成功");
							dataMap.put("statuscode", "TC");
						}

						// 打印设置
						if (!Cache.getInstance().getTransCode()
								.equals("002301")) { // 除余额查询之外的完整PBOC流程，都有打印
							byte[] temp3 = new byte[1024];
							int count3 = mDev.readKernelData(
									EMVTAGStr.getkernelDataForPrint(), temp3);
							byte[] data3 = new byte[count3];
							System.arraycopy(temp3, 0, data3, 0, count3);
							String printData = HexUtil.bcd2str(data3);
							getKernelDataForPrint(dataMap, printData); // 读取需要打印的数据
						}

						// 保存结算
						if (!Cache.getInstance().getTransCode()
								.equals("002321")
								&& !Cache.getInstance().getTransCode()
										.equals("002301")) { // 完整PBOC流程中除了预授权其他都需要参与结算
							logger.log(MyLog.LogType.DEBUG, "保存结算");
							saveSettleRecordToDB(dataMap, Cache.getInstance()
									.getResultMap());
						}

						// 保存交易报文成功数据
						Map<String, String> resMap = Cache.getInstance()
								.getResultMap();
						resMap.put("statuscode", "");
						saveOrUpdateTransRecord(dataMap, resMap);
						// 删除交易前保存的冲正记录
						deleteReverseRecord(dataMap.get("batchbillno"));
						handler.messageSendProgressSuccess("交易成功");
					} else if (0x02 == arg0 || !transferScriptSuc(tag95)) { // 交易拒绝
						logger.log(MyLog.LogType.DEBUG, "交易拒绝"); // 交易拒绝原因有二：1、内核拒绝，2脚本执行失败（或者不存在脚本）

						// IC卡交易拒绝，结算时需上送AAC
						dataMap.put("statuscode", "AC");
						// 保存交易报文成功数据
						Map<String, String> resMap = Cache.getInstance()
								.getResultMap();
						resMap.put("statuscode", "");
						saveOrUpdateTransRecord(dataMap, resMap);
						// 平台交易成功，内核拒绝引发冲正,冲正记录在交易前保存了
						handler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_REFUSE_E307, true);
					} else {

						// 删除交易前保存的冲正记录
						deleteReverseRecord(dataMap.get("batchbillno"));
						handler.messageSendProgressFaild(NldException.ERR_DEV_RESULT_UNKNOW_E308, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 0x03:// 终止
				logger.log(MyLog.LogType.INFO, "交易终止");
				handler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_END_E309, false);
				break;
			case 0x04:// FALLBACK
				break;
			case 0x05:// 采用其它页面
				logger.e("交易异常"+0x05);
				handler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_EXCEPTION_E310, false);
				break;
			case 0x06:
				logger.e("交易异常"+0x06);
				handler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_EXCEPTION_E310, false);
				break;
			case 0x07:
				// 不处理
				break;
			default:
				break;
			}
			try {
				mDev.endPBOC();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void requestAidSelect(int arg0, String[] arg1)
				throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC,请求多应用选择参数1=" + arg0 + ";参数2="
					+ arg1);

		}

		@Override
		public void requestEcashTipsConfirm() throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC,请求电子现金确认");
			mDev.importECashTipConfirmRes(false);
		}

		@Override
		public void requestImportAmount(int arg0) throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC,请求输入金额参数=" + arg0);
			mDev.importAmount(Cache.getInstance().getTransMoney());
		}

		@Override
		public void requestImportPin(int arg0, boolean arg1, String arg2)
				throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC：请求输入PIN arg0=" + arg0 + ";arg1="
					+ arg1 + ";arg2=" + arg2);
			if (3 != arg0) { // 请求输入脱机pin
				try {
					mDev.importPin(Cache.getInstance().getPinBlock());
				} catch (Exception e) {
					logger.e("", e);
					e.printStackTrace();
				}
			} else {
				try {
					mDev.importPin("26888888FFFFFFFF");
				} catch (Exception e) {
					logger.e("联机交易时导入PIN失败", e);
					e.printStackTrace();
				}
			}
		}

		@Override
		public void requestTipsConfirm(String arg0) throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC,请求信息确认参数=" + arg0);
			mDev.importMsgConfirmRes(true);
		}

		@Override
		public void requestUserAuth(int arg0, String arg1)
				throws RemoteException {
			logger.log(MyLog.LogType.INFO, "PBOC：请求输入PIN");
			if (3 != arg0) { // 请求输入脱机pin
				try {
					PbocDev.getInstance(context, deviceService)
							.getOriginalDev()
							.importPin(Cache.getInstance().getPinBlock());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					PbocDev.getInstance(context, deviceService)
							.getOriginalDev().importPin("26888888FFFFFFFF");
				} catch (Exception e) {
					logger.e("联机交易时导入PIN失败", e);
					e.printStackTrace();
				}
			}
		}
	};

	public void getTraceAndArqc() {
		String[] tags = { EMVTAGStr.EMVTAG_APP_PAN, EMVTAGStr.EMVTAG_TRACK2,
				EMVTAGStr.EMVTAG_APP_PAN_SN };
		byte[] track_2_byte = new byte[512];
		int trac_count = 0;
		try {

			trac_count = PbocDev.getInstance(context, deviceService)
					.getOriginalDev().readKernelData(tags, track_2_byte);
		} catch (RemoteException e2) {
			e2.printStackTrace();
			logger.e("读取内核磁道数据失败!", e2);
			return;
		} catch (Exception e2) {
			e2.printStackTrace();
			logger.e("读取内核磁道数据失败!", e2);
			return;
		}
		logger.log(MyLog.LogType.INFO, "再次读取内核获取磁道：读取长度" + trac_count);
		// 非指定账户圈存不获取转入卡磁道数据，其实此处可以去掉，因为在刷卡时已经获取了磁道数据
		if (trac_count > 0
				&& !Cache.getInstance().getTransCode()
						.equals(TransConstans.TRANS_CODE_QC_FZD)) {
			byte[] trackResult = new byte[trac_count];
			System.arraycopy(track_2_byte, 0, trackResult, 0, trac_count);
			String magData = DataConverter.bytesToHexString(trackResult);
			logger.log(MyLog.LogType.INFO, "磁道等数据：" + HexUtil.bcd2str(trackResult));
			String data = HexUtil.bcd2str(trackResult);
			Map<String, String> resMap = TlvUtil.tlvToMap(HexUtil
					.bcd2str(trackResult));
			if (resMap.get("5A") == null) {// 如果5A域未读取到，则从磁道数据中获取
				String track2data = resMap.get("57");
				String card = track2data.split("D")[0];
				resMap.put("5A", card);
			}
			logger.log(MyLog.LogType.INFO, resMap.get("5A").replace("F", "") + " "
					+ resMap.get("57") + " " + resMap.get("5F34"));
			logger.log(MyLog.LogType.INFO,
					"磁道2数据：" + resMap.get("57").replace("F", ""));
			String mSecondTrack = resMap.get("57").replace("F", "");
			mSecondTrack.replaceAll("[:;<>=]", "D");
			Cache.getInstance().setTrack_2_data(mSecondTrack);
			if (!StringUtil.isEmpty(mSecondTrack) && mSecondTrack.contains("D")) {
				int pos = mSecondTrack.indexOf("D");
				String invalidate = mSecondTrack.substring(pos + 1, pos + 5);
				if (StringUtil.isEmpty(invalidate)) {
					invalidate = "0000";
				}
				logger.log(MyLog.LogType.INFO, "IC卡有效期=" + invalidate);
				Cache.getInstance().setInvalidDate(invalidate);
			}
			logger.log(MyLog.LogType.INFO, "卡片序列号：" + resMap.get("5F34"));
			Cache.getInstance().setCardSeqNo(resMap.get("5F34"));
		} else {
			if (Cache.getInstance().getTransCode()
					.equals(TransConstans.TRANS_CODE_QC_FZD)) {
				logger.log(MyLog.LogType.INFO, "非指定账户圈存不读取装入卡的磁道数据");
			}
		}

		AidlPboc mDev = null;
		try {
			mDev = PbocDev.getInstance(context, deviceService).getOriginalDev();
		} catch (Exception e1) {
			logger.e("获取PBOC设备异常，读取ARQC失败");
			e1.printStackTrace();
			return;
		}
		// 读取ARQC值
		String arqc = null;
		try {
			byte[] arqcTemp = new byte[256];
			int num = mDev.readKernelData(new String[] { EMVTAGStr.EMVTAG_AC },
					arqcTemp);
			byte[] arqcData = new byte[num];
			if (num >= 0) {
				System.arraycopy(arqcTemp, 0, arqcData, 0, num);
				arqc = HexUtil.bcd2str(arqcData);
			}
			Cache.getInstance().setPrintArqc(arqc);
		} catch (Exception e) {
			e.printStackTrace();
			logger.e("", e);
		}
		logger.log(MyLog.LogType.INFO, "kernelProc读取 arqc =" + arqc);
	}

	/**
	 * 修改结算表数据（保存需要结算的数据，或者删除反交易数据
	 * 
	 * @param dataobj
	 * @param resobj
	 */
	public void saveSettleRecordToDB(Map<String, String> dataobj,
			Map<String, String> resobj) {
		String code = transCode;
		if (code.equals(TransConstans.TRANS_CODE_CONSUME)
				|| code.equals(TransConstans.TRANS_CODE_PRE_COMPLET)
				|| code.equals(TransConstans.TRANS_CODE_LJTH)
				|| code.equals(TransConstans.TRANS_CODE_DZXJCZ)) {
			SettleData settleData = ParseDataUtils.mapObjToSettleBean(dataobj,
					resobj);
			SettleDataDao dao = new SettleDataDaoImpl();
			dao.save(settleData);
		} else if (code.equals(TransConstans.TRANS_CODE_CONSUME_CX)
				|| code.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)) {
			logger.log(MyLog.LogType.DEBUG, "消费撤销、预授权完成撤销成功后，从结算表中删除对应数据");
			deleteSettleRecordFromDB(dataobj.get("batchbillno"));
		}
	}

	/**
	 * 反交易时，从结算明细表中删除已撤销的交易记录
	 */
	public int deleteSettleRecordFromDB(String batchbillno) {
		// 除预授权完成撤销和消费撤销外，其它交易不需要删除结算信息。
		if (!transCode.equals(TransConstans.TRANS_CODE_CONSUME_CX)
				&& !transCode
						.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)) {
			return 0;
		}
		logger.log(MyLog.LogType.DEBUG, "删除结算表中的记录");
		SettleDataDao settleDataDao = new SettleDataDaoImpl();
		int ret = 0;
		List<SettleData> sDatas = settleDataDao.getSettleData();
		logger.log(MyLog.LogType.DEBUG, "结算表中的记录个数：" + sDatas.size());
		if (sDatas == null || batchbillno.length() < 12) {
			return -1;
		}
		String oldbillno = batchbillno.substring(6, 12);
		logger.log(MyLog.LogType.DEBUG, "batchbillno原交易流水/凭证号：" + oldbillno);
		if (StringUtil.isEmpty(oldbillno)) {
			return -1;
		}
		for (SettleData sData : sDatas) {
			if (StringUtil.isEmpty(sData.batchbillno)
					|| sData.batchbillno.length() < 12) {
				logger.log(MyLog.LogType.DEBUG, "改记录票据号异常：" + sData.batchbillno);
				continue;
			}
			if (oldbillno.equals(sData.batchbillno.subSequence(6, 12))) {
				ret = settleDataDao.delete(sData);
			}
		}
		return ret;
	}

	/**
	 * 删除冲正记录
	 * 
	 * @param batchbillno
	 */
	public int deleteReverseRecord(String batchbillno) {

		ReverseDao mReverseDao = new ReverseDaoImpl();
		List<Reverse> reverList = mReverseDao.getEntities();
		if (batchbillno.length() < 12) {
			logger.log(MyLog.LogType.DEBUG, "删除冲正记录失败，票据号异常");
			return -1;
		}
		String oldbillno = batchbillno.substring(6, 12);
		logger.log(MyLog.LogType.DEBUG, "batchbillno原交易流水/凭证号：" + oldbillno);
		if (StringUtil.isEmpty(oldbillno)) {
			logger.log(MyLog.LogType.DEBUG, "删除冲正记录失败，流水/凭证号异常");
			return -1;
		}
		int ret = -1;
		for (
				Reverse rever : reverList) {
			if (StringUtil.isEmpty(rever.batchbillno)
					|| rever.batchbillno.length() < 12) {
				logger.log(MyLog.LogType.DEBUG, "改记录票据号异常：" + rever.batchbillno);
				continue;
			}
			if (oldbillno.equals(rever.batchbillno.subSequence(6, 12))) {
				ret = mReverseDao.delete(rever);
			}
		}
		return ret;
	}

	/**
	 * 保存或更新交易记录
	 * 
	 * @param transMap
	 */
	public void saveOrUpdateTransRecord(Map<String, String> transMap,
			Map<String, String> resMap) {
		if (transCode.equals(TransConstans.TRANS_CODE_DZXJ_BALANCE_QUERY)
				|| transCode
						.equals(TransConstans.TRANS_CODE_QUERY_BALANCE)) {
			return;
		}
		if (null == resMap) {
			resMap = new HashMap<String, String>();
		}
		// 保存交易报文成功数据
		TransRecord transRecord = ParseDataUtils
				.mapObjToTransBean(transMap, resMap);
		TransRecordDao mTransRecordDao = new TransRecordDaoImpl();
		logger.log(MyLog.LogType.DEBUG, "交易保存记录：" + transRecord.toString());
		TransRecord record = mTransRecordDao
				.getTransRecordByCondition(transRecord.getBatchbillno());
		if (null != record) {
			mTransRecordDao.update(transRecord);
		} else {
			mTransRecordDao.save(transRecord);
		}
	}

	/**
	 * 删除交易记录
	 * 
	 * @param batchBillno
	 */
	private void deleteTransRecord(String batchBillno) {
		TransRecordDao transDao = new TransRecordDaoImpl();
		TransRecord record = transDao.getTransRecordByCondition(batchBillno);
		transDao.delete(record);
	}

	/**
	 * 保存冲正记录
	 */
	public void saveReverseRecord(Map<String, String> transMap) {
		String code = transCode;
		if (code.equals(TransConstans.TRANS_CODE_CONSUME)// 消费
				|| code.equals(TransConstans.TRANS_CODE_CONSUME_CX)// 消费撤销
				|| code.equals(TransConstans.TRANS_CODE_PRE)// 预授权
				|| code.equals(TransConstans.TRANS_CODE_PRE_COMPLET)// 预授权完成
				|| code.equals(TransConstans.TRANS_CODE_PRE_CX)// 预授权撤销
				|| code.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)// 预授权完成撤销
		) {
			ReverseDao mReverseDao = new ReverseDaoImpl();
			Reverse transReverse = ParseDataUtils.mapObjToReverse(transMap);
			mReverseDao.save(transReverse);
		}
	}

	/**
	 * 获取交易组包map
	 * 
	 * @return
	 */
	public Map<String, String> getTransMap() {
		Map<String, String> datamap = null;
		if (transCode.equals(TransConstans.TRANS_CODE_QC_ZD)) {
			datamap = TransactionPackageUtil.getQuanCunZDParam(context,
					deviceService);
		} else if (transCode.equals(TransConstans.TRANS_CODE_QC_FZD)) {
			datamap = TransactionPackageUtil.getQuanCunFZDParam(context,
					deviceService);
		}
		return datamap;
	}

	public String getTransName() {
		String name = "";
		if (transCode.equals(TransConstans.TRANS_CODE_CONSUME)) {
			name = "消费";
		} else if (transCode.equals(TransConstans.TRANS_CODE_QC_ZD)) {
			name = "指定账户圈存";
		} else if (transCode.equals(TransConstans.TRANS_CODE_QC_FZD)) {
			name = "非指定账户圈存";
		}
		return name;
	}

	// 读取用于打印凭条所要求的内核数据
	private void getKernelDataForPrint(Map<String, String> map, String printData) {
		try {
			String arqc = Cache.getInstance().getPrintArqc();
			if (!"".equals(arqc) && arqc != null) {
				logger.log(MyLog.LogType.INFO, "arqc tag替换前：" + arqc);
				arqc = arqc.replaceFirst("9F26", "9F99");
				logger.log(MyLog.LogType.INFO, "arqc tag替换后：" + arqc);
				printData = printData + arqc;
			}
			map.put("reserve4", printData); // 将要打印的数据存放在预留字段4中
			Cache.getInstance().setPrintIcData(printData);
		} catch (Exception e) {
			e.printStackTrace();
			logger.e("", e);
		}
	}

	/**
	 * 判断有没有55域脚本结果数据
	 * 
	 * @return
	 */
	public boolean haveScriptData(String F55Data) {
		try {
			if (StringUtil.isEmpty(F55Data)) {
				return false;
			}
			Map<String, String> icMap = TlvUtil.tlvToMap(F55Data);
			String tag71 = icMap.get("71");
			String tag72 = icMap.get("72");
			if ((null == tag71 || "".equals(tag71))
					&& (null == tag72 || "".equals(tag72))) {
				logger.log(MyLog.LogType.DEBUG, "no script");
				return false;
			}
			return true;
		} catch (Exception e) {
			logger.e("判断脚本出现异常", e);
		}
		return false;
	}

	/**
	 * 判断圈存交易是否脚本执行成功
	 * 
	 * @param TVR
	 * @return
	 */
	private boolean transferScriptSuc(String TVR) {
		String transCode = Cache.getInstance().getTransCode();
		if (transCode.equals("002322") || transCode.equals("002323")
				|| transCode.equals("002321")) {// 指定账户圈存、非指定账户圈存、现金充值
			if (scriptSucess(TVR)) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断终端脚本是否执行成功
	 * 
	 * @param TVR
	 * @return
	 */
	private boolean scriptSucess(String TVR) {
		if (TVR == null || "".equals(TVR))
			return false;
		String byte5 = DataConverter.byteToBinaryString(DataConverter
				.hexStringToByte(TVR.substring(8)));
		if ("0".equals(byte5.substring(3, 4))) {
			logger.log(MyLog.LogType.DEBUG, "脚本tag 72 执行成功");
			return true;
		} else { // tag72 脚本执行失败
			logger.log(MyLog.LogType.DEBUG, "脚本tag 72 执行失败");
			return false;
		}
	}

	/**
	 * 判断ARPC是否执行成功（发卡行认证）
	 * 
	 * @param TVR
	 * @return
	 */
	private boolean arpcSucess(String TVR) {
		if (TVR == null || "".equals(TVR))
			return false;
		String byte5 = DataConverter.byteToBinaryString(DataConverter
				.hexStringToByte(TVR.substring(8)));
		if ("0".equals(byte5.substring(1, 2))) {
			logger.log(MyLog.LogType.DEBUG, "ARPC认证成功");
			return true;
		} else { // ARPC执行失败,需上送ARPC
			logger.log(MyLog.LogType.DEBUG, "ARPC认证失败");
			return false;
		}
	}

    @Override
    public void cancel() {
        if(this.handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
    }
	
	

}
