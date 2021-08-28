## 项目说明 [![](https://jitpack.io/v/zimolab/js-array.svg)](https://jitpack.io/#zimolab/js-array)
​	一个将JavaScript中的Array对象映射为Kotlin（java）对象的库。该项目基于netscape.javascript.JSObject对象，映射了Javascript Array对象的大部分接口，适用于使用WebEngine与底层Javascript代码进行交互的情景。

## API

**JsArrayInterface接口**

```kotlin
interface JsArrayInterface<T> {
    companion object {
        private const val CHECK_IS_JS_ARRAY = "Array.isArray(%s)"

        fun isJsArray(jsObject: JSObject): Boolean {
            return jsObject.eval(CHECK_IS_JS_ARRAY.format("this")) == true
        }

        fun isJsArray(obj: Any): Boolean {
            return if (obj is JSObject) {
                isJsArray(jsObject = obj)
            } else {
                false
            }
        }

        fun isArray(jsObject: JSObject) = isJsArray(jsObject = jsObject)
        fun isArray(obj: Any) = isJsArray(obj = obj)
    }

    val length: Int
    val reference: JSObject

    operator fun set(index: Int, value: T?)
    operator fun get(index: Int): T?
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
    fun find(callback: JsArrayIteratorCallback<T?, Boolean>): T?
    fun findIndex(callback: JsArrayIteratorCallback<T?, Boolean>): Int
    fun includes(element: T, start: Int = 0): Boolean
    fun indexOf(element: T, start: Int = 0): Int
    fun lastIndexOf(element: T, start: Int = -1): Int
    fun forLoop(callback: JsArrayIteratorCallback<T?, Boolean>, startIndex: Int = 0, stopIndex: Int = -1, step: Int = 1)
    fun forEach(callback: JsArrayIteratorCallback<T?, Unit>)
    fun filter(callback: JsArrayIteratorCallback<T?, Boolean>): JsArrayInterface<T>
    fun map(callback: JsArrayIteratorCallback<T?, T?>): JsArrayInterface<T>
    fun every(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean
    fun some(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean
    fun reduce(callback: JsArrayIteratorCallback<T?, T?>): T?
    fun reduceRight(callback: JsArrayIteratorCallback<T?, T?>): T?
    fun sort(sortFunction: JsArraySortFunction<T?>? = null): JsArrayInterface<T>
    fun toJsAnyArray(): JsArrayInterface<Any?>

    // Any版的API
    // Any版的API旨在减少由于类型转换而可能引发的异常。这些异常一般发生在以下两个场景中：
    // 1、从js回调java函数时，如果js传递的参数不能由WebEngine自动映射为java回调函数所声明的参数类型时
    // 2、部分函数的返回值由泛型参数决定，因此需要编写诸如“return result as T”之类的代码，"as T"语句调用失败时
    fun reduceRightAny(callback: UntypedIteratorCallback<Any?>): Any?
    fun reduceAny(callback: UntypedIteratorCallback<Any?>): Any?
    fun mapAny(callback: UntypedIteratorCallback<Any?>): JsArrayInterface<Any?>
    fun filterAny(callback: UntypedIteratorCallback<Boolean>): JsArrayInterface<Any?>
    fun findAny(callback: UntypedIteratorCallback<Boolean>): Any?
    fun shiftAny(): Any?
    fun popAny(): Any?
    fun concatAny(other: JsArrayInterface<T>): JsArrayInterface<Any?>
    fun getAny(index: Int): Any?
    fun includesAny(element: Any?, start: Int=0): Boolean
    fun indexOfAny(element: Any?, start: Int=0): Int
    fun lastIndexOfAny(element: Any?, start: Int=-1): Int
}
```

## 实现类型
### 	JsArray类

​	**JsArray类实现了JsArrayInterface接口，并实现了一些开箱可用的常见类型的Js数组。**

