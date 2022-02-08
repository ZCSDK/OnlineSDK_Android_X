package com.sobot.online.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sobot.online.model.HistoryUserInfoModel;
import com.sobot.online.weight.recyclerview.adapter.HelperRecyclerViewHolder;
import com.sobot.online.weight.recyclerview.adapter.HelperStateRecyclerViewAdapter;
import com.sobot.online.weight.recyclerview.swipemenu.SwipeMenuLayout;
import com.sobot.onlinecommon.utils.SobotResourceUtils;
import com.sobot.onlinecommon.utils.SobotTimeUtils;
import com.sobot.pictureframe.SobotBitmapUtil;

//历史会话-用户列表 adapter
public class SobotHistoryUserChatAdapter extends HelperStateRecyclerViewAdapter<HistoryUserInfoModel> {
    private Context mContext;

    public SobotHistoryUserChatAdapter(Context context) {
        super(context, SobotResourceUtils.getResLayoutId(context, "adapter_reception_layout"));
        mContext = context;
    }

    @Override
    protected void HelperBindData(HelperRecyclerViewHolder viewHolder, int position, HistoryUserInfoModel item) {
        final SwipeMenuLayout superSwipeMenuLayout = (SwipeMenuLayout) viewHolder.itemView;
        superSwipeMenuLayout.setSwipeEnable(false);   //设置是否可以侧滑
        viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_reception_user_nike"), item.getUname());
        viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_reception_last_msg_content"), TextUtils.isEmpty(item.getLastMsg()) ? SobotResourceUtils.getResString(mContext, "online_click_look_msg"): item.getLastMsg());
        if (!TextUtils.isEmpty(item.getTs())) {
            viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_reception_last_msg_time"), SobotTimeUtils.getTimeFormatText(SobotTimeUtils.string2Millis(item.getTs()), SobotResourceUtils.getResString(mContext, "onnline_time_tianqian")));
        }
        ImageView iv_reception_user_mark = viewHolder.getView(SobotResourceUtils.getResId(mContext, "iv_reception_user_mark"));
        if (item.getIsmark() == 1) {
            iv_reception_user_mark.setVisibility(View.VISIBLE);
            viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_reception_menu_star_target"), SobotResourceUtils.getResString(mContext, "online_mark_cancle"));
        } else {
            viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_reception_menu_star_target"), SobotResourceUtils.getResString(mContext, "online_mark"));
            iv_reception_user_mark.setVisibility(View.GONE);
        }
        //头像
        ImageView head_avator = viewHolder.getView(SobotResourceUtils.getResId(mContext, "srcv_reception_user_avatar"));
        if (head_avator != null) {
            setUserHead(item, mContext, head_avator);
        }
    }


    @Override
    public View getEmptyView(ViewGroup parent) {
        return mLInflater.inflate(SobotResourceUtils.getResLayoutId(mContext, "sobot_layout_empty"), parent, false);
    }

    @Override
    public View getErrorView(ViewGroup parent) {
        return mLInflater.inflate(SobotResourceUtils.getResLayoutId(mContext, "sobot_layout_empty"), parent, false);
    }

    @Override
    public View getLoadingView(ViewGroup parent) {
        return mLInflater.inflate(SobotResourceUtils.getResLayoutId(mContext, "sobot_layout_empty"), parent, false);
    }

    private void setUserHead(HistoryUserInfoModel model, Context context, ImageView head_avator) {
        int source = Integer.parseInt(TextUtils.isEmpty(model.getSource()) ? "0" : model.getSource());
        if (!TextUtils.isEmpty(model.getFace())) {
            SobotBitmapUtil.display(mContext, model.getFace(), head_avator);
            return;
        }
        SobotBitmapUtil.display(mContext, getUserAvatorWithSource(context, source, true), head_avator);
    }

    /**
     * 聊天列表获取默认头像id
     *
     * @param source
     * @param isOnline
     * @return
     */
    public int getUserAvatorWithSource(Context context, int source, boolean isOnline) {
        int avatorDrawable;
        switch (source) {
            case 0:
                avatorDrawable = isOnline ? getDrawable(context, "avatar_computer_online") : getDrawable(context, "avatar_computer_offline");
                break;
            case 1:
            case 9:
            case 10:
                avatorDrawable = isOnline ? getDrawable(context, "avatar_wechat_online") : getDrawable(context, "avatar_wechat_offline");
                break;
            case 2:
                avatorDrawable = isOnline ? getDrawable(context, "avatar_app_online") : getDrawable(context, "avatar_app_offline");
                break;
            case 3:
                avatorDrawable = isOnline ? getDrawable(context, "avatar_weibo_online") : getDrawable(context, "avatar_weibo_offline");
                break;
            case 4:
                avatorDrawable = isOnline ? getDrawable(context, "avatar_phone_online") : getDrawable(context, "avatar_phone_offline");
                break;
            default:
                avatorDrawable = isOnline ? getDrawable(context, "avatar_computer_online") : getDrawable(context, "avatar_computer_offline");
                break;
        }
        return avatorDrawable;
    }

    public int getDrawable(Context context, String drawableIdStr) {
        return SobotResourceUtils.getDrawableId(context, drawableIdStr);
    }
}
