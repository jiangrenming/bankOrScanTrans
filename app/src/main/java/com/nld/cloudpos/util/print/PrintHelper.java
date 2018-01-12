package com.nld.cloudpos.util.print;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.MyApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.printer.AidlPrinter;
import com.nld.cloudpos.aidl.printer.AidlPrinterListener;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.data.PrinterConstant;
import com.nld.cloudpos.util.DialogFactory;
import com.nld.logger.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static com.nld.cloudpos.BankApplication.mDeviceService;


public class PrintHelper {
    private Context mContext;
//    private DeviceServiceAIDLConnect mDeviceConnect;
    private AidlDeviceService mServiceManager;
    private AidlPrinter mPrinter = null;
    private static PrintHelper instance;
    //  private IAppLockService mAppLockService;
//  private ResourceLockServiceManager mResourceLockServiceManager;
    private static boolean isFrist;
    //    private Context mActivity;
    public final static int PRINT_STATE_NORMAL = 0;//正常
    public final static int PRINT_STATE_NO_PAPER = 1;//缺纸
    public final static int PRINT_STATE_NO_PAPER2 = 9;//缺纸
    public final static int PRINT_STATE_HOT = 2;//高温

    public static final String PRINT_SCAN_RESULT_MODEL = "PRINT_WATER_WITHOUT_ENGLISH";
    public static final String PRINT_ALL_WATER = "PRINT_ALL_WATER";
    public static final String PRINT_SETTLE = "PRINT_SETTLE";

//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            String errMsg = String.valueOf(msg.obj);
//            switch (msg.what) {
//                case 1:
//                    ToastUtils.toast(mContext, String.valueOf(msg.obj));
//                    LogUtils.d(mActivity.getClass().getName());
//                    DialogUtils.showDialog(mActivity, String.valueOf(msg.obj));
//                    EventBus.getDefault().post(new MessageEvent(AppConstants.EVENT_KEY_SHOW_PRINT_ERR_DIALOG, errMsg));
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

