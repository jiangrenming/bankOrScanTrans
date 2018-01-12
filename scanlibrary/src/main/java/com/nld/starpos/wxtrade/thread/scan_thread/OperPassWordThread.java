package com.nld.starpos.wxtrade.thread.scan_thread;

import android.content.Context;
import android.os.Handler;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.util.LogUtils;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.wxtrade.bean.password.CheckPwRsp;
import com.nld.starpos.wxtrade.bean.scan_common.ResultStatus;
import com.nld.starpos.wxtrade.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.exception.TransException;
import com.nld.starpos.wxtrade.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.thread.ComonThread;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import java.util.TreeMap;

import common.StringUtil;

/**
 *
 * @author jiangrenming
 * @date 2017/11/30
 * 密码相关子线程
 */

public class OperPassWordThread extends ComonThread {

    private ResultStatus result;
    private ScanPayBean mCheckPwbean;

    public OperPassWordThread(Context context, Handler handler, ScanPayBean checkPwBean) {
        super(context, handler);
        this.mCheckPwbean = checkPwBean;
        result = new ResultStatus();
    }

    @Override
    public void run() {
        TreeMap<String ,String> password = new TreeMap<>();
        if (TransParamsValue.AntCompanyInterfaceType.UPDATE_PASS_WORD.equals(mCheckPwbean.getTransType())){  //主管密码
            password.put(CommonParams.OLDPASSWD, mCheckPwbean.getOldPassword());
            password.put(CommonParams.NEWPASSWD,mCheckPwbean.getNewPassword());
            password.put(CommonParams.USER_NO, mCheckPwbean.getManagerNo());
        }else {  //操作员密码
            password.put(CommonParams.USER_NO, mCheckPwbean.getOprId());          //操作员编号
            password.put(CommonParams.OPER_USER_NO, mCheckPwbean.getManagerNo());  //主管账号
            password.put(CommonParams.PASS_WD, mCheckPwbean.getPassWd());       //主管密码
        }
        password.put(CommonParams.TYPE,mCheckPwbean.getTransType());
        password.put(CommonParams.TERMINAL_NO, mCheckPwbean.getTerminalNo());
        String md5_key = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        if (!StringUtil.isEmpty(md5_key)){
            mCheckPwbean.setMd5_key(md5_key);
        }
        String transNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO); //流水号
        mCheckPwbean.setRequestId(System.currentTimeMillis()+transNo);
        String merchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID); //商户号
        if (!StringUtil.isEmpty(merchantId)){
            mCheckPwbean.setMerchantId(merchantId);
        }
        AsyncHttpUtil.httpPostXutils(password,mCheckPwbean,new AsyncRequestCallBack<String>(){
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                dealException(httpException,result);
                ScanCache.getInstance().setResultStatus(result);
                sendMessage(handler.obtainMessage(0x01));
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!ScanTransUtils.checkHmac(responseInfo.result)){
                    ToastUtils.showToast("没有返回hmac校验值或验证失败");
                }
                super.onSuccess(responseInfo);
                if (!StringUtil.isEmpty(responseInfo.result)){
                    LogUtils.i("修改密码返回数据="+responseInfo.result);
                    CheckPwRsp checkPwRsp = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, CheckPwRsp.class);
                    if (null != checkPwRsp){
                        if ("00".equals(checkPwRsp.getRspCd())){  //密码修改成功
                            if (TransParamsValue.AntCompanyInterfaceType.UPDATE_PASS_WORD.equals(mCheckPwbean.getTransType())){
                                ScanParamsUtil.getInstance().update("adminpwd",mCheckPwbean.getNewPassword());  //更新主管密码<加密过后的>
                            }
                            sendMessage(handler.obtainMessage(0x02));
                        }else {
                            result.setRetCode(checkPwRsp.getReturnCode());
                            result.setErrMsg(checkPwRsp.getMessage());
                            result.setSucess(false);
                            ScanCache.getInstance().setResultStatus(result);
                            sendMessage(handler.obtainMessage(0x01));
                        }
                    }else {
                        result.setRetCode(TransException.ERR_DAT_JSON_E207);
                        result.setErrMsg(TransException.getMsg(TransException.ERR_DAT_JSON_E207));
                        ScanCache.getInstance().setResultStatus(result);
                        sendEmptyMessage(0x01);
                    }
                }else {
                    result.setRetCode(TransException.ERR_DAT_EOF_E208);
                    result.setErrMsg(TransException.getMsg(TransException.ERR_DAT_EOF_E208));
                    result.setSucess(false);
                    ScanCache.getInstance().setResultStatus(result);
                    sendMessage(handler.obtainMessage(0x01));
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                sendMessage( handler.obtainMessage(0x03,"密码正在修改中..."));
            }
        });
    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }
}
