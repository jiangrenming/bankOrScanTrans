package com.nld.cloudpos.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.centerm.iso8583.util.DataConverter;
import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.pinpad.AidlPinpad;
import com.nld.cloudpos.aidl.pinpad.GetPinListener;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.interfaces.IDialogItemClickListener;
import com.nld.cloudpos.payment.interfaces.IDialogTimeoutListener;
import com.nld.starpos.banktrade.pinUtils.PbocDev;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.DateTimeUtil;
import common.StringUtil;


public class DialogFactory {
    private static Logger logger = Logger.getLogger(DialogFactory.class);
    private static HashMap<String, Dialog> dialogs = new HashMap<String, Dialog>();


    private static TextView tvTitle;
    private static TextView tvCount;
    private static LinearLayout llBtn;
    private static TextView tvMsg;
    private static TextView tvOk;
    private static TextView tvCancel;
    private static CountDownTimer loadingTimer = null;
    private static TextView tvTime;

    public static boolean dismissAlert(Activity activity) {
        if (null != loadingTimer) {
            loadingTimer.cancel();
            loadingTimer = null;
        }
        Dialog dialog = (Dialog) dialogs.get(activity.toString());
        if ((dialog != null) && (dialog.isShowing())) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                logger.error("弹框关闭异常：", e);
            }
            dialogs.remove(activity.toString());
            return true;
        } else {
            return false;
        }
    }

    private static Dialog createDialog(Activity activity) {

        dismissAlert(activity);
        Dialog dialog = new Dialog(activity, R.style.dialog_basic);
        dialog.setContentView(R.layout.dialog_show_tip);

        dialogs.put(activity.toString(), dialog);

        tvTitle = (TextView) dialog.findViewById(R.id.dialog_tip_title);
        tvCount = (TextView) dialog.findViewById(R.id.dialog_tip_time);
        llBtn = (LinearLayout) dialog.findViewById(R.id.dialog_btn_ll);
        tvMsg = (TextView) dialog.findViewById(R.id.dialog_tip_content);
        tvOk = (TextView) dialog.findViewById(R.id.dialog_tip_confirm_btn);
        tvCancel = (TextView) dialog.findViewById(R.id.dialog_tip_cancel_btn);
        //设置对话框点击空白处不可关闭，点击返回按钮不可关闭。
        dialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                } else {
                    return false; //默认返回 false
                }
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * 显示提示对话框，无取消按钮，不可取消，只能通过调用该类的dismissAlert方法
     *
     * @param context
     * @param title   标题
     * @param msg     内容
     */
    public static void showMessage(Activity context, String title, String msg) {
        clearBeforeDialog(context);
        Dialog dialog = createDialog(context);
        llBtn.setVisibility(View.GONE);
        tvCount.setVisibility(View.GONE);
        tvTitle.setText(title);
        tvMsg.setText(msg);
        dialog.show();
    }

    /**
     * 显示两个消息按钮的消息提示框，按钮点击事件均需实现,取消按钮可以传null；
     *
     * @param context
     * @param title
     * @param msg
     * @param okTitle
     * @param okClick
     * @param canTitle
     * @param canClick
     */
    public static void showMessage(final Activity context, String title, String msg
            , String okTitle, OnClickListener okClick
            , String canTitle, OnClickListener canClick) {
        clearBeforeDialog(context);
        Dialog dialog = createDialog(context);
        llBtn.setVisibility(View.VISIBLE);
        tvCount.setVisibility(View.GONE);
        tvTitle.setText(title);
        tvMsg.setText(msg);
        tvOk.setText(okTitle);
        tvCancel.setText(canTitle);
        tvOk.setOnClickListener(okClick);
        if (null != canClick) {
            tvCancel.setOnClickListener(canClick);
        } else {
            tvCancel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismissAlert(context);
                }
            });
        }
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 显示加载对话框
     *
     * @param activity
     * @param title
     * @param msg
     */
    public static void showLoadingDialog(final Activity activity, String title, String msg) {
        clearBeforeDialog(activity);
        ImageView ivLoad;
        TextView tvLoadTip;
        //final TextView tvLoadCount;
        dismissAlert(activity);
        Dialog dialog = new Dialog(activity, R.style.dialog_basic);
        dialog.setContentView(R.layout.dialog_loading);

        dialogs.put(activity.toString(), dialog);
        ivLoad = (ImageView) dialog.findViewById(R.id.loading_imgview);
        tvLoadTip = (TextView) dialog.findViewById(R.id.loading_tip_tv);
        //tvLoadCount=(TextView) dialog.findViewById(R.id.loading_count_tv);
        //启动动画
        Animation operatingAnim = AnimationUtils.loadAnimation(activity,
                R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        ivLoad.setAnimation(operatingAnim);
        //设置对话框点击空白处不可关闭，点击返回按钮不可关闭。
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME
                        || keyCode == KeyEvent.KEYCODE_MENU) {
                    return true;
                }
                return false;
            }
        });
        tvLoadTip.setText(msg);

        loadingTimer = new CountDownTimer(15000, 1000) {

            @Override
            public void onFinish() {
                // 计时完毕时触发
                logger.info("计时结束");
                dismissAlert(activity);
                ToastUtils.showToast("网络连接超时！");
            }

            @Override
            public void onTick(long millisUntilFinished) {
                // 计时过程显示
                //tvLoadCount.setText(String.valueOf(millisUntilFinished / 1000));
            }
        };
        loadingTimer.cancel();
        loadingTimer.start();
        dialog.show();
    }


    /**
     * 显示确认对话框
     *
     * @param context
     * @param title
     * @param msg
     * @param okStr
     */
    public static void showConfirmMessage(final Activity context, String title, String msg, String okStr,
                                          final OnClickListener okClick) {
        clearBeforeDialog(context);
        Dialog dialog = createDialog(context);
        llBtn.setVisibility(View.VISIBLE);
        tvCount.setVisibility(View.GONE);
        tvTitle.setText(title);
        tvMsg.setText(msg);
        tvOk.setText(okStr);
        tvCancel.setVisibility(View.GONE);
        tvOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismissAlert(context);
                if (null != okClick) {
                    okClick.onClick(v);
                }
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 显示存在倒计时的确认对话框，倒计时结算自动点击
     *
     * @param context
     * @param title
     * @param msg
     * @param okStr
     * @param timeout 单位毫秒
     */
    public static void showConfirmMessageTimeout(int timeout, final Activity context
            , String title, String msg, String okStr
            , final OnClickListener okClick) {
        clearBeforeDialog(context);
        Dialog dialog = createDialog(context);
        llBtn.setVisibility(View.VISIBLE);
        loadingTimer = new CountDownTimer(timeout, 1000) {

            @Override
            public void onFinish() {
                // 计时完毕时触发
                logger.info("计时结束");
                dismissAlert(context);
                if (null != okClick) {
                    okClick.onClick(tvOk);
                }
            }

            @Override
            public void onTick(long millisUntilFinished) {
                // 计时过程显示
                tvCount.setText(String.valueOf(millisUntilFinished / 1000));
            }
        };
        if (timeout > 1000) {
            tvCount.setVisibility(View.VISIBLE);
            loadingTimer.start();
        } else {
            tvCount.setVisibility(View.GONE);
        }
        tvTitle.setText(title);
        tvMsg.setText(msg);
        tvOk.setText(okStr);
        tvCancel.setVisibility(View.GONE);
        tvOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismissAlert(context);
                if (null != okClick) {
                    okClick.onClick(v);
                    if (loadingTimer != null)
                        loadingTimer.cancel();
                }
            }
        });
        dialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_HOME ||
                        keyCode == KeyEvent.KEYCODE_BACK ||
                        keyCode == KeyEvent.KEYCODE_MENU) {
                    return true;
                }
                return false;
            }
        });
        dialog.show();

    }

    /**
     * 显示确认对话框
     *
     * @param context
     * @param title
     * @param msg
     * @param okStr
     */
    public static void showConfirmMessage(final Activity context, String title, String msg, String okStr) {
        clearBeforeDialog(context);
        Dialog dialog = createDialog(context);
        llBtn.setVisibility(View.VISIBLE);
        tvCount.setVisibility(View.GONE);
        tvTitle.setText(title);
        tvMsg.setText(msg);
        tvOk.setText(okStr);
        tvCancel.setVisibility(View.GONE);
        tvOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismissAlert(context);
            }
        });
        dialog.show();
    }

    public static void showChooseListDialog(final Activity activity, String title
            , String okStr, final OnClickListener okClick, final String[] datas, final
                                            IDialogItemClickListener itemClick) {

        clearBeforeDialog(activity);
        Dialog dialog = new Dialog(activity, R.style.dialog_basic);
        dialog.setContentView(R.layout.dialog_choose_list);

        dialogs.put(activity.toString(), dialog);

        TextView tvTitle = (TextView) dialog.findViewById(R.id.dialog_tip_title);
        TextView tvCount = (TextView) dialog.findViewById(R.id.dialog_tip_time);
        LinearLayout llBtn = (LinearLayout) dialog.findViewById(R.id.dialog_btn_ll);
        TextView tvMsg = (TextView) dialog.findViewById(R.id.dialog_tip_content);
        TextView tvOk = (TextView) dialog.findViewById(R.id.dialog_tip_confirm_btn);
        TextView tvCancel = (TextView) dialog.findViewById(R.id.dialog_tip_cancel_btn);
        ListView listView = (ListView) dialog.findViewById(R.id.dialog_choose_listview);
        //设置对话框点击空白处不可关闭，点击返回按钮不可关闭。
        dialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                } else {
                    return false; //默认返回 false
                }
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        tvTitle.setText(title);
        tvCancel.setVisibility(View.GONE);
        if (StringUtil.isEmpty(okStr)) {
            tvOk.setVisibility(View.GONE);
            llBtn.setVisibility(View.GONE);
        }
        tvOk.setText(okStr);
        tvOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != okClick) {
                    okClick.onClick(v);
                } else {
                    dismissAlert(activity);
                }
            }
        });
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        String COL_NAME = "colName";
        for (int i = 0; i < datas.length; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(COL_NAME, datas[i].trim());
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(activity, list, R.layout.item_simple_text
                , new String[]{COL_NAME}, new int[]{R.id.simple_text_value});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                itemClick.onDialogItemClick(view, datas[position], position);
            }
        });
        dialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }
        });
        dialog.show();
    }

    private static Toast mToast;

    public static void showTip(Context mContext, String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.show();
    }


    /**
     * 显示加载对话框
     *
     * @param timeout  超时后自动消失 单位毫秒
     * @param activity
     * @param tip
     */
    public static void showLoadingTip(final int timeout, final Activity activity
            , String tip, final IDialogTimeoutListener listener) {
        clearBeforeDialog(activity);
        ProgressDialog printDialog = null;
        printDialog = new ProgressDialog(activity);
        printDialog.setMessage(tip);
        printDialog.setCancelable(false);
        printDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                return true;
            }
        });
        dialogs.put(activity.toString(), printDialog);
        loadingTimer = new CountDownTimer(timeout, 1000) {

            @Override
            public void onFinish() {
                // 计时完毕时触发
                //                if(dismissAlert(activity)){
                listener.onDialogTimeout("对话框超时");
                dismissAlert(activity);
                logger.debug("结束打印");
                //                }
            }

            @Override
            public void onTick(long millisUntilFinished) {
                // 计时过程显示
                if (millisUntilFinished >= timeout) {
                    logger.debug("打印框计时结束");
                }
            }
        };
        loadingTimer.start();
        printDialog.show();
    }


    private static int mPwdLen = 0;

    /**
     * 显示仅取消按钮的对话框，点击事件需实现
     *
     * @param context
     * @param title
     * @param canTitle
     * @param canClick
     */
    public static void showOfflinePinDialog(final Activity context, final AidlPinpad pinPadDev, String
            title, final String msgstr
            , String canTitle, final OnClickListener canClick) {
        clearBeforeDialog(context);
        int timeout = 60000;
        final Dialog dialog = createDialog(context);
        llBtn.setVisibility(View.VISIBLE);
        tvCount.setVisibility(View.GONE);
        tvTitle.setText(title);
        tvMsg.setText(msgstr);
        tvOk.setVisibility(View.GONE);
        tvCancel.setText(canTitle);
        tvCancel.setOnClickListener(canClick);
        loadingTimer = new CountDownTimer(timeout, 1000) {

            @Override
            public void onFinish() {
                // 计时完毕时触发
                logger.info("计时结束");
                if (null != canClick) {
                    canClick.onClick(tvCancel);
                }
            }

            @Override
            public void onTick(long millisUntilFinished) {
                logger.info("计时:" + millisUntilFinished);
                // 计时过程显示
                tvCount.setText(String.valueOf(millisUntilFinished / 1000));
            }
        };


        final Bundle bd = new Bundle();
        String pikId = ParamsUtil.getInstance().getParam(Constant.FIELD_NEW_PIK_ID);
        int iPikId = Integer.parseInt(StringUtil.isEmpty(pikId) ? "0" : pikId);

        int inputType = 1;
        int minlength = 0;
        int maxlength = 6;
        String carno = Cache.getInstance().getCardNo();
        String pan = "";
        if (!StringUtil.isEmpty(carno)) {
            //          carno=carno+"0000000000";//避免卡号小于15位时取出的pan有误
            carno = carno + "FFFFFFFFFF";//避免卡号小于15位时取出的pan有误
            pan = "0000" + carno.substring(3, 15);
        } else {
            logger.error("打开密码键盘时，卡号数据异常");
        }
        logger.info("pan=" + pan);
        int type = 0x01;
        bd.putInt("wkeyid", iPikId);
        bd.putInt("keytype", type);
        bd.putByteArray("random", null);
        bd.putInt("inputtimes", inputType);
        bd.putInt("minlength", minlength);
        bd.putInt("maxlength", maxlength);
        bd.putString("pan", pan);

        final Handler mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                String tip = "";
                switch (msg.what) {
                    case 1:
                        //显示异常
                        mPwdLen = msg.arg1;
                        tvMsg.setText(msgstr);
                        tip = (String) msg.obj;
                        showTip(context, tip);
                        break;
                    case 2:
                        if (null != canClick) {
                            canClick.onClick(tvCancel);
                        }
                        break;
                    case 3:
                        //导入脱机密码
                        dialog.dismiss();
                        showLoadingDialog(context, "导入脱机密码", "正在导入脱机密码...");
                        break;
                    case 4:
                        getOffLinePin(pinPadDev, bd, context, this);
                        break;
                    default:
                        mPwdLen = msg.arg1;
                        int count = msg.arg1;
                        String content = (String) msg.obj;
                        String psw = "*********";

                        if (count <= 0) {
                            tvMsg.setText(msgstr);
                        } else {
                            tvMsg.setText(psw.substring(0, count));
                        }
                        break;
                }
                super.handleMessage(msg);
            }

        };
        dialog.show();
        //获取密码
        getOffLinePin(pinPadDev, bd, context, mHandler);
    }


    public static void getOffLinePin(final AidlPinpad pinPadDev, final Bundle bd, final Activity context,
                                     final Handler mHandler) {
        new Thread() {
            @Override
            public void run() {
                try {

                    logger.info("开始输入PIN");
                    logger.debug("----------------------开始启动密码键盘-----------------------" + DateTimeUtil
                            .formatMillisecondTimeSecond(System.currentTimeMillis()));

                    pinPadDev.getPin(bd, new DGetPinListener(context, mHandler), Constant.IS_SM);
                    logger.debug("----------------------启动密码键盘完成-----------------------" + DateTimeUtil
                            .formatMillisecondTimeSecond(System.currentTimeMillis()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                tvMsg.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        getOffLinePin(pinPadDev, bd, context, mHandler);
                    }
                });
            }
        }.start();
    }


    /**
     * PIN输入监听器
     */
    public static class DGetPinListener extends GetPinListener.Stub {

        Handler mHandler;
        Activity context;

        public DGetPinListener(Activity context, Handler mHandler) {
            this.mHandler = mHandler;
            this.context = context;
        }

        @Override
        public void onStopGetPin() throws RemoteException {
            logger.info("停止获取密码");
        }

        @Override
        public void onInputKey(int arg0, String arg1) throws RemoteException {
            logger.info("键盘输入：" + arg0 + "  : " + arg1);
            Message msg = new Message();
            msg.arg1 = arg0;
            msg.obj = "clear";
            mHandler.sendMessage(msg);
        }

        @Override
        public void onError(int arg0) throws RemoteException {
            logger.info("密码键盘输入错误：" + arg0);
            Message msg = new Message();
            msg.arg1 = 0;
            msg.obj = "clear";
            mHandler.sendMessage(msg);
            switch (arg0) {
                case -4:// 超时
                    mHandler.sendEmptyMessage(4);
                    break;
            }
        }

        @Override
        public void onConfirmInput(byte[] arg0) throws RemoteException {
            if (mPwdLen > 0 && mPwdLen < 6) {
                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = 0;
                msg.obj = "请输入六位密码";
                mHandler.sendMessage(msg);
                return;
            }
            logger.debug("正在导入脱机密码...");
            mHandler.sendEmptyMessage(3);
            if (mPwdLen <= 0) {
                try {
                    PbocDev.getInstance(context, BankApplication.mDeviceService)
                            .getOriginalDev()
                            .importPin("0000000000000000");
                } catch (RemoteException e) {
                    logger.error("导入脱机PIN失败,PBOC设备获取异常", e);
                } catch (Exception e) {
                    logger.error("导入脱机PIN失败", e);
                }
            } else {
                String pin = "";
                if (null != arg0) {
                    pin = DataConverter.bytesToHexString(arg0);
                    //                    logger.debug("密码键盘获取脱机密码："+pin);
                }
                try {
                    PbocDev.getInstance(context, BankApplication.mDeviceService)
                            .getOriginalDev()
                            .importPin(pin);
                } catch (RemoteException e) {
                    logger.error("导入脱机PIN失败,PBOC设备获取异常", e);
                } catch (Exception e) {
                    logger.error("导入脱机PIN失败", e);
                }
            }
        }

        @Override
        public void onCancelKeyPress() throws RemoteException {
            mHandler.sendEmptyMessage(1);
        }
    }

    /**
     * 清除map里面同一个activity弹出的对话框
     *
     * @param activity
     */
    public static void clearBeforeDialog(Activity activity) {
        Dialog dialog = (Dialog) dialogs.get(activity.toString());
        if (null != dialog && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                logger.error("弹框关闭异常：", e);
            }
            dialogs.remove(activity.toString());
        }
    }

    /***************
     * add by margintop
     **************/
    private static Dialog mDialog;

    public static void showDialog(Activity activity, int resId) {
        showDialog(activity, String.valueOf(resId));
    }

    public static void showDialog(Activity activity, String msg) {
        mDialog = new Dialog(activity, R.style.dialog_new_style);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.TOP;
        window.setAttributes(params);
        mDialog.setContentView(R.layout.dialog_new_msg);
        TextView tvMsg = (TextView) mDialog.findViewById(R.id.tv_msg);
        tvMsg.setText(msg);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void dismissDialog() {
        if (null != loadingTimer) {
            loadingTimer.cancel();
            loadingTimer = null;
        }

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    /**********************
     * end
     ********************/

    public static void showLoadingDialog(final Activity activity, String msg, int second) {
        mDialog = new Dialog(activity, R.style.dialog_new_style);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.TOP;
        window.setAttributes(params);
        mDialog.setContentView(R.layout.dialog_new_msg_loading);
        tvMsg = (TextView) mDialog.findViewById(R.id.tv_msg);
        tvMsg.setText(msg);
        tvTime = (TextView) mDialog.findViewById(R.id.tv_time);
        mDialog.setCancelable(false);

        loadingTimer = new CountDownTimer(second * 1000, 1000) {

            @Override
            public void onFinish() {
                // 计时完毕时触发
                dismissDialog();
                ToastUtils.showToast("网络连接超时！");
            }

            @Override
            public void onTick(long millisUntilFinished) {
                // 计时过程显示
                if (!activity.isFinishing()) {
                    tvTime.setText("(" + String.valueOf(millisUntilFinished / 1000) + ")");
                }
            }
        };
        loadingTimer.cancel();
        loadingTimer.start();
        mDialog.show();
    }

}
