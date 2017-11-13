package com.ezreal.audiorecordbutton;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.media.PlaybackParams;

/**
 * Created by wudeng on 2017/10/30.
 */

public class AudioPlayManager {

    private static MediaPlayer sMediaPlayer;
    private static boolean isPause;
    private static AudioManager sAudioManager;

    /**
     * 播放音频文件
     *
     * @param path     音频文件路径
     * @param listener 播放监听器
     */
    public static void playAudio(Context context, final String path, final OnPlayAudioListener listener) {

        if (sAudioManager == null) {
            sAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        assert sAudioManager != null;
        sAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        if (sMediaPlayer == null) {
            sMediaPlayer = new MediaPlayer();
        } else {
            if (sMediaPlayer.isPlaying()) {
                sMediaPlayer.stop();
            }
            sMediaPlayer.reset();
        }

        sMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                if (listener != null) {
                    listener.onPlay();
                }
            }
        });

        sMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (listener != null) {
                    listener.onError("播放出错,错误码:" + what);
                }
                return false;
            }
        });

        sMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (listener != null) {
                    listener.onComplete();
                }
            }
        });

        try {
            int focus = sAudioManager.requestAudioFocus(null,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                sMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                sMediaPlayer.setDataSource(path);
                sMediaPlayer.prepare();
            } else if (focus == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                if (listener != null) {
                    listener.onError("播放出错:" + "AUDIOFOCUS_REQUEST_FAILED");
                }
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onError("播放出错:" + e.getMessage());
            }
            release();
        }

    }

    /**
     * 暂停播放
     * 请在 context 生命周期 onPause 中调用
     * 可在需要的地方手动调用
     */
    public static void pause() {
        if (sMediaPlayer != null && sMediaPlayer.isPlaying()) { //正在播放的时候
            sMediaPlayer.pause();
            isPause = true;
        }

        if (sAudioManager != null) {
            sAudioManager.abandonAudioFocus(null);
        }
    }

    /**
     * 恢复播放
     * 请在 context 生命周期 onResume 中调用
     * 可在需要的地方手动调用
     */
    public static void resume() {
        if (sMediaPlayer != null && isPause) {
            if (sAudioManager != null) {
                int focus = sAudioManager.requestAudioFocus(null,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                if (focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    sMediaPlayer.start();
                    isPause = false;
                }
            }

        }
    }

    /**
     * 释放资源
     * 请在 context 生命周期 onDestroy 中调用
     * 可在需要的地方手动调用
     */
    public static void release() {
        if (sMediaPlayer != null) {
            sMediaPlayer.release();
            sMediaPlayer = null;
        }

        if (sAudioManager != null) {
            sAudioManager.abandonAudioFocus(null);
            sAudioManager = null;
        }
    }


    public interface OnPlayAudioListener {
        void onPlay();

        void onComplete();

        void onError(String message);
    }
}
