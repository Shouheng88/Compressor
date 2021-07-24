package me.shouheng.sample.utils

import me.shouheng.compress.strategy.SimpleStrategy

/** The simple strategy, which always compress image to half. */
class AlwaysHalfAlgorithm: SimpleStrategy() {

    /** The calculated sample image size. */
    override fun calInSampleSize(): Int = 2

}
