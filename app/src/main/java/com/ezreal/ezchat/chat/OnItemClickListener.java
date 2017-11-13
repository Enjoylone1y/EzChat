package com.ezreal.ezchat.chat;

import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * Created by wudeng on 2017/8/30.
 */

public interface OnItemClickListener {
    void onItemClick(RViewHolder holder, IMMessage message);
}
