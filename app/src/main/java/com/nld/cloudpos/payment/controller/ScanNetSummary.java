package com.nld.cloudpos.payment.controller;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.nld.cloudpos.util.CommonContants;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.bean.scan_query.ScanQueryDataBean;
import com.nld.starpos.wxtrade.bean.scan_query.ScanQueryRes;
import com.nld.starpos.wxtrade.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.ReturnCodeParams;
import com.nld.starpos.wxtrade.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import common.DateTimeUtil;
import common.StringUtil;


/**
 * Created by jiangrenming on 2017/10/12.
 * 分类汇总网络请求数据
 */

public class ScanNetSummary extends AsyncTask<Void, Void, List<ScanQueryDataBean>> {

    private OnNetDataListener onNetDataListener;
    private Context mContext;
    private ScanPayBean scanPayBean ;
    private int needNum = 1;
    private String errorMessage;
    private boolean isExit = false;

    public ScanNetSummary(Activity activity, OnNetDataListener onNetDataListener) {
        this.onNetDataListener = onNetDataListener;
        this.mContext = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        scanPayBean = new ScanPayBean();
        scanPayBean.setTransType( TransParamsValue.AntCompanyInterfaceType.TRANS_QUER);
        scanPayBean.setPageNo(1);
        scanPayBean.setPageNumber(200);
        scanPayBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID));
        scanPayBean.setDateTime(DateTimeUtil.getCurrentDate("yyyyMMdd"));
        scanPayBean.setProjectType(EncodingEmun.antCompany.getType());
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
        scanPayBean.setRequestUrl(CommonContants.url);
    }

    @Override
    protected List<ScanQueryDataBean> doInBackground(Void... params) {

        final List<ScanQueryDataBean> transNetIntroList = new ArrayList<>();
        while (true){
            try{
                TreeMap<String, String> map = new TreeMap<>();
                map.put(CommonParams.TYPE, scanPayBean.getTransType());  //接口类型
                map.put(CommonParams.PAG_NO, String.valueOf(scanPayBean.getPageNo()));  //页数
                map.put(CommonParams.PAG_NUM, String.valueOf(scanPayBean.getPageNumber()));  //记录数
                map.put(CommonParams.TERMINAL_NO, scanPayBean.getTerminalNo());  //终端号
                map.put(CommonParams.START_DATE, scanPayBean.getDateTime());  //默认当天时间起
                map.put(CommonParams.END_DATE, scanPayBean.getDateTime());   //默认当天时间止
  //              AsyncHttpUtil.setCommonBean(scanPayBean);
                if (scanPayBean.getPageNo() == 0 || scanPayBean.getPageNo() > needNum || isExit){
                    break;
                }
                AsyncHttpUtil.httpPostXutils(map,scanPayBean,new AsyncRequestCallBack<String>(){
                    @Override
                    public void onFailure(HttpException httpException, String errorMsg) {
                        super.onFailure(httpException, errorMsg);
                        errorMessage = errorMsg;
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        if (!ScanTransUtils.checkHmac(responseInfo.result)){
                            ToastUtils.showToast("没有返回hmac校验值或验证失败");
                        }
                        super.onSuccess(responseInfo);
                        if (!StringUtil.isEmpty(responseInfo.result)){
                            LogUtils.i("查询结果"+responseInfo.result);
                            try{
                                String[] split = responseInfo.result.split("&");  //处理 datas字段返回的数据类型变化为字符串的特殊处理
                                for (int i = 0; i < split.length; i++) {
                                    if (split[i].startsWith("datas")){
                                        int index = split[i].indexOf("=");
                                        String value = split[i].substring(index + 1);
                                        if (value.equals("0")){
                                            errorMessage = "未查询到相关数据";
                                            isExit = true;
                                            return;
                                        }
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                return;
                            }
                            ScanQueryRes scanQueryRes =  DataAnalysisByJson.getInstance().getObjectByString2(responseInfo.result, ScanQueryRes.class);
                            if (ReturnCodeParams.SUCESS_CODE.equals(scanQueryRes.getReturnCode())){
                                if (scanQueryRes.getPagNo() == 1){
                                    int totCnt = scanQueryRes.getTotCnt();
                                    int pagNum = scanQueryRes.getPagNum();

                                    if (totCnt !=0 && pagNum != 0) {
                                        int netCount = totCnt / pagNum;
                                        needNum = ((totCnt % pagNum) != 0) ? (netCount + 1) : netCount;
                                        LogUtils.d("共需请求网络列表数据" + needNum + "次");
                                    }else {
                                        //没有查询到数据信息
                                        errorMessage = "第一次查询无总记录数或每页记录数";
                                        isExit = true ;
                                        return;
                                    }
                                }
                                List<ScanQueryDataBean> datas = scanQueryRes.getDatas();
                                if (datas != null && !datas.isEmpty()){
                                    if (scanPayBean.getPageNo() > needNum){
                                        isExit = true;
                                        return;
                                    }else {
                                        for (ScanQueryDataBean scanQuery : datas){
                                            if (TextUtils.equals(scanQuery.getCorg_no(), "GTXYPAY")) {
                                                transNetIntroList.add(scanQuery);
                                            }
                                        }
                                        scanPayBean.setPageNo(scanPayBean.getPageNo()+1);
                                    }
                                    LogUtils.d("共" + scanPayBean.getPageNo() + "页");
                                }else {
                                    errorMessage = "无相关查询数据";
                                    isExit = true ;
                                    return;
                                }
                            }
                        }else {
                            errorMessage = "无服务器返回值";
                        }
                    }
                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        super.onLoading(total, current, isUploading);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                break;
            }
        }
        return  transNetIntroList;
    }

    @Override
    protected void onPostExecute(List<ScanQueryDataBean> scanQueryDataBeen) {
        if (onNetDataListener != null) {
            onNetDataListener.onNetDataResult(scanQueryDataBeen, errorMessage);
        }
    }

    public interface OnNetDataListener {
        void onNetDataResult(List<ScanQueryDataBean> scanQueryDataBeen, String errorMessage);
    }
}
