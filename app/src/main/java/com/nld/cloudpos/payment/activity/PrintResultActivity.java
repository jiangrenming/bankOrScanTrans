package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.printer.AidlPrinter;
import com.nld.cloudpos.aidl.printer.AidlPrinterListener;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.data.PrinterConstant;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.cloudpos.payment.controller.TransUtils;
import com.nld.cloudpos.payment.dev.PrintDev;
import com.nld.cloudpos.payment.interfaces.IDialogTimeoutListener;
import com.nld.cloudpos.util.CommonContants;
import com.nld.cloudpos.util.DialogFactory;
import com.nld.cloudpos.util.NormalPrintUtils;
import com.nld.cloudpos.util.printBitmap;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.CommonUtil;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.wxtrade.bean.scan_settle.ScanSettleRes;
import com.nld.starpos.wxtrade.local.db.ScanTransDao;
import com.nld.starpos.wxtrade.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanTransDaoImp;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.utils.params.TransType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.DateTimeUtil;
import common.StringUtil;

import static com.nld.cloudpos.util.printBitmap.getPrintBitmap;
import static common.Utility.transformToMap;

public class PrintResultActivity extends BaseAbstractActivity {

    public final static int PRINT_STATE_NORMAL = 0;//正常
    public final static int PRINT_STATE_NO_PAPER = 1;//缺纸
    public final static int PRINT_STATE_HOT = 2;//高温
    public final static int PRINT_STATE_UNKNOW = 0x03;//未知
    private static final int FONT_SIZE_NORM = 8;
    private static final int FONT_SIZE_MIN = 4;
    private final static String LINE = "------------------------------------------------";

    private AidlPrinter printer = null;
    private String title = "";
    private String transCode = "";
    /**
     * 用于控制打印第二联时重复点击按钮
     */
    private int printTime = 0;

    private ImageView mIcon;
    private TextView mResultTv;
    private TextView mReasonTv;
    private Button mSecondBtn;

    private String billno;
    private String time_stamp;//交易时间
    private String refernumber;//参考号

    private TransRecordDao transDao;

    private Map<String, String> transMap = new HashMap<String, String>();
    /**
     * 标志是否已经打印过，如果打印过，则不在打印
     */
    private boolean isPrinted = false;
    private int printtimes = 1;
    private ScanTransRecord mScanWater;  //扫码交易
    private ScanSettleRes mSsRes;   //扫码结算
    private Object mObject;
    private String mTransType;
    private Bitmap mPrintBitmap;

    @Override
    public int contentViewSourceID() {
        return R.layout.consume_result;
    }

    public Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String tip = (String) msg.obj;
            if (msg.what != 102 && msg.what != 202) {
                showTip(tip);
            }
            switch (msg.what) {
                case 100://打印结束
//                showSecondView();
                    DialogFactory.showConfirmMessageTimeout(5000, mActivity, "提示", "点击“确定”继续打印下一联", "确定", new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if ("3".equals(ParamsUtil.getInstance().getParam("printtimes"))
                                    && printtimes < 2) {
                                isPrinted = false;
                                printtimes++;
                                onServiceConnecteSuccess(mDeviceService);
                            } else {
                                //打印第二联
                                printSecondTip(mSecondBtn);
                            }
                        }
                    });
                    break;
                case 101://打印异常
