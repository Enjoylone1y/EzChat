package com.ezreal.ezchat.camera;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.UriMatcher;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.ezreal.ezchat.R;
import com.ezreal.ezchat.utils.Constant;
import com.flurgle.camerakit.CameraKit;
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.suntek.commonlibrary.utils.ImageUtils;
import com.suntek.commonlibrary.utils.ToastUtils;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wudeng on 2017/9/14.
 */

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = CameraActivity.class.getSimpleName();

    public static final int RESULT_IMAGE = 0x5001;
    public static final int RESULT_VIDEO = 0x5002;

    private static final int REQUEST_SHOW_IMG = 0x301;
    private static final int REQUEST_SHOW_VIDEO = 0x302;

    @BindView(R.id.camera_view)
    CameraView mCameraView;
    @BindView(R.id.iv_flash_status)
    ImageView mIvFlash;
    @BindView(R.id.pre_record_time)
    NumberProgressBar mProgressBar;
    @BindView(R.id.iv_camera_btn)
    ImageView mIvCamera;
    @BindView(R.id.iv_exit)
    ImageView mIvExit;
    @BindView(R.id.iv_change)
    ImageView mIvChange;
    @BindView(R.id.tv_tip)
    TextView mTvTip;

    private static final int[] FLASH_OPTIONS = {
            CameraKit.Constants.FLASH_OFF,
            CameraKit.Constants.FLASH_ON,
            CameraKit.Constants.FLASH_AUTO
    };

    private static final int[] FACE_OPTIONS = {
            CameraKit.Constants.FACING_BACK,
            CameraKit.Constants.FACING_FRONT,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
            R.drawable.ic_flash_auto,
    };

    private int mCurrentFlash = 0;
    private int mCurrentFace = 0;

    private boolean isRecording ;
    private int mRecordTime;
    private String mVideoPath;
    private String mImagePath;
    private static final int MSG_UPDATE_TIME = 0x1001;

    private Runnable mRecordRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording){
                try {
                    Thread.sleep(100);
                    mRecordTime += 1;
                    mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_TIME:
                    mProgressBar.setProgress(mRecordTime);
                    if (mRecordTime > 100){
                        stopRecord();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全屏幕
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        ButterKnife.bind(this);

        initListener();
    }


    private void initListener(){

        mIvCamera.setOnClickListener(this);
        mIvFlash.setOnClickListener(this);
        mIvExit.setOnClickListener(this);
        mIvChange.setOnClickListener(this);

        // 触发长按，开始录像，开启线程统计时间并更新进度条
        mIvCamera.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startRecord();
                return true;
            }
        });

        mIvCamera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isRecording){
                            stopRecord();
                        }
                        break;
                }
                return false;
            }
        });

        mCameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                if (bitmap != null){
                    try {
                        // 已限制只能竖屏拍照，若宽度比高度大，则说明发生了旋转
                        if (bitmap.getWidth() > bitmap.getHeight()){
                            bitmap = ImageUtils.rotateBitmapByDegree(bitmap,90);
                        }
                        mImagePath = Constant.APP_CACHE_IMAGE
                                + File.separator + System.currentTimeMillis()+".jpeg";
                        ImageUtils.saveBitmap2Jpg(bitmap,mImagePath);
                        Intent intent = new Intent(CameraActivity.this,CameraResultActivity.class);
                        intent.putExtra("imagePath",mImagePath);
                        intent.putExtra("FLAG",CameraResultActivity.FLAG_SHOW_IMG);
                        startActivityForResult(intent,REQUEST_SHOW_IMG);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                if (video != null){
                    mVideoPath = video.getAbsolutePath();
                    Intent intent = new Intent(CameraActivity.this,
                            CameraResultActivity.class);
                    intent.putExtra("videoPath",video.getAbsolutePath());
                    intent.putExtra("FLAG",CameraResultActivity.FLAG_SHOW_VIDEO);
                    startActivityForResult(intent,REQUEST_SHOW_VIDEO);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CameraResultActivity.RESULT_OK){
            if (requestCode == REQUEST_SHOW_IMG){
                // 把图片路径返回
                Intent intent = new Intent();
                intent.putExtra("imagePath",mImagePath);
                this.setResult(RESULT_IMAGE,intent);
                this.finish();

            }else if (requestCode == REQUEST_SHOW_VIDEO){
                Intent intent = new Intent();
                intent.putExtra("videoPath",mVideoPath);
                this.setResult(RESULT_VIDEO,intent);
                this.finish();
            }

        }else if (resultCode == CameraResultActivity.RESULT_RESET){
            if (requestCode == REQUEST_SHOW_VIDEO){
                new File(mVideoPath).delete();
            }
        }else if (resultCode == CameraResultActivity.RESULT_CANCEL){
            if (requestCode == REQUEST_SHOW_VIDEO){
                new File(mVideoPath).delete();
            }
            this.finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_camera_btn:
                mCameraView.captureImage();
                break;
            case R.id.iv_flash_status:
                mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                mIvFlash.setBackgroundResource(FLASH_ICONS[mCurrentFlash]);
                mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                break;
            case R.id.iv_change:
                mCurrentFace = (mCurrentFace + 1) % 2;
                mCameraView.setFacing(FACE_OPTIONS[mCurrentFace]);
                break;
            case R.id.iv_exit:
                finish();
                break;
        }
    }

    private void startRecord(){
        mProgressBar.setProgress(0);
        mProgressBar.setVisibility(View.VISIBLE);
        mTvTip.setVisibility(View.INVISIBLE);
        isRecording = true;
        mIvCamera.setImageResource(R.mipmap.record);
        mCameraView.startRecordingVideo();
        new Thread(mRecordRunnable).start();

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    }


    private void stopRecord(){
        mCameraView.stopRecordingVideo();
        mRecordTime = 0;
        isRecording = false;
        mIvCamera.setImageResource(R.mipmap.capture);
        mProgressBar.setProgress(0);
        mTvTip.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

}
