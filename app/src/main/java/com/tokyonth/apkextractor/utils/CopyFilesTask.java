package com.tokyonth.apkextractor.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.os.Message;

import com.tokyonth.apkextractor.R;
import com.tokyonth.apkextractor.activity.BaseActivity;
import com.tokyonth.apkextractor.activity.Main;
import com.tokyonth.apkextractor.data.AppItemInfo;
import com.tokyonth.apkextractor.data.Constants;
import com.tokyonth.apkextractor.multiplexing.FileInfo;

/**
 * ����ָ��AppItemInfo��������ָ��path
 *
 * @author MXR  mxremail@qq.com  https://github.com/ghmxr/apkextractor
 */

public class CopyFilesTask implements Runnable {

    private Context context;
    public List<AppItemInfo> applist;
    private String savepath = BaseActivity.savepath, currentWritePath = null;
    private boolean isInterrupted = false;
    private long progress = 0, total = 0;
    private long progress_check = 0;
    private long zipTime = 0;
    private long zipWriteLength_second = 0;

    private List<String> writePaths = new ArrayList<String>();

    public CopyFilesTask(List<AppItemInfo> list, Context context) {
        applist = list;
        this.context = context;
        this.isInterrupted = false;
        File initialpath = new File(this.savepath);
        if (initialpath.exists() && !initialpath.isDirectory()) {
            initialpath.delete();
        }
        if (!initialpath.exists()) {
            initialpath.mkdirs();
        }
    }

