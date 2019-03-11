package me.shouheng.compress.naming;

/**
 * The factory class used to get the name of compressed image file.
 */
public interface CacheNameFactory {

    /**
     * Get the file name of compressed image.
     *
     * @return the file name.
     */
    String getFileName();

}
