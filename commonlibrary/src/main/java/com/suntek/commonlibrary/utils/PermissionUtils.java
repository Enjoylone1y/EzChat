package com.suntek.commonlibrary.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;


/**
 * 权限工具类
 * Created by wudeng on 2017/8/4.
 */

public class PermissionUtils {


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkPermissions(Context context, String... permissions){
        for (String p : permissions){
            if (context.checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限
     * @param activity Activity
     * @param requestCode 申请码
     * @param permissions 权限组
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestPermissions(Activity activity, int requestCode, String... permissions){
        activity.requestPermissions(permissions,requestCode);
    }


    /**
     * 处理权限申请回调结果，分为 授权，拒绝未勾选不再提醒，拒绝并勾选不再提醒 三组结果返回
     * @param context 上下文
     * @param permission 申请的权限数组{@link ActivityCompat.OnRequestPermissionsResultCallback}
     * @param grantResult 申请结果数组{@link ActivityCompat.OnRequestPermissionsResultCallback}
     * @param callBack 处理回调接口 {@link RequestPermissionCallBack}
     */
    public static void dealPermissionResult(Context context,String[] permission,
                                            int[] grantResult,RequestPermissionCallBack callBack){
        List<String> grant = new ArrayList<>();
        List<String> denied = new ArrayList<>();
        List<String> neverAsk = new ArrayList<>();
        for (int i=0;i<grantResult.length;i++){
            if (grantResult[i] == PackageManager.PERMISSION_GRANTED){
                grant.add(permission[i]);
            }else {
                if (judgePermission(context,permission[i])){
                    denied.add(permission[i]);
                }else {
                    neverAsk.add(permission[i]);
                }
            }
        }

        if (!grant.isEmpty()){
            callBack.onGrant(list2Array(grant));
        }
        if (!denied.isEmpty()){
            callBack.onDenied(list2Array(denied));
        }
        if (!neverAsk.isEmpty()){
            callBack.onDeniedAndNeverAsk(list2Array(neverAsk));
        }
    }

    /**
     * 对于用户拒绝并勾选不再提醒的权限进行提示和辅助跳转到权限管理页面
     * @param context 上下文
     * @param message 权限需求说明
     */
    public static void requestPermissionDialog(final Context context, String message){

        new AlertDialog.Builder(context)
                .setTitle("权限申请")
                .setMessage(message)
                .setPositiveButton("授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                        context.startActivity(intent);

                        // 跳转后请在 onRestart 在中再次检查权限是否已获取
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToastUtils.showMessage(context,"未能获取权限，部分功能将受限");
                    }
                })
                .show();
    }


    /**
     * 判断是否已拒绝过权限
     * 如果应用之前请求过此权限但用户拒绝，此方法将返回 true;
     * -----如果应用第一次请求权限或 用户在过去拒绝了权限请求，
     * -----并在权限请求系统对话框中选择了 Don't ask again 选项，此方法将返回 false。
     */
    private static boolean judgePermission(Context context, String permission) {
        return  ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission);
    }


    private static String[] list2Array(List<String> list){
        String[]  array = new String[list.size()];
        for (int i=0;i<list.size();i++){
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * 权限处理返回接口
     */
    public interface RequestPermissionCallBack {

        /**
         * 用户授予权限
         * @param  permissions 已获得授权的权限
         */
        void onGrant(String... permissions);

        /**
         * 用户拒绝，(未勾选不再提醒)
         * @param permissions 被拒绝的权限
         */
        void onDenied(String... permissions);

        /**
         * 用户拒绝，并且已勾选不再询问选项
         * @param permissions 被拒绝的权限
         */
        void onDeniedAndNeverAsk(String... permissions);
    }

}
