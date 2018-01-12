package com.nld.cloudpos.payment.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.nld.cloudpos.payment.adapter.MenuPagerGridAdapter;

import java.util.List;

public class MenuPageView extends LinearLayout {
    
    private Context mContext;
    private List<MenuItemView> mDataset;
    
    private GridView mGridView;
    private MenuPagerGridAdapter mAdapter;
    private int column=3;

    public MenuPageView(Context context) {
        super(context);
    }

    public MenuPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuPageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public MenuPageView(Context context, List<MenuItemView> dataset){
        super(context);
        mDataset=dataset;
        initView(context);
    }
    
    public List<MenuItemView> getItems(){
        return mDataset;
    }
    
    private void initView(Context context){
        this.mContext=context;
        if(null!=mDataset && mDataset.size()>0){
            updateMenuItem();
            addGridView();
        }
    }
    
    private void addGridView(){
        if(null==mDataset){
            return;
        }
        removeAllViews();
        mGridView=new GridView(mContext);
        android.view.ViewGroup.LayoutParams param=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mGridView.setLayoutParams(param);
        mGridView.setNumColumns(column);
        mAdapter=new MenuPagerGridAdapter(mContext, mDataset);
        mGridView.setAdapter(mAdapter);
        addView(mGridView);
    }
    
    private void updateMenuItem(){
        if(null==mGridView || mAdapter==null){
            addGridView();
        }else{
            mAdapter.notifyDataSetChanged();
        }
    }
    
    public boolean updatePageForce(List<MenuItemView> dataset){
        if(null==dataset){
            return false;
        }
        mDataset=dataset;
        addGridView();
        return true;
    }
    
    public boolean updatePager(){
        updateMenuItem();
        return true;
    }
}
