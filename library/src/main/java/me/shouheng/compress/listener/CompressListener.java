package me.shouheng.compress.listener;

import java.io.File;

/**
 * The compress state callback.
 *
 * @author WngShhng (shouheng2015@gmail.com)
 */
public interface CompressListener {

    /**
     * Will be called when start to compress.
     */
    void onStart();

    /**
     * Will be called when finish compress.
     *
     * @param result the compressed image
     */
    void onSuccess(File result);

    /**
     * Will be called when error occurred.
     *
     * @param throwable the throwable exception
     */
    void onError(Throwable throwable);

}
