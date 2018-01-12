/**
 * 
 */
package com.nld.cloudpos.payment.activity.reprint;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.printer.AidlPrinter;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.activity.BaseActivity;
import com.nld.cloudpos.payment.dev.PrintDev;
import com.nld.cloudpos.payment.interfaces.IDialogTimeoutListener;
import com.nld.cloudpos.util.DialogFactory;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.ParamsUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结算
 * 
 * @author lin 2015年10月20日
 */
public class RePrintSettleMainActivity extends BaseActivity implements
        OnClickListener {

	public final static int PRINT_STATE_NORMAL = 0x00;// 正常
	public final static int PRINT_STATE_NO_PAPER = 0x01;// 缺纸
	public final static int PRINT_STATE_HOT = 0x02;// 高温
	public final static int PRINT_STATE_UNKNOW = 0x03;// 未知

	private RelativeLayout yhkjs;
	private RelativeLayout smjs;
	private TransRecordDao transRecordDao;
	private ParamConfigDao paramConfigDao;
	private Map<String, String> settleDataMap;

	private List<PrintItemObj> printList;

	private AidlPrinter printer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reprint_settle_main);
		initView();
	}

	private void initView() {
	    setTopDefaultReturn();
	    setTopTitle();
		yhkjs = (RelativeLayout) findViewById(R.id.ll_reprint_yhkjs);
		smjs = (RelativeLayout) findViewById(R.id.ll_reprint_smjs);
		yhkjs.setOnClickListener(this);
		smjs.setOnClickListener(this);
		initData();
	}

	private void initData() {
		transRecordDao = new TransRecordDaoImpl();
		paramConfigDao = new ParamConfigDaoImpl();
	}

	@Override
	public void onClick(View v) {
	    if(!isCanClick(1000)){
	        return;
	    }
		String signTag = ParamsUtil.getInstance()
				.getParam("signsymbol");
		String wxSignTag = ParamsUtil.getInstance().getParam(
				"wxsignsymbol");

		String settlebatchno = paramConfigDao.get("settlebatchno");
		String wxsettlebatchno = paramConfigDao.get("wxsettlebatchno");
		switch (v.getId()) {
		case R.id.ll_reprint_yhkjs:
			if ( !TextUtils.isEmpty(settlebatchno)) {

				settleDataMap = new HashMap<String, String>();
				settleDataMap.put("transprocode", "900000");
				settleDataMap.put("batchbillno",
						paramConfigDao.get("settlebatchno"));
				settleDataMap.put("translocaldate",
						paramConfigDao.get("translocaldate"));
				settleDataMap.put("translocaltime",
						paramConfigDao.get("translocaltime"));
				settleDataMap.put("requestSettleData",
						paramConfigDao.get("requestSettleData"));
				settleDataMap.put("settledata",
						paramConfigDao.get("settledata"));
				settleDataMap.put("respcode", paramConfigDao.get("respcode"));
				settleDataMap.put("isReprints", "true");
				print(false);
			} else {
				showTips("暂无结算信息，无法打印！");
			}
			break;

		case R.id.ll_reprint_smjs:
			if (!TextUtils.isEmpty(wxsettlebatchno)) {
				settleDataMap = new HashMap<String, String>();
				settleDataMap.put("transprocode", "900000");
				settleDataMap.put("batchbillno",
						paramConfigDao.get("wxsettlebatchno"));
				settleDataMap.put("translocaldate",
						paramConfigDao.get("wxtranslocaldate"));
				settleDataMap.put("translocaltime",
						paramConfigDao.get("wxtranslocaltime"));
				settleDataMap.put("requestSettleData",
						paramConfigDao.get("wxrequestSettleData"));
				settleDataMap.put("settledata",
						paramConfigDao.get("wxsettledata"));
				settleDataMap.put("respcode", paramConfigDao.get("wxrespcode"));
				settleDataMap.put("isReprints", "true");
				print(true);
			}else {
				showTips("暂无结算信息，无法打印！");
			}
			break;
		default:
			break;
		}
	}

	private void print(final boolean isWx) {
		DialogFactory.showLoadingTip(15000,mActivity, "正在打印凭条，请稍等",new DialogTimeout());
		new Thread() {

			@Override
			public void run() {
				printList = PrintDev.getPrintItemObjs(settleDataMap,RePrintSettleMainActivity.this);
				try {
					printer.printText(printList);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				super.run();
			}

		}.start();
	}

	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			String tip = (String) msg.obj;
            if(msg.what!=102 && msg.what!=202){
                showTips(tip);
            }
			switch (msg.what) {
			case 102:// 打印第一联等待装纸
				DialogFactory.showMessage(mActivity, "打印机无纸", "请放入打印纸后重新开始",
						"继续打印", new OnClickListener() {

							@Override
							public void onClick(View v) {
                                DialogFactory.dismissAlert(mActivity);
								onDeviceConnected(deviceManager);
								DialogFactory.showLoadingTip(15000,mActivity, "正在打印凭条，请稍等",new DialogTimeout());
								new Thread() {

									@Override
									public void run() {
										try {

											printer.printText(
													printList
//													new AidlPrinterListener.Stub() {
//
//														@Override
//														public void onPrintFinish()
//																throws RemoteException {
//															Message msg = new Message();
//															msg.obj = "打印完成";
//															mHandler.sendMessage(msg);
//															DialogFactory.dismissAlert(mActivity);
//														}
//
//														@Override
//														public void onError(
//																int state)
//																throws RemoteException {
//															Message msg = new Message();
//															String tip = "";
//															switch (state) {
//															case PRINT_STATE_NORMAL:
//																tip = "正常";
//																break;
//															case PRINT_STATE_NO_PAPER:
//																msg.what = 102;
//																tip = "无纸";
//																break;
//															case PRINT_STATE_HOT:
//																msg.what = 101;
//																tip = "过热";
//																break;
//															case PRINT_STATE_UNKNOW:
//																msg.what = 101;
//																tip = "未知";
//																break;
//															default:
//																msg.what = 102;
//																tip = "未知";
//																break;
//															}
//															msg.obj = "打印出错,原因："
//																	+ tip;
//															mHandler.sendMessage(msg);
//															DialogFactory.dismissAlert(mActivity);
//														}
//													}
											);
										} catch (RemoteException e) {
											e.printStackTrace();
										} catch (Exception e) {
											e.printStackTrace();
										}
										super.run();
									}

								}.start();

							}
						}, "返回", null);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public void goback(View v) {
		NldPaymentActivityManager.getActivityManager()
				.removeActivity(RePrintSettleMainActivity.this);
	}

	@Override
	public void setTopTitle() {
		TextView topTitle = (TextView) super.findViewById(R.id.top_title);

		topTitle.setText("重打印结算");
	}

	@Override
	public void onDeviceConnected(AidlDeviceService deviceManager) {
		try {
			printer = AidlPrinter.Stub.asInterface(deviceManager.getPrinter());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

    @Override
    public void onDeviceConnectFaild() {
        // TODO Auto-generated method stub
        
    }
    public class DialogTimeout implements IDialogTimeoutListener{

        @Override
        public void onDialogTimeout(String tip) {
            showTips("打印异常");
        }
        
    }
}
