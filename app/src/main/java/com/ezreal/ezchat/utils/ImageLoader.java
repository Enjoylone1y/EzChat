package com.ezreal.ezchat.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 图片加载类
 * Created by wudeng on 2016/12/6.
 */

public class ImageLoader {

    private static ImageLoader mInstance;
    /**
     * 图片缓存对象
     */
    private LruCache<String,Bitmap> mLruCache;
    /**
     * 线程池
     */
    private ExecutorService mTreadPool;
    private static final int DEFAULT_THREAD_COUNT = 1;
    private Type mType = Type.LIFO;
    /**
     * 用户队列
     */
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 后台轮询线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;

    /**
     * 通过信号量，同步
     */
    private Semaphore mSemaPoolThreadHandler = new Semaphore(0);

    private Semaphore mSemaPoolThread;

    private Handler mUIHandler;

    public static ImageLoader getInstance(){
        if (mInstance == null){
           synchronized (ImageLoader.class){
               if (mInstance == null){
                   mInstance = new ImageLoader(DEFAULT_THREAD_COUNT,Type.LIFO);
               }
           }
        }
        return mInstance;
    }

    public static ImageLoader getInstance(int threadCount,Type type){
        if (mInstance == null){
            synchronized (ImageLoader.class){
                if (mInstance == null){
                    mInstance = new ImageLoader(threadCount,type);
                }
            }
        }
        return mInstance;
    }

    private ImageLoader(int ThreadCount,Type type){
        init(ThreadCount,type);
    }

    /**
     * 初始化图片加载类
     * @param threadCount 用于加载图片的最大线程数
     * @param type 图片加载策略
     */
    private void init(int threadCount, Type type) {
        //初始化轮询线程
        mPoolThread = new Thread(){
            @SuppressLint("HandlerLeak")
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        //通过线程池取出任务,执行
                        mTreadPool.execute(getTask());
                        try {
                            mSemaPoolThread.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //实例化完成后释放信号
                mSemaPoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        //获取应用最大可用内存，取1/8作为缓存内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        //初始化图片缓存
        mLruCache = new LruCache<String, Bitmap>(cacheMemory){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //返回图片所占内存
                return value.getRowBytes() * value.getHeight();
            }
        };
        //初始化线程池，传入最大线程数
        mTreadPool = Executors.newFixedThreadPool(threadCount);
        //初始化任务队列
        mTaskQueue = new LinkedList<>();
        mType = type;
        //初始化信号量
        mSemaPoolThread = new Semaphore(threadCount);
    }

    /**
     * 根据path得到图片，显示到imageview上
     * @param path 图片路径
     * @param imageView 显示图片的imageview
     */
    @SuppressLint("HandlerLeak")
    public void loadImage(final String path, final ImageView imageView){
        imageView.setTag(path);

        if (mUIHandler == null){
            mUIHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //获取得到的图片，位Iamgeview 设置图片
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap bitmap = holder.bitmap;
                    ImageView imageview = holder.imageview;
                    String path = holder.path;
                    //使用TAG的方法解决由于复用问题出现的图片加载错乱问题
                    if (imageview.getTag().toString().equals(path)){
                        imageview.setImageBitmap(bitmap);
                    }
                }
            };
        }

