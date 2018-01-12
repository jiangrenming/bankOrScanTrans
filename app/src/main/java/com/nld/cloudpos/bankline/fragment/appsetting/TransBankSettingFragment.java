package com.nld.cloudpos.bankline.fragment.appsetting;

import android.support.annotation.IdRes;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.ShareBankPreferenceUtils;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/9/20.
 * 银行卡交易设置
 */

public class TransBankSettingFragment extends BaseFragment implements View.OnClickListener{

    @ViewInject(R.id.rg_auth_swipe)
    RadioGroup rg_auth_swipe;    //预授权完成 卡

    @ViewInject(R.id.rg_default_pay)
    RadioGroup rg_default_pay;   //默认消费

    @ViewInject(R.id.rg_sale_void_pin)
    RadioGroup rg_sale_void_pin;   //是否使用免签免密

    @ViewInject(R.id.rg_sale_void_swipe)
    RadioGroup rg_sale_void_swipe;  //消费撤销 卡

    @ViewInject(R.id.rg_auth_void_swipe)
    RadioGroup rg_auth_void_swipe;   //预授权完成撤销 卡

    @ViewInject(R.id.rg_sale_cancel_input)
    RadioGroup rg_sale_cancel_input;   //消费撤销 密码

    @ViewInject(R.id.rg_authvoid_input)
    RadioGroup rg_authvoid_input;    //预授权撤销 密码

    @ViewInject(R.id.rg_authsale_void_input)
    RadioGroup rg_authsale_void_input;  //预授权完成撤销 密码

    @ViewInject(R.id.rg_authsale_input)
    RadioGroup rg_authsale_input;    //预授权完成 密码

    @ViewInject(R.id.rg_reserse)
    RadioGroup rg_reserse;     // 冲正超时 是否需要交易查询

    Button save_change;

    private int defaultPay;
    private boolean isSaleVoidSwipe;
    private boolean isAuthVoidSwipe;
    private boolean isAuthSwipe;
    private boolean isVoidInputPwd;
    private boolean isAuthVoidInputPwd;
    private boolean isAuthSaleVoidInputPwd;
    private boolean isAuthSaleInputPwd;
    private boolean isReserse;
    private String pinPad;
    @Override
    public int doGetContentViewId() {
        return R.layout.set_trans_fragment;
    }

    @Override
    public void doInitSubViews(View view) {
        ViewUtils.inject(this,view);
        save_change= (Button) view.findViewById(R.id.btn_save);
        save_change.setOnClickListener(this);
    }

