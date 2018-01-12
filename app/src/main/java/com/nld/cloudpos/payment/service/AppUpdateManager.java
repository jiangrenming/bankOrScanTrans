package com.nld.cloudpos.payment.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.payment.view.dialog.UpdateDialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import common.DensityUtils;

public class AppUpdateManager {
    private Activity mContext;
    private Thread downLoadThread;
    private TextView percent;
    private boolean showToast = false;
    private int progress = 0;
    private String apkUrl;
    /* 下载包安装路径 */
//	private static final String savePath = CommonUtils.getAppCacheDirPath();
    private static final String saveFileName = "/mnt/sdcard/" + System.currentTimeMillis() + ".apk";

    private static final int DOWNLOAD_UPDATE = 1;
    private static final int DOWNLOAD_OVER = 2;
    private static final int DOWNLOAD_FAILED = 3;

    private boolean interceptFlag = false;
    private UpdateDialog updateDialog;

    public AppUpdateManager(Activity context, boolean showToast) {
        this.mContext = context;
        this.showToast = showToast;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_UPDATE:
                    updatePercentView();
                    break;
                case DOWNLOAD_OVER:
                    updatePercentView(mContext.getString(R.string.Downloaded));
                    installApk();
                    break;
                case DOWNLOAD_FAILED:
                    interceptFlag = true;
                    downLoadThread = null;
                    updatePercentView(mContext.getString(R.string.Download_failed));
                    break;
                default:
                    break;
            }
        }

    };

    private void installApk() {
        try {
            File apkfile = new File(saveFileName);
            if (!apkfile.exists()) {
                return;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
            mContext.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkAppUpdate() {
       /* String sn = SharePreferenceUtils.getString(CommonParams.SN, "");
        String merchant_id = GlobeData.merchantInfo.getMerchantId();
        if (TextUtils.isEmpty(sn) || TextUtils.isEmpty(merchant_id)) {
            Log.d("AppUpdateManager", "===sorry! sn or merchant_id = null, app update failed!");
            return;
        }
        String access_token = Base64.encode(MD5Util.getStringMD5(sn + merchant_id).getBytes());
        String url = ApiTools.SAMPLE_API_CHECK_VERSION + "access_token=" + access_token +
                "&sn=" + sn + "&merchant_id=" + merchant_id + "&version=" + CommonUtil.getAppVersion(mContext) + "&type=1";
        AsyncHttpUtil.get(url, new AsyncRequestCallBack<String>(mContext) {
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
//                super.onFailure(httpException, errorMsg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                super.onSuccess(responseInfo);
                try {
                    UpgradeRsp appUpgradeRsp = AsyncHttpUtil.gson.fromJson(responseInfo.result, UpgradeRsp.class);
                    if (appUpgradeRsp.is_need()) {
                        if (!TextUtils.isEmpty(appUpgradeRsp.getData())) {
                            apkUrl = new String(Base64.decode(appUpgradeRsp.getData())).replace("\\", "");
                            showDialog("发现新版本，请立即升级", true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    private void showDialog(String tipMsg, boolean anyway) {
        if (anyway) {
            updateDialog = new UpdateDialog(mContext, false);
            updateDialog.setMessage(tipMsg.replace("\\n", "\n"));
//            updateDialog.setMessageViewGravity(Gravity.LEFT);
            updateDialog.setMessagePadding(DensityUtils.dp2px(mContext, 20), 0, 0, 0);
            updateDialog.setPositiveButton(mContext.getString(R.string.updated_immediately), new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    String str = percent.getText().toString();
                    if (str.equals(mContext.getString(R.string.Downloaded))) {
                        installApk();
                    } else if (str.equals(mContext.getString(R.string.updated_immediately))) {
                        startDownload();
                    } else if (str.equals(mContext.getString(R.string.Download_failed))) {
                        if (mContext instanceof LauncherActivity) {
                            mContext.finish();
                        }
                    }
                }
            });
            updateDialog.setCrossVisibility(View.GONE);
            percent = updateDialog.getPositiveButton();
            updateDialog.show();
        } else {
            updateDialog = new UpdateDialog(mContext, true);
            updateDialog.setTitle(R.string.upgrage_prompt);
            updateDialog.setMessage(tipMsg.replace("\\n", "\n"));
            updateDialog.setMessageViewGravity(Gravity.LEFT);
            updateDialog.setMessagePadding(DensityUtils.dp2px(mContext, 20), 0, 0, 0);
            updateDialog.setPositiveButton(mContext.getString(R.string.updated_immediately), new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    String str = percent.getText().toString();
                    if (str.equals(mContext.getString(R.string.Downloaded))) {
                        installApk();
                    } else if (str.equals(mContext.getString(R.string.updated_immediately)) || str.equals(mContext.getString(R.string.Download_failed))) {
                        startDownload();
                    }
                }
            });
            updateDialog.setCrossListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateDialog.dismiss();
                }
            });
            updateDialog.setCrossVisibility(View.VISIBLE);
            //alertDialog.setCancelable(true);
            percent = updateDialog.getPositiveButton();
            updateDialog.show();
            /*String[] str = new String[] {mContext.getString(R.string.app_sure) };
            AlertView alertView = new AlertView(mContext.getString(R.string.prompt), tipMsg.replace("\\n", "\n"), null, null,
					str, mContext, AlertView.Style.Alert, new OnItemClickListener() {
						@Override
						public void onItemClick(Object object, int position) {
							if(position == 0) {
								String str = percent.getText().toString();
								if(str.equals(mContext.getString(R.string.Downloaded))) {
									installApk();
								} else if(str.equals(mContext.getString(R.string.upgrade_now))
										|| str.equals(mContext.getString(R.string.Download_failed))) {
									startDownload();
								}
							}
						}
					});
			percent = alertView.getAlertTextView();
			alertView.show();*/
        }
    }

    protected void startDownload() {
        if (downLoadThread == null) {
            downLoadThread = new Thread(mdownRunnable);
            downLoadThread.start();
        }
    }

    private void updatePercentView() {
        if (percent != null) {
            percent.setText(mContext.getString(R.string.Downloading) + progress + "%");
        }
    }

    private void updatePercentView(String str) {
        if (percent != null) {
            percent.setText(str);
        }
    }

    HttpURLConnection httpConnection;
    private Runnable mdownRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (TextUtils.isEmpty(apkUrl)) {
                    return;
                }
                URL url = new URL(apkUrl);
                httpConnection = (HttpURLConnection) url.openConnection();
//				httpConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");
                httpConnection.setRequestProperty("User-Agent", "PacificHttpClient");
                httpConnection.setConnectTimeout(10000);
                httpConnection.setReadTimeout(15000);
//				updateTotalSize = httpConnection.getContentLength();
//				if(httpConnection.getResponseCode() == 404) {
//					mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
//					return;
//				}
                httpConnection.connect();
                int length = httpConnection.getContentLength();
                InputStream is = httpConnection.getInputStream();

                File file = new File(saveFileName);
                if (file.exists()) {
                    file.delete();
                }
                File ApkFile = new File(saveFileName);
                FileOutputStream fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[4 * 1024];
                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    mHandler.sendEmptyMessage(DOWNLOAD_UPDATE);
                    if (numread <= 0) {
                        mHandler.sendEmptyMessage(DOWNLOAD_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);
                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                httpConnection.disconnect();
                mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
                e.printStackTrace();
            } catch (IOException e) {
                httpConnection.disconnect();
                mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
                e.printStackTrace();
            }
        }
    };
}