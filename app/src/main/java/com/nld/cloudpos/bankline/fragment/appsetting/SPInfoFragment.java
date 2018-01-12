package com.nld.cloudpos.bankline.fragment.appsetting;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

/**
 * Created by jidongdong on 2017/2/7.
 */

public class SPInfoFragment extends BaseFragment {

    private TextView text_sp_shortname, text_sp_num_1, text_sp_num_2, text_sp_pointnum, text_sp_batnum, text_sp_flownum,text_sp_batnum1,text_sp_flownum1,uniPay_num;
    private Button btn_return;

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_layou_app_set_spinfo;
    }

    @Override
    public void doInitSubViews(View view) {
        text_sp_shortname = queryViewById(R.id.text_sp_shortname);
        text_sp_shortname.setText(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME)); //用户简称
        text_sp_num_1 = queryViewById(R.id.text_sp_num_1);
        text_sp_num_1.setText(ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID)); //银行商户号
        uniPay_num = queryViewById(R.id.uniPay_num);
        uniPay_num.setText(ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID) == null ? "" : ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID)); //银联终端号
        text_sp_num_2 = queryViewById(R.id.text_sp_num_2);
        text_sp_num_2.setText(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID)); //扫码商户号
        text_sp_pointnum = queryViewById(R.id.text_sp_pointnum); //扫码终端号
        text_sp_pointnum.setText(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID));
        text_sp_batnum = queryViewById(R.id.text_sp_batnum); //银行批次号
        text_sp_batnum.setText(ParamsUtil.getInstance().getParam(ParamsConts.TransParamsContns.TYANS_BATCHNO));
        text_sp_flownum = queryViewById(R.id.text_sp_flownum);  //银行流水号
        text_sp_flownum.setText(ParamsUtil.getInstance().getParam(ParamsConts.TransParamsContns.SYSTRANCE_NO));
        text_sp_batnum1 = queryViewById(R.id.text_sp_batnum1); //扫码批次号
        text_sp_batnum1.setText(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO));
        text_sp_flownum1 = queryViewById(R.id.text_sp_flownum1);  //扫码流水号
        text_sp_flownum1.setText(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO));

        btn_return = queryViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }
}
