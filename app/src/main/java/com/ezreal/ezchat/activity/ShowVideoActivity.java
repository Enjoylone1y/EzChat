package com.ezreal.ezchat.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.ezreal.ezchat.R;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.suntek.commonlibrary.utils.TextUtils;
import com.suntek.commonlibrary.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by wudeng on 2017/10/30.
 */

public class ShowVideoActivity extends BaseActivity {

    private static final String TAG = ShowImageActivity.class.getSimpleName();
    @BindView(R.id.video_view)
    VideoView mVideoView;
    @BindView(R.id.tv_show_progress)
    TextView mTvProgress;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    private int mCurrentDur = 0;

    private IMMessage mIMMessage;
    private Observer<AttachmentProgress> mProgressObserver;
    private boolean downloading = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (downloading && msg.what == 0x300) {
                String path = ((VideoAttachment) mIMMessage.getAttachment()).getPath();
                if (!TextUtils.isEmpty(path)) {
                    downloading = false;
                    mTvProgress.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    mVideoView.setMediaController(new MediaController(ShowVideoActivity.this));
                    mVideoView.setVideoPath(path);
                    mVideoView.start();
                    mVideoView.requestFocus();
                } else {
                    mHandler.sendEmptyMessageAtTime(0x300, 1000);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_show_video);
        ButterKnife.bind(this);

        mProgressObserver = new Observer<AttachmentProgress>() {
            @Override
            public void onEvent(AttachmentProgress progress) {
                if (downloading && progress.getUuid().equals(mIMMessage.getUuid())) {
                    int present = (int) (progress.getTransferred() /
                            (progress.getTotal() * 1.0f) * 100.0f);
                    String text = "下载中…… " + String.valueOf(present) + "%";
                    mTvProgress.setText(text);
                    if (present > 60) {
                        // mProgressObserver 并不会回调 100% 下载，所以使用检查路径是否存在来判断
                        mHandler.sendEmptyMessageAtTime(0x300, 1000);
                    }
                }
            }
        };

        initVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentDur > 0) {
            mVideoView.seekTo(mCurrentDur);
        } else {
            mVideoView.seekTo(0);
        }
        mVideoView.resume();
        NIMClient.getService(MsgServiceObserve.class)
                .observeAttachmentProgress(mProgressObserver, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            mCurrentDur = mVideoView.getCurrentPosition();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
        NIMClient.getService(MsgServiceObserve.class)
                .observeAttachmentProgress(mProgressObserver, false);
    }

    private void initVideo() {

        mIMMessage = (IMMessage) getIntent().getSerializableExtra("IMMessage");
        if (mIMMessage == null) {
            ToastUtils.showMessage(this, "视频无法播放，请重试~");
            finish();
            return;
        }

        VideoAttachment attachment = (VideoAttachment) mIMMessage.getAttachment();
        if (attachment == null) {
            ToastUtils.showMessage(this, "视频附件为空，无法播放!");
            finish();
            return;
        }

        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
            new AlertDialog.Builder(this)
                    .setTitle("视频未下载或者地址已失效，是否下载？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 下载视频附件
                            dialog.dismiss();
                            downloadVideo();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).setCancelable(false).show();
            return;
        }

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.seekTo(0);
            }
        });

        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setVideoPath(path);
        mVideoView.start();
        mVideoView.requestFocus();
    }

    private void downloadVideo() {
        downloading = true;
        mTvProgress.setVisibility(View.VISIBLE);
        NIMClient.getService(MsgService.class)
                .downloadAttachment(mIMMessage, false);
        mProgressBar.setVisibility(View.VISIBLE);
    }

}
