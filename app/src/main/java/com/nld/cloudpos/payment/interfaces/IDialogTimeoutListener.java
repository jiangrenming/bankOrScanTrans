package com.nld.cloudpos.payment.interfaces;

public interface IDialogTimeoutListener {
    /**
     * 对话框超时回调
     */
    public void onDialogTimeout(String tip);
}
