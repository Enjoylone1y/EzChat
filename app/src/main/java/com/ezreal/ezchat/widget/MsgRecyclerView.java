package com.ezreal.ezchat.widget;

import android.content.Context;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 聊天记录列表
 * Created by wudeng on 2017/9/4.
 */

public class MsgRecyclerView extends RecyclerView {

    public static final int VIEW_TYPE_HEADER = 100001;
    private final static float OFFSET_RADIO = 1.8f;
    public OnLoadingListener mLoadingListener;
    private MsgViewAdapter mMsgViewAdapter;
    private boolean isLoading = false;
    private float mLastY = -1;
    private MsgHeadView mHeadView;
    private LinearLayoutManager mLayoutManager;

    private final RecyclerView.AdapterDataObserver mDataObserver = new DataObserver();

    public MsgRecyclerView(Context context) {
        this(context, null);
    }

    public MsgRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MsgRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mLayoutManager = new LinearLayoutManager(getContext());
        setLayoutManager(mLayoutManager);
        mHeadView = new MsgHeadView(getContext());
    }

    public void hideHeadView() {
        mHeadView.setVisibleHeight(0);
        isLoading = false;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mMsgViewAdapter = new MsgViewAdapter(adapter);
        super.setAdapter(mMsgViewAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
    }

    @Override
    public Adapter getAdapter() {
        return mMsgViewAdapter.getOriginalAdapter();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指按下时坐标
                mLastY = e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 计算手指滑动距离，并更新当前 Y 值
                final float deltaY = e.getRawY() - mLastY;
                mLastY = e.getRawY();
                // 若当前处于列表最上方，且headView 当前显示高度小于完整高度2倍，则更新 headView 的显示高度
                if (mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0
                        && (deltaY > 0) && !isLoading
                        && mHeadView.getVisibleHeight() <= mHeadView.getHeadHeight() * 2) {
                    mHeadView.setVisibleHeight((int) (deltaY / OFFSET_RADIO + mHeadView.getVisibleHeight()));
                }
                break;
            case MotionEvent.ACTION_UP:
                mLastY = -1;
                // 如果 headView 显示高度大于原始高度，则刷新消息列表
                if (mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    if (!isLoading && mHeadView.getVisibleHeight() > mHeadView.getHeadHeight()) {
                        if (mLoadingListener != null) {
                            mLoadingListener.loadPreMessage();
                            isLoading = true;
                        }
                        mHeadView.setVisibleHeight(mHeadView.getHeadHeight());
                    }else {
                        // 否则，隐藏headView
                        hideHeadView();
                    }
                }
                break;
        }
        return super.onTouchEvent(e);
    }

    public void setLoadingListener(OnLoadingListener loadingListener) {
        mLoadingListener = loadingListener;
    }

    public interface OnLoadingListener {
        void loadPreMessage();
    }

    private class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            if (mMsgViewAdapter != null) {
                mMsgViewAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mMsgViewAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mMsgViewAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mMsgViewAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mMsgViewAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mMsgViewAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    }

    private class MsgViewAdapter extends Adapter<ViewHolder> {

        private Adapter mAdapter;

        public MsgViewAdapter(Adapter adapter) {
            this.mAdapter = adapter;
        }

        public RecyclerView.Adapter getOriginalAdapter() {
            return this.mAdapter;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return VIEW_TYPE_HEADER;
            }
            return mAdapter.getItemViewType(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_HEADER) {
                return new SimpleViewHolder(mHeadView);
            }
            return mAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position == 0) {
                return;
            }
            mAdapter.onBindViewHolder(holder, position);
        }

        @Override
        public int getItemCount() {
            return mAdapter.getItemCount() + 1;
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            mAdapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            mAdapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
            return mAdapter.onFailedToRecycleView(holder);
        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            mAdapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            mAdapter.registerAdapterDataObserver(observer);
        }

        private class SimpleViewHolder extends RecyclerView.ViewHolder {
            SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
