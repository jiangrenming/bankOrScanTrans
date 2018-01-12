package com.nld.cloudpos.payment.controller;/*
package com.nld.cloudpos.payment.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.banklibrary.utils.Constant;
import com.example.banklibrary.utils.ParamsUtil;
import com.example.banklibrary.utils.TransParams;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.payment.MyApplication;
import com.nld.cloudpos.payment.constant.ErrorCode;
import com.nld.cloudpos.payment.util.DataAnalysisByJson;
import com.nld.cloudpos.payment.util.DateTimeUtil;
import com.nld.cloudpos.payment.util.StringUtil;
import com.nld.cloudpos.util.CommonContants;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.wxtrade.bean.asyParams.ParamsValueSny;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

import java.util.TreeMap;

*/
/**
 * Created by jiangrenming on 2017/9/23.
 * 终端绑定界面《需要输入激活码》
 *//*


public class InputverifyCodeActivity extends Activity{


    @ViewInject(R.id.et_active)
    EditText et_active;
    @ViewInject(R.id.btn_next)
    Button btn_next;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_verify_code);
        ViewUtils.inject(this);
        et_active.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int action, KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE ||
                        action == EditorInfo.IME_ACTION_SEND ||
                        action == EditorInfo.IME_ACTION_NEXT ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String value = et_active.getText().toString().trim();
                    if (!TextUtils.isEmpty(value) && value.length() == 10) {
                       bindTerminal(value);
                    } else {
                        ToastUtils.showToast("请输入10位激活码");
                    }
                    return true;
                }
                return false;
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = et_active.getText().toString().trim();
                if (!TextUtils.isEmpty(value) && value.length() == 10) {
                    bindTerminal(value);
                } else {
                    ToastUtils.showToast("请输入10位激活码");
                }
            }
        });
    }

    */
/**
     * 终端绑定
     * @param code
     *//*

    public  void bindTerminal(String code){

        TreeMap<String,String> bindMap = new TreeMap<>();
        String sn = ShareScanPreferenceUtils.getString(this,CommonParams.SN, null);
        String posSn = ShareScanPreferenceUtils.getString(this,CommonParams.POSSn, null);
        bindMap.put(CommonParams.TYPE, TransParamsValue.AntCompanyInterfaceType.BIND_POS);
        if (!StringUtil.isEmpty(sn)){
            bindMap.put(CommonParams.SN,posSn);
        }
        if (!StringUtil.isEmpty(posSn)){
            bindMap.put(CommonParams.POSSn,sn);
        }
        bindMap.put("verifyCode",code);
        ScanPayBean scanPayBean = new ScanPayBean();
        scanPayBean.setRequestUrl(CommonContants.url);
   //     AsyncHttpUtil.setCommonBean(scanPayBean);

        AsyncHttpUtil.httpPostXutils(bindMap,scanPayBean,new AsyncRequestCallBack<String>(){
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                Log.i("终端绑定失败--",errorMsg);
                ToastUtils.showToast(errorMsg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                super.onSuccess(responseInfo);
                if (!StringUtil.isEmpty(responseInfo.result)){
                    Log.i("终端绑定成功--",responseInfo.result);
                    BindTermailInfos bindTermailInfos = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, BindTermailInfos.class);
                    if (bindTermailInfos != null){
                        if (ReturnCodeParams.SUCESS_CODE.equals(bindTermailInfos.getReturnCode())){
                            if (TransUtils.isExitLocalData()){
                                //清空数据库
                                TransUtils.clearWater();
                            }
                            TransUtils.upDateBindInfos(bindTermailInfos,InputverifyCodeActivity.this);
                            goToNextActivity();
                        }else {
                           ToastUtils.showToast("激活终端失败..");
                        }
                    }
                }
            }
        });
    }

    */
