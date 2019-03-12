package me.shouheng.sample.utils

import me.shouheng.compress.strategy.SimpleStrategy

class MySimpleStrategy: SimpleStrategy() {

    override fun calInSampleSize(): Int {
        return 2
    }

}