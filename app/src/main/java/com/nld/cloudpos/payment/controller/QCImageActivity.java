package com.nld.cloudpos.payment.controller;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.activity.ErrorResult;
import com.nld.cloudpos.payment.activity.PrintResultActivity;
import com.nld.cloudpos.payment.activity.TransErrorResultActivity;
import com.nld.cloudpos.util.CommonContants;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.wxtrade.bean.scan_common.ResultStatus;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.thread.ComonThread;
import com.nld.starpos.wxtrade.thread.scan_thread.QCScanPayThread;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.utils.params.TransType;

import java.util.Hashtable;

/**
 * Created by jiangrenming on 2017/9/27.
 * 生成二维码界面
 */

public class QCImageActivity extends AbstractActivity{


    @ViewInject(R.id.iv_paycode)
    ImageView iv_paycode;
    @ViewInject(R.id.tv_message)
    TextView tv_message;

    private ComonThread mComonThread;
    private ResultStatus status;
    private AidlDeviceService aidlDeviceService;

    @Override
    public int contentViewSourceID() {
        return R.layout.show_qr_code_fragment;
    }

    @Override
    public void initView() {
        iv_paycode = (ImageView) findViewById(R.id.iv_paycode);
        tv_message = (TextView) findViewById(R.id.tv_message);
        setTopDefaultReturn();
    }

    @Override
    public void initData() {
       final ScanPayBean scanPayBean = (ScanPayBean) getIntent().getSerializableExtra("scan_pos");
        if (null != scanPayBean){
            setTopTitle(scanPayBean.getTransName());
            if(scanPayBean.getTransName().equals(getResources().getString(
                    R.string.trans_code_alipay))){
                tv_message.setText(getResources().getString(
                        R.string.qrcode_tips_alipay));
            }else {
                tv_message.setText(getResources().getString(
                        R.string.qrcode_tips_wechat));
            }
            try{
                Bitmap bitmap = createBitmap(scanPayBean.getScanResult(), 272,272);
                if (bitmap != null){
                    iv_paycode.setImageBitmap(bitmap);
                }
            }catch (Exception e){
                e.printStackTrace();
                Cache.getInstance().setErrDesc("生成二维码失败");
                gotoNext(new Intent(QCImageActivity.this, TransErrorResultActivity.class));
                finish();
            }

            scanPayBean.setTransNo(scanPayBean.getTransNo());  //流水号
            scanPayBean.setType(TransType.ScanTransType.TRANS_SCAN_POS_CHECK);  //交易类型
            scanPayBean.setBatchNo(scanPayBean.getBatchNo()); //批次号
            scanPayBean.setTransType(TransParamsValue.AntCompanyInterfaceType.SCAN_POS_QUERY); //扫码查询接口
            scanPayBean.setTerminalNo(scanPayBean.getTerminalNo()); //终端号
            scanPayBean.setOrderNo(scanPayBean.getOrderNo()); //订单号
            scanPayBean.setRequestUrl(CommonContants.url);
            scanPayBean.setProjectType(EncodingEmun.antCompany.getType());
            BankApplication.isCancle = false;

            new Thread(){
                @Override
                public void run() {
                    super.run();
                    for (int i = 0; i < 6 ; i++){
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (i == 5){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showToast("交易超时");
                                    finish();
                                }
                            });
                            break;
                        }
                        if ((null != status && status.isSucess())|| BankApplication.isCancle){ //---> 用户点击返回键时 停止线程的运行
                            break;
                        }
                        mComonThread = new QCScanPayThread(QCImageActivity.this,qcScanPayHandler,scanPayBean);
                        new Thread(mComonThread).start();
                    }
                }
            }.start();

        }else {
            Cache.getInstance().setErrDesc("数据格式转换错误");
            gotoNext(new Intent(QCImageActivity.this, TransErrorResultActivity.class));
            finish();
        }
    }

    private void gotoNext(Intent intent) {
        if (mComonThread != null){
            mComonThread =  null;
        }
        startActivity(intent);
        finish();
    }


    private Handler qcScanPayHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){

                case 0x03:
                    gotoNext(new Intent(QCImageActivity.this, ErrorResult.class));
                    break;
                case 0x04:
                    ToastUtils.showToast("等待用户输入密码付款");
                    break;
                case 0x06:
                    status = (ResultStatus) msg.getData().getSerializable("result");
                    break;
                case 0x16: //跳转打印流水
                    ScanTransRecord water = (ScanTransRecord) msg.getData().getSerializable("water");
                    Intent printerIntent = new Intent(QCImageActivity.this, PrintResultActivity.class);
                    if (null != water){
                        printerIntent.putExtra("water", water);
                        printerIntent.putExtra("transType", water.getPayChannel());
                    }
                    startActivity(printerIntent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {
        this.aidlDeviceService = service;
    }

    @Override
    public void onServiceBindFaild() {}

    @Override
    public boolean saveValue() {
        return false;
    }

    private Bitmap createBitmap(String text, int QR_width, int QR_height) throws Exception {
        if (TextUtils.isEmpty(text)){
            return null;
        }
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text,
                BarcodeFormat.QR_CODE, QR_width, QR_height, hints);
        int[] pixels = new int[QR_width * QR_height];
        for (int y = 0; y < QR_height; y++) {
            for (int x = 0; x < QR_width; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * QR_width + x] = 0xff000000;
                } else {
                    pixels[y * QR_width + x] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(QR_width, QR_height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, QR_width, 0, 0, QR_width, QR_height);
        return bitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BankApplication.isCancle = true;
    }
}
