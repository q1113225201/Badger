package com.sjl.badger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
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
        badgeCount = Math.min(Math.abs(badgeCount), 99);
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        if (TextUtils.isEmpty(manufacturer)) {
            return;
        }
        if (manufacturer.contains("vivo")) {
            badgeVivo(context, badgeCount);
        } else if (manufacturer.contains("huawei")) {
            badgeHuawei(context, badgeCount);
        } else if (manufacturer.contains("xiaomi")) {
            badgeXiaomi(context, badgeCount);
        } else if (manufacturer.contains("sony")) {
            badgeSony(context, badgeCount);
        } else if (manufacturer.contains("samsung")) {
            badgeSamsung(context, badgeCount);
        } else if (manufacturer.contains("zuk")) {
            badgeZuk(context, badgeCount);
        } else if (manufacturer.contains("oppo")) {
            badgeOppo(context, badgeCount);
        } else if (manufacturer.contains("htc")) {
            badgeHtc(context, badgeCount);
        } else {
            badgeDefault(context, badgeCount);
        }
    }
    private static NotificationManager notificationManager;
    /**
     * 默认，8.0之前不一定有效，参考其他开发者写法
     */
    private static void badgeDefault(Context context, int badgeCount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //8.0之后添加角标
            if(notificationManager==null) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = new NotificationChannel("badge", "badge", NotificationManager.IMPORTANCE_MIN);
                channel.setShowBadge(true);
                notificationManager.createNotificationChannel(channel);
            }
            Notification notification = new NotificationCompat.Builder(context, "badge")
                    .setContentTitle("通知")
                    .setContentText("新消息")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setNumber(badgeCount)
                    .setAutoCancel(true)
                    .build();
            if(badgeCount>0) {
                notificationManager.notify("badge", 10, notification);
            }else{
                notificationManager.cancel("badge",10);
            }
            return;
        }
        try {
            Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
            intent.putExtra("badge_count", badgeCount);
            intent.putExtra("badge_count_package_name", context.getPackageName());
            intent.putExtra("badge_count_class_name", launcherClassName);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            badgeDefault(context, badgeCount);
        }
    }

    /**
     * HTC权限
     * <uses-permission android:name="com.htc.launcher.permission.READ_SETTINGS"/>
     * <uses-permission android:name="com.htc.launcher.permission.WRITE_SETTINGS"/>
     */
    private static void badgeHtc(Context context, int badgeCount) {
        Intent intentNotification = new Intent("com.htc.launcher.action.SET_NOTIFICATION");
        ComponentName localComponentName = new ComponentName(context.getPackageName(), getLauncherClassName(context));
        intentNotification.putExtra("com.htc.launcher.extra.COMPONENT", localComponentName.flattenToShortString());
        intentNotification.putExtra("com.htc.launcher.extra.COUNT", badgeCount);
        context.sendBroadcast(intentNotification);

        Intent intentShortcut = new Intent("com.htc.launcher.action.UPDATE_SHORTCUT");
        intentShortcut.putExtra("packagename", context.getPackageName());
        intentShortcut.putExtra("count", badgeCount);
        context.sendBroadcast(intentShortcut);
    }

    /**
     * QQ反编译获得，实际使用中需要在oppo开发者平台审核通过
     */
    private static void badgeOppo(Context context, int badgeCount) {
        try {
            Bundle extras = new Bundle();
            extras.putInt("app_badge_count", badgeCount);
            context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"), "setAppBadgeCount", null, extras);
        } catch (Exception e) {
            badgeDefault(context, badgeCount);
        }
    }

    /**
     * 联想ZUK权限
     * <uses-permission android:name="android.permission.READ_APP_BADGE"/>
     */
    private static void badgeZuk(Context context, int badgeCount) {
        Bundle extra = new Bundle();
        extra.putInt("app_badge_count", badgeCount);
        context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"), "setAppBadgeCount", null, extra);
    }

    /**
     * 三星权限
     * <uses-permission android:name="com.sec.android.provider.badge.permission.READ" />
     * <uses-permission android:name="com.sec.android.provider.badge.permission.WRITE" />
     */
    private static void badgeSamsung(Context context, int badgeCount) {
        Uri uri = Uri.parse("content://com.sec.badge/apps");
        String columnId = "_id";
        String columnPackage = "package";
        String columnClass = "class";
        String columnBadgeCount = "badgeCount";
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, new String[]{columnId}, columnPackage + "=?", new String[]{context.getPackageName()}, null);

            if (cursor == null || !cursor.moveToFirst()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(columnPackage, context.getPackageName());
                contentValues.put(columnClass, launcherClassName);
                contentValues.put(columnBadgeCount, badgeCount);
                contentResolver.insert(uri, contentValues);
            } else {
                int idColumnIndex = cursor.getColumnIndex(columnId);

                ContentValues contentValues = new ContentValues();
                contentValues.put(columnBadgeCount, badgeCount);
                contentResolver.update(uri, contentValues, columnId + "=?", new String[]{String.valueOf(cursor.getInt(idColumnIndex))});
            }
        } catch (Exception e) {
            badgeDefault(context, badgeCount);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static AsyncQueryHandler asyncQueryHandler;

    /**
     * 索尼权限
     * <uses-permission android:name="com.sonymobile.home.permission.PROVIDER_INSERT_BADGE" />
     * <uses-permission android:name="com.sonyericsson.home.permission.BROADCAST_BADGE"/>
     */
    private static void badgeSony(Context context, int badgeCount) {
        if (asyncQueryHandler == null) {
            asyncQueryHandler = new AsyncQueryHandler(context.getContentResolver()) {
            };
        }
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("badge_count", badgeCount);
            contentValues.put("package_name", context.getPackageName());
            contentValues.put("activity_name", launcherClassName);
            asyncQueryHandler.startInsert(0, null, Uri.parse("content://com.sonymobile.home.resourceprovider/badge"), contentValues);
        } catch (Exception e) {
            Intent intent = new Intent("com.sonyericsson.home.action.UPDATE_BADGE");
            intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", badgeCount > 0);
            intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", launcherClassName);
            intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(badgeCount > 0 ? badgeCount : ""));
            intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());
            context.sendBroadcast(intent);
        }
    }

    /**
     * 小米
     */
    private static void badgeXiaomi(final Context context, final int badgeCount) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                Notification.Builder builder = new Notification.Builder(context)
                        .setContentTitle("")
                        .setContentText("")
                        .setSmallIcon(resolveInfo.getIconResource());
                Notification notification = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = builder.build();
                }
                try {
                    Field field = notification.getClass().getDeclaredField("extraNotification");
                    Object extraNotification = field.get(notification);
                    Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
                    method.invoke(extraNotification, badgeCount);
                    mNotificationManager.notify(0, notification);
                } catch (Exception e) {
                    e.printStackTrace();
                    // miui 6之前的版本
                    Intent localIntent = new Intent("android.intent.action.APPLICATION_MESSAGE_UPDATE");
                    localIntent.putExtra("android.intent.extra.update_application_component_name", context.getPackageName() + "/" + getLauncherClassName(context));
                    localIntent.putExtra("android.intent.extra.update_application_message_text", String.valueOf(badgeCount == 0 ? "" : badgeCount));
                    context.sendBroadcast(localIntent);
                }
            }
        }, 1000);
    }

    /**
     * vivo
     * 据说有用，但在部分vivo手机上不显示，如x6s
     * 包名改成QQ或微信的就能显示角标
     */
    private static void badgeVivo(Context context, int badgeCount) {
        Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
        intent.putExtra("packageName", context.getPackageName());
        intent.putExtra("className", launcherClassName);
        intent.putExtra("notificationNum", badgeCount);
        context.sendBroadcast(intent);
    }

    /**
     * 华为权限
     * <uses-permission android:name="android.permission.INTERNET"/>
     * <uses-permission android:name="com.huawei.android.launcher.permission.CHANGE_BADGE "/>
     */
    private static void badgeHuawei(Context context, int badgeCount) {
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

    private static String getLauncherClassName(Context context) {
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
