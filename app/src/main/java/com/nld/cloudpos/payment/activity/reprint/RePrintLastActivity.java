/**
 *
 */
package com.nld.cloudpos.payment.activity.reprint;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
import com.nld.cloudpos.util.DialogFactory;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.ParamsUtil;

import java.util.List;
import java.util.Map;

import common.Utility;

/**
 * 重打印上一笔
 *
 * @author lin 2015年10月13日
 */
public class RePrintLastActivity extends BaseAbstractActivity {

    public final static int PRINT_STATE_NORMAL = 0x00;// 正常
    public final static int PRINT_STATE_NO_PAPER = 0x01;// 缺纸
    public final static int PRINT_STATE_HOT = 0x02;// 高温
    public final static int PRINT_STATE_UNKNOW = 0x03;// 未知

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
    private Button btn_confirm;

    private TransRecordDao transRecordDao;
    private Map<String, String> lastTransMap;
    /**
     * 标志是否已经打印过，如果打印过，则不在打印
     */
    private boolean isPrinted = false;
    private int printtimes = 1;

    @Override
    public int contentViewSourceID() {
        return R.layout.reprint_result;
    }

    public Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String tip = (String) msg.obj;
            if (msg.what != 102 && msg.what != 202) {
                showTip(tip);
            }
            switch (msg.what) {
                case 100:// 打印结束
//				showSecondView();

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
                case 101:// 打印异常
                    showErrorView(tip);
                    break;
                case 102:// 打印第一联等待装纸
                    DialogFactory.showMessage(mActivity, "打印机无纸", "请放入打印纸后重新开始",
                            "继续打印", new OnClickListener() {

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
                case 200:// 打印结束
                    showFirstView();
                    break;
                case 201:// 打印异常
                    showErrorView(tip);
                    break;
                case 202:// 等待装纸
                    DialogFactory.showMessage(mActivity, "打印机无纸", "请放入打印纸后重新开始",
                            "继续打印", new OnClickListener() {

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
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    public void initView() {
        transRecordDao = new TransRecordDaoImpl();
        mIcon = (ImageView) findViewById(R.id.print_result_icon);
        mResultTv = (TextView) findViewById(R.id.print_result_tv);
        mReasonTv = (TextView) findViewById(R.id.print_reason_tv);
        mSecondBtn = (Button) findViewById(R.id.print_second_btn);
        showFirstView();
        setTitle("重打印上一笔");
        findViewById(R.id.btn_confirm).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                NldPaymentActivityManager.getActivityManager()
                        .removeAllActivityExceptOne(LauncherActivity.class);
            }
        });
        setTopDefaultReturn();
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
                printer = AidlPrinter.Stub.asInterface(mDeviceService
                        .getPrinter());
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
                    lastTransMap = Utility.transformToMap(transRecordDao
                            .getLastTransRecord());
                    lastTransMap.put("isReprints", "true");
                    lastTransMap.put("isSecond", "true");
                    List<PrintItemObj> list = PrintDev.getPrintItemObjs(
                            lastTransMap, mContext);
                    if (null == list || list.size() <= 0) {
                        logger.debug("获取打印列表失败");
                        Message msg = new Message();
                        msg.obj = "打印内容获取异常";
                        msg.what = 101;
                        mHandler.sendMessage(msg);
                        DialogFactory.dismissAlert(mActivity);
                        return;
                    }
                    try {
                        printer.printText(list
//                        new AidlPrinterListener.Stub() {
//
//                            @Override
//                            public void onPrintFinish() throws RemoteException {
//                                Message msg = new Message();
//                                msg.obj = "打印完成";
//                                msg.what = 200;
//                                mHandler.sendMessage(msg);
//                                DialogFactory.dismissAlert(mActivity);
//                            }
//
//                            @Override
//                            public void onError(int state)
//                                    throws RemoteException {
//                                Message msg = new Message();
//                                logger.debug("打印出错：" + state);
//                                printTime = 0;
//                                String tip = "";
//                                switch (state) {
//                                    case PRINT_STATE_NORMAL:
//                                        tip = "正常";
//                                        break;
//                                    case PRINT_STATE_NO_PAPER:
//                                        msg.what = 202;
//                                        tip = "无纸";
//                                        break;
//                                    case PRINT_STATE_HOT:
//                                        msg.what = 201;
//                                        tip = "过热";
//                                        break;
//                                    case PRINT_STATE_UNKNOW:
//                                        msg.what = 201;
//                                        tip = "未知";
//                                        break;
//                                    default:
//                                        msg.what = 201;
//                                        tip = "未知";
//                                        break;
//                                }
//                                msg.obj = "打印出错,原因：" + tip;
//                                mHandler.sendMessage(msg);
//                                DialogFactory.dismissAlert(mActivity);
//                            }
//                        }
                        );
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    super.run();
                }

            }.start();
        }
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {

        if (null == mDeviceService) {
            bindService();
            return;
        }
        if (null == printer) {
            try {
                printer = AidlPrinter.Stub.asInterface(mDeviceService
                        .getPrinter());
            } catch (RemoteException e) {
                logger.error("获取打印机失败", e);
                e.printStackTrace();
            }
        }
        DialogFactory.showLoadingTip(15000, mActivity, "正在打印凭条，请稍等", new DialogTimeout());
        new Thread() {

            @Override
            public void run() {
                lastTransMap = Utility.transformToMap(transRecordDao
                        .getLastTransRecord());
                lastTransMap.put("isReprints", "true");
                List<PrintItemObj> list = PrintDev.getPrintItemObjs(
                        lastTransMap, mContext);


                if (null == list) {
                    logger.debug("获取打印列表失败");
                    Message msg = new Message();
                    msg.obj = "打印内容获取异常";
                    msg.what = 101;
                    mHandler.sendMessage(msg);
                    DialogFactory.dismissAlert(mActivity);
                    return;
                }
                try {
                    printer.printText(list
//                    new AidlPrinterListener.Stub() {
//
//                        @Override
//                        public void onPrintFinish() throws RemoteException {
//                            isPrinted = true;
//                            Message msg = new Message();
//                            msg.obj = "打印完成";
//                            msg.what = 100;
//                            mHandler.sendMessage(msg);
//                            DialogFactory.dismissAlert(mActivity);
//                        }
//
//                        @Override
//                        public void onError(int state) throws RemoteException {
//                            isPrinted = false;
//                            Message msg = new Message();
//                            String tip = "";
//                            switch (state) {
//                                case PRINT_STATE_NORMAL:
//                                    tip = "正常";
//                                    break;
//                                case PRINT_STATE_NO_PAPER:
//                                    msg.what = 102;
//                                    tip = "无纸";
//                                    break;
//                                case PRINT_STATE_HOT:
//                                    msg.what = 101;
//                                    tip = "过热";
//                                    break;
//                                case PRINT_STATE_UNKNOW:
//                                    msg.what = 101;
//                                    tip = "未知";
//                                    break;
//                                default:
//                                    msg.what = 101;
//                                    tip = "未知";
//                                    break;
//                            }
//                            msg.obj = "打印出错,原因：" + tip;
//                            mHandler.sendMessage(msg);
//                            DialogFactory.dismissAlert(mActivity);
//                        }
//                    }
                    );
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                super.run();
            }

        }.start();

    }

    @Override
    public void onServiceBindFaild() {
        // TODO Auto-generated method stub

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

    @Override
    public boolean saveValue() {
        // TODO Auto-generated method stub
        return false;
    }


    public class DialogTimeout implements IDialogTimeoutListener {

        @Override
        public void onDialogTimeout(String tip) {
            showErrorView("打印异常");
        }
        
    }
}
