package com.nld.thirdlibrary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 * @author jiangrenming
 * @date 2017/12/27
 */

public class ThridFragment extends Fragment{

    private View mFragmentView;
    private View  iv_banner;
    private int mResId;
    public  ThridFragment(int ResId){
        this.mResId = ResId;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.menu_third_page, null);
        mFragmentView = mFragmentView.findViewById(R.id.grid_view);
        iv_banner.setBackgroundResource(mResId);
        return mFragmentView;
    }

}