//                    showErrorView(tip);
//                    break;
                case 102://打印第一联等待装纸
                    DialogFactory.showMessage(mActivity, "打印机" + tip, "请重新尝试", "继续打印", new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            DialogFactory.dismissAlert(mActivity);
                            onServiceConnecteSuccess(mDeviceService);
                        }
                    }, "返回", new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            DialogFactory.dismissAlert(mActivity);
                            onServiceConnecteSuccess(mDeviceService);
                        }
                    });
                    break;
                case 200://打印结束
                    showPrintFinishView();
                    if (Cache.getInstance().isThree()) {//第三方调用，打完第二联，直接结束页面
                        returnMenu();
                    }
                    break;
                case 201://打印异常
                    //  showErrorView(tip);
                    //   break;
                case 202://等待装纸
                    DialogFactory.showMessage(mActivity, "打印机" + tip, "请重新尝试", "继续打印", new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            DialogFactory.dismissAlert(mActivity);
                            printSecondTip(mSecondBtn);
                        }
                    }, "返回", new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            DialogFactory.dismissAlert(mActivity);
                            printSecondTip(mSecondBtn);
                        }
                    });
                    break;
                case 300: //扫码批结
                    //打印结算单中断标志
                    ShareScanPreferenceUtils.putBoolean(PrintResultActivity.this, TransParamsValue.SettleConts.PARAMS_IS_PRINT_SETTLE_HALT, false);
                    //打印明细中断的标志
                    ShareScanPreferenceUtils.putBoolean(PrintResultActivity.this, TransParamsValue.SettleConts.PARAMS_IS_PRINT_ALLWATER_HALT, true);
                    DialogFactory.showConfirmMessageTimeout(5000, mActivity, "提示", "点击确定继续打印明细", "确定", new OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            if ("3".equals(ParamsUtil.getInstance().getParam("printtimes"))
//                                    && printtimes < 2) {
//                                isPrinted = false;
//                                printtimes++;
//                                onServiceConnecteSuccess(mDeviceService);
//                            } else {
                            //打印第二联
                            printerScanSummry(mSecondBtn);
//                            }
                        }
                    });
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    public void initView() {
        if (null == mPrintBitmap) {
            mPrintBitmap = getPrintBitmap(mActivity);
        }
        Intent intent = getIntent();
        mObject = intent.getSerializableExtra("water");
        mTransType = intent.getStringExtra("transType");
//        mSsRes = (ScanSettleRes) intent.getSerializableExtra("scan_settle_water");
        transDao = new TransRecordDaoImpl();
        String inpCode = Cache.getInstance().getSerInputCode();
//        if (inpCode.startsWith("05")) {
//            findViewById(R.id.print_getcard_tip).setVisibility(View.VISIBLE);
//        }
        mIcon = (ImageView) findViewById(R.id.print_result_icon);
        mResultTv = (TextView) findViewById(R.id.print_result_tv);
        mReasonTv = (TextView) findViewById(R.id.print_reason_tv);
        mSecondBtn = (Button) findViewById(R.id.print_second_btn);
        mSecondBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                printSecondTip(v);
            }
        });

        transCode = Cache.getInstance().getTransCode();
        if ("002302".equals(transCode)) { // 消费
            title = "消费";
        } else if ("002301".equals(transCode)) { // 余额查询
        } else if ("002303".equals(transCode)) { // 消费撤销
            title = "消费撤销";
        } else if ("002308".equals(transCode)) { // 签到
        } else if ("002309".equals(transCode)) { // 签退、结算
        } else if ("900004".equals(transCode)) { // 下载证书
        } else if ("002313".equals(transCode)) {    //预授权
            title = "预授权";
        } else if ("002314".equals(transCode)) { // 预授权完成
            title = "预授权完成";
        } else if ("002315".equals(transCode)) {  // 预授权完成撤销
            title = "预授权完成撤销";
        } else if ("002316".equals(transCode)) { // 预授权撤销
            title = "预授权撤销";
        } else if ("002317".equals(transCode)) { // 退货
            title = "退货";
        } else if ("002319".equals(transCode)) { // IC公钥下载
        } else if ("002320".equals(transCode)) { // IC参数下载
        } else if ("002322".equals(transCode)) {    //指定账户圈存
            title = "指定账户圈存";
        } else if ("002323".equals(transCode)) {    //非指定账户圈存
            title = "非指定账户圈存";
        } else if ("002321".equals(transCode)) {    //现金充值
            title = "现金充值";
        } else if ("002324".equals(transCode)) {    //现金充值撤销
            title = "现金充值撤销";
        } else if ("002325".equals(transCode)) {    // 脱机退货
            title = "脱机退货";
        } else if ("002326".equals(transCode)) {    // 自动上送脱机消费
        }
        setTopTitle(title);
        setTopReturnListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                returnMenu();
            }
        });
        showFirstView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // 确认按钮点击
    public void confirm(View v) {
        returnMenu();
    }

    public void returnMenu() {
        NldPaymentActivityManager.getActivityManager()
                .removeAllActivityExceptOne(LauncherActivity.class);
    }

    /**
     * 打印扫码明细第二联
     *
     * @param v
     */
    public void printerScanSummry(View v) {
        if (null == mDeviceService) {
            bindService();
            return;
        }
        if (null == printer) {
            try {
                printer = AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
            } catch (RemoteException e) {
                logger.error("获取打印机失败", e);
                e.printStackTrace();
            }
        }
        DialogFactory.showLoadingTip(15000, mActivity, "正在打印明细，请稍等", new DialogTimeout());
        printTime++;
        if (printTime <= 1) {
            new Thread() {
                @Override
                public void run() {
                    List<PrintItemObj> records = getRecords();
                    Log.d("wqz-交易明细数据", records.size() + "");
                    if (records.isEmpty()) {
                        Message msg = new Message();
                        msg.obj = "打印内容获取异常";
                        msg.what = 101;
                        mHandler.sendMessage(msg);
                        DialogFactory.dismissAlert(mActivity);
                        return;
                    }
                    transMap.put("isSecond", "true");
                    try {
                        printer.open();
                        printer.printText(records);
                        printer.start(new AidlPrinterListener.Stub() {

                            @Override
                            public void onPrintFinish() throws RemoteException {
                                printer.paperSkip(2);
                                Message msg = new Message();
                                msg.obj = "打印完成";
                                msg.what = 200;
                                mHandler.sendMessage(msg);
                                DialogFactory.dismissAlert(mActivity);
                                //打印结算明细中断标志
                                ShareScanPreferenceUtils.putBoolean(PrintResultActivity.this, TransParamsValue.SettleConts.PARAMS_IS_PRINT_ALLWATER_HALT, false);
                            }

                            @Override
                            public void onError(int state) throws RemoteException {
                                Message msg = new Message();
                                logger.debug("打印出错：" + state);
                                printTime = 0;
                                String tip = "";
                                switch (state) {
                                    case PRINT_STATE_NORMAL:
                                        tip = "正常";
                                        break;
                                    case PRINT_STATE_NO_PAPER:
                                        msg.what = 202;
                                        tip = "无纸";
                                        break;
                                    default:
                                        msg.what = 201;
                                        tip = NormalPrintUtils.getErrMsg(state);
                                        break;
                                }
                                msg.obj = "打印出错,原因：" + tip;
                                mHandler.sendMessage(msg);
                                DialogFactory.dismissAlert(mActivity);
                            }
                        });
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    super.run();
                }
            }.start();
        }
    }

    private List<PrintItemObj> getRecords() {
        List<PrintItemObj> records = new ArrayList<>();
        records.add(PrintDev.getPrintItemObj("交易明细/TXN  LIST", PrintDev.FONT_SIZE_MAX, PrintItemObj.ALIGN.CENTER));
        records.add(PrintDev.getPrintItemObj("凭证号   类型    卡号             金额    授权码", PrintDev.FONT_SIZE_MIN));
        records.add(PrintDev.getPrintItemObj("VOUCHER  TYPE    CARD NUMBER     AMOUNT   AUTH  NO", PrintDev.FONT_SIZE_MIN));
        records.add(PrintDev.getPrintItemObj("================================================", PrintDev.FONT_SIZE_MIN));
        ScanTransDao scanTransDao = new ScanTransDaoImp();
        List<ScanTransRecord> scanTransDaoAll = scanTransDao.findAll();
        Log.d("wqz-批结数据", scanTransDaoAll.size() + "");
        if (scanTransDaoAll != null && !scanTransDaoAll.isEmpty()) {
            for (ScanTransRecord scanTransRecord : scanTransDaoAll) {
                ScanTransRecord scanTransDaoByTrace = scanTransDao.findByTrace(scanTransRecord.getSystraceno());
                if (scanTransDaoByTrace != null) {
                    Log.d("wqz-批结数据", scanTransDaoByTrace.getSystraceno());
                    int transType = scanTransDaoByTrace.getTransType();
                    Log.d("wqz-transtye", "transtype:===>" + transType + "amount=" + scanTransDaoByTrace.getTransamount());
                    String type = "";
                    String amount;
                    if (transType != -1) {
                        switch (transType) {
                            case TransType.ScanTransType.TRANS_SCAN_WEIXIN:
                            case TransType.ScanTransType.TRANS_QR_WEIXIN:  //微信
                                type = "W";
                                amount = scanTransDaoByTrace.getTransamount();
//                                records.add(new PrintItemObj((scanTransRecord.getSystraceno() + "  " + type + "       " + amount), PrintDev.FONT_SIZE_MIN));
                                records.add(PrintDev.getPrintItemObj((scanTransRecord.getSystraceno() + "      " + type + "               " + amount), PrintDev.FONT_SIZE_MIN));
                                break;
                            case TransType.ScanTransType.TRANS_SCAN_REFUND: //扫码退款
                                type = "T";
                                amount = scanTransDaoByTrace.getTransamount();
//                                records.add(new PrintItemObj((scanTransRecord.getSystraceno() + "  " + type + "       " + amount), PrintDev.FONT_SIZE_MIN));
                                records.add(PrintDev.getPrintItemObj((scanTransRecord.getSystraceno() + "      " + type + "               " + amount), PrintDev.FONT_SIZE_MIN));
                                break;
                            case TransType.ScanTransType.TRANS_SCAN_ALIPAY: //支付宝
                            case TransType.ScanTransType.TRANS_QR_ALIPAY:
                                type = "Z";
                                amount = scanTransDaoByTrace.getTransamount();
//                                records.add(new PrintItemObj((scanTransRecord.getSystraceno() + "  " + type + "       " + amount), PrintDev.FONT_SIZE_MIN));
                                records.add(PrintDev.getPrintItemObj((scanTransRecord.getSystraceno() + "      " + type + "               " + amount), PrintDev.FONT_SIZE_MIN));
                                break;
                        }
                    }
                }
            }
        }
        records.add(PrintDev.getPrintItemObj(
                "交易类型:S-消费 R-退货 P-预授权完成(请求) C-预授权完成(通知) " +
                        "L-离线结算 E-电子现金(钱包)消费 Q-圈存类、充值类交易  " +
                        "B-积分消费  V-消费撤销  A-完成撤销 W-微信类消费 Y-银联二维码类消费 Z-支付宝类交易 T-扫码退货",
                PrintDev.FONT_SIZE_MIN));
        return records;
    }


    /**
     * 打印第二联按钮
     *
     * @param v
     */
    public void printSecondTip(View v) {
        if (null == mDeviceService) {
            bindService();
            return;
        }
        if (null == printer) {
            try {
                printer = AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
            } catch (RemoteException e) {
                logger.error("获取打印机失败", e);
                e.printStackTrace();
            }
        }

        DialogFactory.showLoadingTip(15000, mActivity, "正在打印凭条，请稍等", new DialogTimeout());
        printTime++;
        if (printTime <= 1) {
            new Thread() {

                @Override
                public void run() {

//                List<PrintItemObj> list = PrintDev.getPrintData(true,mContext,Cache.getInstance().getResultMap(), title);
                    List<PrintItemObj> list = getPrintList(true);
                    transMap.put("isSecond", "true");
//                    List<PrintItemObj> list = getPrintItemObjList(true);
                    try {
                        printer.open();
                        if (null == mObject || mObject instanceof ScanTransRecord) {
                            if (null == mPrintBitmap) {
                                mPrintBitmap = printBitmap.getPrintBitmap(mActivity);
                            }
                            printer.printImage(PrinterConstant.Align.ALIGN_CENTER, mPrintBitmap);
                        }
                        printer.printText(list);
                        printer.start(new AidlPrinterListener.Stub() {

                            @Override
                            public void onPrintFinish() throws RemoteException {
                                printer.paperSkip(2);
                                Message msg = new Message();
                                msg.obj = "打印完成";
                                msg.what = 200;
                                mHandler.sendMessage(msg);
                                DialogFactory.dismissAlert(mActivity);
                            }

                            @Override
                            public void onError(int state) throws RemoteException {
                                Message msg = new Message();
                                logger.debug("打印出错：" + state);
                                printTime = 0;
                                String tip = "";
                                switch (state) {
                                    case PRINT_STATE_NORMAL:
                                        tip = "正常";
                                        break;
                                    case PRINT_STATE_NO_PAPER:
                                        msg.what = 202;
                                        tip = "无纸";
                                        break;
                                    default:
                                        msg.what = 201;
                                        tip = NormalPrintUtils.getErrMsg(state);
                                        break;
                                }
                                msg.obj = "打印出错,原因：" + tip;
                                mHandler.sendMessage(msg);
                                DialogFactory.dismissAlert(mActivity);
                            }
                        });
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    super.run();
                }

            }.start();
        }
    }

    private List<PrintItemObj> getPrintItemObjList(boolean isSecond) {
        int FONT_SIZE_MAX = 16;
        ArrayList<PrintItemObj> printItemObjs = new ArrayList<PrintItemObj>();
        printItemObjs.add(getPrintItemObj("POS签购单 ", FONT_SIZE_MAX, true, PrintItemObj.ALIGN.CENTER));
  //      printItemObjs.add(getPrintItemObj("商户名称:" + GlobeData.merchantInfo.getMercNm()));
 //       printItemObjs.add(getPrintItemObj("商户号:" + GlobeData.merchantInfo.getMerchantId()
  //              + " " + "终端号:" + GlobeData.merchantInfo.getTerminalNo()));
        printItemObjs.add(getPrintItemObj("操作员号:" + CommonContants.OPERATOR + " 收单机构:" + "新大陆"));
        printItemObjs.add(getPrintItemObj("卡号:" + Cache.getInstance().getCardNo()));
        printItemObjs.add(getPrintItemObj("卡类别:" + "银联卡"));
        printItemObjs.add(getPrintItemObj("交易类别:" + "消费", FONT_SIZE_MAX, true));
        printItemObjs.add(getPrintItemObj("批次号:" + "000001"));
        printItemObjs.add(getPrintItemObj("凭证号:" + Cache.getInstance().getSerialNo()));
        printItemObjs.add(getPrintItemObj("日期/时间:" + DateTimeUtil.timeLongToString("yyMMddHHmmss", System.currentTimeMillis())));
        printItemObjs.add(getPrintItemObj("金额:RMB" + " " + Cache.getInstance().getTransMoney() + "元", 16, true));
        printItemObjs.add(getPrintItemObj("备注:", FONT_SIZE_MIN, true));
        String exVersion = "SD_" + CommonUtil.getAppVersion();
        if (!isSecond) {
            printItemObjs.add(getPrintItemObj("持卡人签名:", FONT_SIZE_MIN, true));
            printItemObjs.add(getPrintItemObj("\n\n本人确认以上交易,同意将其记入本卡账户"
                    , FONT_SIZE_MIN, true));
            printItemObjs.add(getPrintItemObj(
                    "I ACKNOWLEDGE SATISFACTORY RECEIPT OF RELATIVE GOODS/SERVICES"
                    , FONT_SIZE_MIN, true));
            printItemObjs.add(getPrintItemObj(LINE));
            printItemObjs.add(getPrintItemObj(exVersion + " 客服热线:400-678-8888"
                    , FONT_SIZE_MIN, true));
            printItemObjs.add(getPrintItemObj("-------------------商户存根---------------------"));
        } else {
            printItemObjs.add(getPrintItemObj(LINE));
            printItemObjs.add(getPrintItemObj(exVersion + " 客服热线:400-678-8888"
                    , FONT_SIZE_MIN, true));
            printItemObjs.add(getPrintItemObj("-------------------持卡人存根-------------------"));
        }
        return printItemObjs;
    }

    //重定义打印以适合各种情况
    public static PrintItemObj getPrintItemObj(String text, int fontSize, boolean isBold,
                                               PrintItemObj.ALIGN align) {
        int fontTypeDefault = PrinterConstant.FontType.FONTTYPE_S;
        int fontType;
        if (isBold) {
            fontType = PrinterConstant.FontType.FONTTYPE_S;
        } else {
            fontType = PrinterConstant.FontType.FONTTYPE_N;
        }
        switch (fontSize) {
            case 20:
                return new PrintItemObj(text, FONT_SIZE_NORM, fontType);
            case 16:
                return new PrintItemObj(text, FONT_SIZE_NORM, CommonContants.FONT_TYPE, align);
            default:
                return new PrintItemObj(text, FONT_SIZE_MIN, CommonContants.FONT_TYPE, align);
        }
    }

    public static PrintItemObj getPrintItemObj(String text, int fontSize, boolean isBold) {
        int fontTypeDefault = PrinterConstant.FontType.FONTTYPE_S;
        int fontType;
        if (isBold) {
            fontType = PrinterConstant.FontType.FONTTYPE_S;
        } else {
            fontType = PrinterConstant.FontType.FONTTYPE_N;
        }
        switch (fontSize) {
            case 20:
                return new PrintItemObj(text, 16, fontType);
            case 16:
                return new PrintItemObj(text, FONT_SIZE_NORM, CommonContants.FONT_TYPE);
            default:
                return new PrintItemObj(text, FONT_SIZE_MIN, CommonContants.FONT_TYPE);
        }
    }

    public static PrintItemObj getPrintItemObj(String text, int fontSize) {

        int fontTypeDefault = PrinterConstant.FontType.FONTTYPE_S;
        switch (fontSize) {
            case 20:
                return new PrintItemObj(text);
            case 16:
                return new PrintItemObj(text, FONT_SIZE_NORM, fontTypeDefault);
            default:
                return new PrintItemObj(text, FONT_SIZE_MIN, fontTypeDefault);
        }
    }

    public static PrintItemObj getPrintItemObj(String text) {
        return new PrintItemObj(text, FONT_SIZE_MIN, CommonContants.FONT_TYPE);
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {
        try {
            printer = AidlPrinter.Stub.asInterface(service.getPrinter());
            if (isPrinted) {
                return;
            }
            //打印明细是否中断的标志
            boolean printer_halt = ShareScanPreferenceUtils.getBoolean(PrintResultActivity.this, TransParamsValue.SettleConts.PARAMS_IS_PRINT_ALLWATER_HALT, false);
            if (printer_halt) {
                printerScanSummry(mSecondBtn);
            } else {
                DialogFactory.showLoadingTip(15000, mActivity, "正在打印凭条，请稍等", new DialogTimeout());
                new Thread() {
                    @Override
                    public void run() {
                        List<PrintItemObj> list = getPrintList(false);
//                    final Object printObject = getPrintObject();
                        if (null == list || list.isEmpty()) {
                            logger.debug("获取打印数据失败");
                            Message msg = new Message();
                            msg.obj = "打印内容获取异常";
                            msg.what = 101;
                            mHandler.sendMessage(msg);
                            DialogFactory.dismissAlert(mActivity);
                            return;
                        }
//                    if (null == mObject) {
//                        //银行卡相关打印
//
//                    } else {
//                        mActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                //扫码相关打印
//                                if (mObject instanceof ScanTransRecord) {
//                                    ScanTransRecord str = (ScanTransRecord) mObject;
//                                    str.setAmount("true");
//                                    str.setPayChannel(PrintDev.getPayChannelStr(str.getPayChannel()));
//                                    PrintHelper.getInstance().print(str, PrinterListenner(), mHandler, mActivity, PrintHelper.PRINT_SCAN_RESULT_MODEL);
//                                }
//                                if (mObject instanceof ScanSettleRes) {
//                                    ScanSettleRes ssr = (ScanSettleRes) mObject;
//                                    Log.d("扫码批结数据===》", "weixinAm" + ssr.getPhWeiXinAmt() + "  " + ssr.getPosWeiXinAmt()
//                                            + "alipayAm" + ssr.getPhAlipayAmt() + " " + ssr.getPosAlipayAmt());
//                                    ssr.initStatistics();
//                                    setScanShopInfo(ssr);
//                                    PrintHelper.getInstance().print(ssr, PrinterListenner(), mHandler, mActivity, PrintHelper.PRINT_SETTLE);
//                                }
//                            }
//                        });
//
//                    }

                        try {
                            int printerState = printer.getPrinterState();
                            if (printerState == PrinterConstant.PrinterState.PRINTER_STATE_NORMAL) {
                                printer.open();
                                if (null == mObject || mObject instanceof ScanTransRecord) {
                                    if (null == mPrintBitmap) {
                                        mPrintBitmap = printBitmap.getPrintBitmap(mActivity);
                                    }
                                    printer.printImage(PrinterConstant.Align.ALIGN_CENTER, mPrintBitmap);
                                }
                                printer.printText(list);
                                printer.start(PrinterListenner());
                            } else {
                                switch (printerState) {
                                    case PRINT_STATE_NO_PAPER:
                                        Message msg = new Message();
                                        msg.obj = "打印码异常" + NormalPrintUtils.getErrMsg(printerState);
                                        msg.what = 102;
                                        mHandler.sendMessage(msg);
                                        DialogFactory.dismissAlert(mActivity);
                                        break;
                                    default:
                                        Message msg1 = new Message();
                                        msg1.obj = "打印码异常" + NormalPrintUtils.getErrMsg(printerState);
                                        msg1.what = 101;
                                        mHandler.sendMessage(msg1);
                                        DialogFactory.dismissAlert(mActivity);
                                }

                            }

                        } catch (RemoteException e) {
                            logger.error("打印失败", e);
                            Message msg = new Message();
                            msg.obj = "打印失败";
                            msg.what = 101;
                            mHandler.sendMessage(msg);
                            DialogFactory.dismissAlert(mActivity);
                            e.printStackTrace();
                        }
                        super.run();
                    }

                }.start();
            }

        } catch (RemoteException e) {
            logger.error("打印失败", e);
            Message msg = new Message();
            msg.obj = "打印失败";
            msg.what = 101;
            mHandler.sendMessage(msg);

            DialogFactory.dismissAlert(mActivity);
            e.printStackTrace();
        }
    }

    private void setScanShopInfo(ScanSettleRes ssr) {
        String MercNm = "";
        String PayMercId = "";
        String TerminalNo = "";
        String operatorcode = "";
        String scan_batchno = "";
        String params_settle_time = "";
        try {
            MercNm = ParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME);//商户名称
            PayMercId = ParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID);//商户编号
            TerminalNo = ParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID);//终端号
            operatorcode = ParamsUtil.getInstance().getParam("operatorcode"); //操作员
            scan_batchno = ParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO);//批次号
            params_settle_time = ShareScanPreferenceUtils.getString(this, TransParamsValue.SettleConts.PARAMS_SETTLE_TIME, ""); //日期时间
        } catch (Exception e) {
            e.printStackTrace();
        }
        ssr.setMerchantName(MercNm);
        ssr.setShopId(PayMercId);
        ssr.setTerminalId(TerminalNo);
        ssr.setOperId(operatorcode);
        ssr.setBatchNo(scan_batchno);
        ssr.setDateTime(params_settle_time);
    }

    /**
     * 根据Bean
     *
     * @return
     */
    private List<PrintItemObj> getPrintList(boolean isSecond) {
        if (null == mObject) {
            //                    List<PrintItemObj> list = PrintDev.getPrintData(false,mContext,Cache.getInstance().getResultMap(), title);
            billno = Cache.getInstance().getSerialNo();
            Log.i("BILLNO", "print  getBatchNo : " + Cache.getInstance().getBatchNo() + "  billno : " + billno);
            TransRecord record = transDao.getTransRecordByCondition(Cache.getInstance().getBatchNo(), billno);
            if (null != record) {
                Calendar c = Calendar.getInstance();
                String year = String.valueOf(c.get(Calendar.YEAR));
                refernumber = record.getRefernumber();//交易成功放回交易参考号
                time_stamp = year + record.getTranslocaldate() + record.getTranslocaltime();//交易成功放回交易时间戳
            }
            transMap = transformToMap(record);
            if (isSecond) {
                if (transMap != null) {
                    transMap.put("isSecond", "true");
                }
            }
            //                    List<PrintItemObj> list = getPrintItemObjList(false);
            return PrintDev.getPrintItemObjs(transMap, mContext);
        } else {
            int type = Integer.valueOf(mTransType);
            switch (type) {
                case TransType.ScanTransType.TRANS_SCAN_SETTLE:
                    if (mObject instanceof ScanSettleRes)
                        mSsRes = (ScanSettleRes) mObject;
                    return PrintDev.getScanJsPrintItemObj(mSsRes, mContext);//扫码结算
                default:
                    if (mObject instanceof ScanTransRecord)
                        mScanWater = (ScanTransRecord) mObject;
                    return PrintDev.getWxScanPrintItemObj(mScanWater, mContext);//扫码交易
            }
        }
    }

    private Object getPrintObject() {
        if (null == mObject) {
            return transDao.getTransRecordByCondition(Cache.getInstance().getBatchNo(), billno);
        } else {
            return mObject;
        }
    }


    private AidlPrinterListener PrinterListenner() {
        return new AidlPrinterListener.Stub() {

            @Override
            public void onPrintFinish() throws RemoteException {
                isPrinted = true;
                Message msg = new Message();
                msg.obj = "打印完成";
                if (mObject != null) {
                    if (TransType.ScanTransType.TRANS_SCAN_SETTLE != Integer.valueOf(mTransType)) {
                        String scan_printer = ScanParamsUtil.getInstance().getParam(TransParamsValue.SCAN_PRINTER_STATUE);
                        if (TransParamsValue.SCAN_PRINTER_TWO_PAPER.equals(StringUtil.isEmpty(scan_printer) ? "1" : scan_printer)){ //代表二联
                            printer.paperSkip(2);
                            msg.what = 100;
                        }else {
                            printer.paperSkip(1);
                            msg.what = 200;
                        }
                    } else {
                        printer.paperSkip(2);
                        msg.what = 300;
                    }
                } else {
                    printer.paperSkip(2);
                    msg.what = 100;
                }
                mHandler.sendMessage(msg);
                DialogFactory.dismissAlert(mActivity);
            }

            @Override
            public void onError(int state) throws RemoteException {
                isPrinted = false;
                logger.debug("打印错误：state=" + state);
                Message msg = new Message();
                String tip = "";
                switch (state) {
                    case PRINT_STATE_NORMAL:
                        tip = "正常";
                        break;
                    case PRINT_STATE_NO_PAPER:
                        msg.what = 202;
                        tip = "无纸";
                        break;
                    default:
                        msg.what = 201;
                        tip = NormalPrintUtils.getErrMsg(state);
                        break;
                }
                msg.obj = "打印出错,原因：" + tip;
                mHandler.sendMessage(msg);
                DialogFactory.dismissAlert(mActivity);
            }
        };
    }

    @Override
    public void onServiceBindFaild() {

    }

    public void showErrorView(String tip) {
        mSecondBtn.setVisibility(View.GONE);
        mIcon.setBackgroundResource(R.drawable.pic_04);
        mResultTv.setText("打印失败");
        mResultTv.setTextColor(mContext.getResources().getColor(R.color.red));
        mReasonTv.setText(tip);
    }

    public void showSecondView() {
        mSecondBtn.setVisibility(View.VISIBLE);
        mIcon.setBackgroundResource(R.drawable.pic_02);
        mResultTv.setText("是否打印");
        mReasonTv.setText("第二联（持卡人存根）");

    }

    public void showFirstView() {
        mSecondBtn.setVisibility(View.GONE);
        mIcon.setBackgroundResource(R.drawable.pic_03);
        mResultTv.setText("正在打印凭条");
        mReasonTv.setText("请妥善保管您的发票凭条");

    }

    public void showPrintFinishView() {
        mSecondBtn.setVisibility(View.GONE);
        mIcon.setBackgroundResource(R.drawable.pic_03);
        mResultTv.setText("打印成功");
        mReasonTv.setText("请妥善保管您的发票凭条");
        //针对扫码批结 (删除数据库，增加批次号)
        if (mTransType != null) {
            if (Integer.valueOf(mTransType) == TransType.ScanTransType.TRANS_SCAN_SETTLE) {
                //清除结算数据的标志
                ShareScanPreferenceUtils.putBoolean(this, TransParamsValue.SettleConts.PARAMS_IS_CLEAR_SETTLT_HLAT, true);
                TransUtils.clearWaterForScanTrans();
            }
        }
    }

    @Override
    public boolean saveValue() {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_HOME
                || keyCode == KeyEvent.KEYCODE_MENU) {
            returnMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public class DialogTimeout implements IDialogTimeoutListener {

        @Override
        public void onDialogTimeout(String tip) {
            showErrorView("打印异常");
        }

    }
}
