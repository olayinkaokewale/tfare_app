package com.wwwebteam.clients.tfare.utils;

/**
 * Created by wwweb on 11/12/2016.
 */
import android.graphics.Bitmap;

public class BitmapResizer {


    public static Bitmap resizeImageWidth(Bitmap bitmap, int reqWidth) {
        float factor = reqWidth/ (float) bitmap.getWidth();
        int bitHeight = (int) (factor*bitmap.getHeight());

        try {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, bitHeight, true);
            if (resizedBitmap != bitmap) bitmap.recycle();
            return resizedBitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap resizeImageHeight(Bitmap bitmap, int reqHeight) {
        float factor = reqHeight/(float) bitmap.getHeight();
        int bitWidth = (int) (factor*bitmap.getWidth());

        try {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitWidth, reqHeight, true);
            if (resizedBitmap != bitmap) bitmap.recycle();
            return resizedBitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[] getTheCorrectWidth(int width, int height, int deviceWidth) {
        float factor = (float) deviceWidth / width;
        int setHeight = (int) (factor*height);
        int[] retSize = new int[] {deviceWidth, setHeight};
        return retSize;
    }
}

