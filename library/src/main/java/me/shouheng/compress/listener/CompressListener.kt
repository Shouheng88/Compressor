package me.shouheng.compress.listener

import me.shouheng.compress.RequestBuilder

import java.io.File

/** The compress state callback. @author Shouheng Wang */
interface CompressListener : RequestBuilder.Callback<File>
