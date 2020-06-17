package com.fenqile.delayqueue.dao.db.domain;

import java.util.Date;

public class DelayMessageError {
    private Integer index;

    private String msgId;

    private String message;

    private Integer excuteTimes;

    private String excuteState;

    private String errorMsg;

    private String errorCode;

    private Date lastExcuteTime;

    private Date manualExcuteTime;

    private String userNo;

    private String manualExcuteMemo;

    private Integer version;

    private Date createTime;

    private Date modifyTime;

    private String topic;

    private String bizType;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId == null ? null : msgId.trim();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }

    public Integer getExcuteTimes() {
        return excuteTimes;
    }

    public void setExcuteTimes(Integer excuteTimes) {
        this.excuteTimes = excuteTimes;
    }

    public String getExcuteState() {
        return excuteState;
    }

    public void setExcuteState(String excuteState) {
        this.excuteState = excuteState == null ? null : excuteState.trim();
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg == null ? null : errorMsg.trim();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode == null ? null : errorCode.trim();
    }

    public Date getLastExcuteTime() {
        return lastExcuteTime;
    }

    public void setLastExcuteTime(Date lastExcuteTime) {
        this.lastExcuteTime = lastExcuteTime;
    }

    public Date getManualExcuteTime() {
        return manualExcuteTime;
    }

    public void setManualExcuteTime(Date manualExcuteTime) {
        this.manualExcuteTime = manualExcuteTime;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo == null ? null : userNo.trim();
    }

    public String getManualExcuteMemo() {
        return manualExcuteMemo;
    }

    public void setManualExcuteMemo(String manualExcuteMemo) {
        this.manualExcuteMemo = manualExcuteMemo == null ? null : manualExcuteMemo.trim();
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic == null ? null : topic.trim();
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType == null ? null : bizType.trim();
    }
}