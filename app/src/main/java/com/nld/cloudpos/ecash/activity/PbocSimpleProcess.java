package com.nld.cloudpos.ecash.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.iso8583.util.DataConverter;
import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.emv.AidlCheckCardListener;
import com.nld.cloudpos.aidl.emv.CardInfo;
import com.nld.cloudpos.aidl.emv.PCardLoadLog;
import com.nld.cloudpos.aidl.emv.PCardTransLog;
import com.nld.cloudpos.aidl.magcard.TrackData;
import com.nld.cloudpos.aidl.pinpad.AidlPinpad;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.activity.TransErrorResultActivity;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.cloudpos.payment.base.PbocListener.PbocProcessListener;
import com.nld.cloudpos.payment.interfaces.IDialogItemClickListener;
import com.nld.cloudpos.payment.view.MEditText;
import com.nld.cloudpos.util.DialogFactory;
import com.nld.cloudpos.util.MyLog;
import com.nld.cloudpos.util.TlvUtil;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.bean.SettleData;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.db.local.SettleDataDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.pinUtils.EMVTAGStr;
import com.nld.starpos.banktrade.pinUtils.PbocDev;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.CommonUtil;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransParamsUtil;

import java.util.HashMap;
import java.util.Map;

import common.HexUtil;
import common.StringUtil;
import common.Utility;

/**
 * @description 非接快速支付
 * @date 2015-10-30 20:21:44
 * @author Xrh
 */
public abstract class PbocSimpleProcess extends BaseAbstractActivity {

	private final MyLog logger = MyLog.getLogger(this.getClass());

	private static final int MSG_TIP = 101;// 显示提示
	private static final int MSG_ERROR = 102;// 错误
	private static final int PBOC_REPEAT = 104;// 重新开始
	private static final int PBOC_GET_PIN = 105;// 获取密码
	private static final int PBOC_ELECT_CASH = 106;// 是否支持电子现金
	private static final int PBOC_START_READ = 107;// 开始加载IC卡数据
	private static final int PBOC_MULT_AID = 109;// 多应用选择
	private static final int TIPS_CONFIRM = 108;// 提示确认

	/**
	 * 该变量在电子现金非指定账户圈存中使用， 用来判断是否为转入卡
	 */
	protected boolean isInCard = false;

	protected LinearLayout mInputLl;
	protected RelativeLayout mCarnoLl, mMoneyLl;
	protected TextView mInputTip, mMoneyTv, mCarnoTv, mCarnoTipTV;
	protected MEditText mCarnoEt;
	protected Button mNextBtn;
	protected ParamConfigDao mParamConfigDao;
	protected TransRecordDao mTransRecordDao;
	protected SettleDataDaoImpl settleDataDao;
	protected ImageView iv_bottom;

	private String batchno; // 批次号
	private String billno; // 凭证号
	private String systraceno; // POS流水号

	private String mCarno;
	private String mFirstTrack;
	private String mSecondTrack;
	private String mThirdTrack;
	private String mInvalidate;
	private String mMoney;
	private int transType = 0;
	private String[] multAids = null;
	private AidlPinpad mDev;

	@Override
	public int contentViewSourceID() {
		return R.layout.consume_submit;
	}

	@Override
	public void initView() {
		setTopDefaultReturn();
		mParamConfigDao = new ParamConfigDaoImpl();
		settleDataDao = new SettleDataDaoImpl();
		mTransRecordDao = new TransRecordDaoImpl();
		mCarnoLl = (RelativeLayout) findViewById(R.id.swipe_carno_tip_ll);
		mInputLl = (LinearLayout) findViewById(R.id.swipe_carno_input_ll);
		mMoneyLl = (RelativeLayout) findViewById(R.id.swipe_carno_show_money_ll);
		mMoneyTv = (TextView) findViewById(R.id.swipe_carno_input_money);
		mInputTip = (TextView) findViewById(R.id.swipe_carno_input_tip);
		mCarnoEt = (MEditText) findViewById(R.id.et_cardno);
		mNextBtn = (Button) findViewById(R.id.swipe_next_btn);
		mCarnoTv = (TextView) findViewById(R.id.tv_tip_cardno);
		mCarnoTipTV = (TextView) findViewById(R.id.swipe_carno_tip_tip);
		iv_bottom = (ImageView) findViewById(R.id.iv_bottom);
		mMoneyTv.setText(Cache.getInstance().getTransMoney());

		 showTransMoney(true);
		 mCarnoLl.setVisibility(View.VISIBLE);
		initViewData();
	}

