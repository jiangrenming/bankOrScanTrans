package com.nld.thirdlibrary;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jiangrenming
 * @date 2017/12/27
 */

public class ThirdAdapter extends FragmentPagerAdapter {

    private final List<Integer> lists = new ArrayList<Integer>();


    public ThirdAdapter(FragmentManager fm, Context context, List<Integer> lists) {
        super(fm);
        this.lists.clear();
        this.lists.addAll(lists);
    }

    @Override
    public Fragment getItem(int paramInt) {
        return new ThridFragment(lists.get(paramInt));
    }

    @Override
    public int getCount() {
        return lists.size();
    }
}
