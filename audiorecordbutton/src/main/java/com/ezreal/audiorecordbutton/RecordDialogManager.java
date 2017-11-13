package com.ezreal.audiorecordbutton;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 录音提示框
 * Created by wudeng on 2017/9/6.
 */

public class RecordDialogManager {

    private Context mContext;
    private Dialog mDialog;
    private LayoutInflater mInflater;
    private ImageView mIvRecord;
    private ImageView mIvVoiceLevel;
    private TextView mTvTip;

    public RecordDialogManager(Context context){
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void showDialogRecord(){
        View view = mInflater.inflate(R.layout.dialog_audio_record_button,null);
        mDialog = new Dialog(mContext,R.style.Theme_Audio_Record_Button);
        mDialog.setContentView(view);
        mIvRecord = (ImageView) mDialog.findViewById(R.id.iv_record);
        mIvVoiceLevel = (ImageView) mDialog.findViewById(R.id.iv_voice_level);
        mTvTip = (TextView) mDialog.findViewById(R.id.tv_dialog_tip);
        mDialog.show();
    }

    public void showRecording(){
        if (mDialog != null && mDialog.isShowing()){
            mIvRecord.setImageResource(R.drawable.recorder);
            mIvVoiceLevel.setVisibility(View.VISIBLE);
            mTvTip.setText(mContext.getString(R.string.move_up_cancel));
        }
    }

    public void showDialogToShort(){
        if (mDialog != null && mDialog.isShowing()){
            mIvRecord.setImageResource(R.drawable.voice_to_short);
            mIvVoiceLevel.setVisibility(View.GONE);
            mTvTip.setText(mContext.getString(R.string.record_to_short));
        }
    }

    public void showDialogWantCancel(){
        if (mDialog != null && mDialog.isShowing()){
            mIvRecord.setImageResource(R.drawable.cancel);
            mIvVoiceLevel.setVisibility(View.GONE);
            mTvTip.setText(mContext.getString(R.string.release_cancel));
        }
    }

    /**
     * 根据音量大小更新 音量图标高度
     * @param level
     */
    public void updateVoiceLevel(int level){
        if (mDialog != null && mDialog.isShowing()){
            int resId = mContext.getResources().getIdentifier("v"+level,
                    "drawable",mContext.getPackageName());
            mIvVoiceLevel.setImageResource(resId);
        }
    }

    public void dismissDialog(){
        if (mDialog != null){
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