	/**
	 * 页面显示交易金额
	 */
	public void showTransMoney(boolean isShow) {
		if (isShow) {
			mMoneyLl.setVisibility(View.VISIBLE);
		} else {
			mMoneyLl.setVisibility(View.GONE);
		}
	}

	@Override
	public void onServiceConnecteSuccess(AidlDeviceService service) {
		logger.log(MyLog.LogType.DEBUG, "绑定成功，开启检卡");
		try {
			PbocDev.getInstance(this, mDeviceService).abortPboc();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		startCheckCard();
		String type= ParamsUtil.getInstance().getParam("pinpadType");
		try {
            mDev= AidlPinpad.Stub.asInterface(service.getPinPad("0".equals(type)?0:1));
        } catch (RemoteException e) {
            logger.e("密码键盘获取失败");
            e.printStackTrace();
        }
	}

	@Override
	public void onServiceBindFaild() {
		logger.log(MyLog.LogType.WARN, "服务绑定失败！");
	}

	@Override
	public boolean saveValue() {

		return true;
	}

	@Override
	protected void onDestroy() {
		BankApplication.mPbocListener.clearListener();
		DialogFactory.dismissAlert(mActivity);
		try {// 结束PBOC流程
			PbocDev.getInstance(mContext, mDeviceService).getOriginalDev().cancelCheckCard();

		} catch (RemoteException e) {
			logger.e("停止PBOC异常", e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.e("停止PBOC异常", e);
			e.printStackTrace();
		}
		abordPboc();
		super.onDestroy();
	}

	/**
	 * @description 开启检卡
	 */
	private void startCheckCard() {
		try {
			boolean supportIC = true;
			boolean supportRF = true;

			String transcode = Cache.getInstance().getTransCode();
			if (TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY
					.equals(transcode)) { // 电子现金快速支付
				supportIC = false;
			} else if (TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER
					.equals(transcode)) {// 电子现金普通消费
				supportRF = false;
			} else if (TransConstans.TRANS_CODE_QC_ZD.equals(transcode)) {// 指定账户圈存
				supportRF = false;
			} else if (TransConstans.TRANS_CODE_QC_FZD.equals(transcode)) {// 非指定账户圈存
				supportRF = false;
			}

			/** 1-磁条、2-接触式、3-非接 */
			PbocDev.getInstance(this, mDeviceService).checkCard(false,
					supportIC, supportRF, 60 * 1000, CheckCardListener);
		} catch (Exception e) {
			e.printStackTrace();
			logger.e("开启检卡异常 ... ", e);
		}
	}

	/**
	 * @description PBOC处理
	 */
	private Handler pbocHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(isCanceled){
				logger.d("页面已取消，不执行");
				return;
			}
			switch (msg.what) {
			case MSG_TIP:
				String tip = (String) msg.obj;
				showTip(tip);
				break;
			case PBOC_REPEAT:
				DialogFactory.dismissAlert(mActivity);
				playSound(2, 0);
				DialogFactory.showConfirmMessage(mActivity, "提示", "读卡失败，请重新插卡", "确定",new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                    	startCheckCard();
                    }
                });
				break;
			case PBOC_START_READ:
				DialogFactory.showLoadingDialog(mActivity, "检测到IC卡", "正在读取IC卡数据...");
				break;
			case PBOC_GET_PIN:// 显示获取脱机密码对话框
				showGetPinDialog();
				break;
			case PBOC_ELECT_CASH:// 显示是否支持电子现金提示框
				showECashDialog();
				break;
			case PBOC_MULT_AID:
				String title = 2 > multAids.length ? "单应用选择" : "多应用选择";
				DialogFactory.dismissAlert(mActivity);
				DialogFactory.showChooseListDialog(mActivity, title, null,
						null, multAids, new IDialogItemClickListener() {
							@Override
							public void onDialogItemClick(View v, String title,
                                                          int pos) {
								DialogFactory.dismissAlert(mActivity);
								pbocHandler.sendEmptyMessage(PBOC_START_READ);
								try {
									PbocDev.getInstance(
											getApplicationContext(),
											mDeviceService).getOriginalDev()
											.importAidSelectRes(pos + 1);
								} catch (Exception e) {
									logger.e("多应用选择结果导入失败", e);
									e.printStackTrace();
								}
							}
						});
				break;
			case TIPS_CONFIRM:
				DialogFactory.dismissAlert(mActivity);
				String msgText = (String) msg.obj;
				DialogFactory.showMessage(mActivity, "信息确认", msgText, "确定",
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								DialogFactory.dismissAlert(mActivity);
								pbocHandler.sendEmptyMessage(PBOC_START_READ);
								try {
									PbocDev.getInstance(mContext,
											mDeviceService).getOriginalDev()
											.importMsgConfirmRes(true);
								} catch (Exception e) {
									e.printStackTrace();
									logger.e("", e);
								}
							}
						}, "取消", new OnClickListener() {
							@Override
							public void onClick(View v) {
								DialogFactory.dismissAlert(mActivity);
								pbocHandler.sendEmptyMessage(PBOC_START_READ);
								try {
									PbocDev.getInstance(mContext,
											mDeviceService).getOriginalDev()
											.importMsgConfirmRes(false);
								} catch (Exception e) {
									e.printStackTrace();
									logger.e("", e);
								}
							}
						});
			default:
				break;
			}

		}
	};

	/**
	 * 显示输入脱机PIN提示对话框
	 */
	private void showGetPinDialog() {
		DialogFactory.showOfflinePinDialog(mActivity,mDev, "输入脱机密码", "请输入脱机密码 ", "取消",
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						abordPboc();
					}
				});
	}

	/**
	 * 显示是否支持电子交易对话框
	 */
	public void showECashDialog() {
		DialogFactory.showMessage(mActivity, "电子现金", "是否使用电子现金交易", "使用",
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							PbocDev.getInstance(mContext, mDeviceService)
									.getOriginalDev()
									.importECashTipConfirmRes(true);
						} catch (RemoteException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, "不使用", new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							PbocDev.getInstance(mContext, mDeviceService)
									.getOriginalDev()
									.importECashTipConfirmRes(false);
						} catch (RemoteException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}

	/**
	 * 中断PBOC
	 */
	public void abordPboc() {
		DialogFactory.dismissAlert(mActivity);
		try {
			logger.log(MyLog.LogType.DEBUG, "中断PBOC交易");
			PbocDev.getInstance(mContext, mDeviceService).getOriginalDev()
					.abortPBOC();
		} catch (RemoteException e) {
			logger.e("中断PBOC流程失败");
			e.printStackTrace();
		} catch (Exception e) {
			logger.e("中断PBOC流程失败");
			e.printStackTrace();
		}
	}

	/**
	 * 结束PBOC流程
	 */
	public void endPboc() {
		DialogFactory.dismissAlert(mActivity);
		try {// 结束PBOC流程
			PbocDev.getInstance(mContext, mDeviceService).getOriginalDev().endPBOC();
		} catch (RemoteException e) {
			logger.e("停止PBOC异常", e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.e("停止PBOC异常", e);
			e.printStackTrace();
		}
		NldPaymentActivityManager.getActivityManager().removeActivity(this);
	}

	/**
	 * @description PBOC检卡回调
	 */
	public AidlCheckCardListener CheckCardListener = new AidlCheckCardListener.Stub() {

		@Override
		public void onCanceled() throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "取消检卡");
		}

		@Override
		public void onError(int arg0) throws RemoteException {
			logger.log(MyLog.LogType.WARN, "检卡错误  " + arg0);
			pbocHandler.sendEmptyMessage(PBOC_REPEAT);
		}

		@Override
		public void onFindICCard() throws RemoteException {
			logger.log(MyLog.LogType.INFO, "检测到接触式IC卡！");
			pbocHandler.sendEmptyMessage(PBOC_START_READ);
			if (!isInCard) {
				Cache.getInstance().setSerInputCode("051");
			}
			try {
				String transcode = Cache.getInstance().getTransCode();
				BankApplication.mPbocListener.setPbocProcessListener(pbocStartListener);
				PbocDev.getInstance(mContext, mDeviceService)
						.getOriginalDev()
						.processPBOC(
								PbocDev.getPbocEmvTransData(Cache.getInstance()
										.getTransCode()), BankApplication.mPbocListener);
			} catch (Exception e) {
				logger.e("启动PBOC流程失败，PBOC对象获取异常", e);
				e.printStackTrace();
			}
		}

		@Override
		public void onFindMagCard(TrackData arg0) throws RemoteException {
			logger.log(MyLog.LogType.INFO, "检测到磁条卡！");
		}

		@Override
		public void onFindRFCard() throws RemoteException {
			logger.log(MyLog.LogType.INFO, "检测到非接触式IC卡！");
			pbocHandler.sendEmptyMessage(PBOC_START_READ);
			Cache.getInstance().setSerInputCode("071");
			try {
				String transcode = Cache.getInstance().getTransCode();
				BankApplication.mPbocListener.setPbocProcessListener(pbocStartListener);
				PbocDev.getInstance(mContext, mDeviceService)
						.getOriginalDev()
						.processPBOC(
								PbocDev.getRFPbocEmvTransData(Cache
										.getInstance().getTransCode()),
								BankApplication.mPbocListener);
			} catch (Exception e) {
				logger.e("启动PBOC流程失败，PBOC对象获取异常", e);
				e.printStackTrace();
			}
		}

		@Override
		public void onSwipeCardFail() throws RemoteException {
			logger.log(MyLog.LogType.WARN, "刷卡失败");
			// 刷卡失败，重新打开刷卡器
			try {
				BankApplication.mPbocListener.setPbocProcessListener(pbocStartListener);
				PbocDev.getInstance(mContext, mDeviceService).startRfProc(
						(byte) transType, (byte) 0x01, BankApplication.mPbocListener);
			} catch (Exception e) {
				logger.e("启动PBOC流程失败，PBOC对象获取异常", e);
				e.printStackTrace();
			}
		}

		@Override
		public void onTimeout() throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "PBOC检卡超时");
			Cache.getInstance().setErrCode(
					NldException.ERR_DEV_READ_CARD_TIMEOUT_E326);
			Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_READ_CARD_TIMEOUT_E326));
			goToNextActivity(TransErrorResultActivity.class);
			finish();
		}
	};

	/**
	 * @description 开启PBOC流程
	 */
	public PbocProcessListener pbocStartListener = new PbocProcessListener() {

		@Override
		public void onConfirmCardInfo(CardInfo arg0) throws RemoteException {
			mCarno = arg0.getCardno();
			logger.log(MyLog.LogType.DEBUG, "请求确认卡信息" + mCarno);
			try {
				PbocDev.getInstance(mContext, mDeviceService).getOriginalDev()
						.importConfirmCardInfoRes(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (isInCard) {   // 当此处插卡的是转入卡,直接开始交易，因为转出卡仅需要55域数据，数据在交易时获取
				logger.log(MyLog.LogType.DEBUG, "PBOC非指定账户圈存，转入卡：" + mCarno);
				mInvalidate = Cache.getInstance().getInvalidDate();
				mFirstTrack = null;
				mSecondTrack = Cache.getInstance().getTrack_2_data();
				mThirdTrack = null;
				Cache.getInstance().setAdddataword(mCarno);// 附加数据为转入卡号
				Intent it = getNextStep();
				if (null == it) {
					logger.log(MyLog.LogType.DEBUG,"下一步界面为空");
					return;
				}
				goToNextActivity(it);
				finish();
				return;
			}
			Cache.getInstance().setCardNo(mCarno);
		}

		@Override
		public void onError(int arg0) throws RemoteException {
			logger.log(MyLog.LogType.WARN, "EMV交易失败：" + arg0);
			Cache.getInstance().setErrCode(NldException.ERR_DEV_READ_CARD_E301);
            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_READ_CARD_E301));
            goToNextActivity(TransErrorResultActivity.class);
            finish();
		}

		@Override
		public void onReadCardLoadLog(String arg0, String arg1,
                                      PCardLoadLog[] arg2) throws RemoteException {
			// 返回读取卡片圈存日志结果
			logger.log(MyLog.LogType.DEBUG, "返回读取卡片圈存日志结果");

		}

		@Override
		public void onReadCardOffLineBalance(String arg0, String arg1,
                                             String arg2, String arg3) throws RemoteException {
			// 返回读取卡片多级余额结果/**返回读取卡片脱机余额结果*/
			logger.d("返回读取卡片多级余额结果");
			logger.d("第一电子现金货币代码" + arg0);
			logger.d("第一电子现金余额" + arg1);
			logger.d("第二电子现金货币代码" + arg2);
			logger.d("第二电子现金余额" + arg3);
		}

		@Override
		public void onReadCardTransLog(PCardTransLog[] arg0)
				throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "返回读取卡片交易日志结果" + arg0.toString());
			Cache.getInstance().setTransLog(arg0);
			goToNextActivity(getNextStep());
			finish();
		}

		@Override
		public void onRequestOnline() throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "请求联机");
			//电子现金余额不足或超出卡片非接限额
			Cache.getInstance().setErrCode(NldException.ERR_DEV_RF_ONLINE__E325);
			Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_RF_ONLINE__E325));
			goToNextActivity(TransErrorResultActivity.class);
			finish();
