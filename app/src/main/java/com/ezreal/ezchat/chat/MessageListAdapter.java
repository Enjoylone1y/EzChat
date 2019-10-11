package com.ezreal.ezchat.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ezreal.emojilibrary.EmojiUtils;
import com.ezreal.ezchat.R;

import com.ezreal.ezchat.activity.FriendInfoActivity;

import com.joooonho.SelectableRoundedImageView;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.suntek.commonlibrary.utils.ImageUtils;
import com.suntek.commonlibrary.utils.TextUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 聊天界面 聊天记录列表 adapter
 * Created by wudeng on 2017/9/13.
 */

public class MessageListAdapter extends RecyclerView.Adapter<RViewHolder> {

    private static final int MSG_TEXT_L = 0x20000;
    private static final int MSG_IMG_L = 0x20001;
    private static final int MSG_AUDIO_L = 0x20002;
    private static final int MSG_VIDEO_L = 0x20003;
    private static final int MSG_LOC_L = 0x20004;

    private static final int MSG_TEXT_R = 0x30000;
    private static final int MSG_IMG_R = 0x30001;
    private static final int MSG_AUDIO_R = 0x30002;
    private static final int MSG_VIDEO_R = 0x30003;
    private static final int MSG_LOC_R = 0x30004;

    private Context mContext;
    private LayoutInflater mInflater;
    private List<IMMessage> mMessageList;
    private SimpleDateFormat mDateFormat;
    private ChatSession mChatSession;
    private ChatUtils mChatUtils;
    private OnItemClickListener mItemClickListener;