    @Override
    public void doInitData() {

        defaultPay = ShareBankPreferenceUtils.getInt(ParamsConts.PARAMS_KEY_DEFAULT_TRANS_TYPE,1);
        if (defaultPay == 0) {
            rg_default_pay.check(R.id.rb_preauth);
        } else {
            rg_default_pay.check(R.id.rb_sale);
        }
        //消费撤销是否用卡
        isSaleVoidSwipe = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_TRANS_VOID_SWIPE,true); // 默认是true,可以改
        if (isSaleVoidSwipe) {
            rg_sale_void_swipe.check(R.id.rb_sale_void_yes);
        } else {
            rg_sale_void_swipe.check(R.id.rb_sale_void_no);
        }
        //预授权完成是否用卡
        isAuthSwipe = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_AUTH_SALE_SWIPE,true); // 默认是true
        if (isAuthSwipe) {
            rg_auth_swipe.check(R.id.rb_auth_yes);
        } else {
            rg_auth_swipe.check(R.id.rb_auth_no);
        }

         pinPad = ParamsUtil.getInstance().getParam(ParamsConts.PINPAD_PIN);
        if (!StringUtil.isEmpty(pinPad) && ParamsConts.NO_PIN.equals(pinPad)){ //免密免签
            rg_sale_void_pin.check(R.id.rb_sale_void_yes_pin);
        }else {
            rg_sale_void_pin.check(R.id.rb_sale_void_no_pin);
        }
        //预授权完成撤销是否用卡
        isAuthVoidSwipe = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_AUTH_SALE_VOID_SWIPE,true);
        if (isAuthVoidSwipe) {
            rg_auth_void_swipe.check(R.id.rb_auth_void_yes);
        } else {
            rg_auth_void_swipe.check(R.id.rb_auth_void_no);
        }
        //消费撤销是否输密
        isVoidInputPwd = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_IS_INPUT_TRANS_VOID,false);
        if (isVoidInputPwd) {
            rg_sale_cancel_input.check(R.id.rb_cancel_input_yes);
        } else {
            rg_sale_cancel_input.check(R.id.rb_cancel_input_no);
        }

        //预授权撤销是否输密
        isAuthVoidInputPwd = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_IS_INPUT_AUTH_VOID,true);
        if (isAuthVoidInputPwd) {
            rg_authvoid_input.check(R.id.rb_authvoid_input_yes);
        } else {
            rg_authvoid_input.check(R.id.rb_authvoid_input_no);
        }
        //预授权完成撤销是否输密
        isAuthSaleVoidInputPwd = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_IS_AUTH_SALE_VOID_PIN,true);
        if (isAuthSaleVoidInputPwd) {
            rg_authsale_void_input.check(R.id.rb_authsale_void_input_yes);
        } else {
            rg_authsale_void_input.check(R.id.rb_authsale_void_input_no);
        }
        //预授权完成请求是否输密
        isAuthSaleInputPwd = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_IS_AUTH_SALE_PIN,true);
        if (isAuthSaleInputPwd) {
            rg_authsale_input.check(R.id.rb_authsale_input_yes);
        } else {
            rg_authsale_input.check(R.id.rb_authsale_input_no);
        }
        //冲正超时15s是否交易查询
        isReserse = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_IS_RESERVE_TIME,true);
        if (isReserse){
            rg_reserse.check(R.id.rg_reserse_yes);
        }else {
            rg_reserse.check(R.id.rg_reserse_no);
        }

        rg_default_pay.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_preauth) {
                    defaultPay = 0;
                } else {
                    defaultPay = 1;
                }
            }
        });

        rg_sale_void_pin.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (checkedId == R.id.rb_sale_void_yes_pin) {
                    pinPad = ParamsConts.NO_PIN;
                } else {
                    pinPad = ParamsConts.NEED_PIN;
                }
            }
        });

        rg_sale_void_swipe.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_sale_void_yes) {
                    isSaleVoidSwipe = true;
                } else {
                    isSaleVoidSwipe = false;
                }
            }
        });

        rg_auth_void_swipe.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_auth_void_yes) {
                    isAuthVoidSwipe = true;
                } else {
                    isAuthVoidSwipe = false;
                }
            }
        });

        rg_sale_cancel_input.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_cancel_input_yes) {
                    isVoidInputPwd = true;
                } else {
                    isVoidInputPwd = false;
                }
            }
        });


        rg_authvoid_input.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_authvoid_input_yes) {
                    isAuthVoidInputPwd = true;
                } else {
                    isAuthVoidInputPwd = false;
                }
            }
        });

        rg_authsale_void_input.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_authsale_void_input_yes) {
                    isAuthSaleVoidInputPwd = true;
                } else {
                    isAuthSaleVoidInputPwd = false;
                }
            }
        });

        rg_authsale_input.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_authsale_input_yes) {
                    isAuthSaleInputPwd = true;
                } else {
                    isAuthSaleInputPwd = false;
                }
            }
        });

        rg_auth_swipe.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_auth_yes) {
                    isAuthSwipe = true;
                } else {
                    isAuthSwipe = false;
                }
            }
        });

        rg_reserse.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (checkedId == R.id.rg_reserse_yes) {
                    isReserse = true;
                } else {
                    isReserse = false;
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_save:
                ShareBankPreferenceUtils.putInt(ParamsConts.PARAMS_KEY_DEFAULT_TRANS_TYPE, defaultPay);
                ShareBankPreferenceUtils.putBoolean(ParamsConts.PARAMS_KEY_TRANS_VOID_SWIPE, isSaleVoidSwipe);
                ShareBankPreferenceUtils.putBoolean(ParamsConts.PARAMS_KEY_AUTH_SALE_SWIPE, isAuthSwipe);
                ShareBankPreferenceUtils.putBoolean(ParamsConts.PARAMS_KEY_AUTH_SALE_VOID_SWIPE, isAuthVoidSwipe);
                ShareBankPreferenceUtils.putBoolean(ParamsConts.PARAMS_KEY_IS_INPUT_TRANS_VOID, isVoidInputPwd);
                ShareBankPreferenceUtils.putBoolean(ParamsConts.PARAMS_KEY_IS_INPUT_AUTH_VOID, isAuthVoidInputPwd);
                ShareBankPreferenceUtils.putBoolean(ParamsConts.PARAMS_KEY_IS_AUTH_SALE_VOID_PIN, isAuthSaleVoidInputPwd);
                ShareBankPreferenceUtils.putBoolean(ParamsConts.PARAMS_KEY_IS_AUTH_SALE_PIN, isAuthSaleInputPwd);
                ShareBankPreferenceUtils.putBoolean(ParamsConts.PARAMS_KEY_IS_RESERVE_TIME, isReserse);
                ParamsUtil.getInstance().update(ParamsConts.PINPAD_PIN,pinPad);
                toast(getString(R.string.tip_save_success));
                break;
            default:
                break;
        }
    }
}
