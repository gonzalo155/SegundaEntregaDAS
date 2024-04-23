package com.example.primeraentrega;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageStorageHelper {

    public static String saveImageToExternalStorage(Context context, Bitmap imageBitmap) {
        String imagePath = "";
        String imageFileName = "movie_image_" + System.currentTimeMillis() + ".jpg";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null) {
            File imageFile = new File(storageDir, imageFileName);
            try {
                OutputStream outputStream = new FileOutputStream(imageFile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                imagePath = imageFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imagePath;
    }

    public static Bitmap loadImageFromStorage(String imagePath) {
        Bitmap imageBitmap = null;
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageBitmap;
    }
}
