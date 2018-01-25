package ocs.imagecapturelibrary.sampleapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.File;

import ocs.imagecapturelibrary.ImageCaptureListener;
import ocs.imagecapturelibrary.ImageUtils;
import ocs.imagecapturelibrary.R;

public class ImagePickerActivity extends AppCompatActivity implements ImagePickerActivityFragment.ImagePickerListener{
    ImageUtils imageUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        imageUtils.onRequestPermissionsResult(requestCode,permissions,grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void setCameraPermission(ImageUtils _imageUtils) {
        this.imageUtils = _imageUtils;
        imageUtils.checkCameraPermission();
    }
}
