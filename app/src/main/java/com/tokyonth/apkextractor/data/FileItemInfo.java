package com.tokyonth.apkextractor.data;

import java.io.File;
import java.util.Locale;

public class FileItemInfo implements Comparable<FileItemInfo> {

    /**
     * ������ƣ�0=Ĭ��,1=��������A��Z��,2=���ƽ���Z��A��
     */
    public static int SortConfig = 1;

    public File file;

    public FileItemInfo(File file) {
        this.file = file;
    }

    @Override
    public int compareTo(FileItemInfo info) {
        int returnvalue = 0;
        switch (SortConfig) {
            default:
                break;
            case 1:
                returnvalue = this.file.getName().trim().toLowerCase(Locale.ENGLISH)
                        .compareTo(info.file.getName().trim().toLowerCase(Locale.ENGLISH));
                break;
            case 2:
                returnvalue = 0 - this.file.getName().trim().toLowerCase(Locale.ENGLISH)
                        .compareTo(info.file.getName().trim().toLowerCase(Locale.ENGLISH));
                break;
        }
        return returnvalue;
    }

}
