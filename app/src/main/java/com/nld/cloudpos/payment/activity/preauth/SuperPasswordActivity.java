package com.nld.cloudpos.payment.activity.preauth;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.FragmentContainerActivity;
import com.nld.cloudpos.bankline.fragment.SettingFragment;
import com.nld.cloudpos.payment.activity.OffLineTransRefundActivity;
import com.nld.cloudpos.payment.activity.OnLineSwipeCardActivity;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.cloudpos.payment.controller.ScanErrorActivity;
import com.nld.cloudpos.payment.controller.ScanRefundAuth;
import com.nld.cloudpos.util.CommonContants;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.activity.StartScanActivity;
import com.nld.starpos.wxtrade.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.ScanTransFlagUtil;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.utils.params.TransType;

import common.SHA256Util;
import common.StringUtil;

public class SuperPasswordActivity extends BaseAbstractActivity implements OnClickListener {
    public static final int CHECK_SETTING_ADMIN = 200;//设置
    @ViewInject(R.id.et_password)
    EditText edt_psw;
    @ViewInject(R.id.btn_ok)
    Button btn_ok;
    private String inputPw ;


    @Override
    public int contentViewSourceID() {
        return R.layout.check_password;
    }

    @Override
    public void initView() {
        setTopDefaultReturn();
        ViewUtils.inject(this);
        btn_ok.setOnClickListener(this);
        initData();
        edt_psw.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int action, KeyEvent event) {
                if( action == EditorInfo.IME_ACTION_DONE ||
                        action == EditorInfo.IME_ACTION_SEND ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    String vaule =edt_psw.getText().toString().trim();
                    if(StringUtil.isEmpty(vaule)){
                        return true;
                    }
                    doCheckPasssWord(vaule);
                    return true;
                }
                return false;
            }
        });
    }



    /**
     * 隐藏系统键盘
     * @param v
     */
    @Override
    public void hideSystemKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void initData() {
        if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_CONSUME_CX)) {
            setTopTitle("消费撤销");
        } else if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)) {
            setTopTitle("预授权完成撤销");
        } else if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_LJTH)) {
            setTopTitle("退货");
        } else if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_PRE_CX)) {
            setTopTitle("预授权撤销");
        }else if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_WX_TH)) {
            setTopTitle("扫码退货");
        }else  if (TransConstans.SETTING_PASSWORD.equals(Cache.getInstance().getTransCode())){  //应用设置
            setTopTitle("应用设置");
        } else {
            setTopTitle("系统管理员登录");
        }
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {}

    @Override
    public void onServiceBindFaild() {}

    @Override
    public boolean saveValue() {
        return false;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                inputPw = edt_psw.getText().toString().trim();
                if (TextUtils.isEmpty(inputPw)) {
                    ToastUtils.showToast("请输入密码");
                    return;
                }
                doCheckPasssWord(inputPw);
                break;

            default:
                break;
        }
    }


    private void checkPwSucess() {

         if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_LJTH)) { //联机退货
                goToNextActivity(OnLineSwipeCardActivity.class);
                finish();
         } else if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_PRE_CX)) { //预授权撤销
                goToNextActivity(PreAuthCancelSubmitActivity.class);
                finish();
         }else if (TransConstans.TRANS_CODE_PRE_COMPLET_CX.equals(Cache.getInstance().getTransCode())||
                    Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_CONSUME_CX)){      //预授权完成撤销,消费撤销
                goToNextActivity(PreAuthCompleteRefundActivity.class);
                finish();
         } else if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_WX_TH)) {  //扫码退款
                startActivity(new Intent(this, ScanRefundAuth.class));
                finish();
         }else  if (TransConstans.SETTING_PASSWORD.equals(Cache.getInstance().getTransCode())){  //应用设置
                FragmentContainerActivity.startFragment(this, SettingFragment.class.getName(), getString(R.string.tab_setting));
                finish();
         }else if (TransConstans.TRANS_CODE_OFF_TH.equals(Cache.getInstance().getTransCode())){  //脱机退货
             goToNextActivity(OffLineTransRefundActivity.class);
         }
    }

    private void doCheckPasssWord(String password) {
        ScanCache.getInstance().setTransCode(ScanTransFlagUtil.OPER_PASSWORD_EXIT);
        ScanPayBean scanPayBean = new ScanPayBean();
        scanPayBean.setPassWd(SHA256Util.Sha256(password));
        scanPayBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID));
        scanPayBean.setManagerNo(CommonContants.USER_NO);
        scanPayBean.setTransType(TransParamsValue.AntCompanyInterfaceType.CHECK_OPERATOR_PASS_WORD);
        scanPayBean.setProjectType(EncodingEmun.antCompany.getType());
        scanPayBean.setRequestUrl(CommonContants.url);
        Intent intent = new Intent(this, StartScanActivity.class);
        intent.putExtra("scan",scanPayBean);
        startActivityForResult(intent, TransType.PWD_UPDATE_EXIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == TransType.PWD_UPDATE_EXIT ){
                checkPwSucess();
            }
        }else if (resultCode == RESULT_FIRST_USER){
            startActivity(new Intent(this, ScanErrorActivity.class));
        }
    }
}
