![Banner](https://github.com/CostCost/Resources/blob/master/github/banner.jpg?raw=true)
![Offical](https://github.com/CostCost/Resources/blob/master/github/offical.png?raw=true)

<h1 align="center">
    一款现代、高效的 Android 图片压缩框架
</h1>

<p align="center">
  <a href="http://www.apache.org/licenses/LICENSE-2.0">
    <img src="https://img.shields.io/hexpm/l/plug.svg" alt="License" />
  </a>
  <a href="https://bintray.com/beta/#/easymark/Android/compressor?tab=overview">
    <img src="https://img.shields.io/maven-metadata/v/https/dl.bintray.com/easymark/Android/me/shouheng/compressor/compressor/maven-metadata.xml.svg" alt="Version" />
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
    <img src="https://img.shields.io/badge/Author-CodeBrick-orange.svg?style=flat-square" alt="Author" />
  </a>
  <a target="_blank" href="https://shang.qq.com/wpa/qunwpa?idkey=2711a5fa2e3ecfbaae34bd2cf2c98a5b25dd7d5cc56a3928abee84ae7a984253">
    <img src="https://img.shields.io/badge/QQ%E7%BE%A4-1018235573-orange.svg?style=flat-square" alt="QQ Group" />
  </a>
</p>

本项目主要基于 Android 自带的图片压缩 API 进行实现，提供了开源压缩方案 [Luban](https://github.com/Curzibn/Luban) 和 [Compressor](https://github.com/zetbaitsu/Compressor) 的实现，解决了单一 Fie 类型数据源的问题，并在它们的基础之上进行了功能上的拓展。该项目的主要目的在于：提供一个统一图片压缩框库的实现，集成常用的两种图片压缩算法，让你以更低的成本集成图片压缩功能到自己的项目中。

## 1、项目背景：开源的图片压缩库

现在 Github 上的图片压缩框架主要有 Luban 和 Compressor 两个。Star 的数量也比较高，一个 9K，另一个 4K. 但是，这两个图片压缩的库有各自的优点和缺点。下面我们通过一个表格总结一下：

|框架|优点|缺点|
|:-:|:-|:-|
|[Luban](https://github.com/Curzibn/Luban)|据说是根据微信图片压缩逆推的算法|1.只适用于一般的图片展示的场景，无法对图片的尺寸进行精准压缩；2.内部封装 AsyncTaks 来进行异步的图片压缩，对于 RxJava 支持不好。3.只支持单一 File 类型数据源和结果类型，应用场景有限。|
|[Compressor](https://github.com/zetbaitsu/Compressor)|1.可以对图片的尺寸进行精准压缩；2.支持 RxJava。|1.尺寸压缩的场景有限，如果有特别的需求，则需要手动修改源代码；2.图片压缩采样的时候计算有问题，导致采样后的图片尺寸总是小于我们指定的尺寸；3.只支持单一 File 类型数据源和结果类型，应用场景有限。|

以上，我们总结了 Github 上面的两款比较受欢迎的开源图片压缩库的优缺点。正如我们上面所说，我们希望能够综合它们的优点，并且解决以上两款开源库设计不好的地方。因此，你可以通过下文来了解我们的库所提供的功能，以及如何在您的项目中接入我们的压缩方案。

## 2、功能和特性

目前，我们的库已经支持了下面的功能，在后面的介绍中，我们会介绍如何在项目中进行详细配置和使用：

- **支持 Luban 压缩方案**：据介绍这是微信逆推的压缩算法，它在我们的项目中只作为一种可选的压缩方案，除此之外您还可以使用 Compressor 进行压缩，以及自定义压缩策略。

- **支持 Compressor 压缩方案**：这种压缩方案综合了 Android 自带的三种压缩方式，可以对压缩结果的尺寸进行精确的控制。此外，在我们的项目中，我们对这种压缩方案的功能进行了拓展，不仅支持了颜色通道的选择，还提供了多种可选的策略，用来对尺寸进行更详细的配置。
- **支持 RxJava 的方式进行压缩**：使用 RxJava 的方式，您可以任意指定压缩任务和结果回调任务所在的线程，在我们的库中，我们提供了一个 Flowable 类型的对象，您可以用它来进行后续的处理。
- **支持 AsyncTask + 回调的压缩方式**：这种方式通过使用 AsyncTask 在后台线程中执行压缩任务，当获取到压缩结果的时候通过 Handler 在主线程中返回压缩结果。
- **提供了同步的压缩方式**：当然，有时候我们并不需要使用回调或者 RxJava 执行异步任务。比如，当我们本身已经处于后台线程的时候，我们希望的只是在当前线程中直接执行压缩任务并拿到压缩结果。因此，为了让我们的库适用于更多的应用场景，我们提出了这种压缩方案。
- **支持多种数据源**：在上面的两款开源库中，要求传入的资源类型是 File。这就意味着，当我们从相机中获取到原始的图片信息（通常是字节数组）的时候，我们不得不先将其写入到文件系统中，然后获取到 File 的时候再进行压缩。这是没必要的，并且无疑地，会带来性能上的损耗。因此，为了能让我们的库应用到更多的场景中，我们支持了多种数据源。目前支持的数据源包括：文件类型 File，原始图片信息 byte[] 以及 Bitmap。
- **支持多种结果类型**：以上两款开源库还存在一个问题，即返回的结果的类型也只支持 File 类型。但很多时候，我们希望传入的是 Bitmap，处理之后传出的结果也是 Bitmap. 因此，为了让我们的库适用于这种场景，我们也支持 Bitmap 类型的返回结果。
- **提供用户自定义压缩算法的接口**：我们希望设计的库可以允许用户自定义压缩策略。在想要替换图片压缩算法的时候，通过链式调用的一个方法直接更换策略即可。即，我们希望能够让用户以最低的成本替换项目中的图片压缩算法。

想要进一步了解该库的特性和功能，你还可以使用我们提供的示例 [APK](resources/app-debug.apk)


[<div align="center"><img height="300" src="resources/sample_preview.jpg" alt="示例程序预览图"/></div>](resources/app-debug.apk)


## 3、使用

### 3.1 在 Gradle 中引用我们的库

在项目中接入我们的库是非常简单的。首先，在项目的 Gradle 中加入 jcenter仓库：

```gradle
repositories {
    jcenter()
}
```

然后，在项目的依赖中添加该库的依赖：

```gradle
implementation 'me.shouheng.compressor:compressor:latest-version'
```

### 3.2 使用我们库进行压缩

详细的用法你可以参考我们提供的 Sample 程序，这里我们介绍下使用我们库的几个要点：

首先，你要使用 Compress 类的静态方法获取一个 Compress 实例，这是所有配置的起点。针对不同的数据源，你需要调用它的三个不同的工厂方法：

```kotlin
    // 使用文件 File 获取 Compress 实例
    val compress = Compress.with(this, file)
    // 使用图片的字节数组获取 Compress 实例
    val compress = Compress.with(this, byteArray)
    // 使用图片的 Bitmap 获取 Compress 实例
    val compress = Compress.with(this, bitmap)
```

然后，你可以调用 Compress 的实例方法来对压缩的参数进行基本的配置：

```kotlin
    compress
        // 指定要求的图片的质量
        .setQuality(60)
        // 指定文件的输出目录（如果返回结果不是 File 的会，无效）
        .setTargetDir(PathUtils.getExternalPicturesPath())
        // 指定压缩结果回调（如哦返回结果不是 File 则不会被回调到）
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

以上是使用 Compress 进行基本的配置，上面我们给出了一些注释。关于各个方法的含义，后续我们会进行详细的介绍。

根据上述配置，我们就得到了一个 Compress 对象。然后，我们需要指定一个图片压缩策略，并调用压缩策略的方法进行更详细的配置。以 Compressor 为例，我们可以调用 Strategies.compressor() 方法获取它的实例：

```kotlin
val compressor = compress
        .strategy(Strategies.compressor())
        .setConfig(config)
        .setMaxHeight(100f)
        .setMaxWidth(100f)
        .setScaleMode(scaleMode)
```

按照上述配置，我们就拿到了 Compressor 实例。当然如果你的配置使用比较频繁，为了简化代码，你可以把这些配置过程放在一个工具方法中，而无需每次都进行这些配置。

下面就是触发图片压缩并获取压缩结果的过程了。

上面我们也提到过，针对 File 类型和 Bitmap 类型的返回结果，我们提供了两个方案。默认的返回类型是 File，为了得到 Bitmap 类型的结果，你只需要调用一下 Compressor 实例的 `asBitmap()` 方法，这样整个流程就‘拐’到了 Bitmap 的构建中去了。

最终触发图片压缩有三种方式，

```kotlin
    // 方式 1：使用 AsyncTask 压缩，此时只能通过之前的回调获取压缩结果
    compressor.launch()
    // 方式 2：将压缩任务转换成 Flowable，使用 RxJava 指定任务的线程和获取结果的线程
    val d = compressor
        .asFlowable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            ToastUtils.showShort("Success [Compressor,File,Flowable] $it")
            displayResult(it.absolutePath)
        }, {
            ToastUtils.showShort("Error [Compressor,File,Flowable] : $it")
        })
    // 方式 3：直接在当前线程中获取返回结果（同步，阻塞）
    val resultFile = compressor.get()
```

对于 Luban 压缩方式的使用与之类似，只需要在指定压缩策略的那一步中将策略替换成 luban 即可。另外，对于自定义图片压缩的方式也是类似的，只需要在指定策略的那一步骤中指定即可。

因此，如果使用的是 RxJava 的方式获取压缩结果，并且输入类型是 File，输出类型是 Bitmap，整个压缩的流程将是下面这样：

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

### 3.3 你可能需要注意的事项

- 如果你期望返回的结果是 Bitmap，那么下面这些方法传入的参数将不会起作用：

    - `setTargetDir(/*...*/)`：用来指定输出文件的位置
    - `setCompressListener(/*...*/)`：用来获取文件的压缩结果的回调

- 当你使用 Luban 算法压缩 Bitmap 的时候，内部会采用类似于 Compressor 的压缩逻辑，但是输出的结果与传入 File 类型时一致。这是因为 Luban 的逻辑无非就是用来采样，而采样是在加载到内存中的时候进行的，因为输入本身已经是 Bitmap，因此无法按照原来的逻辑进行压缩。

- Luban 不支持 Bitmap 的色彩配置，因为本身没有创建 Bitmap。对于 Compressor 的色彩，虽然所有参数都可以使用，但是在使用的时候可能会出现不支持的情况。此时，不会导致程序 Crash，错误的信息会通过回调或者 RxJava 回调出来。

- 1.0.0 到 1.3.0 的迁移：我们已经尽可能适配之前的方案，但是仍然有一部分内容我们做了调整。您可以按照下面的步骤做迁移工作：

    - Compressor 的压缩策略从 Configuration 中移动到了 ScaleMode 中。
    - 重命名 Configuration 类为 Config，因为它可能会与您项目中其他的代码命名冲突而不得不指定包名称。
    - 一些工具类命名调整，您的项目中一般不会用到它们，但是如果用到了注意调整下名称。

## 4、项目资料

如果您对该项目感兴趣并且希望为该项目共享您的代码，那么您可以通过下面的一些资料来了解相关的内容：

1. 项目整体的架构设计：[https://www.processon.com/view/link/5cdfb769e4b00528648784b7](https://www.processon.com/view/link/5cdfb769e4b00528648784b7)
2. Android 图片压缩 API 的介绍，该项目的简介等：[《开源一个 Android 图片压缩框架》](https://juejin.im/post/5c87d01f6fb9a049b7813784)
3. 我们提供的示例 APK：[app-debug.apk](resources/app-debug.apk)
4. [更新日志](CHANGELOG.md)

## 关于作者

你可以通过访问下面的链接来获取作者的信息：

1. Twitter: https://twitter.com/shouheng_wang
2. Github: https://github.com/Shouheng88
3. 掘金：https://juejin.im/user/585555e11b69e6006c907a2a
4. 简书: https://www.jianshu.com/u/e1ad842673e2

## 捐赠项目

我们致力于为广大开发者和个人开发者提供快速开发应用的解决方案。您可以通过下面的渠道来支持我们的项目，

<div style="display:flex;" id="target">
<img src="https://github.com/CostCost/Resources/blob/master/github/ali.jpg?raw=true" width="25%" />
<img src="https://github.com/CostCost/Resources/blob/master/github/mm.png?raw=true" style="margin-left:10px;" width="25%"/>
</div>

## License

```
Copyright (c) 2019-2020 CodeBrick.

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
