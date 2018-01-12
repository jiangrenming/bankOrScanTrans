package com.nld.starpos.banktrade.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.centerm.iso8583.IsoMessage;
import com.centerm.iso8583.MessageFactory;
import com.centerm.iso8583.bean.FormatInfo;
import com.centerm.iso8583.bean.FormatInfoFactory;
import com.centerm.iso8583.enums.IsoMessageMode;
import com.centerm.iso8583.parse.IsoConfigParser;
import com.centerm.iso8583.util.DataConverter;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.pinUtils.PinpadDev;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import common.HexUtil;
import common.StringUtil;

public class NldPacketHandle {

    private final String FILENAME = "signInfo";        // 文件名称
    private Context context = null;
    private static final String filePath_unionpay = "conf/unionpay_mct.xml";//直连配置
    private static String filePath = "conf/unionpay_mct.xml";
    public NldPacketHandle(Context context) throws Exception {
        this.context = context;
    }
    /**
     * 去除两字节报文长度
     *
     * @param message
     * @return
     */
    public byte[] subMessLen(byte[] message) {
        byte[] msg = new byte[message.length - 2];
        System.arraycopy(message, 2, msg, 0, msg.length);
        return msg;
    }

    /**
     * 添加两字节 报文长度
     *
     * @param message
     * @return
     */
    public byte[] addMessageLen(byte[] message) {
        int iLen = message.length;
        byte[] targets = new byte[]{(byte) (iLen / 256), (byte) (iLen % 256)};
        byte[] msg = new byte[iLen + 2];

        System.arraycopy(targets, 0, msg, 0, 2); //拷贝长度
        System.arraycopy(message, 0, msg, 2, iLen); //拷贝报文
        return msg;
    }


    /**
     * 获取是否已经签到标识
     *
     * @return
     */
    public boolean getSignSymbol() {
        boolean flag = false;
        SharedPreferences share = this.context.getSharedPreferences(FILENAME, 0);
        flag = share.getBoolean("signSymbol", false);
        return flag;
    }

    /**
     * 功能描述：用于进行8583解包操作，将解析的报文转换为json字符串
     *
     * @param message 需要进行解析的报文内容
     * @return 8583json形式
     * @throws Exception
     */
    private Map<String, String> unPack(byte[] message, String transCode) throws Exception {
        IsoConfigParser xmlParser = new IsoConfigParser();
        FormatInfoFactory formatInfoFactory = null;
        System.out.print("解包后的数据： " + DataConverter.bytesToHexStringForPrint(message));
        try {
            //FileInputStream fis = new FileInputStream(context.getResources().getString(R.string.baseSystemUri)+filePath);  //add by xrh @20130710
            //formatInfoFactory = xmlParser.parseFromInputStream(fis);//add by xrh @20130710
            formatInfoFactory = xmlParser.parseFromInputStream(this.context.getAssets().open(filePath));//note by xrh @20130710
        } catch (IOException e) {
           LogUtils.d("解包过程中解析mct.xml文件出错" + e.toString());
        }
        FormatInfo formatInfo = formatInfoFactory.getFormatInfo(transCode, IsoMessageMode.UNPACK);        //获取解包配置
        Map<String, String> map = MessageFactory.getIso8583Message().unPackTrns(message, formatInfo);        //进行解包
        LogUtils.d("解析的报文为：" + map.toString());
        return map;
    }

    /**
     * 功能描述：用于进行8583解包操作，将解析的报文转换为json字符串
     *
     * @param message 需要进行解析的报文内容
     * @param type    0为拉卡拉配置 1为银联配置
     * @return 8583json形式
     * @throws Exception
     */
    public Map<String, String> unPack(byte[] message, String transCode, int type) throws Exception {
        initMctType(type);
        return unPack(message, transCode);
    }

    /**
     * 功能描述：组包，根据传入的map格式的数据源和交易码组包
     * @param transCode 交易码，比如余额查询为“T100001”
     * @param type 代表不同的银联配置文件
     * @return
     * @throws Exception
     * @throws FileNotFoundException
     */
    public byte[] pack(AidlDeviceService deviceService, String transCode, Map<String, String> map, int type) throws Exception {
        initMctType(type);
        return pack(deviceService, transCode, map);
    }

    /**
     * 初始化银联配置
     *
     * @param type
     */
    private void initMctType(int type) {
        switch (type) {
            case 1:
                filePath = filePath_unionpay;
                break;

            default:
                break;
        }
    }

