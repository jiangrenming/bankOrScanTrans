package com.nld.cloudpos;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.payment.base.PbocListener;
import com.nld.cloudpos.payment.security.CheckImageLoaderConfiguration;
import com.nld.cloudpos.util.CommonContants;
import com.nld.cloudpos.util.LogcatHelper;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.factory.NetWorkFactory;
import com.nld.netlibrary.https.BankConfigBuilder;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.utils.BankConfig;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.wxtrade.local.ScanDbHelper;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.tinkerlibrary.Log.MyLogImp;
import com.nld.tinkerlibrary.util.TinkerManager;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by jiangrenming on 2017/12/21.
 */
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.nld.cloudpos.MyApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public class BankApplication extends DefaultApplicationLike {

    public BankApplication(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }
    //private AidlSystem systemInf;
    private String sn = "";
    public Logger logger = Logger.getLogger(BankApplication.class);
    public static final String LOGPATH = "/mnt/sdcard/mtms/log/mtms/com.xdl.cloudpos.payment/shoudan.log";

    private static final String TAG = "LauncherApplication";
    public static AidlDeviceService mDeviceService;
    public static X509Certificate mCacert;
    //PBOC监听
    public static PbocListener mPbocListener;
    public static Handler mainHandler;
    //全局上下文。
    public static Context context;
    public static boolean isCancle;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        context = getApplication();
        /*******************tinker热修复基本设置 start***************************/
        //其原理是分包架构，所以在加载初要加载其余的分包
        MultiDex.install(base);
        //保存当前对象
        TinkerManager.setTinkerApplicationLike(this);
        // 崩溃保护
        TinkerManager.initFastCrashProtect();
        // 是否重试,创建了存储路径
        TinkerManager.setUpgradeRetryEnable(true);
        //Log 实现，打印加载补丁的信息
        TinkerInstaller.setLogIml(new MyLogImp());
        // 运行Tinker ，通过Tinker添加一些基本配置
        TinkerManager.installTinker(this);
        Tinker tinker = Tinker.with(getApplication());
        /*************************end************************************/
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        // 生命周期，默认配置
        getApplication().registerActivityLifecycleCallbacks(callback);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        /***************加载sqlcipher所依赖的so库*****************/
        //SQLiteDatabase.loadLibs(this);
        //初始化扫码上下文
        ScanDbHelper.initContext(getApplication());
        mainHandler = new Handler();
        // 初始化缓存
        Cache.getInstance().setContext(getApplication());
        ToastUtils.init(getApplication());
        //初始化logger
        LogUtils.addLogAdapter(new AndroidLogAdapter(PrettyFormatStrategy.newBuilder().tag("POS-BZDIRECT").build()));
        LogUtils.addLogAdapter(new DiskLogAdapter(CsvFormatStrategy.newBuilder().tag("POS-BZDIRECT").folder(CommonContants.LOG_LOCAL_PAHT).build()));
        // 删除七日之前的本地log
        LogcatHelper.getInstance().deleteLog();
        CheckImageLoaderConfiguration.checkImageLoaderConfiguration(getApplication());
        try {
            configAndroidLog4j();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                logger.error("拉卡拉收单应用异常退出......", ex);
                logger.error(ex.getMessage());
                Toast.makeText(getApplication(), "程序发生故障退出，请稍后重试", Toast.LENGTH_SHORT).show();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        mPbocListener = new PbocListener();
        NetWorkFactory.getInstance().initFactory(getApplication());
        loadUnitCer();
    }

    /**
     * 加载银联证书
     */
    private void loadUnitCer() {
        new Thread() {
            @Override
            public void run() {
                LogUtils.e("读取银联证书");
                try {
                    AssetManager am = getApplication().getAssets();
                    InputStream ins = am.open(BankConfig.UNIONPAY_PATH);
                    // 读入客户端证书
                    PEMReader cacertfile = new PEMReader(new InputStreamReader(ins));
                    X509Certificate mCacert = (X509Certificate) cacertfile.readObject();
                    certHandler.sendMessage(certHandler.obtainMessage(0x01,mCacert));
                    cacertfile.close();
                } catch (Exception e) {
                    LogUtils.e("读取银联证书失败");
                }
            }
        }.start();
    }

    private Handler certHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x01){
                X509Certificate mCacert = (X509Certificate) msg.obj;
                if (null != mCacert){
                    //// TODO: 2017/12/7 配置请求的一些参数并加载证书
                    BankConfigBuilder bankConfigBuilder = null;
                    if (null != mCacert){
                        BankApplication.mCacert = mCacert;
                        bankConfigBuilder = new BankConfigBuilder.Builder().setConnectTimes(60)
                                .setRetryTimes(3).setBankCacert(mCacert).build();
                    }
                    HttpConnetionHelper.setConfig(bankConfigBuilder);
                }
            }
        }
    };


    private void configAndroidLog4j() throws Exception {
        final LogConfigurator logConfigurator = new LogConfigurator();
        File file = new File(LOGPATH);
        if (!file.exists()) {
            if (file.getParentFile().mkdirs()) {
                Log.d(TAG, "创建文件路径成功：" + file.getParent());
            } else {
                Log.e(TAG, "创建文件路径失败：" + file.getParent());
            }
            boolean flag = file.createNewFile();
            if (flag) {
                Log.d(TAG, "the log file create success");
            } else {
                throw new FileNotFoundException("the file " + LOGPATH + "not found");
            }
        }
        if (LOGPATH != null) {
            logConfigurator.setFileName(LOGPATH);
            logConfigurator.setRootLevel(getLogLevl(BankConfig.LOG_LEVEL));
            logConfigurator.setLevel("org.apache", getLogLevl(BankConfig.LOG_LEVEL));
            logConfigurator.setMaxFileSize(1024 * 1024 * 5); // 设置日志文件大小5M
            logConfigurator.setUseFileAppender(true);// 设置成true,即可输出到文件
            logConfigurator.configure();
        }
    }

    private Level getLogLevl(String level) {
        if (level == null) {
            return Level.INFO;
        }
        if (level.equals("DEBUG")) {
            return Level.DEBUG;
        } else if (level.equals("INFO")) {
            return Level.INFO;
        } else if (level.equals("ERROR")) {
            return Level.ERROR;
        } else if (level.equals("WARN")) {
            return Level.WARN;
        } else {
            return Level.INFO;
        }
    }

}
