package com.tokyonth.apkextractor.activity;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tokyonth.apkextractor.R;
import com.tokyonth.apkextractor.adapter.AppListAdapter;
import com.tokyonth.apkextractor.data.AppItemInfo;
import com.tokyonth.apkextractor.data.Constants;
import com.tokyonth.apkextractor.listener.ListenerMultiSelectMode;
import com.tokyonth.apkextractor.listener.ListenerNormalMode;
import com.tokyonth.apkextractor.listener.ListenerOnLongClick;
import com.tokyonth.apkextractor.searchbox.SearchFragment;
import com.tokyonth.apkextractor.ui.FileCopyDialog;
import com.tokyonth.apkextractor.ui.LoadListDialog;
import com.tokyonth.apkextractor.ui.SortDialog;
import com.tokyonth.apkextractor.utils.CopyFilesTask;
import com.tokyonth.apkextractor.utils.FileSize;
import com.tokyonth.apkextractor.utils.SearchTask;
import com.tokyonth.apkextractor.utils.StorageUtil;

public class Main extends BaseMsg implements View.OnClickListener {

    public boolean shareAfterExtract = false;
    public boolean showSystemApp = false;
    public boolean isMultiSelectMode = false, isSearchMode = false;

    public LoadListDialog dialog_load_list;
    public FileCopyDialog dialog_copy_file;
    public SortDialog dialog_sort;
    public AlertDialog dialog_wait;
    public List<AppItemInfo> list_extract_multi = new ArrayList<AppItemInfo>();
    public Thread thread_app_info, thread_search, thread_extract_app;
    public CopyFilesTask runnable_extract_app;
    public RecyclerView app_list;

