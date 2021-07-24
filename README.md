<h1 align="center">An easy to use image compress library for Android</h1>

<p align="center">
  <a href="http://www.apache.org/licenses/LICENSE-2.0">
    <img src="https://img.shields.io/hexpm/l/plug.svg" alt="License" />
  </a>
  <a href="https://bintray.com/beta/#/easymark/Android/compressor?tab=overview">
    <img src="https://img.shields.io/maven-metadata/v/https/s01.oss.sonatype.org/service/local/repo_groups/public/content/com/github/Shouheng88/compressor/maven-metadata.xml.svg" alt="Version" />
  </a>
  <a href="https://www.codacy.com/manual/Shouheng88/Compressor?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Shouheng88/Compressor&amp;utm_campaign=Badge_Grade">
    <img src="https://api.codacy.com/project/badge/Grade/84a3602d08034493a4a62c73f7ad86f4" alt="Code Grade"/>
  </a>
  <a href="https://www.travis-ci.org/Shouheng88/Compressor">
    <img src="https://www.travis-ci.org/Shouheng88/Compressor.svg?branch=master" alt="Build"/>
  </a>
    <a href="https://developer.android.com/about/versions/android-4.2.html">
    <img src="https://img.shields.io/badge/API-17%2B-blue.svg?style=flat-square" alt="Min Sdk Version" />
  </a>
   <a href="https://github.com/Shouheng88">
    <img src="https://img.shields.io/badge/Author-SHW-orange.svg?style=flat-square" alt="Author" />
  </a>
  <a target="_blank" href="https://shang.qq.com/wpa/qunwpa?idkey=2711a5fa2e3ecfbaae34bd2cf2c98a5b25dd7d5cc56a3928abee84ae7a984253">
    <img src="https://img.shields.io/badge/QQ%E7%BE%A4-1018235573-orange.svg?style=flat-square" alt="QQ Group" />
  </a>
</p>

<p align="center">
    <a href="./README-zh.md">中文版</a>
</p>

## 1 Introduction

This project is mainly designed based on the Android image compress API. It allow to use  different types of image sources and result types. It provided sync and async API to meet more requirements. And it put forward the struture so that you can easily switch from different compress algorithms.

## 2 Functions and features

Now lets show you the functions and features of our library:

