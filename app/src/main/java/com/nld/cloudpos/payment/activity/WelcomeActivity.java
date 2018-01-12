package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.payment.activity.preauth.SuperPasswordActivity;
import com.nld.cloudpos.payment.controller.AbstractActivity;
import com.nld.cloudpos.payment.controller.BindDownInfos;
import com.nld.cloudpos.payment.controller.BindTemSn;
import com.nld.cloudpos.payment.controller.TransUtils;
import com.nld.cloudpos.router.RouterService;
import com.nld.cloudpos.util.CommonContants;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.ShareBankPreferenceUtils;
import com.nld.starpos.banktrade.utils.TransParams;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import common.StringUtil;

/**
 * 欢迎界面
 */
public class WelcomeActivity extends AbstractActivity {

    private static final int REQ_CHECK_ADMIN = 100;

    @Override
    public int contentViewSourceID() {
        return R.layout.welcome_activity;
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {}

    //获取，存储终端号，灌入主密钥,SN ,CSN《这两个参数作为请求的参数，来请求下发主密钥和终端号》 ,TerminalNo<终端号是请求接口下发的>
    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {
        if (null != service) {
            String CSN = BindTemSn.getCSN(service);
            ShareScanPreferenceUtils.putString(WelcomeActivity.this, CommonParams.POSSn, CSN);
            String sn = BindTemSn.getSN(service);
            ShareScanPreferenceUtils.putString(WelcomeActivity.this, CommonParams.SN, sn);
            Log.i("TAG", "csn=" + CSN + "SN=" + sn);
            loadScanShopId(); //查询终端绑定与终端绑定操作
        }
    }


    @Override
    public void onServiceBindFaild() {
    }

    @Override
    public boolean saveValue() {
        return false;
    }

    /**
     * 系统设置
     *
     * @param v
     * @createtor：Administrator
     * @date:2015-8-17 下午12:26:07
     */
    public void sysSetting(View v) {
        Cache.getInstance().setTransCode("000000");
        Intent intent = new Intent(WelcomeActivity.this, SuperPasswordActivity.class);
        intent.putExtra("requestCode", SuperPasswordActivity.CHECK_SETTING_ADMIN);
        startActivityForResult(intent, REQ_CHECK_ADMIN);
    }


    private BindDownInfos bindDownInfos;

    private void loadScanShopId() {

        //下发二维码商户信息
        final TreeMap<String, String> scanMap = new TreeMap<>();
        scanMap.put(CommonParams.TYPE, TransParamsValue.AntCompanyInterfaceType.SCAN_SHOP_ID); //扫码接口类型
        String posSn = ShareScanPreferenceUtils.getString(WelcomeActivity.this, CommonParams.POSSn, null);
        if (!StringUtil.isEmpty(posSn)) {
            scanMap.put(CommonParams.SN, posSn);  //终端序列号sn
        }
        ScanPayBean bean = new ScanPayBean();
        String transNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO); //流水号
        bean.setRequestId(System.currentTimeMillis() + transNo);
        bean.setTransType(TransParamsValue.AntCompanyInterfaceType.SCAN_SHOP_ID);
        bean.setProjectType(EncodingEmun.antCompany.getType());
        bean.setRequestUrl(CommonContants.url);
 //       AsyncHttpUtil.setCommonBean(bean);
        AsyncHttpUtil.httpPostXutils(scanMap,bean,new AsyncRequestCallBack<String>() {
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                Log.i("TAG--", errorMsg.toString());
                String expCode = NldException.getExpCode(httpException, NldException.ERR_NET_DEFAULT_E102);
                String msg = NldException.getMsg(expCode);
                ToastUtils.showToast(msg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
               super.onSuccess(responseInfo);
                if (!StringUtil.isEmpty(responseInfo.result)) {
                    LogUtils.i("终端查询获取数据:"+responseInfo.result);
                    bindDownInfos = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, BindDownInfos.class);
                    if ("Y".equals(bindDownInfos.getTrmSts())) {  //终端绑定
                        LogUtils.i("终端查询 终端已绑定");
                        if (TransUtils.isExitLocalData()) {  //本地存在数据
                            if (!isSameData()) {  //数据是否相同
                                LogUtils.d("终端查询--- 本地数据与后台不一致，开始无激活码");
                                //删除本地数据库
                                TransUtils.clearWater();
                                saveBindInfos();
                            }
                        } else {
                            LogUtils.d("终端查询 ,本地不存在数据，开始无激活码绑定");
                            saveBindInfos();
                        }
                        startLauncherActivity();
                    } else if ("N".equals(bindDownInfos.getTrmSts())) {  //无终端绑定 ,跳转到输入激活码的界面，激活绑定
                        ToastUtils.showToast("终端未绑定激活，请先绑定激活");
                        if (TransUtils.isExitLocalData()) {
                            ToastUtils.showToast("终端未绑定激活，请先绑定激活");
                            return;
                        }
                    }
                } else {  //无激活码绑定
                    if (TransUtils.isExitLocalData()) {
                        Log.d("TAG", "本地存在数据，开始无激活码绑定");
                        ToastUtils.showToast("网络获取数据异常");
                        return;
                    }
                }
            }
        });
    }
    /**
     * 跳转到主页面
     */
    private void startLauncherActivity() {
        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveBindInfos() {
        try {
            //更新本地数据
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.MD5_KEY, bindDownInfos.getMd5Key());  //md5
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID, bindDownInfos.getTerminalNo()); //终端号
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_QR_CODE_ACCOUNT, bindDownInfos.getPayStlAc()); //扫码结算账号
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID, bindDownInfos.getPayMercId()); //扫码商户号
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME, bindDownInfos.getMercNm()); //商户名称
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_SHOP_ID, bindDownInfos.getStoreNo()); //门店号
          /*  if (StringUtil.isEmpty(bindDownInfos.getUnionMercId()) || StringUtil.isEmpty(bindDownInfos.getUnionTrmNo())){
                ToastUtils.showToast("银行卡商终信息无法获取,请稍后重试");
                ParamsUtil.getInstance(WelcomeActivity.this).update(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID,"");  //银联商户号
                ParamsUtil.getInstance(WelcomeActivity.this).update(ParamsConts.BindParamsContns.UNIONPAY_TERMID,""); //银联终端号
            }else {
                ParamsUtil.getInstance(WelcomeActivity.this).update(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID, bindDownInfos.getUnionMercId());  //银联商户号
                ParamsUtil.getInstance(WelcomeActivity.this).update(ParamsConts.BindParamsContns.UNIONPAY_TERMID,bindDownInfos.getUnionTrmNo()); //银联终端号
            }*/
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID, Constant.MER_NO);  //银联商户号
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.UNIONPAY_TERMID, Constant.TERM_ID); //银联终端号
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_KEY_CARD_ACCOUNT, bindDownInfos.getPosStlAc()); //银行卡结算账号
            //更新配置数据库信息
            ParamsUtil.getInstance().update(ParamsConts.TransParamsContns.TYANS_BATCHNO, "000001"); //银行卡批次号
            ParamsUtil.getInstance().update(ParamsConts.TransParamsContns.SYSTRANCE_NO, "000001"); //银行卡流水号

            //签到,参数更新状态
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_CAVERSION, "00"); //IC卡公钥
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_PARAMVERSION, "00"); //IC卡参数
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_UPDATASTATUS, "0"); //更新状态
            ParamsUtil.getInstance().update(ParamsConts.SIGN_SYMBOL, TransParams.SingValue.UnSingedValue); //签到更为未签到

            ScanParamsUtil.getInstance().update(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO, "000001"); //扫码批次号
            ScanParamsUtil.getInstance().update(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO, "000001"); //扫码流水号

            ShareBankPreferenceUtils.putBoolean(ParamsConts.PARAMS_CARD_SETTLE_SUCESS, true);
            //参数传递
            ShareScanPreferenceUtils.putBoolean(WelcomeActivity.this, TransParamsValue.PARAMS_IS_PARAM_DOWN, true);
            //更新是否为第一次启动
            ShareScanPreferenceUtils.putBoolean(WelcomeActivity.this, TransParamsValue.PARAMS_KEY_IS_FIRST, true);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("终端查询更新数据异常:" + e.getMessage());
        }
    }


    private void initParamConfig() {
        //重新倒入配置参数
        ParamConfigDao paramConfigDao = new ParamConfigDaoImpl();
        //paramConfigDao.dropTable();
        //把商终号倒入数据库
        String bankLineMerchantNo = Constant.MER_NO;
        String bankLineTerminalId = Constant.TERM_ID;
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID, bankLineMerchantNo);//银联直联商户号
        dataMap.put("unionpay_termid", bankLineTerminalId);//银联直联终端号
        paramConfigDao.save(dataMap);
    }

    /**
     * 判断本地的数据是否相同
     *
     * @return
     */
    public boolean isSameData() {
   //     String posMeCid = bindDownInfos.getUnionMercId(); //银行商户号
   //     posMeCid = (TextUtils.isEmpty(posMeCid) ? "" : posMeCid);
        String posMeCid = ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID);
        String payMercId = bindDownInfos.getPayMercId(); //扫码商户号
        payMercId = (TextUtils.isEmpty(payMercId) ? "" : payMercId);
        String terminalNo = bindDownInfos.getTerminalNo(); //终端号
        String data1 = posMeCid + payMercId + terminalNo;
        LogUtils.i("网络数据data1=" + data1 + "/posMeCid=" + posMeCid + "/payMercId=" + payMercId + "/terminalNo=" + terminalNo);
        String bankMerchantId = ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID);
        String scanMerchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID);
        scanMerchantId = (TextUtils.isEmpty(scanMerchantId) ? "" : scanMerchantId);
        String terminal = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID);
        String data2 = bankMerchantId + scanMerchantId + terminal;
        LogUtils.i("网络数据data2=" + data2 + "/bankMerchantId=" + bankMerchantId + "/scanMerchantId=" + scanMerchantId + "/terminal=" + terminal);
        return data1.equals(data2);
    }

}