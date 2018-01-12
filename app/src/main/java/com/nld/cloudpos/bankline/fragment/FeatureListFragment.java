package com.nld.cloudpos.bankline.fragment;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.query.TransQueryFragment;
import com.nld.cloudpos.payment.activity.Network;
import com.nld.cloudpos.payment.activity.QueryBalance;
import com.nld.cloudpos.payment.activity.SettleMainActivity;
import com.nld.cloudpos.payment.activity.SettleResult;
import com.nld.cloudpos.payment.activity.SignSuccessActivity;
import com.nld.cloudpos.payment.activity.TransErrorResultActivity;
import com.nld.cloudpos.payment.activity.preauth.PreAuthCompleteSubmitActivity;
import com.nld.cloudpos.payment.activity.preauth.PreAuthFormFragment;
import com.nld.cloudpos.payment.activity.preauth.SuperPasswordActivity;
import com.nld.cloudpos.payment.controller.ScanPayActivity;
import com.nld.cloudpos.payment.controller.ScanQueryOrderActivity;
import com.nld.cloudpos.payment.controller.TransUtils;
import com.nld.cloudpos.payment.interfaces.IMenuItemClick;
import com.nld.cloudpos.payment.view.MenuItemView;
import com.nld.cloudpos.util.CommonContants;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.ShareBankPreferenceUtils;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import common.StringUtil;

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

/**
 * Created by jidongdong on 2017/2/7.
 * <p>
 * 功能列表菜单Fragment
 */

public class FeatureListFragment extends BaseFragment implements IMenuItemClick {
    private LinearLayout layout_bank_1, layout_bank_2, bar_code_menus, other_menus;
    TransRecordDao transRecordDao;
    //做这些交易前要判断流水数据不超过500条
    private int[] checkIds = {R.drawable.icon_kxf, R.drawable.icon_ysq2, R.drawable.icon_xfcx, R.drawable.icon_ysqcx};
    Map<String, Integer> menu_bank_1 = new LinkedHashMap<String, Integer>() {{
        put("POS签到", R.drawable.icon_sign);
        put("卡消费", R.drawable.icon_kxf);
        put("消费撤销", R.drawable.icon_xfcx);
        put("余额查询", R.drawable.icon_yecx);
    }};
    Map<String, Integer> menu_bank_2 = new LinkedHashMap<String, Integer>() {{
        put("预授权", R.drawable.icon_ysq2);
        put("预授权撤销", R.drawable.icon_ysqcx);
        put("预授权完成", R.drawable.icon_ysqwc);
        put("预授权完成<br/>撤销", R.drawable.icon_ysqwccx);
    }};
    Map<String, Integer> menu_decode_bar = new LinkedHashMap<String, Integer>() {{
        put("消费", R.drawable.icon_xf);
        put("退货", R.drawable.icon_th);
        put("联机退货", R.drawable.icon_tuihuo);
        put("扫码查单", R.drawable.icon_smcd);
        put("", 0);
    }};
    Map<String, Integer> menu_other = new LinkedHashMap<String, Integer>() {{
        put("批结算", R.drawable.icon_pjs);
//        put("重打印", R.drawable.icon_cdy);
        put("业务参数同步", R.drawable.icon_ywsjtb);
        put("交易查询", R.drawable.icon_jycx);
        put("", 0);
    }};

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public int doGetContentViewId() {
        return R.layout.layout_feature_menu_list;
    }

    @Override
    public void doInitSubViews(View view) {
        layout_bank_1 = queryViewById(R.id.layout_bank_1);
        layout_bank_2 = queryViewById(R.id.layout_bank_2);
        bar_code_menus = queryViewById(R.id.bar_code_menus);
        other_menus = queryViewById(R.id.other_menus);
        fillMenuLayout(layout_bank_1, menu_bank_1);
        fillMenuLayout(layout_bank_2, menu_bank_2);
        fillMenuLayout(bar_code_menus, menu_decode_bar);
        fillMenuLayout(other_menus, menu_other);
        transRecordDao = new TransRecordDaoImpl();
    }

