package com.nld.starpos.wxtrade.debug.thread.scan_thread;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.wxtrade.debug.bean.password.CheckPwRsp;
import com.nld.starpos.wxtrade.debug.bean.scan_common.ResultStatus;
import com.nld.starpos.wxtrade.debug.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.debug.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.debug.exception.TransException;
import com.nld.starpos.wxtrade.debug.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.debug.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.debug.thread.ComonThread;
import com.nld.starpos.wxtrade.debug.utils.ToastUtils;
import com.nld.starpos.wxtrade.debug.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.debug.utils.params.CommonParams;
import com.nld.starpos.wxtrade.debug.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.debug.utils.params.TransParamsValue;

import java.util.TreeMap;

import common.StringUtil;

/**
 *
 * @author wqz
 * @date 2017/10/10
 * 操作员验密
 */

public class CheckPwThread extends ComonThread {

    private ScanPayBean mCheckPwBean;
    private Handler mHandler;
    private ResultStatus resultStatus;

    public CheckPwThread(Context context, Handler handler, ScanPayBean checkPwBean) {
        super(context, handler);
        mCheckPwBean = checkPwBean;
        mHandler = handler;
        resultStatus = new ResultStatus();
    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }

    @Override
    public void run() {
        TreeMap<String, String> ck_pw_map = new TreeMap<>();
        ck_pw_map.put(CommonParams.TYPE, mCheckPwBean.getTransType());
        ck_pw_map.put(CommonParams.PASS_WD, mCheckPwBean.getPassWd());
        ck_pw_map.put(CommonParams.USER_NO, mCheckPwBean.getManagerNo());
        ck_pw_map.put(CommonParams.TERMINAL_NO, mCheckPwBean.getTerminalNo());

        String md5_key = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        if (!StringUtil.isEmpty(md5_key)){
            mCheckPwBean.setMd5_key(md5_key);
        }
        String transNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO); //流水号
        mCheckPwBean.setRequestId(System.currentTimeMillis()+transNo);
        String merchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID); //扫码商户号
        if (!StringUtil.isEmpty(merchantId)){
            mCheckPwBean.setMerchantId(merchantId);
        }
        AsyncHttpUtil.httpPostXutils(ck_pw_map,mCheckPwBean,new AsyncRequestCallBack<String>() {
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                dealException(httpException,resultStatus);
                ScanCache.getInstance().setResultStatus(resultStatus);
                mHandler.sendMessage(mHandler.obtainMessage(0x02));
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!ScanTransUtils.checkHmac(responseInfo.result)){
                    ToastUtils.showToast("没有返回hmac校验值或验证失败");
                }
                super.onSuccess(responseInfo);
                if (!StringUtil.isEmpty(responseInfo.result)) {
                    Log.d("wqz", responseInfo.result);
                    CheckPwRsp resultStr = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, CheckPwRsp.class);
                    if (null != resultStr){
                        if ("00".equals(resultStr.getRspCd())) {
                            ScanParamsUtil.getInstance().update("adminpwd",mCheckPwBean.getPassWd());  //更新主管密码<加密过后的>
                            mHandler.sendMessage(mHandler.obtainMessage(0x01, "密码正确"));
                        } else {
                            resultStatus.setRetCode(resultStr.getReturnCode());
                            resultStatus.setErrMsg(resultStr.getMessage());
                            resultStatus.setSucess(false);
                            ScanCache.getInstance().setResultStatus(resultStatus);
                            mHandler.sendMessage(mHandler.obtainMessage(0x02));
                        }
                    }else {
                        resultStatus.setRetCode(TransException.ERR_DAT_JSON_E207);
                        resultStatus.setErrMsg(TransException.getMsg(TransException.ERR_DAT_JSON_E207));
                        ScanCache.getInstance().setResultStatus(resultStatus);
                        sendEmptyMessage(0x02);
                    }
                } else {
                    resultStatus.setRetCode(TransException.ERR_DAT_EOF_E208);
                    resultStatus.setErrMsg(TransException.getMsg(TransException.ERR_DAT_EOF_E208));
                    resultStatus.setSucess(false);
                    ScanCache.getInstance().setResultStatus(resultStatus);
                    mHandler.sendMessage(mHandler.obtainMessage(0x02));
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                mHandler.sendMessage(mHandler.obtainMessage(0x03, "密码验证中"));
            }
        });
    }
}
