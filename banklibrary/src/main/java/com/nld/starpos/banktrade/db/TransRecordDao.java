package com.nld.starpos.banktrade.db;


import com.nld.starpos.banktrade.db.bean.TransRecord;

import java.util.List;

/**
 * 访问交易记录 的DAO层
 * 作者：cxy
 * 时间：2013.03.04
 */
public interface TransRecordDao {
    /**
     * 获取交易记录
     */
    public List<TransRecord> getEntities();
    /**
     * 获取未批上送成功记录
     */
    public List<TransRecord> getICUnBatchSendSucc();

    /**
     * 获取第几条数据
     *
     * @param pageNum
     * @return
     */
    public TransRecord getTransRecordByPage(int pageNum);

    /**
     * 获取最后一笔交易记录
     *
     * @return
     */
    public TransRecord getLastTransRecord();

    /**
     * 根据批次号、票据号获取唯一一笔交易记录
     *
     * @param batchno 批次号
     * @param billno  流水号
     * @return
     */
    public TransRecord getTransRecordByCondition(String batchno, String billno);

    public TransRecord getTransRecordByCondition(String batchnoBillno);


    public TransRecord getConsumeByCondition(String batchno, String billno);

    public TransRecord getTransRevokeByCondition(String batchno, String billno);

    public TransRecord getTransAuthRevokeByCondition(String transprocode,
                                                     String conditionmode, String idrespcode, String reserve1);

    /**
     * 根据处理代码、POS流水号、终端号、商户号
     *
     * @param transprocode 处理码
     * @param systraceno   流水号
     * @return
     */
    public TransRecord getTransRecordByCondition(String transprocode, String systraceno, String terminalid, String acceptoridcode);

    /**
     * 根据状态码查找记录
     *
     * @param code
     * @return
     */
    public List<TransRecord> getTransRecordsByStatuscode(String code);

    /**
     * 保存交易记录
     */
    public int save(TransRecord record);

    /**
     * 更新交易记录
     */
    public int update(TransRecord record);

    public int update(int id, String key, String value);

    //    public int updatePart(String jsonStr,String batchNo,String sysTraNo);
    int updateByNo(int transState, String batchbillno);

    /**
     * 删除交易记录
     */
    public int delete(TransRecord record);

    public int deleteAll();

    public int getTransCount();

    public int getConsumeCount();

    public int getRevokeCount();

    public int getOfflineSaleCount();

    //获取结算
    public TransRecord getSettle();

    public List<TransRecord> getCountTransRecord(String transprocode, String conditionmode, String reserve1);

    //根据不同类型的交易方式获取不同的交易数据
    public List<TransRecord> getTransRecordByType(String type);

    //根据不同的交易类型获取数量
    public String getTransCountByType(String type);

    //根据key value 获取数量
    public String getTransCountByKV(String key, String value);

    // 根据不同的交易类型获取总金额
    public String getTransAmountByType(String type);

    //获取批上送成功条目
    public String getBatchSendSucCount();

    //获取磁条卡未批上送entity
    public List<TransRecord> getUnBatchMagcardList();
}
