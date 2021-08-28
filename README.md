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
### 	1、导入依赖

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



### 2、创建（获取）对象（以JsIntArray为例）

```html
<script>
    var int_array = [1,2,3,5,6,7,9]
</script>
```

```kotlin
val rawObject = engine.executeScript("int_array")
val intJsArray = JsArray.intArrayOf(rawObject as JSObject)
```

### 3、调用接口

```kotlin
intJsArray.toString()
```
```
Output
[1,2,3,5,6,7,9]
```
---

```kotlin
intJsArray.join(";")
```
```
Output
1;2;3;5;6;7;9
```
---

```kotlin
val intJsArray2 = JsArray.newJsIntArray(rawObject, 10)
intJsArray2?.fill(10)
it.println("intArray2: $intJsArray2")
intJsArray2?.concat(intJsArray)
```
```
Output
intArray2: [10,10,10,10,10,10,10,10,10,10]
[10,10,10,10,10,10,10,10,10,10,1,2,3,5,6,7,9]
```
---

```kotlin
val a = intJsArray.reverse()
print("a:$a")
print("intJsArray: $intJsArray")
print("a==intJsArray: ${a == intJsArray}")
```
```
Output
a:[9,7,6,5,3,2,1]
intJsArray: [9,7,6,5,3,2,1]
a==intJsArray: true
```
---

```kotlin
intJsArray.pop()
print("intJsArray: $intJsArray")
```
```
Output
intJsArray: [9,7,6,5,3,2]
```
---

```kotlin
intJsArray.push(-1, -2, -3)
println("intJsArray: $intJsArray")
```
```
Output
intJsArray: [9,7,6,5,3,2,-1,-2,-3]
```
---

```kotlin
intJsArray.shift()
print("intJsArray: $intJsArray")
```
```
Output
intJsArray: [7,6,5,3,2,-1,-2,-3]
```
---

```kotlin
val data = (0..10).map { (random() * 10).toInt() }
print("data: $data")
intJsArray.unshift(*data.toTypedArray())
print("intJsArray: $intJsArray")
```
```
Output
data: [8, 0, 4, 9, 2, 3, 5, 4, 8, 3, 6]
intJsArray: [8,0,4,9,2,3,5,4,8,3,6,7,6,5,3,2,-1,-2,-3]
```
---

```kotlin
val ret = intJsArray.slice(2)
print("ret: $ret")
println("intJsArray: $intJsArray")
```
```
Output
ret: [4,9,2,3,5,4,8,3,6,7,6,5,3,2,-1,-2,-3]
intJsArray: [8,0,4,9,2,3,5,4,8,3,6,7,6,5,3,2,-1,-2,-3]
```
---

```kotlin
val ret = intJsArray.splice(2, 5)
print("ret: $ret")
print("intJsArray: $intJsArray")
```
```
Output
ret: [4,9,2,3,5]
intJsArray: [8,0,4,8,3,6,7,6,5,3,2,-1,-2,-3]
```
---

```kotlin
print("intJsArray: $intJsArray")
intJsArray.includes(0)
```
```
Output
intJsArray: [8,0,4,8,3,6,7,6,5,3,2,-1,-2,-3]
true
```
---


**注意：** 以上代码代码仅仅演示API的基本使用方法，实际可运行的完整例子在[**js-array-demo**](https://github.com/zimolab/js-array-demo) 仓库中可以找到。

