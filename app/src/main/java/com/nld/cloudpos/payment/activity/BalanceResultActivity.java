package com.nld.cloudpos.payment.activity;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.starpos.banktrade.utils.Cache;

import java.util.Map;

import common.Utility;

public class BalanceResultActivity extends BaseAbstractActivity {
    private String paymoney = null;

    @Override
    public int contentViewSourceID() {
        return R.layout.balance_query_result;
    }

    @Override
    public void initView() {
        setTopReturnListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                NldPaymentActivityManager.getActivityManager()
                        .removeAllActivityExceptOne(LauncherActivity.class);
            }
        });
        TextView yuejieguo = (TextView) this.findViewById(R.id.yuejieguo);

        Map<String, String> map = Cache.getInstance()
                .getResultMap();
        String balanceStr = map.get("balanceamount");
        logger.debug("查询到的余额是:" + balanceStr);
        String mount_symbol = String.valueOf(balanceStr.charAt(7));
        String balanceamount = balanceStr.substring(8);
        String[] balances = null;
        String showDataLineTwo = "RMB 0.00";
        if ("C".equals(mount_symbol) || "000000000000".equals(balanceamount)) {
            showDataLineTwo = "RMB " + Utility.unformatMount(balanceamount);
        } else {
            showDataLineTwo = "RMB -" + Utility.unformatMount(balanceamount);
        }
        yuejieguo.setText(showDataLineTwo);
//       if(!StringUtil.isEmpty(balanceStr) && balanceStr.contains("D")){
//           if(balanceStr.startsWith("D")){
//               balanceStr=balanceStr.substring(1);
//           }
//           balances=balanceStr.split("D");
//       }
//       if(null!=balances && balances.length>0){
//           for(int i=0;i<balances.length;i++){
//               logger.debug("第"+i+"账户余额："+balances[i]);
//               if(balances[i].compareTo("000000000000")>0){
//                   yuejieguo.setText(Utility.unformatMount(balances[i]));
//                   break;
//               }
//           }
//       }else{
//           yuejieguo.setText("0.00");
//       }
        
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

    // 确认按钮点击
    public void confirm(View v) {
        NldPaymentActivityManager.getActivityManager()
                .removeAllActivityExceptOne(LauncherActivity.class);
    }
}
