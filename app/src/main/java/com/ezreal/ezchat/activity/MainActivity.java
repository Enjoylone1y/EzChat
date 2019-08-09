package com.ezreal.ezchat.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.ezreal.ezchat.R;
import com.ezreal.ezchat.fragment.BaseFragment;
import com.ezreal.ezchat.fragment.ContractFragment;
import com.ezreal.ezchat.fragment.MeFragment;
import com.ezreal.ezchat.fragment.RecentMsgFragment;
import com.ezreal.ezchat.handler.NimFriendHandler;
import com.ezreal.ezchat.handler.NimOnlineStatusHandler;
import com.ezreal.ezchat.handler.NimSysMsgHandler;
import com.ezreal.ezchat.handler.NimUserHandler;
import com.ezreal.ezchat.widget.ChangeColorIconWithText;
import com.javonlee.dragpointview.view.DragPointView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.suntek.commonlibrary.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity implements OnClickListener,
        ViewPager.OnPageChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.view_page)
    ViewPager mViewPager;
    @BindView(R.id.id_indicator_msg)
    ChangeColorIconWithText mIndicatorMsg;
    @BindView(R.id.id_indicator_contact)
    ChangeColorIconWithText mIndicatorContract;
//    @BindView(R.id.id_indicator_found)
//    ChangeColorIconWithText mIndicatorFound;
    @BindView(R.id.id_indicator_me)
    ChangeColorIconWithText mIndicatorMe;
    @BindView(R.id.dpv_unread_recent_msg)
    DragPointView mDpvUnRead;

    private List<BaseFragment> mFragments;
    private List<ChangeColorIconWithText> mTabIndicators = new ArrayList<>();
    private RecentMsgFragment mMsgFragment;
    private ContractFragment mContractFragment;
   // private FoundFragment mFoundFragment;
    private MeFragment mMeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_main);
        setTitleBar(getString(R.string.app_name),false,false);
        ButterKnife.bind(this);
        initView();
        bindFragment();
        initHandler();

        // 开启通知栏，有信息的时候通知通知
        NIMClient.toggleNotification(true);

        // 由通知栏点击进入后，用于跳转到指定的聊天界面
//        ArrayList<IMMessage> messages = (ArrayList<IMMessage>)
//                getIntent().getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
//        if (messages != null && !messages.isEmpty()){
//            IMMessage message = messages.get(0);
//            NimUserInfo userInfo = NIMClient.getService(UserService.class)
//                    .getUserInfo(message.getSessionId());
//            Intent intent = new Intent(this,P2PChatActivity.class);
//            intent.putExtra("NimUserInfo",userInfo);
//            startActivity(intent);
//        }

    }

    private void initView() {
        mTabIndicators.add(mIndicatorMsg);
        mTabIndicators.add(mIndicatorContract);
//        mTabIndicators.add(mIndicatorFound);
        mTabIndicators.add(mIndicatorMe);
        mIndicatorMsg.setOnClickListener(this);
        mIndicatorContract.setOnClickListener(this);
//        mIndicatorFound.setOnClickListener(this);
        mIndicatorMe.setOnClickListener(this);
        mIndicatorMsg.setIconAlpha(1.0f);
    }

    private void bindFragment() {
        mFragments = new ArrayList<>();
        mMsgFragment = new RecentMsgFragment();
        mFragments.add(mMsgFragment);
        mContractFragment = new ContractFragment();
        mFragments.add(mContractFragment);
//        mFoundFragment = new FoundFragment();
//        mFragments.add(mFoundFragment);
        mMeFragment = new MeFragment();
        mFragments.add(mMeFragment);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }
        };
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);
    }

    private void initHandler() {
        NimOnlineStatusHandler.getInstance().init();
        NimOnlineStatusHandler.getInstance().setStatusChangeListener(
                new NimOnlineStatusHandler.OnStatusChangeListener() {
            @Override
            public void requestReLogin(String message) {
                ToastUtils.showMessage(MainActivity.this,"自动登陆失败或被踢出，请手动登陆~");
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }

            @Override
            public void networkBroken() {

            }
        });

        NimSysMsgHandler.getInstance().init();
        NimFriendHandler.getInstance().init();
        NimUserHandler.getInstance().init();
    }

    @Override
    public void onClick(View v) {
        resetOtherTabs();
        switch (v.getId()) {
            case R.id.id_indicator_msg:
                mIndicatorMsg.setIconAlpha(1.0f);
                mViewPager.setCurrentItem(0, false);
                break;
            case R.id.id_indicator_contact:
                mIndicatorContract.setIconAlpha(1.0f);
                mViewPager.setCurrentItem(1, false);
                break;
//            case R.id.id_indicator_found:
//                mIndicatorFound.setIconAlpha(1.0f);
//                mViewPager.setCurrentItem(2, false);
//                break;
            case R.id.id_indicator_me:
                mIndicatorMe.setIconAlpha(1.0f);
                mViewPager.setCurrentItem(2, false);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void resetOtherTabs() {
        for (int i = 0; i < mTabIndicators.size(); i++) {
            mTabIndicators.get(i).setIconAlpha(0);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
        if (positionOffset > 0) {
            ChangeColorIconWithText left = mTabIndicators.get(position);
            ChangeColorIconWithText right = mTabIndicators.get(position + 1);
            left.setIconAlpha(1 - positionOffset);
            right.setIconAlpha(positionOffset);
        }
    }

    @Override
    public void onPageSelected(int position) {
        // 切换到最近联系人列表界面
        if (position == 0){
            // 能看到新消息提醒，不需要通知栏通知
            NIMClient.getService(MsgService.class)
                    .setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL,
                            SessionTypeEnum.None);
        } else {
            // 不能看到消息提醒，需要通知栏通知
            NIMClient.getService(MsgService.class)
                    .setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE,
                            SessionTypeEnum.None);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
