package com.nld.cloudpos.bankline.fragment.cash;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.printer.AidlPrinter;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.starpos.banktrade.utils.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by L on 2017/2/16.
 *
 * @描述 打印服务。
 */

public abstract class PrintFragment extends BaseFragment {

    private final static int PRINT_STATE_NORMAL = 0x00;//正常
    private final static int PRINT_STATE_NO_PAPER = 0x01;//缺纸
    private final static int PRINT_STATE_HOT = 0x02;//高温
    private final static int PRINT_STATE_UNKNOW = 0x03;//未知
    private AidlDeviceService mDeviceService;
    private AidlPrinter mPrinter;
    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BankApplication.mDeviceService = mDeviceService = AidlDeviceService.Stub.asInterface(iBinder);
            isBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mOnPrintListener.onPrintFailed(R.string.print_link_service_failed+"");
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    String cause = (String) msg.obj;
                    mOnPrintListener.onPrintFailed(R.string.print_error + cause);
                    break;
                case 1002:
                    mOnPrintListener.onPrintSucceed();
                    break;
                default:
                    break;
            }
        }
    };
    private boolean isBinded;
    private List<PrintItemObj> mPrintItemObjList = new ArrayList<PrintItemObj>();
    private OnPrintListener mOnPrintListener;

    protected void startPrint(List<PrintItemObj> content, OnPrintListener onPrintListener) {
        mPrintItemObjList = content;
        mOnPrintListener = onPrintListener;
        mDeviceService = BankApplication.mDeviceService;
        if (mDeviceService == null) {
            bindService();
        }
        if (mPrinter == null) {
            try {
                mPrinter = AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
            } catch (RemoteException e) {
                mOnPrintListener.onPrintFailed(R.string.print_link_machine_failed+"");
                e.printStackTrace();
            }
        }
        toPrint();
    }

    /**
     * 绑定打印服务。
     */
    private void bindService() {
        Intent intent = new Intent();
        intent.setAction(Constant.USDK_NAME_ACTION);
        boolean flag = mActivity.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        if (!flag) {
            mOnPrintListener.onPrintFailed(R.string.print_link_service_failed+"");
        }
    }

    /**
     * 开始打印。
     */
    private void toPrint() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mPrinter.printText(mPrintItemObjList);
                } catch (RemoteException e) {
                    mOnPrintListener.onPrintFailed(R.string.print_link_machine_failed+"");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConn != null && isBinded) {
            mActivity.unbindService(mConn);
        }
        mConn = null;
    }

    public interface OnPrintListener {
        /**
         * 打印成功。
         * @param msg
         */
        void onPrintFailed(String msg);

        /**
         * 打印失败。
         */
        void onPrintSucceed();
    }

}
