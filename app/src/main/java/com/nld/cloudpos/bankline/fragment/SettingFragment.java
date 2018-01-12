package com.nld.cloudpos.bankline.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lidroid.xutils.util.LogUtils;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.appsetting.AdminPwdCheckFragment;
import com.nld.cloudpos.bankline.fragment.appsetting.PWDSettingFragment;
import com.nld.cloudpos.bankline.fragment.appsetting.ParamsDownloadFragment;
import com.nld.cloudpos.bankline.fragment.appsetting.PrinterSetDetailFragment;
import com.nld.cloudpos.bankline.fragment.appsetting.SPInfoFragment;
import com.nld.cloudpos.bankline.fragment.appsetting.TradeSettingFragment;
import com.nld.cloudpos.bankline.fragment.appsetting.VersionInfoFragment;
import com.nld.cloudpos.payment.interfaces.IMenuItemClick;
import com.nld.cloudpos.payment.view.MenuItemView;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by syyyz on 2017/2/7.
 */

public class SettingFragment extends BaseFragment implements IMenuItemClick {
    private LinearLayout layout_items;

    private Map<String, Integer> setting_collection = new LinkedHashMap<String, Integer>() {{
        put("打印设置", R.drawable.icon_dysz);
        put("交易设置", R.drawable.icon_jysz);
        put("密码管理", R.drawable.icon_codeset);
        put("商终信息", R.drawable.icon_deviceinfor);
//        put("清除缓存", R.drawable.icon_qchc);
        put("参数下载", R.drawable.icon_params_download);
        put("版本信息", R.drawable.icon_appinfor);
    }};

    @Override
    public int doGetContentViewId() {
        return R.layout.activity_fragment_setting;
    }

    @Override
    public void doInitSubViews(View view) {
        super.doInitSubViews(view);
        initView();
    }

    void initView() {
        layout_items = queryViewById(R.id.layout_fragment);
        fillMenuLayout();
    }

    private void fillMenuLayout() {
        Iterator<Map.Entry<String, Integer>> iterator = setting_collection.entrySet().iterator();
        int index_tag = 0;
        LinearLayout row_layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.layout_setting_row, null);
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            MenuItemView item = new MenuItemView(getActivity(), entry.getKey(), entry.getValue(), this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            item.setLayoutParams(layoutParams);
            item.setTag(++index_tag);
            if (row_layout != null) {
                row_layout.addView(item);
            }
            if (index_tag > 0 && index_tag % 4 == 0) {
                layout_items.addView(row_layout);
                row_layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.layout_setting_row, null);
            }
        }
        if (row_layout != null && row_layout.getChildCount() > 0) {
            for (int i = 4 - row_layout.getChildCount(); i > 0; i--) {
                MenuItemView item = new MenuItemView(getActivity(), "", 0, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1;
                item.setLayoutParams(layoutParams);
                row_layout.addView(item);
            }
            layout_items.addView(row_layout);
        }
    }

    private void clearCacheData() {
        final Dialog dialog = new Dialog(getActivity(), R.style.Dialog_Clear_Data_Style);
        dialog.setContentView(R.layout.dialog_layout_clear_cache);

        TextView tip_message = (TextView) dialog.findViewById(R.id.tip_message);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        Button btn_continue = (Button) dialog.findViewById(R.id.btn_continue);
        tip_message.setText(Html.fromHtml(getString(R.string.dialog_clear_data_tip_message)));
        ImageView tip_colose = (ImageView) dialog.findViewById(R.id.tip_colose);
        dialog.show();
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Cache.getInstance().clearAllData();
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(Constant.MESSAGE_GOTO_CASH_PAGE));
              //  Intent intent = new Intent(getActivity(), LoginActivity.class);
             //   startActivity(intent);
            }
        });
        tip_colose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onMenuItemClick(View v, int iconId) {
        switch (iconId) {
            case R.drawable.icon_dysz:  //打印设置
               startFragment(getActivity(), PrinterSetDetailFragment.class.getName(), getString(R.string.setting_print_string));
                break;
            case R.drawable.icon_jysz:  //交易设置
                startFragment(getActivity(), TradeSettingFragment.class.getName(), getString(R.string.setting_trade_string));
                break;
            case R.drawable.icon_codeset:  //密码管理
                startFragment(getActivity(), PWDSettingFragment.class.getName(), getString(R.string.setting_pwd_string));
                break;
            case R.drawable.icon_deviceinfor:  //终端信息
                startFragment(getActivity(), SPInfoFragment.class.getName(), getString(R.string.setting_sp_info_string));
                break;
            case R.drawable.icon_qchc:  //清理缓存
                startFragmentForResult(mActivity, AdminPwdCheckFragment.class.getName(),
                        getString(R.string.string_check_admin_pwd_title), Constant.ADMIN_PWD_CHECK_REQ_CODE, null);
                break;
            case R.drawable.icon_appinfor:  //版本信息
                startFragment(getActivity(), VersionInfoFragment.class.getName(), getString(R.string.setting_version_info_string));
                break;
            case R.drawable.icon_params_download: //参数下载
                startFragment(getActivity(), ParamsDownloadFragment.class.getName(), getString(R.string.setting_params_download));
                break;
            default:
                break;
        }
    }

    @Override
    public void onResult(int reqCode, int respCode, Intent intent) {
        LogUtils.i("AdminPwdCheckFragment finish");
        if (respCode == Constant.ADMIN_PWD_CHECK_RESULT_OK) {
            clearCacheData();
        } else {

        }
    }
}