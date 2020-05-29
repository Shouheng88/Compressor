package me.shouheng.compress.listener

import me.shouheng.compress.RequestBuilder

import java.io.File

/**
 * The compress state callback. This callback is used to listen to the file
 * type result. Mainly used to
 *
 * @author WngShhng (shouheng2015@gmail.com)
 */
interface CompressListener : RequestBuilder.Callback<File>
