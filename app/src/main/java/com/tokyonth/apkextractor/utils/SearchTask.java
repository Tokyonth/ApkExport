package com.tokyonth.apkextractor.utils;

import com.tokyonth.apkextractor.activity.BaseActivity;
import com.tokyonth.apkextractor.activity.Main;
import com.tokyonth.apkextractor.data.AppItemInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchTask implements Runnable {

    private String searchinfo;
    private boolean ifCanSearch = true;
    private List<AppItemInfo> listsearch, listsum;

    public SearchTask(String searchinfo) {
        this.searchinfo = searchinfo.trim().toLowerCase(Locale.ENGLISH);
        this.ifCanSearch = true;
        this.listsum = BaseActivity.listsum;
        this.listsearch = new ArrayList<AppItemInfo>();
    }

    @Override
    public void run() {
        if (this.listsum != null) {
            listsearch.clear();
            if (searchinfo.length() > 0) {
                for (int i = 0; i < this.listsum.size(); i++) {

                    if (this.ifCanSearch) {
                        try {
                            if (this.listsum.get(i).getAppName().toLowerCase(Locale.ENGLISH).indexOf(searchinfo) != -1
                                    || this.listsum.get(i).getPackageName().toLowerCase(Locale.ENGLISH).indexOf(searchinfo) != -1
                                    || this.listsum.get(i).getVersion().toLowerCase(Locale.ENGLISH).indexOf(searchinfo) != -1
                                    || PinYin.getFullSpell(this.listsum.get(i).getAppName()).toLowerCase(Locale.ENGLISH).indexOf(searchinfo) != -1
                                    || PinYin.getFirstSpell(this.listsum.get(i).getAppName()).toLowerCase(Locale.ENGLISH).indexOf(searchinfo) != -1
                                    || PinYin.getPinYin(this.listsum.get(i).getAppName()).toLowerCase(Locale.ENGLISH).indexOf(searchinfo) != -1
                            ) {
                                listsearch.add(this.listsum.get(i));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        listsearch.clear();
                        break;
                    }
                }
            } else {
                listsearch.clear();
            }
        } else {
            this.ifCanSearch = false;
        }
        if (this.ifCanSearch) {
            BaseActivity.listsearch = this.listsearch;
            BaseActivity.sendEmptyMessage(Main.MESSAGE_SEARCH_COMPLETE);
        }
    }

    public void setInterrupted() {
        this.ifCanSearch = false;
    }

}
