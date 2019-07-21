package com.tokyonth.apkextractor.listener;

import android.content.DialogInterface;
import android.widget.Toast;

import com.tokyonth.apkextractor.activity.Main;

public class ListenerStopButton implements DialogInterface.OnClickListener {

    private Main main;

    public ListenerStopButton(Main main) {
        this.main = main;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (main.thread_extract_app != null) {
            if (main.runnable_extract_app != null) {
                main.runnable_extract_app.setInterrupted();
            }
            main.thread_extract_app.interrupt();
            main.thread_extract_app = null;
        }
        main.dialog_copy_file.cancel();
        Toast.makeText(main, "已停止!", Toast.LENGTH_SHORT).show();
    }

}
