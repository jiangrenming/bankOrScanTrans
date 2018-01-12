package com.nld.cloudpos.bankline.fragment.appsetting;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.payment.controller.ScanErrorActivity;
import com.nld.cloudpos.util.CommonContants;
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

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

/**
 * Created by jidongdong on 2017/2/7.
 */

public class PWDModifyFragment extends BaseFragment implements View.OnClickListener {

    private EditText et_current_pwd, et_new_pwd, et_new_pwd_confirm;
    private Button btn_confirm;

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_layou_app_pwd_modify;
    }

    @Override
    public void doInitSubViews(View view) {
        et_current_pwd = queryViewById(R.id.et_current_pwd);
        et_new_pwd = queryViewById(R.id.et_new_pwd);
        et_new_pwd_confirm = queryViewById(R.id.et_new_pwd_confirm);
        btn_confirm = queryViewById(R.id.btn_confirm);
    }

    @Override
    public void doInitData() {
        btn_confirm.setOnClickListener(this);
        et_new_pwd.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}
            @Override
            public void afterTextChanged(Editable s) {
                String editable = s.toString();
                if(editable.length() ==0){
                    return;
                }
                char ch=editable.charAt(editable.length()-1);
                if( (ch>='0' &&ch<='9') ||(ch>='a' && ch<='z')){
                }else{
                    String newStr  = editable.substring(0,editable.length()-1);
                    if(newStr==null){
                        newStr="";
                    }
                    et_new_pwd.setText(newStr);
                }
            }
        });
        et_new_pwd_confirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int action, KeyEvent event) {
                if( action == EditorInfo.IME_ACTION_DONE ||
                        action == EditorInfo.IME_ACTION_SEND ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    String vaule = et_new_pwd_confirm.getText().toString().trim();
                    if(StringUtil.isEmpty(vaule)){
                        return true;
                    }
                    changePwd();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm) {
            changePwd();
        }
    }

    /**
     * 修改密码
     */
    private void changePwd() {
        String oldPwd = et_current_pwd.getText().toString().trim();
        if(StringUtil.isEmpty(oldPwd)){
            ToastUtils.showToast(getString(R.string.input_old_password));
            return;
        }
        String newPwd = et_new_pwd.getText().toString().trim();
        if(StringUtil.isEmpty(newPwd)){
            ToastUtils.showToast(getString(R.string.input_new_password));
            return;
        }
        if(newPwd.length() <6 ||newPwd.length() >12){
            ToastUtils.showToast(getString(R.string.new_password_length));
            return;
        }
        String newPwd2 = et_new_pwd_confirm.getText().toString().trim();
        if(!newPwd.equals(newPwd2)){
            ToastUtils.showToast(getString(R.string.twice_password_diff));
            return;
        }

        ScanCache.getInstance().setTransCode(ScanTransFlagUtil.PASSWORD_CHANGE);
        ScanPayBean scanPayBean = new ScanPayBean();
        scanPayBean.setOldPassword(SHA256Util.Sha256(oldPwd));
        scanPayBean.setNewPassword(SHA256Util.Sha256(newPwd));
        scanPayBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID));
        scanPayBean.setManagerNo(CommonContants.USER_NO);
        scanPayBean.setTransType(TransParamsValue.AntCompanyInterfaceType.UPDATE_PASS_WORD);
        scanPayBean.setProjectType(EncodingEmun.antCompany.getType());
        scanPayBean.setRequestUrl(CommonContants.url);
        Intent intent = new Intent(getActivity(), StartScanActivity.class);
        intent.putExtra("scan",scanPayBean);
        startActivityForResult(intent, TransType.PWD_UPDATE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == TransType.PWD_UPDATE_CODE){
                ToastUtils.showToast("密码修改成功");
                finish();
            }
        }else if (resultCode == RESULT_FIRST_USER){
            startActivity(new Intent(getActivity(), ScanErrorActivity.class));
        }
    }
}
