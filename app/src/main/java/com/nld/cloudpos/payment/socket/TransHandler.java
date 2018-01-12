package com.nld.cloudpos.payment.socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Cache;

import org.apache.log4j.Logger;

public abstract class TransHandler extends Handler {

    private Logger logger= Logger.getLogger(TransHandler.class);
  //交易时transHandler使用的常量
    public final static int TRANS_RESULT_CODE_SUCCESS=100;//交易接口成功返回
    public final static int TRANS_RESULT_CODE_FAILD=101;//交易失败
    public final static int TRANS_RESULT_CODE_START=102;//开始网络访问
    public final static int TRANS_RESULT_CODE_SHOW_TIP=103;//提示信息变化
    public final static int TRANS_RESULT_CODE_SHOW_DIALOG=104;//显示对话框
    

    public static final  int DIALOG_TYPE_AID_SELECT=201;//多应用选择
    public static final  int DIALOG_TYPE_ECASH_TIP=202;//电子现金确认
    private boolean isCancel=false;
   
    
    @Override
    public void handleMessage(Message msg) {
        logger.info("收到交易返回的handler: "+msg.what);
        Bundle bd=msg.getData();
        super.handleMessage(msg);
        switch(msg.what){
        case TransHandler.TRANS_RESULT_CODE_SUCCESS://交易成功
            String keep=bd.getString("keep");
            transSuccess(keep);
            break;
        case TRANS_RESULT_CODE_FAILD://交易失败
            String erCode=bd.getString("trans_error_code");
            String erTip=bd.getString("trans_error_tip");
            boolean isNeedReverse=bd.getBoolean("trans_is_need_reverse");
            Cache.getInstance().setErrCode(erCode);
            Cache.getInstance().setErrDesc(erTip);
            transFaild(erCode, erTip,isNeedReverse);
            break;
        case TRANS_RESULT_CODE_START://交易网络访问开始
            int timeOut=bd.getInt("timeOut");
            transStartProgress(timeOut);
            break;
        case TRANS_RESULT_CODE_SHOW_TIP://交易提示消息变更
            String tip=bd.getString("viewTip");
            transViewTipChange(tip);
            break;
        case TRANS_RESULT_CODE_SHOW_DIALOG://提示对话框
            String[] values=bd.getStringArray("values");
            int type=bd.getInt("dialog_type");
            showDialog(values,type);
            break;
        }
    }
   
    
    public  void messageSendResult(Bundle mBundle){
        Message mMessage = Message.obtain();
        mMessage.setData(mBundle);
        mMessage.what = 0x00;
        sendTransMessage(mMessage);
    }
    
    public  void messageSendProgress(int what,Bundle mBundle){
        Message mMessage = Message.obtain();
        if(null!=mBundle){
            mMessage.setData(mBundle);
        }
        mMessage.what = what;
        sendTransMessage(mMessage);
    }
    /**
     * 发送交易失败消息 TransStartActivity.TRANS_RESULT_CODE_FAILD
     * @param code
     * @param tip
     * @param isNeedReverse
     */
    public  void messageSendProgressFaild(String code, String tip, boolean isNeedReverse){
        Message mMessage = Message.obtain();
        Bundle bd=new Bundle();
        bd.putString("trans_error_code", code);
        bd.putString("trans_error_tip", tip);
        bd.putBoolean("trans_is_need_reverse", isNeedReverse);
        mMessage.setData(bd);
        mMessage.what=TRANS_RESULT_CODE_FAILD;
        sendMessage(mMessage);
    }
    
    /**
     * 发送交易失败消息 TransStartActivity.TRANS_RESULT_CODE_FAILD
     * @param code
     * @param isNeedReverse
     */
    public  void messageSendProgressFaild(String code, boolean isNeedReverse){
        Message mMessage = Message.obtain();
        Bundle bd=new Bundle();
        bd.putString("trans_error_code", code);
        bd.putString("trans_error_tip", NldException.getMsg(code));
        bd.putBoolean("trans_is_need_reverse", isNeedReverse);
        mMessage.setData(bd);
        mMessage.what=TRANS_RESULT_CODE_FAILD;
        sendMessage(mMessage);
    }
    /**
     * 发送交易成功
     * @param keep 保留备用
     */
    public void messageSendProgressSuccess(String keep){
        Message mMessage = Message.obtain();
        mMessage.what = TRANS_RESULT_CODE_SUCCESS;
        Bundle bd=new Bundle();
        bd.putString("keep", keep);
        mMessage.setData(bd);
        sendTransMessage(mMessage);
    }
    /**
     * 发送交易提示消息变更
     *  keep 保留备用
     */
    public void messageSendTipChange(String tip){
        Message mMessage = Message.obtain();
        mMessage.what = TRANS_RESULT_CODE_SHOW_TIP;
        Bundle bd=new Bundle();
        bd.putString("viewTip", tip);
        mMessage.setData(bd);
        sendTransMessage(mMessage);
    }
    

    /**
     * 发送交易提示消息变更
     *  keep 保留备用
     */
    public void messageShowDialog(String[] values, int dialogType){
        Message mMessage = Message.obtain();
        mMessage.what = TRANS_RESULT_CODE_SHOW_DIALOG;
        Bundle bd=new Bundle();
        bd.putStringArray("values", values);
        bd.putInt("dialog_type", dialogType);
        mMessage.setData(bd);
        sendTransMessage(mMessage);
    }
    
    /**
     * 交易开始网络访问
     * @param timeOut
     */
    public void messageSendStartProgress(int timeOut){
        Message mMessage = Message.obtain();
        mMessage.what = TRANS_RESULT_CODE_START;
        Bundle bd=new Bundle();
        bd.putInt("timeOut", timeOut);
        mMessage.setData(bd);
        sendTransMessage(mMessage);
    }
    
    /**
     * 交易成功
     * @param keep 保留备用
     */
    public abstract void transSuccess(String keep);
    
    /**
     * 交易失败
     * @param code 错误代码
     * @param msg  错误信息
     * @param isNeedReverse 是否需要冲正
     */
    public abstract void transFaild(String code, String msg, boolean isNeedReverse);
    
    /**
     * 交易提示信息变更
     * @param tip 提示信息
     */
    public abstract void transViewTipChange(String tip);
    
    /**
     * 交易网络访问开始
     * @param timeOut 单位毫秒
     */
    public abstract void transStartProgress(int timeOut);
    
    /**
     * 显示对话框，
     * @param values  对话框内容，可以多条。
     * @param type      对话框类型。
     */
    public abstract void showDialog(String[] values, int type);
    
    /**
     * 发送交易
     * @param msg
     */
    public void sendTransMessage(Message msg){
    	if(!isCancel){
    		sendMessage(msg);
    	}else{
    		logger.debug("Handler被取消，不发送信息");
    	}
    		
    }
    
    public void cancel(){
    	logger.debug("取消TransHandler");
    	isCancel=true;
    	removeCallbacksAndMessages(null);
    }
}
