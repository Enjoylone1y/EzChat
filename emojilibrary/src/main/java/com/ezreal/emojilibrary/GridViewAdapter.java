package com.ezreal.emojilibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by wudeng on 2017/11/6.
 */

public class GridViewAdapter extends BaseAdapter {

    private Context mContext;
    private List<EmojiBean> mData;

    public GridViewAdapter(Context context,List<EmojiBean> data){
        mContext = context;
        mData = data;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public EmojiBean getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_face, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.face_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mData.get(position) != null) {
            int width = EmojiUtils.dip2px(mContext, 32);
            int height = EmojiUtils.dip2px(mContext, 32);
            Bitmap bitmap = EmojiUtils.decodeBitmapFromRes(mContext.getResources(),
                    mData.get(position).getResIndex(), width, height);
            if (bitmap != null){
                holder.iv.setImageBitmap(bitmap);
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView iv;
    }
}
