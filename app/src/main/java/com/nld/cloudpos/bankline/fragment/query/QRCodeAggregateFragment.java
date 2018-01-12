package com.nld.cloudpos.bankline.fragment.query;

import android.text.TextUtils;

import com.nld.cloudpos.bankline.fragment.query.bean.AggregateBean;
import com.nld.cloudpos.payment.controller.FormatUtils;
import com.nld.cloudpos.payment.controller.ScanNetSummary;
import com.nld.starpos.wxtrade.bean.scan_query.ScanQueryDataBean;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.TransType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terrence on 2017/2/13.
 * <p>
 * 分类汇总 - 二维码收款交易
 */

public class QRCodeAggregateFragment extends BaseAggregateFragment {

    int[] types = {TransType.ScanTransType.TRANS_SCAN_ALIPAY, TransType.ScanTransType.TRANS_SCAN_WEIXIN, TransType.ScanTransType.TRANS_SCAN_REFUND};

    @Override
    List<AggregateBean> getAggregateList() {
       final List<AggregateBean> aggregateList = new ArrayList<>();
        new ScanNetSummary(getActivity(), new ScanNetSummary.OnNetDataListener() {
            @Override
            public void onNetDataResult(List<ScanQueryDataBean> scanQueryDataBeen, String errorMessage) {
                aggregateList.clear();
                if (!TextUtils.isEmpty(errorMessage)){
                    ToastUtils.showToast(errorMessage);
                }
                if (scanQueryDataBeen != null && !scanQueryDataBeen.isEmpty()){
                    for (int type : types) {
                        AggregateBean bean = new AggregateBean();
                        bean.type = type;
                        for (ScanQueryDataBean transNetIntro : scanQueryDataBeen) {
                            if (TextUtils.equals(transNetIntro.getTxn_sts(), "S")){
                                switch (type){
                                    case TransType.ScanTransType.TRANS_SCAN_ALIPAY:
                                        if (!TextUtils.equals(transNetIntro.getTxn_cd(), TransType.QueryNet.SCANREFUND) &&
                                                TextUtils.equals(transNetIntro.getPaychannel(), TransType.QueryNet.ALIPAY_NUMBER)) {
                                            bean.total += 1;
                                            bean.amount += FormatUtils.parseLong(transNetIntro.getTxn_amt());
                                        }
                                        break;
                                    case TransType.ScanTransType.TRANS_SCAN_WEIXIN:
                                        if (!TextUtils.equals(transNetIntro.getTxn_cd(), TransType.QueryNet.SCANREFUND) &&
                                                TextUtils.equals(transNetIntro.getPaychannel(), TransType.QueryNet.WEICHAT_NUMBER)) {
                                            bean.total += 1;
                                            bean.amount += FormatUtils.parseLong(transNetIntro.getTxn_amt());
                                        }
                                        break;
                                    case TransType.ScanTransType.TRANS_SCAN_REFUND:
                                        if (TextUtils.equals(transNetIntro.getTxn_cd(), TransType.QueryNet.SCANREFUND)) {
                                            bean.total += 1;
                                            bean.amount += FormatUtils.parseLong(transNetIntro.getTxn_amt());
                                        }
                                        break;
                                    default:
                                        break;

                                }
                            }
                        }
                        aggregateList.add(bean);
                    }
                    mAdapter.setItems(aggregateList);
                }
            }
        }).execute();
        return aggregateList;
    }

}
