package com.ezreal.ezchat.bean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ezreal.ezchat.R;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.List;

/**
 * Created by wudeng on 2017/11/10.
 */

public class UserListAdapter extends BaseAdapter {

    private Context mContext;
    private List<NimUserInfo> mUserList;
    private LayoutInflater mInflater;
    private OnItemClickListener mOnItemClickListener;

    public UserListAdapter(Context context,List<NimUserInfo> users){
        mContext = context;
        mUserList = users;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mUserList != null ? mUserList.size() : 0;
    }

    @Override
    public NimUserInfo getItem(int position) {
        return mUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_user, parent, false);
            holder.imageView = convertView.findViewById(R.id.image_view);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        Glide.with(mContext)
                .load(mUserList.get(position).getAvatar())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.mipmap.bg_img_defalut)
                .into(holder.imageView);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null){
                    mOnItemClickListener.itemClick(position);
                }
            }
        });

        return convertView;
    }

    private class ViewHolder{
        ImageView imageView;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener{
        void itemClick(int position);
    }
}
