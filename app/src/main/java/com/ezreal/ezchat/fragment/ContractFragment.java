package com.ezreal.ezchat.fragment;

import android.content.Intent;

import android.view.View;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ezreal.ezchat.R;
import com.ezreal.ezchat.activity.FriendInfoActivity;
import com.ezreal.ezchat.activity.CheckNotifyListActivity;
import com.ezreal.ezchat.activity.SearchUserActivity;
import com.ezreal.ezchat.handler.NimFriendHandler;
import com.ezreal.ezchat.handler.NimFriendHandler.OnFriendUpdateListener;
import com.ezreal.ezchat.handler.NimSysMsgHandler;
import com.ezreal.ezchat.handler.NimSysMsgHandler.SystemMessageListener;
import com.javonlee.dragpointview.OnPointDragListener;
import com.javonlee.dragpointview.view.AbsDragPointView;
import com.javonlee.dragpointview.view.DragPointView;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.suntek.commonlibrary.adapter.OnItemClickListener;
import com.suntek.commonlibrary.adapter.RViewHolder;
import com.suntek.commonlibrary.adapter.RecycleViewAdapter;
import com.suntek.commonlibrary.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wudeng on 2017/8/28.
 */

public class ContractFragment extends BaseFragment {

    public static final int REQUEST_CHECK_NOTI = 0x5000;

    @BindView(R.id.rcv_friend)
    RecyclerView mRecyclerView;
    @BindView(R.id.dpv_unread_msg)
    DragPointView mDragPointView;
    private List<NimUserInfo> mFriendList;
    private RecycleViewAdapter<NimUserInfo> mViewAdapter;

    @Override
    public int setLayoutID() { return R.layout.fragment_contract; }

    @Override
    public void initView(View rootView) {
        ButterKnife.bind(this,rootView);
        mFriendList = new ArrayList<>();
        mViewAdapter = new RecycleViewAdapter<NimUserInfo>(getContext(), mFriendList) {
            @Override
            public int setItemLayoutId(int position) {
                return R.layout.item_friend;
            }

            @Override
            public void bindView(RViewHolder holder, int position) {
                NimUserInfo item = mFriendList.get(position);
                holder.setImageByUrl(getContext(),R.id.iv_head_picture,
                        item.getAvatar(),R.mipmap.bg_img_defalut);
                holder.setText(R.id.tv_friend_nick,item.getName());
            }
        };
        mViewAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(RViewHolder holder, int position) {
                Intent intent = new Intent(getContext(), FriendInfoActivity.class);
                intent.putExtra("NimUserInfo", mFriendList.get(position));
                intent.putExtra("FLAG",FriendInfoActivity.FLAG_SHOW_FRIEND);
                startActivity(intent);
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mViewAdapter);

        mDragPointView.setOnPointDragListener(new OnPointDragListener() {
            @Override
            public void onRemoveStart(AbsDragPointView view) {

            }

            @Override
            public void onRemoveEnd(AbsDragPointView view) {

            }

            @Override
            public void onRecovery(AbsDragPointView view) {

            }
        });

        NimSysMsgHandler.getInstance().setMessageListener(new SystemMessageListener() {
            @Override
            public void addFriendNotify() {
                updateUnReadMsgView(1);
            }
        });

        NimFriendHandler.getInstance().setUpdateListener(new OnFriendUpdateListener() {
            @Override
            public void friendUpdate() {
                loadFriendList();
            }
        });

        loadFriendList();

    }

    private void loadFriendList() {
        mFriendList.clear();
        mFriendList.addAll(NimFriendHandler.getInstance().getFriendInfos());
        mViewAdapter.notifyDataSetChanged();
    }

    /**
     * 更新(若为当前为隐藏则显示)未读验证消息提醒数
     * @param newMsg 新消息数目
     */
    public void updateUnReadMsgView(int newMsg){
        if (mDragPointView.getVisibility() == View.VISIBLE){
            int msg = Integer.parseInt(mDragPointView.getText().toString());
            mDragPointView.setText(String.valueOf(msg + newMsg));
        }else {
            mDragPointView.setText(String.valueOf(newMsg));
            mDragPointView.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 在进入消息提醒列表后，隐藏未读消息提醒
     */
    public void hindUnReadMsgView(){
        mDragPointView.setVisibility(View.GONE);
    }


    @OnClick(R.id.layout_add_friend)
    public void addFriend(){
        startActivity(new Intent(getContext(), SearchUserActivity.class));
    }

    @OnClick(R.id.layout_msg_notify)
    public void openMsgNotifyActivity(){
        hindUnReadMsgView();
        startActivityForResult(new Intent(getContext(),
                CheckNotifyListActivity.class),REQUEST_CHECK_NOTI);
    }

    @OnClick(R.id.layout_group_chat)
    public void openGroupListActivity(){

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_NOTI){
            if (resultCode == CheckNotifyListActivity.RESULT_HAVE_CHANGE){
                loadFriendList();
            }
        }
    }
}
