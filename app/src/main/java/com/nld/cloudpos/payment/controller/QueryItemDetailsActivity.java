package com.nld.cloudpos.payment.controller;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.activity.PrintResultActivity;
import com.nld.logger.LogUtils;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.TransType;

import common.DateTimeUtil;
import common.StringUtil;

/**
 * Created by jiangrenming on 2017/10/19.
 * 交易明细表单
 */

public class QueryItemDetailsActivity extends AbstractActivity implements View.OnClickListener{


    @ViewInject(R.id.transChannel)
    TextView transChannel;
    @ViewInject(R.id.transState)
    TextView transState;
    @ViewInject(R.id.trans_money)
    TextView trans_money;
    @ViewInject(R.id.trans_time)
    TextView trans_time;
    @ViewInject(R.id.trans_order)
    TextView trans_order;
    @ViewInject(R.id.trans_user)
    TextView trans_user;
    @ViewInject(R.id.trans_printer)
    TextView trans_printer;
    @ViewInject(R.id.trans_finish)
    TextView trans_finish;

    private ScanTransRecord scanRecords;

    @Override
    public int contentViewSourceID() {
        return R.layout.query_itme_details;
    }

    @Override
    public void initView() {
        ViewUtils.inject(this);
        setTopDefaultReturn();
        trans_finish.setOnClickListener(this);
        trans_printer.setOnClickListener(this);
    }

    @Override
    public void initData() {
        setTopTitle("订单明细");
        ScanPayBean scan_query = (ScanPayBean) getIntent().getSerializableExtra("scan_query");
        if (checkRecvFormData(scan_query)){
            scanRecords = new ScanTransRecord();
            int transType = exChangeTransType(scan_query);
            LogUtils.i("交易类型=>"+transType);
            transChannel.setText(TransUtils.getTransType(transType)[0]);
            int state = exChangeTransState(scan_query);
            boolean isSuccess = false;
            switch (state) {
                case TransType.TransStatueType.NORMAL:
                    transState.setText("交易成功");
                    isSuccess = true;
                    break;
                case TransType.TransStatueType.REV:
                    transState.setText("已撤销");
                    isSuccess = true;
                    break;
                case TransType.TransStatueType.ADJUST:
                    transState.setText("已调整");
                    isSuccess = false;
                    break;
                case TransType.TransStatueType.RETURN:
                    transState.setText("已退货");
                    isSuccess = false;
                    break;
                case 1000:
                    transState.setText("交易失败");
                    isSuccess = false;
                    break;
                case 1001:
                    transState.setText("等待授权");
                    isSuccess = false;
                    break;
                case 1002:
                    transState.setText("已冲正");
                    isSuccess = false;
                    break;
                case 1003:
                    transState.setText("初始状态");
                    isSuccess = false;
                    break;
                case 1004:
                    transState.setText("交易超时");
                    isSuccess = false;
                    break;
                default:
                    transState.setText("未知状态");
                    isSuccess = false;
                    break;
            }
            trans_printer.setVisibility(isSuccess ? View.VISIBLE : View.GONE);
            if (scan_query.getAmount() != 0){
                trans_money.setText("￥" + FormatUtils.formatMount(String.valueOf(scan_query.getAmount())));
            }else {
                trans_money.setVisibility(View.GONE);
            }
            trans_order.setText(scan_query.getChannelid());
            String tranDtTm = scan_query.getTranDtTm();
            if (!StringUtil.isEmpty(tranDtTm)){
                String date = tranDtTm.substring(4, 8);
                String time = tranDtTm.substring(8, 14);
                trans_time.setText(DateTimeUtil.timeFormat(date,time));
                scanRecords.setScanDate((date.substring(0,2)+"/"+date.substring(2,4)));
                scanRecords.setScanTime((time.substring(0,2)+":"+time.substring(2,4)+":"+time.substring(4,6)));
            }
            trans_user.setText(scan_query.getOprId());

            scanRecords.setStatuscode(String.valueOf(state));
            scanRecords.setTransType(transType);
            scanRecords.setTransamount(String.valueOf(scan_query.getAmount()));
            scanRecords.setOrderNo(scan_query.getChannelid());
            scanRecords.setOper(scan_query.getOprId());
            scanRecords.setTerminalId(scan_query.getTerminalNo());
            scanRecords.setMemberName(scan_query.getMercNm());
            scanRecords.setSystraceno(scan_query.getOrderId());
            scanRecords.setBatchbillno(scan_query.getBatchNo());
            scanRecords.setAdddataword("重打印凭证");
        }else {
            ToastUtils.showToast("返回数据有误");
        }
    }

    /**
     * 转换交易状态
     * @param scan_query
     * @return
     */
    private int exChangeTransType( ScanPayBean scan_query){
        String payChannel = scan_query.getPayChannel();
        int transType = -1;
        if (!StringUtil.isEmpty(payChannel)){
            scanRecords.setPayChannel(payChannel);
            switch (payChannel){
                case TransType.QueryNet.ALIPAY_STRING:
                    transType = TransType.ScanTransType.TRANS_SCAN_ALIPAY;
                    break;
                case TransType.QueryNet.WEICHAT_STRING:
                    transType = TransType.ScanTransType.TRANS_SCAN_WEIXIN;
                    break;
                default:
                    break;
            }
        }
        if (TextUtils.equals(scan_query.getTXN_CD(), TransType.QueryNet.SCANREFUND)){
            transType = TransType.ScanTransType.TRANS_SCAN_REFUND;
        }
        return transType;
    }

    /**
     * 转换交易状态
     * @param scan_query
     * @return
     */
    private int exChangeTransState( ScanPayBean scan_query){
        int transState = -1;
        String txnSts = scan_query.getTxnSts();
        if (!StringUtil.isEmpty(txnSts)){
            switch (txnSts){
                case "S":
                    transState = TransType.TransStatueType.NORMAL;
                    if (TextUtils.equals(String.valueOf(scan_query.getMAX_REF_AMT()), "0")) {
                        transState = TransType.TransStatueType.REV;
                    }
                    break;
                case "F":
                    transState = 1000;
                    break;
                case "A":
                    transState = 1001;
                    break;
                case "C":
                    transState = 1002;
                    break;
                case "U":
                    transState = 1003;
                    break;
                case "T":
                    transState = 1004;
                    break;
                default:
                    break;
            }
        }
        return transState;
    }


    /**
     * 检验关键字是否返回
     * @param scan_query
     * @return
     */
    private boolean checkRecvFormData( ScanPayBean scan_query) {
        return scan_query != null &&
                !TextUtils.isEmpty(String.valueOf(scan_query.getAmount())) &&
                !TextUtils.isEmpty(scan_query.getPayChannel()) &&
                !TextUtils.isEmpty(scan_query.getTxnSts());
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
            case R.id.trans_finish:
                NldPaymentActivityManager.getActivityManager().removeActivity(this);
                break;
            case R.id.trans_printer:
                Intent printerIntent = new Intent(QueryItemDetailsActivity.this, PrintResultActivity.class);
                printerIntent.putExtra("water", scanRecords);
                LogUtils.i("交易类型="+scanRecords.getTransType());
                printerIntent.putExtra("transType", String.valueOf(scanRecords.getTransType()));
                startActivity(printerIntent);
                break;
            default:
                break;
        }
    }
}
