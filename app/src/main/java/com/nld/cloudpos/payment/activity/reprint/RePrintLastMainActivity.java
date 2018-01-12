/**
 *
 */
package com.nld.cloudpos.payment.activity.reprint;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.activity.BaseActivity;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.TransConstans;

/**
 * 结算
 *
 * @author lin 2015年10月20日
 */
public class RePrintLastMainActivity extends BaseActivity implements
        OnClickListener {

    private RelativeLayout yhkjy;
    private RelativeLayout smjy;
    private TransRecordDao transRecordDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reprint_last_main);
        initView();
        setTopDefaultReturn();
    }

    private void initView() {
        transRecordDao = new TransRecordDaoImpl();
        yhkjy = (RelativeLayout) findViewById(R.id.ll_reprint_last_yhkjy);
        smjy = (RelativeLayout) findViewById(R.id.ll_reprint_last_smjy);
        yhkjy.setOnClickListener(this);
        smjy.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!isCanClick(2000)) {
            return;
        }
        switch (v.getId()) {
            case R.id.ll_reprint_last_yhkjy:
                if (transRecordDao.getLastTransRecord() != null) {
                    Intent intent = null;
                    intent = new Intent(RePrintLastMainActivity.this, RePrintLastActivity.class);
                    startActivity(intent);
//                    DialogFactory.dismissAlert(mActivity);
                } else {
                    showTips("暂无交易记录，无法打印！");
                }
                break;

            case R.id.ll_reprint_last_smjy:
                Bundle bd = new Bundle();
                bd.putString("startfromlauncher", "true");
                Intent it = new Intent();
                ComponentName com = new ComponentName(TransConstans.OLD_PAYMENT_PACKAGE_NAME,
                        TransConstans.OLD_PAYMENT_LAUNCHER_PAGE);
                bd.putString("transcode", TransConstans.TRANS_CODE_REPRINT_LAST);
                try {
                    it.setComponent(com);
                    it.putExtras(bd);
                    startActivity(it);
                } catch (Exception e) {
                    e.printStackTrace();
                    showTips("启动重打印上一笔交易失败");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void goback(View v) {
        NldPaymentActivityManager.getActivityManager().removeActivity(
                RePrintLastMainActivity.this);
    }

    @Override
    public void setTopTitle() {
        TextView topTitle = (TextView) super.findViewById(R.id.top_title);

        topTitle.setText("重打印");
    }

    @Override
    public void onDeviceConnected(AidlDeviceService deviceManager) {

    }

    @Override
    public void onDeviceConnectFaild() {
        // TODO Auto-generated method stub

    }
}
