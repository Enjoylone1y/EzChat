package com.ezreal.ezchat.activity;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.ezreal.ezchat.R;
import com.ezreal.ezchat.bean.LocationPoint;
import com.suntek.commonlibrary.adapter.OnItemClickListener;
import com.suntek.commonlibrary.adapter.RViewHolder;
import com.suntek.commonlibrary.adapter.RecycleViewAdapter;
import com.suntek.commonlibrary.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 选择位置，发送位置信息
 * Created by wudeng on 2017/9/18.
 */

public class SelectLocActivity extends BaseActivity implements AMapLocationListener,
        PoiSearch.OnPoiSearchListener,AMap.OnCameraChangeListener{

    private static final String TAG = SelectLocActivity.class.getSimpleName();

    @BindView(R.id.map_view)
    MapView mMapView;
    @BindView(R.id.rcv_poi_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.iv_back_btn)
    ImageView mIvBack;
    @BindView(R.id.iv_search)
    ImageView mIvSearch;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_btn_send)
    TextView mTvSend;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.iv_selected_point)
    ImageView mIvCenter;

    private AMap mAMap;
    private AMapLocationClient mLocationClient;
    private AMapLocation mLocation;
    private LatLng mCenterPoint;
    private MarkerOptions mLocationMarker;

    private LinearLayoutManager mLayoutManager;
    private List<LocationPoint> mPointList;
    private RecycleViewAdapter<LocationPoint> mAdapter;
    private int mCurrentSelect = -1;

    private float mZoomLevel = 16.0f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_select_location);
        ButterKnife.bind(this);
        mMapView.onCreate(savedInstanceState);
        initPoiList();
        initMap();
    }

    private void initPoiList(){
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mPointList = new ArrayList<>();
        mAdapter = new RecycleViewAdapter<LocationPoint>(this,mPointList) {
            @Override
            public int setItemLayoutId(int position) {
                return R.layout.item_round_poi;
            }

            @Override
            public void bindView(RViewHolder holder, int position) {
                LocationPoint point = mPointList.get(position);
                holder.setText(R.id.tv_poi_name,point.getName());
                holder.setText(R.id.tv_poi_address,point.getAddress());
                if (point.isSelected()){
                    holder.getImageView(R.id.iv_selected).setVisibility(View.VISIBLE);
                }else {
                    holder.getImageView(R.id.iv_selected).setVisibility(View.INVISIBLE);
                }
            }
        };
        mAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(RViewHolder holder, int position) {

                // 清除原来已选的
                mPointList.get(mCurrentSelect).setSelected(false);
                mAdapter.notifyItemChanged(mCurrentSelect);

                // 更新当前已选点
                mCurrentSelect = position;
                mPointList.get(position).setSelected(true);
                mAdapter.notifyItemChanged(position);

                // 刷新地图
                LatLonPoint point = mPointList.get(position).getPoint();
                setMapCenter(point.getLatitude(),point.getLongitude());
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }

    private void initMap(){

        mAMap = mMapView.getMap();
        // 初始位置，广州市中心
        setMapCenter(23.13023, 113.253171);

        // 地图中心发生变化监听
        mAMap.setOnCameraChangeListener(this);

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
        mLocationClient.startLocation();

    }

    /**地图镜头被移动*****/

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

        mZoomLevel = cameraPosition.zoom;

        // 判断是否已经定位成功
        if (mLocation != null){
            // 若移动后，与原中心点距离超过10米，刷新 POI 列表
            float distance = AMapUtils.
                    calculateLineDistance(mCenterPoint,cameraPosition.target);
             if (distance >= 10.0f){
                 setMapCenter(cameraPosition.target.latitude,cameraPosition.target.longitude);
                 searchRoundPoi(new LatLonPoint(cameraPosition.target.latitude,
                         cameraPosition.target.longitude));
             }
        }
    }


    /**我的位置发生移动**/
    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location != null) {
            if (location.getErrorCode() == 0) {
                // 保存当前定位点
                mLocation = location;
                // 移动到定位点
                setMapCenter(location.getLatitude(), location.getLongitude());
                // 显示位置点
                drawLocationPoint();

                // 搜索并显示周边POI点列表
                searchRoundPoi(new LatLonPoint(location.getLatitude(),location.getLongitude()));

            }
        }
    }

    private void setMapCenter(double latitude, double longitude) {
        // 设置地图中心点
        mCenterPoint = new LatLng(latitude, longitude);
        // 将地图移动到当前位置
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mCenterPoint, mZoomLevel);
        mAMap.animateCamera(cameraUpdate);
    }

    private void drawLocationPoint(){
        mLocationMarker = new MarkerOptions();
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_loc);
        mLocationMarker.icon(descriptor);
        mLocationMarker.position(mCenterPoint);
        mAMap.addMarker(mLocationMarker);
    }

    private void searchRoundPoi(LatLonPoint point){

        mProgressBar.setVisibility(View.VISIBLE);

        // 清除原有数据
        mPointList.clear();
        mAdapter.notifyDataSetChanged();

        // 关键字 类型 区域
        PoiSearch.Query query = new PoiSearch.Query("","",mLocation.getCityCode());
        query.setPageNum(1);
        query.setPageSize(50);

        // 位置点周边1000米范围搜索
        PoiSearch search = new PoiSearch(this,query);
        PoiSearch.SearchBound bound = new  PoiSearch.SearchBound(point,1000);
        search.setBound(bound);

        search.setOnPoiSearchListener(this);
        search.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        if (!poiResult.getPois().isEmpty()){
            List<LocationPoint> points = new ArrayList<>(poiResult.getPois().size());
            LocationPoint point;
            for (PoiItem poiItem : poiResult.getPois()){
                point = new LocationPoint();
                point.setId(poiItem.getPoiId());
                point.setName(poiItem.getTitle());
                point.setPoint(poiItem.getLatLonPoint());
                point.setSelected(false);

                String address = poiItem.getProvinceName()
                        + poiItem.getCityName()
                        + poiItem.getAdName()
                        + poiItem.getSnippet();
                point.setAddress(address);
                points.add(point);
            }
            mCurrentSelect = 0;
            // 默认选择第一项
            points.get(0).setSelected(true);

            mPointList.addAll(points);
            mAdapter.notifyDataSetChanged();

            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @OnClick(R.id.iv_back_btn)
    public void backOnClick(){
        this.finish();
    }

    @OnClick(R.id.tv_btn_send)
    public void send(){
        if (!mPointList.isEmpty() && mCurrentSelect != -1){
            Intent intent = new Intent();
            intent.putExtra("location",mPointList.get(mCurrentSelect).getPoint());
            intent.putExtra("address",mPointList.get(mCurrentSelect).getAddress());
            setResult(RESULT_OK,intent);
            finish();
        }else {
            ToastUtils.showMessage(this,"位置获取失败，请稍后再试~");
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

        if (mLocationClient.isStarted()){
            mLocationClient.stopLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.onDestroy();
    }

}
