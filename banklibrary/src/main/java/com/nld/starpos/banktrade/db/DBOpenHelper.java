package com.nld.starpos.banktrade.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.utils.BankConfig;
import com.nld.starpos.banktrade.utils.CommonUtil;
import com.nld.starpos.banktrade.utils.Constant;

/**
 * SQLite数据的访问帮助器
 * 作者：cxy
 * 时间：2013.02.04
 * @author jiangrenming
 * @author jiangrenming
 */
public class DBOpenHelper extends SQLiteOpenHelper {
	
    private SQLiteDatabase mWritableDatabase = null;
    private SQLiteDatabase mReadableDatabase = null;
    
//    public  SQLiteDatabase getReadableDatabase() {
//        if(mReadableDatabase==null){
//            mReadableDatabase = super.getReadableDatabase("123456");
//        }
//        return mReadableDatabase;
//    }
//
//    public  SQLiteDatabase getWritableDatabase() {
//        if(mWritableDatabase==null){
//            mWritableDatabase = super.getWritableDatabase("123456");
//        }
//        return mWritableDatabase;
//    }

    private static final String DBNAME = "localpos.db";//数据库名称
    /**
	 * 数据库版本号,默认从1开始，IC卡开发时升级为2，需要数据库更新时，更改此值。
	 */
    private static final int VERSION = 1;//数据库版本
    private static DBOpenHelper helper;//数据库帮助器实例

    public  static synchronized DBOpenHelper getInstance() {
    	if (helper == null) {
    		helper = new DBOpenHelper();
    	}
    	return helper;
    }
    
    public DBOpenHelper() {
    	super(HttpConnetionHelper.getmContext(), DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	LogUtils.d("创建数据库表开始");
    	try {
			bulidParamsTables(db); //参数表
			buildSriptTable(db); //脚本表
			buildReverseTable(db); //冲正表
			buildSettleTable(db); //结算表
			buildWaterTable(db); //流水表
			buildScanWaterTable(db);  //扫码流水表
			buildFailWaterTable(db); //失败流水表
            final int oldVersion = 1;
			onUpgrade(db, oldVersion, VERSION);
    	} catch (Exception e) {
    		e.printStackTrace();
    		LogUtils.e("创建数据库表异常", e);
    	}
    }

	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try{
			/*for (int i = oldVersion; i< newVersion; i++){
 				switch (i){
					case 1:
						String md5_key = getSQLColumData(db,"md5_key");
						String termid = getSQLColumData(db,"termid");
						String scan_account = getSQLColumData(db,"scan_account");
						String scan_merid = getSQLColumData(db, "scan_merid");
						String mchntname = getSQLColumData(db, "mchntname");
						String shop_id = getSQLColumData(db,"shop_id");
						String scan_batchno = getSQLColumData(db,"scan_batchno");
						String scan_systraceno = getSQLColumData(db,"scan_systraceno");
						LogUtils.i("旧数据库数据:"+md5_key+"/termid="+termid+"/scan_account="+scan_account+"/scan_merid="+scan_merid+"/mchntname="+mchntname+"/shop_id="+shop_id
						+"/scan_batchno="+scan_batchno+"/scan_systraceno="+scan_systraceno);
						//扫码参数复制
						ScanParamsDao scanParamsDao = new ScanParamsDaoImpl();
						Map<String,String> params = new HashMap<>();
						params.put(TransParamsValue.BindParamsContns.MD5_KEY,md5_key);
						params.put(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID,termid);
						params.put(TransParamsValue.BindParamsContns.PARAMS_KEY_QR_CODE_ACCOUNT,scan_account);
						params.put(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID,scan_merid);
						params.put(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME,mchntname);
						params.put(TransParamsValue.BindParamsContns.PARAMS_SHOP_ID,shop_id);
						params.put(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO,scan_batchno);
						params.put(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO,scan_systraceno);
						scanParamsDao.save(params);
                        LogUtils.i("复制参数表成功");
						saveNewScanData(db);

						break;
					case 2:

						break;
					default:
						break;
				}
			}*/
		}catch (Exception e){
			LogUtils.i("失败"+e.getMessage());
			e.printStackTrace();
		}
    }

