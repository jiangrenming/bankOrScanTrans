package com.nld.cloudpos.payment.activity.preauth;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ShareBankPreferenceUtils;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransParamsUtil;
import com.nld.starpos.wxtrade.utils.ToastUtils;

import common.StringUtil;
import common.Utility;

/**
 * Created by jiangrenming on 2017/12/5.
 * 预授权完成撤销
 */

public class PreAuthCompleteRefundActivity extends BaseAbstractActivity implements View.OnClickListener{


    @ViewInject(R.id.et_input)
    EditText et_input;
    @ViewInject(R.id.tv_confirm)
    TextView tv_confirm;
    private TransRecord record;
    private boolean isInputPwd;
    private boolean isUseCard;
    private String transCode;
    @Override
    public int contentViewSourceID() {
        return R.layout.pre_auth_complete_refund;
    }

    @Override
    public void initView() {
        transCode = Cache.getInstance().getTransCode();
        if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_CONSUME_CX)){
            setTopTitle("消费撤销");
        }else if (TransConstans.TRANS_CODE_PRE_COMPLET_CX.equals(Cache.getInstance().getTransCode())){
            setTopTitle("预授权完成撤销");
        }
        setTopDefaultReturn();
        ViewUtils.inject(this);
        tv_confirm.setOnClickListener(this);
        et_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int action,
                                          KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE
                        || action == EditorInfo.IME_ACTION_SEND
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String result=et_input.getText().toString().trim();
                    done(result);
                    return true;
                }
                return false;
            }
        });

        getDefaultData();
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_confirm:
                String result=et_input.getText().toString().trim();
                done(result);
                break;
            default:
                break;
        }
    }

    private void done(String result) {
        if (StringUtil.isEmpty(result)){
            ToastUtils.showToast("请输入原交易凭证号");
            return;
        }
        if (result.length() >=1 && result.length() <= 6){
            result= Utility.addZeroForNum(result, 6); //补齐6位
            TransRecordDao dao= new TransRecordDaoImpl();
            record = dao.getTransRecordByCondition(TransParamsUtil.getCurrentBatchNo(), result);
            TransRecord revokeRecord = dao.getTransRevokeByCondition(TransParamsUtil.getCurrentBatchNo(), result);
            if(null != revokeRecord){
                showTip("该交易已撤销");
                return;
            }
            if(null== record){
                showTip("无该交易凭证，请确认");
                return;
            }
            if((record.getConditionmode().equals("06") || record.getConditionmode().equals("00"))
                    && record.getTransprocode().equals("000000")
                    && record.getReserve1().equals("0210")){}else{
                showTip("找不到交易记录");
                return ;
            }
            Cache.getInstance().setTransRecord(record);
            Cache.getInstance().setTransMoney(Utility.unformatMount(record.getTransamount()));
            Cache.getInstance().setSerialNo(result);
            if (isUseCard){
                goToNextActivity(PreAuthComCancelSubmitActivity.class);
            }else {
                Cache.getInstance().setSerInputCode("012");
                Cache.getInstance().setCardNo(record.getPriaccount());
                if (isInputPwd){
                    goToNextActivity(PreAuthComCancelGetpinActivity.class);
                }else {
                    /*Intent it=new Intent(mContext,TransStartActivity.class);
                    it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, PrintResultActivity.class.getName());
                    startActivity(it);*/
                    goToNextActivity(new Intent(mContext, StartTransActivity.class));
                }
            }
        }else {
            ToastUtils.showToast("请输入长度大于1且小于6的原交易凭证号");
            return;
        }
    }

    public void getDefaultData() {
        if (TransConstans.TRANS_CODE_CONSUME_CX.equals(transCode)){
            isUseCard = ShareBankPreferenceUtils.getBoolean( ParamsConts.PARAMS_KEY_TRANS_VOID_SWIPE, true);
            isInputPwd= ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_IS_INPUT_TRANS_VOID, true);
        }else if (TransConstans.TRANS_CODE_PRE_COMPLET_CX.equals(transCode)){
            isInputPwd = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_IS_INPUT_AUTH_VOID,true);
            isUseCard = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_AUTH_SALE_VOID_SWIPE,true);
        }
    }
}
