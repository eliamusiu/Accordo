package com.example.accordo;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

public class Utils {

    public static Bitmap getBitmapFromBase64(String base64) {       // TODO: gestire errore "java.lang.IllegalArgumentException: bad base-64"
        byte[] decodedString = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    public static String getBase64FromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }

    public static int getBitmapPositionInList(List<Bitmap> images, Bitmap bitmap) {
        for (Bitmap image : images) {
            if (image.sameAs(bitmap)) {
                return images.indexOf(image);
            }
        }
        return -1;
    }

    public static Bitmap getBitmapFromUri(Uri uri, ContentResolver contentResolver) {
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(inputStream);
    }

    public static Bitmap cropImageToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width) ? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0) ? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0) ? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);

        return cropImg;
    }
}
