package com.sobot.online.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.common.frame.http.callback.SobotResultCallBack;
import com.sobot.common.utils.SobotKeyboardUtils;
import com.sobot.common.utils.SobotResourceUtils;
import com.sobot.common.utils.SobotSPUtils;
import com.sobot.common.utils.SobotScreenUtils;
import com.sobot.common.utils.SobotSizeUtils;
import com.sobot.online.OnlineConstant;
import com.sobot.online.adapter.SobotPopRebotAdapter;
import com.sobot.online.adapter.SobotRebotKnoweledgeAdapter;
import com.sobot.online.api.OnlineBaseCode;
import com.sobot.online.base.SobotOnlineBaseFragment;
import com.sobot.online.model.ChatMessageRichTextModel;
import com.sobot.online.model.CusRobotConfigModel;
import com.sobot.online.model.CustomerServiceInfoModel;
import com.sobot.online.model.RebotSmartAnswerModel;
import com.sobot.online.weight.SobotCustomPopWindow;
import com.sobot.online.weight.emoji.SobotInputHelper;
import com.sobot.online.weight.recyclerview.SobotRecyclerView;
import com.sobot.online.weight.recyclerview.divider.HorizontalDividerItemDecoration;
import com.sobot.online.weight.recyclerview.swipemenu.SobotSwipeMenuRecyclerView;
import com.sobot.online.weight.toast.SobotToastUtil;

import java.util.ArrayList;
import java.util.List;

import static com.sobot.online.OnlineConstant.SOBOT_CUSTOM_USER;

/**
 * @Description: 智能回复 机器人知识库
 * @Author: znw
 * @CreateDate: 2020/10/12 14:37
 * @Version: 1.0
 */
public class SobotRebotKnowledgeFragment extends SobotOnlineBaseFragment implements SobotRecyclerView.LoadingListener, View.OnClickListener {
    private View mRootView;
    private SobotSwipeMenuRecyclerView ssmrv_online_rebot_knowledge;
    private TextView tv_online_select_rebot;
    private TextView tv_online_select_rebot_ids;
    private View v_online_split;
    private SobotRebotKnoweledgeAdapter rebotKnoweledgeAdapter;
    private List<RebotSmartAnswerModel> robotSmartAnswerModelList = new ArrayList<>();
    private SobotCustomPopWindow mPopWindow;//切换机器人弹窗
    private View mPopContentView;
    private RecyclerView rv_pop_knowledge_swith_rebot;
    private SobotPopRebotAdapter rebotAdapter;

