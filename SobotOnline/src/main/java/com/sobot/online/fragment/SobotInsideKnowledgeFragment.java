package com.sobot.online.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sobot.common.frame.http.callback.SobotResultCallBack;
import com.sobot.online.adapter.SobotRebotInsideKnoweledgeAdapter;
import com.sobot.online.base.SobotOnlineBaseFragment;
import com.sobot.online.model.RebotSmartAnswerModel;
import com.sobot.online.weight.recyclerview.SobotRecyclerView;
import com.sobot.online.weight.recyclerview.swipemenu.SobotSwipeMenuRecyclerView;
import com.sobot.online.weight.toast.SobotToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 智能回复 内部知识库
 * @Author: znw
 * @CreateDate: 2020/10/12 18:37
 * @Version: 1.0
 */
public class SobotInsideKnowledgeFragment extends SobotOnlineBaseFragment implements SobotRecyclerView.LoadingListener, View.OnClickListener {
    private View mRootView;

    SobotSwipeMenuRecyclerView ssmrv_online_inside_knowledge;//分组 第一层
    SobotRebotInsideKnoweledgeAdapter rebotInsideKnoweledgeAdapter;
    List<RebotSmartAnswerModel> rebotSmartAnswerModelArrayList = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(getResLayoutId("sobot_fragment_inside_knowledge"), container, false);
        initView();
        initData();
        return mRootView;
    }

    protected void initView() {
        ssmrv_online_inside_knowledge = mRootView.findViewById(getResId("ssmrv_online_inside_knowledge"));

        LinearLayoutManager firstlayoutManager = new LinearLayoutManager(getSobotActivity());
        firstlayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ssmrv_online_inside_knowledge.setLayoutManager(firstlayoutManager);
        ssmrv_online_inside_knowledge.setPullRefreshEnabled(false);
        ssmrv_online_inside_knowledge.setLoadingMoreEnabled(false);
        ssmrv_online_inside_knowledge.setLoadingListener(this);
    }

    private void initData() {
        rebotInsideKnoweledgeAdapter = new SobotRebotInsideKnoweledgeAdapter(getSobotActivity(), rebotSmartAnswerModelArrayList);
        ssmrv_online_inside_knowledge.setAdapter(rebotInsideKnoweledgeAdapter);
        rebotInsideKnoweledgeAdapter.setGroups(rebotSmartAnswerModelArrayList);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onClick(View v) {

    }


    //根据关键字模糊查询快捷回复内容列表
    public void innerInternalChat(String keyword) {
        zhiChiApi.innerInternalChat(getSobotActivity(), keyword,  new SobotResultCallBack<List<RebotSmartAnswerModel>>() {

            @Override
            public void onSuccess(List<RebotSmartAnswerModel> rebotInsideAnswerModels) {
                if (rebotInsideAnswerModels != null) {
                    rebotInsideKnoweledgeAdapter.setGroups(rebotInsideAnswerModels);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                SobotToastUtil.showCustomToast(getSobotActivity(), des);
            }
        });
    }
}