package ocs.imagecapturelibrary.Utils;

/**
 * Created by subhasri on 8/14/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    public static void cameraPermission(Context context, boolean isFirstTime) {
        SharedPreferences sharedPreference = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        sharedPreference.edit().putBoolean("CAMERA_PERMISSION", isFirstTime).apply();
    }

    public static boolean isCameraPermission(Context context) {
        return context.getSharedPreferences("CAMERA_PERMISSION", Context.MODE_PRIVATE).getBoolean("LOCATION_PERMISSION", true);
    }



}
