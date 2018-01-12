package com.nld.cloudpos.payment.activity.reprint;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

/**
 * Created by syyyz on 2017/2/9.
 */

public class TranMenuActivity extends BaseAbstractActivity implements View.OnClickListener {

    private FrameLayout mFlContainer;

    @Override
    public int contentViewSourceID() {
        return R.layout.activity_menu_tran_query;
    }

    @Override
    public void initView() {
        setTopTitle("交易查询");
        setTopDefaultReturn();
        mFlContainer = (FrameLayout) findViewById(R.id.fl_container);

        View childView = getContentViewById(R.layout.activity_tran_menu);
        mFlContainer.addView(childView);
        onViewCreated(childView);

    }

    private View getContentViewById(int contentViewId) {
        return LayoutInflater.from(this).inflate(contentViewId, mFlContainer, false);
    }

    protected void onViewCreated(View view) {
        view.findViewById(R.id.tiv_local).setOnClickListener(this);
        view.findViewById(R.id.tiv_network).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        Intent intent = null;

        switch (view.getId()) {
            case R.id.tiv_local: // 本地数据

                break;

            case R.id.tiv_network: // 联网查询
           //     intent = new Intent(mContext, TransQueryActivity.class);
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_WX_QUERY);
                startActivity(intent);

                break;

        }
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {

    }

    @Override
    public void onServiceBindFaild() {

    }

    @Override
    public boolean saveValue() {
        return false;
    }

}
