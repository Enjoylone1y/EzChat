package com.ezreal.ezchat.camera;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ezreal.ezchat.R;
import com.suntek.commonlibrary.utils.ImageUtils;
import com.suntek.commonlibrary.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 结果展示界面
 * Created by wudeng on 2017/9/15.
 */

public class CameraResultActivity extends AppCompatActivity {

    private static final String TAG = CameraResultActivity.class.getSimpleName();

    public static final int FLAG_SHOW_IMG = 0x100;
    public static final int FLAG_SHOW_VIDEO = 0x101;

    public static final int RESULT_OK = 0x200;
    public static final int RESULT_CANCEL = 0x201;
    public static final int RESULT_RESET = 0x202;

    @BindView(R.id.video_view)
    VideoView mVideoView;
    @BindView(R.id.image_view)
    ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全屏幕
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_result);
        ButterKnife.bind(this);
        init();
    }

    private void init(){
        // 重复播放
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });

        Intent intent = getIntent();
        int flags = intent.getIntExtra("FLAG",FLAG_SHOW_IMG);
        if (flags == FLAG_SHOW_IMG){
            mImageView.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
            showImg(intent);
        }else if (flags == FLAG_SHOW_VIDEO){
            mVideoView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            showVideo(intent);
        }
    }

    private void showImg(Intent intent){
        ImageUtils.setImageByFile(this,mImageView,
                intent.getStringExtra("imagePath"),R.mipmap.bg_img_defalut);
    }

    private void showVideo(Intent intent){
        try {
            mVideoView.setVideoPath(intent.getStringExtra("videoPath"));
            mVideoView.start();
            mVideoView.requestFocus();
        }catch (Exception e){
            ToastUtils.showMessage(this,"播放出错:" + e.getMessage());
            this.setResult(RESULT_CANCEL);
            this.finish();
        }

    }

    @OnClick(R.id.iv_ok)
    public void okClick(){
        release();
        this.setResult(RESULT_OK);
        this.finish();
    }

    @OnClick(R.id.iv_reset)
    public void resetClick(){
        release();
        this.setResult(RESULT_RESET);
        this.finish();
    }

    private void release() {
        if (mVideoView.isPlaying()){
            mVideoView.stopPlayback();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView.getVisibility() == View.VISIBLE){
            mVideoView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView.getVisibility() == View.VISIBLE){
            mVideoView.pause();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_BACK){
            release();
            this.setResult(RESULT_CANCEL);
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
