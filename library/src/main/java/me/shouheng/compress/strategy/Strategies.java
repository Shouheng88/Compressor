package me.shouheng.compress.strategy;

import me.shouheng.compress.strategy.compress.Compressor;
import me.shouheng.compress.strategy.luban.Luban;

/**
 * The provided strategies.
 *
 * @author WngShhng
 */
public final class Strategies {

    private Strategies() {
        throw new UnsupportedOperationException("u can't initialize me");
    }

    /**
     * The luban compress strategy
     *
     * @return luban strategy
     */
    public static Luban luban() {
        return new Luban();
    }

    /**
     * The compressor compress strategy
     *
     * @return compressor strategy
     */
    public static Compressor compressor() {
        return new Compressor();
    }

}
