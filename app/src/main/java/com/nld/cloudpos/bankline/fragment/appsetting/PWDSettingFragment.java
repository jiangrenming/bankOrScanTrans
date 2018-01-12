package com.nld.cloudpos.bankline.fragment.appsetting;

import android.os.Bundle;
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
 * Created by jidongdong on 2017/2/7.
 */

public class PWDSettingFragment extends BaseFragment implements View.OnClickListener {
    private LinearLayout layout_items;
    private Map<String, Integer> setting_collection = new LinkedHashMap<String, Integer>() {{
        put("主管密码修改", 1);
        put("操作员密码重置", 2);
    }};

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_layou_app_set_pwd;
    }

    @Override
    public void doInitSubViews(View view) {
        initView();
    }

    void initView() {
        layout_items = queryViewById(R.id.layout_items);
        Iterator<Map.Entry<String, Integer>> iterator = setting_collection.entrySet().iterator();
        int index_tag = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            LinearLayout layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.layout_app_setting_item, null);
            layout.setTag(++index_tag);
            layout.setBackgroundResource(R.drawable.menu_feature_item_bg_style);
            ImageView icon = (ImageView) layout.findViewById(R.id.setting_item_icon);
            icon.setVisibility(View.GONE);
            TextView textView = (TextView) layout.findViewById(R.id.setting_item_text);
            textView.setText(entry.getKey());
            layout.setOnClickListener(this);
            layout_items.addView(layout);
        }
    }


    @Override
    public void onClick(View view) {
        int id = Integer.parseInt(String.valueOf(view.getTag()));
        Bundle bundle = new Bundle();
        switch (id) {
            case 1:
                bundle.putInt("roleType", id);
                startFragment(getActivity(), PWDModifyFragment.class.getName(), getString(R.string.page_title_modify_password_string), bundle);
                break;
            case 2:
                bundle.putInt("roleType", id);
                startFragment(getActivity(), PWDReSetFragment.class.getName(), getString(R.string.page_title_string_password_reset), bundle);
                break;
            default:
                break;
        }
    }
}
