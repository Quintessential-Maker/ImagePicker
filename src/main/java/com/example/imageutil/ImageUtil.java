package com.example.imageutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ImageUtil {

    public static final int MAX_FILE_SIZE_IN_KB=60;

    Activity activity;

    public String mCurrentPhotoPath;
    public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    public static final String DIRECTORY_NAME="/ShikshaMitra_data/";
    public static final String DECODED_DIRECTORY_NAME="/ShikshaMitra_data_cmp/";

    public static final int IMAGE_WIDTH=612;
    public static final int IMAGE_HEIGHT=816;

    public ImageUtil(Activity activity){
        this.activity=activity;
    }

    public ImageUtil(Context context){
        this.activity=(Activity)context;
    }

    public void openCamera(Fragment fragment, int requestCode){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if(takePictureIntent.resolveActivity(activity.getPackageManager())!=null){
            File photo=createImageFile();
            if(photo!=null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(activity,
                                activity.getApplicationContext().getPackageName() + ".provider",
                                photo));
                fragment.startActivityForResult(takePictureIntent, requestCode);
            }
        }else {
            Toast.makeText(activity, "Image file can't be created", Toast.LENGTH_LONG).show();
        }
    }

    public void openCamera(Activity activity,int requestCode){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if(takePictureIntent.resolveActivity(activity.getPackageManager())!=null){
            File photo=createImageFile();
            if(photo!=null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(activity,
                                activity.getApplicationContext().getPackageName() + ".provider",
                                photo));
                activity.startActivityForResult(takePictureIntent, requestCode);
            }
        }else {
            Toast.makeText(activity, "Image file can't be created", Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile(){
        try {
            String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
            String imageFileName = "image" + timeStamp + "_";

            //File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + AppConstants.DIRECTORY_NAME);
            File storageDir = new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + DIRECTORY_NAME);

            if (!storageDir.exists())
                storageDir.mkdirs();

            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        }catch (IOException io){
            return null;
        }
    }

    /*----*/
    /*Set photo into image view*/
    public void setPic(ImageView ivCapturedPhoto) {

        try {
            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);


        Glide.with(activity)
                .load(mCurrentPhotoPath)
                .into(ivCapturedPhoto);

    }

    public static boolean isImageSizeValid(File imgFile){
        long fileSizeInBytes = imgFile.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        double fileSizeInKB = fileSizeInBytes / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        double fileSizeInMB = fileSizeInKB / 1024;


        if(fileSizeInKB>MAX_FILE_SIZE_IN_KB){
            //Crashlytics.log("Image Size limit exceeded-"+fileSizeInMB+" MB");
            return false;
        }
        return true;
    }

    public static String decodeFile(Context context,String path, int DESIREDWIDTH, int DESIREDHEIGHT) {

        if(isImageSizeValid(new File(path)))
            return path;
        String imgPath=decode(context, path,DESIREDWIDTH-=50,DESIREDHEIGHT-=50);
        /*File oldfile=new File(path);
        if (oldfile.exists()) {
            if (oldfile.delete()) {
                System.out.println("file Deleted :" + path);
            }
        }*/

        File file=new File(imgPath);
        if(isImageSizeValid(file))
            return imgPath;
        else {
            return decodeFile(context,imgPath, DESIREDWIDTH-=50, DESIREDHEIGHT-=50);
        }
    }

    /*Reduce image by dimension*/
    public static String decode(Context context, String path, int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            //scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            } else {
                unscaledBitmap.recycle();
                return path;
            }

            scaledBitmap = getRotatedPic(scaledBitmap,path);

            // Store to tmp file
            String extr = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
            File storageDir = new File(extr + DECODED_DIRECTORY_NAME);
            if (!storageDir.exists()) {
                storageDir.mkdir();
            }

            String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
            String imageFileName = "image" + timeStamp + "_";

            File f = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            scaledBitmap.recycle();
        } catch (Throwable e) {
        }
        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void createGeoTagImage(String path, double lat, double lon) {
        try {
            File file=new File(path);

            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            /*if(lat==null){
                lat = "0.00";
                lon = "0.00";
            }*/
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, LocationUtil.convert(lat));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, LocationUtil.latitudeRef(lat));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, LocationUtil.convert(lon));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, LocationUtil.longitudeRef(lon));
            exif.saveAttributes();
        } catch (Exception e) {
            Log.e("BaseAct", e.getMessage());
        }
    }

    public static void createGeoTagImage2(String path, double lat, double lon) {
        try {
            File file=new File(path);

            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            /*if(lat==null){
                lat = "0.00";
                lon = "0.00";
            }*/
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, LocationUtil.convert(lat));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, LocationUtil.latitudeRef(lat));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, LocationUtil.convert(lon));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, LocationUtil.longitudeRef(lon));
            exif.saveAttributes();
        } catch (Exception e) {
            Log.e("BaseAct", e.getMessage());
        }
    }

    public static String convertBitMapToFile(Context context, Bitmap bitmap){
        String strMyImagePath=null;
        String extr = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
        File storageDir = new File(extr + DECODED_DIRECTORY_NAME);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
        String imageFileName = "image" + timeStamp + "_";

        try {
            File f = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;

            fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strMyImagePath;
    }

    public static Bitmap getRotatedPic(Bitmap bitmapParam,String navCurrentImagePath) {
        Bitmap bitmap=bitmapParam;
        try {
            ExifInterface ei = new ExifInterface(navCurrentImagePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