    @Override
    public void run() {
        total = getTotalLenth();
        long bytetemp = 0;
        long bytesPerSecond = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < this.applist.size(); i++) {
            AppItemInfo item = applist.get(i);
            if (!this.isInterrupted) {
                Message msg_currentapp = new Message();
                msg_currentapp.what = Main.MESSAGE_COPYFILE_CURRENTAPP;
                msg_currentapp.obj = Integer.valueOf(i);
                Main.sendMessage(msg_currentapp);
                if ((!item.exportData) && (!item.exportObb)) {
                    int byteread = 0;
                    try {
                        //String writepath=this.savepath+"/"+item.getPackageName()+"-"+item.getVersionCode()+".apk";
                        String writepath = FileInfo.getAbsoluteWritePath(context, item, "apk");
                        this.currentWritePath = writepath;
                        InputStream in = new FileInputStream(item.getResourcePath()); //����ԭ�ļ�
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(writepath));

                        Message msg_currentfile = new Message();
                        msg_currentfile.what = BaseActivity.MESSAGE_COPYFILE_CURRENTFILE;
                        String sendpath = new String(writepath);
                        if (sendpath.length() > 90)
                            sendpath = "..." + sendpath.substring(sendpath.length() - 90, sendpath.length());
                        msg_currentfile.obj = context.getResources().getString(R.string.copytask_apk_current) + sendpath;
                        BaseActivity.sendMessage(msg_currentfile);

                        byte[] buffer = new byte[1024 * 10];
                        while ((byteread = in.read(buffer)) != -1 && !this.isInterrupted) {
                            out.write(buffer, 0, byteread);
                            progress += byteread;
                            bytesPerSecond += byteread;
                            long endTime = System.currentTimeMillis();
                            if ((endTime - startTime) > 1000) {
                                startTime = endTime;
                                Long speed = Long.valueOf(bytesPerSecond);
                                bytesPerSecond = 0;
                                Message msg_speed = new Message();
                                msg_speed.what = BaseActivity.MESSAGE_COPYFILE_REFRESH_SPEED;
                                msg_speed.obj = speed;
                                BaseActivity.sendMessage(msg_speed);
                            }

                            if ((progress - bytetemp) > 100 * 1024) {   //ÿд100K����һ�θ��½��ȵ�Message
                                bytetemp = progress;
                                Message msg_progress = new Message();
                                Long progressinfo[] = new Long[]{Long.valueOf(progress), Long.valueOf(total)};
                                msg_progress.what = BaseActivity.MESSAGE_COPYFILE_REFRESH_PROGRESS;
                                msg_progress.obj = progressinfo;
                                BaseActivity.sendMessage(msg_progress);
                            }

                        }
                        out.flush();
                        in.close();
                        out.close();
                        writePaths.add(writepath);
                    }
					/*catch(FileNotFoundException fe){
						fe.printStackTrace();						
					}					
					catch(IOException e){
						e.printStackTrace();				
						BaseActivity.sendEmptyMessage(BaseActivity.MESSAGE_COPYFILE_IOEXCEPTION);
					}*/ catch (Exception e) {
                        e.printStackTrace();
                        try {
                            File file = new File(this.currentWritePath);
                            if (file.exists() && !file.isDirectory()) {
                                file.delete();
                            }
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                        progress += item.getPackageSize();
                        Message msg_exception = new Message();
                        String filename = item.getAppName() + " " + item.getVersion();
                        msg_exception.what = BaseActivity.MESSAGE_COPYFILE_FILE_NOTFOUND_EXCEPTION;
                        msg_exception.obj = filename + "\nError Message:" + e.toString();
                        BaseActivity.sendMessage(msg_exception);
                    }

                } else {
                    try {
                        //String writePath=this.savepath+"/"+item.getPackageName()+"-"+item.getVersionCode()+".zip";
                        String writePath = FileInfo.getAbsoluteWritePath(context, item, "zip");
                        this.currentWritePath = writePath;
                        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(new File(writePath))));
                        zos.setComment("Packaged by TOKYONTH");
                        int zip_level = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(Constants.PREFERENCE_ZIP_COMPRESS_LEVEL, Constants.PREFERENCE_ZIP_COMPRESS_LEVEL_DEFAULT);

                        if (zip_level >= 0 && zip_level <= 9) zos.setLevel(zip_level);

                        writeZip(new File(item.getResourcePath()), "", zos, zip_level);
                        if (item.exportData) {
                            writeZip(new File(StorageUtil.getMainStoragePath() + "/android/data/" + item.packageName), "Android/data/", zos, zip_level);
                        }
                        if (item.exportObb) {
                            writeZip(new File(StorageUtil.getMainStoragePath() + "/android/obb/" + item.packageName), "Android/obb/", zos, zip_level);
                        }
                        zos.flush();
                        zos.close();
                        writePaths.add(writePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            File file = new File(this.currentWritePath);
                            if (file.exists() && !file.isDirectory()) {
                                file.delete();
                            }
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                        Message msg_filenotfound_exception = new Message();
                        String filename = item.getAppName() + " " + item.getVersion();
                        msg_filenotfound_exception.what = BaseActivity.MESSAGE_COPYFILE_FILE_NOTFOUND_EXCEPTION;
                        msg_filenotfound_exception.obj = filename + "\n" + "Error Message:" + e.toString();
                        BaseActivity.sendMessage(msg_filenotfound_exception);
                    }
                    if (isInterrupted) new File(this.currentWritePath).delete();
                }
            } else {
                break;
            }

        }

        if (!this.isInterrupted) {
            Message msg = new Message();
            msg.what = BaseActivity.MESSAGE_COPYFILE_COMPLETE;
            msg.obj = writePaths;
            BaseActivity.sendMessage(msg);
        }

    }

    private void writeZip(File file, String parent, ZipOutputStream zos, final int zip_level) {
        if (file == null || parent == null || zos == null) return;
        if (isInterrupted) return;
        if (file.exists()) {
            if (file.isDirectory()) {
                parent += file.getName() + File.separator;
                File files[] = file.listFiles();
                if (files.length > 0) {
                    for (File f : files) {
                        writeZip(f, parent, zos, zip_level);
                    }
                } else {
                    try {
                        zos.putNextEntry(new ZipEntry(parent));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    FileInputStream in = new FileInputStream(file);
                    ZipEntry zipentry = new ZipEntry(parent + file.getName());

                    if (zip_level == Constants.ZIP_LEVEL_STORED) {
                        zipentry.setMethod(ZipOutputStream.STORED);
                        zipentry.setCompressedSize(file.length());
                        zipentry.setSize(file.length());
                        zipentry.setCrc(getCRC32FromFile(file).getValue());
                    }

                    zos.putNextEntry(zipentry);
                    byte[] buffer = new byte[1024];
                    int length;
                    //long progressCheck=this.progress;

                    Message msg_currentfile = new Message();
                    msg_currentfile.what = BaseActivity.MESSAGE_COPYFILE_CURRENTFILE;
                    String currentPath = file.getAbsolutePath();
                    if (currentPath.length() > 90)
                        currentPath = "..." + currentPath.substring(currentPath.length() - 90, currentPath.length());
                    msg_currentfile.obj = context.getResources().getString(R.string.copytask_zip_current) + currentPath;
                    BaseActivity.sendMessage(msg_currentfile);

                    while ((length = in.read(buffer)) != -1 && !isInterrupted) {
                        zos.write(buffer, 0, length);
                        this.progress += length;
                        this.zipWriteLength_second += length;
                        Long endTime = System.currentTimeMillis();
                        if (endTime - this.zipTime > 1000) {
                            this.zipTime = endTime;
                            Message msg_speed = new Message();
                            msg_speed.what = BaseActivity.MESSAGE_COPYFILE_REFRESH_SPEED;
                            msg_speed.obj = this.zipWriteLength_second;
                            BaseActivity.sendMessage(msg_speed);
                            this.zipWriteLength_second = 0;
                        }
                        if (this.progress - progress_check > 100 * 1024) {
                            progress_check = this.progress;
                            Message msg = new Message();
                            msg.what = Main.MESSAGE_COPYFILE_REFRESH_PROGRESS;
                            msg.obj = new Long[]{this.progress, this.total};
                            BaseActivity.sendMessage(msg);
                        }

                    }
                    zos.flush();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private long getTotalLenth() {
        long total = 0;
        for (AppItemInfo item : applist) {
            total += item.appsize;
            if (item.exportData) {
                total += FileSize.getFileOrFolderSize(new File(StorageUtil.getMainStoragePath() + "/android/data/" + item.packageName));
            }
            if (item.exportObb) {
                total += FileSize.getFileOrFolderSize(new File(StorageUtil.getMainStoragePath() + "/android/obb/" + item.packageName));
            }
        }
        return total;
    }

    public void setInterrupted() {
        this.isInterrupted = true;
        try {
            File file = new File(this.currentWritePath);
            if (file.exists() && !file.isDirectory()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static CRC32 getCRC32FromFile(File file) throws Exception {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
        CRC32 crc = new CRC32();
        byte[] bytes = new byte[1024];
        int cnt;
        while ((cnt = inputStream.read(bytes)) != -1) {
            crc.update(bytes, 0, cnt);
        }
        inputStream.close();
        return crc;
    }
}
