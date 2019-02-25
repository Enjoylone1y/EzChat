package com.ezreal.ezchat.fragment;


import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.ezreal.ezchat.R;
import com.ezreal.ezchat.activity.P2PChatActivity;
import com.ezreal.ezchat.bean.RecentContactBean;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.suntek.commonlibrary.adapter.OnItemClickListener;
import com.suntek.commonlibrary.adapter.RViewHolder;
import com.suntek.commonlibrary.adapter.RecycleViewAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by wudeng on 2017/8/28.
 */

public class RecentMsgFragment extends BaseFragment {

    private static final String TAG = RecentMsgFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private List<RecentContactBean> mContactList;
    private RecycleViewAdapter<RecentContactBean> mViewAdapter;
    private Observer<List<RecentContact>> mObserver;
    private SimpleDateFormat mDateFormat;


    @Override
    public int setLayoutID() {
        return R.layout.fragment_message;
    }

    @Override
    public void initView(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.rcv_message_list);
        mDateFormat = new SimpleDateFormat("HH:mm");
        initRecyclerView();
        initListener();
        loadRecentList();
        NIMClient.getService(MsgServiceObserve.class).observeRecentContact(mObserver,true);
    }

    private void initRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mContactList = new ArrayList<>();
        mRecyclerView.setLayoutManager(layoutManager);
        mViewAdapter = new RecycleViewAdapter<RecentContactBean>(getContext(),mContactList) {
            @Override
            public int setItemLayoutId(int position) {
                return R.layout.item_recent_msg;
            }

            @Override
            public void bindView(RViewHolder holder, int position) {
                RecentContactBean contactBean= mContactList.get(position);
                UserInfo userInfo = contactBean.getUserInfo();
                if (userInfo != null){
                    mContactList.get(position).setUserInfo(userInfo);
                    holder.setImageByUrl(getContext(),R.id.iv_head_picture,
                            contactBean.getUserInfo().getAvatar(),R.mipmap.bg_img_defalut);
                    holder.setText(R.id.tv_recent_name,contactBean.getUserInfo().getName());
                }else {
                    getUserInfoByAccount(contactBean.getRecentContact().getFromAccount());
                }
                holder.setText(R.id.tv_recent_content,contactBean.getRecentContact().getContent());
                String time = mDateFormat.format(new Date(contactBean.getRecentContact().getTime()));
                holder.setText(R.id.tv_recent_time,time);
            }
        };

        mViewAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(RViewHolder holder, int position) {
                RecentContactBean contactBean = mContactList.get(position);
                Intent intent;
                if (contactBean.getRecentContact().getSessionType() == SessionTypeEnum.P2P){
                    intent = new Intent(getContext(), P2PChatActivity.class);
                    intent.putExtra("NimUserInfo",contactBean.getUserInfo());
                    startActivity(intent);
                }
            }
        });

        mRecyclerView.setAdapter(mViewAdapter);
    }

    private void initListener(){
        mObserver = new Observer<List<RecentContact>>() {
            @Override
            public void onEvent(List<RecentContact> recentContacts) {
                Log.e(TAG,"Observer RecentContact size = " + recentContacts.size());
                if (mContactList.isEmpty()){
                    List<RecentContactBean> contactBeans = createContactBeans(recentContacts);
                    mContactList.addAll(contactBeans);
                    mViewAdapter.notifyDataSetChanged();
                    return;
                }
                for (RecentContact contact : recentContacts){
                    refreshRecentList(contact);
                }
            }
        };
    }

    private void refreshRecentList(RecentContact contact){
        for (int i=0;i<mContactList.size();i++){
            RecentContactBean bean = mContactList.get(i);
            if (bean.getRecentContact().getContactId().equals(contact.getContactId())){
                bean.setRecentContact(contact);
                mViewAdapter.notifyItemChanged(i);
                break;
            }else if (i == mContactList.size()-1){
                // 否则为新的最近会话
                RecentContactBean newBean = new RecentContactBean();
                newBean.setRecentContact(contact);
                newBean.setUserInfo(getUserInfoByAccount(contact.getContactId()));
                mContactList.add(0,newBean);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG,"onPause");

    }

    private void loadRecentList(){
        NIMClient.getService(MsgService.class).queryRecentContacts()
                .setCallback(new RequestCallbackWrapper<List<RecentContact>>() {
            @Override
            public void onResult(int code, List<RecentContact> result, Throwable exception) {
                if (exception != null){
                    Log.e(TAG,"loadRecentList exception = " + exception.getMessage());
                    return;
                }
                if (code != 200){
                    Log.e(TAG,"loadRecentList error code = " + code);
                    return;
                }
                Log.e(TAG,"loadRecentList size = " + result.size());
                List<RecentContactBean> contactBeans = createContactBeans(result);
                mContactList.clear();
                mContactList.addAll(contactBeans);
                mViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private List<RecentContactBean> createContactBeans(List<RecentContact> recentContacts){
        List<RecentContactBean> beanList = new ArrayList<>();
        RecentContactBean bean;
        for (RecentContact contact : recentContacts){
            bean = new RecentContactBean();
            bean.setRecentContact(contact);
            bean.setUserInfo(getUserInfoByAccount(contact.getContactId()));
            beanList.add(bean);
        }
        return beanList;
    }

    private NimUserInfo getUserInfoByAccount(String account){

        return NIMClient.getService(UserService.class).getUserInfo(account);
    }
}
