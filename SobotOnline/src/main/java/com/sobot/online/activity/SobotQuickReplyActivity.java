package com.sobot.online.activity;

import android.view.View;
import android.widget.TextView;

import com.sobot.common.utils.SobotKeyboardUtils;
import com.sobot.common.utils.SobotSPUtils;
import com.sobot.online.adapter.SobotViewPagerAdapter;
import com.sobot.online.base.SobotBaseFragment;
import com.sobot.online.base.SobotOnlineBaseActivity;
import com.sobot.online.fragment.SobotMyReplyFragment;
import com.sobot.online.fragment.SobotPublicReplyFragment;
import com.sobot.online.model.CustomerServiceInfoModel;
import com.sobot.online.weight.SobotNoScrollViewPager;
import com.sobot.online.weight.SobotPagerSlidingTab;

import java.util.ArrayList;
import java.util.List;

import static com.sobot.online.OnlineConstant.SOBOT_CUSTOM_USER;

/**
 * @Description: 快捷回复主页面
 * @Author: znw
 * @CreateDate: 2020/10/11 16:05
 * @Version: 1.0
 */
public class SobotQuickReplyActivity extends SobotOnlineBaseActivity {
    private SobotNoScrollViewPager sobot_online_viewPager;
    private SobotViewPagerAdapter mAdapter;
    private SobotPagerSlidingTab sobot_online_tab_indicator;
    //头部返回按钮
    private TextView sobot_online_tab_back_tv;

    CustomerServiceInfoModel admin;

    private List<SobotBaseFragment> mFragments;
    private SobotMyReplyFragment myReplyFragment;
    private SobotPublicReplyFragment publicReplyFragment;

    @Override
    protected int getContentViewResId() {
        return getResLayoutId("sobot_activity_quick_reply");
    }

    @Override
    protected void initView() {
        admin = (CustomerServiceInfoModel) SobotSPUtils.getInstance().getObject(SOBOT_CUSTOM_USER);
        sobot_online_viewPager = findViewById(getResId("sobot_online_viewPager"));
        sobot_online_tab_indicator = findViewById(getResId("sobot_online_tab_indicator"));
        sobot_online_tab_back_tv = findViewById(getResId("sobot_online_tab_back_tv"));
        initViewPager();
        //头部左侧返回按钮点击返回
        if (sobot_online_tab_back_tv != null) {
            sobot_online_tab_back_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SobotKeyboardUtils.hideSoftInput(getSobotActivity());
                    finish();
                }
            });
        }
    }



    @Override
    protected void initData() {
    }

    private void initViewPager() {
        sobot_online_viewPager.setNoScroll(false);
        myReplyFragment = new SobotMyReplyFragment();
        publicReplyFragment = new SobotPublicReplyFragment();
        mFragments = new ArrayList<SobotBaseFragment>();
        mFragments.clear();
        if (myReplyFragment != null) {
            mFragments.add(myReplyFragment);
        }
        if (publicReplyFragment != null) {
            mFragments.add(publicReplyFragment);
        }
        mAdapter = new SobotViewPagerAdapter(getSobotContext(), getSupportFragmentManager(), new String[]{getResString("online_my"), getResString("online_enterprise")}, mFragments);
        sobot_online_viewPager.setAdapter(mAdapter);
        sobot_online_tab_indicator.setViewPager(sobot_online_viewPager);
    }
}
