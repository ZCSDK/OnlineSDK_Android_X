package com.sobot.sobotappsdkdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;


import com.sobot.common.socket.SobotSocketConstant;
import com.sobot.common.utils.SobotActivityManager;
import com.sobot.common.utils.SobotAppUtils;
import com.sobot.common.utils.SobotLogUtils;
import com.sobot.common.utils.SobotSPUtils;
import com.sobot.online.SobotOnlineService;
import com.sobot.online.weight.SobotContainsEmojiEditText;
import com.sobot.online.weight.toast.SobotToastUtil;

public class MainActivity extends AppCompatActivity {
    private MyReceiver receiver;
    private SobotContainsEmojiEditText yuming;
    private SobotContainsEmojiEditText appid;
    private SobotContainsEmojiEditText appkey;
    private SobotContainsEmojiEditText account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv_start = findViewById(R.id.tv_start);
        appid = findViewById(R.id.appid);
        appkey = findViewById(R.id.appkey);
        account = findViewById(R.id.account);
        yuming = findViewById(R.id.yuming);
        final String yumingStr = SharedPreferencesUtil.getStringData(this, "yuming", "");
        if (!TextUtils.isEmpty(yumingStr)) {
            yuming.setText(yumingStr);
        }

        appid.setText(SobotSPUtils.getInstance().getString("appid"));
        appkey.setText(SobotSPUtils.getInstance().getString("appkey"));
        account.setText(SobotSPUtils.getInstance().getString("account"));
        tv_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(appid.getText().toString().trim())) {
                    SobotToastUtil.showCustomToast(MainActivity.this, "appid 不能为空");
                    return;
                }
                if (TextUtils.isEmpty(appkey.getText().toString().trim())) {
                    SobotToastUtil.showCustomToast(MainActivity.this, "appkey 不能为空");
                    return;
                }
                if (TextUtils.isEmpty(account.getText().toString().trim())) {
                    SobotToastUtil.showCustomToast(MainActivity.this, "客服账号 不能为空");
                    return;
                }
                if (!yumingStr.equals(yuming.getText().toString())) {
                    SharedPreferencesUtil.saveStringData(MainActivity.this, "yuming", yuming.getText().toString());
                    finish();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            System.exit(0);
                        }
                    }, 500);
                    return;
                }
                SobotOnlineService.startAuthWithAcount(MainActivity.this, appid.getText().toString().trim(),
                        appkey.getText().toString().trim(), account.getText().toString().trim(), 1);
                SobotSPUtils.getInstance().put("account", account.getText().toString().trim());
                SobotSPUtils.getInstance().put("appid", appid.getText().toString().trim());
                SobotSPUtils.getInstance().put("appkey", appkey.getText().toString().trim());
            }
        });
        //注册广播获取新收到的信息和未读消息数
        if (receiver == null) {
            receiver = new MyReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(SobotSocketConstant.BROADCAST_SOBOT_NEW_MSG);
        registerReceiver(receiver, filter);
    }

    //设置广播获取新收到的信息和未读消息数
    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (SobotSocketConstant.BROADCAST_SOBOT_NEW_MSG.equals(intent.getAction())) {
                //新消息内容
                String content = intent.getStringExtra("msgContent");
                //完整内容json
                String contentJson = intent.getStringExtra("msgContentJson");
                SobotLogUtils.i(" 新消息内容:" + content + "   完整内容:" + contentJson);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}
