package com.nld.cloudpos.payment.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nld.cloudpos.payment.view.MenuItemView;

import java.util.List;

public class MenuPagerGridAdapter extends BaseAdapter {

    private Context mContext;
    private List<MenuItemView> mDataset;
    
    public MenuPagerGridAdapter(Context context, List<MenuItemView> dataset) {
        mDataset=dataset;
        mContext=context;
    }
    
    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        return mDataset.get(position);
    }

}
