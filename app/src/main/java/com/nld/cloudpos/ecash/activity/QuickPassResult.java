package com.nld.cloudpos.ecash.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.printer.AidlPrinter;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.activity.BaseActivity;
import com.nld.cloudpos.payment.dev.PrintDev;
import com.nld.cloudpos.util.MyLog;
import com.nld.starpos.banktrade.utils.Cache;

import java.util.List;

public class QuickPassResult extends BaseActivity {

    private final MyLog logger = MyLog.getLogger(this.getClass());

    private String paymoney = null;
    private String cardno = null;
    private String title = "";

    private ProgressDialog printDialog = null;

    private AidlPrinter printer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.consume_result);
        Intent intent = this.getIntent();
        cardno = intent.getStringExtra("cardno");
        paymoney = intent.getStringExtra("paymoney");
        printDialog = new ProgressDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        printDialog.setMessage("正在打印凭条，请稍等");
        printDialog.show();
        printDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                return true;
            }
        });
    }

    @Override
    public void goback(View v) {
        NldPaymentActivityManager.getActivityManager()
                .removeAllActivityExceptOne(LauncherActivity.class);
    }

    // 确认按钮点击
    public void confirm(View v) {
        NldPaymentActivityManager.getActivityManager()
                .removeAllActivityExceptOne(LauncherActivity.class);
    }

    @Override
    public void setTopTitle() {
        TextView topTitle = (TextView) super.findViewById(R.id.top_title);
        String transCode = Cache.getInstance().getTransCode();
        title = "快速消费";
        topTitle.setText(title);
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String tip = (String) msg.obj;
            showTips(tip);
            super.handleMessage(msg);
        }
    };

    @Override
    public void onDeviceConnected(AidlDeviceService deviceManager) {
        try {
            printer = AidlPrinter.Stub.asInterface(deviceManager.getPrinter());
            new Thread() {
                @Override
                public void run() {
                    logger.log(MyLog.LogType.DEBUG, "快速支付打印结果：" + Cache.getInstance().getResultMap());
                    List<PrintItemObj> list = PrintDev.getPrintData(false,
                            QuickPassResult.this, Cache.getInstance()
                                    .getResultMap(), title);
                    try {
                        printer.printText(list
//                        new AidlPrinterListener.Stub() {
//                            @Override
//                            public void onPrintFinish() throws RemoteException {
//                                Message msg = new Message();
//                                msg.obj = "打印完成";
//                                mHandler.sendMessage(msg);
//                                printDialog.dismiss();
//                            }
//
//                            @Override
//                            public void onError(int arg0)
//                                    throws RemoteException {
//                                Message msg = new Message();
//                                msg.obj = "打印出错,错误码" + arg0;
//                                mHandler.sendMessage(msg);
//                                printDialog.dismiss();
//                            }
//                        }
                        );
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    super.run();
                }
            }.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceConnectFaild() {

    }
}
