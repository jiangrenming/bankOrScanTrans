package com.nld.cloudpos.bankline.fragment.appsetting;

import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.nld.cloudpos.aidl.emv.AidlPboc;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.payment.activity.ErrorResult;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.utils.ToastUtils;

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;
import static com.nld.cloudpos.BankApplication.mDeviceService;

/**
 * Created by cxg on 2017/10/16.
 */

public class ParamsDownloadFragment extends BaseFragment implements View.OnClickListener {

    private TextView trans_start_coutdowntime_tv;
    private TextView trans_start_tip_tv;

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_param_download;
    }

    @Override
    public void doInitSubViews(View view) {
        queryViewById(R.id.tvIcCard).setOnClickListener(this);
        queryViewById(R.id.tvDelIcCard).setOnClickListener(this);
        trans_start_tip_tv = queryViewById(R.id.trans_start_tip_tv);
        trans_start_coutdowntime_tv = queryViewById(R.id.trans_start_coutdowntime_tv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvIcCard:
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_IC_PARAM_QUERY);
                Intent intent = new Intent(getActivity(), StartTransActivity.class);
                intent.putExtra("ic_card",true);
                startActivityForResult(intent, Constant.PARAMS_DOWNLOAD_DATA);
                break;
            case R.id.tvDelIcCard:
                try {
                    AidlPboc aidlEmv = AidlPboc.Stub.asInterface(mDeviceService.getEMVL2());
                    boolean flag = aidlEmv.updateAID(0x03, null);
                    flag = flag & aidlEmv.updateCAPK(0x03, null);
                    Log.e("TAG--", "IC 解绑" + flag);
                    int result = aidlEmv.isExistAidPublicKey();
                    LogUtils.d("AID公钥及参数： " + result);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.e("TAG--", "IC 解绑 失败");
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            ToastUtils.showToast("IC卡相关参数下载成功");
            finish();
        }else if (resultCode == RESULT_FIRST_USER){
            startActivity(new Intent(getActivity(), ErrorResult.class));
        }
    }
}
