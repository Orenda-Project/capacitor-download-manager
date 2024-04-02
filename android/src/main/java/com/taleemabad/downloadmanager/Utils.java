package com.taleemabad.downloadmanager;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

public class Utils {
    public static String getFilePath(String url, Context context) {
        String fileName = getNameFromUrl(url);
        String dir = getSaveDir(context);
        return dir + "/" + fileName;
    }

    public static String getNameFromUrl(String url) {
        return Uri.parse(url).getLastPathSegment();
    }

    public static String getSaveDir(Context context) {
        try {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        } catch (Exception e) {
            return String.valueOf(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
        }
    }
}
