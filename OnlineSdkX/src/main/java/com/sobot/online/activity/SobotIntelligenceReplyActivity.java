package com.sobot.online.activity;

import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.sobot.online.adapter.SobotViewPagerAdapter;
import com.sobot.online.base.SobotBaseFragment;
import com.sobot.online.base.SobotOnlineBaseActivity;
import com.sobot.online.fragment.SobotInsideKnowledgeFragment;
import com.sobot.online.fragment.SobotRebotKnowledgeFragment;
import com.sobot.online.weight.SobotNoScrollViewPager;
import com.sobot.online.weight.SobotPagerSlidingTab;
import com.sobot.onlinecommon.control.CustomerServiceInfoModel;
import com.sobot.onlinecommon.utils.SobotResourceUtils;
import com.sobot.onlinecommon.utils.SobotSPUtils;

import java.util.ArrayList;
import java.util.List;

import static com.sobot.online.OnlineConstant.SOBOT_CUSTOM_USER;

/**
 * @Description: 智能回复主页面
 * @Author: znw
 * @CreateDate: 2020/10/12 16:05
 * @Version: 1.0
 */
public class SobotIntelligenceReplyActivity extends SobotOnlineBaseActivity {
    private SobotNoScrollViewPager sobot_online_viewPager;
    private SobotViewPagerAdapter mAdapter;
    private SobotPagerSlidingTab sobot_online_tab_indicator;
    private EditText et_online_search_content;
    CustomerServiceInfoModel admin;

    private List<SobotBaseFragment> mFragments;
    private SobotRebotKnowledgeFragment rebotKnowledgeFragment;
    private SobotInsideKnowledgeFragment insideKnowledgeFragment;

    @Override
    protected int getContentViewResId() {
        return getResLayoutId("sobot_activity_intelligence_reply");
    }

    @Override
    protected void initView() {
        setHearderTitle(SobotResourceUtils.getResString(getSobotContext(), "online_intelligence_reply"));
        admin = (CustomerServiceInfoModel) SobotSPUtils.getInstance().getObject(SOBOT_CUSTOM_USER);
        sobot_online_viewPager = findViewById(getResId("sobot_online_viewPager"));
        sobot_online_tab_indicator = findViewById(getResId("sobot_online_tab_indicator"));
        et_online_search_content = findViewById(getResId("et_online_search_content"));
        initViewPager();
    }


    @Override
    protected void initData() {
        et_online_search_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Fragment fragment = mFragments.get(sobot_online_viewPager.getCurrentItem());
                if (fragment instanceof SobotRebotKnowledgeFragment) {
                    ((SobotRebotKnowledgeFragment) fragment).searchReplyByKeyword(s.toString());
                }
                if (fragment instanceof SobotInsideKnowledgeFragment) {
                    ((SobotInsideKnowledgeFragment) fragment).innerInternalChat(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initViewPager() {
        sobot_online_viewPager.setNoScroll(false);
        rebotKnowledgeFragment = new SobotRebotKnowledgeFragment();
        insideKnowledgeFragment = new SobotInsideKnowledgeFragment();
        mFragments = new ArrayList<SobotBaseFragment>();
        mFragments.clear();
        if (rebotKnowledgeFragment != null) {
            mFragments.add(rebotKnowledgeFragment);
        }
        if (insideKnowledgeFragment != null) {
            mFragments.add(insideKnowledgeFragment);
        }
        mAdapter = new SobotViewPagerAdapter(getSobotContext(), getSupportFragmentManager(), new String[]{getResString("online_rebot_knowledge"), "  "+getResString("online_inside_knowledge")+"  "}, mFragments);
        sobot_online_viewPager.setAdapter(mAdapter);
        sobot_online_tab_indicator.setViewPager(sobot_online_viewPager);
    }
}
