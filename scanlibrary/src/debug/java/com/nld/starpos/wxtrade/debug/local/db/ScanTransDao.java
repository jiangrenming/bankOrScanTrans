package com.nld.starpos.wxtrade.debug.local.db;


import com.nld.starpos.wxtrade.debug.exception.DuplicatedTraceException;
import com.nld.starpos.wxtrade.debug.local.db.bean.ScanTransRecord;

import java.util.List;

/**
 * Created by jiangrenming on 2017/9/25.
 * 扫码流水表
 */

public interface ScanTransDao {

    //添加一个新的流水
    public long addWater(ScanTransRecord water) throws DuplicatedTraceException;

    /**
     * 添加一个流水，但不检查重复流水。
     * @param water
     * @return
     */
    public long addWaterCanRepeat(ScanTransRecord water);

    /**
     * 更新一个流水
     *
     * @param water
     * @return
     */
    public int updateWater(ScanTransRecord water);

    /**
     * 根据主键进行删除记录
     * @param id 主键唯一ID
     * @return
     */
    public int deleteById(long id);

    /**
     * 根据交易码进行删除记录
     * @param transType
     * @return
     */
    public int deleteByTransType(int transType);

    /**
     * 根据流水号进行删除记录
     *
     * @param trace 流水号
     * @return
     */
    public int deleteByTrace(String trace);

    /**
     * 根据POS流水号查找记录
     *
     * @param trace POS流水号
     * @return
     */
    public ScanTransRecord findByTrace(String trace);

    /**
     * 根据第三方订单号查找记录
     *
     * @param thirdOrderNo
     * @return
     */
    public ScanTransRecord findByThirdOrderNo(String thirdOrderNo);

    /**
     * 根据参考号查找记录
     * @param referNum 参考号
     * @return
     */
    public ScanTransRecord findByReferNum(String referNum);

    /**
     * 根据索引查找流水记录
     * @param
     * @return
     */
    public ScanTransRecord findById(long id);

    /**
     * 获取最后一条流水记录
     *
     * @return
     */
    public ScanTransRecord findLastWater();

    /**
     * 获取流水数量
     *
     * @return
     */
    public int getWaterCount();

    /**
     * 翻页查询
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    public List<ScanTransRecord> findByPage(int pageNo, int pageSize);


    /**
     * 通过流水号模糊查询流水对象
     *
     * @param :pageNo
     * @param :pageSize
     * @return
     */
    public List<ScanTransRecord> findAllLikeTrace(String trace);

    /**
     * 查找所有流水
     */
    public List<ScanTransRecord> findAll();

    /**
     * 根据交易类型和交易状态获取流水
     *
     * @param transType
     * @param transStatus
     * @return
     */
    public List<ScanTransRecord> findByTransTypeAndTransStatus(int transType, int transStatus);

    /**
     * 根据交易类型获取流水
     *
     * @param transType
     * @param :transStatus
     * @return
     */

    public List<ScanTransRecord> findByTransType(int transType);

    /**
     * 根据交易订单号获取流水
     *
     * @param :transType
     * @param :transStatus
     * @return
     */

    public List<ScanTransRecord> findByOrderNo(String orderNo);

    /**
     * 根据交易订单号获取流水
     *
     * @param :transType
     * @param :transStatus
     * @return
     */

    public List<ScanTransRecord> findByOrderNoAndType(String orderNo, int type);

    /**
     * 清空扫码交易流水
     *
     * @return
     */
    public int clearScanWater();

    /**
     * 重置流水
     */
    public void clearWater();

    /**
     * 批量复制流水
     */
    public  long copyOldWater(List<ScanTransRecord> scanTransRecords);


}
