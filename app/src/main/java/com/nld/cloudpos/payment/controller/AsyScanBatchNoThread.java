package com.nld.cloudpos.payment.controller;

import android.content.Context;
import android.os.Handler;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.nld.cloudpos.util.CommonContants;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.banktrade.bean.ResultStatus;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.bean.scan_batch_no.AsyScanBatchNo;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.exception.TransException;
import com.nld.starpos.wxtrade.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.ReturnCodeParams;
import com.nld.starpos.wxtrade.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

import java.util.TreeMap;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/10/17.
 * 同步批次号线程
 */

public class AsyScanBatchNoThread extends ComonThread {
    private ResultStatus result;

    public AsyScanBatchNoThread(Context context, Handler handler) {
        super(context, handler);
        result = new ResultStatus();
    }

    @Override
    public void run() {

        TreeMap<String,String> asyScan = new TreeMap<>();
        asyScan.put(CommonParams.TERMINAL_NO, ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID)); //终端号
        asyScan.put(CommonParams.TYPE, TransParamsValue.AntCompanyInterfaceType.SNYBATCHNO); //接口

        ScanPayBean scanPayBean = new ScanPayBean();
        String md5_key = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        if (!StringUtil.isEmpty(md5_key)){
            scanPayBean.setMd5_key(md5_key);
        }
        String transNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO); //流水号
        scanPayBean.setRequestId(System.currentTimeMillis() + transNo);
        String merchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID); //扫码商户号
        if (!StringUtil.isEmpty(merchantId)){
            scanPayBean.setMerchantId(merchantId);
        }
        scanPayBean.setTransType(TransParamsValue.AntCompanyInterfaceType.SNYBATCHNO);
        scanPayBean.setRequestUrl(CommonContants.url);
      //  AsyncHttpUtil.setCommonBean(scanPayBean);

        AsyncHttpUtil.httpPostXutils(asyScan,scanPayBean,new AsyncRequestCallBack<String>(){
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                result.setTransCode(TransConstans.TRANS_CODE_BATCHNO);
                dealException(httpException,result);
                Cache.getInstance().setRestatus(result);
                sendMessage(handler.obtainMessage(0x01));
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!ScanTransUtils.checkHmac(responseInfo.result)){
                    ToastUtils.showToast("没有返回hmac校验值或验证失败");
                }
                super.onSuccess(responseInfo);
                if (!StringUtil.isEmpty(responseInfo.result)){
                    LogUtils.i("返回批次同步的数据="+responseInfo.result);
                    AsyScanBatchNo asyBatchNo = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, AsyScanBatchNo.class);
                    if (ReturnCodeParams.SUCESS_CODE.equals(asyBatchNo.getReturnCode())){
                        String batNo = asyBatchNo.getBatNo();
                        if (!StringUtil.isEmpty(batNo)){  //同步本地批次号
                            int currenNo = 1;
                            try {
                                currenNo = Integer.parseInt(batNo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            int nextNo = (currenNo + 1) % 1000000;
                            ScanParamsUtil.getInstance().update(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO, String.format("%06d", nextNo));
                        }
                        sendMessage(handler.obtainMessage(0x12));
                    }else {
                        result.setRetCode(asyBatchNo.getReturnCode());
                        result.setErrMsg(asyBatchNo.getMessage());
                        result.setTransCode(TransConstans.TRANS_CODE_BATCHNO);
                        Cache.getInstance().setRestatus(result);
                        result.setSucess(false);
                        sendMessage(handler.obtainMessage(0x01));
                    }
                }else {
                    //返回的数据为空信息
                    result.setRetCode(TransException.ERR_DAT_EOF_E208);
                    result.setErrMsg(TransException.getMsg(TransException.ERR_DAT_EOF_E208));
                    result.setSucess(false);
                    result.setTransCode(TransConstans.TRANS_CODE_BATCHNO);
                    Cache.getInstance().setRestatus(result);
                    sendMessage(handler.obtainMessage(0x01));
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                sendMessage( handler.obtainMessage(0x11,"正在同步批次号"));
            }
        });
    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }
}
