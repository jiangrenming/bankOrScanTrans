package com.nld.starpos.wxtrade.debug.local;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite数据的访问帮助器
 * 作者：cxy
 * 时间：2013.02.04
 */
public class ScanDbHelper extends SQLiteOpenHelper {

    private static final String DBNAME = "scan_pay.db";//数据库名称
    private static final int VERSION = 1;//数据库版本
    private static ScanDbHelper helper;//数据库帮助器实例
    private static Context context;

	public static void initContext(Context context){
		ScanDbHelper.context = context;
	}
    public static synchronized ScanDbHelper getInstance() {
		if (context == null){
			throw new RuntimeException("扫码context未初始化");
		}
    	if (helper == null) {
    		helper = new ScanDbHelper(context);
    	}
    	return helper;
    }
    
    private ScanDbHelper(Context context) {
    	super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	try {
			bulidParamsTables(db); //参数表
			buildScanWaterTable(db);  //扫码流水表
            final int oldVersion = 1;
			onUpgrade(db, oldVersion, VERSION);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try{
			for (int i = oldVersion; i< newVersion; i++){
 				switch (i){
					case 1:

						break;
					case 2:

						break;
					default:
						break;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
    }

    //数据库降级
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try{
			db.execSQL("DROP TABLE IF EXISTS param_config");
			db.execSQL("DROP TABLE IF EXISTS scriptnotity");
			db.execSQL("DROP TABLE IF EXISTS reverse");
			db.execSQL("DROP TABLE IF EXISTS settledata");
			db.execSQL("DROP TABLE IF EXISTS transrecord");
			db.execSQL("DROP TABLE IF EXISTS emvfailwater");
			db.execSQL("DROP TABLE IF EXISTS wxtransrecord");
			db.execSQL("DROP TABLE IF EXISTS wxsettledata");
			onCreate(db);
		}catch (Exception e){
			Log.i("TAG","数据库降级建表失败...");
			e.printStackTrace();
		}
	}

    @Override
	public String getDatabaseName() {
        return DBNAME;
    }

	/**
	 * 建表<参数表，冲正表，交易表，脚本表，流水表,失败流水表></>
	 * @param db
	 */
	public void bulidParamsTables(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS Scan_params("
				+"tagname VARCHAR(32) PRIMARY KEY,"
				+"tagval VARCHAR(128))");
		initTablesData(db);
	}
	//扫码流水表
	private void buildScanWaterTable(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS Scan_Trans("
				+"id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+"transamount  VARCHAR(12),"  //交易金额
				+"systraceno VARCHAR(6),"     //pos交易流水号
				+"logNo VARCHAR(32),"     //系统流水号
				+"member VARCHAR(100),"     //会员信息
				+"payType VARCHAR(2),"     //支付类型
				+"translocaltime VARCHAR(6),"  //交易时间
				+"translocaldate VARCHAR(4),"  //交易日期
				+"settledata VARCHAR(126),"    //结算日期
				+"terminalid VARCHAR(8),"     //终端号
				+"acceptoridcode VARCHAR(15),"  //商户号
				+"acceptoridname VARCHAR(40),"  //商户名称
				+"batchbillno VARCHAR(12),"    //批次号
				+"statuscode VARCHAR(2),"     //交易结果状态码
				+"oper VARCHAR(10),"     //操作员
				+"adddataword VARCHAR(512),"    //附加数据-文字信息
				+"transcurrcode VARCHAR(3),"    //交易货币代码
				+"payChannel VARCHAR(20),"  //扫码交易渠道
				+"orderNo VARCHAR(20),"  //订单号
				+"respcode VARCHAR(2),"        //返回码
				+"transprocode VARCHAR(6),"  //交易处理码
				+"transtotalamount VARCHAR(6),"  //交易总金额
				+"authCode VARCHAR(6),"  //授权码
				+"isrevoke VARCHAR(6),"  //交易是否撤销
				+"oldType VARCHAR(10),"  //原交易类型
				+"type VARCHAR(100),"
                + "transyear VARCHAR(6))");    //交易年份
	}

	private void initTablesData(SQLiteDatabase db) {
		try{
			db.beginTransaction();
			//绑定终端的相关信息
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('md5_key','')"); //md5_key
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('termid','')"); //终端号
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('scan_account','')"); //二维账码结算号
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('scan_merid','')"); //扫码商户号
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('mchntname','')"); //商户名称
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('shop_id','')"); //门店号
			//流水号，批次号<扫码>
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('scan_batchno','000066')");// 批次号
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('scan_systraceno','000001')"); //pos流水号，范围1-999999
			//操作员
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('operatorcode','001')");//操作员号，默认
			//管理員密码
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('adminpwd','E10ADC3949BA59ABBE56E057F20F883E')");
			//打印设置
			db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES('scan_printer','1')"); //打印联张 <1代表1联，2代表2联>
			db.setTransactionSuccessful();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			db.endTransaction();
		}
	}

	/**
	 * 判断同个数据库中是否存在相同的列
	 */
	private static boolean checkColumnExist(SQLiteDatabase db, String tableName,String columnName){
		boolean result = false ;
		Cursor cursor = null ;
		try{
			//查询一行
			cursor = db.rawQuery( "SELECT * FROM " + tableName + " LIMIT 0", null );
			result = cursor != null && cursor.getColumnIndex(columnName) != -1 ;
		}catch (Exception e){
			Log.e("TAG","checkColumnExists1..." + e.getMessage()) ;
		}finally{
			if(null != cursor && !cursor.isClosed()){
				cursor.close() ;
			}
		}
		return result ;
	}
}
