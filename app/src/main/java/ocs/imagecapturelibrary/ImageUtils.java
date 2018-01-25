package ocs.imagecapturelibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ocs.imagecapturelibrary.Utils.PreferenceManager;
import ocs.imagecapturelibrary.Utils.Utils;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

/**
 * Created by subhasri on 1/25/2018.
 */

@SuppressLint("SdCardPath")
public class ImageUtils {
    Context context;
    private Activity currentActivity;
    private Fragment currentFragment;
    private ImageCaptureListener imageCaptureListener;
    private boolean isFragment = false;
    private final int CAMERA_PERMISSION_REQUEST = 100;
    private int CAMERA_PHOTO_REQUEST = 101;
    private int GALLERY_PHOTO_REQUEST = 102;
    private File mediaFile;
    private Uri mImageCaptureUri;
    String dialogTitle="", cameraText="", galleryText="";
    private String fileName, selectedFilePath;
    private int LOCAL_IMAGE_LOADING_DELAY_HIGH = 80;

    public ImageUtils(Activity act,String imagePickerDialogTitle, String cameraText, String galleryText) {
        this.context = act;
        this.currentActivity = act;
        imageCaptureListener = (ImageCaptureListener) context;
        this.dialogTitle = imagePickerDialogTitle;
        this.cameraText = cameraText;
        this.galleryText = galleryText;
    }

    public ImageUtils(Activity act, Fragment fragment, boolean isFragment,String imagePickerDialogTitle, String cameraText, String galleryText) {
        this.context = act;
        this.currentActivity = act;
        this.dialogTitle = imagePickerDialogTitle;
        this.cameraText = cameraText;
        this.galleryText = galleryText;
        Log.d("camera TExt",""+cameraText);
        imageCaptureListener = (ImageCaptureListener) fragment;
        if (isFragment) {
            this.isFragment = true;
            currentFragment = fragment;
        }
    }

