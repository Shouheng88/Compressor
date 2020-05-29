package me.shouheng.sample

import me.shouheng.compress.strategy.SimpleStrategy

/**
 * The simple strategy demo
 */
class MySimpleStrategy: SimpleStrategy() {

    override fun calInSampleSize(): Int {
        return 2
    }
}