package ocs.imagecapturelibrary.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.provider.MediaStore;
import android.support.v4.graphics.BitmapCompat;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ocs.imagecapturelibrary.R;

/**
 * Created by subhasri on 1/25/2018.
 */

public class Utils {

    public static Matrix imageMatrix(String fileName) {
        Matrix rotateMatrix = new Matrix();
        ExifInterface exifReader = null;
        try {
            exifReader = new ExifInterface(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotateMatrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                rotateMatrix.setRotate(180);
                rotateMatrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                rotateMatrix.setRotate(90);
                rotateMatrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotateMatrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                rotateMatrix.setRotate(-90);
                rotateMatrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotateMatrix.setRotate(-90);
                rotateMatrix.postScale(-1, 1);
                break;
            default:
                break;
        }
        return rotateMatrix;
    }

    public static File getOutputMediaFile(Activity currentActivity ) {
        File mediaStorageDir = new File(android.os.Environment.getExternalStorageDirectory(), currentActivity.getString(R.string.app_name));
        Log.d("mediaStorageDir @@@@", "" + mediaStorageDir);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String fileName = getFileName();
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return mediaFile;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getFileName() {
        String timeStamp = "";
        try {
            timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeStamp + ".jpg";
    }
    public String getFileNameFromFilePath(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());

    }
    public static void scanFile(String path,Activity currentActivity) {
        try {
            MediaScannerConnection.scanFile(currentActivity,
                    new String[]{path}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Uri getPathInSamsung(Activity currentActivity) {
        Uri picUri = null;
        // Describe the columns you'd like to have returned. Selecting from the Thumbnails location gives you both the Thumbnail Image ID, as well as the original image ID
        String[] projection = {
                MediaStore.Images.Thumbnails._ID,  // The columns we want
                MediaStore.Images.Thumbnails.IMAGE_ID,
                MediaStore.Images.Thumbnails.KIND,
                MediaStore.Images.Thumbnails.DATA};
        String selection = MediaStore.Images.Thumbnails.KIND + "=" + // Select only mini's
                MediaStore.Images.Thumbnails.MINI_KIND;
        String sort = MediaStore.Images.Thumbnails._ID + " DESC";
        //At the moment, this is a bit of a hack, as I'm returning ALL images, and just taking the latest one. There is a better way to narrow this down I think with a WHERE clause which is currently the selection variable
        Cursor myCursor = currentActivity.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, selection, null, sort);
        long imageId = 0l;
        try {
            myCursor.moveToFirst();
            imageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
        } finally {
            myCursor.close();
        }
        //Create new Cursor to obtain the file Path for the large image
        String[] largeFileProjection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.ORIENTATION,
                MediaStore.Images.ImageColumns.DATE_TAKEN};
        String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
        myCursor = currentActivity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
        try {
            myCursor.moveToFirst();
        } finally {
            myCursor.close();
        }
        // These are the two URI's you'll be interested in. They give you a handle to the actual images
        Uri uriLargeImage = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
        picUri = uriLargeImage;
        return picUri;
    }

    public static String getPath(Uri uri,Activity currentActivity) {
        Log.d("", "===getPath ==" + uri);
        Cursor cursor = null;
        int column_index = 0;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = currentActivity.getContentResolver().query(uri, projection, null, null, null);
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor.getString(column_index).toString();
    }

    /**
     * Check Camera Availability
     *
     * @return
     */

    public static boolean isDeviceSupportCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    public static File saveToInternalStorage(Activity activity,Bitmap bitmapImage) {
        File imageFile = null;
        try {
            imageFile = getOutputMediaFile(activity);
            FileOutputStream outputStream;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (BitmapCompat.getAllocationByteCount(bitmapImage) > 2000000) {
                do {
                    bitmapImage = Bitmap.createScaledBitmap(bitmapImage, (int) (bitmapImage.getWidth() * 0.8), (int) (bitmapImage.getHeight() * 0.8), true);
                }
                while (BitmapCompat.getAllocationByteCount(bitmapImage) > 2000000);
            }
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            try {
                outputStream = new FileOutputStream(imageFile);
                //outputStream = activity.openFileOutput(dishImageFile.getName(), Context.MODE_PRIVATE);

                outputStream.write(stream.toByteArray());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return imageFile;

    }

    public static boolean isImageLarger(Bitmap bitmapImage) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        long bmpLength = imageInByte.length;
        return bmpLength > 2000000;
    }
    public static void checkAndDeleteIfExists(File mediaFile,Activity activity) {
        try{
            if (mediaFile != null) {
                if (mediaFile.exists()) {
                    mediaFile.getAbsoluteFile().delete();
                    Utils.scanGallery(mediaFile, activity);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void scanGallery(File dishImageFile, Activity activity){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(dishImageFile);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }
    public static Bitmap compressImage(String filePath,Matrix rotateMatrix) {
        Bitmap scaledBitmap = null;

        try {
            //String filePath = getRealPathFromURI(activity,imageUri);

            BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }

//      setting inSampleSize value allows to load a scaled down version of the original image

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
//          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(filePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), rotateMatrix,false);

        }catch (Exception e){
            e.printStackTrace();
        }
        return scaledBitmap;

    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }       final float totalPixels = width * height;       final float totalReqPixelsCap = reqWidth * reqHeight * 2;       while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
}
