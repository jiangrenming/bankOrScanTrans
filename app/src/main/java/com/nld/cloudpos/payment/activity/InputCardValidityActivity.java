package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.logger.LogUtils;
import com.nld.starpos.wxtrade.utils.ToastUtils;

import common.StringUtil;


/**
 * @author jiangrenming
 */
public class InputCardValidityActivity extends BaseAbstractActivity implements View.OnClickListener{

    private EditText card_date;
    private TextView tv_authsale_enter;
    
    @Override
    public int contentViewSourceID() {
        return R.layout.act_input_validity;
    }

    @Override
    public void initView() {
        setTopTitle("卡有效期");
        setTopDefaultReturn();
        card_date = (EditText) findViewById(R.id.et_card_date);
        tv_authsale_enter = (TextView)findViewById(R.id.tv_authsale_enter);
        tv_authsale_enter.setOnClickListener(this);
        card_date.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int action,
                                          KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE
                        || action == EditorInfo.IME_ACTION_SEND
                        || action == EditorInfo.IME_ACTION_NEXT
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (checkDate(card_date.getText().toString().trim())) {
                       doNext();
                    } else {
                        ToastUtils.showToast("卡有效期格式错误！");
                    }
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 检测输入的格式是否正确
     * @param str
     * @return
     */
    public boolean checkDate(String str) {
        boolean res = false;
        if (!StringUtil.isEmpty(str) && str.length() == 4) {    //有效期只核对逻辑有效性，具体的值提交给服务器去判断
            if (Integer.valueOf(str.substring(2, 4)) > 0
                    && Integer.valueOf(str.substring(2, 4)) < 13) {
                res = true;
            }
        }
        return res;
    }
    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {}

    @Override
    public void onServiceBindFaild() {}

    @Override
    public boolean saveValue() {
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_authsale_enter){
            if (checkDate(card_date.getText().toString().trim())){
                doNext();
            }else {
                ToastUtils.showToast("请输入卡有效期");
                return;
            }
        }
    }

    private void doNext() {
        String aClass = getIntent().getStringExtra("class");
        try{
            if (!StringUtil.isEmpty(aClass)){
                LogUtils.i("是否跳转");
                Intent it = new Intent(this, Class.forName(aClass));
                goToNextActivity(it);
                finish();
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtils.e("转换数据异常");
        }
    }
}
