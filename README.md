# 项目说明 [![](https://jitpack.io/v/zimolab/js-array.svg)](https://jitpack.io/#zimolab/js-array)
一个将JavaScript中的Array对象映射为Kotlin（java）对象的库。该项目基于netscape.javascript.JSObject对象，映射了Javascript Array对象的大部分接口，
适用于使用WebEngine与底层Javascript代码进行交互的情景。

## API

### 接口
```kotlin
// 创建实例：
// 1 、一般通过JsArray.xxArrayOf(jsObject: JSObject) ，从现有JSObject（必须指向一个js Array对象）创建实例
// JsArray类中的相关接口包括：
fun stringArrayOf(reference: JSObject): JsArray<String?>
fun booleanArrayOf(reference: JSObject): JsArray<Boolean?>
fun intArrayOf(reference: JSObject): JsArray<Int?>
fun doubleArrayOf(reference: JSObject): JsArray<Double?>
fun jsObjectArrayOf(reference: JSObject): JsArray<JSObject?>
fun anyArrayOf(reference: JSObject): JsArray<Any?>
// 2、也可以调用newArray(...)方法在Javascript环境中新建一个对象，然后再调用上述方法创建实例
fun newArray(env: WebEngine, initialSize: Int = 0): JSObject?
fun newArray(env: JSObject, initialSize: Int = 0): JSObject?

// JsArrayInterface APIs:

// 存取值操作。重载了[]操作符，以便使用a[0], a[1] = ...的语法
operator fun set(index: Int, value: T?)
operator fun get(index: Int): T?

// 数组操作
fun concat(other: JsArrayInterface<T>): JsArrayInterface<T>
fun join(separator: String = ","): String
fun reverse(): JsArrayInterface<T>
fun pop(): T?
fun push(vararg elements: T?): Int
fun shift(): T?
fun unshift(vararg elements: T?): Int
fun slice(start: Int, end: Int? = null): JsArrayInterface<T>
fun splice(index: Int, count: Int, vararg items: T?): JsArrayInterface<T>
fun fill(value: T?, start: Int = 0, end: Int? = null): JsArrayInterface<T>

// 与迭代有关的API
fun find(callback: JsArrayIteratorCallback<T?, Boolean>): T?
fun findIndex(callback: JsArrayIteratorCallback<T, Boolean>): Int
fun includes(element: T?, start: Int = 0): Boolean
fun indexOf(element: T?, start: Int = 0): Int
fun lastIndexOf(element: T?, start: Int = -1): Int
fun forLoop(callback: JsArrayIteratorCallback<T?, Boolean>, startIndex: Int = 0, stopIndex: Int = -1, step: Int = 1)
fun forEach(callback: JsArrayIteratorCallback<T?, Unit>)
fun filter(callback: JsArrayIteratorCallback<T?, Boolean>): JsArrayInterface<T>
fun map(callback: JsArrayIteratorCallback<T?, T?>): JsArrayInterface<T>
fun every(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean
fun some(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean
fun <R> reduce(callback: JsArrayIteratorCallback<T?, R?>): R?
fun <R> reduceRight(callback: JsArrayIteratorCallback<T?, R?>): R?

// 排序操作
fun sort(sortFunction: JsArraySortFunction<T?>? = null): JsArrayInterface<T>
```

### 实现类型
JsArray类实现了JsArrayInterface接口，并实现了一些开箱可用的“常见”类型的Js数组。

