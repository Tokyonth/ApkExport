package com.tokyonth.apkextractor.ui;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tokyonth.apkextractor.R;

public class AppDetailDialog extends BottomSheetDialog {

    private TextView textview_att;
    public RelativeLayout area_extract, area_share, area_detail;
    private Context context;
    private String appinfo = "";
    private TextView app_name;
    private ImageView app_icon;

    public AppDetailDialog(@NonNull Context context, int theme) {
        super(context, theme);
        this.context = context;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogview = layoutInflater.inflate(R.layout.layout_dialog_appdetail, null);
        setContentView(dialogview);
        this.textview_att = (TextView) dialogview.findViewById(R.id.dialog_appdetail_text);
        this.area_extract = (RelativeLayout) dialogview.findViewById(R.id.dialog_appdetail_area_extract);
        this.area_share = (RelativeLayout) dialogview.findViewById(R.id.dialog_appdetail_area_share);
        this.area_detail = (RelativeLayout) dialogview.findViewById(R.id.dialog_appdetail_area_detail);

        app_name = (TextView) dialogview.findViewById(R.id.app_detail_name);
        app_icon = (ImageView) dialogview.findViewById(R.id.app_detail_icon);

        this.setCancelable(true);
        this.setCanceledOnTouchOutside(true);
    }


    public void setTitle(String string) {
        app_name.setText(string);
    }

    public void setIcon(Drawable drawable) {
        app_icon.setImageDrawable(drawable);
    }

    public void setAppInfo(String varsion, int versioncode, long lastupdatetime, long appsize) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lastupdatetime);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        String appinfo = this.context.getResources().getString(R.string.dialog_appdetail_text_versioncode) + versioncode + "\n"
                + this.context.getResources().getString(R.string.varsion) + varsion + "\n"
                + this.context.getResources().getString(R.string.dialog_appdetail_text_appsize) + Formatter.formatFileSize(this.context, appsize) + "\n"
                + this.context.getResources().getString(R.string.dialog_appdetail_text_lastupdatetime) + format(year) + "/" + format(month) + "/" + format(day) + "/" + format(hour) + ":" + format(min) + ":" + format(sec);

        this.appinfo = appinfo;
        this.textview_att.setText(this.appinfo);
    }

    /**
     * Call this method after called setAPPInfo(int,int,long)
     * and require API Level 24!!!!
     */
    public void setAPPMinSDKVersion(int version) {
        if (Build.VERSION.SDK_INT >= 24) {
            String title = this.context.getResources().getString(R.string.dialog_appdetail_text_minsdkversion);
            this.appinfo += "\n" + title + version + " ";
            switch (version) {
                default:
                    break;
                case 1:
                    this.appinfo += " (Android 1.0 Base)";
                    break;
                case 2:
                    this.appinfo += " (Android 1.1 BASE_1_1)";
                    break;
                case 3:
                    this.appinfo += " (Android 1.5 CUPCAKE)";
                    break;
                case 4:
                    this.appinfo += " (Android 1.6 DONUT)";
                    break;
                case 5:
                    this.appinfo += " (Android 2.0	 ECLAIR)";
                    break;
                case 6:
                    this.appinfo += " (Android 2.0.1 ECLAIR_0_1)";
                    break;
                case 7:
                    this.appinfo += " (Android 2.1 ECLAIR_MR1)";
                    break;
                case 8:
                    this.appinfo += " (Android 2.2 FROYO)";
                    break;
                case 9:
                    this.appinfo += " (Android 2.3 GINGERBREAD)";
                    break;
                case 10:
                    this.appinfo += " (Android 2.3.3 GINGERBREAD_MR1)";
                    break;
                case 11:
                    this.appinfo += " (Android 3.0 HONEYCOMB)";
                    break;
                case 12:
                    this.appinfo += " (Android 3.1 HONEYCOMB_MR1)";
                    break;
                case 13:
                    this.appinfo += " (Android 3.2 HONEYCOMB_MR2)";
                    break;
                case 14:
                    this.appinfo += " (Android 4.0 ICE_CREAM_SANDWICH)";
                    break;
                case 15:
                    this.appinfo += " (Android 4.0 ICE_CREAM_SANDWICH_MR1)";
                    break;
                case 16:
                    this.appinfo += " (Android 4.1 JELLY_BEAN)";
                    break;
                case 17:
                    this.appinfo += " (Android 4.2 JELLY_BEAN_MR1)";
                    break;
                case 18:
                    this.appinfo += " (Android 4.3 JELLY_BEAN_MR2)";
                    break;
                case 19:
                    this.appinfo += " (Android 4.4 KITKAT)";
                    break;
                case 20:
                    this.appinfo += " (Android 4.4W KITKAT_WATCH)";
                    break;
                case 21:
                    this.appinfo += " (Android 5.0 LOLLIPOP)";
                    break;
                case 22:
                    this.appinfo += " (Android 5.1 LOLLIPOP_MR1)";
                    break;
                case 23:
                    this.appinfo += " (Android 6.0 Marshmallow)";
                    break;
                case 24:
                    this.appinfo += " (Android 7.0 Nougat)";
                    break;
                case 25:
                    this.appinfo += " (Android 7.1 Nougat_MR1)";
                    break;
                case 26:
                    this.appinfo += " (Android 8.0 Oreo)";
                    break;
                case 27:
                    this.appinfo += " (Android 8.1 Oreo_MR1)";
                    break;
                //the newest api now is 27 before I wrote this program

            }

            this.textview_att.setText(this.appinfo);
        }
    }

    private String format(int x) {
        String s = "" + x;
        if (s.length() == 1)
            s = "0" + s;
        return s;
    }

}
