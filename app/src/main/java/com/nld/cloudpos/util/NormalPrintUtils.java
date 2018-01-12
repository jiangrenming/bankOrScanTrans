package com.nld.cloudpos.util;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import com.nld.cloudpos.aidl.printer.AidlPrinter;
import com.nld.cloudpos.aidl.printer.AidlPrinterListener;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.data.PrinterConstant;
import com.nld.cloudpos.payment.activity.SettleResult;
import com.nld.cloudpos.payment.dev.PrintDev;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.bean.TransRecord;

import java.util.List;

import common.Utility;

import static com.nld.cloudpos.BankApplication.mDeviceService;


/**
 * Created by wqz on 2017/9/15.
 */

public class NormalPrintUtils {
    public static final int PRINT_STATE_SUCESS = 100;
    public static final int PRINT_STATE_FAILE = 101;

    public static void printNormal(final List<PrintItemObj> list, final Activity context, final boolean isSecond, final SettleResult.PrintCallBack printCallBack) {
        if (list == null || list.isEmpty()) {
//            Log.d("wqz", "无数据");
            LogUtils.d("printNormal--无数据");
            return;
        }
        if (context == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mDeviceService != null) {
                        final AidlPrinter printer = AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
//                        final int fontType = PrinterConstant.FontType.FONTTYPE_N;
//                        final int fontScale = PrinterConstant.FontScale.FONTSCALE_DW_DH;
//                        final int small = PrinterConstant.FontSize.SMALL;
//                        final int alignLeft = PrinterConstant.Align.ALIGN_LEFT;
//                        final int lineHeight = 2;
//                        final PrintItemObj.ALIGN align = PrintItemObj.ALIGN.LEFT;
                        int printerState = printer.getPrinterState();
                        if (printerState == PrinterConstant.PrinterState.PRINTER_STATE_NORMAL) {
                            printer.open();
                            printer.printText(list);
                            printer.start(getAidlPrinterListener(printer, context, list, isSecond));
                            if (printCallBack != null) {
                                printCallBack.onSuccess();
                            }
                        } else {
//                            Log.d("wqz", "打印状态错误" + printerState);
                            LogUtils.d("printNormal--打印状态错误" + printerState);
                            if (printCallBack != null) {
                                printCallBack.onFail();
                            }
                            final String errMsg = getErrMsg(printerState);
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
                                    showErrorTryAgain(context, errMsg, list, printCallBack);
                                }
                            });
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    if (printCallBack != null) {
                        printCallBack.onFail();
                    }
                    showErrorTryAgain(context, "catch异常请重试", list, printCallBack);
                }
            }
        }).start();
    }

    @NonNull
    private static AidlPrinterListener.Stub getAidlPrinterListener(final AidlPrinter printer, final Activity context, final List<PrintItemObj> list, final boolean isSecond) {
        return new AidlPrinterListener.Stub() {

            @Override
            public void onError(int i) throws RemoteException {
                Log.d("wqz", "打印失败");
                LogUtils.d("printNormal--onError  " + i);
                final String errMsg = getErrMsg(i);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
                        showErrorTryAgain(context, errMsg, list, null);
                    }
                });


            }

            @Override
            public void onPrintFinish() throws RemoteException {
                Log.d("wqz", "打印完成");
                printer.paperSkip(2);//走纸
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isSecond) {
                            DialogFactory.showConfirmMessageTimeout(5000, context, "提示", "点击“确定”继续打印下一联", "确定", new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
//                        printTransRecord(tr, context);
                                    printNormal(list, context, false, null);
                                }
                            });
                        }
                    }
                });
            }
        };
    }

    @NonNull
    private static AidlPrinterListener.Stub getAidlPrinterListener(final TransRecord record, final AidlPrinter printer, final Activity context, final Handler handler, final boolean isNeedSecond) {
        return new AidlPrinterListener.Stub() {

            @Override
            public void onError(int i) throws RemoteException {
                Log.d("wqz", "打印失败");
                LogUtils.d("printTransRecode---onError  " + i);
                final String errMsg = getErrMsg(i);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
                        showErrorTryAgain(record, context, errMsg, handler, isNeedSecond);
                    }
                });
            }

            @Override
            public void onPrintFinish() throws RemoteException {
                Log.d("wqz", "打印完成");
                printer.paperSkip(2);//走纸
                Message message = Message.obtain();
                message.what = PRINT_STATE_SUCESS;
                message.obj = "打印完成";
                handler.sendMessage(message);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isNeedSecond) {
                            DialogFactory.showConfirmMessageTimeout(5000, context, "提示", "点击“确定”继续打印下一联", "确定", new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
//                        printTransRecord(tr, context);
                                    printTransRecode(record, context, handler, false);
                                }
                            });
                        }
                    }
                });
            }
        };
    }

    private static void showErrorTryAgain(final Activity context, String errMsg, final List<PrintItemObj> list, final SettleResult.PrintCallBack printCallBack) {
//        DialogFactory.showMessage(context, "打印机" + errMsg, "请重新尝试", "继续打印", new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                DialogFactory.dismissAlert(context);
//                printNormal(list, context, false);
//            }
//        }, "返回", new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                DialogFactory.dismissAlert(context);
//            }
//        });
        DialogFactory.showConfirmMessage(context, "打印机" + errMsg, "请重新尝试", "继续打印", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFactory.dismissAlert(context);
                printNormal(list, context, false, printCallBack);
            }
        });
    }

    private static void showErrorTryAgain(final TransRecord record, final Activity context, String errMsg, final Handler handler, final boolean isNeedSeond) {
//        DialogFactory.showMessage(context, "打印机" + errMsg, "请重新尝试", "继续打印", new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                DialogFactory.dismissAlert(context);
//                printTransRecode(record, context, handler, isNeedSeond);
//            }
//        }, "返回", new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                DialogFactory.dismissAlert(context);
//            }
//        });
        DialogFactory.showConfirmMessage(context, "打印机" + errMsg, "请重新尝试", "继续打印", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFactory.dismissAlert(context);
                printTransRecode(record, context, handler, isNeedSeond);
            }
        });
    }

    public static String getErrMsg(int errCode) {
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

    public static void printTransRecode(final TransRecord record, final Activity context, final Handler handler, final boolean isNeedSecond) {
        if (record == null) {
            Message message = Message.obtain();
            message.what = PRINT_STATE_FAILE;
            message.obj = "数据异常";
            handler.sendMessage(message);
            LogUtils.d("printTransRecode---无数据");
            return;
        }
        if (context == null) {
            Message message = Message.obtain();
            message.what = PRINT_STATE_FAILE;
            message.obj = "打印参数异常";
            handler.sendMessage(message);
            LogUtils.d("printTransRecode---打印参数异常");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mDeviceService != null) {
                    try {
                        final AidlPrinter printer = AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
                        int printerState = printer.getPrinterState();
                        if (printerState == PrinterConstant.PrinterState.PRINTER_STATE_NORMAL) {
                            printer.open();
//                            PrintDev.getPrintItemObjs(Utility.transformToMap(record), context);
                            printer.printText(PrintDev.getPrintItemObjs(Utility.transformToMap(record), context));
                            printer.start(getAidlPrinterListener(record, printer, context, handler, isNeedSecond));
                        } else {
                            final String errMsg = getErrMsg(printerState);
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showErrorTryAgain(record, context, errMsg, handler, isNeedSecond);
                                }
                            });
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        showErrorTryAgain(record, context, "catch异常", handler, isNeedSecond);
                    }
                } else {
                    Message message = Message.obtain();
                    message.what = PRINT_STATE_FAILE;
                    message.obj = "打印服务异常";
                    LogUtils.d("printTransRecode---打印服务异常");
                    handler.sendMessage(message);
                }
            }
        }).start();
    }
}