    public PrintHelper() {
        mContext = BankApplication.context;
//        initDeviceConnect();
        if (mDeviceService != null) {
            try {
                if (mPrinter == null)
                    mPrinter = AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
//        mResourceLockServiceManager = new ResourceLockServiceManager(TKOApplication.getInstance()) {
//            @Override
//            public void onConnected(IAppLockService appLockService) {
//                mAppLockService = appLockService;
//            }
//
//            @Override
//            public void onConnectFailed(String reason) {
//                mAppLockService = null;
//            }
//
//            @Override
//            public void onDisconnected() {
//                mAppLockService = null;
//            }
//        };
    }

    public static PrintHelper getInstance() {
        if (instance == null) {
            synchronized (PrintHelper.class) {
                if (instance == null) {
                    instance = new PrintHelper();
                }
            }
        }
        return instance;
    }

    public static void init() {
        getInstance();
    }

//    public void releaseResourceLock() {
//        mResourceLockServiceManager.unbindService();
//    }

//    void initDeviceConnect() {
//        mDeviceConnect = new DeviceServiceAIDLConnect(mContext) {
//            @Override
//            public void onConnected() {
//                mPrinter = mDeviceConnect.getPrinter();
//
//                mServiceManager = mDeviceConnect.getServiceManager();
//            }
//
//            @Override
//            public void onConnectFailed(String reason) {
//
//            }
//
//            @Override
//            public void onDisconnected() {
//
//            }
//        };
//        mDeviceConnect.bindService();
//    }

    public int getPrinterState() {
        try {
            return mPrinter.getPrinterState();
        } catch (RemoteException e) {
            e.printStackTrace();
            return PrinterConstant.PrinterState.PRINTER_STATE_UNKNOWN;
        }
    }

    public String getErrMsg(int errCode) {
        switch (errCode) {
            case 1:
                return "缺纸,请装入打印纸后重试";
            case 2:
                return "设备过热,请稍等";
            case 3:
                return "未知错误";
            case 4:
                return "设备未打开";
            case 5:
                return "设备忙";
            case 6:
                return "设备忙,请稍等";
            case 7:
                return "打印位图错误";
            case 8:
                return "打印条码错误";
            case 9:
                return "机器缺纸,请装入打印纸后重试";
//			return "参数错误";
            case 10:
                return "打印文本错误";
            case 11:
                return "mac校验错";
            default:
                return "其他错误";
        }
    }


    /**
     * 文本转成自定格式，到非文本数据位置
     *
     * @param printData //     * @param printItemObjs
     * @return 当前字符串位置
     */
    private int strToPrintItems(String printData) {
        List<PrintItemObj> printItemObjs = new ArrayList<PrintItemObj>();
        int fontType = PrinterConstant.FontType.FONTTYPE_N;
        int fontScale = PrinterConstant.FontScale.FONTSCALE_W_H;
        int lineHeight = 6;
        PrintItemObj.ALIGN align = PrintItemObj.ALIGN.LEFT;
        String[] datas = printData.split("\n");
        for (int i = 0; i < datas.length; i++) {
            int index = datas[i].indexOf("!NLFONT ");
            if (index != -1) {
                String tmp = datas[i].substring(index + "!NLFONT ".length()).trim();
                LogUtils.e("字体大小：" + tmp);
                switch (Integer.valueOf(tmp)) {
                    case 4:
                        fontType = PrinterConstant.FontType.FONTTYPE_S;
                        fontScale = PrinterConstant.FontScale.FONTSCALE_W_H;
                        break;
                    case 8:
                        fontType = PrinterConstant.FontType.FONTTYPE_N;
                        fontScale = PrinterConstant.FontScale.FONTSCALE_W_H;
                        break;
                    case 16:
                        fontType = PrinterConstant.FontType.FONTTYPE_S;
                        fontScale = PrinterConstant.FontScale.FONTSCALE_DW_DH;
                        break;
                    case 24:
                        fontType = PrinterConstant.FontType.FONTTYPE_N;
                        fontScale = PrinterConstant.FontScale.FONTSCALE_DW_DH;
                        break;
                    default:
                        break;
                }
            }
            index = datas[i].indexOf("!yspace ");
            if (index != -1) {
                String tmp = datas[i].substring(index + "!yspace ".length()).trim();
                lineHeight = Integer.valueOf(tmp);
            }
            index = datas[i].indexOf("!image!");
            if (index != -1) {
                try {
                    int end = datas[i].substring(index).indexOf("!r!n");
                    if (end != -1) {
                        end += index;
                        if (printItemObjs.size() != 0) {
                            mPrinter.printText(printItemObjs);
                            printItemObjs.clear();
                        }
                        String path = datas[i].substring(index + "!image!".length(), end);
                        FileInputStream fis = new FileInputStream(path);
                        File file = new File(path);
                        if (file.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeStream(fis);
                            mPrinter.printImage(PrinterConstant.Align.ALIGN_CENTER, bitmap);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            index = datas[i].indexOf("*text ");
            if (index != -1) {
                String text = datas[i].substring(index + "*text ".length());
                String position = text.substring(0, 2);
                if (position.equals("l ")) {
                    align = PrintItemObj.ALIGN.LEFT;
                } else if (position.equals("c ")) {
                    align = PrintItemObj.ALIGN.CENTER;
                } else if (position.equals("r ")) {
                    align = PrintItemObj.ALIGN.RIGHT;
                }
                text = text.substring(2);
                LogUtils.d(text);
                if (text == null) {
                    text = "";
                }
                printItemObjs.add(new PrintItemObj(text, fontScale, fontType, align, false, lineHeight));
            }
        }
        try {
            mPrinter.printText(printItemObjs);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return datas.length;
    }

    public void printScript(String printData, AidlPrinterListener printListener, Handler handler, Activity activity) {
        LogUtils.d("打印数据---" + printData);
        try {
            int printerState = mPrinter.getPrinterState();
            if (printerState == PrinterConstant.PrinterState.PRINTER_STATE_NORMAL) {
                mPrinter.open();
                strToPrintItems(printData);
                mPrinter.start(printListener);
                mPrinter.paperSkip(2);
            } else {
                Message message = Message.obtain();
                message.what = printerState;
                handler.sendMessage(message);
            }
        } catch (RemoteException e1) {
            e1.printStackTrace();
            Message message = Message.obtain();
            message.what = 3;
            handler.sendMessage(message);
            DialogFactory.dismissAlert(activity);
        }
    }
//
//    private Handler handler = new Handler();
//
//    /**
//     * 统计页面不需要打印两联
//     *
//     * @param printOrderModel
//     * @param modeType        1：订单  2：统计
//     */
//    public void printOrder(final Object printOrderModel, final int modeType) {
//        mActivity = context;

    //蓝牙打印
//        if (SharePreferencesUtils.getInstance().getInt(AppConstants.KEY_PRINTER_CURRENT_USE) == 2) {
//            String strMode = "";
//            switch (modeType) {
//                case 1:
//                    strMode = SharePreferencesUtils.getInstance().getString(AppConstants.KEY_PRINT_MODEL_BT);
//                    if (TextUtils.isEmpty(strMode)) {
//                        strMode = "PRINT_ORDER_INFO_NORMAL_BT";
//                    }
//                    break;
//                case 2:
//                    strMode = "PRINT_REPORT_INFO_NORMAL_BT";
//                    break;
//                default:
//                    break;
//            }

//            String strMode = SharePreferencesUtils.getInstance().getString(AppConstants.KEY_PRINT_MODEL_BT);
//            if (TextUtils.isEmpty(strMode)) {
//                strMode = "PRINT_ORDER_INFO_NORMAL_BT";
//            }

//            final byte[] printString = NormalPrintUtils.getPrintDataBT(printOrderModel, strMode);
//            int count = SharePreferencesUtils.getInstance().getInt(AppConstants.KEY_PRINT_NUM_BT) == 2 ? 2 : 1;
//            PrintQueue.getQueue(TKOApplication.getInstance()).add(printString);
//            if (count == 2 && modeType != 2) { // 两联打印
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        PrintQueue.getQueue(TKOApplication.getInstance()).add(printString);
//                    }
//                }, 1500);
//            }
//            return;
//        }
//        if (mPrinter == null) {
//            LogUtils.d("打印机链接出错！");
//            return;
//        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    printDataWithLock(printOrderModel, modeType);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }

//    /**
//     * 打印第二联
//     *
//     * @param printOrderModel
//     */
//    void printOrderSecond(final Object printOrderModel, final Context context, final int modeType) {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                isFrist = true;//第二联已经打印
//                printOrder(printOrderModel, modeType);
//            }
//        }, 1500);
//    }

//    private void printSecond(final PrintObjectMessage msgTask) {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                isFrist = true;//第二联已经打印
//                try {
//                    print(msgTask);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 1500);
//    }


    private synchronized void printDataWithLock(final Object printOrderModel, final int modeType) throws RemoteException {

        // 兼容Launcher 没锁也要能打印
//        try {
//            if (mAppLockService == null) {
//                LogUtils.d("资源锁获取失败");
//            }
//            while (mAppLockService.isLocked()) {
//                try {
//                    LogUtils.d("【外卖】等待释放资源锁。。");
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            LogUtils.d("【外卖】添加资源锁锁定。。");
//            mAppLockService.addLock();
//        } catch (Exception e) {
//        }
//
//        PrintObjectMessage printerBean = new PrintObjectMessage();
//        int printNum = SharePreferencesUtils.getInstance().getInt(AppConstants.KEY_PRINT_NUM_LOCAL);
//        int printSize = SharePreferencesUtils.getInstance().getInt(AppConstants.KEY_PRINT_FONT_LOCAL);
//        String strMode = SharePreferencesUtils.getInstance().getString(AppConstants.KEY_PRINT_MODEL_LOCAL);
//        switch (modeType) {
//            case 2://统计页面只需要打印一联
//                printerBean.setNum(1);
//                break;
//            default:
//                printerBean.setNum(printNum);
//                break;
//        }
//        printerBean.setModel(printOrderModel);
//        printerBean.setSize(printSize);
//        printerBean.setStrModel(strMode);
//        if (!PrintQueueManager.getInstance().isExecuting()) {
//            PrintQueueManager.getInstance().addPrintTask(printerBean);
//        } else {
//            while (PrintQueueManager.getInstance().isExecuting()) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            PrintQueueManager.getInstance().addPrintTask(printerBean);
//        }
    }

    //    public void print(final PrintObjectMessage msgTask) throws RemoteException {
////        final Object printOrderModel = msgTask.getModel();
//        PrintQueueManager.getInstance().setPrintStart(true);
//        String printString = PrintHelper.getInstance().getPrintInfoString(msgTask);
//        int status = 0;
//        status = PrintHelper.getInstance().getPrinterState();
//        if (TextUtils.isEmpty(printString)) {
//            LogUtils.d("打印数据为空！");
//            return;
//        }
//
//        int statueTemp = 0;
//        switch (status) {
//            case 0: //PrinterConstant.PrinterState.PRINTER_STATE_NORMAL
//                PrintHelper.getInstance().printScript(printString, new AidlPrinterListener.Stub() {
//                    @Override
//                    public void onPrintFinish() throws RemoteException {
//                        LogUtils.i("[订单]打印结束");
//                        LogUtils.d("【外卖】释放资源锁锁定。。");
//                        if ((SharePreferencesUtils.getInstance().getInt(AppConstants.KEY_PRINTER_CURRENT_USE) == 1 && msgTask.getNum() == 2)) {
//                            if (!isFrist) {//打印第二联
////                                printOrderSecond(printOrderModel, mActivity, modeType);
//                                printSecond(msgTask);
//                            } else {
//                                isFrist = false;//表明第二联已经打印完成
//                                PrintQueueManager.getInstance().setPrintEnd(false);
//                            }
//                        } else {// 只有一联
//                            PrintQueueManager.getInstance().setPrintEnd(false);
//                        }
//                        if (mAppLockService != null) {
//                            LogUtils.d("【外卖】释放资源锁锁定。。");
//                            mAppLockService.releaseLock();
//                        }
//                    }
//
//                    @Override
//                    public void onError(final int errorCode) throws RemoteException {
//                        LogUtils.e("[订单]打印错误码:" + errorCode);
//                        LogUtils.e("[订单]打印错误信息:" + PrintHelper.getInstance().getErrMsg(errorCode));
//                        mHandler.sendMessage(mHandler.obtainMessage(1, PrintHelper.getInstance().getErrMsg(errorCode)));
//                        LogUtils.e(PrintHelper.getInstance().getErrMsg(errorCode));
//                        if (mAppLockService != null) {
//                            mAppLockService.releaseLock();
//                            LogUtils.d("【外卖】释放资源锁锁定。。");
//                        }
//                        PrintQueueManager.getInstance().setPrintEnd(false);
//                    }
//                });
//                break;
//            case 6: //PrinterConstant.PrinterState.PRINTER_BUSY
//                statueTemp = 1;
//                mHandler.sendMessage(mHandler.obtainMessage(1, "打印机忙"));
//                break;
//            case 1: //PrinterConstant.PrinterState.PRINTER_STATE_NOPAPER
//                statueTemp = 1;
//                mHandler.sendMessage(mHandler.obtainMessage(1, "打印机缺纸"));
//                break;
//            case 2: //PrinterConstant.PrinterState.PRINTER_STATE_HIGHTEMP
//                statueTemp = 1;
//                mHandler.sendMessage(mHandler.obtainMessage(1, "打印机温度过高"));
//                break;
//            default: //
//                statueTemp = 1;
//                mHandler.sendMessage(mHandler.obtainMessage(1, "未知打印错误"));
//                break;
//        }
//
//        if (statueTemp == 1) {
//            PrintQueueManager.getInstance().setPrintEnd(false);
//            if (mAppLockService != null) {
//                LogUtils.d("【外卖】释放资源锁锁定。。");
//                mAppLockService.releaseLock();
//            }
//        }
//    }
//
//    @NonNull
//    private String getPrintInfoString(PrintObjectMessage msgTask) {
//        Object printModel = msgTask.getModel();
//        if (printModel == null) {
//            return "";
//        }
//        // 打印订单
//        if (printModel instanceof PrintOrderModel) {
//            PrintOrderModel printOrderModel = (PrintOrderModel) printModel;
////            String strModePrex = SharePreferencesUtils.getInstance().getString(AppConstants.KEY_PRINT_MODEL_LOCAL);
//            String strModePrex = msgTask.getStrModel();
//            if (TextUtils.isEmpty(strModePrex)) {
//                strModePrex = "PRINT_ORDER_INFO_NORMAL";
//            }
//            StringBuffer printBuffer = new StringBuffer();
//
//            String platforName = printOrderModel.getPlatforName();
//            if (AppConstants.WM_CHANNEL_E_AUTH_PAY.equals(platforName)) {
//                printOrderModel.setPlatforName("饿了么");
//            } else if (AppConstants.WM_CHANNEL_BAIDU_ERP.equals(platforName)) {
//                printOrderModel.setPlatforName("百度外卖");
//            } else if (AppConstants.WM_CHANNEL_MEITUAN_ERP.equals(platforName)) {
//                printOrderModel.setPlatforName("美团外卖");
//            }
//
//            String printTop = NormalPrintUtils.getPrintDataEndLine(strModePrex + "_TOP", printOrderModel);
//            printBuffer.append(printTop);
//            if (printOrderModel.getFoodItem() != null) {
//                ArrayList<ProductItem> foodlist = printOrderModel.getFoodItem();
//                ProductItem productItemTemp = new ProductItem();
//                for (int i = 0, size = foodlist.size(); i < size; i++) {
////                productItemTemp = foodlist.get(i);
//                    if (foodlist.get(i).getFoodName().length() > 8) {
//                        String name = foodlist.get(i).getFoodName();
//                        productItemTemp.setFoodName(name.substring(0, 8));
//                        productItemTemp.setFoodNum("");
//                        productItemTemp.setFoodPrice("");
//                        String string_temp_1 = NormalPrintUtils.getPrintDataEndLine(strModePrex + "_MIDDLE", productItemTemp);
//                        printBuffer.append(string_temp_1);
//
//                        productItemTemp.setFoodName(name.substring(8));
//                        productItemTemp.setFoodNum(StringUtil.fill("x" + foodlist.get(i).getFoodNum(), " ", 18 - StringUtil.length(productItemTemp.getFoodName()), true));
//                        productItemTemp.setFoodPrice(foodlist.get(i).getFoodPrice());
//
//                        String string_temp_2 = NormalPrintUtils.getPrintDataEndLine(strModePrex + "_MIDDLE", productItemTemp);
//                        printBuffer.append(string_temp_2);
//                    } else {
//                        productItemTemp.setFoodName(foodlist.get(i).getFoodName());
//                        productItemTemp.setFoodNum(StringUtil.fill("x" + foodlist.get(i).getFoodNum(), " ", 18 - StringUtil.length(productItemTemp.getFoodName()), true));
//                        productItemTemp.setFoodPrice(foodlist.get(i).getFoodPrice());
//                        String printMiddle = NormalPrintUtils.getPrintDataEndLine(strModePrex + "_MIDDLE", productItemTemp);
//                        printBuffer.append(printMiddle);
//                    }
//
//                }
//            }
//
//            String box_price = printOrderModel.getBoxPrice();
//            String shippingFee = printOrderModel.getShippingFee();
//            String originalPrice = printOrderModel.getOriginalPrice();
//            String total = printOrderModel.getTotal();
//            String phone = printOrderModel.getPhone();
//
//            printOrderModel.setBoxPrice(StringUtil.fill(box_price, " ", 11, true));
//            printOrderModel.setShippingFee(StringUtil.fill(shippingFee, " ", 9, true));
//            printOrderModel.setOriginalPrice(StringUtil.fill(originalPrice, " ", 18, true));
//            printOrderModel.setTotal(StringUtil.fill(total, " ", 18, true));
//            printOrderModel.setPhone(StringUtil.fill(phone, " ", 18 - StringUtil.length(printOrderModel.getName()), true));
//
//            String printBottom = NormalPrintUtils.getPrintDataEndLine(strModePrex + "_BOTTOM", printOrderModel);
//            printBuffer.append(printBottom);
//            return printBuffer.toString();
//        } else if (printModel instanceof PrintReportModel) {
//            PrintReportModel printReportModel = (PrintReportModel) printModel;
//            String strModePrex = "";
//            String fontSize = SharePreferencesUtils.getInstance().getString(AppConstants.LOCAL_PRINT_FONT_SIZE);
//            switch (fontSize) {
//                case "normal_local":
//                    strModePrex = "PRINT_REPORT_INFO_NORMAL";
//                    break;
//                case "small_local":
//                    strModePrex = "PRINT_REPORT_INFO_SMALL";
//                    break;
//                case "big_local":
//                    strModePrex = "PRINT_REPORT_INFO_BIG";
//                    break;
//            }
//            if (TextUtils.isEmpty(strModePrex)) {
//                strModePrex = "PRINT_REPORT_INFO_NORMAL";
//            }
//            return NormalPrintUtils.getPrintDataEndLine(strModePrex, printReportModel);
//        } else if (printModel instanceof PrintCheckIncomeModel) {
//            PrintCheckIncomeModel printCheckIncomeModel = (PrintCheckIncomeModel) printModel;
//            String strModePrex = "PRINT_CHECK_INCOME_INFO_NOMAL";
//            PrintCheckIncomeModel pcim = new PrintCheckIncomeModel();
//            StringBuffer printBuffer = new StringBuffer();
//            pcim.setTime(printCheckIncomeModel.getTime());
//            pcim.setPtName(printCheckIncomeModel.getPtName());
//            pcim.setShopName(printCheckIncomeModel.getShopName());
//            int str_all = StringUtil.length("总计");
//            String totalOriginalPrice = printCheckIncomeModel.getTotalOriginalPrice();
//            String str = StringUtil.fill(totalOriginalPrice + "", " ", 13 - str_all, true);
//            pcim.setTotalOriginalPrice(str);
//            pcim.setTotalShopReceptPrice(printCheckIncomeModel.getTotalShopReceptPrice());
//            String printTop = NormalPrintUtils.getPrintDataEndLine(strModePrex + "_TOP", pcim);
//            printBuffer.append(printTop);
//
//            ArrayList<DailyPtInfo> dayPtInfo = printCheckIncomeModel.getDayPtInfo();
//            DailyPtInfo dailyPtInfo = new DailyPtInfo();
//            if (!ListUtil.isEmpty(dayPtInfo)) {
//                for (int i = 0; i < dayPtInfo.size(); i++) {
//                    dailyPtInfo.setDaySqe(dayPtInfo.get(i).getDaySqe());
//                    String originalPrice = dayPtInfo.get(i).getOriginalPrice();
//                    int len = 13 - (StringUtil.length(dayPtInfo.get(i).getDaySqe()));
//                    dailyPtInfo.setOriginalPrice(StringUtil.fill(originalPrice + "", " ", len, true));
//                    dailyPtInfo.setShopReceptPrice(dayPtInfo.get(i).getShopReceptPrice());
//                    String printMiddle = NormalPrintUtils.getPrintDataEndLine(strModePrex + "_MIDDLE", dailyPtInfo);
//                    printBuffer.append(printMiddle);
//                }
//            }
//            printCheckIncomeModel.setPrintTime(DateUtil.formatDate(new Date(), DateUtil.FULL_DATE_TIME_FORMAT_1));
//            String printBottom = NormalPrintUtils.getPrintDataEndLine(strModePrex + "_BOTTOM", printCheckIncomeModel);
//            printBuffer.append(printBottom);
//            return printBuffer.toString();
//        }
//        return "";
//    }
    public String getPrintInfoString(String modelName, Object printModel) {
        return PrintUtils.getPrintDataEndLine(modelName, printModel);
    }

    /**
     * 通用打印
     *
     * @param printModelName
     * @param printModel     打印数据
     * @param printListener  打印监听
     * @param handler        错误回调
     */
    public void print(Object printModel, AidlPrinterListener printListener, Handler handler, Activity activity, String printModelName) {
        String printInfoString = getPrintInfoString(printModelName, printModel);
        printScript(printInfoString, printListener, handler, activity);
    }
}
