package com.nld.cloudpos.payment.activity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.ecash.activity.AppointLoadCard;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

public class QuanCunMenuActivity extends BaseAbstractActivity {

	private ListView mListView;
	private LayoutInflater mInflater;
	private MenuListAdapter mAdapter;
	private MenuClickListener mLitener;

	private String[] titles = { "指定账户圈存", "非指定账户圈存"
	// "现金充值",
	// "现金充值撤销",
	};

	@Override
	public int contentViewSourceID() {
		return R.layout.act_ic_set;
	}

	@Override
	public void initView() {
		setTopDefaultReturn();
		setTopTitle("圈存");
		mListView = (ListView) findViewById(R.id.ic_set_listview);
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		updateMenus();
	}

	public void updateMenus() {
		if (null == mAdapter) {
			mAdapter = new MenuListAdapter();
			mLitener = new MenuClickListener();
			mListView.setAdapter(mAdapter);
		} else {
			mAdapter.notifyDataSetChanged();
		}

	}

	public class MenuClickListener {
		public void onClick(int iconId) {
			Intent intent = null;
			Cache.getInstance().clearAllData();
			switch (iconId) {
			case 0:// 指定账户圈存
				Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_QC_ZD);
				intent = new Intent(mContext, AppointLoadCard.class);
				startActivity(intent);
				break;
			case 1:// 非指定账户圈存
				Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_QC_FZD);
				intent = new Intent(mContext, QuanCunFZDSwipeOutActivity.class);
				startActivity(intent);
				break;
			default:
				showTip("此功能尚未开通");
				break;
			}
		}
	}

	private class MenuListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public Object getItem(int position) {
			return titles[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			view = mInflater.inflate(R.layout.item_ic_set_menu, null);
			TextView title = (TextView) view.findViewById(R.id.item_set_title);
			ImageView icon = (ImageView) view.findViewById(R.id.item_set_arrow);
			title.setText(titles[position]);
			icon.setVisibility(View.GONE);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mLitener.onClick(position);
				}
			});
			return view;
		}

	}

	@Override
	public void onServiceConnecteSuccess(AidlDeviceService service) {
	}

	@Override
	public void onServiceBindFaild() {
	}

	@Override
	public boolean saveValue() {
		return false;
	}
}
