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


    protected RecordDialogManager(Context context){
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    protected void showDialogRecord(){
        View view = mInflater.inflate(R.layout.dialog_audio_record_button,null);
        mDialog = new Dialog(mContext,R.style.Theme_Audio_Record_Button);
        mDialog.setContentView(view);
        mIvRecord =  mDialog.findViewById(R.id.iv_record);
        mIvVoiceLevel = mDialog.findViewById(R.id.iv_voice_level);
        mTvTip =  mDialog.findViewById(R.id.tv_dialog_tip);
        mDialog.show();
    }

    protected void showRecording(){
        if (mDialog != null && mDialog.isShowing()){
            mIvRecord.setImageResource(R.drawable.recorder);
            mIvVoiceLevel.setVisibility(View.VISIBLE);
            mTvTip.setText(mContext.getString(R.string.move_up_cancel));
        }
    }

    protected void showDialogToShort(){
        if (mDialog != null && mDialog.isShowing()){
            mIvRecord.setImageResource(R.drawable.voice_to_short);
            mIvVoiceLevel.setVisibility(View.GONE);
            mTvTip.setText(mContext.getString(R.string.record_to_short));
        }
    }

    protected void showDialogWantCancel(){
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
    protected void updateVoiceLevel(int level){
        if (mDialog != null && mDialog.isShowing()){
            int resId = mContext.getResources().getIdentifier("v"+level,
                    "drawable",mContext.getPackageName());
            mIvVoiceLevel.setImageResource(resId);
        }
    }

    protected void dismissDialog(){
        if (mDialog != null){
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
