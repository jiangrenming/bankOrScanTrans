package com.nld.starpos.banktrade.db;

import com.nld.starpos.banktrade.db.bean.EMVFailWater;

import java.util.List;

/**
 * Created by jiangrenming on 2017/10/14.
 * 失败流水表
 */

public interface EmvFailWaterDao {

    public long insert(EMVFailWater emvFailWater);

    public int update(EMVFailWater emvFailWater);

    public int deleteAll();

    public void revertSeq();

    public EMVFailWater findById(long id);

    /**
     * 获取失败流水数量
     * @return
     */
    public int getCount();

    /**
     * 返回所有流水
     * @return
     */
    public List<EMVFailWater> findAll();
}
