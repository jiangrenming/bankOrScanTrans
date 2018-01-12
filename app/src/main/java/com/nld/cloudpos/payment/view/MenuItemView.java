package com.nld.cloudpos.payment.view;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.interfaces.IMenuItemClick;

public class MenuItemView extends LinearLayout {

    private Context mContext;
    private LayoutInflater mInflater;
    private View mView;

    private IMenuItemClick mClick;
    private TextView mTitle;
    private ImageView mIcon;

    private String mText;
    private int mIconId;

    public MenuItemView(Context context) {
        super(context);
        initView(context);
    }

    public MenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public MenuItemView(Context context, String title, int icon, IMenuItemClick listener) {
        super(context);
        mClick = listener;
        initView(context);
        setViewData(title, icon);
    }

    public void setViewData(String title, int icon) {
        setTitle(title);
        setIcon(icon);
    }

    public void setTitle(String title) {
        mText = title;
        mTitle.setText(Html.fromHtml(title));
    }

    public void setIcon(int icon) {
        mIconId = icon;
        mIcon.setBackgroundResource(icon);
    }

    public void initView(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setBackgroundResource(R.drawable.menu_feature_item_bg_style);
        mView = mInflater.inflate(R.layout.item_menu, this);
        mTitle = (TextView) findViewById(R.id.menu_item_title);
        mIcon = (ImageView) findViewById(R.id.menu_item_icon);

        setItemClick(mClick);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setItemClick(IMenuItemClick listener) {
        mClick = listener;
        if (null != mClick) {
            setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mClick.onMenuItemClick(v, mIconId);
                }
            });
        }
    }
}