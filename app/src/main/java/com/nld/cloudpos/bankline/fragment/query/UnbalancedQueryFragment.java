package com.nld.cloudpos.bankline.fragment.query;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.bankline.fragment.query.adapter.TransQueryAdapter;
import com.nld.cloudpos.payment.view.MEditText;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;

import java.util.ArrayList;
import java.util.List;

import static com.nld.cloudpos.bankline.R.id.search;

/**
 * Created by Terrence on 2017/2/18.
 */

public class UnbalancedQueryFragment extends BaseFragment {

    private ListView mTransQueryLv ;
    private TransQueryAdapter mAdaper;
    private TransRecordDao mTransRecordDao;
    private MEditText etTransCode;
    private ImageView ivSercher;
    private String batchNo;
    private ParamConfigDao paramConfigDao;
    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_bank_card_query;
    }

    @Override
    public void doInitSubViews(View view) {
        mTransQueryLv = queryViewById(R.id.trans_query_listview);
        etTransCode = queryViewById(R.id.trans_query_search_et);
        ivSercher = queryViewById(R.id.search);
        mAdaper = new TransQueryAdapter(mActivity);
        mTransQueryLv.setAdapter(mAdaper);
        mTransRecordDao = new TransRecordDaoImpl();
        paramConfigDao = new ParamConfigDaoImpl();
        batchNo = paramConfigDao.get("batchno");

        ivSercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTransByCode();
            }
        });
        mTransQueryLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                TransRecord record = mAdaper.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("orderDetail",record);
                startFragment(getActivity(),OrderDetailsFragment.class.getName(),getString(R.string.order_details),bundle);
            }
        });
    }

    private void searchTransByCode() {
        String billNo = etTransCode.getText().toString();
        if (TextUtils.isEmpty(billNo)){
            toastMsg("请输入凭证号！");
            return;
        }
        if (TextUtils.isEmpty(batchNo)){
            batchNo = paramConfigDao.get("batchno");
        }
        TransRecord transRecord = mTransRecordDao.getConsumeByCondition(batchNo, billNo);
        if (transRecord == null){
            toastMsg("未查询到数据");
        }else {
            ArrayList<TransRecord> transRecords = new ArrayList<>();
            transRecords.add(transRecord);
            mAdaper.setData(transRecords);
        }
    }

    @Override
    public void doInitData() {
        List<TransRecord> entities = mTransRecordDao.getEntities();
        for (TransRecord trasn:
             entities ) {
            LogUtils.i("数据库数据="+trasn.getTransType());
        }
        mAdaper.setData(entities);
    }

    @Override
    public boolean setRightButton(Button view) {
        view.setVisibility(View.VISIBLE);
        view.setText("分类汇总");
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFragment(getActivity(), UnbalancedAggregateFragment.class.getName(), getString(R.string
                        .aggregate));
            }
        });
        return true;
    }

    private void toastMsg(String message){
        if (TextUtils.isEmpty(message)){
            return;
        }
        Toast.makeText(mActivity,message, Toast.LENGTH_SHORT).show();
    }

}
