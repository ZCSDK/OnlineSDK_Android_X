package com.sobot.online.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.sobot.common.frame.http.callback.SobotResultCallBack;
import com.sobot.common.utils.SobotSPUtils;
import com.sobot.online.OnlineConstant;
import com.sobot.online.api.ZhiChiOnlineApi;
import com.sobot.online.api.ZhiChiOnlineApiFactory;
import com.sobot.online.model.ConversationConfigModelResult;

/**
 * @Description: 在线客服基类 BaseActivity
 * @Author: znw
 * @CreateDate: 2020/09/1 11:05
 * @Version: 1.0
 */
public class SobotOnlineBaseActivity extends SobotBaseActivity {

    public ZhiChiOnlineApi zhiChiApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (zhiChiApi == null) {
            synchronized (SobotOnlineBaseActivity.class) {
                if (zhiChiApi == null) {
                    zhiChiApi = ZhiChiOnlineApiFactory.createZhiChiApi(SobotOnlineBaseActivity.this);
                }
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewResId() {
        return 0;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    /**
     * 填充用户配置信息
     */
    public void fillUserConfig() {
        zhiChiApi.conversationConfig(this, new SobotResultCallBack<ConversationConfigModelResult.ConversationConfigModel>() {
            @Override
            public void onSuccess(ConversationConfigModelResult.ConversationConfigModel conversationConfigModel) {

                SobotSPUtils.getInstance().put(OnlineConstant
                        .SCAN_PATH_FLAG, conversationConfigModel.getScanPathFlag() == 1);
                SobotSPUtils.getInstance().put(OnlineConstant
                        .ISINVITE_FLAG, conversationConfigModel.getIsInvite() == 0);

                //服务总结
                SobotSPUtils.getInstance().put(OnlineConstant
                        .OPEN_SUMMARY_FLAG, conversationConfigModel.getOpenSummaryFlag() == 1);
                SobotSPUtils.getInstance().put(OnlineConstant
                        .SUMMARY_OPERATION_FLAG, conversationConfigModel.getSummaryOperationFlag() == 1);
                SobotSPUtils.getInstance().put(OnlineConstant
                        .SUMMARY_OPERATION_INPUT_FLAG, conversationConfigModel.getSummaryOperationInputFlag() == 1);
                SobotSPUtils.getInstance().put(OnlineConstant
                        .SUMMARY_TYPE_FLAG, conversationConfigModel.getSummaryTypeFlag() == 1);
                SobotSPUtils.getInstance().put(OnlineConstant
                        .SUMMARY_STATUS_FLAG, conversationConfigModel.getSummaryStatusFlag() == 1);
                SobotSPUtils.getInstance().put(OnlineConstant
                        .SUMMARY_STATUS_INPUT_FLAG, conversationConfigModel.getSummaryStatusInputFlag() == 1);
                SobotSPUtils.getInstance().put(OnlineConstant
                        .QDESCRIBE_SHOW_FLAG, conversationConfigModel.getQDescribeShowFlag() == 1);
            }

            @Override
            public void onFailure(Exception e, String des) {

            }
        });
    }
}
