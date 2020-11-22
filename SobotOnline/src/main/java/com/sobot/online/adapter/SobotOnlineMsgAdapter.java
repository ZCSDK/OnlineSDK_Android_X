package com.sobot.online.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sobot.common.frame.http.callback.SobotResultCallBack;
import com.sobot.common.gson.SobotGsonUtil;
import com.sobot.common.socket.SobotSocketConstant;
import com.sobot.common.utils.SobotLogUtils;
import com.sobot.common.utils.SobotResourceUtils;
import com.sobot.common.utils.SobotSPUtils;
import com.sobot.common.utils.SobotSizeUtils;
import com.sobot.common.utils.SobotTimeUtils;
import com.sobot.common.utils.SobotUtils;
import com.sobot.online.OnlineConstant;
import com.sobot.online.activity.OnlineWebViewActivity;
import com.sobot.online.activity.SobotFileDetailActivity;
import com.sobot.online.activity.SobotIntelligenceReplyActivity;
import com.sobot.online.activity.SobotPhotoActivity;
import com.sobot.online.activity.SobotUserInfoActivity;
import com.sobot.online.activity.SobotVideoActivity;
import com.sobot.online.api.ZhiChiOnlineApi;
import com.sobot.online.api.ZhiChiOnlineApiFactory;
import com.sobot.online.dialog.OnlineReSendDialog;
import com.sobot.online.dialog.SobotOnlyRetractDialog;
import com.sobot.online.imageloader.SobotBitmapUtil;
import com.sobot.online.model.ChatMessageAudioModel;
import com.sobot.online.model.ChatMessageConsultingModel;
import com.sobot.online.model.ChatMessageFileModel;
import com.sobot.online.model.ChatMessageLocationModel;
import com.sobot.online.model.ChatMessageModel;
import com.sobot.online.model.ChatMessageObjectModel;
import com.sobot.online.model.ChatMessageOrderCardModel;
import com.sobot.online.model.ChatMessageRichTextModel;
import com.sobot.online.model.ChatMessageVideoModel;
import com.sobot.online.model.CustomerServiceInfoModel;
import com.sobot.online.model.HistoryUserInfoModel;
import com.sobot.online.model.OnlineCommonModel;
import com.sobot.online.model.SobotCacheFile;
import com.sobot.online.util.HtmlTools;
import com.sobot.online.util.SobotFileTypeUtil;
import com.sobot.online.util.SobotMapOpenHelper;
import com.sobot.online.weight.image.SobotRCImageView;
import com.sobot.online.weight.recyclerview.adapter.HelperRecyclerViewAdapter;
import com.sobot.online.weight.recyclerview.adapter.HelperRecyclerViewHolder;
import com.sobot.online.weight.toast.SobotToastUtil;

import static com.sobot.online.OnlineConstant.SOBOT_CUSTOM_USER;

//聊天主页面 消息列表 adapter
public class SobotOnlineMsgAdapter extends HelperRecyclerViewAdapter<ChatMessageModel> {

    private Context mContext;
    private HistoryUserInfoModel mUserInfoModel;
    private OnlineMsgCallBack mMsgCallBack;
    private CustomerServiceInfoModel admin;
    private int mUserSource = -1;// 用户来源  0桌面1微信2app3微博4移动网站9企业微信，10微信小程序

    public SobotOnlineMsgAdapter(Context context, HistoryUserInfoModel userInfoModel, int userSource, OnlineMsgCallBack msgCallBack) {
        super(context,
                //0 文本类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_text"),
                //1 图片类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_image"),
                //2 音频类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_audio"),
                //3 视频类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_video"),
                //4 文件类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_file"),
                //5 富文本类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_richtext"),
                //6 多伦会话类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_text"),
                //7 位置类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_location"),
                //8 小卡片类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_consulting"),
                //9 订单卡片类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_ordercard"),
                //10 提醒类型消息
                SobotResourceUtils.getResLayoutId(context, "adapter_chat_msg_item_remind"));
        mContext = context;
        mUserInfoModel = userInfoModel;
        mMsgCallBack = msgCallBack;
        mUserSource = userSource;
        admin = (CustomerServiceInfoModel) SobotSPUtils.getInstance().getObject(SOBOT_CUSTOM_USER);
    }

