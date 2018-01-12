/**
 *
 */
package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.controller.ScanErrorActivity;
import com.nld.cloudpos.util.CommonContants;
import com.nld.cloudpos.util.DialogFactory;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.activity.StartScanActivity;
import com.nld.starpos.wxtrade.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.bean.scan_settle.ScanSettleRes;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.ScanTransFlagUtil;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.utils.params.TransType;

import common.DateTimeUtil;
import common.StringUtil;

/**
 * 结算
 *
 * @author lin 2015年10月20日
 */
public class SettleMainActivity extends BaseActivity implements OnClickListener {
    private RelativeLayout js;
    private RelativeLayout wxjs;
    private String signTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settle_main);
        signTag = ParamsUtil.getInstance().getParam(ParamsConts.SIGN_SYMBOL);
        initView();
    }

    private void initView() {
        setTopDefaultReturn();
        js = (RelativeLayout) findViewById(R.id.ll_settle_js);
        wxjs = (RelativeLayout) findViewById(R.id.ll_settle_wxjs);
        js.setOnClickListener(this);
        wxjs.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_settle_js:
                if (!signTag.equals("1")) {
                    showTips("请先签到再结算");
                    return;
                }
                DialogFactory.showMessage(mActivity, "提示", "确定进行结算", "确定", new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFactory.dismissAlert(mActivity);
                        Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_SIGN_JS);
                        Intent intent = new Intent(SettleMainActivity.this, StartTransActivity.class);
                        startActivityForResult(intent, Constant.BANK_SETTLE);
                     //   intent = new Intent(SettleMainActivity.this, SettleNetwork.class);
                     //   intent.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, SignSuccessActivity.class.getName());
                      //  startActivity(intent);
                    //    DialogFactory.dismissAlert(mActivity);
                    }
                }, "取消", null);
                break;
            case R.id.ll_settle_wxjs: //扫码批结
                ScanCache.getInstance().setTransCode(ScanTransFlagUtil.TRANS_CODE_WX_SETTLE);
                ScanPayBean scan = getScan();
                Intent intent = new Intent(SettleMainActivity.this,StartScanActivity.class);
                intent.putExtra("scan",scan);
                startActivityForResult(intent, TransType.SCAN_SETTLE_CODE);
                break;
            default:
                break;
        }
    }

    private ScanPayBean getScan(){
        ScanPayBean scanPayBean = new ScanPayBean();
        scanPayBean.setType(TransType.ScanTransType.TRANS_QUERY_LIST);  //交易类型
        scanPayBean.setTransType(TransParamsValue.AntCompanyInterfaceType.TRANS_QUER); //交易接口
        scanPayBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID)); //终端号
        scanPayBean.setDateTime(DateTimeUtil.getCurrentDate("yyyyMMdd"));//时间
        scanPayBean.setBatchNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO)); //批次号
        scanPayBean.setPageNumber(40); //条数
        scanPayBean.setPageNo(1);  //页数
        scanPayBean.setRequestUrl(CommonContants.url); //请求路径
        scanPayBean.setProjectType(EncodingEmun.antCompany.getType()); //项目类型
        return  scanPayBean;
    }

    @Override
    public void goback(View v) {
        NldPaymentActivityManager.getActivityManager()
                .removeActivity(SettleMainActivity.this);
    }

    @Override
    public void setTopTitle() {
        TextView topTitle = (TextView) super.findViewById(R.id.top_title);

        topTitle.setText("结算");
    }

    @Override
    public void onDeviceConnected(AidlDeviceService deviceManager) {}

    @Override
    public void onDeviceConnectFaild() {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode ==RESULT_OK){
            if (data != null && requestCode == TransType.SCAN_SETTLE_CODE){  //扫码批结
                ScanSettleRes scanSettleRes = (ScanSettleRes) data.getSerializableExtra("water");
                if (scanSettleRes != null){
                    Intent intent = new Intent(SettleMainActivity.this, PrintResultActivity.class);
                    intent.putExtra("water",scanSettleRes);
                    intent.putExtra("transType",scanSettleRes.getTransType());
                    startActivity(intent);
                }
            }else if (requestCode == Constant.BANK_SETTLE){  //银行卡批结对账平
                startActivity(new Intent(SettleMainActivity.this, SettleResult.class));
            }
        }else if (resultCode == RESULT_FIRST_USER){
            if ( requestCode == TransType.SCAN_SETTLE_CODE){
                startActivity(new Intent(SettleMainActivity.this, ScanErrorActivity.class));
            }else if (requestCode == Constant.BANK_SETTLE){
                startActivity(new Intent(SettleMainActivity.this, ErrorResult.class));
            }
        }else if (resultCode == RESULT_CANCELED){
            if ( requestCode == TransType.SCAN_SETTLE_CODE){
                String error = data.getStringExtra("error");
                if (!StringUtil.isEmpty(error)){
                    ToastUtils.showToast(error);
                }
            }
        }
    }
}
