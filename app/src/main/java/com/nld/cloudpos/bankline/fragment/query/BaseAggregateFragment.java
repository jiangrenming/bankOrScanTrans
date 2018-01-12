package com.nld.cloudpos.bankline.fragment.query;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.adapter.FinalAdapter;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.bankline.fragment.query.bean.AggregateBean;
import com.nld.cloudpos.payment.controller.FormatUtils;
import com.nld.cloudpos.payment.controller.TransUtils;

import java.util.ArrayList;
import java.util.List;

import common.Utility;

/**
 * Created by Terrence on 2017/2/13.
 * <p>
 * 分类汇总Base类
 */

public abstract class BaseAggregateFragment extends BaseFragment implements FinalAdapter
        .FinalAdapterListener {

    private ListView mTransLV;
    public FinalAdapter mAdapter;
    private List<AggregateBean> mAggregateBeanList = new ArrayList<>();

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_aggregate;
    }

    @Override
    public void doInitSubViews(View view) {
        initView();
    }

    private void initView() {
        mTransLV = queryViewById(R.id.trans_query_listview);
        mAdapter = new FinalAdapter(R.layout.item_aggregate_query, this);
        mTransLV.setAdapter(mAdapter);
    }

    @Override
    public void doInitData() {
        mAggregateBeanList = getAggregateList();
        mAdapter.setItems(mAggregateBeanList);
    }

    @Override
    public void bindView(int position, FinalAdapter.ViewHolder viewHolder) {
        TextView v1 = viewHolder.getView(R.id.item_trans_value_1, TextView.class);
        TextView v2 = viewHolder.getView(R.id.item_trans_value_2, TextView.class);
        TextView v3 = viewHolder.getView(R.id.item_trans_value_3, TextView.class);

        AggregateBean bean = mAggregateBeanList.get(position);
        if (bean.type != 0){
            v1.setText(TransUtils.getTransType(bean.type)[0]);
            v2.setText(String.valueOf(bean.total));
            v3.setText("￥"+ String.valueOf(FormatUtils.formatMount(String.valueOf(bean.amount))));
        }else {
            v1.setText(bean.getTransType());
            v2.setText(bean.getTotalTrans());
            String totalAmouont = "￥" + Utility.unformatMount(bean.getTotalAmount());
            v3.setText(totalAmouont);
        }

    }

    abstract List<AggregateBean> getAggregateList() ;
}
