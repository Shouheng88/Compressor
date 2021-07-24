package me.shouheng.compress.listener

import me.shouheng.compress.RequestBuilder

import java.io.File

/**
 * The compress state callback. This callback is used to listen to the file
 * type result. Mainly used to
 *
 * @author Shouheng Wang
 */
interface CompressListener : RequestBuilder.Callback<File>