/***
     * 相关业务参数同步
     *//*

    public  void goToNextActivity() {
        final TreeMap<String,String> params = new TreeMap<>();
        params.put(CommonParams.TYPE, TransParamsValue.AntCompanyInterfaceType.POSPARMSET);
        params.put(CommonParams.TERMINAL_NO, ParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID)); //终端号
        ScanPayBean scanPay = new ScanPayBean();
        scanPay.setRequestUrl(CommonContants.url);
    //   AsyncHttpUtil.setCommonBean(scanPay);
        AsyncHttpUtil.httpPostXutils(params,scanPay,new AsyncRequestCallBack<String>(){
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                LogUtils.i("异常信息",errorMsg);
                ToastUtils.showToast(errorMsg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                super.onSuccess(responseInfo);
                if (!StringUtil.isEmpty(responseInfo.result)){
                    LogUtils.i("返回业务数据为:"+responseInfo.result);
                    ParamsValueSny paramsVaule = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, ParamsValueSny.class);
                    if (null != paramsVaule){
                        if (ReturnCodeParams.SUCESS_CODE.equals(paramsVaule.getReturnCode())){
                            //超时时间
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_COMMUNICATION_OUT_TIME,paramsVaule.getTimeOut());
                            //重试次数
                            ParamsUtil.getInstance().save(TransParams.RECONN_TIMES,paramsVaule.getRetryNum());
                            //电话
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_DIAL_PHONE1,paramsVaule.getPhNo1());
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_DIAL_PHONE2,paramsVaule.getPhNo2());
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_DIAL_PHONE3,paramsVaule.getPhNo3());
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_MANAGE_PHONE,paramsVaule.getPhNoMng());
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_IS_CARD_INPUT,paramsVaule.getSupInputCard());
                            ParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME,paramsVaule.getMerNam()); //商户名称
                            //交易重发次数(冲正重发次数)
                            ParamsUtil.getInstance().update(TransParams.RECONN_TIMES, paramsVaule.getRetranNm());
                           */
/*支持的交易类型*//*

                            for (int i = 0; i < 20; i++) {
                                boolean flag = paramsVaule.getTxnList().charAt(i) - '0' != 0;
                                switch (i) {
                                    case 8:
                                        ShareScanPreferenceUtils.putBoolean(InputverifyCodeActivity.this,TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_WEIXIN, flag);
                                        break;
                                    case 9:
                                        ShareScanPreferenceUtils.putBoolean(InputverifyCodeActivity.this,TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_ALIPAY, flag);
                                        break;
                                    case 10:
                                        ShareScanPreferenceUtils.putBoolean(InputverifyCodeActivity.this,TransParamsValue.PARAMS_KEY_TRANS_SCAN_POS_WEIXIN, flag);
                                        break;
                                    case 11:
                                        ShareScanPreferenceUtils.putBoolean(InputverifyCodeActivity.this,TransParamsValue.PARAMS_KEY_TRANS_SCAN_POS_ALIPAY, flag);
                                        break;
                                    case 12:
                                        ShareScanPreferenceUtils.putBoolean(InputverifyCodeActivity.this,TransParamsValue.PARAMS_KEY_TRANS_SCAN_REFUND, flag);
                                        break;
                                    */
/*case 14:
                                        SharePreferenceUtils.putBoolean(ParamsConts.PARAMS_KEY_TRANS_SCAN_PHONE_YLPAY, flag);
                                        break;
                                    case 15:
                                        SharePreferenceUtils.putBoolean(ParamsConts.PARAMS_KEY_TRANS_SCAN_POS_YLPAY, flag);
                                        break;*//*

                                    default:
                                        break;
                                }
                            }
                            //参数下载成功
                            ShareScanPreferenceUtils.putBoolean(InputverifyCodeActivity.this,TransParamsValue.PARAMS_IS_PARAM_DOWN, false);
                            ShareScanPreferenceUtils.putString(InputverifyCodeActivity.this,TransParamsValue.PARAMS_TRANSMIT_DATE, DateTimeUtil.getCurrentDate());
                            startLauncherActivity();
                        }else {
                            ToastUtils.showToast("返回异常码:"+paramsVaule.getReturnCode()+"异常信息:"+paramsVaule.getMessage());
                        }
                    }else {
                        ToastUtils.showToast("解析数据有误");
                    }
                }else {
                    ToastUtils.showToast("返回数据为空");
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
            }
        });
    }

    public void startLauncherActivity(){
        Intent intent = new Intent(this,LauncherActivity.class);
        startActivity(intent);
        finish();
    }
}
*/