之所以要预先定义这些类型的数组，一方面是因为这些类型基本涵盖了多数使用需求和场景，另一方面则是因为这些类型可以自动在Js与Java之间进行转换。
关于这一点，可以参考JavaFx WebView文档中关于Java与JS之间类型映射的部分，比如[这里](https://openjfx.io/javadoc/11/javafx.web/javafx/scene/web/WebEngine.html) 。
> #### Mapping JavaScript values to Java objects
> JavaScript values are represented using the obvious Java classes: 
> **null becomes Java null**; 
> a **boolean becomes a java.lang.Boolean**; 
> and a **string becomes a java.lang.String**. 
> A **number** can be **java.lang.Double** or a **java.lang.Integer**, depending. 
> The **undefined** value maps to a **specific unique String object whose value is "undefined"**.
>
> If the result is a **JavaScript object**, it is wrapped as an instance of the **JSObject** class. 
> (As a special case, if the JavaScript object is a JavaRuntimeObject as discussed in the next section, then the original Java object is extracted instead.) 
> The JSObject class is a proxy that provides access to methods and properties of its underlying JavaScript object. The most commonly used JSObject methods are getMember (to read a named property), setMember (to set or define a property), and call (to call a function-valued property).

> #### Mapping Java objects to JavaScript values
> The arguments of the JSObject methods setMember and call pass Java objects to the JavaScript environment. 
> This is **roughly the inverse of the JavaScript-to-Java mapping described above**:
> **Java String, Number, or Boolean objects are converted to the obvious JavaScript values**.
> **A JSObject object is converted to the original wrapped JavaScript object**.
> Otherwise a JavaRuntimeObject is created. This is a JavaScript object that acts as a proxy for the Java object, in that accessing properties of the JavaRuntimeObject causes the Java field or method with the same name to be accessed.

|  Javascript类型   | Java类型        |
|  ----------------| -------------- |
| string           | String         |
| bool            | Boolean         |
| number          | Integer或Double |
| undefine        | "undefined"    |
| 其他Object       | JSObject      |

#### 一些开箱可用的JS数组类型（实际上是具体JsArray<T>类的类型别名）
##### JsStringArray (JsArray<String?>)
字符串数组

##### JsIntArray
整数型数组

##### JsDoubleArray
双精度浮点数数组

##### JsBooleanArray
布尔型数组

##### JSObjectArray
Javascript对象数组（即netscape.javascript.JSObject对象数组）

##### JsAnyArray
任意型数组，包括字符串、整数、浮点数、boolean以及JavaScript中的任意对象（需要被映射为JSObject对象）

#### 自定义类型的JS数组
除了上述内置的几种类型，还可以自行实现JsArrayInterface<T>接口，以实现自定义类型的JS数组。 可以参考JsArray类的实现代码。

## 快速入门
导入依赖
```
// 1、在build.gradle中添加jitpack仓库地址
// 1) groovy
repositories {
    maven { url 'https://jitpack.io' }
}
// 2) kotlin DSL
repositories {
    maven {
        setUrl("https://jitpack.io")
    }
}

// 2、添加依赖
// 1) groovy
dependencies {
    implementation 'com.github.zimolab:js-array:v0.1.0-SNAPSHOT'
}

// 2) kotlin DSL
dependencies {
    implementation("com.github.zimolab:js-array:v0.1.0-SNAPSHOT")
}
```

1、创建对象（以JsIntArray为例）
```kotlin
// 1）、从已有对象创建 （假设在js中有一个名为intArr的全局对象）
val arrayInJs: JSObject = webEngine.executeScript("window.intArr") as JSObject
// 在这一步前，可以调用JsInterface.isArray()判断arrayInJs是否为Array对象，防止抛出异常
val jsIntArray = JsArray.intArrayOf(arrayInJs)

//2）、新建一个对象
val arrayInJs = JsArray.newArray(10)
val jsIntArray = JsArray.intArrayOf(arrayInJs)
// 可以用fill填充，防止空值
jsIntArray.fill(0)
```

2、调用接口
```kotlin
// 创建成功后就可以调用各种api了，和js中的用法基本一致
// 例如
// join()
println(jsIntArray.join(";"))
// reverse()
println(jsIntArray.reverse())
// splice()
print(jsIntArray.splice(0, 2, 100, 1001))

// 各迭代相关的函数也可以使用，需要借助IteratorCallback对象。
// 以forEach()为例：
// 1、使用TypedIteratorCallback
jsIntArray.forEach(object : TypedIteratorCallback<Int?, Unit>{
    override fun call(currentValue: Int?, index: Int, total: Int?, arr: Any?) {
        println("jsIntArray[$index] = $currentValue")
    }
})
// 2、使用UnTypedIteratorCallback
jsIntArray.forEach(object : UnTypedIteratorCallback<Unit>{
    override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?) {
        println("jsIntArray[$index] = $currentValue")
    }
})
// 3、使用lambda函数
jsIntArray.forEach { index, value->
    println("jsIntArray[$index] = $value")
}

// 其他迭代相关的函数，如map()、filter()、every()等以此类推。又比如，reduce()、reduceRight()的基本使用如下（简单的数值累加的例子）：
jsIntArray.reduce(object : TypedIteratorCallback<Int?, Int>{
    override fun call(currentValue: Int?, index: Int, total: Int?, arr: Any?): Int {
        if(currentValue is Int)
            return currentValue + total!!
        return total!!
    }
})

// 除了Array对象的原生接口，还封装了Js中原生的for循环，即for(let i=start; i < stop; i = i + step){...}
// 可以设置初值、终值、步进（默认初值为0，终值为数组长度，步进为1），并且通过回调函数的返回值控制是否跳出循环
jsIntArray.forLoop(step = 2, callback = object : UnTypedIteratorCallback<Boolean>{
    override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
        if (currentValue == null)
            return false // break or not
        println("jsIntArray[$index]=$currentValue")
        return true
    }
})

// sort（排序），需要借助JsArraySortFunction，具体可以选择使用TypedSortFunction或者UnTypedSortFunction的其中一种
jsIntArray.sort(object : TypedSortFunction<Int?>{
    override fun compare(a: Int?, b: Int?): Boolean {
        if (a == null && b != null)
            return true
        if (a != null && b == null)
            return false
        if (a == null && b == null)
            return false
        return a!! >= b!!
    }
})
println("sorted:${jsIntArray}")
jsIntArray.sort(object : UnTypedSortFunction{
    override fun compare(a: Any?, b: Any?): Boolean {
        if(a !is Int || b !is Int)
            return false
        return a < b
    }
})
println("sorted: $jsIntArray")
```

**注意：** 以上代码仅仅演示API的基本使用方法，无法确保能够不经修改直接运行，实际可运行的例子在[**js-array-demo**](https://github.com/zimolab/js-array-demo) 仓库中可以找到。