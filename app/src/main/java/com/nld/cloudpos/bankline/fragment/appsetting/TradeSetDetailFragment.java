package com.nld.cloudpos.bankline.fragment.appsetting;

import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

/**
 * Created by jidongdong on 2017/2/7.
 * <p>
 * 交易设置Fragmnent
 */

public class TradeSetDetailFragment extends BaseFragment implements View.OnClickListener {

    private RadioGroup rg_pay_type,rg_scanner;
    private Button btn_save_change;

    private int scan_pos_phone;
    private int scanner;

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_layou_app_set_trade_detail;
    }

    @Override
    public void doInitSubViews(View view) {
        btn_save_change = queryViewById(R.id.btn_save_change);
        btn_save_change.setOnClickListener(this);
        rg_pay_type = (RadioGroup) view.findViewById(R.id.rg_pay_type);
        rg_scanner = (RadioGroup) view.findViewById(R.id.rg_scanner);
        scanner = ShareScanPreferenceUtils.getInt(getActivity(), TransParamsValue.PARAMS_KEY_SCANNER, 1); //后置
        scan_pos_phone = ShareScanPreferenceUtils.getInt(getActivity(), TransParamsValue.PARAMS_KEY_SCAN_POS_PHONE, 0);
        if (scan_pos_phone == 0){
            rg_pay_type.check(R.id.rb_scan_phone);  //扫手机
        }else {
            rg_pay_type.check(R.id.rb_scan_pos);  //扫pos
        }
        if (scanner == 0){
            rg_scanner.check(R.id.rb_front_scanner);  //前置摄像头
        }else {
            rg_scanner.check(R.id.rb_back_scanner);  //后置摄像头
        }

        rg_pay_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rb_scan_phone){
                    scan_pos_phone= 0;
                }else{
                    scan_pos_phone=1;
                }
            }
        });

        rg_scanner.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rb_front_scanner){
                    scanner= 0;
                }else{
                    scanner=1;
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (R.id.btn_save_change == view.getId()) {
            saveChange();
        }
    }

    /**
     * 保存设置
     */
    private void saveChange() {
        ShareScanPreferenceUtils.putInt(getActivity(), TransParamsValue.PARAMS_KEY_SCAN_POS_PHONE,scan_pos_phone);
        ShareScanPreferenceUtils.putInt(getActivity(), TransParamsValue.PARAMS_KEY_SCANNER,scanner);
        ToastUtils.showToast("保存成功");
    }
}