    private SearchTask runnable_search;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private CardView cardView;
    private Menu menu;
    private ProgressBar pg_search;
    private String keyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.layout_main);
        initView();
        getPermission();
        BaseInit(this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        getSystemService(Context.INPUT_METHOD_SERVICE);

        dialog_load_list = new LoadListDialog(this);
        dialog_load_list.setTitle(getResources().getString(R.string.activity_main_loading));
        dialog_load_list.setCancelable(false);
        dialog_load_list.setCanceledOnTouchOutside(false);
        dialog_load_list.setMax(getPackageManager().getInstalledPackages(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).size());

        AppItemInfo.SortConfig = settings.getInt(Constants.PREFERENCE_SORT_CONFIG, 0);
        showSystemApp = settings.getBoolean(Constants.PREFERENCE_SHOW_SYSTEM_APP, false);
        ((CheckBox) findViewById(R.id.showSystemAPP)).setChecked(showSystemApp);
        refreshList(true);
        ((CheckBox) findViewById(R.id.showSystemAPP)).setOnCheckedChangeListener((button, isChecked) -> {
            if (isMultiSelectMode) {
                closeMultiSelectMode();
            }
            showSystemApp = isChecked;
            editor.putBoolean(Constants.PREFERENCE_SHOW_SYSTEM_APP, isChecked);
            editor.apply();
            refreshList(true);
        });
        storage_path = settings.getString(Constants.PREFERENCE_STORAGE_PATH, Constants.PREFERENCE_STORAGE_PATH_DEFAULT);
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        app_list = (RecyclerView) findViewById(R.id.applist);
        cardView = (CardView) findViewById(R.id.card_bar);
        pg_search = (ProgressBar)findViewById(R.id.progressbar_search);
        findViewById(R.id.choice_app_view).setVisibility(View.GONE);
        findViewById(R.id.main_msg_view).setVisibility(View.VISIBLE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        app_list.setLayoutManager(layoutManager);
        app_list.setHasFixedSize(true);

        setSupportActionBar(toolbar);
        fab.setOnClickListener(this);
    }

    public void processExtractMsg(Message msg) {
        isExtractSuccess = true;
        errorMessage = "";
        if (msg.what == MESSAGE_EXTRACT_SINGLE_APP || msg.what == MESSAGE_EXTRACT_MULTI_APP) {
            BaseActivity.sendMessage(msg);
        }
    }

    private void extractMultiSelectedApps(List<AppItemInfo> extract_list, boolean is_share) {
        shareAfterExtract = is_share;
        final List<AppItemInfo> list = new ArrayList<AppItemInfo>();
        for (int i = 0; i < extract_list.size(); i++) {
            list.add(new AppItemInfo(extract_list.get(i)));
        }
        list_extract_multi = list;
        dialog_wait = new AlertDialog.Builder(Main.this)
                .setTitle(getResources().getString(R.string.activity_main_wait))
                .setView(LayoutInflater.from(Main.this).inflate(R.layout.layout_extract_multi_extra, null))
                .setCancelable(false)
                .show();
        new Thread(() -> {
            long data = 0, obb = 0;
            for (AppItemInfo item : list_extract_multi) {
                long data_get = FileSize.getFileOrFolderSize(new File(StorageUtil.getMainStoragePath() + "/android/data/" + item.packageName));
                long obb_get = FileSize.getFileOrFolderSize(new File(StorageUtil.getMainStoragePath() + "/android/obb/" + item.packageName));
                if (data_get > 0) item.exportData = true;
                if (obb_get > 0) item.exportObb = true;
                data += data_get;
                obb += obb_get;
            }
            Message msg = new Message();
            msg.what = MESSAGE_EXTRA_MULTI_SHOW_SELECTION_IAG;
            msg.obj = new Long[]{data, obb};
            sendMessage(msg);
        }).start();
    }

    private void closeSearchView(){
        isSearchMode = false;
        if(runnable_search!=null){
            runnable_search.setInterrupted();
        }
        if(this.thread_search!=null){
            thread_search.interrupt();
            thread_search=null;
        }

        list_adapter = new AppListAdapter(this,listsum,true);
        pg_search.setVisibility(View.GONE);
        app_list.setAdapter(list_adapter);
        list_adapter.setItemClickListener(new ListenerNormalMode(this, list_adapter));
        list_adapter.setLongClickListener(new ListenerOnLongClick(this));

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_multiselectarea_entry);
        cardView.startAnimation(anim);
        cardView.setVisibility(View.VISIBLE);
        setMenuVisible(true);
        Main.sendEmptyMessage(MESSAGE_SET_NORMAL_TEXT_ATT);
    }

    private void showSearchView(){
        updateSearchList(keyword);
        isSearchMode = true;
        list_adapter.setLongClickListener(null);
        setMenuVisible(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isSearchMode)
            Main.sendEmptyMessage(Main.MESSAGE_SET_NORMAL_TEXT_ATT);
    }

    public void startMultiSelectMode(int position) {
        this.isMultiSelectMode = true;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        list_adapter.setMultiSelectMode(position);
        updateMultiSelectMode(position);

        TextView select_all = findViewById(R.id.text_selectall);
        TextView deselect_all = findViewById(R.id.text_deselectall);

        select_all.setClickable(true);
        select_all.setOnClickListener(v -> {
            list_adapter.selectAll();
            updateMultiSelectMode(-1);
        });

        deselect_all.setClickable(true);
        deselect_all.setOnClickListener(v -> {
            list_adapter.deselectAll();
            updateMultiSelectMode(-1);
        });

        list_adapter.setItemClickListener(new ListenerMultiSelectMode(this));
        list_adapter.setLongClickListener(null);

        View multi_select_area = findViewById(R.id.choice_app_view);
        findViewById(R.id.main_msg_view).setVisibility(View.GONE);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_multiselectarea_entry);
        multi_select_area.startAnimation(anim);
        multi_select_area.setVisibility(View.VISIBLE);
    }

    public void updateMultiSelectMode(int position) {
        TextView app_inst = (TextView) findViewById(R.id.appinst);
        TextView extract = (TextView) findViewById(R.id.text_extract);
        TextView share = (TextView) findViewById(R.id.text_share);
        list_adapter.onItemClicked(position);
        app_inst.setText(getResources().getString(R.string.activity_main_multiselect_att_head) + list_adapter.getSelectedNum() + getResources().getString(R.string.activity_main_multiselect_att_item)
                + "\n" + getResources().getString(R.string.activity_main_multiselect_att_end)
                + Formatter.formatFileSize(this, list_adapter.getSelectedAppsSize()));
        extract.setText(Main.this.getResources().getString(R.string.button_extract) + "(" + list_adapter.getSelectedNum() + ")");
        share.setText(Main.this.getResources().getString(R.string.button_share) + "(" + list_adapter.getSelectedNum() + ")");

        if (list_adapter.getSelectedNum() > 0) {
            extract.setClickable(true);
            share.setClickable(true);
            extract.setOnClickListener(this);
            share.setOnClickListener(this);
        } else {
            share.setOnClickListener(null);
            extract.setOnClickListener(null);
        }
    }

    public void closeMultiSelectMode() {
        TextView extract = (TextView) findViewById(R.id.text_extract);
        TextView share = (TextView) findViewById(R.id.text_share);
        TextView select_all = (TextView) findViewById(R.id.text_selectall);
        TextView deselect_all = (TextView) findViewById(R.id.text_deselectall);
        isMultiSelectMode = false;
        extract.setOnClickListener(null);
        share.setOnClickListener(null);
        select_all.setOnClickListener(null);
        deselect_all.setOnClickListener(null);
        Main.sendEmptyMessage(MESSAGE_SET_NORMAL_TEXT_ATT);

        list_adapter.cancelMutiSelectMode();
        list_adapter.setItemClickListener(new ListenerNormalMode(this, list_adapter));
        list_adapter.setLongClickListener(new ListenerOnLongClick(this));

        Animation anim0 = AnimationUtils.loadAnimation(this, R.anim.anim_multiselectarea_exit);
        Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.anim_multiselectarea_entry);

        View multi_select_area = findViewById(R.id.choice_app_view);
        View main_msg_view = findViewById(R.id.main_msg_view);

        multi_select_area.startAnimation(anim0);
        multi_select_area.setVisibility(View.GONE);

        main_msg_view.startAnimation(anim1);
        main_msg_view.setVisibility(View.VISIBLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void refreshList(boolean isShowProcessDialog) {
        app_list.setAdapter(null);
        Main.sendEmptyMessage(MESSAGE_SET_NORMAL_TEXT_ATT);
        ((CheckBox) findViewById(R.id.showSystemAPP)).setEnabled(false);
        listsum = new ArrayList<AppItemInfo>();
        if (dialog_load_list != null && isShowProcessDialog) {
            dialog_load_list.show();
        }
        thread_app_info = new Thread(() -> {
            PackageManager packagemanager = getPackageManager();
            List<PackageInfo> package_list = packagemanager.getInstalledPackages(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            for (int i = 0; i < package_list.size(); i++) {
                PackageInfo pak = (PackageInfo) package_list.get(i);
                AppItemInfo app_item = new AppItemInfo();
                if (!showSystemApp) {
                    if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                        app_item.setIcon(packagemanager.getApplicationIcon(pak.applicationInfo));
                        app_item.setAppName(packagemanager.getApplicationLabel(pak.applicationInfo).toString());
                        app_item.setPackageName(pak.applicationInfo.packageName);
                        app_item.setPackageSize(FileSize.getFileSize(pak.applicationInfo.sourceDir));
                        app_item.setResourcePath(pak.applicationInfo.sourceDir);
                        app_item.setVersion(pak.versionName);
                        app_item.setVersionCode(pak.versionCode);
                        app_item.setLastUpdateTime(pak.lastUpdateTime);
                        if (Build.VERSION.SDK_INT >= 24)
                            app_item.setMinSDKVersion(pak.applicationInfo.minSdkVersion);
                        listsum.add(app_item);
                    }
                } else {
                    app_item.setIcon(packagemanager.getApplicationIcon(pak.applicationInfo));
                    app_item.setAppName(packagemanager.getApplicationLabel(pak.applicationInfo).toString());
                    app_item.setPackageName(pak.applicationInfo.packageName);
                    app_item.setPackageSize(FileSize.getFileSize(pak.applicationInfo.sourceDir));
                    app_item.setResourcePath(pak.applicationInfo.sourceDir);
                    app_item.setVersion(pak.versionName);
                    app_item.setVersionCode(pak.versionCode);
                    app_item.setLastUpdateTime(pak.lastUpdateTime);
                    if (Build.VERSION.SDK_INT >= 24)
                        app_item.setMinSDKVersion(pak.applicationInfo.minSdkVersion);
                    if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
                        app_item.isSystemApp = true;
                    listsum.add(app_item);
                }
                Message msg_this_loop = new Message();
                Integer progress = i + 1;
                msg_this_loop.what = MESSAGE_LOADLIST_REFRESH_PROGRESS;
                msg_this_loop.obj = progress;
                Main.sendMessage(msg_this_loop);

            }
            Main.sendEmptyMessage(MESSAGE_LOADLIST_COMPLETE);
        });
        this.thread_app_info.start();
    }

    private void updateSearchList(String text) {
        final String search_info = text.trim().toLowerCase(Locale.ENGLISH);
        findViewById(R.id.progressbar_search).setVisibility(View.VISIBLE);
        ((RecyclerView) findViewById(R.id.applist)).setAdapter(list_adapter);
        if (thread_search != null) {
            if (runnable_search != null) {
                runnable_search.setInterrupted();
            }
            thread_search.interrupt();
            thread_search = null;
        }
        runnable_search = new SearchTask(search_info);
        thread_search = new Thread(this.runnable_search);
        thread_search.start();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (isMultiSelectMode) {
                closeMultiSelectMode();
            } else if (isSearchMode) {
                closeSearchView();
            } else {
                this.finish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void setMenuVisible(boolean isVisible) {
        if (this.menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                this.menu.getItem(i).setEnabled(isVisible);
                this.menu.getItem(i).setVisible(isVisible);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) { }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            View dialog_view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_about, null);
            dialog_view.findViewById(R.id.layout_about_donate).setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://qr.alipay.com/FKX08041Y09ZGT6ZT91FA5")));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
            AlertDialog dialog_about = new AlertDialog.Builder(Main.this)
                    .setTitle(this.getResources().getString(R.string.dialog_about_title))
                    .setIcon(R.mipmap.ic_launcher_round)
                    .setCancelable(true)
                    .setView(dialog_view)
                    .setPositiveButton(getResources().getString(R.string.dialog_button_positive), (arg0, arg1) -> { }).create();

            dialog_about.show();
            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this, com.tokyonth.apkextractor.activity.Settings.class);
            startActivity(intent);
            return true;
        }
        if (id == android.R.id.home) {
            if (isMultiSelectMode) {
                closeMultiSelectMode();
            } else if (isSearchMode) {
                closeSearchView();
            }
            return true;
        }

        if (id == R.id.action_search) {
            isSearchMode=true;
            if (isMultiSelectMode) {
                closeMultiSelectMode();
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_multiselectarea_entry);
                cardView.startAnimation(anim);
                cardView.setVisibility(View.VISIBLE);
            }
            SearchFragment searchFragment = SearchFragment.newInstance();
            searchFragment.showFragment(getSupportFragmentManager(),SearchFragment.TAG);
            searchFragment.setOnSearchClickListener(keyword -> {
                Toast.makeText(getBaseContext(), keyword, Toast.LENGTH_SHORT).show();
                this.keyword = keyword;
                showSearchView();
            });
            searchFragment.setOnBackClickListener(this::closeSearchView);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_multiselectarea_exit);
            cardView.startAnimation(anim);
            cardView.setVisibility(View.GONE);
        }
        if (id == R.id.action_sort) {
            this.dialog_sort = new SortDialog(this);
            this.dialog_sort.show();
            dialog_sort.r_default.setChecked(AppItemInfo.SortConfig == 0);
            dialog_sort.r_a_appname.setChecked(AppItemInfo.SortConfig == 1);
            dialog_sort.r_d_appname.setChecked(AppItemInfo.SortConfig == 2);
            dialog_sort.r_a_size.setChecked(AppItemInfo.SortConfig == 3);
            dialog_sort.r_d_size.setChecked(AppItemInfo.SortConfig == 4);
            dialog_sort.r_a_date.setChecked(AppItemInfo.SortConfig == 5);
            dialog_sort.r_d_date.setChecked(AppItemInfo.SortConfig == 6);
            this.dialog_sort.r_default.setOnClickListener(v -> {
                if (isMultiSelectMode) {
                    closeMultiSelectMode();
                }
                AppItemInfo.SortConfig = 0;
                ((RecyclerView) findViewById(R.id.applist)).setAdapter(null);
                refreshList(true);
                Main.this.dialog_sort.cancel();
            });

            this.dialog_sort.r_a_appname.setOnClickListener(v -> {
                AppItemInfo.SortConfig = 1;
                sortList();
                dialog_sort.cancel();
            });

            this.dialog_sort.r_d_appname.setOnClickListener(v -> {
                AppItemInfo.SortConfig = 2;
                sortList();
                dialog_sort.cancel();
            });

            this.dialog_sort.r_a_size.setOnClickListener(v -> {
                AppItemInfo.SortConfig = 3;
                sortList();
                dialog_sort.cancel();
            });

            this.dialog_sort.r_d_size.setOnClickListener(v -> {
                AppItemInfo.SortConfig = 4;
                sortList();
                dialog_sort.cancel();
            });

            this.dialog_sort.r_a_date.setOnClickListener(v -> {
                AppItemInfo.SortConfig = 5;
                sortList();
                dialog_sort.cancel();
            });

            this.dialog_sort.r_d_date.setOnClickListener(v -> {
                AppItemInfo.SortConfig = 6;
                sortList();
                dialog_sort.cancel();
            });

            dialog_sort.setOnCancelListener(dialog -> {
                editor.putInt(Constants.PREFERENCE_SORT_CONFIG, AppItemInfo.SortConfig);
                editor.apply();
            });
        }

        return super.onOptionsItemSelected(item);
    }

    public void sortList() {
        if (list_adapter != null && !isSearchMode) {
            if (isMultiSelectMode) {
                closeMultiSelectMode();
            }
            ((CheckBox) findViewById(R.id.showSystemAPP)).setEnabled(false);
            ((RecyclerView) findViewById(R.id.applist)).setAdapter(null);
            new Thread(() -> {
                synchronized (Main.class) {
                    Collections.sort(listsum);
                    sendEmptyMessage(MESSAGE_SORT_LIST_COMPLETE);
                }
            }).start();

        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_extract:
                if (list_adapter == null) return;
                List<AppItemInfo> list_extract = new ArrayList<AppItemInfo>();
                for (int i = 0; i < list_adapter.getAppList().size(); i++) {
                    if (list_adapter.getIsSelected()[i])
                        list_extract.add(list_adapter.getAppList().get(i));
                }
                extractMultiSelectedApps(list_extract, false);
                break;
            case R.id.text_share:
                if (settings.getInt(Constants.PREFERENCE_SHAREMODE, Constants.PREFERENCE_SHAREMODE_DEFAULT) == Constants.SHARE_MODE_DIRECT)
                    Main.sendEmptyMessage(MESSAGE_SHARE_MULTI_APP);
                else if (settings.getInt(Constants.PREFERENCE_SHAREMODE, Constants.PREFERENCE_SHAREMODE_DEFAULT) == Constants.SHARE_MODE_AFTER_EXTRACT) {
                    if (list_adapter == null) return;
                    List<AppItemInfo> list_share = new ArrayList<AppItemInfo>();
                    for (int i = 0; i < list_adapter.getAppList().size(); i++) {
                        if (list_adapter.getIsSelected()[i])
                            list_share.add(list_adapter.getAppList().get(i));
                    }
                    extractMultiSelectedApps(list_share, true);
                }
                break;
            case R.id.fab:
                if (isSearchMode)
                    updateSearchList(keyword);
                else
                    refreshList(true);
                break;
        }
    }

}