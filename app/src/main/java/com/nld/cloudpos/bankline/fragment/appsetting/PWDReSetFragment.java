package com.nld.cloudpos.bankline.fragment.appsetting;

import android.content.Intent;
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

import common.StringUtil;

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

/**
 * Created by jidongdong on 2017/2/7.
 */

public class PWDReSetFragment extends BaseFragment implements View.OnClickListener {

    private Button btn_confirm;
    private EditText et_reset_pwd;

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_layou_app_pwd_reset;
    }

    @Override
    public void doInitSubViews(View view) {
        btn_confirm = queryViewById(R.id.btn_confirm);
        et_reset_pwd = queryViewById(R.id.et_reset_pwd);
        btn_confirm.setOnClickListener(this);
        et_reset_pwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int action, KeyEvent event) {
                if( action == EditorInfo.IME_ACTION_DONE ||
                        action == EditorInfo.IME_ACTION_SEND ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    String vaule =et_reset_pwd.getText().toString().trim();
                    if(StringUtil.isEmpty(vaule)){
                        return true;
                    }
                    resetPwd(vaule);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm) {
            String oper_Id = et_reset_pwd.getText().toString();
            if (StringUtil.isEmpty(oper_Id)) {
                ToastUtils.showToast("操作员账号不能为空");
                return;
            }
            resetPwd(oper_Id);
        }
    }

    private void resetPwd(String value) {
        String admin_Pwd = ScanParamsUtil.getInstance().getParam("adminpwd");
        ScanPayBean scanPayBean = new ScanPayBean();
        scanPayBean.setManagerNo(CommonContants.USER_NO); //主管账号
        scanPayBean.setPassWd(admin_Pwd); //主管操作密码
        scanPayBean.setOprId(value);  //操作员账号
        scanPayBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID));
        scanPayBean.setTransType(TransParamsValue.AntCompanyInterfaceType.OPER_NO_UPDATE);
        scanPayBean.setProjectType(EncodingEmun.antCompany.getType());
        scanPayBean.setRequestUrl(CommonContants.url);
        ScanCache.getInstance().setTransCode(ScanTransFlagUtil.OPER_PASSWORD_CHANGE);
        Intent intent = new Intent(getActivity(), StartScanActivity.class);
        intent.putExtra("scan",scanPayBean);
        startActivityForResult(intent, TransType.PWD_UPDATE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == TransType.PWD_UPDATE_CODE){
                ToastUtils.showToast("密码重置成功");
                finish();
            }
        }else if (resultCode == RESULT_FIRST_USER){
            startActivity(new Intent(getActivity(), ScanErrorActivity.class));
        }
    }
}
