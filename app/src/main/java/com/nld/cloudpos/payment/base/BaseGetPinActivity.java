package com.nld.cloudpos.payment.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.centerm.iso8583.util.DataConverter;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.pinpad.AidlPinpad;
import com.nld.cloudpos.aidl.pinpad.GetPinListener;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.view.BillItemView;
import com.nld.cloudpos.payment.view.gridpwdview.GridPasswordView;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.ParamsUtil;

import java.util.List;
import java.util.Map;

import common.DateTimeUtil;
import common.StringUtil;

public abstract class BaseGetPinActivity extends BaseAbstractActivity {
    private final static int TOAST_TIP = 1;

    private TextView mMoney, mCarno;
    private RelativeLayout mMoneyLl;
    private RelativeLayout mCarnoLl;
    private GridPasswordView mPswView;
    private Button mConfirmBtn;
    private MyConfirmClickListener mConfirmListener;
    private String mPsw;
    private AidlPinpad mDev;
    private GridView gview;
    private int mPwdLen = 0;
    private List<Map<String, Object>> data_list;
    private SimpleAdapter sim_adapter;
    private BillItemView bivBank;
    private BillItemView bivAccount;
    private TextView tvPwdInput;
    private TextView tvRight;
    private TextView tvActualAccount;


    @Override
    public int contentViewSourceID() {
        return R.layout.consume_getpin;
    }

