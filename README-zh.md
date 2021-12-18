<h1 align="center">
    一款易用的 Android 图片压缩框架
</h1>

<p align="center">
  <a href="http://www.apache.org/licenses/LICENSE-2.0">
    <img src="https://img.shields.io/hexpm/l/plug.svg" alt="License" />
  </a>
  <a href="https://bintray.com/beta/#/easymark/Android/compressor?tab=overview">
    <img src="https://img.shields.io/maven-metadata/v/https/s01.oss.sonatype.org/service/local/repo_groups/public/content/com/github/Shouheng88/compressor/maven-metadata.xml.svg" alt="Version" />
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
    <img src="https://img.shields.io/badge/Author-CodeBrick-orange.svg?style=flat-square" alt="Author" />
  </a>
  <a target="_blank" href="https://shang.qq.com/wpa/qunwpa?idkey=2711a5fa2e3ecfbaae34bd2cf2c98a5b25dd7d5cc56a3928abee84ae7a984253">
    <img src="https://img.shields.io/badge/QQ%E7%BE%A4-1018235573-orange.svg?style=flat-square" alt="QQ Group" />
  </a>
</p>

更高级的 Android 图片压缩库，支持自定义：

- 图片类型 (Uri/File/bitmap/Byte 数组/自定义)
- 结果类型 (Bitmap/File)
- 任务执行模式 (阻塞/RxJava/Kotlin 协程/AsyncTask)
- 压缩算法

要使用该库，首先添加 maven central 仓库，

```gradle
repositories { mavenCentral() }
```

然后添加该项目的依赖，

```gradle
implementation 'com.github.Shouheng88:compressor:latest-version'
```

示例：以压缩 Uri 图片、获取 File 类型的结果、在 Kotlin 协程中启动为例。整个代码是，

```kotlin
GlobalScope.launch {
    val result = Compress.with(context, file.uri(context))
        .setQuality(80)
        .concrete {
            withMaxWidth(100f)
            withMaxHeight(100f)
            withScaleMode(ScaleMode.SCALE_HEIGHT)
            withIgnoreIfSmaller(true)
        }
        .get(Dispatchers.IO)
    withContext(Dispatchers.Main) {
        Glide.with(context).load(result).into(binding.iv6)
    }
}
```

该库允许你对多个地方进行自定义，

- 了解更多使用方式，请阅读 [示例工程](sample/app/src/main/java/me/shouheng/sample/view/SampleActivity.kt).
- 了解如何自定义图片压缩算法，请阅读 [自定义算法示例](sample/app/src/main/java/me/shouheng/sample/custom/AlwaysHalfAlgorithm.kt).
- 了解如何自定义图片类型，请阅读 [自定义图片类型示例](sample/app/src/main/java/me/shouheng/sample/custom/AssetsResourceSource.kt).

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