    CustomerServiceInfoModel admin;//登录用户

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(getResLayoutId("sobot_fragment_rebot_knowledge"), container, false);
        initView();
        initData();
        return mRootView;
    }

    protected void initView() {
        admin = (CustomerServiceInfoModel) SobotSPUtils.getInstance().getObject(SOBOT_CUSTOM_USER);
        CusRobotConfigModel cusRobotConfigModel = new CusRobotConfigModel();
        cusRobotConfigModel.setRobotName(SobotResourceUtils.getResString(getSobotActivity(), "online_all_rebot"));
        cusRobotConfigModel.setRobotFlag(-100);
        //机器人列表第一项添加"全部机器人"
        admin.getRobots().add(0, cusRobotConfigModel);
        ssmrv_online_rebot_knowledge = mRootView.findViewById(getResId("ssmrv_online_rebot_knowledge"));
        tv_online_select_rebot = mRootView.findViewById(getResId("tv_online_select_rebot"));
        tv_online_select_rebot.setOnClickListener(this);
        tv_online_select_rebot_ids = mRootView.findViewById(getResId("tv_online_select_rebot_ids"));
        v_online_split = mRootView.findViewById(getResId("v_online_split"));
        LinearLayoutManager firstlayoutManager = new LinearLayoutManager(getSobotActivity());
        firstlayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ssmrv_online_rebot_knowledge.setLayoutManager(firstlayoutManager);
        ssmrv_online_rebot_knowledge.setPullRefreshEnabled(false);
        ssmrv_online_rebot_knowledge.setLoadingMoreEnabled(false);
        ssmrv_online_rebot_knowledge.setLoadingListener(this);
    }

    private void initData() {
        rebotKnoweledgeAdapter = new SobotRebotKnoweledgeAdapter(getSobotActivity(), robotSmartAnswerModelList, new SobotRebotKnoweledgeAdapter.OnSendReplyContentListener() {
            @Override
            public void onSendReplyContent(boolean isAutoSend, String sendContent, ArrayList<ChatMessageRichTextModel.ChatMessageRichListModel> richListModelList) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("richList", richListModelList);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.putExtra("isAutoSend", isAutoSend);
                intent.putExtra("sendContent", sendContent);
                intent.putExtra("sendType", OnlineConstant.SOBOT_MSG_TYPE_RICH);
                getSobotActivity().setResult(Activity.RESULT_OK, intent);
                getSobotActivity().finish();
            }
        });
        ssmrv_online_rebot_knowledge.setAdapter(rebotKnoweledgeAdapter);
        rebotKnoweledgeAdapter.setGroups(robotSmartAnswerModelList);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onClick(View v) {
        if (v == tv_online_select_rebot) {
            SobotKeyboardUtils.hideSoftInput(getSobotActivity());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getPopContentView();
                    if (mPopWindow == null) {
                        int[] location = new int[2];
                        v_online_split.getLocationInWindow(location);
                        int x = location[0];//获取当前位置的横坐标
                        int y = location[1];//获取当前位置的纵坐标
                        mPopWindow = new SobotCustomPopWindow.PopupWindowBuilder(getSobotActivity())
                                .setView(mPopContentView)
                                .setFocusable(false)
                                .size(SobotScreenUtils.getScreenWidth(), SobotScreenUtils.getScreenHeight() - y - v_online_split.getHeight())
                                .enableBackgroundDark(false)
                                .setOutsideTouchable(true)
                                .setWidthMatchParent(false)
                                .create();
                    }
                    if (mPopWindow != null && rebotAdapter != null && admin != null && admin.getRobots() != null) {
                        rebotAdapter.setListAll(admin.getRobots());
                        mPopWindow.showAsDropDown(v_online_split);
                    }
                }
            }, 100);
        }
    }


    //根据关键字模糊查询快捷回复内容列表
    public void searchReplyByKeyword(String keyword) {
        zhiChiApi.getSmartReplys(getSobotActivity(), keyword, "", new SobotResultCallBack<List<RebotSmartAnswerModel>>() {

            @Override
            public void onSuccess(List<RebotSmartAnswerModel> robotSmartAnswerModels) {
                if (robotSmartAnswerModels != null) {
                    rebotKnoweledgeAdapter.setGroups(robotSmartAnswerModels);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                SobotToastUtil.showCustomToast(getSobotActivity(), des);
            }
        });
    }

    //智能回复切换选择的机器人
    public void modifyAdminConfig() {
        zhiChiApi.modifyAdminConfig(getSobotActivity(), tv_online_select_rebot_ids.getText().toString(), new SobotResultCallBack<OnlineBaseCode>() {

            @Override
            public void onSuccess(OnlineBaseCode robotSmartAnswerModels) {

            }

            @Override
            public void onFailure(Exception e, String des) {
                SobotToastUtil.showCustomToast(getSobotActivity(), des);
            }
        });
    }


    private View getPopContentView() {
        if (mPopContentView == null) {
            mPopContentView = LayoutInflater.from(getSobotActivity()).inflate(SobotResourceUtils.getResLayoutId(getSobotActivity(), "pop_layout_knowledge_swith_rebot"), null);
            if (mPopContentView != null) {
                rv_pop_knowledge_swith_rebot = mPopContentView.findViewById(SobotResourceUtils.getResId(getSobotActivity(), "rv_pop_knowledge_swith_rebot"));
                LinearLayoutManager layoutManager = new LinearLayoutManager(getSobotActivity());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                rv_pop_knowledge_swith_rebot.setLayoutManager(layoutManager);
                if (admin != null && admin.getRobots() != null && admin.getRobots().size() > 6) {
                    //大于7个机器人时固定列表的高度
                    LinearLayout ll_pop_rebot = mPopContentView.findViewById(SobotResourceUtils.getResId(getSobotActivity(), "ll_pop_rebot"));
                    ViewGroup.LayoutParams params = (LinearLayout.LayoutParams) ll_pop_rebot.getLayoutParams();
                    params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    params.height = 7 * SobotSizeUtils.dp2px(50);
                    ll_pop_rebot.setLayoutParams(params);
                }
                rv_pop_knowledge_swith_rebot.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).colorResId(SobotResourceUtils.getResColorId(getSobotActivity(), "sobot_online_line_color")).size(1).showLastDivider().build());
                rebotAdapter = new SobotPopRebotAdapter(getSobotActivity(), tv_online_select_rebot_ids.getText().toString(), tv_online_select_rebot.getText().toString());
                rv_pop_knowledge_swith_rebot.setAdapter(rebotAdapter);
                TextView tv_sobot_online_save = mPopContentView.findViewById(SobotResourceUtils.getResId(getSobotActivity(), "tv_sobot_online_save"));
                tv_sobot_online_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (rebotAdapter != null) {
                            if (TextUtils.isEmpty(rebotAdapter.getSelectRebotFlags())) {
                                tv_online_select_rebot.setText(SobotResourceUtils.getResString(getSobotActivity(), "online_all_rebot"));
                                tv_online_select_rebot_ids.setText("");
                            } else {
                                tv_online_select_rebot.setText(rebotAdapter.getSelectRebotNames());
                                tv_online_select_rebot_ids.setText(rebotAdapter.getSelectRebotFlags());
                            }
                            modifyAdminConfig();
                        }
                        if (mPopWindow != null) {
                            mPopWindow.dissmiss();
                        }
                    }
                });

            }
        }
        return mPopContentView;
    }
}