    @Override
    public void initView() {
        setTopTitle("输入密码");
        setTopDefaultReturn();
        bivBank = (BillItemView) findViewById(R.id.biv_bank);
        bivAccount = (BillItemView) findViewById(R.id.biv_account);
        bivAccount.setTextContent("￥ " + Cache.getInstance().getTransMoney());
        tvRight = (TextView) findViewById(R.id.tv_union_right1);
        tvPwdInput = (TextView) findViewById(R.id.tv_pwd_input);

        tvActualAccount = (TextView) findViewById(R.id.actual_account);
        mMoney = (TextView) findViewById(R.id.paymoney);
        mMoneyLl = (RelativeLayout) findViewById(R.id.get_pin_money_ll);
        mCarno = (TextView) findViewById(R.id.paycardno);
        mCarnoLl = (RelativeLayout) findViewById(R.id.get_pin_carno_ll);
        mConfirmBtn = (Button) findViewById(R.id.get_pin_confirm_btn);
        mConfirmBtn.setVisibility(View.GONE);
        mPswView = (GridPasswordView) findViewById(R.id.gpv_normal);
        mConfirmListener = new MyConfirmClickListener();
        mConfirmBtn.setOnClickListener(mConfirmListener);
        mMoney.setText("￥ " + StringUtil.addComma(Cache.getInstance().getTransMoney()));
        mCarno.setText(StringUtil.splitBankCardNo(Cache.getInstance().getCardNo()));
        hideSystemKeyboard(mPswView);
        mPswView.setHoleViewClick(new OnClickListener() {

            @Override
            public void onClick(View v) {
                hideSystemKeyboard(v);
                if (!isCanClick(1000)) {
                    return;
                }
                getPin();
            }
        });
        initViewData();
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {

        String type = ParamsUtil.getInstance().getParam("pinpadType");
        LogUtils.d("服务绑定成功，准备开启密码键盘，类型=" + type);
        try {
            mDev = AidlPinpad.Stub.asInterface(service.getPinPad("0".equals(type) ? 0 : 1));
            getPin();
        } catch (RemoteException e) {
            LogUtils.e("密码键盘获取失败");
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceBindFaild() {
        LogUtils.i("服务绑定失败，重新绑定");
    }

    public void getPinPad(View view) {
        getPin();
    }

    /**
     * 调用密码键盘获取密码
     */
    private void getPin() {
        LogUtils.d("开始获取PINBLOCK");
        if (null != mDev) {
            final Bundle bd = new Bundle();
            String pikId = ParamsUtil.getInstance().getParam(Constant.FIELD_NEW_PIK_ID);
            int iPikId = Integer.parseInt(StringUtil.isEmpty(pikId) ? "0" : pikId);

            int inputType = 1;
            int minlength = 0;
            int maxlength = 6;
            String carno = Cache.getInstance().getCardNo();
            String pan = "";
            LogUtils.i("pan carno=" + carno);
            if (carno.contains("F")) {
                pan = carno.substring(0, carno.length() - 1);
            } else {
                pan = carno;
            }
            LogUtils.i("pan=" + pan);

            //TODO
            //int type=0x01;
            int type = getPinType();
            bd.putInt("wkeyid", iPikId);
            bd.putInt("keytype", type);
            bd.putByteArray("random", null);
            bd.putInt("inputtimes", inputType);
            bd.putInt("minlength", minlength);
            bd.putInt("maxlength", maxlength);
            bd.putString("pan", pan);

            new Thread() {
                @Override
                public void run() {
                    try {
                        LogUtils.i("开始输入PIN");
                        LogUtils.d("----------------------开始启动密码键盘-----------------------" + DateTimeUtil.formatMillisecondTimeSecond(System.currentTimeMillis()));
                        mDev.getPin(bd, new MyGetPinListener(), Constant.IS_SM);
                        LogUtils.d("----------------------启动密码键盘完成-----------------------" + DateTimeUtil.formatMillisecondTimeSecond(System.currentTimeMillis()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            LogUtils.e("密码键盘开启失败");
            showTip("密码键盘开启失败");
        }
    }

    @Override
    public boolean saveValue() {
        return true;
    }

    // 处理52域数据
    private String pinDataToBinString(String pinkey) {
        //带卡号信息双倍长密钥算法
        String key = pinkey;
        return key;
    }

    /**
     * 设置金额是否显示
     *
     * @param isShow
     */
    public void showMoneyView(boolean isShow) {
        if (isShow) {
            mMoneyLl.setVisibility(View.VISIBLE);
            bivAccount.setVisibility(View.VISIBLE);
        } else {
            mMoneyLl.setVisibility(View.GONE);
            bivAccount.setVisibility(View.GONE);
        }
    }

    /**
     * 进入到下一步
     */
    private void nextStep() {
        if (!saveValue()) {
            LogUtils.i("保存信息失败");
            return;
        }
        Intent it = getNextStep();
        if (null == it) {
            LogUtils.i("下一步activity无效");
            return;
        }
        goToNextActivity(it);
    }

    public class MyConfirmClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            nextStep();
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String tip = "";
            switch (msg.what) {
                case TOAST_TIP:
                    tip = (String) msg.obj;
                    showTip(tip);
                    break;
                default:

                    int count = msg.arg1;
                    String psw = "";
                    int textSize = 0;
                    if (count <= 0) {
                        psw = "";
                        textSize = 15;
                    } else {
                        for (int j = 0; j < count; j++) {
                            psw += "     *";
                        }
                        textSize = 24;
                    }
 //                   tvPwdInput.setText(psw);
  //                  tvPwdInput.setTextSize(textSize);
                    break;
            }
            super.handleMessage(msg);
        }

    };

    /**
     * PIN输入监听器
     */
    private class MyGetPinListener extends GetPinListener.Stub {
        @Override
        public void onStopGetPin() throws RemoteException {
            LogUtils.i("停止获取密码");
        }

        @Override
        public void onInputKey(int arg0, String arg1) throws RemoteException {
            LogUtils.d("键盘输入：" + arg0 + "  : " + arg1);
            setPwdText(arg0, arg1);
        }

        @Override
        public void onError(int arg0) throws RemoteException {
            LogUtils.i("密码键盘输入错误：" + arg0);
            setPwdText(0, "error:" + arg0);
            switch (arg0) {
                case -4://超时
                    finish();
                    break;
                case -5:
                    Toast.makeText(getApplicationContext(), "前往功能页进行'POS签到'后方可进行交易", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onConfirmInput(byte[] arg0) throws RemoteException {

            String pin = "";
            if (null != arg0) {
                pin = DataConverter.bytesToHexString(arg0);
            }
            if (mPwdLen <= 0) {
                Cache.getInstance().setHasPin(false);
            } else {
                Cache.getInstance().setHasPin(true);
            }
            String serInputCode = Cache.getInstance().getSerInputCode();
            if (!StringUtil.isEmpty(serInputCode)) {
                if (serInputCode.startsWith("01")) {// 手工输入方式
                    if (0 == mPwdLen) {
                        serInputCode = "012";
                    } else {
                        serInputCode = "011";
                    }
                } else if (serInputCode.startsWith("02")) {// 磁条卡
                    if (0 == mPwdLen) {
                        serInputCode = "022";
                    } else {
                        serInputCode = "021";
                    }
                } else if (serInputCode.startsWith("05")) {// IC卡输入
                    if (0 == mPwdLen) {
                        serInputCode = "052";
                    } else {
                        serInputCode = "051";
                    }
                } else if (serInputCode.startsWith("07")) { //非接
                    if (0 == mPwdLen) {
                        serInputCode = "072";
                    } else {
                        serInputCode = "071";
                    }
                } else if (serInputCode.startsWith("80")) {//非接
                    if (0 == mPwdLen) {
                        serInputCode = "802";
                    } else {
                        serInputCode = "801";
                    }
                }
            }
            Cache.getInstance().setSerInputCode(serInputCode);
            Cache.getInstance().setPinBlock(pinDataToBinString(pin));
            nextStep();
        }

        @Override
        public void onCancelKeyPress() throws RemoteException {
            LogUtils.i("取消密码输入");
            finish();
        }
    }

    /**
     * 用handler发送Toast消息提示
     *
     * @param tip
     */
    private void showToastTip(String tip) {
        Message msg = mHandler.obtainMessage();
        msg.obj = tip;
        msg.what = TOAST_TIP;
        mHandler.sendMessage(msg);
    }

    /**
     * 发送handler消息，设置页面密码显示
     *
     * @param len
     * @param tip
     */
    private void setPwdText(int len, String tip) {
        Message msg = new Message();
        msg.arg1 = len;
        msg.obj = "clear";
        mPwdLen = len;
        mHandler.sendMessage(msg);
    }

    /**
     * 此方法最好设置成抽象方法，让子类实现。暂时写死成0x00;
     * 返回0x00表示联接pin ； 0x01脱机pin
     *
     * @return
     */
    public int getPinType() {
        return 0x00;
    }

    /**
     * 在页面OnCreate时，子类可以设置页面
     */
    public abstract void initViewData();

    /**
     * 获取下一个页面的activity
     *
     * @return
     */
    public abstract Intent getNextStep();

}
