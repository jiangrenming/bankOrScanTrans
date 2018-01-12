package com.nld.cloudpos.payment.activity;

import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.printer.AidlPrinter;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.cloudpos.payment.dev.PrintDev;
import com.nld.cloudpos.payment.interfaces.IDialogTimeoutListener;
import com.nld.cloudpos.util.MyLog;
import com.nld.cloudpos.util.NormalPrintUtils;
import com.nld.starpos.banktrade.db.SettleDataDao;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.local.SettleDataDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.ShareBankPreferenceUtils;
import com.nld.starpos.banktrade.utils.TransParams;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import common.StringUtil;

public class SettleResult extends BaseAbstractActivity {


    public final static int PRINT_STATE_NORMAL = 0x00;//正常
    public final static int PRINT_STATE_NO_PAPER = 0x01;//缺纸
    public final static int PRINT_STATE_HOT = 0x02;//高温
    public final static int PRINT_STATE_UNKNOW = 0x03;//未知
    private static MyLog logger = MyLog.getLogger(SettleResult.class);

    private AidlPrinter printer = null;
    private TextView mTip;
    private TextView mResultTitle;
    private Button mConfirm, mPrint;
    private boolean isPrintDetail = false;

    @Override
    public int contentViewSourceID() {
        return R.layout.act_sign_success;
    }

    @Override
    public void initView() {
        ShareBankPreferenceUtils.putString(Constant.BATCH_SETTLE_INTERRUPT_FLAG, Constant.BATCH_SETTLE_INTERRUPT_STEP2);

        setTopTitle("结算成功");
        setTopReturnListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                returnSettleMain();
            }
        });
        mConfirm = (Button) findViewById(R.id.sign_success_confirm);
        mPrint = (Button) findViewById(R.id.sign_success_print);
        mTip = (TextView) findViewById(R.id.sign_success__msg);
        mTip.setVisibility(View.GONE);
        mResultTitle = (TextView) findViewById(R.id.sign_title);
        String errCode = Cache.getInstance().getErrCode();
        mResultTitle.setText("结算成功，对账平");
        mPrint.setVisibility(View.VISIBLE);
        Map<String, String> resultMap = Cache.getInstance().getResultMap();
        String respCode = "";
        if (resultMap != null) {
            respCode = resultMap.get("respcode");
            ShareBankPreferenceUtils.putString(Constant.PARAMS_SETTLE_DATA, StringUtil.transMapToString(resultMap));
        }

        if ("95".equals(errCode) || "95".equals(respCode)) {
            mResultTitle.setText("结算成功，对账不平");
        }

        mPrint.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null == mDeviceService) {
                    bindService();
                    return;
                }
                printDetail(true);
            }
        });

        mConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                returnSettleMain();
            }
        });
    }

    /**
     * 返回接收菜单页
     */
    private void returnSettleMain() {
        clearTransRecord();
        NldPaymentActivityManager.getActivityManager().removeAllActivityExceptOne(LauncherActivity.class);
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {
        printDetail(false);
    }

    @Override
    public void onServiceBindFaild() {

    }

    @Override
    public boolean saveValue() {
        return false;
    }

    public void printDetail(final boolean printDetail) {
        isPrintDetail = printDetail;
//        DialogFactory.showLoadingTip(60000, mActivity, "正在打印凭条，请稍等", new DialogTimeout());
        if (null == printer) {
            try {
                printer = AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
            } catch (RemoteException e) {
                logger.e("获取打印机失败", e);
                e.printStackTrace();
            }
        }
        new Thread() {

            @Override
            public void run() {
                List<PrintItemObj> list = PrintDev.getSettlePrintMap(mContext, printDetail, Cache.getInstance().getResultMap(), false);

                Map resultMap = Cache.getInstance().getResultMap();
                if (resultMap == null) {
                    Log.i("SettleResult", "resultMap 为null");
                } else {
                    Iterator<Map.Entry<String, String>> iterator = resultMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, String> entry = iterator.next();
                        Log.d("SettleResult", "key : " + entry.getKey() + "   value : " + entry.getValue());
                    }
                }
                try {
                    NormalPrintUtils.printNormal(list, SettleResult.this, false, mPrintCallBack);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.run();
            }

        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME ||
                keyCode == KeyEvent.KEYCODE_BACK) {
            returnSettleMain();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void clearTransRecord() {

        //修改签到标志为未签到
        ParamsUtil.getInstance().update(ParamsConts.SIGN_SYMBOL, TransParams.SingValue.UnSingedValue);
        ShareBankPreferenceUtils.putBoolean(
                ParamsConts.PARAMS_CARD_SETTLE_SUCESS,true);

        //交易记录库清0
        logger.d("清空所有交易记录");
        TransRecordDao mTransRecordDao;
        mTransRecordDao = new TransRecordDaoImpl();
        mTransRecordDao.deleteAll();

        logger.d("清空结算表");
        SettleDataDao settleDao = new SettleDataDaoImpl();
        settleDao.delete();

        // 重置参数
        ShareBankPreferenceUtils.putString(Constant.BATCH_SETTLE_INTERRUPT_FLAG, Constant.BATCH_SETTLE_INTERRUPT_STEP4);
        ShareBankPreferenceUtils.putString(Constant.PARAMS_SETTLE_DATA, "");
        Cache.getInstance().clearAllData();
    }

    public class DialogTimeout implements IDialogTimeoutListener {

        @Override
        public void onDialogTimeout(String tip) {
            showTip("打印异常");
        }

    }


    private PrintCallBack mPrintCallBack = new PrintCallBack() {
        @Override
        public void onSuccess() {
            if (isPrintDetail)
                return;
            ShareBankPreferenceUtils.putString(Constant.BATCH_SETTLE_INTERRUPT_FLAG, Constant.BATCH_SETTLE_INTERRUPT_STEP3);
        }

        @Override
        public void onFail() {
            if (isPrintDetail)
                return;
            ShareBankPreferenceUtils.putString(Constant.BATCH_SETTLE_INTERRUPT_FLAG, Constant.BATCH_SETTLE_INTERRUPT_STEP2);
        }
    };

    public static interface PrintCallBack {
        void onSuccess();

        void onFail();
    }
}
