package com.nld.cloudpos.bankline.fragment.query.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.nld.cloudpos.bankline.R;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.utils.Constant;

import java.util.ArrayList;
import java.util.List;
import common.Utility;

/**
 * Created by cxg on 2017/9/28.
 */

public class TransQueryAdapter extends BaseAdapter {

    private ArrayList<TransRecord> mTransQueryList;
    private Context context;

    public TransQueryAdapter(Context context) {
        this.context = context;
        this.mTransQueryList = new ArrayList<>();
    }
    @Override
    public int getCount() {
        return mTransQueryList == null ? 0 : mTransQueryList.size();
    }

    @Override
    public TransRecord getItem(int position) {
        return mTransQueryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.layout_local_bank_card_query_header, null);
            vh = new ViewHolder();
            vh.tvTransData = (TextView) convertView.findViewById(R.id.trans_value_1);
            vh.tvTransCode = (TextView) convertView.findViewById(R.id.trans_value_2);
            vh.tvTransType = (TextView) convertView.findViewById(R.id.trans_value_3);
            vh.tvAmount = (TextView) convertView.findViewById(R.id.trans_value_4);
            vh.tvTransState = (TextView) convertView.findViewById(R.id.trans_value_5);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        TransRecord transRecord = mTransQueryList.get(position);
        vh.tvTransData.setText(transRecord.getTranslocaldate());
        vh.tvTransCode.setText(transRecord.getSystraceno());
        vh.tvTransType.setText(Constant.transType2Value(transRecord.getTransType()));
        vh.tvAmount.setText(Utility.unformatMount(transRecord.getTransamount()));

        String state = "";
        switch (transRecord.getTransState()) {
            case "1":
                state = "交易成功";
                break;
            case "2":
                state = "已撤销";
                break;
            default:
                break;
        }
        vh.tvTransState.setText(state);

        return convertView;
    }

    public void setData(List<TransRecord> list) {
        mTransQueryList.clear();
        mTransQueryList.addAll(list);
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        public TextView tvTransData;
        public TextView tvTransCode;
        public TextView tvTransType;
        public TextView tvAmount;
        public TextView tvTransState;
    }
}
