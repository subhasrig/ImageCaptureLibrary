package ocs.imagecapturelibrary;

import android.net.Uri;

import java.io.File;

/**
 * Created by subhasri on 1/25/2018.
 */

public interface ImageCaptureListener {
    public void captureImage(String fileName, String filePath, File imageFile, Uri uri);
}
