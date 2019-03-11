package me.shouheng.sample.utils

import io.reactivex.Flowable
import me.shouheng.compress.AbstractStrategy
import java.io.File

class MyStrategy : AbstractStrategy() {

    override fun asFlowable(): Flowable<File> {
        return Flowable.just(srcFile)
    }

    override fun launch() {
        // empty
    }

}