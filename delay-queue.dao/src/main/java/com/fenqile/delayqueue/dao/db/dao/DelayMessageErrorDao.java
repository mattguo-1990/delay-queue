package com.fenqile.delayqueue.dao.db.dao;

import com.fenqile.dao.datasource.annotation.SwitchReadDB;
import com.fenqile.delayqueue.dao.db.domain.DelayMessageError;

import java.util.List;

public interface DelayMessageErrorDao {
    int deleteByPrimaryKey(Integer index);

    int insert(DelayMessageError record);

    int insertSelective(DelayMessageError record);

    DelayMessageError selectByPrimaryKey(Integer index);

    int updateByPrimaryKeySelective(DelayMessageError record);

    int updateByPrimaryKey(DelayMessageError record);

    List<DelayMessageError> selectByMsgId(String msgId);

    int updateByMsgId(DelayMessageError delayMessageError);
}