    public MessageListAdapter(Context context, List<IMMessage> messages, ChatSession session) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mChatUtils = new ChatUtils(context);
        mMessageList = messages;
        mChatSession = session;
        mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessageList.get(position - 1).getUuid() == null) {
            return R.layout.item_msg_list_time;
        } else {
            return getViewLayoutId(getMsgViewType(mMessageList.get(position - 1).getDirect(),
                    mMessageList.get(position - 1).getMsgType()));
        }
    }

    @Override
    public RViewHolder onCreateViewHolder(ViewGroup parent, int layoutId) {
        View view = mInflater.inflate(layoutId, parent, false);
        return new RViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(RViewHolder holder, int position) {
        if (mMessageList.get(position - 1).getUuid() == null) {
            String time = mDateFormat.format(new Date(mMessageList.get(position - 1).getTime()));
            holder.setText(R.id.tv_msg_time, time);
        } else {
            bindMsgView(holder, mMessageList.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    private void bindMsgView(final RViewHolder holder, final IMMessage message) {

       ImageView headView = holder.getImageView(R.id.iv_head_picture);
        // 设置头像
        if (message.getDirect() == MsgDirectionEnum.In) {

            ImageUtils.setImageByUrl(mContext, headView, mChatSession.getChatInfo().getAvatar(),
                    R.mipmap.app_logo);

            // 设置好友头像点击事件--打开好友信息界面
            headView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, FriendInfoActivity.class);
                    intent.putExtra("NimUserInfo", mChatSession.getChatInfo());
                    intent.putExtra("FLAG",FriendInfoActivity.FLAG_SHOW_FRIEND);
                    mContext.startActivity(intent);
                }
            });

        } else {
            ImageUtils.setImageByUrl(mContext, headView, mChatSession.getMyInfo().getAvatar(),
                    R.mipmap.app_logo);
        }

        // 根据消息状态和附件传输状态决定是否显示progress bar
        if (mChatUtils.isTransferring(message)) {
            holder.setVisible(R.id.progress_status, true);
        } else {
            holder.setVisible(R.id.progress_status, false);
        }

        // 根据类型绑定数据
        int viewType = getMsgViewType(message.getDirect(), message.getMsgType());
        switch (viewType) {

            // 文本
            case MSG_TEXT_L:
            case MSG_TEXT_R:
                TextView textView = holder.getTextView(R.id.tv_chat_msg);
                textView.setText(EmojiUtils.text2Emoji(mContext,message.getContent(),
                        textView.getTextSize()));
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mItemClickListener != null){
                            mItemClickListener.onItemClick(holder,message);
                        }
                    }
                });
                break;

            // 图像
            case MSG_IMG_L:
            case MSG_IMG_R:
                ImageAttachment imageAttachment = (ImageAttachment) message.getAttachment();
                final SelectableRoundedImageView imageView = (SelectableRoundedImageView)
                        holder.getImageView(R.id.iv_msg_img);
                Bitmap bitmap = mChatUtils.getBitmap(imageAttachment);
                if (bitmap != null){
                    imageView.setImageBitmap(bitmap);
                }else {
                    imageView.setImageResource(R.mipmap.bg_img_defalut);
                }
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mItemClickListener != null){
                            mItemClickListener.onItemClick(holder,message);
                        }
                    }
                });
                break;

            // 音频
            case MSG_AUDIO_L:
            case MSG_AUDIO_R:
                AudioAttachment audioAttachment = (AudioAttachment) message.getAttachment();
                holder.setText(R.id.tv_audio_time, mChatUtils.getAudioTime(audioAttachment.getDuration()));
                RelativeLayout layout = holder.getReltiveLayout(R.id.layout_audio_msg);
                mChatUtils.setAudioLayoutWidth(layout, audioAttachment.getDuration());

                holder.getReltiveLayout(R.id.layout_audio_msg)
                        .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mItemClickListener != null){
                            mItemClickListener.onItemClick(holder,message);
                        }
                    }
                });
                break;

            // 视频
            case MSG_VIDEO_L:
            case MSG_VIDEO_R:
                VideoAttachment videoAttachment = (VideoAttachment) message.getAttachment();
                Bitmap videoCover = mChatUtils.getVideoCover(videoAttachment);
                SelectableRoundedImageView roundedImageView =
                        (SelectableRoundedImageView) holder.getImageView(R.id.iv_video_cover);
                if (videoCover != null) {
                    roundedImageView.setImageBitmap(videoCover);
                } else {
                    roundedImageView.setImageResource(R.mipmap.bg_img_defalut);
                }
                ImageView play = holder.getImageView(R.id.iv_btn_play);
                if (mChatUtils.isTransferring(message)) {
                    play.setVisibility(View.GONE);
                } else {
                    play.setVisibility(View.VISIBLE);
                    play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mItemClickListener != null){
                                mItemClickListener.onItemClick(holder,message);
                            }
                        }
                    });
                }
                holder.setText(R.id.tv_video_time,
                        mChatUtils.getVideoTime(videoAttachment.getDuration()));
                break;

            // 位置
            case MSG_LOC_L:
            case MSG_LOC_R:
                LocationAttachment locationAttachment = (LocationAttachment) message.getAttachment();
                holder.setText(R.id.tv_loc_address,locationAttachment.getAddress());
                holder.getTextView(R.id.tv_show_loc).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mItemClickListener != null){
                            mItemClickListener.onItemClick(holder,message);
                        }
                    }
                });
                break;

        }
    }


    private int getViewLayoutId(int viewType) {
        switch (viewType) {
            // 收到的消息
            case MSG_TEXT_L:
                return R.layout.item_msg_text_left;
            case MSG_IMG_L:
                return R.layout.item_msg_img_left;
            case MSG_AUDIO_L:
                return R.layout.item_msg_audio_left;
            case MSG_VIDEO_L:
                return R.layout.item_msg_video_left;
            case MSG_LOC_L:
                return R.layout.item_msg_loc_left;

            // 发出的消息
            case MSG_TEXT_R:
                return R.layout.item_msg_text_right;
            case MSG_IMG_R:
                return R.layout.item_msg_img_right;
            case MSG_AUDIO_R:
                return R.layout.item_msg_audio_right;
            case MSG_VIDEO_R:
                return R.layout.item_msg_video_right;
            case MSG_LOC_R:
                return R.layout.item_msg_loc_right;

            // 其他消息
            default:
                return R.layout.item_msg_list_time;

        }
    }

    private int getMsgViewType(MsgDirectionEnum direct, MsgTypeEnum type) {

        // 收到的消息，头像显示在 left
        if (direct == MsgDirectionEnum.In) {
            if (type == MsgTypeEnum.text) {
                return MSG_TEXT_L;
            } else if (type == MsgTypeEnum.image) {
                return MSG_IMG_L;
            } else if (type == MsgTypeEnum.audio) {
                return MSG_AUDIO_L;
            } else if (type == MsgTypeEnum.video) {
                return MSG_VIDEO_L;
            } else if (type == MsgTypeEnum.location) {
                return MSG_LOC_L;
            } else {
                return 0;
            }
        } else { // 发出的消息,头像显示在右边
            if (type == MsgTypeEnum.text) {
                return MSG_TEXT_R;
            } else if (type == MsgTypeEnum.image) {
                return MSG_IMG_R;
            } else if (type == MsgTypeEnum.audio) {
                return MSG_AUDIO_R;
            } else if (type == MsgTypeEnum.video) {
                return MSG_VIDEO_R;
            } else if (type == MsgTypeEnum.location) {
                return MSG_LOC_R;
            } else {
                return 0;
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mItemClickListener = listener;
    }

}
