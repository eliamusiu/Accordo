package com.example.accordo;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    /**
     * Trasforma l'immagine da {@link Base64} a {@link Bitmap}
     * @param base64 Immagine  da trasformare
     * @return Immagine {@link Bitmap}
     */
    public static Bitmap getBitmapFromBase64(String base64) {       // TODO: gestire errore "java.lang.IllegalArgumentException: bad base-64"
        byte[] decodedString = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    /**
     * Trasforma una lista di immagini da {@link Base64} a {@link Bitmap}
     * @param base64List Lista di immagini da trasformare
     * @return lista di immagini {@link Bitmap}
     */
    public static ArrayList<Bitmap> getBitmapFromBase64(ArrayList<String> base64List) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for (String base64 : base64List) {
            byte[] decodedString = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            bitmaps.add(decodedByte);
        }
        return bitmaps;
    }

    /**
     * Trasforma l'immagine da {@link Bitmap} a {@link Base64}
     * @param bitmap Immagine da trasformare
     * @return Immagine trasformata
     */
    public static String getBase64FromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }


    /**
     * Ritorna la posizione di un'immagine nella lista di tutti i post
     * @param postPosition Posizione del post (tra tutti i tipi di post)
     * @param posts Lista dei post da scorrere
     * @return posizione dell'immagine (tra i post di tipo immagine)
     */
    public static int getImagePositionInPosts(int postPosition, ArrayList<Post> posts) {
        ArrayList<Post> cPosts = new ArrayList<>(posts);
        Collections.reverse(cPosts); // TODO: togliere questa copia quando metteremo che il model ritorna direttamente una copia
        int imagePosition = 0;
        postPosition = (cPosts.size() - postPosition) - 1;
        for (Post post : cPosts) {
            if (post.getType().equals("i")) {
                imagePosition++;
            }
            if (postPosition == cPosts.indexOf(post)) {
                return imagePosition - 1;
            }
        }
        return -1;
    }

    /**
     * Dato l'{@link Uri} ritorna il {@link Bitmap}
     * @param uri
     * @param contentResolver
     * @return Immagine
     */
    public static Bitmap getBitmapFromUri(Uri uri, ContentResolver contentResolver) {
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(inputStream);
    }

    /**
     * Trasforma un {@link Bitmap} in un {@link Bitmap} quadrato ritagliandolo
     * @param bitmap Immagine da ritagliare
     * @return Immagine ritagliata
     */
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
