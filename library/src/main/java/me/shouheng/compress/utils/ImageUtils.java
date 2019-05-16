package me.shouheng.compress.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;

public final class ImageUtils {

    private ImageUtils() {
        throw new UnsupportedOperationException("u can't initialize me");
    }

    /**
     * Get angle from image attribute.
     *
     * @param file the image file
     * @return the angle of image
     */
    public static int getImageAngle(File file) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            switch (orientation) {
                case 6:
                    return 90;
                case 3:
                    return 180;
                case 8:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Need the image compress according to the file size and the least compress size.
     *
     * @param filePath file path
     * @param leastCompressSize least compress size
     * @return true if need to compress
     */
    public static boolean needCompress(String filePath, int leastCompressSize) {
        if (leastCompressSize > 0) {
            File source = new File(filePath);
            return source.exists() && source.length() > (leastCompressSize << 10);
        }
        return true;
    }

    /**
     * Rotate given bitmap and return the result.
     *
     * @param srcBitmap the source bitmap
     * @param angle the angle to rotate
     * @return the rotated bitmap
     */
    public static Bitmap rotateBitmap(Bitmap srcBitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
    }

}
