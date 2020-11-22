package com.sobot.online.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.sobot.common.frame.http.callback.SobotResultCallBack;
import com.sobot.common.socket.SobotSocketConstant;
import com.sobot.common.socket.channel.Const;
import com.sobot.common.utils.SobotActivityManager;
import com.sobot.common.utils.SobotLogUtils;
import com.sobot.common.utils.SobotMD5Utils;
import com.sobot.common.utils.SobotResourceUtils;
import com.sobot.common.utils.SobotSPUtils;
import com.sobot.common.utils.SobotUtils;
import com.sobot.online.OnlineConstant;
import com.sobot.online.base.SobotOnlineDialogBaseActivity;
import com.sobot.online.control.OnlineMsgManager;
import com.sobot.online.model.CustomerServiceInfoModel;
import com.sobot.online.model.OnlineTokenModel;
import com.sobot.online.weight.toast.SobotToastUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sobot.online.OnlineConstant.SOBOT_CUSTOM_USER;

//被下线界面
public class SobotOnlineExitActivity extends SobotOnlineDialogBaseActivity implements View.OnClickListener {

    private Button sobot_btn_cancle_conversation, sobot_btn_temporary_leave;
    private TextView sobot_tv_will_end_conversation;
    private String customKickStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //窗口对齐屏幕宽度
        Window win = this.getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.BOTTOM;
        win.setAttributes(lp);
    }

    @Override
    protected int getContentViewResId() {
        return SobotResourceUtils.getResLayoutId(this, "sobot_exit_popup");
    }


    @Override
    public void initView() {
        customKickStatus = getIntent().getStringExtra("customKickStatus");
        sobot_btn_cancle_conversation = (Button) findViewById(SobotResourceUtils.getIdByName(getContext(), "id", "sobot_btn_cancle_conversation"));
        sobot_btn_cancle_conversation.setText(SobotResourceUtils.getResString(getContext(), "onnline_exit"));
        sobot_btn_temporary_leave = (Button) findViewById(SobotResourceUtils.getIdByName(getContext(), "id", "sobot_btn_temporary_leave"));
        sobot_btn_temporary_leave.setText(SobotResourceUtils.getResString(getContext(), "onnline_relogin"));
        sobot_tv_will_end_conversation = (TextView) findViewById(SobotResourceUtils.getIdByName(getContext(), "id", "sobot_tv_will_end_conversation"));
        sobot_btn_cancle_conversation.setOnClickListener(this);
        sobot_btn_temporary_leave.setOnClickListener(this);

        if (SobotSocketConstant.push_custom_outline_kick.equals(customKickStatus)) {
            sobot_tv_will_end_conversation.setText(SobotResourceUtils.getResString(getContext(), "onnline_kicked"));
        } else if (SobotSocketConstant.push_custom_outline_kick_by_admin.equals(customKickStatus)) {
            sobot_tv_will_end_conversation.setText(SobotResourceUtils.getResString(getContext(), "onnline_kicked_by_admin"));
        } else {
            sobot_tv_will_end_conversation.setText(SobotResourceUtils.getResString(getContext(), "onnline_kicked"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void initData() {
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == SobotResourceUtils.getResId(getSobotContext(), "sobot_btn_cancle_conversation")) {
            //退出智齿
            SobotLogUtils.i("退出智齿");
            disconnChannel();
            //清除缓存用户信息
            OnlineMsgManager.getInstance(getSobotContext()).setCustomerServiceInfoModel(null);
            SobotSPUtils.getInstance().remove(SOBOT_CUSTOM_USER);
            //离线成功返回登录界面
            SobotActivityManager.getInstance().finishAllActivity();
        } else if (v.getId() == SobotResourceUtils.getResId(getSobotContext(), "sobot_btn_temporary_leave")) {
            //重新登录
            SobotLogUtils.i("重新登录");
            disconnChannel();
            getTokenAndLogin();
        }
    }

    public void getTokenAndLogin() {
        String appid = SobotSPUtils.getInstance().getString(OnlineConstant.ONLINE_APPID);
        String appkey = SobotSPUtils.getInstance().getString(OnlineConstant.ONLINE_APPKEY);

        final Map<String, String> tokebMap = new HashMap<>();
        String nowMills = System.currentTimeMillis() + "";
        tokebMap.put("appid", appid);
        tokebMap.put("create_time", nowMills);
        tokebMap.put("sign", SobotMD5Utils.getMD5Str(appid + nowMills + appkey));

        zhiChiApi.getToken(getSobotActivity(), tokebMap, new SobotResultCallBack<OnlineTokenModel>() {
            @Override
            public void onSuccess(OnlineTokenModel onlineTokenModel) {
                if (onlineTokenModel.getItem() != null && !TextUtils.isEmpty(onlineTokenModel.getRet_code()) && "000000".equals(onlineTokenModel.getRet_code())) {
                    String account = SobotSPUtils.getInstance().getString(OnlineConstant.SOBOT_CUSTOM_ACCOUNT);
                    login(account, 1, onlineTokenModel.getItem().getToken());
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                SobotToastUtil.showCustomToast(getSobotContext(), des);
            }
        });
    }

    public void login(String account, final int loginStatus, String loginToken) {
        if (TextUtils.isEmpty(account)) {
            SobotLogUtils.e("登录账号不能为空");
            return;
        }
        if (TextUtils.isEmpty(loginToken)) {
            SobotLogUtils.e("登录loginToken不能为空");
            return;
        }
        zhiChiApi.login(getSobotActivity(), account, loginStatus + "", loginToken, new SobotResultCallBack<CustomerServiceInfoModel>() {
            @Override
            public void onSuccess(CustomerServiceInfoModel user) {
                SobotSPUtils.getInstance().put(OnlineConstant.ONLINE_LOGIN_STATUS, loginStatus);
                SobotSPUtils.getInstance().put(SOBOT_CUSTOM_USER, user);
                SobotSPUtils.getInstance().put(OnlineConstant.KEY_TEMP_ID, user.getTempId());
                SobotSPUtils.getInstance().put(OnlineConstant.KEY_TOKEN, user.getToken());
                connChannel(user.getWslinkBak(), user.getWslinkDefault
                        (), user.getAid(), user.getCompanyId(), user.getPuid());
                SobotSPUtils.getInstance().put(SobotSocketConstant.WSLINK_BAK, user.getWslinkDefault());
                SobotSPUtils.getInstance().put(SobotSocketConstant.TOPFLAG, user.getTopFlag());
                SobotSPUtils.getInstance().put(SobotSocketConstant.SORTFLAG, user.getSortFlag());
                Intent intent = new Intent();
                intent.setAction(SobotSocketConstant.BROADCAST_SOBOT_LIST_SYNCHRONOUS_USERS);
                SobotUtils.getApp().sendBroadcast(intent);
                fillUserConfig();
                finish();
            }

            @Override
            public void onFailure(Exception e, String des) {
                SobotToastUtil.showCustomToast(getSobotContext(), des);
            }
        });
    }

    public void connChannel(List<String> wslinkBak, String wslinkDefault, String aid, String companyId, String puid) {
        if (!TextUtils.isEmpty(wslinkDefault) && !TextUtils.isEmpty(aid) && !TextUtils.isEmpty(companyId) && !TextUtils.isEmpty(puid)) {
            Intent intent = new Intent(Const.SOBOT_CUSTOME_CONNCHANNEL);
            if (wslinkBak != null && wslinkBak.size() > 0) {
                intent.putExtra("wslinkBak", wslinkBak.get(0));
            }
            intent.putExtra("wslinkDefault", wslinkDefault);
            intent.putExtra("companyId", companyId);
            intent.putExtra("aid", aid);
            intent.putExtra("puid", puid);
            intent.putExtra("userType", Const.user_type_customer);
            sendBroadcast(intent);
        }
    }

    /*************通道相关*********************/

    protected void disconnChannel() {
        sendBroadcast(new Intent(Const.SOBOT_CUSTOME_DISCONNCHANNEL));
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            //不执行父类点击事件
            return true;
        }
        //继续执行父类其他点击事件
        return super.onKeyUp(keyCode, event);
    }
}