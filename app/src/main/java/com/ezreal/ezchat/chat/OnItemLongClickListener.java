package com.ezreal.ezchat.chat;

import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.suntek.commonlibrary.adapter.RViewHolder;

/**
 * Created by wudeng on 2017/8/30.
 */

public interface OnItemLongClickListener {
    void onItemLongClick(RViewHolder holder, IMMessage message);
}
