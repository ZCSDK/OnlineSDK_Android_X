package com.sobot.online.model;

import java.io.Serializable;

//机器人信息实体类
public class CusRobotConfigModel implements Serializable {
    private String configId;
    private String companyId;
    private String robotName;
    private String robotLogo;
    private String robotAlias;  //机器人别名
    private Integer robotStatus;
    private Integer chinchinType;
    private Integer chinchinBaseType;
    private String updateUserId;
    private Long updateTime;
    private Integer guideFlag;
    private Integer robotFlag;
    private String robotRemark;
    private Integer robotFrom;    //来源 0在线 2套电

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public String getRobotLogo() {
        return robotLogo;
    }

    public void setRobotLogo(String robotLogo) {
        this.robotLogo = robotLogo;
    }

    public String getRobotAlias() {
        return robotAlias;
    }

    public void setRobotAlias(String robotAlias) {
        this.robotAlias = robotAlias;
    }

    public Integer getRobotStatus() {
        return robotStatus;
    }

    public void setRobotStatus(Integer robotStatus) {
        this.robotStatus = robotStatus;
    }

    public Integer getChinchinType() {
        return chinchinType;
    }

    public void setChinchinType(Integer chinchinType) {
        this.chinchinType = chinchinType;
    }

    public Integer getChinchinBaseType() {
        return chinchinBaseType;
    }

    public void setChinchinBaseType(Integer chinchinBaseType) {
        this.chinchinBaseType = chinchinBaseType;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getGuideFlag() {
        return guideFlag;
    }

    public void setGuideFlag(Integer guideFlag) {
        this.guideFlag = guideFlag;
    }

    public Integer getRobotFlag() {
        return robotFlag;
    }

    public void setRobotFlag(Integer robotFlag) {
        this.robotFlag = robotFlag;
    }

    public String getRobotRemark() {
        return robotRemark;
    }

    public void setRobotRemark(String robotRemark) {
        this.robotRemark = robotRemark;
    }


    public Integer getRobotFrom() {
        return robotFrom;
    }

    public void setRobotFrom(Integer robotFrom) {
        this.robotFrom = robotFrom;
    }

    @Override
    public String toString() {
        return "CusRobotConfigModel{" +
                "configId='" + configId + '\'' +
                ", companyId='" + companyId + '\'' +
                ", robotName='" + robotName + '\'' +
                ", robotLogo='" + robotLogo + '\'' +
                ", robotAlias='" + robotAlias + '\'' +
                ", robotStatus=" + robotStatus +
                ", chinchinType=" + chinchinType +
                ", chinchinBaseType=" + chinchinBaseType +
                ", updateUserId='" + updateUserId + '\'' +
                ", updateTime=" + updateTime +
                ", guideFlag=" + guideFlag +
                ", robotFlag=" + robotFlag +
                ", robotRemark='" + robotRemark + '\'' +
                ", robotFrom=" + robotFrom +
                '}';
    }
}
