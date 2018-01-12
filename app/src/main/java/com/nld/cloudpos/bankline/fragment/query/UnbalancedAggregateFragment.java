package com.nld.cloudpos.bankline.fragment.query;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.query.bean.AggregateBean;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terrence on 2017/2/13.
 * <p>
 * 分类汇总 - 银行卡收款交易
 */

public class UnbalancedAggregateFragment extends BaseAggregateFragment {


    @Override
    List<AggregateBean> getAggregateList() {
        TransRecordDao transRecordDao = new TransRecordDaoImpl();
        String[] transTypes = getActivity().getResources().getStringArray(R.array.band_trans_types);
        List<AggregateBean> aggregateList = new ArrayList<>();
        for (String type : transTypes) {
            AggregateBean bean = new AggregateBean();
            bean.setTransType(Constant.transType2Value(type));
            bean.setTotalAmount(transRecordDao.getTransAmountByType(type));
            bean.setTotalTrans(transRecordDao.getTransCountByType(type));
            aggregateList.add(bean);
        }
        return aggregateList;
    }

}
