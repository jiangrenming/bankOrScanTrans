package com.nld.cloudpos.bankline.fragment.query;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Terrence on 2017/2/13.
 */

public class TransQueryFragment extends BaseFragment implements View.OnClickListener {

    private LinearLayout layout_items;
    private Map<String, Integer> setting_collection = new LinkedHashMap<String, Integer>() {
        {
            put("银行卡收款交易", R.drawable.icon_bankcard);
            put("二维码收款交易", R.drawable.icon_scan);
        }
    };

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_layou_app_set_trade;
    }

    @Override
    public void doInitSubViews(View view) {
        initView();
    }

    private void initView() {
        layout_items = queryViewById(R.id.layout_items);
        Iterator<Map.Entry<String, Integer>> iterator = setting_collection.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            LinearLayout layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout
                    .layout_app_setting_item, null);
            layout.setTag(entry.getValue());
            layout.setBackgroundResource(R.drawable.menu_feature_item_bg_style);
            ImageView icon = (ImageView) layout.findViewById(R.id.setting_item_icon);
            icon.setBackgroundResource(entry.getValue());
            TextView textView = (TextView) layout.findViewById(R.id.setting_item_text);
            textView.setText(entry.getKey());
            layout.setOnClickListener(this);
            layout_items.addView(layout);

        }
    }

    @Override
    public void onClick(View view) {
        switch (Integer.parseInt(view.getTag().toString())) {
            case R.drawable.icon_bankcard:
                startFragment(getActivity(), UnbalancedQueryFragment.class.getName(), getString(R.string
                        .bank_card_payment_transactions));
                break;

            case R.drawable.icon_scan:
                startFragment(getActivity(), QRCodeQueryFragment.class.getName(), getString(R.string
                        .qr_code_payment_transactions));
                break;

            default:
                break;
        }
    }

}