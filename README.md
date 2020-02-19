# Mordern and advanced: an easy to use image compress library for Android

[![Build Status](https://www.travis-ci.org/Shouheng88/Compressor.svg?branch=master)](https://www.travis-ci.org/Shouheng88/Compressor)
![SLicense](https://img.shields.io/hexpm/l/plug.svg)
![Version](https://img.shields.io/maven-metadata/v/https/dl.bintray.com/easymark/Android/me/shouheng/compressor/compressor/maven-metadata.xml.svg)

Other languages README docs: [中文版](./README-zh.md)

This project is mainly designed based on the Android image compress API. Also, it provided two image compress implementions based on open souce libraries  [Luban](https://github.com/Curzibn/Luban) and [Compressor](https://github.com/zetbaitsu/Compressor). In this library, it provided interfaces for multiple and different types of image source and result -- file, byte array, bitmap etc. It provided sync and async API to meet more circumstances. And it put forward the struture so that you can easily and conveniently switch from different strategys mentationed above.

## 1 Background: Open source image libraries

Now, on github, here are maily two image compress libraries for Android witch has a large number stars, namely, Luban and Compressor. But both of these two libraries has its pros and cons. Here we made the table below:

|Library|Strength|Weakness|
|:-:|:-|:-|
|[Luban](https://github.com/Curzibn/Luban)|Based on the algorithm of WeChat|1. can't make an exact output image size；2. based on AsyncTask, do not support RxJava; 3. support only one image source type|
|[Compressor](https://github.com/zetbaitsu/Compressor)|1. can make an exact output image size; 2. support RxJava|1. support only one output size strategy, if you have special requirement, you have to change the source code; 2. support only one image source type|

Above, we summarized two most welcome libraries on Github. As we talked above, we want to design an libraries witch combine the strengths and discard the weakness of them. Next we will show the function and features of our library.

## 2 Functions and features

Now lets show you the functions and features of our library:

- **Support Luban**: As mentationed, it provided an algorithm based on WeChat. Except that, you can also use Compressor to get more features.

- **Support Compressor**: This strategy combined three Android image compressed methods, so you can get an exact output image size. Expecially, we, in our library, strengthend them, and provided more options to support more circumstances.

- **Support RxJava callback**: By RxJava, you can specify thread of async tasks and result callback. In this library, we provided a Flowable object, you can use it to processed later.

- **Support AsyncTask callback**: Except RxJava, you can also use AsyncTask to run background task, and get the result in main thread from callback.

- **Support synchronous APIs**: Sometimes, synchronous APIs can be more useful. For example, you want to make an custom call, run and get the result of task all in current thread. To meet this requirement, we provided synchronous APIs.

- **Support more image sources types**: Most of the liraries, the required image type was File. But when we got the image data from camera APIs, it turn out to be byte array. So in other libraries, you have to transfer data from byte array to File. That means you have to write data to file system, witch no doubt may lower the performance of your application. Currently, our library support image source types include File, byte array, file path and Bitmap.

- **Support more image result types**: Sometimes, when we got the compressed result, we have to process it later. In Android, we use Bitmap. So in this circumstance, it's better to get Bitmap than File. So, to meet this requirement, we provided result type of Bitmap.

- **Provided custom interfaces**: Except algorithms above, we also provided user custom interfaces. We built an structure so that user can easily and conveniently switch from different strategys. And a builder chain for user to elegantly call this library.

- **More**: To get more features and functions about this library, you can install our sample [APK](resources/app-debug.apk) to get more informations.

[<div align="center"><img height="300" src="resources/sample_preview.jpg" alt="示例程序预览图"/></div>](resources/app-debug.apk)

## 3 Usage

### 3.1 Introduce our library in Gradle

It's convenient to introduce our lirary in your project. First, add  jcenter repository in your project:

```gradle
repositories {
    jcenter()
}
```

Then, add our library in your dependency:

```gradle
implementation 'me.shouheng.compressor:compressor:latest-version'
```

### 3.2 Use our library

In this article, we will show you some main points. For details, please install our sample App.

First, you should use the static methods of Compress to get a an instance of it, which is all magic begins. It has three different factory methods:

```kotlin
    // Factory 1: Use File to get Compress instance
    val compress = Compress.with(this, file)
    // Factory 2: Use byte array to get Compress instance
    val compress = Compress.with(this, byteArray)
    // Factory 3: Use Bitmap to get Compress instance
    val compress = Compress.with(this, bitmap)
```

Then, you can call methods of Compress to config image options:

```kotlin
    compress
        // Sepcify image quality
        .setQuality(60)
        // Specify output directory
        .setTargetDir(PathUtils.getExternalPicturesPath())
        // Specify callback of result
        .setCompressListener(object : CompressListener {
            override fun onStart() {
                LogUtils.d(Thread.currentThread().toString())
                ToastUtils.showShort("Start [Compressor,File]")
            }
    
            override fun onSuccess(result: File?) {
                LogUtils.d(Thread.currentThread().toString())
                displayResult(result?.absolutePath)
                ToastUtils.showShort("Success [Compressor,File] : $result")
            }
    
            override fun onError(throwable: Throwable?) {
                LogUtils.d(Thread.currentThread().toString())
                ToastUtils.showShort("Error [Compressor,File] : $throwable")
            }
        })
```

These are basic confiurations of Compress, by comments, you can find out meanings of every method.

As above, we got a Compress oject. Then we could specify image compress strategy. Take Compressor strategy as example, we could use `Strategies.compressor()` to get instance of this strategy:

```kotlin
val compressor = compress
        // Specify strategy
        .strategy(Strategies.compressor())
        .setConfig(config)
        // Set desired output width and height
        .setMaxHeight(100f)
        .setMaxWidth(100f)
        // Set desiged output scale mode
        .setScaleMode(scaleMode)
```

Below is the process of getting image compress result. As mentationed, for  File and Bitmap type results. The default result type is File, to get Bitmap, you need to call `asBitmap()` of Compressor.

To finally get the result you have three options:

```kotlin
    // Option 1: Use AsyncTask to run task and listener to get result
    compressor.launch()
    // Option 2: To transfer result to Flowable and use RxJava to specify thread and get result
    val d = compressor
        .asFlowable()
        // Specify thread
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        // Subscribe and get result
        .subscribe({
            ToastUtils.showShort("Success [Compressor,File,Flowable] $it")
            displayResult(it.absolutePath)
        }, {
            ToastUtils.showShort("Error [Compressor,File,Flowable] : $it")
        })
    // Option 3: Use async and blocking API to get result in current thread
    val resultFile = compressor.get()
```

If you want to use another strategy, you can simply use `Strategies.luban()` instead of `Strategies.compressor()` to switch. Excpet these two provided strategies, you can also make a custom strategy by implementing interface.

So, the full code will be:

```kotlin
    val compressor = Compress.with(this@MainActivity, file)
        .strategy(Strategies.compressor())
        .setConfig(config)
        .setMaxHeight(100f)
        .setMaxWidth(100f)
        .setScaleMode(scaleMode)
        .asBitmap()
        .asFlowable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            ToastUtils.showShort("Success [Compressor,Bitmap,Flowable] $it")
            displayResult(it)
        }, {
            ToastUtils.showShort("Error [Compressor,Bitmap,Flowable] : $it")
        })
```

Elegant, isn't it?

## 3 More about project

We are glad if you could contribute to this project. Here, we provied more about our project to help you:

1. Library structure: [https://www.processon.com/view/link/5cdfb769e4b00528648784b7](https://www.processon.com/view/link/5cdfb769e4b00528648784b7)
2. Android compress APIs and this library introduction: [《开源一个 Android 图片压缩框架》](https://juejin.im/post/5c87d01f6fb9a049b7813784)
3. Sample APK: [app-debug.apk](resources/app-debug.apk)

## Release logs

- Version 1.3.1:

	- Do image quality compress when return Bitmap
	- Solved the Bitmap recycle problem

- Version 1.3.0:

    - Added more image source types
    - Added Bitmap result type
    - Added async APIs
    - Added color option
    - Changed project structure

- Version 1.0.0:

  - Solved problem of rotation of Compressor image

## Author

Visit the links below to get more information about author:

1. Twitter: https://twitter.com/shouheng_wang
2. Github: https://github.com/Shouheng88
3. Juejin：https://juejin.im/user/585555e11b69e6006c907a2a

## License

```
Copyright (c) 2019 WngShhng.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
