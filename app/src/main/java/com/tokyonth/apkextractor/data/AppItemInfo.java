package com.tokyonth.apkextractor.data;

import android.graphics.drawable.Drawable;

import com.tokyonth.apkextractor.utils.PinYin;

public class AppItemInfo implements Comparable<AppItemInfo> {
    /**
     * ����ģʽ��
     * 0 - Ĭ��
     * 1 - ��������
     * 2 - ���ƽ���
     * 3 - ��С����
     * 4 - ��С����
     * 5 - ��������
     * 6 - ���ڽ���
     */
    public static int SortConfig = 0;

    public String appName = "";             // ������
    public String packageName = "";         // �������
    public Drawable icon;                 // ����ͼ��
    public long appsize = 0;                // Ӧ�ô�С
    public String path = "";                  // apk��Դλ��
    public String version = "";             // Ӧ�ð汾
    public int versioncode = 0;             // Ӧ�ð汾intֵ
    public long lastupdatetime = 0;          // Ӧ�ø��°�װʱ��
    public int minsdkversion = 0;              // Ӧ��Ҫ������api�汾
    //��������CopyFilesTaskʱ��
    public boolean exportData = false;
    public boolean exportObb = false;

    //if is system app
    public boolean isSystemApp = false;

    public AppItemInfo() {

    }

    public AppItemInfo(AppItemInfo item) {
        this.appName = new String(item.appName);
        this.packageName = new String(item.packageName);
        this.icon = item.icon;
        this.appsize = item.appsize;
        this.path = new String(item.path);
        this.version = new String(item.version);
        this.versioncode = item.versioncode;
        this.lastupdatetime = item.lastupdatetime;
        this.minsdkversion = item.minsdkversion;
        this.exportData = false;
        this.exportObb = false;
    }

    // Set resources
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setAppName(String name) {
        this.appName = name;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setResourcePath(String apkpath) {
        this.path = apkpath;
    }

    public void setPackageSize(long size) {
        this.appsize = size;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setVersionCode(int value) {
        this.versioncode = value;
    }

    public void setLastUpdateTime(long millis) {
        this.lastupdatetime = millis;
    }

    public void setMinSDKVersion(int value) {
        this.minsdkversion = value;
    }

    //Get resources

    public Drawable getIcon() {
        return this.icon;
    }

    public String getAppName() {
        return this.appName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public long getPackageSize() {
        return this.appsize;
    }

    public String getResourcePath() {
        return this.path;
    }

    public String getVersion() {
        return this.version;
    }

    public int getVersionCode() {
        return this.versioncode;
    }

    public long getLastUpdateTime() {
        return this.lastupdatetime;
    }

    public int getMinSDKVersion() {
        return this.minsdkversion;
    }

    @Override
    public int compareTo(AppItemInfo o) {
        int returnvalue = 0;
        switch (SortConfig) {
            default:
                break;
            case 0:
                break;
            case 1:
                returnvalue = PinYin.getFirstSpell(this.appName).compareTo(PinYin.getFirstSpell(o.appName));
                break;
            case 2:
                returnvalue = 0 - PinYin.getFirstSpell(this.appName).compareTo(PinYin.getFirstSpell(o.appName));
                break;
            case 3:
                returnvalue = Long.valueOf(this.appsize).compareTo(o.appsize);
                break;
            case 4:
                returnvalue = 0 - Long.valueOf(this.appsize).compareTo(o.appsize);
                break;
            case 5:
                returnvalue = Long.valueOf(this.lastupdatetime).compareTo(o.lastupdatetime);
                break;
            case 6:
                returnvalue = 0 - Long.valueOf(this.lastupdatetime).compareTo(o.lastupdatetime);
                break;
        }
        return returnvalue;
    }

}

