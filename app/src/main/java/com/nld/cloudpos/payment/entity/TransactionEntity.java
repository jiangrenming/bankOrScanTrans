package com.nld.cloudpos.payment.entity;

import java.io.Serializable;

/**
 * 交易数据对象，用于保存每个交易节点产生的数据。
 * @author Administrator
 *
 */
public class TransactionEntity implements Serializable {

    private static final long serialVersionUID = 5390682994083594853L;

    public static final String TRANSACTION_CODE="transprocode";
    
    
    public String mBatchBillNo;//交易凭证号
    public double mTransMoney;//交易金额
    public String mCardno;//卡号
}
