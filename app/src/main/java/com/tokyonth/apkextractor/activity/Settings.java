package com.tokyonth.apkextractor.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tokyonth.apkextractor.R;
import com.tokyonth.apkextractor.adapter.SettingsListAdapter;
import com.tokyonth.apkextractor.bean.SettingsContentBean;
import com.tokyonth.apkextractor.data.Constants;

import java.util.ArrayList;
import java.util.List;

public class Settings extends BaseActivity implements AdapterView.OnItemClickListener {

    private Toolbar toolbar;
    private ListView settings_list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);
        initView();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getResources().getString(R.string.action_settings));
    }

    @Override
    public void processMessage(Message msg) { }

    private void initView() {
        settings_list = (ListView) findViewById(R.id.settings_list);
        List<SettingsContentBean> list = new ArrayList<>();
        list.add(new SettingsContentBean(getResources().getString(R.string.action_editpath), R.drawable.ic_settings_export));
        list.add(new SettingsContentBean(getResources().getString(R.string.action_filename), R.drawable.ic_settings_rule));
        list.add(new SettingsContentBean(getResources().getString(R.string.action_sharemode), R.drawable.ic_settings_share));
        SettingsListAdapter adapter = new SettingsListAdapter(this, R.layout.settings_item, list);
        settings_list.setAdapter(adapter);
        settings_list.setOnItemClickListener(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private String getFormatExportFileName(String apk, String zip) {
        final String PREVIEW_APP_NAME = getResources().getString(R.string.dialog_filename_preview_appname);
        final String PREVIEW_PACKAGE_NAME = getResources().getString(R.string.dialog_filename_preview_packagename);
        final String PREVIEW_VERSION = getResources().getString(R.string.dialog_filename_preview_version);
        final String PREVIEW_VERSIONCODE = getResources().getString(R.string.dialog_filename_preview_versioncode);
        return getResources().getString(R.string.preview) + ":\n\nAPK:  " + apk.replace(Constants.FONT_APP_NAME, PREVIEW_APP_NAME)
                .replace(Constants.FONT_APP_PACKAGE_NAME, PREVIEW_PACKAGE_NAME).replace(Constants.FONT_APP_VERSIONCODE, PREVIEW_VERSIONCODE).replace(Constants.FONT_APP_VERSIONNAME, PREVIEW_VERSION) + ".apk\n\n"
                + "ZIP:  " + zip.replace(Constants.FONT_APP_NAME, PREVIEW_PACKAGE_NAME)
                .replace(Constants.FONT_APP_PACKAGE_NAME, PREVIEW_PACKAGE_NAME).replace(Constants.FONT_APP_VERSIONCODE, PREVIEW_VERSIONCODE).replace(Constants.FONT_APP_VERSIONNAME, PREVIEW_VERSION) + ".zip";
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                Intent i = new Intent();
                i.setClass(this, FolderSelector.class);
                startActivity(i);
                break;
            case 1:
                final View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_filename, null);
                final EditText edit_apk = (EditText) dialogView.findViewById(R.id.filename_apk);
                final EditText edit_zip = (EditText) dialogView.findViewById(R.id.filename_zip);
                final TextView preview = ((TextView) dialogView.findViewById(R.id.filename_preview));
                final Spinner spinner = ((Spinner) dialogView.findViewById(R.id.spinner_zip_level));

                edit_apk.setText(settings.getString(Constants.PREFERENCE_FILENAME_FONT_APK, Constants.PREFERENCE_FILENAME_FONT_DEFAULT));
                edit_zip.setText(settings.getString(Constants.PREFERENCE_FILENAME_FONT_ZIP, Constants.PREFERENCE_FILENAME_FONT_DEFAULT));
                preview.setText(getFormatExportFileName(edit_apk.getText().toString(), edit_zip.getText().toString()));
                spinner.setAdapter(new ArrayAdapter<String>(this, R.layout.layout_item_spinner_text, R.id.item_storage_text, new String[]{getResources().getString(R.string.zip_level_default),
                        getResources().getString(R.string.zip_level_stored), getResources().getString(R.string.zip_level_low), getResources().getString(R.string.zip_level_normal), getResources().getString(R.string.zip_level_high)}));
                int level_set = settings.getInt(Constants.PREFERENCE_ZIP_COMPRESS_LEVEL, Constants.PREFERENCE_ZIP_COMPRESS_LEVEL_DEFAULT);
                try {
                    switch (level_set) {
                        default:
                            spinner.setSelection(0);
                            break;
                        case Constants.ZIP_LEVEL_STORED:
                            spinner.setSelection(1);
                            break;
                        case Constants.ZIP_LEVEL_LOW:
                            spinner.setSelection(2);
                            break;
                        case Constants.ZIP_LEVEL_NORMAL:
                            spinner.setSelection(3);
                            break;
                        case Constants.ZIP_LEVEL_HIGH:
                            spinner.setSelection(4);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!edit_apk.getText().toString().contains(Constants.FONT_APP_NAME) && !edit_apk.getText().toString().contains(Constants.FONT_APP_PACKAGE_NAME)
                        && !edit_apk.getText().toString().contains(Constants.FONT_APP_VERSIONCODE) && !edit_apk.getText().toString().contains(Constants.FONT_APP_VERSIONNAME)) {
                    dialogView.findViewById(R.id.filename_apk_warn).setVisibility(View.VISIBLE);
                } else {
                    dialogView.findViewById(R.id.filename_apk_warn).setVisibility(View.GONE);
                }

                if (!edit_zip.getText().toString().contains(Constants.FONT_APP_NAME) && !edit_zip.getText().toString().contains(Constants.FONT_APP_PACKAGE_NAME)
                        && !edit_zip.getText().toString().contains(Constants.FONT_APP_VERSIONCODE) && !edit_zip.getText().toString().contains(Constants.FONT_APP_VERSIONNAME)) {
                    dialogView.findViewById(R.id.filename_zip_warn).setVisibility(View.VISIBLE);
                } else {
                    dialogView.findViewById(R.id.filename_zip_warn).setVisibility(View.GONE);
                }

                final AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_filename_title))
                        .setView(dialogView)
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive), null)
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), (dialog1, which) -> {

                        })
                        .show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (edit_apk.getText().toString().trim().equals("") || edit_zip.getText().toString().trim().equals("")) {
                        Toast.makeText(this, getResources().getString(R.string.dialog_filename_toast_blank), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String apk_replaced_variables = edit_apk.getText().toString().replace(Constants.FONT_APP_NAME, "").replace(Constants.FONT_APP_PACKAGE_NAME, "").replace(Constants.FONT_APP_VERSIONCODE, "").replace(Constants.FONT_APP_VERSIONNAME, "");
                    String zip_replaced_variables = edit_zip.getText().toString().replace(Constants.FONT_APP_NAME, "").replace(Constants.FONT_APP_PACKAGE_NAME, "").replace(Constants.FONT_APP_VERSIONCODE, "").replace(Constants.FONT_APP_VERSIONNAME, "");
                    if (apk_replaced_variables.contains("?") || apk_replaced_variables.contains("\\") || apk_replaced_variables.contains("/") || apk_replaced_variables.contains(":") || apk_replaced_variables.contains("*") || apk_replaced_variables.contains("\"")
                            || apk_replaced_variables.contains("<") || apk_replaced_variables.contains(">") || apk_replaced_variables.contains("|")
                            || zip_replaced_variables.contains("?") || zip_replaced_variables.contains("\\") || zip_replaced_variables.contains("/") || zip_replaced_variables.contains(":") || zip_replaced_variables.contains("*") || zip_replaced_variables.contains("\"")
                            || zip_replaced_variables.contains("<") || zip_replaced_variables.contains(">") || zip_replaced_variables.contains("|")) {
                        Toast.makeText(this, getResources().getString(R.string.activity_folder_selector_invalid_foldername), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    editor.putString(Constants.PREFERENCE_FILENAME_FONT_APK, edit_apk.getText().toString());
                    editor.putString(Constants.PREFERENCE_FILENAME_FONT_ZIP, edit_zip.getText().toString());
                    int zip_selection = spinner.getSelectedItemPosition();
                    switch (zip_selection) {
                        default:
                            break;
                        case 0:
                            editor.putInt(Constants.PREFERENCE_ZIP_COMPRESS_LEVEL, Constants.PREFERENCE_ZIP_COMPRESS_LEVEL_DEFAULT);
                            break;
                        case 1:
                            editor.putInt(Constants.PREFERENCE_ZIP_COMPRESS_LEVEL, Constants.ZIP_LEVEL_STORED);
                            break;
                        case 2:
                            editor.putInt(Constants.PREFERENCE_ZIP_COMPRESS_LEVEL, Constants.ZIP_LEVEL_LOW);
                            break;
                        case 3:
                            editor.putInt(Constants.PREFERENCE_ZIP_COMPRESS_LEVEL, Constants.ZIP_LEVEL_NORMAL);
                            break;
                        case 4:
                            editor.putInt(Constants.PREFERENCE_ZIP_COMPRESS_LEVEL, Constants.ZIP_LEVEL_HIGH);
                            break;
                    }
                    editor.apply();
                    dialog.cancel();
                });
                edit_apk.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        preview.setText(getFormatExportFileName(edit_apk.getText().toString(), edit_zip.getText().toString()));
                        if (!edit_apk.getText().toString().contains(Constants.FONT_APP_NAME) && !edit_apk.getText().toString().contains(Constants.FONT_APP_PACKAGE_NAME)
                                && !edit_apk.getText().toString().contains(Constants.FONT_APP_VERSIONCODE) && !edit_apk.getText().toString().contains(Constants.FONT_APP_VERSIONNAME)) {
                            dialogView.findViewById(R.id.filename_apk_warn).setVisibility(View.VISIBLE);
                        } else {
                            dialogView.findViewById(R.id.filename_apk_warn).setVisibility(View.GONE);
                        }
                    }

                });
                edit_zip.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        preview.setText(getFormatExportFileName(edit_apk.getText().toString(), edit_zip.getText().toString()));
                        if (!edit_zip.getText().toString().contains(Constants.FONT_APP_NAME) && !edit_zip.getText().toString().contains(Constants.FONT_APP_PACKAGE_NAME)
                                && !edit_zip.getText().toString().contains(Constants.FONT_APP_VERSIONCODE) && !edit_zip.getText().toString().contains(Constants.FONT_APP_VERSIONNAME)) {
                            dialogView.findViewById(R.id.filename_zip_warn).setVisibility(View.VISIBLE);
                        } else {
                            dialogView.findViewById(R.id.filename_zip_warn).setVisibility(View.GONE);
                        }
                    }

                });

                dialogView.findViewById(R.id.filename_appname).setOnClickListener(v -> {
                    if (edit_apk.isFocused()) {
                        edit_apk.getText().insert(edit_apk.getSelectionStart(), Constants.FONT_APP_NAME);
                    }
                    if (edit_zip.isFocused()) {
                        edit_zip.getText().insert(edit_zip.getSelectionStart(), Constants.FONT_APP_NAME);
                    }
                });

                dialogView.findViewById(R.id.filename_packagename).setOnClickListener(v -> {
                    if (edit_apk.isFocused()) {
                        edit_apk.getText().insert(edit_apk.getSelectionStart(), Constants.FONT_APP_PACKAGE_NAME);
                    }
                    if (edit_zip.isFocused()) {
                        edit_zip.getText().insert(edit_zip.getSelectionStart(), Constants.FONT_APP_PACKAGE_NAME);
                    }
                });

                dialogView.findViewById(R.id.filename_version).setOnClickListener(v -> {
                    if (edit_apk.isFocused()) {
                        edit_apk.getText().insert(edit_apk.getSelectionStart(), Constants.FONT_APP_VERSIONNAME);
                    }
                    if (edit_zip.isFocused()) {
                        edit_zip.getText().insert(edit_zip.getSelectionStart(), Constants.FONT_APP_VERSIONNAME);
                    }
                });

                dialogView.findViewById(R.id.filename_versioncode).setOnClickListener(v -> {
                    if (edit_apk.isFocused()) {
                        edit_apk.getText().insert(edit_apk.getSelectionStart(), Constants.FONT_APP_VERSIONCODE);
                    }
                    if (edit_zip.isFocused()) {
                        edit_zip.getText().insert(edit_zip.getSelectionStart(), Constants.FONT_APP_VERSIONCODE);
                    }
                });

                dialogView.findViewById(R.id.filename_connector).setOnClickListener(v -> {
                    if (edit_apk.isFocused()) {
                        edit_apk.getText().insert(edit_apk.getSelectionStart(), "-");
                    }
                    if (edit_zip.isFocused()) {
                        edit_zip.getText().insert(edit_zip.getSelectionStart(), "-");
                    }
                });

                dialogView.findViewById(R.id.filename_upderline).setOnClickListener(v -> {
                    if (edit_apk.isFocused()) {
                        edit_apk.getText().insert(edit_apk.getSelectionStart(), "_");
                    }
                    if (edit_zip.isFocused()) {
                        edit_zip.getText().insert(edit_zip.getSelectionStart(), "_");
                    }
                });

                break;
            case 2:
                int mode = settings.getInt(Constants.PREFERENCE_SHAREMODE, Constants.PREFERENCE_SHAREMODE_DEFAULT);
                View dialog_View = LayoutInflater.from(this).inflate(R.layout.layout_dialog_sharemode, null, false);
                RadioButton ra_direct = ((RadioButton) dialog_View.findViewById(R.id.share_mode_direct_ra));
                RadioButton ra_after_extract = ((RadioButton) dialog_View.findViewById(R.id.share_mode_after_extract_ra));
                ra_direct.setChecked(mode == Constants.SHARE_MODE_DIRECT);
                ra_after_extract.setChecked(mode == Constants.SHARE_MODE_AFTER_EXTRACT);
                final AlertDialog share_dialog = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.action_sharemode))
                        .setView(dialog_View)
                        .show();
                share_dialog.findViewById(R.id.share_mode_direct).setOnClickListener(v -> {
                    editor.putInt(Constants.PREFERENCE_SHAREMODE, Constants.SHARE_MODE_DIRECT);
                    editor.apply();
                    share_dialog.cancel();
                });
                dialog_View.findViewById(R.id.share_mode_after_extract).setOnClickListener(v -> {
                    editor.putInt(Constants.PREFERENCE_SHAREMODE, Constants.SHARE_MODE_AFTER_EXTRACT);
                    editor.apply();
                    share_dialog.cancel();
                });
                break;
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
