package com.sobot.online.control;

import android.content.Context;

import com.sobot.common.socket.module.PushMessageModel;
import com.sobot.online.model.CustomerServiceInfoModel;
import com.sobot.online.util.SobotCache;

import java.util.ArrayList;

/**
 * 消息管理类
 */
public class OnlineMsgManager {

    private static OnlineMsgManager instance;
    private Context mContext;
    private SobotCache mCache;

    private OnlineMsgManager(Context context) {
        mContext = context;
        mCache = SobotCache.get(context.getApplicationContext());
    }

    public static OnlineMsgManager getInstance(Context context) {
        if (instance == null) {
            instance = new OnlineMsgManager(context.getApplicationContext());
        }
        return instance;
    }

    //-----------------------------------------------------以下为会话保持所需变量-----------------------------------------------------
    //存储的临时变量
    private CustomerServiceInfoModel customerServiceInfoModel;

    public CustomerServiceInfoModel getCustomerServiceInfoModel() {
        return customerServiceInfoModel;
    }

    public void setCustomerServiceInfoModel(CustomerServiceInfoModel customerServiceInfoModel) {
        this.customerServiceInfoModel = customerServiceInfoModel;
    }

    public ArrayList<PushMessageModel> getTempMsgList() {
        return (ArrayList<PushMessageModel>) mCache.getAsObject("tempMsgList");
    }

    public void setTempMsgList(ArrayList<PushMessageModel> tempMsgList) {
        mCache.put("tempMsgList", tempMsgList);
    }
}
