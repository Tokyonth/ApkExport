package com.tokyonth.apkextractor.activity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tokyonth.apkextractor.data.AppItemInfo;
import com.tokyonth.apkextractor.data.Constants;
import com.tokyonth.apkextractor.utils.StorageUtil;

public abstract class BaseActivity extends AppCompatActivity {

    private static int REQUEST_CODE = 100;
    // 要申请的权限
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    private AlertDialog.Builder dialog;

    public static List<AppItemInfo> listsum = new ArrayList<AppItemInfo>();
    public static List<AppItemInfo> listsearch = new ArrayList<AppItemInfo>();
    public static LinkedList<BaseActivity> queue = new LinkedList<BaseActivity>();

    public SharedPreferences settings;
    public SharedPreferences.Editor editor;
    public boolean isatFront = false;

    public static final int MESSAGE_COPYFILE_COMPLETE = 1;
    public static final int MESSAGE_COPYFILE_INTERRUPT = 2;
    public static final int MESSAGE_COPYFILE_FILE_NOTFOUND_EXCEPTION = 3;

    public static final int MESSAGE_STORAGE_NOTENOUGH = 4;
    public static final int MESSAGE_COPYFILE_REFRESH_SPEED = 5;
    public static final int MESSAGE_COPYFILE_REFRESH_PROGRESS = 6;

    public static final int MESSAGE_COPYFILE_IOEXCEPTION = 7;
    public static final int MESSAGE_COPYFILE_CURRENTFILE = 8;
    public static final int MESSAGE_COPYFILE_CURRENTAPP = 9;

    public static final int MESSAGE_LOADLIST_REFRESH_PROGRESS = 10;
    public static final int MESSAGE_LOADLIST_COMPLETE = 11;

    public static String savepath = Constants.PREFERENCE_SAVE_PATH_DEFAULT;
    public static String storage_path = StorageUtil.getMainStoragePath();

    public static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (queue.size() > 0) {
                queue.getLast().processMessage(msg);
            }
        }
    };

    @SuppressLint("NewApi")
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (!queue.contains(this)) {
            queue.add(this);
        }
        settings = getSharedPreferences(Constants.PREFERENCE_NAME, Activity.MODE_PRIVATE);
        editor = settings.edit();
        savepath = settings.getString(Constants.PREFERENCE_SAVE_PATH, Constants.PREFERENCE_SAVE_PATH_DEFAULT);
    }

    public void onResume() {
        super.onResume();
        this.isatFront = true;
    }

    public void onPause() {
        super.onPause();
        this.isatFront = false;
    }

    public static void sendEmptyMessage(int what) {
        handler.sendEmptyMessage(what);
    }

    public static void sendMessage(Message msg) {
        handler.sendMessage(msg);
    }

    public abstract void processMessage(Message msg);

    public void finish() {
        super.finish();
        if (queue.contains(this)) {
            queue.remove(this);
        }
    }

    public void getPermission() {
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                showDialogTipUserRequestPermission();
            }
        }
    }

    // 提示用户该请求权限的弹出框
    private void showDialogTipUserRequestPermission() {
        dialog = new AlertDialog.Builder(this);
        dialog.setTitle("权限不可用");
        dialog.setMessage("使用本软件需要储存读写权限；\n否则，您将无法正常使用");
        dialog.setCancelable(false);
        dialog.setPositiveButton("立即开启", (dialog, which) -> {
            startRequestPermission();
            dialog.dismiss();
        });
        dialog.setNegativeButton("拒绝", (dialog, which) -> {
            finish();
            dialog.dismiss();
        });
        dialog.show();
    }

    // 开始提交请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }

    // 用户权限 申请 的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                      //  showDialogTipUserGoToAppSettting();
                    } else
                        finish();
                } else {
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}

