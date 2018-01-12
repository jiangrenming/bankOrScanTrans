package com.nld.cloudpos.payment.activity.preauth;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * 消费结果页
 *
 * @author Tianxiaobo
 */
public class PreAuthResultActivity extends BaseActivity {
    Logger logger = Logger.getLogger(PreAuthResultActivity.class);

    private String paymoney = null;
    private String cardno = null;

    private ProgressDialog printDialog = null;
    String transCode = "";
    private AidlPrinter printer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.consume_result);
        Intent intent = this.getIntent();
        cardno = intent.getStringExtra("cardno");
        paymoney = intent.getStringExtra("paymoney");
        printDialog = new ProgressDialog(this);
        logger.debug("预授权结果页启动");
    }

    @Override
    protected void onStop() {
        super.onStop();
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
    protected void onPause() {
        super.onPause();
        if (printDialog.isShowing()) {
            printDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 确认按钮点击
    public void confirm(View v) {
        NldPaymentActivityManager.getActivityManager()
                .removeAllActivityExceptOne(LauncherActivity.class);
    }

    @Override
    public void goback(View v) {
        NldPaymentActivityManager.getActivityManager().removeAllActivityExceptOne(LauncherActivity.class);
    }

    @Override
    public void setTopTitle() {
        TextView topTitle = (TextView) super.findViewById(R.id.top_title);
        transCode = Cache.getInstance().getTransCode();
        if (transCode.equals(TransConstans.TRANS_CODE_PRE)) {
            topTitle.setText("预授权");
        } else if (transCode.equals(TransConstans.TRANS_CODE_PRE_COMPLET)) {
            topTitle.setText("预授权完成");
        } else if (transCode.equals(TransConstans.TRANS_CODE_PRE_CX)) {
            topTitle.setText("预授权撤销");
        }
    }

    @Override
    public void onDeviceConnected(AidlDeviceService deviceManager) {

        logger.debug("预授权结果页服务绑定成功，启动打印");
        try {
            printer = AidlPrinter.Stub.asInterface(deviceManager.getPrinter());
            String name = "";
            if (transCode.equals(TransConstans.TRANS_CODE_PRE)) {
                name = "预授权(AUTH)";
            } else if (transCode.equals(TransConstans.TRANS_CODE_PRE_COMPLET)) {
                name = "预授权完成";
            } else if (transCode.equals(TransConstans.TRANS_CODE_PRE_CX)) {
                name = "预授权撤销";
            }
            List<PrintItemObj> list = PrintDev.getPrintData(false, PreAuthResultActivity.this, Cache.getInstance().getResultMap(), name);
            printer.printText(list
//            new AidlPrinterListener.Stub() {
//
//                @Override
//                public void onPrintFinish() throws RemoteException {
//                    showTips("打印完成");
//                    printDialog.dismiss();
//                }
//
//                @Override
//                public void onError(int arg0) throws RemoteException {
//                    showTips("打印出错,错误码" + arg0);
//                }
//            }
            );
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
//	/**
//	 * 获取打印数据
//	 * @param cardno
//	 * @param paymoney
//	 * @return
//	 * @createtor：Administrator
//	 * @date:2015-8-18 下午9:24:47
//	 */
//	public List<PrintItemObj> getPrintData(String cardno,String paymoney){
//		List<PrintItemObj> list = new ArrayList<PrintItemObj>();
//		list.add(new PrintItemObj("POS签购单", 16, true, ALIGN.CENTER));
//		list.add(new PrintItemObj("商户名称(MERCHANT NAME)"));
//		list.add(new PrintItemObj("一键测试商户", 16, true));
//		list.add(new PrintItemObj("商户编号(MERCHANTNO)"));
//		list.add(new PrintItemObj("  123456789012345"));
//		list.add(new PrintItemObj("终端号(TERMINAL)"));
//		list.add(new PrintItemObj("  87654321"));
//		list.add(new PrintItemObj("收单机构:拉卡拉"));
//		list.add(new PrintItemObj("卡号(CARD NO):"));
//		list.add(new PrintItemObj("  " + cardno));
//		list.add(new PrintItemObj("有效期(EXP DATE)："));
//		list.add(new PrintItemObj("交易类别(TRANS TYPE):"));
//		list.add(new PrintItemObj("预授权(AUTH)", 16, true));
//		list.add(new PrintItemObj("批次号(BATCH NO):000001"));
//		list.add(new PrintItemObj("凭证号(VOUCHER NO):000033"));
//		list.add(new PrintItemObj("参考号(REFER NO)"));
//		list.add(new PrintItemObj("外卡参考号(FRERER NO)"));
//		list.add(new PrintItemObj("日期/时间(DATE/TIME)"));
//		list.add(new PrintItemObj(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
//				.format(new Date())));
//		list.add(new PrintItemObj("交易金额(AMOUNT):"));
//		list.add(new PrintItemObj("RMB" + paymoney + "元", 16, true));
//		list.add(new PrintItemObj("备注(REFERENCE):" + paymoney + "元"));
//		list.add(new PrintItemObj("持卡人签名(CARDHOLDER SIGNATURE)\n\n"));
//		list.add(new PrintItemObj("本人确认以上交易"));
//		list.add(new PrintItemObj("同意将其计入本卡账户"));
//		list.add(new PrintItemObj(
//				"I ACKNOWLEDGE SATISFACTORY RECEIPT OF RELATIVE GOOGS/SERVICES"));
//		list.add(new PrintItemObj("--------------------------------"));
//		list.add(new PrintItemObj("YHK_N90SHV150812A"));
//		list.add(new PrintItemObj("客服务热线:400-766-6666"));
//		list.add(new PrintItemObj("------------商户存根------------"));
//		return list;
//	}

    @Override
    public void onDeviceConnectFaild() {

    }

}