//			if (Cache.getInstance().getTransCode()
//					.equals(TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY)) {
//				getTrans2Data();
//				// Cache.getInstance().setSerInputCode("071");
//				Cache.getInstance().setSerInputCode("981");
//				goToNextActivity(getNextOnline());
//				AidlRFCard rfDev = AidlRFCard.Stub.asInterface(mDeviceService
//						.getRFIDReader());
//				rfDev.reset(1);
//				abordPboc();
//			} else if (Cache
//					.getInstance()
//					.getTransCode()
//					.equals(TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER)) {
//				getTrans2Data();
//				Cache.getInstance().setSerInputCode("051");
//				goToNextActivity(getNextOnline());
//				abordPboc();
//			} else {
//				goToNextActivity(getNextOnline());
//			}
		}

		@Override
		public void onTransResult(int arg0) throws RemoteException {
			// 批准: 0x01 拒绝: 0x02 终止: 0x03
			// FALLBACK: 0x04 采用其他界面: 0x05 其他： 0x06
			logger.log(MyLog.LogType.DEBUG, "交易结果：" + arg0);
			switch (arg0) {
			case 0x01:
//				showResultTip("交易成功");
				if (Cache.getInstance().getTransCode() == TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY) {
					// 快速支付，跳转到打印凭条
					Intent intent = getNextStep();
					intent.putExtra("cardno", mCarno);
					intent.putExtra("paymoney", Cache.getInstance()
							.getTransMoney());
					getTrans2Data();
					getICData();
					saveDataToDB();
					goToNextActivity(intent);
				} else if (Cache
						.getInstance()
						.getTransCode()
						.equals(TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER)) {
					Intent intent = getNextStep();
					intent.putExtra("cardno", mCarno);
					intent.putExtra("paymoney", Cache.getInstance()
							.getTransMoney());
					getTrans2Data();
					getICData();
					saveDataToDB();
					goToNextActivity(intent);
				}
				endPboc();
				break;
			case 0x02:
				//交易拒绝
				Cache.getInstance().setErrCode(NldException.ERR_DEV_TRANS_REFUSE_E307);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_TRANS_REFUSE_E307));
                goToNextActivity(TransErrorResultActivity.class);
                finish();
				break;
			case 0x03:
				//读卡失败，交易终止
				Cache.getInstance().setErrCode(NldException.ERR_DEV_READ_CARD_E302);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_READ_CARD_E302));
                goToNextActivity(TransErrorResultActivity.class);
                finish();
				break;
			case 0x04:
