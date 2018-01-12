package com.nld.cloudpos.payment.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;

import common.StringUtil;

/**
 * Created by jidongdong on 2017/2/9.
 */

public class RadioGroupItemView extends LinearLayout {
    private RadioGroup radioGroup;

    public RadioGroupItemView(Context context) {
        super(context);
    }

    public RadioGroupItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
    }

    public RadioGroupItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initParams(context, attrs);
    }

    private void initParams(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.layout_radio_group_item_view, this);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadioGroupItemView_Style);
        String desc = a.getString(R.styleable.RadioGroupItemView_Style_desc);
        String selects = a.getString(R.styleable.RadioGroupItemView_Style_selects);
        String select_values = a.getString(R.styleable.RadioGroupItemView_Style_select_values);
        String default_sel_val = a.getString(R.styleable.RadioGroupItemView_Style_default_sel_val);
        int orientation = a.getInt(R.styleable.RadioGroupItemView_Style_orientation, 0);
        initView(context, orientation, desc, selects, select_values, default_sel_val);
        a.recycle();
    }

    public void initView(Context context, int orientation, String desc, String selects, String select_values, String default_sel_val) {
        TextView text_desc = (TextView) findViewById(R.id.tv_title);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        if (orientation == 1) {
            radioGroup.setOrientation(VERTICAL);
        }
        if (StringUtil.isEmpty(desc)) {
            text_desc.setVisibility(GONE);
        } else {
            text_desc.setText(desc);
        }
        if (!TextUtils.isEmpty(selects) && !TextUtils.isEmpty(select_values)) {
            String[] items = selects.split("/");
            String[] tags = select_values.split("/");
            for (int i = 0; i < items.length; i++) {
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(" " + Html.fromHtml(items[i]));
                radioButton.setTag(tags[i]);
                radioButton.setButtonDrawable(R.drawable.selector_radio_button_bg);
//                radioButton.setBackgroundResource(R.drawable.selector_radio_button_bg);
                RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                layoutParams.weight = 1;
                layoutParams.rightMargin = 30;
                if (orientation == 1) {
                    layoutParams.topMargin = 20;
                }
                radioButton.setLayoutParams(layoutParams);
                radioGroup.addView(radioButton);
            }
            setSelectedRadio(default_sel_val);
        }
    }

    /**
     * 获取选中的值的Tag
     *
     * @return
     */
    public String getSelectedRadioValue() {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            RadioButton rb = (RadioButton) radioGroup.getChildAt(i);
            if (rb.isChecked()) {
                return rb.getTag().toString();
            }
        }
        return "";
    }

    /**
     * 设置选中的项
     *
     * @param tag
     */
    public void setSelectedRadio(String tag) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            RadioButton rb = (RadioButton) radioGroup.getChildAt(i);
            if (tag.equals(rb.getTag())) {
                radioGroup.check(rb.getId());
                break;
            }
        }
    }

}
