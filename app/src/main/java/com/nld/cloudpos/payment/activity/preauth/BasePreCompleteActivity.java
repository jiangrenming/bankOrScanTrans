package com.nld.cloudpos.payment.activity.preauth;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.utils.ToastUtils;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/12/4.
 *
 */

public abstract  class BasePreCompleteActivity  extends BaseAbstractActivity implements View.OnClickListener{

   private EditText et_old_date;
   private EditText et_old_auth;
   private EditText et_amt;
   private TextView tv_authsale_enter;
   private TextView auth_code;
    private String transCode;

    @Override
    public int contentViewSourceID() {
        return R.layout.input_authcode_layout;
    }

    @Override
    public void initView() {
         transCode = Cache.getInstance().getTransCode();
        if (TransConstans.TRANS_CODE_PRE_COMPLET.equals(transCode)){
            setTopTitle("预授权完成请求");
        }else if (TransConstans.TRANS_CODE_PRE_CX.equals(transCode)){
            setTopTitle("预授权撤销");
        }else if (TransConstans.TRANS_CODE_LJTH.equals(transCode)){
            setTopTitle("联机退货");
        }
        setTopDefaultReturn();
        et_old_date = (EditText) findViewById(R.id.et_old_date);
        et_old_auth = (EditText) findViewById(R.id.et_old_auth);
        et_amt = (EditText) findViewById(R.id.et_amt);
        tv_authsale_enter = (TextView) findViewById(R.id.tv_authsale_enter);
        auth_code = (TextView)findViewById(R.id.auth_code);
        if (TransConstans.TRANS_CODE_LJTH.equals(transCode)){
            auth_code.setText("请输入原凭证号");
        }
        tv_authsale_enter.setOnClickListener(this);
        et_old_date.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int action,
                                          KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE
                        || action == EditorInfo.IME_ACTION_SEND
                        || action == EditorInfo.IME_ACTION_NEXT
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (checkDate(et_old_date.getText().toString())) {
                        et_old_auth.setFocusable(true);
                        et_old_auth.setFocusableInTouchMode(true);
                        et_old_auth.requestFocus();
                    } else {
                        ToastUtils.showToast("原日期格式错误！");
                    }
                    return true;
                }
                return false;
            }
        });

        et_old_date.addTextChangedListener(watcher);
        et_old_auth.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int action,
                                          KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE
                        || action == EditorInfo.IME_ACTION_SEND
                        || action == EditorInfo.IME_ACTION_NEXT
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    et_amt.setFocusable(true);
                    et_amt.setFocusableInTouchMode(true);
                    et_amt.requestFocus();
                    return true;
                }
                return false;
            }
        });

        et_amt.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int action,
                                          KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE
                        || action == EditorInfo.IME_ACTION_SEND
                        || action == EditorInfo.IME_ACTION_NEXT
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (allRight()) {
                        Intent intent = goNextStep();
                        goToNextActivity(intent);
                        return true;
                    }
                    return true;
                }
                return false;
            }
        });
    }

    // 1、正则 表达式月日
    public boolean checkDate(String str) {
        // 要验证的字符串
        // 编译正则表达式
        if (str.equals("0230") || str.equals("0431") || str.equals("0631") || str.equals("0931") || str.equals("1131")) {
            return false;
        }
        // 邮箱验证规则
        String regEx = "^((0[1-9])|(1[012]))((0[1-9])|([12]\\d)|(3[01]))$";
        Pattern pattern = Pattern.compile(regEx);
        // 忽略大小写的写法
        // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        // 字符串是否与正则表达式相匹配
        boolean rs = matcher.matches();

        return rs;
    }
    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {}

    @Override
    public void onServiceBindFaild() {}

    @Override
    public boolean saveValue() {
        return false;
    }

    // 2、描述监听
    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private boolean allRight() {

        String oldDate = et_old_date.getText().toString().trim();
        if (StringUtil.isEmpty(oldDate) || !checkDate(oldDate)) {
            ToastUtils.showToast("原交易日期输入有误");
            return false;
        }
        Cache.getInstance().setTransDate(oldDate);
        String oldAuth = et_old_auth.getText().toString().trim();
        if (StringUtil.isEmpty(oldAuth)) {
            if (TransConstans.TRANS_CODE_LJTH.equals(transCode)){
                ToastUtils.showToast("请输入原凭证号");
            }else {
                ToastUtils.showToast("请输入原授权号");
            }
            return false;
        }
        if (TransConstans.TRANS_CODE_LJTH.equals(transCode)){
            Cache.getInstance().setOldBatchBillno(oldAuth);
        }else {
            Cache.getInstance().setAuthCode(oldAuth);
        }
        String amt = et_amt.getText().toString().trim();
        if (StringUtil.isEmpty(amt)|| TextUtils.equals(amt,"0.00")) {
            ToastUtils.showToast("请输入金额");
            return false;
        }
        String money = amt.replace(".", "").trim();
        if (Long.valueOf(money) / 100 >= 100000000){
            ToastUtils.showToast("金额输入超限！");
            return false;
        }
        try{
            DecimalFormat decfmat = new DecimalFormat("#######0.00");
            Cache.getInstance().setTransMoney(decfmat.format(Double.valueOf(amt)));
        }catch (Exception e){
            e.printStackTrace();
            ToastUtils.showToast("金额输入有误！");
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_authsale_enter:
                if (allRight()){
                    Intent intent = goNextStep();
                    goToNextActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 获取下一个页面的activity
     * @return
     */
    public abstract Intent goNextStep();

}
