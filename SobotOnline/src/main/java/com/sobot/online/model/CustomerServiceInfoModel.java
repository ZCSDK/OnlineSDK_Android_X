package com.sobot.online.model;


import com.sobot.common.gson.annotations.Expose;
import com.sobot.common.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 客服信息实例
 */
public class CustomerServiceInfoModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private String tempId;    //客服temp-id
    private String nickName;    //客服昵称
    private String staffName;    //客服名称
    private int maxServiceCount;    //最大接待量
    private int blackFunction = 1;    //是否有拉黑权限   0 没有  1  有
    private int transferFunction = 1;    //是否有转接权限   0 没有  1  有
    private String face; //头像url
    /**
     * 管理员状态,0 离线 1人工在线；2人工忙碌；3人工小休；4人工培训；人工会议；人工用餐；7人工活动
     */
    private int status;
    private String companyId;
    private String rongyunId;
    private String appKey;
    private String sid;
    private String pu;
    private String puid;
    private String token;
    private int imFlag;//0是工单客服  1是在线客服
    private int cusRoleId;//身份id  3333：超级管理员；1111：在线客服；2222：管理员
    private String cusRoleName;//身份名称
    private String aid;//userid
    private int topFlag;//星标置顶 0不置顶 1置顶
    private int sortFlag;//会话排序 0 按接入顺序 1 按新消息时间
    private String centerNumber;//如果不为空，说明开通了呼叫功能

    @Expose
    @SerializedName("wslink.bak")
    private List<String> wslinkBak;//通道地址

    @Expose
    @SerializedName("wslink.default")
    private String wslinkDefault;//通道默认地址

    private List<CusRobotConfigModel> robots;

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public int getBlackFunction() {
        return blackFunction;
    }

    public void setBlackFunction(int blackFunction) {
        this.blackFunction = blackFunction;
    }

    public int getTransferFunction() {
        return transferFunction;
    }

    public void setTransferFunction(int transferFunction) {
        this.transferFunction = transferFunction;
    }

    public List<String> getWslinkBak() {
        return wslinkBak;
    }

    public void setWslinkBak(List<String> wslinkBak) {
        this.wslinkBak = wslinkBak;
    }

    public String getWslinkDefault() {
        return wslinkDefault;
    }

    public void setWslinkDefault(String wslinkDefault) {
        this.wslinkDefault = wslinkDefault;
    }

    public int getCusRoleId() {
        return cusRoleId;
    }

    public void setCusRoleId(int cusRoleId) {
        this.cusRoleId = cusRoleId;
    }

    public String getCusRoleName() {
        return cusRoleName;
    }

    public void setCusRoleName(String cusRoleName) {
        this.cusRoleName = cusRoleName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int isImFlag() {
        return imFlag;
    }

    public void setImFlag(int imFlag) {
        this.imFlag = imFlag;
    }

    public String getPu() {
        return pu;
    }

    public void setPu(String pu) {
        this.pu = pu;
    }

    public String getPuid() {
        return puid;
    }

    public void setPuid(String puid) {
        this.puid = puid;
    }

    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public int getMaxServiceCount() {
        return maxServiceCount;
    }

    public void setMaxServiceCount(int maxServiceCount) {
        this.maxServiceCount = maxServiceCount;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getRongyunId() {
        return rongyunId;
    }

    public void setRongyunId(String rongyunId) {
        this.rongyunId = rongyunId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public int getTopFlag() {
        return topFlag;
    }

    public void setTopFlag(int topFlag) {
        this.topFlag = topFlag;
    }

    public int getSortFlag() {
        return sortFlag;
    }

    public void setSortFlag(int sortFlag) {
        this.sortFlag = sortFlag;
    }

    public String getCenterNumber() {
        return centerNumber;
    }

    public void setCenterNumber(String centerNumber) {
        this.centerNumber = centerNumber;
    }

    public List<CusRobotConfigModel> getRobots() {
        return robots;
    }

    public void setRobots(List<CusRobotConfigModel> robots) {
        this.robots = robots;
    }

    @Override
    public String toString() {
        return "CustomerServiceInfoModel{" +
                "tempId='" + tempId + '\'' +
                ", nickName='" + nickName + '\'' +
                ", staffName='" + staffName + '\'' +
                ", maxServiceCount=" + maxServiceCount +
                ", blackFunction=" + blackFunction +
                ", transferFunction=" + transferFunction +
                ", face='" + face + '\'' +
                ", status=" + status +
                ", companyId='" + companyId + '\'' +
                ", rongyunId='" + rongyunId + '\'' +
                ", appKey='" + appKey + '\'' +
                ", sid='" + sid + '\'' +
                ", pu='" + pu + '\'' +
                ", puid='" + puid + '\'' +
                ", token='" + token + '\'' +
                ", imFlag=" + imFlag +
                ", cusRoleId=" + cusRoleId +
                ", cusRoleName='" + cusRoleName + '\'' +
                ", aid='" + aid + '\'' +
                ", topFlag=" + topFlag +
                ", sortFlag=" + sortFlag +
                ", centerNumber='" + centerNumber + '\'' +
                ", wslinkBak=" + wslinkBak +
                ", wslinkDefault='" + wslinkDefault + '\'' +
                ", robots=" + robots +
                '}';
    }
}