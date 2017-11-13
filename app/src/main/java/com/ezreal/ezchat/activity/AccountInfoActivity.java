package com.ezreal.ezchat.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ezreal.ezchat.R;
import com.ezreal.ezchat.bean.LocalAccountBean;
import com.ezreal.ezchat.handler.NimUserHandler;
import com.ezreal.ezchat.utils.Constant;
import com.ezreal.timeselectview.CityPickerView;
import com.ezreal.timeselectview.TimePickerView;
import com.joooonho.SelectableRoundedImageView;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.suntek.commonlibrary.utils.ImageUtils;
import com.suntek.commonlibrary.utils.TextUtils;
import com.suntek.commonlibrary.utils.ToastUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 账号信息详情页
 * Created by wudeng on 2017/8/31.
 */

public class AccountInfoActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {

    private static final String TAG = AccountInfoActivity.class.getSimpleName();

    @BindView(R.id.layout_head)
    RelativeLayout mLayoutHead;
    @BindView(R.id.iv_head_picture)
    SelectableRoundedImageView mIvHead;
    @BindView(R.id.tv_account)
    TextView mTvAccount;
    @BindView(R.id.et_account_nick)
    EditText mEtNick;
    @BindView(R.id.tv_account_sex)
    TextView mTvSex;
    @BindView(R.id.tv_account_birth)
    TextView mTvBirthDay;
    @BindView(R.id.tv_account_location)
    TextView mTvLocation;
    @BindView(R.id.et_account_signature)
    EditText mEtSignature;
    // 个人信息
    private LocalAccountBean mAccountBean;
    // 头像本地路径
    private String mHeadImgPath = "";
    // 获取图像请求码
    private static final int SELECT_PHOTO = 30000;
    private static final int TAKE_PHOTO = 30001;
    // 信息是否有被更新
    private boolean haveAccountChange = false;
    // 是否处于编辑状态
    private boolean isEditor;
    // 输入服务，用于显示键盘
    private InputMethodManager mInputMethodManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_account_info);
        setTitleBar("个人信息", true, true);
        ButterKnife.bind(this);
        showData();
        init();
    }

    // 显示数据
    private void showData() {
        mAccountBean = NimUserHandler.getInstance().getLocalAccount();
        if (mAccountBean != null) {
            ImageUtils.setImageByFile(this, mIvHead,
                    mAccountBean.getHeadImgUrl(), R.mipmap.bg_img_defalut);
            mTvAccount.setText(mAccountBean.getAccount());
            mEtNick.setText(mAccountBean.getNick());
            if (mAccountBean.getGenderEnum() == GenderEnum.FEMALE) {
                mTvSex.setText("女");
            } else if (mAccountBean.getGenderEnum() == GenderEnum.MALE) {
                mTvSex.setText("男");
            } else {
                mTvSex.setText("保密");
            }
            mEtSignature.setText(mAccountBean.getSignature());
            String birthday = mAccountBean.getBirthDay();
            if (TextUtils.isEmpty(birthday)) {
                mTvBirthDay.setText("未设置");
            } else {
                mTvBirthDay.setText(birthday);
            }
            String location = mAccountBean.getLocation();
            if (TextUtils.isEmpty(location)) {
                mTvLocation.setText("未设置");
            } else {
                mTvLocation.setText(location);
            }
        }
    }

    private void init() {
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // 文字
        mLayoutHead.setOnClickListener(this);
        mTvSex.setOnClickListener(this);
        mTvBirthDay.setOnClickListener(this);
        mTvLocation.setOnClickListener(this);

        // 标题栏
        mIvBack.setOnClickListener(this);
        mIvMenu.setOnClickListener(this);

        // 输入框
        mEtNick.setOnTouchListener(this);
        mEtSignature.setOnTouchListener(this);

        // 结束编辑，相当于初始化为非编辑状态
        finishEdit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_head:
                setHeadImg();
                break;
            case R.id.tv_account_sex:
                setSex();
                break;
            case R.id.tv_account_location:
                setLocation();
                break;
            case R.id.tv_account_birth:
                setBirthday();
                break;
            case R.id.iv_back_btn:
                this.finish();
                break;
            case R.id.iv_menu_btn:
                if (isEditor) {
                    finishEdit();
                } else {
                    startEdit();
                }
                break;
        }
    }

    // EditText 获取焦点并将光标移动到末尾
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isEditor) {
            if (v.getId() == R.id.et_account_nick) {
                mEtNick.requestFocus();
                mEtNick.setSelection(mEtNick.getText().length());
                mInputMethodManager.showSoftInput(mEtNick, 0);
            } else if (v.getId() == R.id.et_account_signature) {
                mEtSignature.requestFocus();
                mEtSignature.setSelection(mEtSignature.getText().length());
                mInputMethodManager.showSoftInput(mEtSignature, 0);
            }
            return true;
        }
        return false;
    }

    /**
     * 启动编辑
     */
    private void startEdit() {
        mIvMenu.setImageResource(R.mipmap.done);
        // 可点击
        mLayoutHead.setClickable(true);
        mTvSex.setClickable(true);
        mTvLocation.setClickable(true);
        mTvBirthDay.setClickable(true);
        // 可编辑
        mEtNick.setFocusable(true);
        mEtNick.setFocusableInTouchMode(true);
        mEtSignature.setFocusable(true);
        mEtSignature.setFocusableInTouchMode(true);

        isEditor = true;
    }

    /**
     * 结束编辑，判断是否有修改，决定是否同步缓存数据
     */
    private void finishEdit() {
        if (!mEtNick.getText().toString()
                .equals(mAccountBean.getNick())) {
            mAccountBean.setNick(mEtNick.getText().toString());
            haveAccountChange = true;
        }

        if (!mEtSignature.getText().toString()
                .equals(mAccountBean.getSignature())) {
            mAccountBean.setSignature(mEtSignature.getText().toString());
            haveAccountChange = true;
        }

        if (haveAccountChange) {

            // 将数据更新到缓存
            NimUserHandler.getInstance().setLocalAccount(mAccountBean);
            // 通知handler将数据更新到服务器
            NimUserHandler.getInstance().syncChange2Service();

            haveAccountChange = false;
        }

        mIvMenu.setImageResource(R.mipmap.editor);
        // 不可点击
        mLayoutHead.setClickable(false);
        mTvSex.setClickable(false);
        mTvLocation.setClickable(false);
        mTvBirthDay.setClickable(false);
        // 不可编辑
        mEtNick.setFocusable(false);
        mEtNick.setFocusableInTouchMode(false);
        mEtSignature.setFocusable(false);
        mEtSignature.setFocusableInTouchMode(false);

        isEditor = false;
    }

    /**
     * 设置性别
     */
    private void setSex(){
        final int[] selected = new int[1];
        if (mAccountBean.getGenderEnum() == GenderEnum.MALE) {
            selected[0] = 0;
        } else if (mAccountBean.getGenderEnum() == GenderEnum.FEMALE) {
            selected[0] = 1;
        } else {
            selected[0] = 2;
        }
        final String[] items = new String[]{"男", "女", "保密"};
        new AlertDialog.Builder(this)
                .setTitle("性别")
                .setSingleChoiceItems(items, selected[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which != selected[0]) {
                            if (which == 0) {
                                mAccountBean.setGenderEnum(GenderEnum.MALE);
                                mTvSex.setText("男");
                            } else if (which == 1) {
                                mAccountBean.setGenderEnum(GenderEnum.FEMALE);
                                mTvSex.setText("女");
                            } else {
                                mAccountBean.setGenderEnum(GenderEnum.UNKNOWN);
                                mTvSex.setText("保密");
                            }
                            haveAccountChange = true;
                        }
                        dialog.dismiss();
                    }
                }).create().show();
    }

    /**
     * 设置生日
     */
    private void setBirthday() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_birthday, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        TimePickerView timePickerView = (TimePickerView) view.findViewById(R.id.date_picker);
        timePickerView.setSelectedListener(new TimePickerView.OnDateSelectedListener() {
            @Override
            public void selectedDate(int year, int month, int day) {
                String yearString = String.valueOf(year);
                String monthString = String.valueOf(month);
                String dayString = String.valueOf(day);
                if (monthString.length() == 1){
                    monthString = "0" + monthString;
                }
                if (dayString.length() == 1){
                    dayString = "0" + dayString;
                }
                String birthday = String.format("%s-%s-%s", yearString, monthString, dayString);
                if (!birthday.equals(mTvBirthDay.getText().toString())) {
                    mAccountBean.setBirthDay(birthday);
                    mTvBirthDay.setText(birthday);
                    haveAccountChange = true;
                }
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    /**
     * 设置地区
     */
    private void setLocation(){
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_location, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        CityPickerView cityPickerView = (CityPickerView) view.findViewById(R.id.city_picker);
        cityPickerView.setCitySelectedListener(new CityPickerView.OnCitySelectedListener() {
            @Override
            public void citySelected(String province, String city) {
                String location = province + "/" + city;
                if (!location.equals(mTvLocation.getText().toString())) {
                    mAccountBean.setLocation(location);
                    mTvLocation.setText(location);
                    haveAccountChange = true;
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 设置头像，拍照或选择照片
     */
    private void setHeadImg() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_set_head_img, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();
        TextView take = (TextView) view.findViewById(R.id.tv_take_photo);
        TextView select = (TextView) view.findViewById(R.id.tv_select_img);
        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mHeadImgPath = Constant.APP_CACHE_PATH + File.separator + "image"
                            + File.separator + mAccountBean.getAccount() + ".jpg";
                    Uri uri = Uri.fromFile(new File(mHeadImgPath));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, TAKE_PHOTO);
                } catch (Exception e) {
                    ToastUtils.showMessage(AccountInfoActivity.this, "启动相机出错！请重试");
                    e.printStackTrace();
                }

            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "选择头像图片"), SELECT_PHOTO);
            }
        });
        alertDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == TAKE_PHOTO) {
                dealTakePhotoResult();
            } else if (requestCode == SELECT_PHOTO) {
                mHeadImgPath = ImageUtils.getFilePathFromUri(AccountInfoActivity.this, data.getData());
                dealTakePhotoResult();
            }
        }
    }

    /**
     * 处理拍照回传数据
     */
    private void dealTakePhotoResult() {
        Flowable.just(mHeadImgPath)
                .map(new Function<String, Bitmap>() {
                    @Override
                    public Bitmap apply(String path) throws Exception {
                        // 调整旋转角度，压缩
                        int bitmapDegree = ImageUtils.getBitmapDegree(mHeadImgPath);
                        Bitmap bitmap = ImageUtils.getBitmapFromFile(mHeadImgPath, 600, 400);
                        bitmap = ImageUtils.rotateBitmapByDegree(bitmap, bitmapDegree);
                        ImageUtils.saveBitmap2Jpg(bitmap, path);
                        return bitmap;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        // 显示，记录更新，同步至网易云服务器
                        if (bitmap != null) {
                            // 上传至服务器
                            uploadHeadImg(bitmap);
                        }
                    }
                });
    }

    /**
     * 将头像数据上传至网易云服务器存储，获取服务器返回URL
     */
    private void uploadHeadImg(final Bitmap bitmap) {
        AbortableFuture<String> upload = NIMClient.getService(NosService.class)
                .upload(new File(mHeadImgPath), "image/ipeg");
        upload.setCallback(new RequestCallback() {
            @Override
            public void onSuccess(Object param) {
                Log.e(TAG,"uploadHeadImg onSuccess url = " + param.toString());
                mIvHead.setImageBitmap(bitmap);
                // 保存图片本地路径和服务器路径
                mAccountBean.setHeadImgUrl(param.toString());
                haveAccountChange = true;
            }

            @Override
            public void onFailed(int code) {
                Log.e(TAG,"uploadHeadImg onFailed code " + code);
                ToastUtils.showMessage(AccountInfoActivity.this,
                        "修改失败，头像上传失败，code:" + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.e(TAG,"uploadHeadImg onException message " + exception.getMessage());
                ToastUtils.showMessage(AccountInfoActivity.this,
                        "修改失败,图像上传出错:" + exception.getMessage());
            }
        });
    }

}
