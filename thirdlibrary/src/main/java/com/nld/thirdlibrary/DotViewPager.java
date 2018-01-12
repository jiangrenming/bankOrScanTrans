package com.nld.thirdlibrary;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import java.util.Timer;

/**
 * 带点的viewpager
 * 
 * @author CB
 * @time 2015年1月21日 上午9:27:56
 */
public class DotViewPager extends FrameLayout {
	
	/** viewpager */
	private ViewPager viewPager;
	private LinearLayout llDot;
	
	private Context context;
/*	*//** 第几张 *//*
	private int pagerIndex = 0;*/
	/** 轮播线程 */
	private final ThreadLocal<Timer> timer = new ThreadLocal<>();
	/** 选中时的资源 **/
	private int resCurrentDot = R.drawable.icon_nav_bg;
	/** 选中时的资源 **/
	private int resNormalDot = R.drawable.no_select;
	
	private PagerAdapter pagerAdapter;

	public DotViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context);
	}

	public DotViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	public DotViewPager(Context context) {
		super(context);
		initData(context);
	}
	
	
	
	private void initData(Context context) {
		this.context = context;
		View view = LayoutInflater.from(context).inflate(R.layout.dot_view_pager, this);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        llDot = (LinearLayout) findViewById(R.id.ll_dot);
		viewPager.setOffscreenPageLimit(999);
		viewPager.setCurrentItem(1);
		initEvent();
	}

	private void initEvent() {

		// 翻页事件
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				selDotStyle(position);
		
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int position) {
			}
		});
	}

	public void setAdapter(PagerAdapter pagerAdapter,int resDotSize) {
		if (pagerAdapter != null) {
			viewPager.setAdapter(pagerAdapter);
			this.pagerAdapter = pagerAdapter;
			notifyDot();
		}
	}
	public void setAdapter(PagerAdapter pagerAdapter) {
		setAdapter(pagerAdapter, -1);
	}
	
	/**
	 * 更新小圆点
	 */
	public void notifyDot() {
		notifyDot(-1);
	}
	/**
	 * 更新小圆点
	 * @param resDotSize
	 */
	public void notifyDot(int resDotSize) {

		int dotSize_w = DisplayUtils.getDimensPx(context, resDotSize == -1 ? R.dimen.dp8 : 4*resDotSize);
		int dotSize_h = DisplayUtils.getDimensPx(context, resDotSize == -1 ? R.dimen.dp2 : resDotSize);
		int dotPaddingSize = DisplayUtils.dip2px(context,context.getResources().getDimension(R.dimen.dp2));

		
		// 动态添加小圆点
		llDot.removeAllViews();
		for (int i = 0, count = this.pagerAdapter.getCount(); i < count; i++) {
			ImageView ivDot = new ImageView(context);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize_w, dotSize_h);
			params.leftMargin = dotPaddingSize;
			params.rightMargin = dotPaddingSize;
			ivDot.setLayoutParams(params);
			ivDot.setScaleType(ScaleType.FIT_XY);
			llDot.addView(ivDot);
		}
		selDotStyle(0);
		
		if (this.pagerAdapter.getCount() == 1) {
			llDot.setVisibility(View.GONE);
		} else {
			llDot.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 设置小圆点样式
	 * @param resCurrent
	 * @param resNormal
	 */
	public void selDotResource(int resCurrent,int resNormal) {
		resCurrentDot = resCurrent;
		resNormalDot = resNormal;
	}
	
	/**
	 * 设置小圆点位置
	 * @param gravity
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void setDotPosition(int gravity, int left, int top, int right, int bottom) {
		LayoutParams params = (LayoutParams) llDot.getLayoutParams();
		params.gravity = gravity;
		params.setMargins(left, top, right, bottom);
		llDot.setLayoutParams(params);
	}
	
	/**
	 * 设置小圆点位置
	 * @param :gravity
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void setViewPagerPosition(int left, int top, int right, int bottom) {
		LayoutParams params = (LayoutParams) viewPager.getLayoutParams();
		params.setMargins(left, top, right, bottom);
		viewPager.setLayoutParams(params);
	}
	
	/**
	 * 设置选中小圆点样式
	 * @param position
	 */
	private void selDotStyle(int position){
		for (int i = 0, count = llDot.getChildCount(); i < count; i++) {
			ImageView ivDot = (ImageView) llDot.getChildAt(i);
			ivDot.setBackgroundResource(i==position ? resCurrentDot : resNormalDot);
		}
	}
	
	
	public void onDestroy() {
		timer.get().cancel();
		timer.set(null);
	}
	
	public int getCurrentItem() {
		return viewPager.getCurrentItem();
	}
}
