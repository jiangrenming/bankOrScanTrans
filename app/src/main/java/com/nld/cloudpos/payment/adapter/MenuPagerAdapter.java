package com.nld.cloudpos.payment.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.nld.cloudpos.payment.view.MenuPageView;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MenuPagerAdapter extends PagerAdapter {

    private Logger logger= Logger.getLogger(MenuPagerAdapter.class);
    
    private List<MenuPageView> mPagers=new ArrayList<MenuPageView>();
    private Context mContext;
    private int mCount;
    private int mCurrentPos=0;
    
    public MenuPagerAdapter(Context context, List<MenuPageView> pagers) {
        mContext=context;
        mPagers.addAll(pagers);
        mCount=mPagers.size();
        
        if(2==mCount){
            MenuPageView pageView1=new MenuPageView(mContext, mPagers.get(0).getItems());
            mPagers.add(pageView1);
            MenuPageView pageView2=new MenuPageView(mContext, mPagers.get(1).getItems());
            mPagers.add(pageView2);
        }
    }
  
    public int getCurrentPos(){
        return mCurrentPos;
    }
    
    @Override
    public int getCount() {
        if (mCount == 1) {// 只有一页时不动。
            return mCount;
        }
        return Integer.MAX_VALUE;
    }
    /** 
    * 判断出去的view是否等于进来的view 如果为true直接复用 
    */  
   @Override
   public boolean isViewFromObject(View arg0, Object arg1) {
       return arg0 == arg1;  
   }  
   /** 
    * 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来，就是position， 
    * 因为mImageViewList只有五条数据，而position将会取到很大的值， 
    * 所以使用取余数的方法来获取每一条数据项。 
    */  
   @Override
   public void destroyItem(ViewGroup container, int position, Object object) {
//       container.removeView(mPagers.get(position % mPagers.size()));  
   }  

   /** 
    * 创建一个view， 
    */  
   @Override
   public Object instantiateItem(ViewGroup v, int i) {
//       if (v.getChildCount() == mPagers.size()) {
           v.removeView(mPagers.get(i % mPagers.size()));
//       }
       try{
       v.addView(mPagers.get(i % mPagers.size()), 0);
       }catch(Exception e){
           logger.error("addView异常：",e);
       }
       mCurrentPos=i % mPagers.size();
       if(mCurrentPos>=mCount){//针对只有两页时做处理
           mCurrentPos=2;
       }
       return mPagers.get(i % mPagers.size());
   }

   /**
    * 据说是为了解决调用notifyDatasetChanged不会刷新pager的问题，未验证
    */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }  
    
}
