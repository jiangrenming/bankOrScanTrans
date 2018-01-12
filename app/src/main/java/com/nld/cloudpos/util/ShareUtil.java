package com.nld.cloudpos.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.utils.Cache;

import common.StringUtil;

public class ShareUtil {
	/** SharedPreferences名称 */
	public static final String TAG_APP_CONFIG = "AppConfig";

	public static final Context context = Cache.getInstance().getContext();

	/**
	 * @description 获取应用版本号
	 * @return
	 */
	public static String getAppVersion() {
		PackageManager pm = context.getPackageManager();
		PackageInfo info = null;
		try {
			info = pm.getPackageInfo(context.getPackageName(),
					PackageManager.GET_CONFIGURATIONS);
			String versionName = info.versionName;
			return versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @description 获取配置参数
	 * @return
	 */
	public synchronized static String getAppConfig(String key,
                                                   String defaultValue) {
//		SharedPreferences shared = context.getSharedPreferences(TAG_APP_CONFIG,
//				Context.MODE_PRIVATE);
//		String value = shared.getString(key, defaultValue);

		ParamConfigDao mParamDao = new ParamConfigDaoImpl();
		String value = mParamDao.get(key);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * @description 保存配置参数
	 * @return
	 */
	public synchronized static void saveAppConfig(String key, String value) {
//		SharedPreferences shared = context.getSharedPreferences(TAG_APP_CONFIG,
//				Context.MODE_PRIVATE);
//		SharedPreferences.Editor editor = shared.edit();
//		editor.putString(key, value);
//		editor.commit();
		ParamConfigDao mParamDao = new ParamConfigDaoImpl();
	    mParamDao.save(key,value);
	}

	/**
	 * @description 获取交易流水号
	 * @return
	 */
	public synchronized static String getSerialNo() {
		SharedPreferences shared = context.getSharedPreferences(TAG_APP_CONFIG,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		String systracenoStr = shared.getString("serialNo", "000000");
		long systraceno = Long.valueOf(systracenoStr) + 1; // POS流水号
		if (systraceno > 999999) {
			systraceno = 1;
		}
		
		String sendSeqId = StringUtil.addHeadZero(systraceno, 6);
		editor.putString("serialNo", sendSeqId);
		editor.commit();
		return sendSeqId;
	}

	/**
	 * @description 获取密码键盘类型，默认内置
	 * @return
	 */
	public static String getPinPadType() {
		SharedPreferences shared = context.getSharedPreferences(TAG_APP_CONFIG,
				Context.MODE_PRIVATE);
		String KeyType = shared.getString("pinpadType", "1");
		return KeyType;
	}

	/**
	 * @description 获取密码键盘索引
	 * @return
	 */
	public static String getTmkIndex() {
		SharedPreferences shared = context.getSharedPreferences(TAG_APP_CONFIG,
				Context.MODE_PRIVATE);
		String keyindex = shared.getString("tmkIndex", "0");
		return keyindex;
	}

}