        Bitmap bm = getBitMapFromLruCache(path);
        if (bm != null){
            Message message = mUIHandler.obtainMessage();
            ImgBeanHolder holder = new ImgBeanHolder();
            holder.bitmap = bm;
            holder.imageview = imageView;
            holder.path = path;
            message.obj = holder;
            message.sendToTarget();
        }else{ //如果缓存中不存在该路径所代表的图片，则发送加载图片任务至轮询线程
            addTask(new Runnable(){
                @Override
                public void run() {
                    //1.得到图片要显示的宽高
                    ImageSize size = getImageViewSize(imageView);
                    //2.压缩图片
                    Bitmap bm = decodeSampleBitmapFromPath(path,size.height,size.width);
                    //3.将图片加入缓存
                    if (getBitMapFromLruCache(path) == null){
                        mLruCache.put(path,bm);
                    }
                    //4.将图片发送出去
                    Message message = mUIHandler.obtainMessage();
                    ImgBeanHolder holder = new ImgBeanHolder();
                    holder.bitmap = bm;
                    holder.imageview = imageView;
                    holder.path = path;
                    message.obj = holder;
                    message.sendToTarget();
                    //每执行完一个任务，释放一个信号量
                    mSemaPoolThread.release();
                }
            });
        }
    }

    /**
     * 根据图片需要显示的宽高得到bitmap
     * @param path 图片路径
     * @param height 显示高度
     * @param width 显示宽度
     * @return 压缩后的bitmap
     */
    private Bitmap decodeSampleBitmapFromPath(String path, int height, int width) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;//只得到宽高，不加载图片
        BitmapFactory.decodeFile(path,op);//op中现在有图片实际的宽高
        op.inSampleSize = calculateInSampleSize(op,height,width);
        op.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path,op);
        return bitmap;
    }

    /**
     * 根据图片实际的宽高和需要显示的宽高，计算缩略图大小
     */
    private int calculateInSampleSize(BitmapFactory.Options op, int reqHeight, int reqWidth) {
        int width = op.outWidth;
        int height = op.outHeight;
        int inSampleSize = 1;

        //图片实际宽度大于需求宽度
        if (width > reqWidth || height > reqHeight){
            int widthRatio = Math.round(width * 1.0f/reqWidth);
            int heightRatio = Math.round(height * 1.0f/reqHeight);
            inSampleSize = Math.max(widthRatio,heightRatio);
        }
        return inSampleSize;
    }

    /**
     * 根据iamgeview 获取适当的宽高
     * @param imageView 用于显示图片的imageview
     * @return 图片显示大小
     */
    private ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imagesize = new ImageSize();
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        final DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();

        int width = imageView.getWidth();  //获取实际宽度，若图片未加载，为0
        if (width <= 0){
            width = layoutParams.width;//获取iamgeview在layout中声明的宽度，若为wrapcontent 也为0
        }
        if (width <= 0){
            width = imageView.getMaxWidth();//检查最大值
        }
        if (width <=0){ //若还是0，给定为屏幕宽度
            width = displayMetrics.widthPixels;
        }

        int height = imageView.getHeight();  //获取实际高度，若图片未加载，为0
        if (height <= 0){
            height = layoutParams.height;//获取iamgeview在layout中声明的高度，若为wrapcontent 也为0
        }
        if (height <= 0){
            height = imageView.getMaxHeight();//检查最大值
        }
        if (height <=0){ //若还是0，给定为屏幕高度
            height = displayMetrics.heightPixels;
        }

        imagesize.height = height;
        imagesize.width = width;

        return imagesize;
    }

    /**
     * 将获取图片的任务加载到任务队列中
     * @param runnable 待执行的任务
     */
    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        //发送消息，让队列线程执行获取图片的任务
        try {
            //如果为空，发出请求
            if (mPoolThreadHandler == null){
                mSemaPoolThreadHandler.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(0x110);
    }
    /**
     * 根据TYPE，从任务队列中取出一个Runable
     * @return Runnable
     */
    private Runnable getTask(){
        if(mType == Type.FIFO){
            return mTaskQueue.removeFirst();
        }else if(mType == Type.LIFO){
            return mTaskQueue.removeLast();
        }
        return null;
    }
    /**
     * 根据path从缓存中获取图片bitmap
     * @param path 图片路径
     * @return 从缓存中取出的Bitmap
     */
    private Bitmap getBitMapFromLruCache(String path) {
        return mLruCache.get(path);
    }

    private class ImageSize{
        int height;
        int width;
    }

    private class ImgBeanHolder{
        Bitmap bitmap;
        ImageView imageview;
        String path;
    }

    public enum Type {
        FIFO,LIFO,
    }
}
