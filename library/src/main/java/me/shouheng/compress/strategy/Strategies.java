package me.shouheng.compress.strategy;

import me.shouheng.compress.strategy.compress.Compressor;
import me.shouheng.compress.strategy.luban.Luban;

/**
 * The provided strategies.
 */
public final class Strategies {

    private Strategies() {
    }

    public static Luban luban() {
        return new Luban();
    }

    public static Compressor compressor() {
        return new Compressor();
    }

}
