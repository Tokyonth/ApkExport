package com.tokyonth.apkextractor.multiplexing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.tokyonth.apkextractor.activity.BaseActivity;
import com.tokyonth.apkextractor.data.AppItemInfo;
import com.tokyonth.apkextractor.data.Constants;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static com.tokyonth.apkextractor.activity.BaseActivity.savepath;

public class FileInfo {

    public static String getDuplicateFileInfo(Context context, List<AppItemInfo> items, String extension) {
        try {
            String result = "";
            for (AppItemInfo item : items) {
                File file = new File(getAbsoluteWritePath(context, item, extension));
                if (file.exists() && !file.isDirectory()) {
                    result += file.getAbsolutePath();
                    result += "\n\n";
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 返回此项的绝对写入路径
     *
     * @param item      应用程序信息
     * @param extension 必须是“apk”或“zip”，否则此方法将返回空字符串
     * @return 此AppItemInfo的绝对写入路径
     */
    public static String getAbsoluteWritePath(Context context, AppItemInfo item, String extension) {
        try {
            SharedPreferences settings = context.getSharedPreferences(Constants.PREFERENCE_NAME, Activity.MODE_PRIVATE);
            if (extension.toLowerCase(Locale.ENGLISH).equals("apk")) {
                return savepath + "/" + settings.getString(Constants.PREFERENCE_FILENAME_FONT_APK,
                        Constants.PREFERENCE_FILENAME_FONT_DEFAULT).replace(Constants.FONT_APP_NAME, String.valueOf(item.appName))
                        .replace(Constants.FONT_APP_PACKAGE_NAME, String.valueOf(item.packageName))
                        .replace(Constants.FONT_APP_VERSIONCODE, String.valueOf(item.versioncode))
                        .replace(Constants.FONT_APP_VERSIONNAME, String.valueOf(item.version)) + ".apk";
            }
            if (extension.toLowerCase(Locale.ENGLISH).equals("zip")) {
                return savepath + "/" + settings.getString(Constants.PREFERENCE_FILENAME_FONT_ZIP,
                        Constants.PREFERENCE_FILENAME_FONT_DEFAULT).replace(Constants.FONT_APP_NAME, String.valueOf(item.appName))
                        .replace(Constants.FONT_APP_PACKAGE_NAME, String.valueOf(item.packageName))
                        .replace(Constants.FONT_APP_VERSIONCODE, String.valueOf(item.versioncode))
                        .replace(Constants.FONT_APP_VERSIONNAME, String.valueOf(item.version)) + ".zip";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