    private void fillMenuLayout(LinearLayout layout, Map<String, Integer> items) {
        Iterator<Map.Entry<String, Integer>> iterator = items.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            MenuItemView item = new MenuItemView(getActivity(), entry.getKey(), entry.getValue(), entry.getValue() == 0 ? null : this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            item.setLayoutParams(layoutParams);
            item.setTag(entry.getValue());
            layout.addView(item);
        }
    }

    @Override
    public void doInitData() {
        super.doInitData();
    }

    @Override
    public void onMenuItemClick(View v, int iconId) {

        if (!doByStpe()){
            LogUtils.d("批处理未完成");
            return;
        }
        // 流水超过500要先去做批结
        if (isBankWaterOver(iconId)) {
            return;
        }
        Intent intent = null;
        switch (iconId) {
            case R.drawable.icon_kxf: //卡消费
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_CONSUME);
                PreAuthFormFragment.startSelfFragment(mActivity, Constant.TYPE_CARD_EXPENSE);
                break;
            case R.drawable.icon_sign:// pos签到
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_SIGN);
                startActivityForResult(new Intent(getActivity(),StartTransActivity.class), Constant.BANK_SIGN);
                break;
            case R.drawable.icon_jycx:// 交易查询
                if (!checkRevel()){
                    return;
                }
                startFragment(mActivity, TransQueryFragment.class.getName(), getString(R.string.query_trade_string));
                break;
            case R.drawable.icon_xfcx:// 消费撤销
                intent = new Intent(getActivity(), SuperPasswordActivity.class);
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_CONSUME_CX);
                startActivity(intent);
                break;
            case R.drawable.icon_ysq2:// 预授权

                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_PRE);
                PreAuthFormFragment.startSelfFragment(mActivity, Constant.TYPE_CARD_PREAUTHORIZATION);
                break;
            case R.drawable.icon_ysqwc:// 预授权完成

                boolean pre_complete_swipe = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_AUTH_SALE_SWIPE, true);
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_PRE_COMPLET);
                Intent pre_complete =null;
            /*    if (pre_complete_swipe){  //需要刷卡 ，暂时这个是否需要卡 用不上，待用
                    pre_complete = new Intent(getActivity(), PreAuthCompleteSubmitActivity.class);
                }else {
                    pre_complete =   new Intent(getActivity(),
                            PreAuthCompleteInputAuthActivity.class);
                }*/
                pre_complete = new Intent(getActivity(), PreAuthCompleteSubmitActivity.class);
                startActivity(pre_complete);
                break;
            case R.drawable.icon_ysqcx:// 预授权撤销

                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_PRE_CX);
                intent = new Intent(getActivity(), SuperPasswordActivity.class);
                startActivity(intent);
                break;
            case R.drawable.icon_ysqwccx:// 预授权完成撤销
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_PRE_COMPLET_CX);
                intent = new Intent(getActivity(), SuperPasswordActivity.class);
                startActivity(intent);
                break;
            case R.drawable.icon_tuihuo: //联机退货
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_LJTH);
                intent = new Intent(getActivity(), SuperPasswordActivity.class);
                startActivity(intent);
                break;
            case R.drawable.icon_xf:  //二维码消费
                startActivity(new Intent(getActivity(), ScanPayActivity.class));
                break;
            case R.drawable.icon_th://二维码扫码退货
                if (!ShareScanPreferenceUtils.getBoolean(getActivity(), TransParamsValue.PARAMS_KEY_TRANS_SCAN_REFUND, false)) {
                    ToastUtils.showToast("该功能尚未开通，请联系管理员");
                    return;
                }
                if (!TransUtils.isExitLocalScanData()) {
                    ToastUtils.showToast("无法获取到商终，请重启试试，谢谢");
                    return;
                }
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_WX_TH);
                startActivity(new Intent(getActivity(), SuperPasswordActivity.class));
                break;
            case R.drawable.icon_smcd://扫码查单
                startActivity(new Intent(getActivity(), ScanQueryOrderActivity.class));
                break;
            case R.drawable.icon_ywsjtb: //业务参数同步
                asyParams();
                break;
            case R.drawable.icon_yecx: //余额查询
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_QUERY_BALANCE);
                startActivity(new Intent(getActivity(), QueryBalance.class));
                break;
            case R.drawable.icon_cdy:
              //  startFragment(mActivity, ReprintMainFragment.class.getName(), getString(R.string.page_title_reprint));
                break;
            case R.drawable.icon_pjs://批结算
                intent = new Intent(getActivity(), SettleMainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void asyParams() {
        ShareScanPreferenceUtils.putBoolean(getActivity(), TransParamsValue.PARAMS_IS_PARAM, false);
        Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_PARAMS);
        ScanPayBean scanPayBean = new ScanPayBean();
        scanPayBean.setRequestUrl(CommonContants.url);
        scanPayBean.setTransType(TransParamsValue.InterfaceType.POSPARMSET);
        scanPayBean.setProjectType(EncodingEmun.antCompany.getType());
        scanPayBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID));

        Intent intent = new Intent(getActivity(),Network.class);
        intent.putExtra("scan",scanPayBean);
        startActivity(intent);
    }

    /**
     * 检查批结是否完成
     */
    private boolean checkRevel() {
        return doByStpe();
    }

    private boolean isBankWaterOver(int iconId) {
        try {
            if (Arrays.asList(checkIds).contains(iconId)) {
                int count = transRecordDao.getTransCount();
                int recordCountMax = Integer.valueOf(ParamsUtil.getInstance().getParam("systracemax"));
                if (count > recordCountMax) {
                    Toast.makeText(mActivity, "交易笔数超过" + recordCountMax + ",请先进行批结", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            LogUtils.d(e);
        }
        return false;
    }


    /**
     * 根据批结中断状态处理
     * @return
     */
    public  boolean doByStpe() {
        String step = ShareBankPreferenceUtils.getString(Constant.BATCH_SETTLE_INTERRUPT_FLAG, Constant.BATCH_SETTLE_INTERRUPT_STEP4);
        boolean isBettleDone = true;
        switch (step) {
            case Constant.BATCH_SETTLE_INTERRUPT_STEP1:
                isBettleDone = false;
                ToastUtils.showToast("批结算未完成，请先进行批结算");
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        doSettleSend();
                    }
                }.start();
                break;
            case Constant.BATCH_SETTLE_INTERRUPT_STEP2:
            case Constant.BATCH_SETTLE_INTERRUPT_STEP3:
                ToastUtils.showToast("批结算未完成，请先进行批结算");
                isBettleDone = false;
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        doPrintDetail();
                    }
                }.start();
                break;
            case Constant.BATCH_SETTLE_INTERRUPT_STEP4:
                isBettleDone = true;
                break;
            default:
                break;
        }
        return isBettleDone;
    }

    /**
     * 批结算打印中断
     */
    private  void doPrintDetail() {
        Intent intent = new Intent(getActivity(),SettleResult.class);
        Map<String, String> resultMap = StringUtil.transStringToMap(ShareBankPreferenceUtils.getString(Constant.PARAMS_SETTLE_DATA, ""));
        Cache.getInstance().setResultMap(resultMap);
        startActivity(intent);
    }

    /**
     * 批结对账不平的时候，批上送中断
     */
    private  void doSettleSend() {
        Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_BATCH_SEND_START);
        Map<String, String> resultMap = StringUtil.transStringToMap(ShareBankPreferenceUtils.getString(Constant.PARAMS_SETTLE_DATA, ""));
        resultMap.put("respcode", "95");
        Cache.getInstance().setResultMap(resultMap);
        Cache.getInstance().setErrCode("95");
        Intent intent = new Intent(getActivity(), StartTransActivity.class);
        startActivityForResult(intent, Constant.BANK_SEND_SETTLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == Constant.BANK_SEND_SETTLE){
                startActivity(new Intent(getActivity(),SettleResult.class));
            }else if (requestCode == Constant.BANK_SIGN && null != data){  //签到成功
                String transResultTip = data.getStringExtra("transResultTip");
                Intent intent = new Intent(getActivity(), SignSuccessActivity.class);
                intent.putExtra("transResultTip",transResultTip);
                startActivity(intent);
            }
        }else if (resultCode == RESULT_FIRST_USER){
            if (requestCode == Constant.BANK_SIGN){  //签到失败
                startActivity(new Intent(getActivity(),TransErrorResultActivity.class));
            }
        }
    }
}