package ocs.imagecapturelibrary.sampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import ocs.imagecapturelibrary.ImageCaptureListener;
import ocs.imagecapturelibrary.ImageUtils;
import ocs.imagecapturelibrary.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class ImagePickerActivityFragment extends Fragment implements ImageCaptureListener {
    ImageView ivCameraImage,ivUserImage;
    ImageUtils imageUtils;
    String selectedFilePath;
    File imageFile;
    Uri imageUri;
    ImagePickerListener imagePickerListener;
    interface ImagePickerListener{
        public void setCameraPermission(ImageUtils imageUtils);
    }
    public ImagePickerActivityFragment() {
    }

    @Override
    public void onAttach(Context context) {
        imagePickerListener = (ImagePickerListener)context;
        super.onAttach(context);
    }

    @Override
    public void onAttach(Activity activity) {
        imagePickerListener = (ImagePickerListener)activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        imagePickerListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_picker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ivCameraImage = view.findViewById(R.id.ivCameraImage);
        ivUserImage = view.findViewById(R.id.ivUserImage);
        imageUtils = new ImageUtils(getActivity(),this,true,"","Camera","Gallery");
        ivCameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickerListener.setCameraPermission(imageUtils);
            }
        });
        ivUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickerListener.setCameraPermission(imageUtils);
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageUtils.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void captureImage(String fileName, String filePath, File imageFile, Uri uri) {
        this.selectedFilePath = filePath;
        this.imageFile = imageFile;
        this.imageUri = uri;
        showImage();
    }
    public void showImage() {
        Picasso.with(getActivity()) //
                .load("file://" + selectedFilePath).
                resize(ivUserImage.getWidth(), ivUserImage.getHeight()).centerCrop()
                .into(ivUserImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        ivCameraImage.setVisibility(View.GONE);
                        ivUserImage.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                    }
                });
    }
}
