package com.tokyonth.apkextractor.listener;

import com.tokyonth.apkextractor.activity.Main;
import com.tokyonth.apkextractor.adapter.AppListAdapter;

public class ListenerMultiSelectMode implements AppListAdapter.OnItemClickListener {

    private Main main;

    public ListenerMultiSelectMode(Main main) {
        this.main = main;
    }

    @Override
    public void onItemClick(int position) {
        main.updateMultiSelectMode(position);
    }

}
