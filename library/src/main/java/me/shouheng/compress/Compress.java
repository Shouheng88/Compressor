package me.shouheng.compress;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.IntRange;
import android.text.TextUtils;
import me.shouheng.compress.listener.CompressListener;
import me.shouheng.compress.naming.CacheNameFactory;
import me.shouheng.compress.naming.DefaultNameFactory;
import me.shouheng.compress.strategy.config.Config;
import me.shouheng.compress.utils.FileUtils;

import java.io.File;

/**
 * The Compress connector. Sample:
 * <code>
 *     Compress.with(context, file)
 *             .setFormat(Bitmap.CompressFormat.JPEG)
 *             .setQuality(80)
 *             .strategy(Strategies.luban())
 *             .setCompressListener(compressListener)
 *             .launch()
 * </code>
 */
public final class Compress {

    private Context context;
    private File srcFile;
    private Bitmap srcBitmap;
    private byte[] srcData;
    private Bitmap.CompressFormat format = Config.DEFAULT_COMPRESS_FORMAT;
    private int quality = Config.DEFAULT_COMPRESS_QUALITY;
    private String targetDir;

    private CacheNameFactory cacheNameFactory;
    private CompressListener compressListener;

    public static Compress with(Context context, File file) {
        return new Compress(context, file, null, null);
    }

    public static Compress with(Context context, Bitmap srcBitmap) {
        return new Compress(context, null, srcBitmap, null);
    }

    public static Compress with(Context context, byte[] srcData) {
        return new Compress(context, null, null, srcData);
    }

    private Compress(Context context, File file, Bitmap srcBitmap, byte[] srcData) {
        this.context = context;
        this.srcFile = file;
        this.srcBitmap = srcBitmap;
        this.srcData = srcData;
        this.cacheNameFactory = DefaultNameFactory.getFactory();
    }

    /**
     * Set the format of compressed image.
     *
     * @param format image format
     * @return the compress object
     */
    public Compress setFormat(Bitmap.CompressFormat format) {
        this.format = format;
        return this;
    }

    /**
     * Set the quality of compressed image, should be an integer between 0 and 100, aks [0, 100].
     *
     * @param quality the quality of compressed image
     * @return the compress object
     */
    public Compress setQuality(@IntRange(from = 0, to = 100) int quality) {
        this.quality = quality;
        return this;
    }

    /**
     * The directory compressed image will be saved to.
     *
     * @param targetDir the target directory
     * @return the compress object
     */
    public Compress setTargetDir(String targetDir) {
        this.targetDir = targetDir;
        return this;
    }

    /**
     * The factory witch will used to provide the name of compressed file.
     *
     * @param cacheNameFactory the cache name factory
     * @return the compress object
     */
    public Compress setCacheNameFactory(CacheNameFactory cacheNameFactory) {
        this.cacheNameFactory = cacheNameFactory;
        return this;
    }

    /**
     * Set the compress listener, you can get the compressed image and the progress.
     *
     * @param compressListener the listener
     * @return the compress object
     */
    public Compress setCompressListener(CompressListener compressListener) {
        this.compressListener = compressListener;
        return this;
    }

    /**
     * Set the strategy used to compress the image. This method is often the last one for basic
     * options, other options are provided by each strategy.
     *
     * @param t the strategy instance, use {@link me.shouheng.compress.strategy.Strategies} to get
     *          all provided strategies.
     * @param <T> the strategy type.
     * @return the strategy instance.
     * @see AbstractStrategy
     */
    public <T extends AbstractStrategy> T strategy(T t) {
        t.setSrcFile(srcFile);
        t.setSrcBitmap(srcBitmap);
        t.setSrcData(srcData);
        t.setFormat(format);
        t.setQuality(quality);
        t.setOutFile(getOutFile());
        t.setCompressListener(compressListener);
        return t;
    }

    /**
     * Get the file the compressed image will be saved to.
     *
     * @return the output file.
     */
    private File getOutFile() {
        if (TextUtils.isEmpty(targetDir)) {
            File cacheDir = FileUtils.getDefaultCacheDir(context, Config.DEFAULT_CACHE_DIRECTORY_NAME);
            if (cacheDir == null) {
                throw new IllegalStateException("Cache directory is null, check your storage permission and try again.");
            } else {
                targetDir = cacheDir.getAbsolutePath();
            }
        }
        return new File(targetDir + File.separator + cacheNameFactory.getFileName());
    }

}
