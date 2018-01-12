package com.nld.cloudpos.bankline.fragment.query;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.bankline.view.BillItemView;
import com.nld.cloudpos.util.NormalPrintUtils;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.utils.Constant;

import java.util.Map;

import common.StringUtil;
import common.Utility;

/**
 * 订单明细
 * Created by cxg on 2017/10/9.
 */

public class OrderDetailsFragment extends BaseFragment {
    private LinearLayout llContainer;
    private ParamConfigDao paramConfigDao;
    private TransRecord record;
    private static boolean isPrint = false; // 是否在打印中

    private static Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NormalPrintUtils.PRINT_STATE_FAILE:
                case NormalPrintUtils.PRINT_STATE_SUCESS:
                    isPrint = false;
                    break;
            }
        }
    };

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_order_details;
    }

    @Override
    public void doInitSubViews(View view) {
        llContainer = queryViewById(R.id.ll_container);
        queryViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPrint) {
                    LogUtils.d("正在在打印详情。。。");
                    return;
                }
                finish();
            }
        });
        queryViewById(R.id.tv_print).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPrint) {
                    LogUtils.d("正在在打印详情。。。");
                    return;
                }
                isPrint = true;
                NormalPrintUtils.printTransRecode(record, mActivity, mHandle, true);
            }
        });
    }

    @Override
    public void doInitData() {
        Bundle arguments = getArguments();
        record = (TransRecord) arguments.getSerializable("orderDetail");
        if (record == null) {
            LogUtils.d("订单明细传入 TransRecord 为 null");
            record = new TransRecord();
        }
        LogUtils.d("record: " + record);
        paramConfigDao = new ParamConfigDaoImpl();

        Map<String, String> recordMap = Utility.transformToMap(record);
        String[] orderDetailsStr = getResources().getStringArray(R.array.order_details);
        for (String item : orderDetailsStr) {
            BillItemView itemView = new BillItemView(new ContextThemeWrapper(getActivity(), R.style.bill_item_order_details), null, 0);
            itemView.setLeftText(item + ":");
            itemView.setTextContent(getTypeName(item, recordMap));
            if ("交易金额".equals(item)) {
                itemView.setContentColor(getResources().getColor(R.color.red));
            }
            llContainer.addView(itemView);
        }
    }

    @Override
    public boolean onBack() {
        if (isPrint) {
            return true;
        }
        return super.onBack();
    }

    private String getTypeName(String type, Map<String, String> record) {
        String value = "";
        try {
            switch (type) {
                case "交易类型":
                    value = Constant.transType2Value(record.get("transType"));
                    break;
                case "交易状态":
                    String transType = record.get("transState");
                    switch (transType) {
                        case "1":
                            value = "交易成功";
                            break;
                        case "2":
                            value = "已撤销";
                            break;
                        default:
                            break;
                    }
                    break;
                case "支付方式":
                    value = "银行卡";
                    break;
                case "交易金额":
                    value = "￥" + Utility.unformatMount(record.get("transamount"));
                    break;
                case "交易卡号":
                    value = Utility.formatCardno(record.get("priaccount"));
                    break;
                case "交易时间":
                    value = getTransTime(record);
                    break;
                case "批次号":
                    value = record.get("batchbillno").substring(0, 6);
                    break;
                case "凭证号":
                    value = record.get("systraceno");
                    break;
                case "参考号":
                    value = record.get("refernumber");
                    break;
                case "操作员":
                    value = getOperate();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LogUtils.d(type + " 获取对应值出错： " + e.getMessage());
        }

        return value;
    }

    private String getTransTime(Map<String, String> record) {
        String strdate = record.get("translocaldate");
        String strtime = record.get("translocaltime");
        if (StringUtil.isEmpty(strdate)) {
            strdate = "0000";

        }
        if (StringUtil.isEmpty(strtime)) {
            strtime = "000000";

        }
        String datetime = strdate
                + strtime;
        datetime = Utility.printFormatDateTime(datetime);
        return datetime;
    }

    private String getOperate() {
        String operatorcode = paramConfigDao.get("operatorcode");
        return operatorcode;
    }
}
