package com.ezreal.ezchat.activity;

import android.os.Bundle;

import android.widget.TextView;

import androidx.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.ezreal.ezchat.R;
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.suntek.commonlibrary.utils.TextUtils;
import com.suntek.commonlibrary.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wudeng on 2017/10/30.
 */

public class ShowLocActivity extends BaseActivity implements AMapLocationListener {

    @BindView(R.id.map_view)
    MapView mMapView;
    @BindView(R.id.tv_address)
    TextView mTvAddress;

    private IMMessage mIMMessage;

    private AMap mAMap;
    private AMapLocationClient mLocationClient;
    private float mZoomLevel = 16.0f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_show_loc);
        ButterKnife.bind(this);
        setTitleBar("位置信息", true, false);
        initMap(savedInstanceState);
        showMsgLocation();
    }

    private void initMap(Bundle savedInstanceState) {

        mMapView.onCreate(savedInstanceState);
        mAMap = mMapView.getMap();

        // 初始位置，广州市中心
        LatLng latLng = new LatLng(23.13023, 113.253171);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel);
        mAMap.animateCamera(cameraUpdate);

        // 缩放控件
        UiSettings settings = mAMap.getUiSettings();
        settings.setZoomGesturesEnabled(true);

        settings.setMyLocationButtonEnabled(false);
        settings.setZoomControlsEnabled(false);

        mLocationClient = new AMapLocationClient(this);

        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setGpsFirst(false);
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 单次定位
        option.setOnceLocation(true);

        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(this);
    }


    private void showMsgLocation(){
        mIMMessage = (IMMessage) getIntent().getSerializableExtra("IMMessage");
        LocationAttachment attachment = (LocationAttachment) mIMMessage.getAttachment();
        if (attachment == null){
            ToastUtils.showMessage(this,"附件获取失败，请重试~");
            finish();
            return;
        }

        double latitude = attachment.getLatitude();
        double longitude = attachment.getLongitude();
        if (latitude < 0.0 || longitude < 0.0){
            ToastUtils.showMessage(this,"地理坐标失效，无法显示!");
        }else {
            LatLng latLng = new LatLng(latitude,longitude);
            // 显示标记
            MarkerOptions options = new MarkerOptions();
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_center);
            options.icon(descriptor);
            options.position(latLng);
            mAMap.addMarker(options);
            // 移动地图视角
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel);
            mAMap.animateCamera(cameraUpdate);
        }

        String address = attachment.getAddress();
        if (!TextUtils.isEmpty(address)){
            mTvAddress.setText(address);
        }else {
            mTvAddress.setText("地址描述获取失败……");
        }
    }

    @OnClick(R.id.iv_my_location)
    public void location(){
        mLocationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location != null) {
            if (location.getErrorCode() == 0) {
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                MarkerOptions options = new MarkerOptions();
                BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_loc);
                options.icon(descriptor);
                options.position(latLng);
                mAMap.addMarker(options);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel);
                mAMap.animateCamera(cameraUpdate);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.onDestroy();
    }

    @OnClick(R.id.iv_back_btn)
    public void back(){
        finish();
    }

    @OnClick(R.id.iv_navigation)
    public void navigation() {
        // 打开地图导航
    }

}
