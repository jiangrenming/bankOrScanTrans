package com.nld.cloudpos.payment.activity;

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
import com.nld.starpos.wxtrade.utils.ToastUtils;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.StringUtil;
import common.Utility;

/**
 *
 * @author jiangrenming
 * @date 2017/12/14
 * 脱机退货
 */

public abstract  class BaseOffLineRefundActivity  extends BaseAbstractActivity implements View.OnClickListener{

    private EditText offline_et_old_date;
    private EditText offline_et_old_batch;
    private EditText offline_et_amt;
    private EditText offline_et_old_sys;
    private TextView tv_authsale_enter;

    @Override
    public int contentViewSourceID() {
        return R.layout.offline_refund;
    }

    @Override
    public void initView() {
        setTopTitle("脱机退货");
        setTopDefaultReturn();
        offline_et_old_date = (EditText) findViewById(R.id.offline_et_old_date);
        offline_et_old_batch = (EditText) findViewById(R.id.offline_et_old_bitch);
        offline_et_old_sys = (EditText) findViewById(R.id.offline_et_old_sys);
        offline_et_amt = (EditText) findViewById(R.id.off_line_et_amt);
        tv_authsale_enter = (TextView) findViewById(R.id.tv_authsale_enter);
        tv_authsale_enter.setOnClickListener(this);

        offline_et_old_date.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int action,
                                          KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE
                        || action == EditorInfo.IME_ACTION_SEND
                        || action == EditorInfo.IME_ACTION_NEXT
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (checkDate(offline_et_old_date.getText().toString())) {
                        offline_et_old_batch.setFocusable(true);
                        offline_et_old_batch.setFocusableInTouchMode(true);
                        offline_et_old_batch.requestFocus();
                    } else {
                        ToastUtils.showToast("原日期格式错误！");
                    }
                    return true;
                }
                return false;
            }
        });

        offline_et_old_date.addTextChangedListener(watcher);
        offline_et_old_batch.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int action,
                                          KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE
                        || action == EditorInfo.IME_ACTION_SEND
                        || action == EditorInfo.IME_ACTION_NEXT
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (checkLength(offline_et_old_batch.getText().toString())){
                        offline_et_old_sys.setFocusable(true);
                        offline_et_old_sys.setFocusableInTouchMode(true);
                        offline_et_old_sys.requestFocus();
                    }else {
                        ToastUtils.showToast("请输入长度大于1且小于6的原交易批次号");
                    }
                    return true;
                }
                return false;
            }
        });

        offline_et_old_sys.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int action,
                                          KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE
                        || action == EditorInfo.IME_ACTION_SEND
                        || action == EditorInfo.IME_ACTION_NEXT
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (checkLength(offline_et_old_sys.getText().toString())){
                        offline_et_amt.setFocusable(true);
                        offline_et_amt.setFocusableInTouchMode(true);
                        offline_et_amt.requestFocus();
                    }else {
                        ToastUtils.showToast("请输入长度大于1且小于6的原交易凭证号");
                    }
                    return true;
                }
                return false;
            }
        });

        offline_et_amt.setOnEditorActionListener(new TextView.OnEditorActionListener() {

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


    private boolean checkLength(String result){
        if (result.length() >=1 && result.length() <= 6){
            return true;
        }
        return false;
    }

    private boolean allRight() {

        String oldDate = offline_et_old_date.getText().toString().trim();
        if (StringUtil.isEmpty(oldDate) || !checkDate(oldDate)) {
            ToastUtils.showToast("原交易日期输入有误");
            return false;
        }
        Cache.getInstance().setTransDate(oldDate);
        String oldAuth = offline_et_old_batch.getText().toString().trim();
        if (StringUtil.isEmpty(oldAuth)) {
            ToastUtils.showToast("请输入原批次号");
            return false;
        }
        String oldBatch = Utility.addZeroForNum(oldAuth, 6); //补齐6位
        Cache.getInstance().setBatchNo(oldBatch);

        String oldSys = offline_et_old_sys.getText().toString().trim();
        if (StringUtil.isEmpty(oldSys)) {
            ToastUtils.showToast("请输入原流水号");
            return false;
        }
        String oldSystrance = Utility.addZeroForNum(oldSys, 6);
        Cache.getInstance().setOldBatchBillno(oldSystrance);
        String amt = offline_et_amt.getText().toString().trim();
        if (StringUtil.isEmpty(amt)|| TextUtils.equals(amt, "0.00")) {
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
    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {}

    @Override
    public void onServiceBindFaild() {}

    @Override
    public boolean saveValue() {
        return false;
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
