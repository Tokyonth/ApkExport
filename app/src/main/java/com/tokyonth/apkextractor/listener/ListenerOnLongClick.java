package com.tokyonth.apkextractor.listener;

import com.tokyonth.apkextractor.activity.Main;
import com.tokyonth.apkextractor.adapter.AppListAdapter;

public class ListenerOnLongClick implements AppListAdapter.OnLongClickListener {

    private Main main;

    public ListenerOnLongClick(Main main) {
        this.main = main;
    }

    @Override
    public boolean onLongClick(int position) {
        main.startMultiSelectMode(position);
        return false;
    }
}
