package com.tokyonth.apkextractor.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.tokyonth.apkextractor.R;
import com.tokyonth.apkextractor.adapter.AppListAdapter;
import com.tokyonth.apkextractor.data.AppItemInfo;
import com.tokyonth.apkextractor.data.Constants;
import com.tokyonth.apkextractor.listener.ListenerNormalMode;
import com.tokyonth.apkextractor.listener.ListenerOnLongClick;
import com.tokyonth.apkextractor.listener.ListenerStopButton;
import com.tokyonth.apkextractor.multiplexing.FileInfo;
import com.tokyonth.apkextractor.ui.FileCopyDialog;
import com.tokyonth.apkextractor.utils.CopyFilesTask;
import com.tokyonth.apkextractor.utils.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseMsg extends BaseActivity {

    public static final int MESSAGE_SET_NORMAL_TEXT_ATT = 12;
    public static final int MESSAGE_SORT_LIST_COMPLETE = 13;
    public static final int MESSAGE_EXTRACT_SINGLE_APP = 20;
    public static final int MESSAGE_EXTRACT_MULTI_APP = 21;
    public static final int MESSAGE_SHARE_SINGLE_APP = 22;
    public static final int MESSAGE_SHARE_MULTI_APP = 23;
    public static final int MESSAGE_SEARCH_COMPLETE = 30;
    public static final int MESSAGE_EXTRA_MULTI_SHOW_SELECTION_IAG = 60;

    public String errorMessage = "";
    public boolean isExtractSuccess = true;
    public static AppListAdapter list_adapter;
    private static Main main;

    public static void BaseInit(Main main0) {
        main = main0;
    }

    @Override
    public void processMessage(Message msg) {
        switch (msg.what) {
            default:
                break;
            case MESSAGE_SET_NORMAL_TEXT_ATT: {
                if (!main.isMultiSelectMode && !main.isSearchMode) {
                    ((TextView) findViewById(R.id.appinst)).setText(main.getResources().getString(R.string.text_appinst) + "\n" + this.getResources().getString(R.string.text_avaliableroom) + Formatter.formatFileSize(this, StorageUtil.getAvaliableSizeOfPath(storage_path)));
                }
            }
            break;

            case MESSAGE_LOADLIST_COMPLETE: {
                if (main.dialog_load_list != null) main.dialog_load_list.cancel();
                list_adapter = new AppListAdapter(this, listsum, true);
                list_adapter.setItemClickListener(new ListenerNormalMode(main, list_adapter));
                list_adapter.setLongClickListener(new ListenerOnLongClick(main));
                main.app_list.setAdapter(list_adapter);

                if (AppItemInfo.SortConfig != 0) main.sortList();
                else {
                    ((CheckBox) findViewById(R.id.showSystemAPP)).setEnabled(true);
                }
            }
            break;
            case MESSAGE_SEARCH_COMPLETE: {
                list_adapter = new AppListAdapter(this, listsearch, false);
                main.app_list.setAdapter(list_adapter);
                list_adapter.setItemClickListener(new ListenerNormalMode(main, list_adapter));
                findViewById(R.id.progressbar_search).setVisibility(View.GONE);
            }
            break;
            case MESSAGE_LOADLIST_REFRESH_PROGRESS: {
                if (main.dialog_load_list != null) {
                    Integer progress = (Integer) msg.obj;
                    main.dialog_load_list.setProgress(progress);
                }
            }
            break;
            case MESSAGE_EXTRACT_SINGLE_APP: {
                List<AppItemInfo> list;
                Integer position[] = (Integer[]) msg.obj;
                if (list_adapter != null) {
                    list = list_adapter.getAppList();
                    if (list != null) {
                        if (list.size() > 0) {
                            List<AppItemInfo> export_list = new ArrayList<AppItemInfo>();
                            AppItemInfo item = new AppItemInfo(list.get(position[0]));
                            if (position[1] == 1) item.exportData = true;
                            if (position[2] == 1) item.exportObb = true;
                            export_list.add(item);
                            main.runnable_extract_app = new CopyFilesTask(export_list, this);
                            main.thread_extract_app = new Thread(main.runnable_extract_app);
                            main.dialog_copy_file = new FileCopyDialog(this);
                            main.dialog_copy_file.setCancelable(false);
                            main.dialog_copy_file.setCanceledOnTouchOutside(false);
                            main.dialog_copy_file.setMax(list.get(position[0]).getPackageSize());
                            main.dialog_copy_file.setIcon(list.get(position[0]).getIcon());
                            main.dialog_copy_file.setTitle(getResources().getString(R.string.activity_main_extracting_title));
                            main.dialog_copy_file.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.activity_main_stop), new ListenerStopButton(main));
                            main.dialog_copy_file.show();
                            main.thread_extract_app.start();
                        }
                    }
                }
            }
            break;

            case MESSAGE_EXTRACT_MULTI_APP: {
                if (!main.isSearchMode) main.closeMultiSelectMode();
                if (main.list_extract_multi == null) return;
                String msg_duplicate = "";
                boolean isDuplicate = false;
                for (AppItemInfo item : main.list_extract_multi) {
                    List<AppItemInfo> checklist = new ArrayList<AppItemInfo>();
                    checklist.add(item);
                    if (item.exportData || item.exportObb) {
                        String duplicate = FileInfo.getDuplicateFileInfo(this, checklist, "zip");
                        if (duplicate.length() > 0) {
                            isDuplicate = true;
                            msg_duplicate += duplicate;
                        }
                    } else {
                        String duplicate = FileInfo.getDuplicateFileInfo(this, checklist, "apk");
                        if (duplicate.length() > 0) {
                            isDuplicate = true;
                            msg_duplicate += duplicate;
                        }
                    }
                }
                if (isDuplicate) {
                    new AlertDialog.Builder(this)
                            .setIcon(R.drawable.ic_icon_warn)
                            .setTitle(getResources().getString(R.string.activity_main_duplicate_title))
                            .setCancelable(true)
                            .setMessage(getResources().getString(R.string.activity_main_duplicate_message) + "\n\n" + msg_duplicate)
                            .setPositiveButton(getResources().getString(R.string.dialog_button_positive), (dialog, which) -> {
                                main.runnable_extract_app = new CopyFilesTask(main.list_extract_multi, main);
                                main.thread_extract_app = new Thread(main.runnable_extract_app);
                                main.dialog_copy_file = new FileCopyDialog(main);
                                main.dialog_copy_file.setTitle(getResources().getString(R.string.activity_main_extracting_title));
                                main.dialog_copy_file.setIcon(R.mipmap.ic_launcher);
                                main.dialog_copy_file.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.activity_main_stop), new ListenerStopButton(main));
                                main.dialog_copy_file.setCancelable(false);
                                main.dialog_copy_file.setCanceledOnTouchOutside(false);
                                main.dialog_copy_file.show();
                                main.thread_extract_app.start();
                            })
                            .setNegativeButton(getResources().getString(R.string.dialog_button_negative), (dialog, which) -> { })
                            //  .setNeutralButton(getResources().getString(R.string.dialog_button_skip), (dialog, which) -> { })
                            .show();
                } else {
                    main.runnable_extract_app = new CopyFilesTask(main.list_extract_multi, main);
                    main.thread_extract_app = new Thread(main.runnable_extract_app);
                    main.dialog_copy_file = new FileCopyDialog(main);
                    main.dialog_copy_file.setTitle(getResources().getString(R.string.activity_main_extracting_title));
                    main.dialog_copy_file.setIcon(R.mipmap.ic_launcher);
                    main.dialog_copy_file.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.activity_main_stop), new ListenerStopButton(main));
                    main.dialog_copy_file.setCancelable(false);
                    main.dialog_copy_file.setCanceledOnTouchOutside(false);
                    main.dialog_copy_file.show();
                    main.thread_extract_app.start();
                }
            }
            break;
            case MESSAGE_COPYFILE_CURRENTAPP: {
                Integer i = (Integer) msg.obj;
                if (main.dialog_copy_file == null) return;
                if (main.runnable_extract_app == null) return;
                try {
                    main.dialog_copy_file.setIcon(main.runnable_extract_app.applist.get(i).icon);
                    main.dialog_copy_file.setTitle(getResources().getString(R.string.activity_main_extracting_title) + (i + 1) + "/" + main.runnable_extract_app.applist.size() + " " + main.runnable_extract_app.applist.get(i).appName);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            break;
            case MESSAGE_COPYFILE_CURRENTFILE: {
                String currentFile = (String) msg.obj;
                if (main.dialog_copy_file != null) {
                    ((TextView) main.dialog_copy_file.findViewById(R.id.currentfile)).setText(currentFile);
                }
            }
            break;
            case MESSAGE_COPYFILE_REFRESH_PROGRESS: {
                Long progress[] = (Long[]) msg.obj;
                if (main.dialog_copy_file != null) {
                    main.dialog_copy_file.setMax(progress[1]);
                    main.dialog_copy_file.setProgress(progress[0]);
                }

            }
            break;
            case MESSAGE_COPYFILE_REFRESH_SPEED: {
                Long speed = (Long) msg.obj;
                if (main.dialog_copy_file != null) {
                    main.dialog_copy_file.setSpeed(speed);
                }
            }
            break;
            case MESSAGE_COPYFILE_COMPLETE: {
                if (main.dialog_copy_file != null) {
                    main.dialog_copy_file.cancel();
                }
                if (isExtractSuccess)
                    Toast.makeText(this, getResources().getString(R.string.activity_main_complete) + savepath, Toast.LENGTH_LONG).show();
                if (!main.isSearchMode) {
                    Main.sendEmptyMessage(MESSAGE_SET_NORMAL_TEXT_ATT);
                }

                if (!isExtractSuccess) {
                    new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.attention))
                            .setIcon(R.drawable.ic_icon_warn)
                            .setMessage(getResources().getString(R.string.activity_main_exception_head) + errorMessage + getResources().getString(R.string.activity_main_exception_end))
                            .setPositiveButton(getResources().getString(R.string.dialog_button_positive), (dialog, which) -> { })
                            .show();
                }
                isExtractSuccess = true;
                errorMessage = "";
                if (main.shareAfterExtract && settings.getInt(Constants.PREFERENCE_SHAREMODE, Constants.PREFERENCE_SHAREMODE_DEFAULT) == Constants.SHARE_MODE_AFTER_EXTRACT) {
                    try {
                        List<String> paths = (List<String>) msg.obj;
                        Intent i = new Intent();
                        i.setType("application/x-zip-compressed");
                        if (paths.size() == 1) {
                            i.setAction(Intent.ACTION_SEND);
                            Uri uri = Uri.fromFile(new File(paths.get(0)));
                            i.putExtra(Intent.EXTRA_STREAM, uri);
                        } else {
                            i.setAction(Intent.ACTION_SEND_MULTIPLE);
                            ArrayList<Uri> uris = new ArrayList<Uri>();
                            for (int n = 0; n < paths.size(); n++) {
                                uris.add(Uri.fromFile(new File(paths.get(n))));
                            }
                            i.putExtra(Intent.EXTRA_STREAM, uris);
                        }
                        i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share));
                        i.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent.createChooser(i, getResources().getString(R.string.share)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
            break;

            case MESSAGE_SHARE_SINGLE_APP: {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    Integer position = (Integer) msg.obj;
                    int pos = position.intValue();
                    List<AppItemInfo> list = list_adapter.getAppList();
                    String apk_path = list.get(pos).getResourcePath();
                    File apk = new File(apk_path);
                    Uri uri = Uri.fromFile(apk);
                    intent.setType("application/vnd.android.package-archive");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share) + list.get(pos).getAppName());
                    intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share) + list.get(pos).getAppName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.share) + list.get(pos).getAppName()));

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
            break;

            case MESSAGE_SHARE_MULTI_APP: {
                try {
                    List<AppItemInfo> list = list_adapter.getAppList();
                    boolean isSelected[] = list_adapter.getIsSelected();
                    Intent intent;
                    if (list_adapter.getSelectedNum() > 1) {
                        intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        ArrayList<Uri> uris = new ArrayList<Uri>();
                        for (int i = 0; i < list.size(); i++) {
                            if (isSelected[i]) {
                                File file = new File(list.get(i).getResourcePath());
                                if (file.exists() && !file.isDirectory()) {
                                    uris.add(Uri.fromFile(file));
                                }
                            }
                        }
                        intent.putExtra(Intent.EXTRA_STREAM, uris);
                        intent.setType("application/vnd.android.package-archive");
                        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.activity_main_share_title));
                        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.activity_main_share_title));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent.createChooser(intent, getResources().getString(R.string.activity_main_share_title)));

                    } else if (list_adapter.getSelectedNum() == 1) {
                        intent = new Intent(Intent.ACTION_SEND);
                        String path = "", app_name = "";
                        for (int j = 0; j < list.size(); j++) {
                            if (isSelected[j]) {
                                path = list.get(j).getResourcePath();
                                app_name = list.get(j).getAppName();
                                break;
                            }
                        }
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
                        intent.setType("application/vnd.android.package-archive");
                        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share) + app_name);
                        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share) + app_name);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent.createChooser(intent, getResources().getString(R.string.share) + app_name));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
            break;

            case MESSAGE_COPYFILE_FILE_NOTFOUND_EXCEPTION: {
                isExtractSuccess = false;
                errorMessage += (String) msg.obj;
                errorMessage += "\n\n";
                break;
            }

            case MESSAGE_COPYFILE_IOEXCEPTION: {
                isExtractSuccess = false;
            }
            break;
            case MESSAGE_EXTRA_MULTI_SHOW_SELECTION_IAG: {
                if (main.dialog_wait == null) return;
                if (msg.obj == null) return;
                main.dialog_wait.cancel();
                main.dialog_wait = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.activity_main_extract_multi_additional_title))
                        .setView(LayoutInflater.from(this).inflate(R.layout.layout_extract_multi_extra, null))
                        .setPositiveButton(getResources().getString(R.string.dialog_button_continue), null)
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), null)
                        .show();
                final CheckBox cb_data = (CheckBox) main.dialog_wait.findViewById(R.id.extract_multi_data_cb);
                final CheckBox cb_obb = (CheckBox) main.dialog_wait.findViewById(R.id.extract_multi_obb_cb);
                Long[] values = (Long[]) msg.obj;
                cb_data.setEnabled(values[0] > 0);
                cb_obb.setEnabled(values[1] > 0);
                if (values[0] <= 0 && values[1] <= 0) {
                    main.dialog_wait.cancel();
                    Message msg_extract_multi = new Message();
                    msg_extract_multi.what = MESSAGE_EXTRACT_MULTI_APP;
                    main.processExtractMsg(msg_extract_multi);
                } else {
                    main.dialog_wait.findViewById(R.id.extract_multi_wait).setVisibility(View.GONE);
                    main.dialog_wait.findViewById(R.id.extract_multi_selections).setVisibility(View.VISIBLE);
                    cb_data.setText("Data(" + Formatter.formatFileSize(main, values[0]) + ")");
                    cb_obb.setText("Obb(" + Formatter.formatFileSize(main, values[1]) + ")");
                    main.dialog_wait.setCancelable(true);
                    main.dialog_wait.setCanceledOnTouchOutside(true);
                    main. dialog_wait.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                        main.dialog_wait.cancel();
                        if (!cb_data.isChecked()) {
                            for (AppItemInfo item : main.list_extract_multi) {
                                item.exportData = false;
                            }
                        }
                        if (!cb_obb.isChecked()) {
                            for (AppItemInfo item : main.list_extract_multi) {
                                item.exportObb = false;
                            }
                        }
                        Message msg_extract_multi = new Message();
                        msg_extract_multi.what = MESSAGE_EXTRACT_MULTI_APP;
                        main.processExtractMsg(msg_extract_multi);
                    });

                    main.dialog_wait.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> main.dialog_wait.cancel());
                }

            }
            break;
            case MESSAGE_SORT_LIST_COMPLETE: {
                list_adapter = new AppListAdapter(this, listsum, true);
                ((RecyclerView) findViewById(R.id.applist)).setAdapter(list_adapter);
                list_adapter.setItemClickListener(new ListenerNormalMode(main, list_adapter));
                list_adapter.setLongClickListener(new ListenerOnLongClick(main));
                ((CheckBox) findViewById(R.id.showSystemAPP)).setEnabled(true);
            }
            break;
        }

    }

}
