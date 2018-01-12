package com.nld.cloudpos.bankline.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;

/**
 * Created by L on 2017/2/8.
 *
 * @描述 选择付款方式的条目View。
 */

public class TypeItemView extends FrameLayout {

    private String mContent;
    private boolean mShowArrow;
    private Drawable mLeftDrawable;

    public TypeItemView(Context context) {
        this(context, null);
    }

    public TypeItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TypeItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.view_type_item, this, true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TypeItemView);
        mContent = a.getString(R.styleable.TypeItemView_tiv_content);
        mShowArrow = a.getBoolean(R.styleable.TypeItemView_tiv_showArrow, true);
        mLeftDrawable = a.getDrawable(R.styleable.TypeItemView_tiv_leftDrawable);
        initView();
        a.recycle();
    }

    private void initView() {
        ImageView ivLog = (ImageView) findViewById(R.id.iv_icon);
        ivLog.setImageDrawable(mLeftDrawable);
        TextView tvSelect = (TextView) findViewById(R.id.tv_type);
        tvSelect.setText(mContent);
        ImageView ivOther = (ImageView) findViewById(R.id.iv_arrow);
        if (!mShowArrow) {
            ivOther.setVisibility(GONE);
        }
    }

}
