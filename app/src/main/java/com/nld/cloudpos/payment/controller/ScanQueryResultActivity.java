package com.nld.cloudpos.payment.controller;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.activity.PrintResultActivity;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.utils.params.TransType;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/9/29.
 * 扫码查单的结果界面
 */

public class ScanQueryResultActivity extends AbstractActivity implements View.OnClickListener{

    @ViewInject(R.id.tv_no)
    TextView tv_no;
    @ViewInject(R.id.tv_data)
    TextView tv_data;
    @ViewInject(R.id.tv_money)
    TextView tv_money;
    @ViewInject(R.id.tv_type)
    TextView tv_type;
    @ViewInject(R.id.tv_state)
    TextView tv_state;
    @ViewInject(R.id.tv_finish)
    TextView tv_finish;
    @ViewInject(R.id.tv_reprint)
    TextView tv_reprint;

    private ScanTransRecord water;

    @Override
    public int contentViewSourceID() {
        return R.layout.reprint_detial_fragment;
    }

    @Override
    public void initView() {
        ViewUtils.inject(this);
        tv_finish.setOnClickListener(this);
        tv_reprint.setOnClickListener(this);
        setTopDefaultReturn();
    }

    @Override
    public void initData() {
        setTopTitle("扫码查看详情");
        ScanPayBean scanRefundBean = (ScanPayBean) getIntent().getSerializableExtra("scan_query");
        if (null != scanRefundBean){
            water = new ScanTransRecord();
            water.setTranscurrcode("156");
            water.setOper(scanRefundBean.getUserNo());
            water.setMemberName(scanRefundBean.getMercNm());
            water.setOrderNo(scanRefundBean.getLogNo());
            tv_no.setText(water.getOrderNo());
            water.setBatchbillno(scanRefundBean.getBatchNo());
            water.setSystraceno(scanRefundBean.getOrderId());
            water.setTransamount(String.valueOf(scanRefundBean.getAmount()));
            water.setPayChannel(scanRefundBean.getPayChannel());
            water.setTerminalId(scanRefundBean.getTerminalNo());
            tv_money.setText(FormatUtils.formatMount(String.valueOf(scanRefundBean.getAmount())));
            String year = scanRefundBean.getTranDtTm().substring(0, 4);
            String moon = scanRefundBean.getTranDtTm().substring(4, 6);
            String day = scanRefundBean.getTranDtTm().substring(6, 8);
            String h = scanRefundBean.getTranDtTm().substring(8, 10);
            String m = scanRefundBean.getTranDtTm().substring(10, 12);
            String s = scanRefundBean.getTranDtTm().substring(12, 14);
            water.setPayChannel(scanRefundBean.getPayChannel());
            water.setScanDate((moon +"/"+ day));
            water.setScanTime((h +":"+ m +":"+ s));
            tv_data.setText(year + "/" + moon + "/" + day + " " + h + ":" + m + ":" + s);
            String payChannel = "";
            if ("WXPAY".equals(scanRefundBean.getPayChannel())) {
                payChannel = "微信";
                water.setTransType(TransType.ScanTransType.TRANS_SCAN_WEIXIN);
            } else if ("ALIPAY".equals(scanRefundBean.getPayChannel())) {
                payChannel = "支付宝";
                water.setTransType(TransType.ScanTransType.TRANS_SCAN_ALIPAY);
            }
            tv_type.setText(payChannel);
            String status;
            char txtSts = scanRefundBean.getTxnSts().charAt(0);
            switch (txtSts) {
                case 'S':
                    tv_reprint.setVisibility(View.VISIBLE);
                    if (TextUtils.equals(String.valueOf(scanRefundBean.getMAX_REF_AMT()), "0") ||
                            !StringUtil.isEmpty(scanRefundBean.getTXN_CD()) && scanRefundBean.getTXN_CD().equals("4433005")) {
                        status = "已退货";
                       water.setTransType(TransType.ScanTransType.TRANS_SCAN_REFUND);
                    } else {
                        status = "支付成功";
                    }
                    break;
                case 'F':
                    status = "失败";
                    break;
                case 'A':
                    status = "待授权";
                    break;
                case 'C':
                    status = "被冲正";
                    break;
                case 'U':
                    status = "初始状态";
                    break;
                case 'T':
                    status = "超时";
                    break;
                default:
                    status = "未知";
                    break;
            }
            tv_state.setText(status);
        }
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
        switch (view.getId()){
            case R.id.tv_finish:
                finish();
                break;
            case R.id.tv_reprint: //重打印
                Intent printerIntent = new Intent(ScanQueryResultActivity.this, PrintResultActivity.class);
                printerIntent.putExtra("water", water);
                printerIntent.putExtra("transType", String.valueOf(water.getTransType()));
                startActivity(printerIntent);
                finish();
                break;
            default:
                break;
        }
    }
}