- **Support automatic algorithm**: Calculate the in sample size according to image size, see [Luban](https://github.com/Curzibn/Luban).

- **Support aoncrete algoruthm**: You are able to get an exact image size, see [Compressor](https://github.com/zetbaitsu/Compressor).

- **Support RxJava callback**: We will return a Flowable object so you can use it as RxJava.

- **Support AsyncTask callback**: Except RxJava, you can also use AsyncTask to run background task, and get the result in main thread from callback.

- **Support kotlin coroutines**: Also, you can use the library in kotlin coroutines.

- **Support synchronous APIs**

- **Support to stretch images by width/height/longer side/smaller side**

- **Support 3 image sources types**: Most of the liraries, the required image type was File. But when we got the image data from camera APIs, it turn out to be byte array. So in other libraries, you have to transfer data from byte array to File. That means you have to write data to file system, witch no doubt may lower the performance of your App.. Currently, our library support image source types include File, byte array, file path and Bitmap.

- **Support 2 image result types**: Sometimes, when we got the compressed result, we have to process it later. In Android, we use Bitmap. In this circumstance, it's better to get Bitmap than File. So, to meet this requirement, we provided result type of Bitmap.

- **Provided custom interfaces**: Except algorithms above, we also provided user custom interfaces. We built an structure so that user can easily and conveniently switch from different strategys.

- **More**: To get more features and functions about this library, you can install our sample [APK](resources/app-debug.apk) to get more informations.

[<div align="center"><img height="300" src="resources/sample_preview.jpg" alt="示例程序预览图"/></div>](resources/app-debug.apk)

## 3 Usage

### 3.1 Introduce our library in Gradle

It's convenient to use our lirary in your project. 

First, add  jcenter repository in your project:

```gradle
repositories { mavenCentral() }
```

Then, add our library in your dependency:

```gradle
implementation 'com.github.Shouheng88:compressor:latest-version'
```

### 3.2 Use our library

**1. Get compress object**

First, you should use the static methods of Compress to get a an instance of it, which is the magic begins. It has three different factory methods correspond to three different type of image sources:

```kotlin
// Factory 1: Use File to get Compress instance
val compress = Compress.with(this, file)

// Factory 2: Use byte array to get Compress instance
val compress = Compress.with(this, byteArray)

// Factory 3: Use Bitmap to get Compress instance
val compress = Compress.with(this, bitmap)
```

Then, you can call methods of `compress` instance to config basic image options. Basic options are those used in all strategies. That's why you can easily switch from different algorithms.

```kotlin
compress
    // Sepcify image quality
    .setQuality(60)
    // Specify output directory
    .setTargetDir(PathUtils.getExternalPicturesPath())
    // Specify callback of result
    .setCompressListener(object : CompressListener {
        override fun onStart() {
            // callback when compress start
        }

        override fun onSuccess(result: File?) {
            // callback when compress succeed
        }

        override fun onError(throwable: Throwable?) {
            // callback when compress error
        }
    })
```

**2. Specify algorithm**

Then we need to specify compress strategy (algorithm). Take Compressor strategy as an example, we could use `Strategies.compressor()` to get instance of it. And set details of it by calling `setMaxHeight`, `setMaxWidth` etc. Different algorithm might have different configurations.

```kotlin
val compressor = compress
    .strategy(Strategies.compressor()) // Specify strategy
    .setMaxHeight(100f) // Set desired output width and height
    .setMaxWidth(100f)
    .setScaleMode(scaleMode) // Set desiged output scale mode
```

On 1.4.0 and above, you can use the kotlin DSL to specify algorithm,

```kotlin
compress
    .setQuality(60)
    .setTargetDir(PathUtils.getExternalPicturesPath())
    .setCompressListener(getCompressListener("Concrete", compressorResultType))
    .concrete {
        this.config = colorConfig
        this.maxWidth = 100f
        this.maxHeight = 100f
        this.scaleMode = imageScaleMode
    }
// --- or ---
Compress.with(context, byteArray)
    .automatic {
        // do nothing
    }
    .asBitmap()
    .get()
```

**3. Change result type**

Next, as mentioned above, if you want to get compressed image of Bitmap, you should use `asBitmap()`. Otherwise, the compressed result will be File type,

```kotlin
compressor = compressor.asBitmap()
```

**4. Invoke the compress**

To finally get the result you have 4 options correspond to 4 different ways async/sync apis:

```kotlin
// Option 1: use AsyncTask to execute async task and to get result from callback
compressor.launch()

// Option 2: use Flowable and RxJava to get result
val d = compressor
    .asFlowable()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe( ... )

// Option 3: use sync and blocking API to get result in current thread
val resultFile = compressor.get()

// Option 4: get the result by kotlin coroutines
GlobalScope.launch {
    val resultFile = compressor.get(Dispatchers.IO)
}
```

**5. Complete code**

If you want to use another strategy, you can simply use `Strategies.luban()` instead of `Strategies.compressor()`. Excpet these two strategies, you can also make a custom strategy.

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
    .subscribe( ... )
```

### 3.3 Detail configurations about compressor

**1. ignoreIfSmaller**

This filed used to specifiy action when the current image size is smaller than required size. If it's true, the image will be ignored and the origin image will be returned, otherwise, the origin image will be stretched to required size.

**2. scaleMode**

The scale mode is used to specify image stretching ways while current image size ratio differs from desired image size ratio. It has 4 options:

- SCALE_LARGER: Scale according to larger side, another will change according to original image width/height ratio. For example: 1). If the original image is (W:1000, H:500), destination is (W:100, H:100), then the result size will be (W:100, H:50). 2). If the original image is (W:500, H:1000), destination is (W:100, H:100), then the result size will be (W:50, H:100).

- SCALE_SMALLER: Scale according to smaller, another side will change according to original image width/height ratio. For example: 1). If the original image is (W:1000, H:500), destination is (W:100, H:100), then the result size will be (W:200, H:100). 2). If the original image is (W:500, H:1000), destination is (W:100, H:100), then the result size will be (W:100, H:200).

- SCALE_WIDTH: Scale the width, and the height will change according to the image ratio. For example: 1). If the original image is (W:1000, H:500), destination is (W:100, H:100). then the result size will be (W:100, H:50). 2). If the original image is (W:500, H:1000), destination is (W:100, H:100), then the result size will be (W:100, H:200).

- SCALE_HEIGHT: Scale the width, and the height will change according to the image ratio. For example: 1). If the original image is (W:1000, H:500), destination is (W:100, H:100). then the result size will be (W:200, H:100). 2). If the original image is (W:500, H:1000), destination is (W:100, H:100), then the result size will be (W:50, H:100).

## 3 More

I'm glad if you could contribute to this project. Here, we provied more about our project to help you:

1. Library structure: [https://www.processon.com/view/link/5cdfb769e4b00528648784b7](https://www.processon.com/view/link/5cdfb769e4b00528648784b7)
2. Sample APK: [app-debug.apk](resources/app-debug.apk)
3. [Release Log](CHANGELOG.md)

## License

```
Copyright (c) 2019-2021 Shouheng Wang.

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
