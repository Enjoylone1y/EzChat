package com.ezreal.audiorecordbutton;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by wudeng on 2017/9/6.
 */

public class AudioRecordManager {

    private static AudioRecordManager instance;
    private MediaRecorder mMediaRecorder;
    private String mAudioDir;
    private String mCurrentFilePath;
    private OnAudioStateListener mStateListener;
    private boolean hasPrepare = false;

    public static AudioRecordManager getInstance(String audioDir){
        if (instance == null){
            synchronized (AudioRecordManager.class){
                if (instance == null){
                    instance = new AudioRecordManager(audioDir);
                }
            }
        }
        return instance;
    }

    public void setAudioStateListener(OnAudioStateListener listener){
        mStateListener = listener;
    }

    public void prepareAudio(){
        try {
            hasPrepare = false;
            File dir = new File(mAudioDir);
            if (!dir.exists()){
                dir.mkdirs();
            }
            mMediaRecorder = new MediaRecorder();
            String fileName = UUID.randomUUID().toString() + ".arm";
            File file = new File(dir,fileName);
            mCurrentFilePath = file.getAbsolutePath();
            // 设置输出路径
            mMediaRecorder.setOutputFile(mCurrentFilePath);
            // 设置音频源,麦克风
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置输出格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            // 设置音频编码
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // 准备
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            hasPrepare = true;

            if (mStateListener != null){
                mStateListener.prepareFinish(mCurrentFilePath);
            }
        } catch (IOException e) {
            if (mStateListener != null){
                mStateListener.prepareError(e.getMessage());
            }
            e.printStackTrace();
        }

    }

    public int getVoiceLevel(int maxLevel){
        try {
            if (hasPrepare){
                // getMaxAmplitude = 0 - 32767
                return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
            }
        }catch (Exception e){
            return 1;
        }
        return 1;
    }

    public void releaseAudio(){
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
        hasPrepare = false;
    }

    public void cancelAudio(){
        releaseAudio();
        if (mCurrentFilePath != null){
            File file = new File(mCurrentFilePath);
            file.delete();
        }
    }
    private AudioRecordManager(String audioDir){
        mAudioDir = audioDir;
    }

    public interface OnAudioStateListener{
        void prepareError(String message);
        void prepareFinish(String audioFilePath);
    }
}
