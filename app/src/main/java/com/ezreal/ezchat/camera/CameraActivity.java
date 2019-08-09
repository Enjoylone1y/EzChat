package com.ezreal.ezchat.camera;

import android.annotation.SuppressLint;
import android.content.Intent;

import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.ezreal.ezchat.R;
import com.ezreal.ezchat.utils.Constant;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.FileCallback;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Grid;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.controls.VideoCodec;
import com.otaliastudios.cameraview.size.SizeSelectors;
import com.otaliastudios.cameraview.video.encoding.MediaEncoderEngine;

import java.io.File;

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

    private static final Flash[] FLASH_OPTIONS = {
            Flash.OFF,
            Flash.ON,
            Flash.AUTO
    };

    private static final Facing[] FACE_OPTIONS = {
            Facing.BACK,
            Facing.FRONT
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

        mCameraView.setVideoCodec(VideoCodec.H_264);
        mCameraView.setGrid(Grid.DRAW_PHI);
        mCameraView.setPlaySounds(false);
        mCameraView.setVideoBitRate(48000);

        initListener();
    }


    @SuppressLint("ClickableViewAccessibility")
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


        mCameraView.addCameraListener(new CameraListener() {
            @Override
            public void onCameraOpened(@NonNull CameraOptions options) {
                super.onCameraOpened(options);

            }

            @Override
            public void onCameraClosed() {
                super.onCameraClosed();
                Log.i(TAG,"Camera Close");
            }

            @Override
            public void onCameraError(@NonNull CameraException exception) {
                super.onCameraError(exception);
                Log.e(TAG,"Camera Exception :" + exception.getMessage());
                Toast.makeText(CameraActivity.this,
                        "抱歉，相机出了点问题，请稍后重试",Toast.LENGTH_LONG).show();
                if (isRecording){
                    startRecord();
                }
            }

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                mImagePath = Constant.APP_CACHE_IMAGE
                        + File.separator + System.currentTimeMillis()+".jpeg";
                result.toFile(new File(mImagePath), new FileCallback() {
                    @Override
                    public void onFileReady(File file) {
                        Intent intent = new Intent(CameraActivity.this,CameraResultActivity.class);
                        intent.putExtra("imagePath",mImagePath);
                        intent.putExtra("FLAG",CameraResultActivity.FLAG_SHOW_IMG);
                        startActivityForResult(intent,REQUEST_SHOW_IMG);
                    }
                });
            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
                Intent intent = new Intent(CameraActivity.this,
                        CameraResultActivity.class);
                intent.putExtra("videoPath",mVideoPath);
                intent.putExtra("FLAG",CameraResultActivity.FLAG_SHOW_VIDEO);
                startActivityForResult(intent,REQUEST_SHOW_VIDEO);
            }


            @Override
            public void onVideoRecordingStart() {
                super.onVideoRecordingStart();
                Log.i(TAG,"onVideoRecordingStart");
            }


            @Override
            public void onVideoRecordingEnd() {
                super.onVideoRecordingEnd();
                Log.i(TAG,"onVideoRecordingEnd");
            }

            @Override
            public void onOrientationChanged(int orientation) {
                super.onOrientationChanged(orientation);
            }

            @Override
            public void onAutoFocusStart(PointF point) {
                super.onAutoFocusStart(point);
            }

            @Override
            public void onAutoFocusEnd(boolean successful, PointF point) {
                super.onAutoFocusEnd(successful, point);
            }

            @Override
            public void onZoomChanged(float newValue,float[] bounds, PointF[] fingers) {
                super.onZoomChanged(newValue, bounds, fingers);
            }

            @Override
            public void onExposureCorrectionChanged(float newValue, float[] bounds, PointF[] fingers) {
                super.onExposureCorrectionChanged(newValue, bounds, fingers);
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
                mCameraView.takePicture();
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
        mVideoPath = Constant.APP_CACHE_VIDEO + File.separator +  System.currentTimeMillis() + ".mp4";
        File file = new File(mVideoPath);
        isRecording = true;
        mCameraView.setMode(Mode.VIDEO);
        mCameraView.takeVideo(file);
        mProgressBar.setProgress(0);
        mProgressBar.setVisibility(View.VISIBLE);
        mTvTip.setVisibility(View.INVISIBLE);
        mIvCamera.setImageResource(R.mipmap.record);
        new Thread(mRecordRunnable).start();
    }


    private void stopRecord(){
        mCameraView.stopVideo();
        mCameraView.setMode(Mode.PICTURE);
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
        mCameraView.open();
    }

    @Override
    protected void onPause() {
        mCameraView.close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        mCameraView.destroy();
    }

}
