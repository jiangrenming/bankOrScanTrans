package com.nld.cloudpos.payment.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Method;

public class MEditText extends EditText {
	
	private Context mContext;

	public MEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext=context;
	}

	public MEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
	}

	public MEditText(Context context) {
		super(context);
		mContext=context;
	}

	@Override
	protected void onAttachedToWindow() {
		setLongClickable(false);

		setCustomSelectionActionModeCallback(new ActionMode.Callback() {

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {

			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) {
				return false;
			}
		});
		setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		super.onAttachedToWindow();
	}

	public void closeSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);  
        imm.hideSoftInputFromWindow(getWindowToken(), 0);     
        ((Activity)mContext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        try {
	        Class<EditText> cls = EditText.class;
	        Method setSoftInputShownOnFocus;
	        setSoftInputShownOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
	        setSoftInputShownOnFocus.setAccessible(true);
	        setSoftInputShownOnFocus.invoke(this, false);
        } catch (Exception e) {
        e.printStackTrace();
        }
	}

}
