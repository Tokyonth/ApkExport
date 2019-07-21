package com.tokyonth.apkextractor.listener;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.tokyonth.apkextractor.R;
import com.tokyonth.apkextractor.activity.Main;
import com.tokyonth.apkextractor.adapter.AppListAdapter;
import com.tokyonth.apkextractor.data.AppItemInfo;
import com.tokyonth.apkextractor.data.Constants;
import com.tokyonth.apkextractor.multiplexing.FileInfo;
import com.tokyonth.apkextractor.ui.AppDetailDialog;
import com.tokyonth.apkextractor.utils.FileSize;
import com.tokyonth.apkextractor.utils.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListenerNormalMode implements AppListAdapter.OnItemClickListener {

    private Main main;
    private AppListAdapter list_adapter;

    public ListenerNormalMode(Main main, AppListAdapter list_adapter) {
        this.main = main;
        this.list_adapter = list_adapter;
    }

    @Override
    public void onItemClick(int position) {
        if (list_adapter != null) {
            final AppItemInfo item = list_adapter.getAppList().get(position);
            final AppDetailDialog dialog_app_detail = new AppDetailDialog(main, R.style.BottomSheetDialog);
            dialog_app_detail.setTitle(item.getAppName());
            dialog_app_detail.setIcon(item.getIcon());
            dialog_app_detail.setAppInfo(item.getVersion(), item.getVersionCode(), item.getLastUpdateTime(), item.getPackageSize());
            if (Build.VERSION.SDK_INT >= 24) {
                dialog_app_detail.setAPPMinSDKVersion(item.getMinSDKVersion());
            }
            dialog_app_detail.show();
            new Thread(() -> {
                final long dataSize = FileSize.getFileOrFolderSize(new File(StorageUtil.getMainStoragePath() + "/android/data/" + item.packageName));
                final long obbSize = FileSize.getFileOrFolderSize(new File(StorageUtil.getMainStoragePath() + "/android/obb/" + item.packageName));
                main.handler.post(() -> {
                    try {
                        CheckBox cb_data = (CheckBox) dialog_app_detail.findViewById(R.id.dialog_appdetail_extract_data_cb);
                        CheckBox cb_obb = (CheckBox) dialog_app_detail.findViewById(R.id.dialog_appdetail_extract_obb_cb);
                        cb_data.setText("Data(" + Formatter.formatFileSize(main, dataSize) + ")");
                        cb_obb.setText("Obb(" + Formatter.formatFileSize(main, obbSize) + ")");
                        cb_data.setVisibility(View.VISIBLE);
                        cb_obb.setVisibility(View.VISIBLE);
                        dialog_app_detail.findViewById(R.id.dialog_appdetail_extract_extra_pb).setVisibility(View.GONE);
                        cb_data.setEnabled(dataSize > 0);
                        cb_obb.setEnabled(obbSize > 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }).start();

            dialog_app_detail.area_extract.setOnClickListener(v -> {
                if (dialog_app_detail != null) dialog_app_detail.cancel();
                final boolean data = ((CheckBox) dialog_app_detail.findViewById(R.id.dialog_appdetail_extract_data_cb)).isChecked();
                final boolean obb = ((CheckBox) dialog_app_detail.findViewById(R.id.dialog_appdetail_extract_obb_cb)).isChecked();
                List<AppItemInfo> selectedList = new ArrayList<AppItemInfo>();
                selectedList.add(item);

                List<AppItemInfo> list_item = new ArrayList<AppItemInfo>();
                list_item.add(list_adapter.getAppList().get(position));
                String duplicate = FileInfo.getDuplicateFileInfo(main, list_item, (data || obb) ? "zip" : "apk");

                if (duplicate.length() > 0) {
                    new AlertDialog.Builder(main)
                            .setIcon(R.drawable.ic_icon_warn)
                            .setTitle(main.getResources().getString(R.string.activity_main_duplicate_title))
                            .setCancelable(true)
                            .setMessage(main.getResources().getString(R.string.activity_main_duplicate_message) + "\n\n" + duplicate)
                            .setPositiveButton(main.getResources().getString(R.string.dialog_button_positive), (dialog, which) -> {
                                main.shareAfterExtract = false;
                                Message msg_extract = new Message();
                                msg_extract.what = Main.MESSAGE_EXTRACT_SINGLE_APP;
                                msg_extract.obj = new Integer[]{Integer.valueOf(position), data ? 1 : 0, obb ? 1 : 0};
                                main.processExtractMsg(msg_extract);
                            })
                            .setNegativeButton(main.getResources().getString(R.string.dialog_button_negative), (dialog, which) -> {

                            })
                            .show();

                } else {
                    main.shareAfterExtract = false;
                    Message msg_extract = new Message();
                    msg_extract.what = Main.MESSAGE_EXTRACT_SINGLE_APP;
                    msg_extract.obj = new Integer[]{Integer.valueOf(position), data ? 1 : 0, obb ? 1 : 0};
                    main.processExtractMsg(msg_extract);
                }
            });

            dialog_app_detail.area_share.setOnClickListener(v -> {
                if (dialog_app_detail != null) dialog_app_detail.cancel();

                if (main.settings.getInt(Constants.PREFERENCE_SHAREMODE, Constants.PREFERENCE_SHAREMODE_DEFAULT) == Constants.SHARE_MODE_DIRECT) {
                    main.shareAfterExtract = false;
                    Message msg_share = new Message();
                    msg_share.what = Main.MESSAGE_SHARE_SINGLE_APP;
                    msg_share.obj = Integer.valueOf(position);
                    Main.sendMessage(msg_share);
                } else if (main.settings.getInt(Constants.PREFERENCE_SHAREMODE, Constants.PREFERENCE_SHAREMODE_DEFAULT) == Constants.SHARE_MODE_AFTER_EXTRACT) {
                    // main.shareAfterExtract = true;
                    List<AppItemInfo> list_single = new ArrayList<AppItemInfo>();
                    list_single.add(list_adapter.getAppList().get(position));
                    extractMultiSelectedApps(list_single, true);
                }

            });

            dialog_app_detail.area_detail.setOnClickListener(v -> {
                if (dialog_app_detail != null) dialog_app_detail.cancel();
                Intent app_detail = new Intent();
                app_detail.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                app_detail.setData(Uri.fromParts("package", item.getPackageName(), null));
                main.startActivity(app_detail);
            });

        }
    }

    private void extractMultiSelectedApps(List<AppItemInfo> extract_list, boolean is_share) {
        main.shareAfterExtract = is_share;
        final List<AppItemInfo> list = new ArrayList<AppItemInfo>();
        for (int i = 0; i < extract_list.size(); i++) {
            list.add(new AppItemInfo(extract_list.get(i)));
        }
        main.list_extract_multi = list;
        main.dialog_wait = new AlertDialog.Builder(main)
                .setTitle(main.getResources().getString(R.string.activity_main_wait))
                .setView(LayoutInflater.from(main).inflate(R.layout.layout_extract_multi_extra, null))
                .setCancelable(false)
                .show();
        new Thread(() -> {
            long data = 0, obb = 0;
            for (AppItemInfo item : main.list_extract_multi) {
                long data_get = FileSize.getFileOrFolderSize(new File(StorageUtil.getMainStoragePath() + "/android/data/" + item.packageName));
                long obb_get = FileSize.getFileOrFolderSize(new File(StorageUtil.getMainStoragePath() + "/android/obb/" + item.packageName));
                if (data_get > 0) item.exportData = true;
                if (obb_get > 0) item.exportObb = true;
                data += data_get;
                obb += obb_get;
            }
            Message msg = new Message();
            msg.what = main.MESSAGE_EXTRA_MULTI_SHOW_SELECTION_IAG;
            msg.obj = new Long[]{data, obb};
            main.sendMessage(msg);
        }).start();
    }

}
