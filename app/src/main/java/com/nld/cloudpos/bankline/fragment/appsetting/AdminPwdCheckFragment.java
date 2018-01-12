package com.nld.cloudpos.bankline.fragment.appsetting;

import android.view.View;
import android.widget.Button;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.payment.view.MEditText;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.wxtrade.utils.ToastUtils;

import common.MD5Util;
import common.StringUtil;

/**
 * Created by jidongdong on 2017/2/16.
 * 暂时尚未调用到本类
 * 用于管理员密码校验，且本页面只做管理员密码校验的功能，校验完成会将结果返回的前页面
 * <p>
 * 其他逻辑需要在前页面做处理
 */

public class AdminPwdCheckFragment extends BaseFragment implements View.OnClickListener {
    private Button btn_confirm;
    private MEditText et_reset_pwd;

    @Override
    public void doInitSubViews(View view) {
        btn_confirm = queryViewById(R.id.btn_confirm);
        et_reset_pwd = queryViewById(R.id.et_reset_pwd);
        btn_confirm.setOnClickListener(this);
    }

    @Override
    public boolean onBack() {
        setResult(Constant.ADMIN_PWD_CHECK_RESULT_NO, null);
        return super.onBack();
    }

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_layout_admin_pwd_check;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm) {
            if (StringUtil.isEmpty(et_reset_pwd.getText().toString())) {
                ToastUtils.showToast("填写密码不能为空");
                return;
            }
            String sysoldpsd = ParamsUtil.getInstance().getParam("operpswd"); // 系统管理员旧的密码
            String operate_old_psd = ParamsUtil.getInstance().getParam("adminpwd"); // 操作员旧的密码
            String pwd = MD5Util.getStringMD5(et_reset_pwd.getText().toString().trim());
            pwd = pwd.toUpperCase();
            boolean flag = false;
            if (pwd.equals(operate_old_psd) || pwd.equals(sysoldpsd)) {
                setResult(Constant.ADMIN_PWD_CHECK_RESULT_OK, null);
            } else {
                ToastUtils.showToast("密码错误");
                setResult(Constant.ADMIN_PWD_CHECK_RESULT_NO, null);
            }
            finish();
        }
    }
}