    /**
     * 功能描述：组包，根据传入的map格式的数据源和交易码组包
     * @param transCode 交易码，比如余额查询为“T100001”
     * @return
     * @throws Exception
     * @throws FileNotFoundException
     */
    private byte[] pack(AidlDeviceService deviceService, String transCode, Map<String, String> map) throws Exception {

        byte[] messageData = null;
        FormatInfoFactory formatInfoFactory = null;
        IsoConfigParser xmlParser = new IsoConfigParser();
        try {
            formatInfoFactory = xmlParser.parseFromInputStream(this.context.getAssets().open(filePath));
        } catch (IOException e) {
            LogUtils.d("组包过程中解析mct.xml文件模块出错" + e.toString());
        }
        FormatInfo formatInfo = formatInfoFactory.getFormatInfo(transCode, IsoMessageMode.PACK);        //根据交易码获取对应的报文格式控制对象
        IsoMessage message = null;
        try {
            message = MessageFactory.getIso8583Message().packTrns(map, formatInfo);
        } catch (Exception e) {
            LogUtils.d("组包过程出错!" + e.getMessage());
            throw new Exception("上送报文错误!");
        }
        LogUtils.d("发送的数据： "+message+"/转换后的数据="+DataConverter.bytesToHexStringForPrint(message.getAllMessageByteData()));
        messageData = message.getAllMessageByteData();        //按照字节数组获取报文内容
        if (map.get("udf_fld") != null && !map.get("udf_fld").startsWith("00")) { //不为管理类交易，需要计算MAC
            Log.i("LogActivity", "需要MAC计算");
            byte mkeyid = getMkeyId(context);
            byte[] macInfo = null;
            String macFilterStr = formatInfoFactory.getMabInfo(transCode, IsoMessageMode.PACK);        //获取mac过滤字符串
            String macBlock = message.getMacBlock(macFilterStr);    //获取macBlock
            macBlock = DataConverter.addZeroRightToMod16Equal0(macBlock);        //对macBlock进行补位操作，直到其长度对16取模为0
            LogUtils.d("待计算mac数据：" + macBlock);
            if ("0".equals(PinpadDev.getPinPadDevSymbol())) {
                PinpadDev dev = new PinpadDev(deviceService, 0);
                macInfo = dev.getMac(mkeyid, HexUtil.hexStringToByte(macBlock));
            } else {
                PinpadDev dev = new PinpadDev(deviceService, 1);
                macInfo = dev.getMac(mkeyid, HexUtil.hexStringToByte(macBlock));
            }
            if (null == macInfo) {
                LogUtils.d("mac计算结果：" + macInfo);
            } else {
                LogUtils.d("mac计算结果：" + DataConverter.bytesToHexString(macInfo));
            }
            byte[] messageDataTemp = message.getAllMessageByteData();        //按照字节数组获取报文内容
            messageData = new byte[messageDataTemp.length];        //重新构建一个字节数组用来存放更换mac后的报文信息
            System.arraycopy(messageDataTemp, 0, messageData, 0, messageDataTemp.length - 8);
            System.arraycopy(macInfo, 0, messageData, messageDataTemp.length - 8, 8);
        }
        return messageData;
    }

    public byte getMkeyId(Context context) {
        byte mkeyid = 0x01;
        ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl();
        String keystr = mParamConfigDao.get(Constant.FIELD_NEW_MAK_ID);
        if (StringUtil.isEmpty(keystr)) {
            return mkeyid;
        }
        mkeyid = (byte) Integer.parseInt(keystr);
        return mkeyid;
    }

    /**
     * 计算mac
     */
    private final int tpduLen = 11; //TPDU 压缩的字节长度
    public String getMac(AidlDeviceService deviceService, byte[] responce) {
        String ret = "";
        try {
            byte[] macBlock = new byte[responce.length - 8];
            System.arraycopy(responce, tpduLen, macBlock, 0, responce.length - 8 - tpduLen);
            byte[] macInfo = null;
            byte mkeyid = this.getMkeyId(context);
            String macBloacStr = DataConverter.bytesToHexString(macBlock);
            macBloacStr = DataConverter.addZeroRightToMod16Equal0(macBloacStr);
            byte[] macBlockByte = DataConverter.getStringXor(macBloacStr);
            if ("0".equals(PinpadDev.getPinPadDevSymbol())) {
                PinpadDev dev = new PinpadDev(deviceService, 0);
                macInfo = dev.getMac(mkeyid, HexUtil.hexStringToByte(macBloacStr));
            } else {
                PinpadDev dev = new PinpadDev(deviceService, 1);
                macInfo = dev.getMac(mkeyid, HexUtil.hexStringToByte(macBloacStr));
            }
            ret = DataConverter.bytesToHexString(macInfo);
        } catch (Exception e) {
            LogUtils.e("计算mac失败...", e);
            e.printStackTrace();
        }
        return ret;
    }
}
