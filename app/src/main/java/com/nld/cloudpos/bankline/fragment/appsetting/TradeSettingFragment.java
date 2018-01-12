package com.nld.cloudpos.bankline.fragment.appsetting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.starpos.banktrade.utils.TransParams;
import com.nld.starpos.wxtrade.utils.params.TransType;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jidongdong on 2017/2/7.
 * <p>
 * 交易设置Fragmnent
 */

public class TradeSettingFragment extends BaseFragment implements View.OnClickListener {
    private LinearLayout layout_items;
    private Map<String, Integer> setting_collection = new LinkedHashMap<String, Integer>() {{
        put("银行卡收款", R.drawable.icon_bankcard);
        put("二维码收款", R.drawable.icon_scan);
    }};

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_layou_app_set_trade;
    }

    @Override
    public void doInitSubViews(View view) {
        initView();
    }

    void initView() {
        layout_items = queryViewById(R.id.layout_items);
        Iterator<Map.Entry<String, Integer>> iterator = setting_collection.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            LinearLayout layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.layout_app_setting_item, null);
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
        Bundle bundle = new Bundle();
        switch (Integer.parseInt(view.getTag().toString())) {
            case R.drawable.icon_bankcard:
                bundle.putInt(TransType.SettingTransType.YL_QC_CARD_TYPE, TransParams.SettingTransValue.LY_CARD_TYPE);
                startFragment(getActivity(), TransBankSettingFragment.class.getName(), "银行卡收款", bundle);
                break;
            case R.drawable.icon_scan:
                bundle.putInt(TransType.SettingTransType.YL_QC_CARD_TYPE,  TransParams.SettingTransValue.QC_CARD_TYPE);
                startFragment(getActivity(), TradeSetDetailFragment.class.getName(), "二维码收款", bundle);
                break;
            default:
                break;
        }
    }
}