    public void openImagePicker() {
        try {
            final CharSequence[] items;
            if (Utils.isDeviceSupportCamera(context)) {
                items = new CharSequence[2];
                items[0] = cameraText;
                items[1] = galleryText;
            } else {
                items = new CharSequence[1];
                items[0] = galleryText;
            }
            android.app.AlertDialog.Builder alertdialog = new android.app.AlertDialog.Builder(currentActivity);
            alertdialog.setTitle(dialogTitle);
            alertdialog.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals(cameraText)) {
                        openCamera();
                    } else if (items[item].equals(galleryText)) {
                        openGallery();
                    }
                }
            });
            alertdialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void checkCameraPermission() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int hasCameraPermission = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.CAMERA);
                int hasWritePermission = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int hasReadPermission = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.READ_EXTERNAL_STORAGE);

                if (hasCameraPermission != PackageManager.PERMISSION_GRANTED || hasReadPermission != PackageManager.PERMISSION_GRANTED || hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(currentActivity,
                            Manifest.permission.CAMERA)) {
                        showPermissionAlert();

                    } else {
                        ActivityCompat.requestPermissions(currentActivity,
                                new String[]{Manifest.permission.CAMERA,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                CAMERA_PERMISSION_REQUEST);
                    }
                } else
                    openImagePicker();
            }else
                openImagePicker();
        }catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void showPermissionAlert() {
        showMessageOKCancel("Kindly accept permission for adding images",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.cameraPermission(currentActivity,true);
                        ActivityCompat.requestPermissions(currentActivity,
                                new String[]{Manifest.permission.CAMERA,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                CAMERA_PERMISSION_REQUEST);
                    }
                });
    }


    /**
     * Check permission
     *
     *//*

    public void checkPermissionInFragment(final int permissionCode, String imagePickerDialogTitle, String cameraText, String galleryText) {
        Log.d(TAG, "checkPermissionInFragment: " + permissionCode);
        int hasCameraPermission = ContextCompat.checkSelfPermission(currentActivity,Manifest.permission.CAMERA);
        int hasWritePermission = ContextCompat.checkSelfPermission(currentActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasReadPermission = ContextCompat.checkSelfPermission(currentActivity,Manifest.permission.READ_EXTERNAL_STORAGE);

        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED || hasReadPermission != PackageManager.PERMISSION_GRANTED || hasWritePermission != PackageManager.PERMISSION_GRANTED)
        {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(currentActivity,
                    Manifest.permission.CAMERA)) {
                showMessageOKCancel("Kindly accept permission for adding images",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                currentFragment.requestPermissions(
                                        new String[]{Manifest.permission.CAMERA,
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        permissionCode);
                            }
                        });

            }else{
                currentFragment.requestPermissions(
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        permissionCode);
            }
        }else {
            openImagePicker(imagePickerDialogTitle,cameraText,galleryText);
        }
    }
    }*/
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(currentActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    /**
     * Capture image from camera
     */

    public void openCamera() {
        try {
            Intent cameraIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaFile = Utils.getOutputMediaFile(currentActivity);
                mImageCaptureUri = FileProvider.getUriForFile(currentActivity,
                        BuildConfig.APPLICATION_ID + ".provider",
                        mediaFile);
                fileName = mediaFile.getAbsolutePath();
                cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            } else {
                mediaFile = Utils.getOutputMediaFile(currentActivity);
                fileName = mediaFile.getAbsolutePath();
                mImageCaptureUri = Uri.fromFile(mediaFile);
                cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            }
            if (isFragment)
                currentFragment.startActivityForResult(cameraIntent, CAMERA_PHOTO_REQUEST);
            else
                currentActivity.startActivityForResult(cameraIntent, CAMERA_PHOTO_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * pick image from Gallery
     */

    public void openGallery() {
        Log.d(TAG, "openGallery: ");
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (isFragment)
            currentFragment.startActivityForResult(photoPickerIntent, GALLERY_PHOTO_REQUEST);
        else
            currentActivity.startActivityForResult(photoPickerIntent, GALLERY_PHOTO_REQUEST);

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            switch (requestCode) {
                case CAMERA_PERMISSION_REQUEST: {
                    if (grantResults.length > 0
                            && grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {
                        openImagePicker();
                    } else if (grantResults.length > 0
                            && grantResults[0] !=
                            PackageManager.PERMISSION_GRANTED) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, Manifest.permission.CAMERA) && PreferenceManager.isCameraPermission(currentActivity)) {

                            // user selected Never Ask Again. do something
                           showAlert("",currentActivity.getString(R.string.alert_set_camera_permissions_settings));
                        } else {
                            // all other conditions like first time asked, previously denied etc are captured here and can be extended if required.
                            showPermissionAlert();
                        }
                    }
                    return;

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PHOTO_REQUEST && resultCode == RESULT_OK) {
            try {
                if (mImageCaptureUri != null)
                    Utils.scanFile(mImageCaptureUri.getPath(),currentActivity);
                if (mImageCaptureUri == null) {
                    try {
                        mImageCaptureUri = Utils.getPathInSamsung(currentActivity);
                        fileName = Utils.getPath(mImageCaptureUri, currentActivity);
                        if (fileName != null)
                            mediaFile = new File(fileName);
                        Utils.scanFile(mImageCaptureUri.getPath(), currentActivity);
                        if (mediaFile == null) {
                            if (data != null) {
                                mImageCaptureUri = data.getData();
                                fileName = Utils.getPath(mImageCaptureUri, currentActivity);
                                if (fileName != null)
                                    mediaFile = new File(fileName);
                            } else {
                                mImageCaptureUri = Utils.getPathInSamsung(currentActivity);
                                fileName = Utils.getPath(mImageCaptureUri, currentActivity);
                                if (fileName != null)
                                    mediaFile = new File(fileName);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mImageCaptureUri = Uri.parse(fileName);
                    fileName = mImageCaptureUri.getPath();
                    mediaFile = new File(mImageCaptureUri.getPath());
                }

                try {
                    selectedFilePath = mImageCaptureUri.toString();
                    loadCameraImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == GALLERY_PHOTO_REQUEST && resultCode != 0) {
            try {
                mImageCaptureUri = data.getData();
                selectedFilePath = Utils.getPath(mImageCaptureUri,currentActivity);
                if (selectedFilePath == null)
                    selectedFilePath = mImageCaptureUri.getPath();
                mediaFile = new File(selectedFilePath);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaFile.exists()) {
                            Bitmap bmp = BitmapFactory.decodeFile(selectedFilePath);
                            if(Utils.isImageLarger(bmp))
                                loadGalleryImage(mImageCaptureUri);
                            else
                                imageCaptureListener.captureImage(fileName,selectedFilePath,mediaFile,mImageCaptureUri);
                        } else
                            loadGalleryImage(mImageCaptureUri);
                    }
                },20);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected void loadCameraImage() {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Matrix rotateMatrix = Utils.imageMatrix(fileName);
                    Bitmap photo = Utils.compressImage(selectedFilePath,rotateMatrix);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 95, bos);
                    Utils.checkAndDeleteIfExists(mediaFile,currentActivity);
                    mediaFile = Utils.saveToInternalStorage(currentActivity, photo);
                    Utils.scanGallery(mediaFile, currentActivity);
                    selectedFilePath = mediaFile.getPath();
                    imageCaptureListener.captureImage(fileName,selectedFilePath,mediaFile,mImageCaptureUri);
                }
            }, LOCAL_IMAGE_LOADING_DELAY_HIGH);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadGalleryImage( final Uri selectedImageUri) {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mediaFile = Utils.saveToInternalStorage(currentActivity,(MediaStore.Images.Media.getBitmap(currentActivity.getContentResolver(), selectedImageUri)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Utils.scanGallery(mediaFile,currentActivity);
                    selectedFilePath = mediaFile.getPath();
                    imageCaptureListener.captureImage(fileName,selectedFilePath,mediaFile,mImageCaptureUri);
                }
            }, LOCAL_IMAGE_LOADING_DELAY_HIGH);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showAlert(String title, String message) {
        new AlertDialog.Builder(currentActivity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                            currentActivity.finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }

}

