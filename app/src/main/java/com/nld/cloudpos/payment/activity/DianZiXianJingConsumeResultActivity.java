package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.printer.AidlPrinter;
import com.nld.cloudpos.aidl.printer.AidlPrinterListener;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.dev.PrintDev;
import com.nld.cloudpos.payment.interfaces.IDialogTimeoutListener;
import com.nld.cloudpos.util.DialogFactory;
import com.nld.starpos.banktrade.utils.Cache;

import java.util.List;

/**
 * Created by jiangrenming on 2017/12/15.
 */

public class DianZiXianJingConsumeResultActivity extends BaseActivity {

    private String paymoney = null;
    private String cardno = null;
    private String title = "";


    private AidlPrinter printer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.consume_result);
        Intent intent = this.getIntent();
        cardno = intent.getStringExtra("cardno");
        paymoney = intent.getStringExtra("paymoney");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {

        DialogFactory.showLoadingTip(15000, mActivity, "正在打印凭条，请稍等", new DialogTimeout());
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DialogFactory.dismissAlert(mActivity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 确认按钮点击
    public void confirm(View v) {
        NldPaymentActivityManager.getActivityManager().removeAllActivityExceptOne(LauncherActivity.class);
    }

    @Override
    public void goback(View v) {
        NldPaymentActivityManager.getActivityManager().removeAllActivityExceptOne(LauncherActivity.class);
    }

    @Override
    public void setTopTitle() {
        TextView topTitle = (TextView) super.findViewById(R.id.top_title);
        String transCode = Cache.getInstance().getTransCode();

        title = "普通消费";

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
                    List<PrintItemObj> list = PrintDev.getPrintData(false, DianZiXianJingConsumeResultActivity.this, Cache.getInstance().getResultMap(), title);
                    try {
                        printer.printText(list);
                        new AidlPrinterListener.Stub() {
                           @Override
                            public void onPrintFinish() throws RemoteException {
                                Message msg = new Message();
                                msg.obj = "打印完成";
                                mHandler.sendMessage(msg);
                                DialogFactory.dismissAlert(mActivity);
                            }

                            @Override
                            public void onError(int arg0) throws RemoteException {
                                Message msg = new Message();
                                msg.obj = "打印出错,错误码" + arg0;
                                mHandler.sendMessage(msg);
                                DialogFactory.dismissAlert(mActivity);
                            }
                        };
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

    public class DialogTimeout implements IDialogTimeoutListener {

        @Override
        public void onDialogTimeout(String tip) {
            showTips("打印异常");
        }

    }
}