​	之所以要预先定义这些类型的数组，一方面是因为这些类型基本涵盖了多数使用需求和场景，另一方面则是因为这些类型可以自动在Js与Java之间进行转换。关于这一点，可以参考JavaFx WebView文档中关于Java与JS之间类型映射的部分（[可以点这里](https://openjfx.io/javadoc/11/javafx.web/javafx/scene/web/WebEngine.html) ）。下面摘录了部分重要说明。

> #### Mapping JavaScript values to Java objects
> ​	JavaScript values are represented using the obvious Java classes: **null becomes Java null**; a **boolean becomes a java.lang.Boolean**; and a **string becomes a java.lang.String**. A **number** can be **java.lang.Double** or a **java.lang.Integer**, depending. The **undefined** value maps to a **specific unique String object whose value is "undefined"**.
>
> ​	If the result is a **JavaScript object**, it is wrapped as an instance of the **JSObject** class. (As a special case, if the JavaScript object is a JavaRuntimeObject as discussed in the next section, then the original Java object is extracted instead.) 
>
> ​	The JSObject class is a proxy that provides access to methods and properties of its underlying JavaScript object. The most commonly used JSObject methods are getMember (to read a named property), setMember (to set or define a property), and call (to call a function-valued property).

> #### Mapping Java objects to JavaScript values
> ​	The arguments of the JSObject methods setMember and call pass Java objects to the JavaScript environment. 
> This is **roughly the inverse of the JavaScript-to-Java mapping described above**:**Java String, Number, or Boolean objects are converted to the obvious JavaScript values**.**A JSObject object is converted to the original wrapped JavaScript object**.Otherwise a JavaRuntimeObject is created. This is a JavaScript object that acts as a proxy for the Java object, in that accessing properties of the JavaRuntimeObject causes the Java field or method with the same name to be accessed.

|  Javascript类型   | Java类型        |
|  :--------------:| :------------: |
| string           | String         |
| bool            | Boolean         |
| number          | Integer或Double |
| undefine        | "undefined"    |
| 其他Object       | JSObject      |

#### 一些开箱即用的类型

##### JsStringArray (JsArray<String?>)
​	字符串数组

##### JsIntArray
​	整数型数组

##### JsDoubleArray
​	双精度浮点数数组

##### JsBooleanArray
​	布尔型数组

##### JSObjectArray
​	Javascript对象数组（即netscape.javascript.JSObject对象数组）

##### JsAnyArray
​	任意型数组，包括字符串、整数、浮点数、boolean以及JavaScript中的任意对象（需要被映射为JSObject对象）

### 自定义类型的JS数组

​	除了上述内置的几种类型，还可以自行实现JsArrayInterface<T>接口，以实现自定义类型的JS数组。具体实现可以参考JsArray类代码。



## 快速入门
### 一般使用

#### 	1、导入依赖

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


#### 2、创建（获取）对象（以JsIntArray为例）

```html
<script>
    var int_array = [1,2,3,5,6,7,9]
</script>
```



```kotlin
val rawObject = engine.executeScript("int_array")
val intJsArray = JsArray.intArrayOf(rawObject as JSObject)
```

#### 3、调用接口

```
print(intJsArray[0])
intJsArray[0] = 1
```

```kotlin
intJsArray.toString()
```
> **Output**
> [1,2,3,5,6,7,9]


```kotlin
intJsArray.join(";")
```
> **Output**
> 1;2;3;5;6;7;9

```kotlin
val intJsArray2 = JsArray.newJsIntArray(rawObject, 10)
intJsArray2?.fill(10)
it.println("intArray2: $intJsArray2")
intJsArray2?.concat(intJsArray)
```
> **Output**
> intArray2: [10,10,10,10,10,10,10,10,10,10]
> [10,10,10,10,10,10,10,10,10,10,1,2,3,5,6,7,9]

```kotlin
val a = intJsArray.reverse()
print("a:$a")
print("intJsArray: $intJsArray")
print("a==intJsArray: ${a == intJsArray}")
```

> **Output**
> a:[9,7,6,5,3,2,1]
> intJsArray: [9,7,6,5,3,2,1]
> a==intJsArray: true

```kotlin
intJsArray.pop()
print("intJsArray: $intJsArray")
```
> **Output**
> intJsArray: [9,7,6,5,3,2]


```kotlin
intJsArray.push(-1, -2, -3)
println("intJsArray: $intJsArray")
```
> **Output**
> intJsArray: [9,7,6,5,3,2,-1,-2,-3]

```kotlin
intJsArray.shift()
print("intJsArray: $intJsArray")
```
> **Output**
> intJsArray: [7,6,5,3,2,-1,-2,-3]

```kotlin
val data = (0..10).map { (random() * 10).toInt() }
print("data: $data")
intJsArray.unshift(*data.toTypedArray())
print("intJsArray: $intJsArray")
```
> **Output**
> data: [8, 0, 4, 9, 2, 3, 5, 4, 8, 3, 6]
> intJsArray: [8,0,4,9,2,3,5,4,8,3,6,7,6,5,3,2,-1,-2,-3]

```kotlin
val ret = intJsArray.slice(2)
print("ret: $ret")
println("intJsArray: $intJsArray")
```
> **Output**
> ret: [4,9,2,3,5,4,8,3,6,7,6,5,3,2,-1,-2,-3]
> intJsArray: [8,0,4,9,2,3,5,4,8,3,6,7,6,5,3,2,-1,-2,-3]

```kotlin
val ret = intJsArray.splice(2, 5)
print("ret: $ret")
print("intJsArray: $intJsArray")
```
> **Output**
> ret: [4,9,2,3,5]
> intJsArray: [8,0,4,8,3,6,7,6,5,3,2,-1,-2,-3]

```kotlin
print("intJsArray: $intJsArray")
intJsArray.includes(0)
```
> **Output**
> intJsArray: [8,0,4,8,3,6,7,6,5,3,2,-1,-2,-3]
> true



  对于大多数迭代相关的函数，都可以使用JsArrayIteratorCallback<T, R>或者是lambda函数作为回调，以forEach()为例：

​    1）使用JsArrayIteratorCallback<T, R>

```kotlin
jsArray.forEach(object : TypedIteratorCallback<Int?, Unit>{
    override fun call(currentValue: Int?, index: Int, total: Int?, arr: Any?) {
        print("index: $index, value: $currentValue")
    }
})
```

> **Output**
> index: 0, value: 1
> index: 1, value: 2
> index: 2, value: 3
> index: 3, value: 5
> index: 4, value: 6
> index: 5, value: 7
> index: 6, value: 9



  2）使用更为简洁的lambda函数

```kotlin
jsArray.forEach { index, value ->
    print("index: $index, value: $value")
}
```
> **Output**
> index: 0, value: 1
> index: 1, value: 2
> index: 2, value: 3
> index: 3, value: 5
> index: 4, value: 6
> index: 5, value: 7
> index: 6, value: 9



### 多类型元素数组的处理

  上面演示的代码都是类型受限的情形。这里所说的类型受限，是指我们提前能够明确（限定）数组的类型，也即是数组中元素的类型。

  而JS数组与Java(Kotlin)数组很重要的一个区别在于，kotlin的数组只能容纳同一类型的元素；而在JS中，数组可以容纳不同类型的对象。虽然一个数组只存放一种类型的数据是一种很好的编程实践，但我们不能假设这是一种永远得到遵守的约定，因此，我们不得不处理多类型元素数组的情形。这里面大致又能分为两种情形：

  其一，我们能够大致能够确定JS数组中元素的类型，比如说我们知道数组中的元素"大多数时候"、"通常情况下"都是某种类型，仅仅在"偶尔的情形下"可能会出现其他类型的数据；或者是，可以确信数组中绝大多数数据都是某一类型的，只有少数数据可能为其他类型。

  其二，数组中元素的类型完全是不受限定的，也就是说，对于该数组，从一开始就没有关于其元素类型的任何假定。或者该数组从一开始就是为存储任意类型数据而创建的。

  对于以上两种情况，应对的策略有所不同。

  对于第二种情形，最佳的策略是使用JsArray<Any?>类型的数组，从而将类型判别的逻辑推迟到业务代码中。就像下面代码演示的那样：

```html
<script>
var multi_types_array = [1,2,3, "a", "b", "c", null, undefined, new Date(), new Object(), 12.3, true, false]
</script>
```

```kotlin
val rawArray = engine.execute("multi_types_array") as JSObject
val anyJsArray = JsArray.anyArrayOf(rawArray)
anyJsArray.forEach(object : JsArrayIteratorCallback<Any?, Unit>{
    override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?){
        when(currentValue) {
            null-> {
                // 处理null元素
            }
            is Int -> {
                // 处理Int元素
            }
            is Double -> {
                // 处理Double元素
            }
            is String -> {
                // 处理String元素
            }
            is JSObject -> {
                // 处理JSObject对象
            }
            else -> {
                // 其他的情况
            }
        }
    }
})
```

  下面这个示例演示如何打印数组中所有元素及其类型：

```kotlin
anyJsArray.forEach { index, value ->
	print("""index: $index, value: $value  (type: ${value?.let { it::class}})""")
}
```

>  **Output**
>
> index: 0, value: 1 (type: class kotlin.Int)
>
> index: 1, value: 2 (type: class kotlin.Int)
>
> index: 2, value: 3 (type: class kotlin.Int)
>
> index: 3, value: a (type: class kotlin.String)
>
> index: 4, value: b (type: class kotlin.String)
>
> index: 5, value: c (type: class kotlin.String)
>
> index: 6, value: null (type: null)
>
> index: 7, value: null (type: null)
>
> index: 8, value: Sat Aug 28 2021 23:13:24 GMT+0800 (中国标准时间) (type: class com.sun.webkit.dom.JSObject)
>
> index: 9, value: [object Object] (type: class com.sun.webkit.dom.JSObject)
>
> index: 10, value: 12.3 (type: class kotlin.Double)
>
> index: 11, value: true (type: class kotlin.Boolean)
>
> index: 12, value: false (type: class kotlin.Boolean)

 对于第一种情形，虽然也可以使用JsArray<Any?>将类型做模糊化处理。但存在另外一种可行的方案：依旧使用类型受限的数组对象。不过，需要留意那些能够引发类型转换异常的地方，注意api的选择和使用。

  比如下面的代码就将引发一个异常：

```kotlin
// multi_types_array: [1,2,3, "a", "b", "c", null, undefined, new Date(), new Object(), 12.3, true, false]
val rawArray = engine.execute("multi_types_array") as JSObject
val intJsArray = JsArray.intArrayOf(rawArray)
//由于index=3的元素是字符串"a",而我们这里声明的是一个JsArray<Int?>，所以，intJsArray[3]需要返回一个Int?类型的数据
// 由此就引发了类型转换异常
val a = intJsArray[3]
```

> **异常**
>
> java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')

  针对这种情况，JsArray中特别设计了一些**Any版本的接口（以Any为尾缀的函数）**，当我们拿不准某个元素是否为其他类型时，使用这些接口可以很大程度避免上述异常。不难看出，这些Any版的接口，事实上就是将JsArray<Any?>中的部分逻辑推广到类型限定的情形下。

  上述例子可以改为使用getAny()接口：

```kotlin
val a = intJsArray.getAny(3)
```

  又比如，下面这种情况也将引发同样的异常：

```kotlin
intJsArray.forEach { index, value ->
    print("index: $index, value: $value")
}
```

   但是我们发现，并没有一个名为forEachAny()的方法。这是因为在设计迭代回调器时已经将这种情况做了处理，无需增加另外的接口。这时我们可以采用以下的方法：

```kotlin
intJsArray.forEach(object : JsArrayIteratorCallback<Int?, Unit>{
    override fun call(currentValue: Int?, index: Int, total: Int?, arr: Any?) {
        print("index: $index, value: $currentValue")
    }
})
```

   需要注意的是，如果传入JsArrayIteratorCallback<Int?, Unit>作类型的allback，那么数组中所有的非Int?类型的元素都将被视为null，上述代码的输出如下：

> **Output**
>
> index: 0, value: 1
>
> index: 1, value: 2
>
> index: 2, value: 3
>
> index: 3, value: null
>
> index: 4, value: null
>
> index: 5, value: null
>
> index: 6, value: null
>
> index: 7, value: null
>
> index: 8, value: null
>
> index: 9, value: null
>
> index: 10, value: null
>
> index: 11, value: null
>
> index: 12, value: null

  显然，这种做法适用于仅对数组中的某一类型元素感兴趣的场景。如果需要对数组中的所有元素进行处理(即要到达类似JsArray<Any?>.forEach()的效果)时，则需要传入JsArrayIteratorCallback<Any?, Unit>类型的callback：

```kotlin
intJsArray.forEach(object : JsArrayIteratorCallback<Int?, Unit>{
    override fun call(currentValue: Int?, index: Int, total: Int?, arr: Any?) {
        print("index: $index, value: $currentValue")
    }
})
```
  也可以采用更加简化的写法：
  ```kotlin
  intJsArray.forEach {(index, value)->
      print("index: $index, value: $value, ${value?.let { it::class }}")
  }
  ```

> **Output**
>
> index: 0, value: 1, class kotlin.Int
>
> index: 1, value: 2, class kotlin.Int
>
> index: 2, value: 3, class kotlin.Int
>
> index: 3, value: a, class kotlin.String
>
> index: 4, value: b, class kotlin.String
>
> index: 5, value: c, class kotlin.String
>
> index: 6, value: null, null
>
> index: 7, value: null, null
>
> index: 8, value: Sun Aug 29 2021 00:19:29 GMT+0800 (中国标准时间), class com.sun.webkit.dom.JSObject
>
> index: 9, value: [object Object], class com.sun.webkit.dom.JSObject
>
> index: 10, value: 12.3, class kotlin.Double
>
> index: 11, value: true, class kotlin.Boolean
>
> index: 12, value: false, class kotlin.Boolean

  除了，上面演示的forEach()接口，其他接受JsArrayIteratorCallback<T, R>类型callback的接口大多数都实现了类似的逻辑，或者是提供了Any版本的接口，可以按照实际情况进行选择。

**注意：** 以上代码代码仅仅演示API的基本使用方法，实际可运行的完整例子在[**js-array-demo**](https://github.com/zimolab/js-array-demo) 仓库中可以找到。