    @Override
    protected void HelperBindData(final HelperRecyclerViewHolder viewHolder, final int position, final ChatMessageModel item) {
        int chatMsgType = getItemViewType(position);
        SobotRCImageView userAvatar = viewHolder.getView(SobotResourceUtils.getResId(mContext, "srcv_msg_item_text_left_user_avatar"));
        if (userAvatar != null) {
            userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userInfoIntent = new Intent(mContext, SobotUserInfoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userinfo", mUserInfoModel);
                    userInfoIntent.putExtras(bundle);
                    mContext.startActivity(userInfoIntent);
                }
            });
        }
        final ProgressBar sendLoadingPB = viewHolder.getView(SobotResourceUtils.getResId(mContext, "iv_msg_item_send_loading"));
        final ImageView sendStatusIV = viewHolder.getView(SobotResourceUtils.getResId(mContext, "iv_msg_item_send_failure"));
        if (sendStatusIV != null) {
            //1 成功 0 发送失败 2 发送中
            if (item != null && item.getIsSendOk() == 0) {
                sendStatusIV.setVisibility(View.VISIBLE);
            } else {
                sendStatusIV.setVisibility(View.GONE);
            }
            sendStatusIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMsgCallBack != null && !item.isRevokeFlag()) {
                        showReSendDialog(mContext, item, sendLoadingPB, sendStatusIV, mMsgCallBack);
                    }
                }
            });
        }
        if (sendLoadingPB != null) {
            //1 成功 0 发送失败 2 发送中
            if (item != null && item.getIsSendOk() == 2) {
                sendLoadingPB.setVisibility(View.VISIBLE);
            } else {
                sendLoadingPB.setVisibility(View.GONE);
            }
        }
        if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_TEXT) {
            if (item.getSenderType() == 0) {// 左侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_text_left")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_text_right")).setVisibility(View.GONE);
                viewHolder.setRichText(SobotResourceUtils.getResId(mContext, "tv_msg_item_text_left_content"), (String) item.getMessage().getContent(), true);
                setUserHead(item.getSenderFace(), mContext, userAvatar);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "tv_msg_item_text_left_content")).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showPopWindows(v, item, position, false);
                        return true;
                    }
                });
            }
            if (item.getSenderType() == 1 || item.getSenderType() == 2) {// 右侧
                //历史记录重发时重新复制
                item.setAdminSendContent((String) item.getMessage().getContent());
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_text_right")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_text_left")).setVisibility(View.GONE);
                viewHolder.setRichText(SobotResourceUtils.getResId(mContext, "tv_msg_item_text_right_content"), (String) item.getMessage().getContent(), false);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "srcv_msg_item_text_right_user_avatar"), item.getSenderFace(), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"));
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "tv_msg_item_text_right_content")).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showPopWindows(v, item, position, true);
                        return true;
                    }
                });
            }
        } else if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_IMG) {
            if (item.getSenderType() == 0) {// 左侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_image_left")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_image_right")).setVisibility(View.GONE);
                setUserHead(item.getSenderFace(), mContext, userAvatar);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "iv_msg_item_image_left_content"), (String) item.getMessage().getContent());
            }
            if (item.getSenderType() == 1 || item.getSenderType() == 2) {// 右侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_image_right")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_image_left")).setVisibility(View.GONE);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "srcv_msg_item_image_right_user_avatar"), item.getSenderFace(), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"));
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "iv_msg_item_image_right_content"), (String) item.getMessage().getContent());
            }
            viewHolder.getView(SobotResourceUtils.getResId(mContext, "iv_msg_item_image_left_content")).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent photoIntent = new Intent(mContext, SobotPhotoActivity.class);
                    photoIntent.putExtra("imageUrL", (String) item.getMessage().getContent());
                    mContext.startActivity(photoIntent);
                }
            });
            viewHolder.getView(SobotResourceUtils.getResId(mContext, "iv_msg_item_image_right_content")).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent photoIntent = new Intent(mContext, SobotPhotoActivity.class);
                    photoIntent.putExtra("imageUrL", (String) item.getMessage().getContent());
                    mContext.startActivity(photoIntent);
                }
            });
        } else if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_AUDIO) {
            ChatMessageAudioModel messageAudioModel = null;
            if (item.getMessage().getContent() != null) {
                String temp = SobotGsonUtil.gsonString(item.getMessage().getContent());
                if (!TextUtils.isEmpty(temp)) {
                    messageAudioModel = SobotGsonUtil.gsonToBean(temp, ChatMessageAudioModel.class);
                }
            }
            ImageView ivPlayAudio = viewHolder.getView(SobotResourceUtils.getResId(mContext, "iv_msg_item_audio_left_play"));
            if (item.getSenderType() == 0) {// 左侧
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_audio_left_content"), messageAudioModel != null ? messageAudioModel.getDuration() : "");
                setUserHead(item.getSenderFace(), mContext, userAvatar);
                if (item.isVoideIsPlaying()) {
                    resetAnim(ivPlayAudio);
                } else {
                    ivPlayAudio.setImageResource(
                            SobotResourceUtils.getIdByName(mContext, "drawable", "sobot_pop_voice_receive_anime_3"));
                }
            }
            viewHolder.getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView playAudio = v.findViewById(SobotResourceUtils.getResId(mContext, "iv_msg_item_audio_left_play"));
                    if (mMsgCallBack != null) {
                        //点击播放用户发过来的语音时，需要改为历史记录才行
                        item.setIsHistory(1);
                        mMsgCallBack.clickAudioItem(item, playAudio);
                    }
                }
            });
        } else if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_VIDEO) {
            ChatMessageVideoModel messageVideoModel = null;
            if (item.getMessage().getContent() != null) {
                String temp = SobotGsonUtil.gsonString(item.getMessage().getContent());
                if (!TextUtils.isEmpty(temp)) {
                    messageVideoModel = SobotGsonUtil.gsonToBean(temp, ChatMessageVideoModel.class);
                }
            }
            if (item.getSenderType() == 0) {// 左侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_video_left")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_video_right")).setVisibility(View.GONE);
                setUserHead(item.getSenderFace(), mContext, userAvatar);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "iv_msg_item_video_left_content"), messageVideoModel != null ? messageVideoModel.getSnapshot() : "");
            }
            if (item.getSenderType() == 1 || item.getSenderType() == 2) {// 右侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_video_right")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_video_left")).setVisibility(View.GONE);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "srcv_msg_item_video_right_user_avatar"), item.getSenderFace(), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"));
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "iv_msg_item_video_right_content"), messageVideoModel != null ? messageVideoModel.getSnapshot() : "");
            }
            final ChatMessageVideoModel finalMessageVideoModel = messageVideoModel;
            viewHolder.getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != finalMessageVideoModel) {
                        SobotCacheFile cacheFile = new SobotCacheFile();
                        cacheFile.setFileName(finalMessageVideoModel.getFileName());
                        cacheFile.setUrl(item.getSenderType() == 0 ? finalMessageVideoModel.getUrl() : finalMessageVideoModel.getServiceUrl());
                        cacheFile.setMsgId(item.getMsgId());
                        Intent intent = SobotVideoActivity.newIntent(mContext, cacheFile);
                        mContext.startActivity(intent);
                    }
                }
            });
        } else if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_FILE) {
            ChatMessageFileModel messageFileModel = null;
            if (item.getMessage().getContent() != null) {
                String temp = SobotGsonUtil.gsonString(item.getMessage().getContent());
                if (!TextUtils.isEmpty(temp)) {
                    messageFileModel = SobotGsonUtil.gsonToBean(temp, ChatMessageFileModel.class);
                }
            }
            if (item.getSenderType() == 0) {// 左侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_file_left")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_msg_item_file_right")).setVisibility(View.GONE);
                setUserHead(item.getSenderFace(), mContext, userAvatar);
                viewHolder.setImageDrawableRes(SobotResourceUtils.getResId(mContext, "iv_msg_item_left_file_type_icon"), SobotFileTypeUtil.getFileIcon(mContext, messageFileModel != null ? messageFileModel.getType() : 0));
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_file_name"), messageFileModel != null ? messageFileModel.getFileName() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_file_size"), messageFileModel != null ? messageFileModel.getFileSize() : "");
            }
            if (item.getSenderType() == 1 || item.getSenderType() == 2) {// 右侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_msg_item_file_right")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_file_left")).setVisibility(View.GONE);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "srcv_msg_item_file_right_user_avatar"), item.getSenderFace(), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"));
                viewHolder.setImageDrawableRes(SobotResourceUtils.getResId(mContext, "iv_msg_item_right_file_type_icon"), SobotFileTypeUtil.getFileIcon(mContext, messageFileModel != null ? messageFileModel.getType() : 0));
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_file_name"), messageFileModel != null ? messageFileModel.getFileName() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_file_size"), messageFileModel != null ? messageFileModel.getFileSize() : "");
            }
            final ChatMessageFileModel finalMessageFileModel = messageFileModel;
            viewHolder.getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != finalMessageFileModel) {
                        SobotCacheFile cacheFile = new SobotCacheFile();
                        cacheFile.setFileName(finalMessageFileModel.getFileName());
                        cacheFile.setUrl(finalMessageFileModel.getUrl());
                        cacheFile.setMsgId(item.getMsgId());
                        cacheFile.setFileSize(finalMessageFileModel.getFileSize());
                        cacheFile.setFileType( finalMessageFileModel != null ? finalMessageFileModel.getType() : 0);

                        // 打开详情页面
                        Intent intent = new Intent(mContext, SobotFileDetailActivity.class);
                        intent.putExtra(OnlineConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                }
            });
        } else if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_RICH) {
            ChatMessageRichTextModel richTextModel = null;
            ChatMessageObjectModel messageObjectModel = null;
            if (item.getMessage().getContent() != null) {
                String temp = SobotGsonUtil.gsonString(item.getMessage().getContent());
                if (!TextUtils.isEmpty(temp)) {
                    messageObjectModel = SobotGsonUtil.gsonToBean(temp, ChatMessageObjectModel.class);
                    if (messageObjectModel != null) {
                        String msgtemp = SobotGsonUtil.gsonString(messageObjectModel.getMsg());
                        if (!TextUtils.isEmpty(msgtemp)) {
                            richTextModel = SobotGsonUtil.gsonToBean(msgtemp, ChatMessageRichTextModel.class);
                            if (richTextModel != null) {

                                if (item.getSenderType() == 0) {// 左侧
                                    viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_richtext_left")).setVisibility(View.VISIBLE);
                                    viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_richtext_right")).setVisibility(View.GONE);
                                    setUserHead(item.getSenderFace(), mContext, userAvatar);
                                }
                                if (item.getSenderType() == 1 || item.getSenderType() == 2) {// 右侧
                                    LinearLayout richMsgLL = viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_richtext_right_content"));
                                    viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_richtext_right")).setVisibility(View.VISIBLE);
                                    viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_richtext_left")).setVisibility(View.GONE);
                                    viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "srcv_msg_item_richtext_right_user_avatar"), item.getSenderFace(), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"));
                                    if (richTextModel.getRichList() != null) {
                                        LinearLayout.LayoutParams wlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT);
                                        richMsgLL.removeAllViews();
                                        for (int i = 0; i < richTextModel.getRichList().size(); i++) {
                                            ChatMessageRichTextModel.ChatMessageRichListModel richListModel = richTextModel.getRichList().get(i);
                                            // 0：文本，1：图片，2：音频，3：视频，4：文件
                                            if (richListModel.getType() == 0) {
                                                TextView textView = new TextView(mContext);
                                                HtmlTools.getInstance(mContext).setRichText(textView, richListModel.getMsg(), getLinkTextColor(false));
                                                textView.setLayoutParams(wlayoutParams);
                                                textView.setTextColor(ContextCompat.getColor(mContext, SobotResourceUtils.getResColorId(mContext, "sobot_right_msg_text_color")));
                                                richMsgLL.addView(textView);
                                            }
                                            if (richListModel.getType() == 1) {
                                                LinearLayout.LayoutParams mlayoutParams = new LinearLayout.LayoutParams(SobotSizeUtils.dp2px(200),
                                                        SobotSizeUtils.dp2px(200));
                                                ImageView imageView = new ImageView(mContext);
                                                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                                imageView.setLayoutParams(mlayoutParams);
                                                SobotBitmapUtil.display(mContext, richListModel.getMsg(), imageView);
                                                richMsgLL.addView(imageView);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_ROUNDS) {
            if (item.getSenderType() == 0) {// 左侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_text_left")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_text_right")).setVisibility(View.GONE);
                viewHolder.setRichText(SobotResourceUtils.getResId(mContext, "tv_msg_item_text_left_content"), SobotResourceUtils.getResString(mContext, "online_no_support_type_msg"), true);
                setUserHead(item.getSenderFace(), mContext, userAvatar);
            }
            if (item.getSenderType() == 1 || item.getSenderType() == 2) {// 右侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_text_right")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_text_left")).setVisibility(View.GONE);
                viewHolder.setRichText(SobotResourceUtils.getResId(mContext, "tv_msg_item_text_right_content"), SobotResourceUtils.getResString(mContext, "online_no_support_type_msg"), false);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "srcv_msg_item_text_right_user_avatar"), item.getSenderFace(), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"));
            }
        } else if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_LOCATION) {
            ChatMessageObjectModel messageObjectModel = null;
            ChatMessageLocationModel locationModel = null;
            if (item.getMessage().getContent() != null) {
                String temp = SobotGsonUtil.gsonString(item.getMessage().getContent());
                if (!TextUtils.isEmpty(temp)) {
                    messageObjectModel = SobotGsonUtil.gsonToBean(temp, ChatMessageObjectModel.class);
                    if (messageObjectModel != null) {
                        String msgtemp = SobotGsonUtil.gsonString(messageObjectModel.getMsg());
                        if (!TextUtils.isEmpty(msgtemp)) {
                            locationModel = SobotGsonUtil.gsonToBean(msgtemp, ChatMessageLocationModel.class);
                        }
                    }
                }
            }

            if (item.getSenderType() == 0) {// 左侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_location_left")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_msg_item_location_right")).setVisibility(View.GONE);
                setUserHead(item.getSenderFace(), mContext, userAvatar);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "iv_msg_item_left_location_icon"), locationModel != null ? locationModel.getPicUrl() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_location_title"), locationModel != null ? locationModel.getTitle() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_location_des"), locationModel != null ? locationModel.getDesc() : "");

            }
            if (item.getSenderType() == 1) {// 右侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_msg_item_location_right")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_location_left")).setVisibility(View.GONE);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "srcv_msg_item_location_right_user_avatar"), item.getSenderFace(), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"));
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "iv_msg_item_right_location_icon"), locationModel != null ? locationModel.getPicUrl() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_location_title"), locationModel != null ? locationModel.getTitle() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_location_des"), locationModel != null ? locationModel.getDesc() : "");
            }
            if (locationModel != null) {
                startMap(viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_right_content")), locationModel);
                startMap(viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_left_content")), locationModel);
            }

        } else if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_CONSULTING) {
            ChatMessageObjectModel messageObjectModel = null;
            ChatMessageConsultingModel messageConsultingModel = null;
            if (item.getMessage().getContent() != null) {
                String temp = SobotGsonUtil.gsonString(item.getMessage().getContent());
                if (!TextUtils.isEmpty(temp)) {
                    messageObjectModel = SobotGsonUtil.gsonToBean(temp, ChatMessageObjectModel.class);
                    if (messageObjectModel != null) {
                        String msgtemp = SobotGsonUtil.gsonString(messageObjectModel.getMsg());
                        if (!TextUtils.isEmpty(msgtemp)) {
                            messageConsultingModel = SobotGsonUtil.gsonToBean(msgtemp, ChatMessageConsultingModel.class);
                        }
                    }
                }
            }

            if (item.getSenderType() == 0) {// 左侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_consulting_left")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_msg_item_consulting_right")).setVisibility(View.GONE);
                setUserHead(item.getSenderFace(), mContext, userAvatar);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "iv_msg_item_left_consulting_thumbnail"), messageConsultingModel != null ? messageConsultingModel.getThumbnail() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_consulting_title"), messageConsultingModel != null ? messageConsultingModel.getTitle() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_consulting_description"), messageConsultingModel != null ? messageConsultingModel.getDescription() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_consulting_label"), messageConsultingModel != null ? messageConsultingModel.getLabel() : "");

            }
            if (item.getSenderType() == 2) {// 右侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_msg_item_consulting_right")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_consulting_left")).setVisibility(View.GONE);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "srcv_msg_item_consulting_right_user_avatar"), item.getSenderFace(), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"));
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "iv_msg_item_right_consulting_thumbnail"), messageConsultingModel != null ? messageConsultingModel.getThumbnail() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_consulting_title"), messageConsultingModel != null ? messageConsultingModel.getTitle() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_consulting_description"), messageConsultingModel != null ? messageConsultingModel.getDescription() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_consulting_label"), messageConsultingModel != null ? messageConsultingModel.getLabel() : "");
            }
            if (messageConsultingModel != null) {
                String url = messageConsultingModel.getUrl();
                startWebview(viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_right_content")), url);
                startWebview(viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_left_content")), url);
            }
        } else if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_ORDERCARD) {
            ChatMessageObjectModel messageObjectModel = null;
            ChatMessageOrderCardModel orderCardModel = null;
            if (item.getMessage().getContent() != null) {
                String temp = SobotGsonUtil.gsonString(item.getMessage().getContent());
                if (!TextUtils.isEmpty(temp)) {
                    messageObjectModel = SobotGsonUtil.gsonToBean(temp, ChatMessageObjectModel.class);
                    if (messageObjectModel != null) {
                        String msgtemp = SobotGsonUtil.gsonString(messageObjectModel.getMsg());
                        if (!TextUtils.isEmpty(msgtemp)) {
                            orderCardModel = SobotGsonUtil.gsonToBean(msgtemp, ChatMessageOrderCardModel.class);
                            SobotLogUtils.i(orderCardModel.toString());
                        }
                    }
                }
            }
            String statusStr = "";
            if (orderCardModel != null) {
                //待付款: 1   待发货: 2   运输中: 3   派送中: 4   已完成: 5   待评价: 6   已取消: 7
                switch (orderCardModel.getOrderStatus()) {
                    case 1:
                        statusStr = SobotResourceUtils.getResString(mContext, "sobot_order_status_1");
                        break;
                    case 2:
                        statusStr = SobotResourceUtils.getResString(mContext, "sobot_order_status_2");
                        break;
                    case 3:
                        statusStr = SobotResourceUtils.getResString(mContext, "sobot_order_status_3");
                        break;
                    case 4:
                        statusStr = SobotResourceUtils.getResString(mContext, "sobot_order_status_4");
                        break;
                    case 5:
                        statusStr = SobotResourceUtils.getResString(mContext, "sobot_order_status_5");
                        break;
                    case 6:
                        statusStr = SobotResourceUtils.getResString(mContext, "sobot_order_status_6");
                        break;
                    case 7:
                        statusStr = SobotResourceUtils.getResString(mContext, "sobot_order_status_7");
                        break;
                }
            }
            if (item.getSenderType() == 0) {// 左侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_ordercard_left")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_msg_item_ordercard_right")).setVisibility(View.GONE);
                setUserHead(item.getSenderFace(), mContext, userAvatar);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "iv_msg_item_left_ordercard_firstthumbnail"), (orderCardModel != null && orderCardModel.getGoods().size() > 0) ? orderCardModel.getGoods().get(0).getPictureUrl() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_ordercard_firsttitle"), (orderCardModel != null && orderCardModel.getGoods().size() > 0) ? orderCardModel.getGoods().get(0).getName() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_ordercard_totaldes"), orderCardModel != null ? orderCardModel.getGoods().size() + "件商品,合计¥" + orderCardModel.getTotalFee() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_ordercard_status"), SobotResourceUtils.getResString(mContext, "sobot_order_status_lable") + "<b><font color=\'#E67F17\'>" + statusStr + "</font></b>");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_ordercard_ordernum"), SobotResourceUtils.getResString(mContext, "sobot_order_code_lable") + (orderCardModel != null ? orderCardModel.getOrderCode() : ""));
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_left_ordercard_creattime"), SobotResourceUtils.getResString(mContext, "sobot_order_time_lable") + (orderCardModel != null ? orderCardModel.getCreateTimeFormat() : ""));
            }
            if (item.getSenderType() == 2) {// 右侧
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_msg_item_ordercard_right")).setVisibility(View.VISIBLE);
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "ll_msg_item_ordercard_left")).setVisibility(View.GONE);
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "srcv_msg_item_ordercard_right_user_avatar"), item.getSenderFace(), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"), SobotResourceUtils.getDrawableId(mContext, "sobot_service_header_def"));
                viewHolder.setImageUrl(SobotResourceUtils.getResId(mContext, "iv_msg_item_right_ordercard_firstthumbnail"), (orderCardModel != null && orderCardModel.getGoods().size() > 0) ? orderCardModel.getGoods().get(0).getPictureUrl() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_ordercard_firsttitle"), (orderCardModel != null && orderCardModel.getGoods().size() > 0) ? orderCardModel.getGoods().get(0).getName() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_ordercard_totaldes"), orderCardModel != null ? orderCardModel.getGoods().size() + "件商品,合计¥" + orderCardModel.getTotalFee() : "");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_ordercard_status"), SobotResourceUtils.getResString(mContext, "sobot_order_status_lable") + "<b><font color=\'#E67F17\'>" + statusStr + "</font></b>");
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_ordercard_ordernum"), SobotResourceUtils.getResString(mContext, "sobot_order_code_lable") + (orderCardModel != null ? orderCardModel.getOrderCode() : ""));
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_right_ordercard_creattime"), SobotResourceUtils.getResString(mContext, "sobot_order_time_lable") + (orderCardModel != null ? orderCardModel.getCreateTimeFormat() : ""));

            }
            if (orderCardModel != null) {
                String url = orderCardModel.getOrderUrl();
                startWebview(viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_right_content")), url);
                startWebview(viewHolder.getView(SobotResourceUtils.getResId(mContext, "rl_left_content")), url);
            }
        } else if (chatMsgType == OnlineConstant.SOBOT_MSG_TYPE_REMIND) {
            //所有的提示消息类型，具体赋值时根据item.getAction()去选择
            if (TextUtils.isEmpty(item.getTs())) {
                viewHolder.getView(SobotResourceUtils.getResId(mContext, "tv_msg_item_remind_time")).setVisibility(View.GONE);
            } else {
                viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_remind_time"), SobotTimeUtils.formatDateTime(item.getTs(), ""));
            }
            viewHolder.setText(SobotResourceUtils.getResId(mContext, "tv_msg_item_remind_content"), getRemindMessageContent(viewHolder, item));
        }
    }

    @Override
    public int checkLayout(ChatMessageModel item, int position) {
        if (item != null) {
            if (item.isRevokeFlag()) {
                return OnlineConstant.SOBOT_MSG_TYPE_REMIND;
            } else if (item.getAction() == 5) {
                if (item.getMessage() != null && !TextUtils.isEmpty(item.getMessage().getMsgType())) {
                    //msgType：文本,图片,音频,视频,文件,对象
                    //msgType：0,1,2,3,4,5
                    //当msgType=5 时，根据content里边的 type 判断具体的时哪种消息 0-富文本 1-多伦会话 2-位置 3-小卡片 4-订单卡片
                    if ("5".equals(item.getMessage().getMsgType().trim())) {
                        ChatMessageObjectModel messageObjectModel = null;
                        if (item.getMessage().getContent() != null) {
                            String temp = SobotGsonUtil.gsonString(item.getMessage().getContent());
                            if (!TextUtils.isEmpty(temp) && temp.contains("msg")) {
                                messageObjectModel = SobotGsonUtil.gsonToBean(temp, ChatMessageObjectModel.class);
                                if (messageObjectModel != null) {
                                    switch (messageObjectModel.getType()) {
                                        case 0:
                                            return OnlineConstant.SOBOT_MSG_TYPE_RICH;
                                        case 1:
                                            return OnlineConstant.SOBOT_MSG_TYPE_ROUNDS;
                                        case 2:
                                            return OnlineConstant.SOBOT_MSG_TYPE_LOCATION;
                                        case 3:
                                            return OnlineConstant.SOBOT_MSG_TYPE_CONSULTING;
                                        case 4:
                                            return OnlineConstant.SOBOT_MSG_TYPE_ORDERCARD;
                                    }
                                }
                            }
                        }
                    }
                    return Integer.parseInt(item.getMessage().getMsgType());
                }
            } else {
                //所有的提示消息类型，具体赋值时根据item.getAction()去选择
                return OnlineConstant.SOBOT_MSG_TYPE_REMIND;
            }
        }
        //未识别类型
        return OnlineConstant.SOBOT_MSG_TYPE_ERROR;
    }

    // 提醒消息
    private String getRemindMessageContent(HelperRecyclerViewHolder viewHolder, final ChatMessageModel item) {

        String receiver;
        if (item.getAction() == OnlineConstant.MSG_TYPE_ADMIN_CONNECTED_USER) {
            receiver = "  " + item.getSenderName() + "  ";
        } else {
            receiver = "  " + item.getReceiverName() + "  ";
        }
        String sender = "  " + item.getSenderName() + "  ";

        if (item.getAction() == OnlineConstant.MSG_TYPE_CUSTOMER_LOGIN) {
            return SobotResourceUtils.getResString(mContext, "online_kefu_online");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_CUSTOMER_LOGOUT) {
            return SobotResourceUtils.getResString(mContext, "online_kefu_offline");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_USER_ROBOT_ONLINE) {
            return SobotResourceUtils.getResString(mContext, "online_yonghu_jiqiren_create_chat");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_SOBOT_TRANSFER_ADMIN) {
            return SobotResourceUtils.getResString(mContext, "online_user_zhuanrengong");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_USER_QUEUE) {
            return SobotResourceUtils.getResString(mContext, "online_user_paidui");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_USER_ADMIN_CONNECTED) {
            return SobotResourceUtils.getResString(mContext, "online_user_and_kefu") + receiver + SobotResourceUtils.getResString(mContext, "online_create_chat");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_ADMIN_TRANSFER_ADMIN) {
            return SobotResourceUtils.getResString(mContext, "online_kefu") + sender + SobotResourceUtils.getResString(mContext, "online_user_to_kefu") + receiver;
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_USER_OFFLINE) {
            return SobotResourceUtils.getResString(mContext, "online_user_xiaxian");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_ADD_BLACKLIST) {
            return SobotResourceUtils.getResString(mContext, "online_user_bei_kefu") + sender + SobotResourceUtils.getResString(mContext, "online_lahei");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_DELETE_BLACKLIST) {
            return SobotResourceUtils.getResString(mContext, "online_user_bei_kefu") + sender + SobotResourceUtils.getResString(mContext, "online_remove_lahei");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_ADMIN_BUSY) {
            return SobotResourceUtils.getResString(mContext, "online_admin_busy");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_ADD_MARKLIST) {
            return SobotResourceUtils.getResString(mContext, "online_user_bei_kefu") + sender + SobotResourceUtils.getResString(mContext, "online_add_mark");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_DELETE_MARKLIST) {
            return SobotResourceUtils.getResString(mContext, "online_user_bei_kefu") + sender + SobotResourceUtils.getResString(mContext, "online_remove_mark");
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_EVALUATE && item.getMessage() != null) {
            return item.getMessage().getContent() == null ? "" : item.getMessage().getContent() + "";
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_HISTORY_EMPTY) {
        }
        if (item.getAction() == OnlineConstant.MSG_TYPE_HISTORY_AUTO_REPLY) {
            return item.getTitle();
        }
        if (item.isRevokeFlag()) {
            CustomerServiceInfoModel admin = (CustomerServiceInfoModel) SobotSPUtils.getInstance().getObject(SOBOT_CUSTOM_USER);
            String mLastCid = SobotSPUtils.getInstance().getString("lastCid");
            String tipStr = SobotResourceUtils.getResString(mContext, "custom_retracted_msg_tip");
            String sensitiveStr = SobotResourceUtils.getResString(mContext, "custom_retracted_sensitive");
            if (!TextUtils.isEmpty(item.getSender()) && !TextUtils.isEmpty(item.getCid()) && admin != null
                    && item.getSender().equals(admin.getAid()) && item.getCid().equals(mLastCid) && item.getIsHistory() != 0) {
                String str = String.format(tipStr, TextUtils.isEmpty(item.getSenderName()) ? "" : item.getSenderName());
                //撤回的时候只有文本  添加  重新编辑
                if (item.getMsgType() == 0) {
                    str = str + " <font  color=\"" + SobotResourceUtils.getColorById(mContext, "sobot_chat_remind_link_color") + "\">   " + SobotResourceUtils.getResString(mContext, "sobot_re_edit") + "</font>";
                } else if (item.getMsgType() == MessageType.MESSAGE_TYPE_SENSITIVE) {
                    str = sensitiveStr + " <font   color=\"" + SobotResourceUtils.getColorById(mContext, "sobot_chat_remind_link_color") + "\">   " + SobotResourceUtils.getResString(mContext, "sobot_re_edit") + "</font>";
                }
                TextView remindText = viewHolder.getView(SobotResourceUtils.getResId(mContext, "tv_msg_item_remind_content"));
                if (remindText != null) {
                    remindText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (item.getMessage() == null || item.getMessage().getContent() == null) {
                                return;
                            }
                            Intent intent = new Intent();
                            intent.setAction(SobotSocketConstant.BROADCAST_SOBOT_REVOKE_MESSAGE);/* 发送撤回的消息重新编辑广播 */
                            intent.putExtra("message_revoke", TextUtils.isEmpty((String) item.getMessage().getContent()) ? "" : (String) item.getMessage().getContent());
                            SobotUtils.getApp().sendBroadcast(intent);
                        }
                    });
                }
                return str;
            } else {
                return String.format(tipStr,
                        TextUtils.isEmpty(item.getSenderName()) ? "" : item.getSenderName());
            }
        }

        //时间提醒
        if (item.getAction() != OnlineConstant.MSG_TYPE_HISTORY_EMPTY && item.getAction() != OnlineConstant.MSG_TYPE_USER_OFFLINE) {
            return item.getTs();
        }

        if (item.getAction() == OnlineConstant.MSG_TYPE_ADMIN_CONNECTED_USER) {
            return SobotResourceUtils.getResString(mContext, "online_kefu") + receiver + SobotResourceUtils.getResString(mContext, "online_zhudong_create_chat");
        }
        return "";
    }


    private void resetAnim(ImageView ivAudioPlay) {
        ivAudioPlay.setImageResource(
                SobotResourceUtils.getIdByName(mContext, "drawable", "sobot_voice_from_icon"));
        Drawable playDrawable = ivAudioPlay.getDrawable();
        if (playDrawable != null
                && playDrawable instanceof AnimationDrawable) {
            ((AnimationDrawable) playDrawable).start();
        }
    }

    /**
     * 显示重新发送dialog
     */
    public static void showReSendDialog(Context context, final ChatMessageModel messageModel, final ProgressBar sendLoadingPB, final ImageView sendStatusIV, final OnlineMsgCallBack mMsgCallBack) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int widths = 0;
        if (width == 480) {
            widths = 80;
        } else {
            widths = 120;
        }
        final OnlineReSendDialog reSendDialog = new OnlineReSendDialog(context);
        reSendDialog.setOnClickListener(new OnlineReSendDialog.OnItemClick() {
            @Override
            public void OnClick(int type) {
                if (type == 0) {// 0：确定 1：取消
                    sendLoadingPB.setVisibility(View.VISIBLE);
                    sendStatusIV.setVisibility(View.GONE);
                    mMsgCallBack.reSendMsg(messageModel, sendStatusIV);
                }
                reSendDialog.dismiss();
            }
        });
        reSendDialog.show();
        sendStatusIV.setClickable(true);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        if (reSendDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = reSendDialog.getWindow().getAttributes();
            lp.width = (int) (display.getWidth() - widths); // 设置宽度
            reSendDialog.getWindow().setAttributes(lp);
        }
    }


    public interface OnlineMsgCallBack {
        //播放语音
        void clickAudioItem(ChatMessageModel message, ImageView ivAudioPlay);

        //重发消息
        void reSendMsg(ChatMessageModel message, ImageView sendStatusIV);
    }

    //左右两边气泡内链接文字的字体颜色
    protected int getLinkTextColor(boolean isLeft) {
        if (isLeft) {
            return SobotResourceUtils.getIdByName(mContext, "color", "sobot_color_link");
        } else {
            return SobotResourceUtils.getIdByName(mContext, "color", "sobot_color_rlink");
        }
    }

    private void showPopWindows(View v, final ChatMessageModel chatMsg, final int posi, boolean isRight) {
        SobotOnlyRetractDialog onlyRetractDialog = SobotOnlyRetractDialog.newInstance(mContext, retractOk(chatMsg) ? 2 : 3, isRight);
        onlyRetractDialog.setRetractListener(new SobotOnlyRetractDialog.RetractListener() {
            @Override
            public void onRetractClickL(View v, int whichBtn) {
                switch (whichBtn) {
                    case 1:
                        if (chatMsg.getMessage().getContent() != null) {
                            ClipboardManager cmb = (ClipboardManager) mContext
                                    .getApplicationContext().getSystemService(
                                            Context.CLIPBOARD_SERVICE);
                            cmb.setText((String) chatMsg.getMessage().getContent());
                            cmb.getText();
                            SobotToastUtil.showCustomToast(mContext, "复制成功!");
                        }
                        break;
                    case 2:
                        Bundle bundle = new Bundle();
                        bundle.putString("content", chatMsg.getMsg());
                        //进入智能回复界面
                        Intent intent = new Intent(mContext, SobotIntelligenceReplyActivity.class);
                        // mContext.startActivity(intent, OnlineConstant.SOBOT_REQUEST_CODE_INTELLIGENCE_REPLY);
                        break;
                    case 3:
                        revokeMsg(mUserInfoModel, mContext, SobotOnlineMsgAdapter.this, chatMsg, posi);
                        break;
                }
            }
        });
        onlyRetractDialog.showPopWindow(v);
    }


    /**
     * 判断是否显示撤回按钮
     *
     * @param chatMsg
     * @return
     */
    private boolean retractOk(ChatMessageModel chatMsg) {
        return ((2 == chatMsg.getSenderType() && !TextUtils.isEmpty(chatMsg.getSender()) &&
                chatMsg.getSender().equals(admin.getAid()) && 1 == chatMsg.getIsSendOk() && isPastTowMinute(chatMsg)) || (1 == chatMsg.getUserNoSeeFlag() && isPastTowMinute(chatMsg)));
    }

    /**
     * 判断发送成功的消息是否超过2分钟
     *
     * @param chatMsg
     * @return
     */
    private boolean isPastTowMinute(ChatMessageModel chatMsg) {

        boolean isPastTowMinute;
        try {
            long t = Long.parseLong(chatMsg.getT());
            long time = System.currentTimeMillis() - t;
            long mill = (long) Math.ceil(time / 1000);
            if (mill < 120) {
                isPastTowMinute = true;
            } else {
                isPastTowMinute = false;
            }
        } catch (Exception e) {
            isPastTowMinute = false;
        }
        return isPastTowMinute;
    }

    /**
     * 消息撤回
     *
     * @param userInfo 需要用户id
     * @param context  上下文对象
     * @param adapter
     * @param chatMsg  当前消息
     */
    public static void revokeMsg(HistoryUserInfoModel userInfo, final Context context, final SobotOnlineMsgAdapter adapter, final ChatMessageModel chatMsg, final int posi) {
        ZhiChiOnlineApi zhiChiApi = ZhiChiOnlineApiFactory.createZhiChiApi(context);
        zhiChiApi.revokeMsg(context, chatMsg.getMsgId(),
                userInfo.getId(), chatMsg.getCid(), new SobotResultCallBack<OnlineCommonModel>() {
                    @Override
                    public void onSuccess(OnlineCommonModel onlineCommonModel) {
                        if ("1".equals(onlineCommonModel.getStatus())) {
                            chatMsg.setRevokeFlag(1);
                            if (1 == chatMsg.getUserNoSeeFlag()) {
                                adapter.getList().remove(posi + 1);
                            }
                            adapter.notifyDataSetChanged();
                        } else if ("2".equals(onlineCommonModel.getStatus())) {
                            SobotToastUtil.showCustomToast(context, "用户端不支持");
                        } else if ("3".equals(onlineCommonModel.getStatus())) {
                            SobotToastUtil.showCustomToast(context, "发送时间超过2分钟的消息，不能被撤回");
                        } else if ("4".equals(onlineCommonModel.getStatus())) {
                            SobotToastUtil.showCustomToast(context, "未找到对应的消息");
                        } else if ("5".equals(onlineCommonModel.getStatus())) {
                            SobotToastUtil.showCustomToast(context, "没有开启消息撤回功能");
                        }
                    }

                    @Override
                    public void onFailure(Exception e, String des) {

                    }
                });
    }

    //跳转到webivew中
    public void startWebview(View view, final String url) {
        if (view == null)
            return;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, OnlineWebViewActivity.class);
                intent.putExtra("url", url);
                mContext.startActivity(intent);
            }
        });
    }

    //跳转到地图
    public void startMap(View view, final ChatMessageLocationModel locationModel) {
        if (view == null)
            return;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SobotMapOpenHelper.openMap(mContext, locationModel);
            }
        });
    }

    private void setUserHead(String face, Context context, ImageView head_avator) {
        if (!TextUtils.isEmpty(face)) {
            SobotBitmapUtil.display(mContext, face, head_avator);
            return;
        }
        SobotBitmapUtil.display(mContext, getUserAvatorWithSource(context, mUserSource, true), head_avator);
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