//				showResultTip("读卡失败，请重试");
				pbocHandler.sendEmptyMessage(PBOC_REPEAT);
				break;
			case 0x05:
				//请采用其他界面进行交易
				Cache.getInstance().setErrCode(NldException.ERR_DEV_TRANS_OTHERPAGE_E324);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_TRANS_OTHERPAGE_E324));
                goToNextActivity(TransErrorResultActivity.class);
                finish();
				break;
			case 0x07:// 动联机具中断PBOC流程后会返回0x07错误码
				break;
			default:
				logger.log(MyLog.LogType.ERROR, "IC卡卡号读取异常：" + arg0);
				endPboc();
				pbocHandler.sendEmptyMessage(PBOC_REPEAT);
				break;
			}
		}

		@Override
		public void requestAidSelect(int arg0, String[] arg1)
				throws RemoteException {
			logger.log(MyLog.LogType.DEBUG, "请求多应用选择");
			multAids = arg1;
			pbocHandler.sendEmptyMessage(PBOC_MULT_AID);
		}

		@Override
		public void requestEcashTipsConfirm() throws RemoteException {
			// /**请求确认是否使用电子现金*/
			logger.log(MyLog.LogType.DEBUG, "请求确认是否使用电子现金");
			try {
				String mchntname = ParamsUtil.getInstance().getParam("mchntname"); // 商户名称
				String merchant =
						CommonUtil
						.makeParamToEMV(PbocSimpleProcess.this);
				String tag = "9F4E";
				byte[] mchntname_hex = mchntname.getBytes("GBK");
				PbocDev.getInstance(mContext, mDeviceService).getOriginalDev()
						.setTlv(tag, mchntname_hex);
				PbocDev.getInstance(mContext, mDeviceService).getOriginalDev()
						.importECashTipConfirmRes(true);
			} catch (Exception e) {
				logger.e("确认使用电子现金失败", e);
				e.printStackTrace();
			}
		}

		@Override
		public void requestImportAmount(int arg0) throws RemoteException {
			/** 请求输入金额 */
			logger.log(MyLog.LogType.DEBUG, "请求输入金额:" + arg0);
			/*
			 * 金额类别（ 1byte），取值说明： 0x01：只要授权金额； 0x02：只要返现金额； 0x03：既要授权金额，也要返现金额；
			 */
			switch (arg0) {
			case 0x01:
				logger.log(MyLog.LogType.DEBUG, "导入金额："
						+ Cache.getInstance().getTransMoney());
				if (!StringUtil.isEmpty(Cache.getInstance().getTransMoney())) {
					// 如果缓存中不存在金额，说明需要输入金额，所以进入下一页（此处默认下一页会是输入金额）
					// 如果缓存中存在金额，则说明在刷卡前已输入金额，则直接传入金额
					String money = Cache.getInstance().getTransMoney();
					try {
						logger.log(MyLog.LogType.DEBUG, "导入金额：" + money);
						PbocDev.getInstance(mContext, mDeviceService)
								.importMoney(money);
					} catch (Exception e) {
						logger.e("金额导入失败，获取PBOC实例异常", e);
						e.printStackTrace();
					}
				} else {
					try {
						PbocDev.getInstance(mContext, mDeviceService)
								.importMoney("0.00");
					} catch (Exception e) {
						logger.e("金额导入失败，获取PBOC实例异常", e);
						e.printStackTrace();
					}
				}
				break;
			case 0x02:
				break;
			case 0x03:
				break;
			}
		}

		@Override
		public void requestImportPin(int arg0, boolean arg1, String arg2)
				throws RemoteException {
			// /** 请求导入 PIN */
			logger.log(MyLog.LogType.DEBUG, "请求导入 PIN arg0" + arg0 + ";arg1:" + arg1
					+ "arg2:" + arg2);
			if (3 != arg0) { // 请求输入脱机pin
				pbocHandler.sendEmptyMessage(PBOC_GET_PIN);
			} else {
				try {
					PbocDev.getInstance(mContext, mDeviceService)
							.getOriginalDev().importPin("26888888FFFFFFFF");
				} catch (Exception e) {
					pbocHandler.sendEmptyMessage(PBOC_REPEAT);
					logger.e("联机交易时导入PIN失败");
					e.printStackTrace();
				}
			}
		}

		@Override
		public void requestTipsConfirm(String arg0) throws RemoteException {
			/**
			 * 请求提示信息， 提示信息格式为 16 进制字符串， 格式为 显示标志+显示超时时间+显示标题长度+显
			 * 示标题内容+显示内容长度+显示内容； 显示标志： 1byte，表示是否需要持卡人确 认； 0x00：不需要确认；
			 * 0x01：需要确认； 显示超时时间： 1byte，单位 s； 显示标题长度： 1byte，若为0，标题内容不 存在； 标题内容：
			 * ASC 码，若“显示标题长度”为 0，则该字段不存在； 显示内容长度： 1byte，若为0，若“显示内
			 * 容长度”为0，则该字段不存在； 显示内容： ASC 码
			 */
			logger.log(MyLog.LogType.DEBUG, "请求提示信息" + arg0);
			Message msg = new Message();
			msg.what = TIPS_CONFIRM;
			msg.obj = arg0;
			pbocHandler.sendMessage(msg);
		}

		@Override
		public void requestUserAuth(int arg0, String arg1)
				throws RemoteException {
			// /** 请求身份认证 */
			logger.log(MyLog.LogType.DEBUG, "请求身份认证 arg0=" + arg0 + ";arg1=" + arg1);
			try {
				PbocDev.getInstance(mContext, mDeviceService).getOriginalDev().importUserAuthRes(true);
			} catch (Exception e) {
				logger.e("请求身份认证失败");
				e.printStackTrace();
			}
		}
	};

	private void showResultTip(String tip) {
		Message msg = new Message();
		msg.what = MSG_TIP;
		msg.obj = tip;
		pbocHandler.sendMessage(msg);
	}

	/**
	 * 将脱机消费数据保存到数据库
	 * 
	 */
	public void saveDataToDB() {
		batchno = TransParamsUtil.getCurrentBatchNo(); // 批次号
		billno = TransParamsUtil.getBillNo(); // 凭证号
		Cache.getInstance().setSerialNo(billno);
		Cache.getInstance().setBatchNo(batchno);
		systraceno = billno; // POS流水号

		TransRecord transRecord = MapObjToTransBean(); // 交易流水表记录
		mTransRecordDao.save(transRecord);
		SettleData settleData = MapObjToSettleBean(); // 结算表记录
		settleDataDao.save(settleData);
	}

	/**
	 * 将Map数据转化为交易对象 modify by chenkehui for 8583jar-ic
	 * 
	 * @return
	 */
	public TransRecord MapObjToTransBean() {
		Map<String, String> dataMap = new HashMap<String, String>();
		TransRecord record = null;
		try {
			record = new TransRecord();
			record.setPriaccount(Cache.getInstance().getCardNo()); // 主账号 F2
			dataMap.put("priaccount", Cache.getInstance().getCardNo());
			record.setTransprocode("000000"); // 交易处理码 F3
			dataMap.put("transprocode", "000000");
			record.setTransamount(Utility.formatMount(Cache.getInstance()
					.getTransMoney())); // 交易金额 F4
			dataMap.put("transamount", Cache.getInstance().getTransMoney());
			record.setSystraceno(String.valueOf(systraceno)); // pos流水号(11域)
			dataMap.put("systraceno", "000000");
			String date = Utility.getTransLocalDate();
			record.setTranslocaldate(date);
			dataMap.put("translocaldate", date);
			String time = Utility.getTransLocalTime();
			record.setTranslocaltime(time);
			dataMap.put("translocaltime", time);
			record.setExpireddate(Cache.getInstance().getInvalidDate()); // 卡有效期（14域）
			dataMap.put("expireddate", Cache.getInstance().getInvalidDate());
			Cache.getInstance().setSerInputCode("072");
			record.setEntrymode("072"); // POS输入方式(22域)
			dataMap.put("entrymode", "072");
			record.setSeqnumber(Cache.getInstance().getCardSeqNo()); // F23
			dataMap.put("seqnumber", Cache.getInstance().getCardSeqNo());
			record.setConditionmode("00"); // F25
			dataMap.put("conditionmode", "00");
			String termId = mParamConfigDao.get("unionpay_termid");
			record.setTerminalid(termId); // F41
			dataMap.put("terminalid", termId);
			String merId = mParamConfigDao.get("unionpay_merid");
			record.setAcceptoridcode(merId); // F42
			dataMap.put("acceptoridcode", merId);
			record.setAdddataword("300"); // F48
			dataMap.put("adddataword", "");
			record.setTranscurrcode("156"); // F49
			dataMap.put("transcurrcode", "156");
			record.setIcdata(Cache.getInstance().getTlvTag55()); // F55
			dataMap.put("icdata", Cache.getInstance().getTlvTag55());
			String loadPa = mParamConfigDao.get("operatorcode")
					+ mParamConfigDao.get("operatorpwd") + "50";
			record.setLoadparams(loadPa); // F60
			dataMap.put("loadparams", loadPa);
			record.setBatchbillno(batchno + billno); // F62
			dataMap.put("batchbillno", batchno + billno);
			String printData = getKernelDataForPrint();
			record.setReserve4(printData);
			dataMap.put("reserve4", printData);
			record.setStatuscode("OF"); // offline脱机标志
			record.setReserve1("0330"); // 保留字段1，保存交易下发的消息类型
			Cache.getInstance().setResultMap(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			logger.e("", e);
		}
		return record;
	}

	// 将Map数据转化为结算明细表记录对象
	public SettleData MapObjToSettleBean() {
		SettleData record = new SettleData();
		record.priaccount = Cache.getInstance().getCardNo();// 卡号
		record.batchbillno = batchno + billno;
		record.transamount = Utility.formatMount(Cache.getInstance()
				.getTransMoney());// 金额
		record.conditionmode = "00";// 服务条件码
		record.transprocode = "000000";// 交易处理码
		record.reserve1 = "0330"; // 保留字段1，保存交易下发的消息类型
		return record;
	}

	// 读取用于打印凭条所要求的内核数据
	private String getKernelDataForPrint() {
		String printData = null;
		try {
			byte[] temp3 = new byte[1024];
			int count3 = PbocDev.getInstance(mContext, mDeviceService)
					.getOriginalDev()
					.readKernelData(EMVTAGStr.getkernelDataForPrint(), temp3);
			byte[] data3 = new byte[count3];
			System.arraycopy(temp3, 0, data3, 0, count3);
			printData = HexUtil.bcd2str(data3);
			Log.i("ckh", "reserve4 == " + printData);
		} catch (Exception e) {
			e.printStackTrace();
			logger.e("", e);
		}
		return printData;
	}

	public void getICData() {
		byte[] resultTemp = new byte[1024];
		try {
			int count = PbocDev
					.getInstance(mContext, mDeviceService)
					.getOriginalDev()
					.readKernelData(EMVTAGStr.getLakalaF55UseModeOne(),
							resultTemp);
			if (count <= 0) {
				return;
			}
			byte[] icData = new byte[count];
			System.arraycopy(resultTemp, 0, icData, 0, count);
			logger.log(MyLog.LogType.INFO, "55域数据：" + HexUtil.bcd2Str(icData));
			Cache.getInstance().setTlvTag55(HexUtil.bcd2str(icData));
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getTrans2Data() {
		try {
			String[] tags = { EMVTAGStr.EMVTAG_APP_PAN,
					EMVTAGStr.EMVTAG_TRACK2, EMVTAGStr.EMVTAG_APP_PAN_SN };
			byte[] track_2_byte = new byte[512];
			int trac_count;
			trac_count = PbocDev
					.getInstance(getApplicationContext(), mDeviceService)
					.getOriginalDev().readKernelData(tags, track_2_byte);
			logger.log(MyLog.LogType.INFO, "PBOC再次读取内核获取磁道：读取长度" + trac_count);
			if (trac_count > 0) {
				byte[] trackResult = new byte[trac_count];
				System.arraycopy(track_2_byte, 0, trackResult, 0, trac_count);
				logger.log(MyLog.LogType.INFO,
						"磁道等数据：" + DataConverter.bytesToHexString(trackResult));
				Map<String, String> resMap = TlvUtil.tlvToMap(HexUtil
						.bcd2str(trackResult));
				if (resMap.get("5A") == null) {// 如果5A域未读取到，则从磁道数据中获取
					String track2data = resMap.get("57");
					String card = track2data.split("D")[0];
					resMap.put("5A", card);
				}
				logger.log(MyLog.LogType.INFO, resMap.get("5A").replace("F", "")
						+ " " + resMap.get("57") + " " + resMap.get("5F34"));
				logger.log(MyLog.LogType.INFO,
						"磁道2数据：" + resMap.get("57").replace("F", ""));
				String mSecondTrack = resMap.get("57").replace("F", "");
				mSecondTrack.replaceAll("[:;<>=]", "D");
				String invalidate = null;
				if (!StringUtil.isEmpty(mSecondTrack)
						&& mSecondTrack.contains("D")) {
					int pos = mSecondTrack.indexOf("D");
					invalidate = mSecondTrack.substring(pos + 1, pos + 5);
					if (StringUtil.isEmpty(invalidate)) {
						invalidate = "0000";
					}
				}
				mInvalidate = invalidate;
				mFirstTrack = null;
				this.mSecondTrack = mSecondTrack;
				mThirdTrack = null;
				logger.log(MyLog.LogType.INFO, "卡片序列号：" + resMap.get("5F34"));
				Cache.getInstance().setCardSeqNo(resMap.get("5F34"));
				logger.log(MyLog.LogType.INFO, "IC卡有效期=" + invalidate);
				Cache.getInstance().setInvalidDate(invalidate);
				mCarno = resMap.get("5A").replace("F", "");
				logger.log(MyLog.LogType.INFO, "IC卡卡号" + mCarno);
				Cache.getInstance().setCardNo(mCarno);
				logger.log(MyLog.LogType.INFO, "IC卡磁道2数据" + mSecondTrack);
				Cache.getInstance().setTrack_2_data(mSecondTrack);
			}
		} catch (RemoteException e) {
			logger.e("读取IC卡数据失败", e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.e("读取IC卡数据失败", e);
			e.printStackTrace();
		}
	}

	/**
	 * @description 初始化页面
	 */
	public abstract void initViewData();

	/**
	 * @description 获取下一个页面
	 */
	public abstract Intent getNextStep();

	/**
	 * @description 请求联机
	 */
	public abstract Intent getNextOnline();

}
