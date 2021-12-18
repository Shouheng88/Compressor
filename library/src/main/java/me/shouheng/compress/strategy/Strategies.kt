package me.shouheng.compress.strategy

import me.shouheng.compress.strategy.compress.Compressor
import me.shouheng.compress.strategy.luban.Luban

/**
 * The provided strategies.
 *
 * @author Shouheng Wang
 */
object Strategies {

    /** The luban compress strategy, used to get the image for preview.
     * See https://github.com/Curzibn/Luban for details. */
    fun luban(): Luban = Luban()

    /** The compressor compress strategy, used to specify the image width height constraints. */
    fun compressor(): Compressor = Compressor()
}