	/**
	 * 获取某个字段的属性值
	 * @return
	 */
	private String getSQLColumData(SQLiteDatabase db ,String key){
		String value= "";
		Cursor cursor = null;
		try{
			cursor = db.query("param_config",new String[]{"tagval"},"tagname = ?",new String[]{key},null,null,null);
			if (null != cursor && cursor.getCount() >0 && cursor.moveToFirst()){
				value = cursor.getString(0);
				LogUtils.i("获取的值="+value);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return value;
	}

    /*private void saveNewScanData(SQLiteDatabase db){
		ScanTransDao scanTransDao = new ScanTransDaoImp();
		List<ScanTransRecord> scanTransRecords = null;
		Cursor cursor= null;
		try{
			scanTransRecords = new ArrayList<>();
			cursor = db.rawQuery("select * from ScanTrans",null);
			if (null != cursor && cursor.getCount() > 0){
				while (cursor.moveToNext()) {
					ScanTransRecord scanTransRecord = new ScanTransRecord();
					scanTransRecord.setId(cursor.getInt(0));
					scanTransRecord.setTransamount(cursor.getString(1));
					scanTransRecord.setSystraceno(cursor.getString(2));
					scanTransRecord.setLogNo(cursor.getString(3));
					scanTransRecord.setMember(cursor.getString(4));
					scanTransRecord.setPayType(cursor.getString(5));
					scanTransRecord.setScanTime(cursor.getString(6));
					scanTransRecord.setScanDate(cursor.getString(7));
					scanTransRecord.setSettledata(cursor.getString(8));
					scanTransRecord.setTerminalId(cursor.getString(9));
					scanTransRecord.setMemberId(cursor.getString(10));
					scanTransRecord.setMemberName(cursor.getString(11));
					scanTransRecord.setBatchbillno(cursor.getString(12));
					scanTransRecord.setStatuscode(cursor.getString(13));
					scanTransRecord.setOper(cursor.getString(14));
					scanTransRecord.setAdddataword(cursor.getString(15));
					scanTransRecord.setTranscurrcode(cursor.getString(16));
					scanTransRecord.setPayChannel(cursor.getString(17));
					scanTransRecord.setOrderNo(cursor.getString(18));
					scanTransRecord.setRespcode(cursor.getString(19));
					scanTransRecord.setTransprocode(cursor.getString(20));
					scanTransRecord.setTotalAmount(cursor.getString(21));
					scanTransRecord.setAuthCode(cursor.getString(22));
					scanTransRecord.setIsrevoke(cursor.getString(23));
					scanTransRecord.setOldTransType(cursor.getInt(24));
					scanTransRecord.setTransType(cursor.getInt(25));
					scanTransRecords.add(scanTransRecord);
				}
				scanTransDao.copyOldWater(scanTransRecords);
			}
            LogUtils.i("复制流水表成功");
		}catch (Exception e){
			e.printStackTrace();
			LogUtils.e("获取老流水表数据异常");
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}*/


    //数据库降级
//	@Override
//	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		try{
//			db.execSQL("DROP TABLE IF EXISTS param_config");
//			db.execSQL("DROP TABLE IF EXISTS scriptnotity");
//			db.execSQL("DROP TABLE IF EXISTS reverse");
//			db.execSQL("DROP TABLE IF EXISTS settledata");
//			db.execSQL("DROP TABLE IF EXISTS transrecord");
//			db.execSQL("DROP TABLE IF EXISTS emvfailwater");
//			db.execSQL("DROP TABLE IF EXISTS wxtransrecord");
//			db.execSQL("DROP TABLE IF EXISTS wxsettledata");
//			onCreate(db);
//		}catch (Exception e){
//			Log.i("TAG","数据库降级建表失败...");
//			e.printStackTrace();
//		}
//	}

    @Override
	public String getDatabaseName() {
        return DBNAME;
    }

	/**
	 * 建表<参数表，冲正表，交易表，脚本表，流水表,失败流水表></>
	 * @param db
	 */
	public void bulidParamsTables(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS param_config("
				+"tagname VARCHAR(32) PRIMARY KEY,"
				+"tagval VARCHAR(128))");
		initTablesData(db);
	}

	private void buildFailWaterTable(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS emvfailwater("
				+"id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+"priaccount VARCHAR(19),"  //主账号
				+"oper VARCHAR(10),"     //操作员
				+"transprocode VARCHAR(6),"  //交易处理码
				+"transamount  VARCHAR(12),"  //交易金额
				+"systraceno VARCHAR(6),"     //POS流水号
				+"translocaltime VARCHAR(6),"  //交易时间
				+"translocaldate VARCHAR(4),"  //交易日期
				+"translocalyear VARCHAR(10),"    //操作年份
				+"expireddate VARCHAR(4),"     //卡有效期
				+"entrymode VARCHAR(3),"       //POS输入方式码
				+"seqnumber VARCHAR(3),"        //卡序列号
				+"conditionmode VARCHAR(2),"   //服务条件码
				+"updatecode VARCHAR(2),"      //更新标识码
				+"track2data VARCHAR(38),"     //2磁道数据
				+"track3data VARCHAR(104),"    //3磁道数据
				+"refernumber VARCHAR(12),"    //系统参考号
				+"idrespcode VARCHAR(6),"      //授权码
				+"respcode VARCHAR(2),"        //返回码
				+"terminalid VARCHAR(8),"     //终端号
				+"acceptoridcode VARCHAR(15),"  //商户号
				+"acceptoridname VARCHAR(40),"  //商户名称
				+"addrespkey VARCHAR(64),"      //附加响应-密钥数据
				+"adddataword VARCHAR(512),"    //附加数据-文字信息
				+"transcurrcode VARCHAR(3),"    //交易货币代码
				+"pindata VARCHAR(12),"         //个人密码PIN
				+"secctrlinfo VARCHAR(16),"     //安全控制信息
				+"balanceamount VARCHAR(26),"   //附加金额
				+"icdata VARCHAR(255),"        //IC卡数据域
				+"adddatapri VARCHAR(100),"    //附件数据-私有
				+"pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
				+"loadparams VARCHAR(100),"    //参数下装信息
				+"cardholderid VARCHAR(18),"   //持卡人身份证
				+"batchbillno VARCHAR(12),"    //批次号票据号
				+"settledata VARCHAR(126),"    //结算信息
				+"mesauthcode VARCHAR(8),"     //消息认证码
				+"statuscode VARCHAR(2),"     //交易结果状态码
				+"reversetimes VARCHAR(2),"     //冲正次数
				+"reserve1 VARCHAR(100),"		//应答的消息类型
				+"reserve2 VARCHAR(100),"		//受理方标识码
				+"reserve3 VARCHAR(100),"		//TC、AAC、ARPC上送报文中F55
				+"reserve4 VARCHAR(100),"		//基于PBOC的交易中需要在备注中打印的数据
				+"reserve5 VARCHAR(100),"
				+"type VARCHAR(100))");       //失败流水类型
		Log.i("TAG","是否創建了失敗流水錶");
	}


	private void buildWaterTable(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS transrecord("
				+"id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+"priaccount VARCHAR(19),"  //主账号
				+"transprocode VARCHAR(6),"  //交易处理码
				+"transamount  VARCHAR(12),"  //交易金额
				+"systraceno VARCHAR(6),"     //POS流水号
				+"translocaltime VARCHAR(6),"  //交易时间
				+"translocaldate VARCHAR(4),"  //交易日期
				+"expireddate VARCHAR(4),"     //卡有效期
				+"entrymode VARCHAR(3),"       //POS输入方式码
				+"seqnumber VARCHAR(3),"        //卡序列号
				+"conditionmode VARCHAR(2),"   //服务条件码
				+"updatecode VARCHAR(2),"      //更新标识码
				+"track2data VARCHAR(38),"     //2磁道数据
				+"track3data VARCHAR(104),"    //3磁道数据
				+"refernumber VARCHAR(12),"    //系统参考号
				+"idrespcode VARCHAR(6),"      //授权码
				+"respcode VARCHAR(2),"        //返回码
				+"terminalid VARCHAR(8),"     //终端号
				+"acceptoridcode VARCHAR(15),"  //商户号
				+"acceptoridname VARCHAR(40),"  //商户名称
				+"addrespkey VARCHAR(64),"      //附加响应-密钥数据
				+"adddataword VARCHAR(512),"    //附加数据-文字信息
				+"transcurrcode VARCHAR(3),"    //交易货币代码
				+"pindata VARCHAR(12),"         //个人密码PIN
				+"secctrlinfo VARCHAR(16),"     //安全控制信息
				+"balanceamount VARCHAR(26),"   //附加金额
				+"icdata VARCHAR(255),"        //IC卡数据域
				+"adddatapri VARCHAR(100),"    //附件数据-私有
				+"pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
				+"loadparams VARCHAR(100),"    //参数下装信息
				+"cardholderid VARCHAR(18),"   //持卡人身份证
				+"batchbillno VARCHAR(12),"    //批次号票据号
				+"settledata VARCHAR(126),"    //结算信息
				+"mesauthcode VARCHAR(8),"     //消息认证码
				+"statuscode VARCHAR(2),"     //交易结果状态码
				+"reversetimes VARCHAR(2),"     //冲正次数
				+"reserve1 VARCHAR(100),"		//应答的消息类型
				+"reserve2 VARCHAR(100),"		//受理方标识码
				+"reserve3 VARCHAR(100),"		//TC、AAC、ARPC上送报文中F55
				+"reserve4 VARCHAR(100),"		//基于PBOC的交易中需要在备注中打印的数据
				+"reserve5 VARCHAR(100),"
				+"transType VARCHAR(20),"	//交易类型
				+"transState VARCHAR(2))");    //交易状态
	}

	//扫码流水表
	private void buildScanWaterTable(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS ScanTrans("
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
				+"type VARCHAR(100))");    //交易类型
	}

	private void buildSettleTable(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS settledata("
				+"id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+"priaccount VARCHAR(19),"  //主账号
				+"transprocode VARCHAR(6),"  //交易处理码
				+"transamount  VARCHAR(12),"  //交易金额
				+"systraceno VARCHAR(6),"     //POS流水号
				+"translocaltime VARCHAR(6),"  //交易时间
				+"translocaldate VARCHAR(4),"  //交易日期
				+"expireddate VARCHAR(4),"     //卡有效期
				+"entrymode VARCHAR(3),"       //POS输入方式码
				+"seqnumber VARCHAR(3),"        //卡序列号
				+"conditionmode VARCHAR(2),"   //服务条件码
				+"updatecode VARCHAR(2),"      //更新标识码
				+"track2data VARCHAR(38),"     //2磁道数据
				+"track3data VARCHAR(104),"    //3磁道数据
				+"refernumber VARCHAR(12),"    //系统参考号
				+"idrespcode VARCHAR(6),"      //授权码
				+"respcode VARCHAR(2),"        //返回码
				+"terminalid VARCHAR(8),"     //终端号
				+"acceptoridcode VARCHAR(15),"  //商户号
				+"acceptoridname VARCHAR(40),"  //商户名称
				+"addrespkey VARCHAR(64),"      //附加响应-密钥数据
				+"adddataword VARCHAR(512),"    //附加数据-文字信息
				+"transcurrcode VARCHAR(3),"    //交易货币代码
				+"pindata VARCHAR(12),"         //个人密码PIN
				+"secctrlinfo VARCHAR(16),"     //安全控制信息
				+"balanceamount VARCHAR(26),"   //附加金额
				+"icdata VARCHAR(255),"        //IC卡数据域
				+"adddatapri VARCHAR(100),"    //附件数据-私有
				+"pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
				+"loadparams VARCHAR(100),"    //参数下装信息
				+"cardholderid VARCHAR(18),"   //持卡人身份证
				+"batchbillno VARCHAR(12),"    //批次号票据号
				+"settledata VARCHAR(126),"    //结算信息
				+"mesauthcode VARCHAR(8),"     //消息认证码
				+"statuscode VARCHAR(2),"     //交易结果状态码
				+"reversetimes VARCHAR(2),"     //冲正次数
				+"reserve1 VARCHAR(100),"
				+"reserve2 VARCHAR(100),"
				+"reserve3 VARCHAR(100),"
				+"reserve4 VARCHAR(100),"
				+"reserve5 VARCHAR(100),"
				+"type VARCHAR(100))");      //结算的类型
	}

	public void buildReverseTable(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS reverse("
				+"id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+"priaccount VARCHAR(19),"  //主账号
				+"transprocode VARCHAR(6),"  //交易处理码
				+"transamount  VARCHAR(12),"  //交易金额
				+"systraceno VARCHAR(6),"     //POS流水号
				+"translocaltime VARCHAR(6),"  //交易时间
				+"translocaldate VARCHAR(4),"  //交易日期
				+"expireddate VARCHAR(4),"     //卡有效期
				+"entrymode VARCHAR(3),"       //POS输入方式码
				+"seqnumber VARCHAR(3),"        //卡序列号
				+"conditionmode VARCHAR(2),"   //服务条件码
				+"updatecode VARCHAR(2),"      //更新标识码
				+"track2data VARCHAR(38),"     //2磁道数据
				+"track3data VARCHAR(104),"    //3磁道数据
				+"refernumber VARCHAR(12),"    //系统参考号
				+"idrespcode VARCHAR(6),"      //授权码
				+"respcode VARCHAR(2),"        //返回码
				+"terminalid VARCHAR(8),"     //终端号
				+"acceptoridcode VARCHAR(15),"  //商户号
				+"acceptoridname VARCHAR(40),"  //商户名称
				+"addrespkey VARCHAR(64),"      //附加响应-密钥数据
				+"adddataword VARCHAR(512),"    //附加数据-文字信息
				+"transcurrcode VARCHAR(3),"    //交易货币代码
				+"pindata VARCHAR(12),"         //个人密码PIN
				+"secctrlinfo VARCHAR(16),"     //安全控制信息
				+"balanceamount VARCHAR(26),"   //附加金额
				+"icdata VARCHAR(255),"        //IC卡数据域
				+"adddatapri VARCHAR(100),"    //附件数据-私有
				+"pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
				+"loadparams VARCHAR(100),"    //参数下装信息
				+"cardholderid VARCHAR(18),"   //持卡人身份证
				+"batchbillno VARCHAR(12),"    //批次号票据号
				+"settledata VARCHAR(126),"    //结算信息
				+"mesauthcode VARCHAR(8),"     //消息认证码
				+"statuscode VARCHAR(2),"     //交易结果状态码
				+"reversetimes VARCHAR(2),"     //冲正次数
				+"reserve1 VARCHAR(100),"
				+"reserve2 VARCHAR(100),"	//受理方标识码
				+"reserve3 VARCHAR(100),"
				+"reserve4 VARCHAR(100),"
				+"reserve5 VARCHAR(100)," +
				"type VARCHAR(100))");
	}

	public  void buildSriptTable(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS scriptnotity("
				+"id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+"priaccount VARCHAR(19),"  //主账号
				+"transprocode VARCHAR(6),"  //交易处理码
				+"transamount  VARCHAR(12),"  //交易金额
				+"systraceno VARCHAR(6),"     //POS流水号
				+"translocaltime VARCHAR(6),"  //交易时间
				+"translocaldate VARCHAR(4),"  //交易日期
				+"expireddate VARCHAR(4),"     //卡有效期
				+"entrymode VARCHAR(3),"       //POS输入方式码
				+"seqnumber VARCHAR(3),"        //卡序列号
				+"conditionmode VARCHAR(2),"   //服务条件码
				+"updatecode VARCHAR(2),"      //更新标识码
				+"track2data VARCHAR(38),"     //2磁道数据
				+"track3data VARCHAR(104),"    //3磁道数据
				+"refernumber VARCHAR(12),"    //系统参考号
				+"idrespcode VARCHAR(6),"      //授权码
				+"respcode VARCHAR(2),"        //返回码
				+"terminalid VARCHAR(8),"     //终端号
				+"acceptoridcode VARCHAR(15),"  //商户号
				+"acceptoridname VARCHAR(40),"  //商户名称
				+"addrespkey VARCHAR(64),"      //附加响应-密钥数据
				+"adddataword VARCHAR(512),"    //附加数据-文字信息
				+"transcurrcode VARCHAR(3),"    //交易货币代码
				+"pindata VARCHAR(12),"         //个人密码PIN
				+"secctrlinfo VARCHAR(16),"     //安全控制信息
				+"balanceamount VARCHAR(26),"   //附加金额
				+"icdata VARCHAR(255),"        //IC卡数据域
				+"adddatapri VARCHAR(100),"    //附件数据-私有
				+"pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
				+"loadparams VARCHAR(100),"    //参数下装信息
				+"cardholderid VARCHAR(18),"   //持卡人身份证
				+"batchbillno VARCHAR(12),"    //批次号票据号
				+"settledata VARCHAR(126),"    //结算信息
				+"mesauthcode VARCHAR(8),"     //消息认证码
				+"statuscode VARCHAR(2),"     //交易结果状态码
				+"reversetimes VARCHAR(2),"     //冲正次数
				+"reserve1 VARCHAR(100),"
				+"reserve2 VARCHAR(100),"       //受理方标识码
				+"reserve3 VARCHAR(100),"
				+"reserve4 VARCHAR(100),"
				+"reserve5 VARCHAR(100)," +
				"type VARCHAR(100))");
	}

	private void initTablesData(SQLiteDatabase db) {
		try{
			db.beginTransaction();
			//绑定终端的相关信息
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('unionpay_card_account','')"); //银行结算账号
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('unionpay_merid','')"); //银联商户号
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('unionpay_termid','')"); //银联终端号
			//流水号与批次号<银行卡>
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('batchno','000020')");// 批次号
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('systraceno','000001')"); //pos流水号，范围1-999999
			//操作员
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('dowmloadparam','0')");//参数下载状态 0为签退(即结算)后未执行参数下载，1为签退后有执行参数下载
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('settle_account','0')");//批结对账标志
			db.execSQL("Replace INTO param_config(tagname,tagval) VALUES('caversion','00')");   //当前IC卡公钥版本
			db.execSQL("Replace INTO param_config(tagname,tagval) VALUES('paramversion','00')");    //当前IC卡参数版本
			db.execSQL("Replace INTO param_config(tagname,tagval) VALUES('updatastatus','0')"); //更新状态，“0”都不需更新，“1” 公钥需要更新， “2”参数需要更新 ，“4” 公钥和参数都需要更新，” “表示更新中断标志，签到后两者都更新”
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('signsymbol','0')"); //签到状态，“0”为未签到
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('pinpadType','0')"); 		//密码键盘类型   -- 0外置;1内置

            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('translocaldate','')"); //签到日期
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('translocaltime','')"); //签到时间

			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('input_mode','')"); //是否支持手动输入卡号
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('reversetimes','3')"); //冲正次数
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('dealtimeout','60')");//交易超时
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('phoneOne','')"); //电话1
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('phoneTwo','')"); //电话2
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('phoneThree','')"); //电话3
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('phoneManager','')"); //管理电话



			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('settlebatchno','')"); //结算批次号
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('respcode','')"); //结算结果
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('settlesymbol','0')"); //结算状态，“0”为非结算状态
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('mkeyidsymbol','1')"); //密钥是否改变标志
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('"+ Constant.FIELD_NEW_MKEY_ID+"','10')"); //新主密钥索引
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('"+ Constant.FIELD_NEW_PIK_ID+"','11')"); //PIK索引
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('"+ Constant.FIELD_NEW_TDK_ID+"','12')"); //TDK索引
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('"+ Constant.FIELD_NEW_MAK_ID+"','13')"); //MAK索引
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('oldmkeyid','1')"); //旧主密钥索引
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('"+ Constant.FIELD_WX_NEW_MKEY_ID+"','2')"); //WX新主密钥索引
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('"+ Constant.FIELD_WX_NEW_PIK_ID+"','2')"); //WXPIK索引
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('"+ Constant.FIELD_WX_NEW_TDK_ID+"','2')"); //WXTDK索引
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('"+ Constant.FIELD_WX_NEW_MAK_ID+"','2')"); //WXMAK索引
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('posparamsymbol','1')"); //第一次安装应用判断是否需弹出，未下发配置文件提示
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('billno','000001')");

			/**************************************一下参数不知是否有用，暂且不删除****************************************************/

			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('standbytimeout','60')");//待机超时,原生新添加记录
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('tpdu','6007180000')"); //TPDU
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('unionpay_tpdu','" + BankConfig.TPDU_UNIONPAY + "')"); //银联TPDU 6005010000
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('systracemax','500')"); //流水上限
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('neednopin','0')"); //非接免密 0为需要密码 1为不需要密码
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('nopinamount','300')"); //非接免密上限
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('printtimes','2')"); //打印联数
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('reconntimes','3')"); //重拨次数
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('redowntimes','3')");
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('operatorpwd','0000')");//操作员密码
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('operpswd','6EB77D55F6C6EBB5EDDF310EAB6AA724')"); //运维密码12369874
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxbillno','000001')");
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('enabled','0')");//是否激活
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('enabled2','0')");//是否确认激活
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('fid','YP')");// 厂商标识  统一使用YP
			//打印状态，“”为无打印数据状态；“strans”为交易数据未打印完成；“settle”为结算数据未打印完成
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('printsymbol','')");
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('requestSettleData','')"); //上送的结算信息
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('settledata','')"); //下发的结算信息
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('printAppVersionId','SD_"+ CommonUtil.getAppVersion()+"')"); //凭条打印收单版本号


			/*******************************接口路径的保存*****************************************************/
			String ipPortUnionPay= CommonUtil.getIpAndPortUnionPay();
			db.execSQL("Replace INTO param_config(tagname,tagval) VALUES('connect_mode','1')");//通讯模式0表示3g；1表示专网
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('unionpay_trans_url','https://"+ipPortUnionPay+"')"); //https 银联交易地址
			//--------------------------只在数据库中保存三个地址两套--start------------------------------------
			//--------------------------只在数据库中保存三个地址两套--end------------------------------------
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
