package com.nld.cloudpos.bankline.fragment.query;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.adapter.FinalAdapter;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.payment.controller.QueryItemDetailsActivity;
import com.nld.cloudpos.payment.controller.ScanErrorActivity;
import com.nld.cloudpos.payment.view.MEditText;
import com.nld.cloudpos.util.CommonContants;
import com.nld.cloudpos.util.DialogFactory;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.wxtrade.activity.StartScanActivity;
import com.nld.starpos.wxtrade.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.bean.scan_query.ScanQueryDataBean;
import com.nld.starpos.wxtrade.bean.scan_query.ScanQueryRes;
import com.nld.starpos.wxtrade.bean.scan_query.ScanQueryWater;
import com.nld.starpos.wxtrade.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.ReturnCodeParams;
import com.nld.starpos.wxtrade.utils.params.ScanTransFlagUtil;
import com.nld.starpos.wxtrade.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.utils.params.TransType;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import common.DateTimeUtil;
import common.StringUtil;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Terrence on 2017/2/13.
 */

public class QRCodeQueryFragment extends BaseFragment implements OnClickListener, FinalAdapter
        .FinalAdapterListener {

    private PullToRefreshListView mTransLV;
    private MEditText mSearchEt;
    private FinalAdapter mAdapter;
    private ScanQueryRes scanQueryRes;
    private ImageView mSearch;
    private TextView mV1, mV2, mV3, mV4, mV5;
    private int PAG_NUM = 1;
    private int TOT_PAG = 1;
    private List<ScanQueryDataBean> mTotList;
    private List<ScanQueryDataBean> datas;
    private int type =1;
    private List<ScanQueryDataBean> localScanQueryData;
    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_qrcode_query;
    }

    @Override
    public void doInitSubViews(View view) {
        initView();
    }

    private void initView() {
        mTransLV = queryViewById(R.id.trans_query_listview);
        mTransLV.getLoadingLayoutProxy(true, true).setLoadingDrawable(getActivity().getResources().getDrawable(R.color.transparent));
        // 下拉刷新时的提示文本设置
        mTransLV.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新...");
        mTransLV.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在载入...");
        mTransLV.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新...");
        // 上拉加载更多时的提示文本设置
        mTransLV.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多...");
        mTransLV.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在载入...");
        mTransLV.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载更多...");
        mSearchEt = queryViewById(R.id.trans_query_search_et);
        mSearch = queryViewById(R.id.search);
        mSearch.setOnClickListener(this);
        mAdapter = new FinalAdapter(R.layout.item_network_query, this);

        mTransLV.setAdapter(mAdapter);

        mTransLV.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                PAG_NUM = 1;
                mTotList = new ArrayList<>();
                doListQuery();

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 上拉加载更多
                if (PAG_NUM < TOT_PAG) {
                    PAG_NUM += 1;
                    doListQuery();
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mTransLV.onRefreshComplete();
                        }
                    }, 100);

                    ToastUtils.showToast("没有更多数据");
                }
            }
        });

        mSearchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_SEND
                        ||actionId == EditorInfo.IME_ACTION_NEXT
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context
                            .INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                    }
                    String orderNo = mSearchEt.getEditableText().toString().trim();
                    if (TextUtils.isEmpty(orderNo)) {
                        ToastUtils.showToast("请输入订单号！");
                        return true;
                    }
                    localScanQueryData = selectAndUpdateListView(orderNo);
                    if (localScanQueryData == null || localScanQueryData.size() == 0){
                        ToastUtils.showToast("未查找到相关记录,请下来刷新试试");
                        mSearchEt.setText("");
                        return true;
                    }
                    type =2;
                    mAdapter.setItems(localScanQueryData);
                    return true;
                }
                return false;
            }
        });
        //点击列表项查看明细
        mTransLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String log_no = mTotList.get(position-1).getLog_no();
                ScanCache.getInstance().setTransCode(ScanTransFlagUtil.TRANS_CODE_WX_QUERY);
                if (!StringUtil.isEmpty(log_no)){
                    ScanPayBean auth = getScanBean(log_no);
                    Intent intent = new Intent(getActivity(), StartScanActivity.class);
                    intent.putExtra("scan",auth);
                    startActivityForResult(intent, TransType.SCAN_PAY_QUERY_CODE);
                }
            }
        });
    }

    private ScanPayBean getScanBean(String log_no){
        ScanPayBean auth = new ScanPayBean();
        auth.setLogNo(log_no);
        auth.setTransType(TransParamsValue.AntCompanyInterfaceType.SCAN_QUER);
        auth.setProjectType(EncodingEmun.antCompany.getType());
        auth.setRequestUrl(CommonContants.url);
        return  auth;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == TransType.SCAN_PAY_QUERY_CODE){
                if (data != null){
                    ScanPayBean scan_query = (ScanPayBean) data.getSerializableExtra("scan_query");
                    if (scan_query != null){
                        Intent i = new Intent(getActivity(),QueryItemDetailsActivity.class);
                        i.putExtra("scan_query",scan_query);
                        startActivity(i);
                    }
                }
            }
        }else if (resultCode == RESULT_FIRST_USER){
            Intent intent = new Intent(getActivity(),ScanErrorActivity.class);
            startActivity(intent);
        }else if (resultCode == RESULT_CANCELED){
            ToastUtils.showToast(getString(R.string.no_query_data));
        }
    }

    /**
     * 根据凭证号查找数据
     */
    private List<ScanQueryDataBean> selectAndUpdateListView(String trance) {
        List<ScanQueryDataBean> rsWaterList = new ArrayList<>();
        if (mTotList != null && mTotList.size() > 0){
            for (ScanQueryDataBean scanQuery :mTotList){
                String cseq_no = scanQuery.getCseq_no();
                if (trance.equals(cseq_no)){
                    rsWaterList.add(scanQuery);
                }
            }
        }
        return rsWaterList;
    }

    @Override
    public void doInitData() {
        DialogFactory.showDialog(mActivity, "正在加载数据...");
        mTotList = new ArrayList<>();
        doListQuery();

    }

    // 交易列表查询
    private synchronized void doListQuery() {
        type = 1;
        TreeMap<String, String> map = new TreeMap<>();
        String startDate = DateTimeUtil.getCurrentDate("yyyyMMdd");
        final ScanPayBean scanPayBean = new ScanPayBean();
        scanPayBean.setTransType( TransParamsValue.AntCompanyInterfaceType.TRANS_QUER);
        scanPayBean.setPageNo(PAG_NUM);
        scanPayBean.setPageNumber(200);
        scanPayBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID));
        scanPayBean.setDateTime(startDate);
        scanPayBean.setProjectType(EncodingEmun.antCompany.getType());

        String md5_key = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        if (!StringUtil.isEmpty(md5_key)){
            scanPayBean.setMd5_key(md5_key);
        }
        String transNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO); //流水号
        scanPayBean.setRequestId(System.currentTimeMillis() + transNo);
        String merchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID); //扫码商户号
        if (!StringUtil.isEmpty(merchantId)){
            scanPayBean.setMerchantId(merchantId);
        }
        scanPayBean.setRequestUrl(CommonContants.url);
        map.put(CommonParams.TYPE, scanPayBean.getTransType());  //接口类型
        map.put(CommonParams.PAG_NO, String.valueOf(scanPayBean.getPageNo()));  //页数
        map.put(CommonParams.PAG_NUM, String.valueOf(scanPayBean.getPageNumber()));  //记录数
        map.put(CommonParams.TERMINAL_NO, scanPayBean.getTerminalNo());  //终端号
        map.put(CommonParams.START_DATE, scanPayBean.getDateTime());  //默认当天时间起
        map.put(CommonParams.END_DATE, scanPayBean.getDateTime());   //默认当天时间止

 //       AsyncHttpUtil.setCommonBean(scanPayBean);

        AsyncHttpUtil.httpPostXutils(map,scanPayBean,new AsyncRequestCallBack<String>() {

            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                DialogFactory.dismissDialog();
                mTransLV.onRefreshComplete();
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!ScanTransUtils.checkHmac(responseInfo.result)){
                    ToastUtils.showToast("没有返回hmac校验值或验证失败");
                }
                super.onSuccess(responseInfo);
                DialogFactory.dismissDialog();
                mTransLV.onRefreshComplete();
                if (!StringUtil.isEmpty(responseInfo.result)){
                    Log.i("TAG","二维码查询结果"+responseInfo.result);
                    String[] split = responseInfo.result.split("&");
                    for (int i = 0; i < split.length; i++) {
                        if (split[i].startsWith("datas")){
                            int index = split[i].indexOf("=");
                            String value = split[i].substring(index + 1);
                           if (value.equals("0")){
                               ToastUtils.showToast("未查到相关记录");
                               return;
                           }
                        }
                    }
                    scanQueryRes =  DataAnalysisByJson.getInstance().getObjectByString2(responseInfo.result, ScanQueryRes.class);
                    if (ReturnCodeParams.SUCESS_CODE.equals(scanQueryRes.getReturnCode())){
                        if (PAG_NUM == 1) {
                            int totCnt = scanQueryRes.getTotCnt();
                            int pagNum = scanQueryRes.getPagNum();
                            if (totCnt !=0 && pagNum != 0) {
                                int netCount = totCnt / pagNum;
                                TOT_PAG = ((totCnt % pagNum) != 0) ? (netCount + 1) : netCount;
                            }
                        }
                         QRCodeQueryFragment.this.datas = scanQueryRes.getDatas();
                         mTotList.addAll(QRCodeQueryFragment.this.datas);
                         mAdapter.setItems(mTotList);
                    }else {
                        if (PAG_NUM == 1) {
                            mAdapter.clear();
                        }
                        ToastUtils.showToast(scanQueryRes.getMessage());
                    }
                }else {
                    ToastUtils.showToast("未查询到记录");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                String orderNo = mSearchEt.getEditableText().toString().trim();
                if (TextUtils.isEmpty(orderNo)) {
                    ToastUtils.showToast("请输入订单号！");
                    return;
                }
                type = 2;
                selectAndUpdateListView(orderNo);
                break;
            default:
                break;
        }
    }


    @Override
    public void bindView(int position, FinalAdapter.ViewHolder viewHolder) {
        mV1 = viewHolder.getView(R.id.item_trans_value_1, TextView.class);
        mV2 = viewHolder.getView(R.id.item_trans_value_2, TextView.class);
        mV3 = viewHolder.getView(R.id.item_trans_value_3, TextView.class);
        mV4 = viewHolder.getView(R.id.item_trans_value_4, TextView.class);
        mV5 = viewHolder.getView(R.id.item_trans_value_5, TextView.class);
        if (type == 1){
            ScanQueryDataBean datasBean = mTotList.get(position);
            mV1.setText(datasBean.getAc_dt());
            mV2.setText(datasBean.getCseq_no());
            mV3.setText(getTransType(datasBean));
            mV4.setText(formatAmount(datasBean.getTxn_amt()));
            mV5.setText(ScanTransUtils.setTextStyle(mActivity, mV5, datasBean));
        }else {
            ScanQueryDataBean scanQueryDataBean = localScanQueryData.get(position);
            mV1.setText(scanQueryDataBean.getAc_dt());
            mV2.setText(scanQueryDataBean.getCseq_no());
            mV3.setText(getTransType(scanQueryDataBean));
            mV4.setText(formatAmount(scanQueryDataBean.getTxn_amt()));
            mV5.setText(ScanTransUtils.setTextStyle(mActivity, mV5, scanQueryDataBean));
        }
    }

    /**
     * 交易类型。
     *
     * @param: transNetIntro
     * @return
     */
    private String getTransType(Object object) {
        String type = "";
        if (object instanceof ScanQueryDataBean){
            ScanQueryDataBean scanQueryDataBean = (ScanQueryDataBean) object;
            if (scanQueryDataBean != null && !TextUtils.isEmpty(scanQueryDataBean.getPaychannel())){
                switch (scanQueryDataBean.getPaychannel()){
                    case TransType.QueryNet.ALIPAY_NUMBER:
                        type = "支付宝支付";
                        break;
                    case TransType.QueryNet.WEICHAT_NUMBER:
                        type = "微信支付";
                        break;
                    default:
                        type = "未知类型";
                        break;
                }
                if (TextUtils.equals(scanQueryDataBean.getTxn_cd(), TransType.QueryNet.SCANREFUND)) {
                    type = "扫码退货";
                }
            }
        }else if (object instanceof ScanQueryWater){
            ScanQueryWater scanQueryWater = (ScanQueryWater) object;
            if (scanQueryWater != null && !TextUtils.isEmpty(scanQueryWater.getPayChannel())){
                switch (scanQueryWater.getPayChannel()){
                    case TransType.QueryNet.ALIPAY_NUMBER:
                        type = "支付宝支付";
                        break;
                    case TransType.QueryNet.WEICHAT_NUMBER:
                        type = "微信支付";
                        break;
                    default:
                        type = "未知类型";
                        break;
                }
                if (TextUtils.equals(scanQueryWater.getTXN_CD(), TransType.QueryNet.SCANREFUND)) {
                    type = "扫码退货";
                }
            }
        }
        return type;
    }

    private String formatAmount(String s) {
        if (CommonParams.TYPE .equals(TransParamsValue.AntCompanyInterfaceType.TRANS_QUER)) {
            s = s.substring(0, s.lastIndexOf("."));
        }
        double d = (double) (Integer.parseInt(s)) / 100;
        String format = String.format("%.2f", d);

        return format;
    }

    @Override
    public boolean setRightButton(Button view) {
        view.setVisibility(View.VISIBLE);
        view.setText("分类汇总");
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startFragment(getActivity(), QRCodeAggregateFragment.class.getName(), getString(R.string.aggregate));
            }
        });

        return true;
    }
}