package com.nld.cloudpos.bankline.fragment.cash;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.data.PrinterConstant;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.dev.PrintDev;
import com.nld.cloudpos.util.CommonContants;
import com.nld.cloudpos.util.DialogFactory;
import com.nld.cloudpos.util.ShareUtil;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.wxtrade.utils.ToastUtils;

import java.util.ArrayList;

import common.DateTimeUtil;

/**
 * Created by L on 2017/2/15.
 *
 * @描述 打印签购单页面。
 */

public class PrintReceiptFragment extends PrintFragment {

    private Button mBtnOk;

    public static void startSelfFragment(Context context) {
        startFragment(context, PrintReceiptFragment.class.getName(), R.string.print_receipt_title+"");
    }

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_print_receipt;
    }

    @Override
    public void doInitSubViews(View view) {
        DialogFactory.showDialog(mActivity, R.string.print_receipt_dialog);
        mBtnOk = queryViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NldPaymentActivityManager.getActivityManager().backToActivity(LauncherActivity.class);
            }
        });
    }

    @Override
    public void doInitData() {
        initData();

        final int fontType = PrinterConstant.FontType.FONTTYPE_S;
        startPrint(new ArrayList<PrintItemObj>() {{
            add(new PrintItemObj("POS签购单(现金)", 16, fontType, PrintItemObj.ALIGN.CENTER));
 //           add(new PrintItemObj("商户名称:" + GlobeData.merchantInfo.getMercNm(), 4));
//            add(new PrintItemObj("商户号:" + GlobeData.merchantInfo.getMerchantId() + " " + "终端号:" + GlobeData
//                    .merchantInfo.getTerminalNo(), 4));
            add(new PrintItemObj("操作员号:" + CommonContants.OPERATOR + " 收单机构:" + "新大陆", 4));
            add(new PrintItemObj("交易类别:" + "现金", 8, fontType));
            add(new PrintItemObj("批次号:" + Cache.getInstance().getPosBatch(), 4));
            add(new PrintItemObj("凭证号:" + Cache.getInstance().getSerialNo(), 4));
            add(new PrintItemObj("日期/时间:" + DateTimeUtil.timeLongToString("yy/MM/dd HH:mm:ss", System
                    .currentTimeMillis()), 4));
            add(new PrintItemObj("金额:" + Cache.getInstance().getTransMoney() + "RMB", 8, fontType));
            add(PrintDev.getPrintItemObj("\n\n\n\n\n\n"));
        }}, new OnPrintListener() {
            @Override
            public void onPrintFailed(String msg) {
                ToastUtils.showToast(msg);
                DialogFactory.dismissDialog();
            }

            @Override
            public void onPrintSucceed() {
                DialogFactory.dismissDialog();
            }
        });
    }

    private void initData() {
        String requestId = ShareUtil.getSerialNo();
        Cache.getInstance().setSerialNo(requestId);
    }

}
