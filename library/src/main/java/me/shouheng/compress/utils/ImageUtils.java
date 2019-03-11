package me.shouheng.compress.utils;

import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;

public class ImageUtils {

    private ImageUtils() {
    }

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

}
