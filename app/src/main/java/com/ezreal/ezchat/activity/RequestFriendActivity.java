package com.ezreal.ezchat.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.ezreal.ezchat.R;
import com.netease.nimlib.sdk.InvocationFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.suntek.commonlibrary.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wudeng on 2017/8/29.
 */

public class RequestFriendActivity extends BaseActivity {

    @BindView(R.id.et_request_msg)
    EditText mEtRequestMsg;
    private RequestCallback<Void> mRequestCallback;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_request_friend);
        setTitleBar("添加好友",true,false);
        ButterKnife.bind(this);
        initCallBack();
    }

    private void initCallBack(){
        mRequestCallback = new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                if (exception != null){
                    ToastUtils.showMessage(RequestFriendActivity.this,"请求出错,请重试：" + exception.getMessage());
                }else {
                    if (code == 200){
                        ToastUtils.showMessage(RequestFriendActivity.this,"请求已发出~");
                        finish();
                    }else {
                        ToastUtils.showMessage(RequestFriendActivity.this,"请求异常，请重试，异常代码：" + code);
                    }
                }
            }
        };
    }

    @OnClick(R.id.tv_send_request)
    public void sendRequest(){
        String account = getIntent().getStringExtra("account");
        String msg = "";
        if (mEtRequestMsg.getText() != null){
            msg = mEtRequestMsg.getText().toString().trim();
        }
        VerifyType type = VerifyType.VERIFY_REQUEST;
        InvocationFuture<Void> addFriend = NIMClient.getService(FriendService.class)
                .addFriend(new AddFriendData(account, type, msg));
        addFriend.setCallback(mRequestCallback);
    }


}
