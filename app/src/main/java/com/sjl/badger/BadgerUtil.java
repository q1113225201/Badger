package com.sjl.badger;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * BadgerUtil
 *
 * @author 林zero
 * @date 2018/3/29
 */

public class BadgerUtil {
    private static final String TAG = "BadgerUtil";

    public static void addBadger(Context context, int badgeCount) {
        if (TextUtils.isEmpty(getLauncherClassName(context))) {
            Log.e(TAG, "launcherClassName==null");
            return;
        }
        badgeCount = Math.min(badgeCount, 99);
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        if(TextUtils.isEmpty(manufacturer)){
            return;
        }
        if (manufacturer.contains("vivo")) {
            badgerVivo(context, badgeCount);
        } else if (manufacturer.contains("huawei")) {
            badgerHuawei(context, badgeCount);
        } else if (manufacturer.contains("xiaomi")) {
            badgerXiaomi(context, badgeCount);
        }else if (manufacturer.contains("sony")) {
            badgerSony(context, badgeCount);
        }
    }

    private static AsyncQueryHandler asyncQueryHandler;

    /**
     索尼权限
     <uses-permission android:name="com.sonymobile.home.permission.PROVIDER_INSERT_BADGE" />
     */
    private static void badgerSony(Context context, int badgeCount) {
        if (asyncQueryHandler == null) {
            asyncQueryHandler = new AsyncQueryHandler(context.getContentResolver()) {};
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("badge_count", badgeCount);
        contentValues.put("package_name", context.getPackageName());
        contentValues.put("activity_name", launcherClassName);
        asyncQueryHandler.startInsert(0, null, Uri.parse("content://com.sonymobile.home.resourceprovider/badge"), contentValues);
    }

    private static void badgerXiaomi(Context context, int badgeCount) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("")
                .setContentText("")
                .setSmallIcon(resolveInfo.getIconResource());
        Notification notification = builder.build();
        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, badgeCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mNotificationManager.notify(0, notification);
    }

    /**
     * 据说有用，但在vivo X6S不显示
     */
    private static void badgerVivo(Context context, int badgeCount) {
        Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
        intent.putExtra("packageName", context.getPackageName());
        intent.putExtra("className", launcherClassName);
        intent.putExtra("notificationNum", badgeCount);
        context.sendBroadcast(intent);
    }

    /**
     华为权限
     <uses-permission android:name="android.permission.INTERNET"/>
     <uses-permission android:name="com.huawei.android.launcher.permission.CHANGE_BADGE "/>
     */
    private static void badgerHuawei(Context context, int badgeCount) {
        try {
            Bundle bunlde = new Bundle();
            bunlde.putString("package", context.getPackageName());
            bunlde.putString("class", launcherClassName);
            bunlde.putInt("badgenumber", badgeCount);
            context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, bunlde);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private static String launcherClassName;

    public static String getLauncherClassName(Context context) {
        if (!TextUtils.isEmpty(launcherClassName)) {
            return launcherClassName;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfoList) {
            if (context != null && context.getPackageName().equalsIgnoreCase(resolveInfo.activityInfo.applicationInfo.packageName)) {
                launcherClassName = resolveInfo.activityInfo.name;
                break;
            }
        }
        return launcherClassName;
    }
}
