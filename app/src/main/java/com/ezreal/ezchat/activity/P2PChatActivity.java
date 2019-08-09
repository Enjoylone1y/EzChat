package com.ezreal.ezchat.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.amap.api.services.core.LatLonPoint;
import com.ezreal.audiorecordbutton.AudioPlayManager;
import com.ezreal.ezchat.R;
import com.ezreal.ezchat.camera.CameraActivity;
import com.ezreal.ezchat.chat.AudioPlayHandler;
import com.ezreal.ezchat.chat.ChatMsgHandler;
import com.ezreal.ezchat.chat.ChatSession;
import com.ezreal.ezchat.chat.MessageListAdapter;
import com.ezreal.ezchat.chat.OnItemClickListener;
import com.ezreal.ezchat.chat.RViewHolder;
import com.ezreal.ezchat.handler.NimUserHandler;
import com.ezreal.ezchat.widget.ChatInputLayout;
import com.ezreal.ezchat.widget.MsgRecyclerView;
import com.ezreal.photoselector.PhotoSelectActivity;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.suntek.commonlibrary.utils.TextUtils;
import com.suntek.commonlibrary.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 点对点聊天界面
 * Created by wudeng on 2017/9/4.
 */

public class P2PChatActivity extends BaseActivity
        implements ChatMsgHandler.OnLoadMsgListener, ChatInputLayout.OnInputLayoutListener {

    private static final String TAG = P2PChatActivity.class.getSimpleName();

    private static final long TEN_MINUTE = 1000 * 60 * 10;

    private static final int SELECT_PHOTO = 0x6001;
    private static final int START_CAMERA = 0x6002;
    private static final int SELECT_LOCATION = 0x6003;

    @BindView(R.id.rcv_msg_list)
    MsgRecyclerView mRecyclerView;
    @BindView(R.id.input_layout)
    ChatInputLayout mInputLayout;

    private ChatMsgHandler mChatHandler;
    private ChatSession mChatSession;
    private LinearLayoutManager mLayoutManager;
    private List<IMMessage> mMsgList;
    private MessageListAdapter mListAdapter;
    private Observer<List<IMMessage>> mMsgReceiveObserver;
    private Observer<IMMessage> mMsgStatusObserver;
    private Observer<AttachmentProgress> mProgressObserver;

    private AudioPlayHandler mAudioPlayHandler;
    private boolean isAudioPlay = false;
    private String mPlayId = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_p2p_chat);
        createChatSession();
        initTitle();
        ButterKnife.bind(this);

        mInputLayout.setLayoutListener(this);
        mInputLayout.bindInputLayout(this, mRecyclerView);

        initMsgList();
        initListener();

        // 注册监听
        NIMClient.getService(MsgServiceObserve.class)
                .observeReceiveMessage(mMsgReceiveObserver, true);
        NIMClient.getService(MsgServiceObserve.class)
                .observeMsgStatus(mMsgStatusObserver, true);
        NIMClient.getService(MsgServiceObserve.class)
                .observeAttachmentProgress(mProgressObserver, true);

        // 加载历史消息数据
        loadMessage();
    }

    /**
     * 初始化 当前聊天会话
     */
    private void createChatSession() {
        NimUserInfo chatInfo = (NimUserInfo) getIntent().getSerializableExtra("NimUserInfo");
        NimUserInfo myInfo = NimUserHandler.getInstance().getUserInfo();
        mChatSession = new ChatSession();
        mChatSession.setSessionId(chatInfo.getAccount());
        mChatSession.setSessionType(SessionTypeEnum.P2P);
        mChatSession.setChatAccount(chatInfo.getAccount());
        mChatSession.setMyAccount(myInfo.getAccount());
        mChatSession.setChatInfo(chatInfo);
        mChatSession.setMyInfo(myInfo);

        mChatHandler = new ChatMsgHandler(this, mChatSession);
    }

    /**
     * 初始化标题栏
     */
    private void initTitle() {
        if (!TextUtils.isEmpty(mChatSession.getChatNick())) {
            setTitleBar(mChatSession.getChatNick(), true, true);
        } else {
            setTitleBar(mChatSession.getChatInfo().getName(), true, true);
        }
        mIvMenu.setImageResource(R.mipmap.people);
        // 打开聊天设置界面
        mIvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showMessage(P2PChatActivity.this,"开发中");
            }
        });
    }

    /**
     * 初始化消息列表
     */
    private void initMsgList() {
        mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mMsgList = new ArrayList<>();
        mListAdapter = new MessageListAdapter(this, mMsgList, mChatSession);
        mListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(RViewHolder holder, IMMessage message) {
                switch (message.getMsgType()) {
                    case image:
                        showAttachOnActivity(ShowImageActivity.class, message);
                        break;
                    case audio:
                        playAudio(holder, message);
                        break;
                    case video:
                        showAttachOnActivity(ShowVideoActivity.class, message);
                        break;
                    case location:
                        showAttachOnActivity(ShowLocActivity.class, message);
                        break;
                }
            }
        });

        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLoadingListener(new MsgRecyclerView.OnLoadingListener() {
            @Override
            public void loadPreMessage() {
                loadMessage();
            }
        });
    }


    /*** 播放音频，并监听播放进度，更新页面动画 ***/
    public void playAudio(final RViewHolder holder, final IMMessage message) {

        if (isAudioPlay) {
            // 如果正在播放，那会先关闭当前播放
            AudioPlayManager.pause();
            AudioPlayManager.release();
            mAudioPlayHandler.stopAnimTimer();
            isAudioPlay = false;

            // 如果关闭的是自己,那关闭后就停止执行下面的操作
            if (message.getUuid().equals(mPlayId)) {
                mPlayId = "";
                return;
            }
        }

        if (mAudioPlayHandler == null) {
            mAudioPlayHandler = new AudioPlayHandler();
        }

        AudioAttachment audioAttachment = (AudioAttachment) message.getAttachment();
        if (audioAttachment == null || TextUtils.isEmpty(audioAttachment.getPath())) {
            ToastUtils.showMessage(this, "音频附件失效，播放失败！");
            return;
        }

        final ImageView imageView = holder.getImageView(R.id.iv_audio_sound);
        final boolean isLeft = message.getDirect() == MsgDirectionEnum.In;

        AudioPlayManager.playAudio(this, audioAttachment.getPath(),
                new AudioPlayManager.OnPlayAudioListener() {
                    @Override
                    public void onPlay() {
                        // 启动播放动画
                        isAudioPlay = true;
                        mPlayId = message.getUuid();
                        mAudioPlayHandler.startAudioAnim(imageView, isLeft);
                    }

                    @Override
                    public void onComplete() {
                        isAudioPlay = false;
                        mPlayId = "";
                        mAudioPlayHandler.stopAnimTimer();
                    }

                    @Override
                    public void onError(String message) {
                        isAudioPlay = false;
                        mPlayId = "";
                        mAudioPlayHandler.stopAnimTimer();
                        ToastUtils.showMessage(P2PChatActivity.this, message);
                    }
                });
    }


    /**
     * 根据消息附件内容，跳转至相应界面显示附件
     **/
    private void showAttachOnActivity(Class<?> activity, IMMessage message) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("IMMessage", message);
        startActivity(intent);
    }


    /*** 初始化各类消息监听 *****/
    private void initListener() {

        mRecyclerView.setOnTouchListener(new MyTouchListener());

        final View decorView = getWindow().getDecorView();
        decorView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    private int previousKeyboardHeight = 0;

                    @Override
                    public void onGlobalLayout() {
                        Rect rect = new Rect();
                        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                        int displayHeight = rect.bottom;
                        int height = decorView.getHeight();
                        int keyboardHeight = height - displayHeight;
                        if (previousKeyboardHeight != keyboardHeight) {
                            boolean hide = (double) displayHeight / height > 0.8;
                            if (!hide) {
                                mLayoutManager.scrollToPosition(mMsgList.size());
                            }
                        }
                    }
                });

        // 网易云信消息接收监听
        mMsgReceiveObserver = new Observer<List<IMMessage>>() {
            @Override
            public void onEvent(List<IMMessage> imMessages) {
                // 通过判断，决定是否添加收到消息的时间
                IMMessage imMessage = imMessages.get(0);
                if (mMsgList.isEmpty()) {
                    if (imMessage.getSessionType() == SessionTypeEnum.P2P
                            && imMessage.getSessionId().equals(mChatSession.getChatAccount())) {
                        mMsgList.add(mChatHandler.createTimeMessage(imMessage));
                    }
                } else {
                    IMMessage lastMsg = mMsgList.get(mMsgList.size() - 1);
                    if (imMessage.getSessionType() == SessionTypeEnum.P2P
                            && imMessage.getSessionId().equals(mChatSession.getChatAccount())
                            && imMessage.getTime() - lastMsg.getTime() > TEN_MINUTE) {
                        mMsgList.add(mChatHandler.createTimeMessage(imMessage));
                    }
                }

                // 将收到的消息添加到列表中
                int receiveCount = 0;
                for (IMMessage message : imMessages) {
                    if (message.getSessionType() == SessionTypeEnum.P2P
                            && message.getSessionId().equals(mChatSession.getChatAccount())) {
                        mMsgList.add(message);
                        receiveCount++;
                    }
                }

                if (receiveCount > 0) {
                    mListAdapter.notifyDataSetChanged();

                    // 对于整个 mListAdapter 来说,第0个 item 是 HeadView
                    // 即mMsgList的第 i 条数据，相当于mListAdapter来说是第 i+1 条
                    mLayoutManager.scrollToPosition(mMsgList.size());
                }
            }
        };
        // 网易云信消息状态监听
        mMsgStatusObserver = new Observer<IMMessage>() {
            @Override
            public void onEvent(IMMessage message) {
                // 收到消息状态更新，从后往前更新消息状态
                for (int i = mMsgList.size() - 1; i >= 0; i--) {
                    // 时间 item  UUid  为空
                    if (TextUtils.isEmpty(mMsgList.get(i).getUuid())) {
                        continue;
                    }
                    if (mMsgList.get(i).getUuid().equals(message.getUuid())) {
                        mMsgList.get(i).setStatus(message.getStatus());
                        mMsgList.get(i).setAttachStatus(message.getAttachStatus());

                        // 对于整个 mListAdapter 来说,第0个 item 是 HeadView
                        // 即 mMsgList 的第 i 条数据，相当于mListAdapter来说是第 i+1 条
                        mListAdapter.notifyItemChanged(i + 1);
                        break;
                    }
                }
            }
        };

        // 附件传输进度监听
        mProgressObserver = new Observer<AttachmentProgress>() {
            @Override
            public void onEvent(AttachmentProgress progress) {

            }
        };
    }


    /*** 消息加载与加载回调 ***/

    private void loadMessage() {
        if (mMsgList.isEmpty()) {
            // 记录为空时，以当前时间为锚点
            IMMessage anchorMessage = MessageBuilder.createEmptyMessage(mChatSession.getSessionId(),
                    mChatSession.getSessionType(), System.currentTimeMillis());
            mChatHandler.loadMessage(anchorMessage, this);
        } else {
            // 否则，以最上一条消息为锚点
            IMMessage firstMsg = mMsgList.get(0);
            if (TextUtils.isEmpty(firstMsg.getUuid())) {
                firstMsg = mMsgList.get(1);
            }
            mChatHandler.loadMessage(firstMsg, this);
        }
    }

    @Override
    public void loadSuccess(List<IMMessage> messages, IMMessage anchorMessage) {
        mRecyclerView.hideHeadView();

        boolean scroll = false;
        // 如果原本没有，为第一次加载，需要在加载完成后移动到最后一项
        if (mMsgList.isEmpty()) {
            scroll = true;
        }
        if (!messages.isEmpty()) {
            mMsgList.addAll(0, mChatHandler.dealLoadMessage(messages, anchorMessage));
            mListAdapter.notifyDataSetChanged();
        }
        if (scroll) {
            mLayoutManager.scrollToPosition(mMsgList.size());
        }
    }


    @Override
    public void loadFail(String message) {
        mRecyclerView.hideHeadView();
        ToastUtils.showMessage(this, "消息加载失败：" + message);
        Log.e(TAG, "load message fail:" + message);
    }

    /******** 页面跳转回调 ********/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO) {
            if (resultCode == RESULT_OK) {
                String[] images = data.getStringArrayExtra("images");
                for (String path : images) {
                    sendMessage(mChatHandler.createImageMessage(path));
                }

            }
        } else if (requestCode == START_CAMERA) {
            if (resultCode == CameraActivity.RESULT_IMAGE) {
                String imagePath = data.getStringExtra("imagePath");
                sendMessage(mChatHandler.createImageMessage(imagePath));

            } else if (resultCode == CameraActivity.RESULT_VIDEO) {
                String videoPath = data.getStringExtra("videoPath");
                sendMessage(mChatHandler.createVideoMessage(videoPath));
            }
        } else if (requestCode == SELECT_LOCATION) {
            if (resultCode == RESULT_OK) {
                String address = data.getStringExtra("address");
                LatLonPoint loc = data.getParcelableExtra("location");
                sendMessage(mChatHandler.createLocMessage(loc, address));
            }
        }
    }

    /*** 消息发送 ***/
    private void sendMessage(IMMessage message) {

        if (mMsgList.isEmpty() ||
                message.getTime() - mMsgList.get(mMsgList.size() - 1).getTime() > TEN_MINUTE) {
            mMsgList.add(mChatHandler.createTimeMessage(message));
        }

        // 将新消息加入列表并刷新界面
        mMsgList.add(message);
        mListAdapter.notifyItemInserted(mMsgList.size());
        mLayoutManager.scrollToPosition(mMsgList.size());
        // 发送消息并监听消息发送状态
        NIMClient.getService(MsgService.class).sendMessage(message, false);
    }


    /*** Activity 生命周期，注册或注销各类监听 ***/

    @Override
    protected void onResume() {
        super.onResume();
        // 设置当前聊天对象，即如果为mChatPersonAccount用户的消息，则不在通知了进行通知
        NIMClient.getService(MsgService.class).setChattingAccount(mChatSession.getChatAccount(),
                mChatSession.getSessionType());

        AudioPlayManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 当前无聊天对象，需要通知栏提醒
        NIMClient.getService(MsgService.class)
                .setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE,
                        SessionTypeEnum.None);

        AudioPlayManager.pause();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销各类监听事件
        NIMClient.getService(MsgServiceObserve.class)
                .observeReceiveMessage(mMsgReceiveObserver, false);
        NIMClient.getService(MsgServiceObserve.class)
                .observeMsgStatus(mMsgStatusObserver, false);
        NIMClient.getService(MsgServiceObserve.class)
                .observeAttachmentProgress(mProgressObserver, true);

        AudioPlayManager.release();
    }

    /********** 输入面板事件回调 *********/

    @Override
    public void sendBtnClick(String textMessage) {
        sendMessage(mChatHandler.createTextMessage(textMessage));
    }

    @Override
    public void photoBtnClick() {
        Intent intent = new Intent(this, PhotoSelectActivity.class);
        startActivityForResult(intent, SELECT_PHOTO);
    }

    @Override
    public void locationBtnClick() {
        Intent intent = new Intent(this, SelectLocActivity.class);
        startActivityForResult(intent, SELECT_LOCATION);
    }

    @Override
    public void cameraBtnClick() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, START_CAMERA);
    }

    @Override
    public void audioRecordFinish(String audioFilePath, long recordTime) {
        sendMessage(mChatHandler.createAudioMessage(audioFilePath, recordTime));
    }

    @Override
    public void audioRecordError(String message) {
        ToastUtils.showMessage(this, "录音出错:" + message);
    }

    @Override
    public void exLayoutShow() {
        mLayoutManager.scrollToPosition(mMsgList.size());
    }

    /***  标题栏返回按钮事件 *****/

    @OnClick(R.id.iv_back_btn)
    public void finishActivity() {
        this.finish();
    }


    /******  消息列表触摸事件   *******/

    private class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mInputLayout.hideOverView();
            }
            return false;
        }
    }

}
