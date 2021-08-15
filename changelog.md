# Changelog
##  v0.1.0-SNAPSHOT（2021/8/12）
### Bug Fixes
无
### Features
- 第一个可用的测试版本。
- 实现了对Javascript Array对象多数接口的映射
- 确定了API的基本框架，但API尚未完全稳定，后续可能发生变化
- 后续主要工作是在此版本代码基础上：
  - 发现并解决Bug
  - 优化实现，增强稳定性
  - 固化API
  - 增加更多开箱即用的类型
- 目前可用的Javascript Array接口：
  - isArray()
  - length 
  - concat()
  - every()
  - fill()
  - filter()
  - find()
  - findIndex()
  - forEach()
  - from()
  - includes()
  - indexOf()
  - join()
  - map()
  - pop()
  - push()
  - reduce()
  - reduceRight()
  - reverse()
  - shift()
  - slice()
  - some()
  - sort()
  - splice()
  - toString()
  - unshift()

- 目前开箱即用的类型：
  - JsStringArray
  - JsBooleanArray
  - JsIntArray
  - JsDoubleArray
  - JsObjectArray
  - JsAnyArray
### Breaking Changes
无

##  v0.1.1-SNAPSHOT（2021/8/14）
### Bug Fixes
- 解决了一个潜在的类型转换异常:
  
> 说明：由于Array中某些方法（例如find()、findIndex()等）方法会遍历数组中的所有值，包括undefined, 而在从js到java的类型映射中，
> WebEngine内在的类型映射机制会将Js中的undefined值映射成Java中一个值为"undefined"的字符串。
>
> 这样就带来一个问题，如果我们完全依赖内在的类型映射机制，那么就意味着我们在java层面无法分辨一个来自js的值为"undefined"实际到底指代的是什么，换言之，这就造成了潜在的歧义性。
> 这种歧义性的消除当然可以依赖我们与js代码之间的约定，比如可以规定，在js代码中不允许将一个undefined值传递到java中，然而这种约定很难是强制性的，它很可能由于编码上的不谨慎
> 从而在某个意想不到的的场合被打破。
> 
> 除了歧义性，一个更加实际的问题在于，一个代表undefined的字符串很可能会被当初其他类型的值作为函数参数进行传递，这时就有可能引发类型错误，考虑以下情形。
> 假设在js中我们有这样一个数组**var raw = [0, 1, 2, 3, null, undefined, 6]**，现在我们需要在java中遍历它，依次对其中的值进行一些计算，那么可以使用如下方法：
> 
>>val raw = webEngine.executeScript("raw")
>>
>>val jsArray =  JsArray.intArrayOf(raw)
>> 
>>jsArray.forEach { index, value ->
>>
>> if(value != null)  doSomeCalculationWith(value) // 对每一个元素做一些计算工作
>>
>> }
>
> 在上面这个例子中，value参数的类型为Int?，在处理raw数组的前五个元素时，一切都进行的很顺利，因为这些元素或者是整数或者是null；然而，当处理到第六个元素时，异常就发生了。第六个元素的值为undefined，在java中，WebEngine的类型映射机制
> 会将其映射为一个“undefined”字符串，而value参数要求一个Int?值，因此系统会尝试将“undefined”字符串强制转换为Int?，这种类型转换当然是要失败的，因此最终的结果就是引发了一个类型转换异常。
> 
> 要解决这个问题实际上并不困难，不难看出，这个问题的根源在于js与java的类型系统的不兼容性——二者的类型并非一一对应的，具体而言，java中没有一个在语义上与js undefined严格对应的值或者类型，WebEngine的解决方案是将undefined转换为对应的字符串，这显然是一种折衷的做法。
> 而在实际中，undefined的语义常常与null解决（尤其是当我们从java的视角来考虑问题时），因此，一个同样折衷的做法是，在js层面将可能传递到java的undefined的值预先就转换为null。
> 在实际的代码中，我们定义了一个undefined2Null()函数来生成相关的代码片段。

### Features
- 增加了以下静态接口，更加方便使用
  - JsArrayInterface<T>.isJsArray(obj: Any)
  - JsArray<T>.newJsStringArray(env: JSObject, initialSize: Int = 0)
  - JsArray<T>.newJsIntArray(env: JSObject, initialSize: Int = 0)
  - JsArray<T>.newJsDoubleArray(env: JSObject, initialSize: Int = 0)
  - JsArray<T>.newBooleanArray(env: JSObject, initialSize: Int = 0)
  - JsArray<T>.newJSObjectArray(env: JSObject, initialSize: Int = 0)
  - JsArray<T>.newJSObjectArray(env: JSObject, initialSize: Int = 0)
  - JsArray<T>.newJsAnyArray(env: JSObject, initialSize: Int = 0)


- 为JsArray中迭代相关的接口添加了扩展API，现在我们可以使用kotlin为lambda函数提供语法糖来写回调函数，而无需创建匿名对象了，包括：
  - JsArray<T>.find()
  - JsArray<T>.findIndex()
  - JsArray<T>.forLoop()
  - JsArray<T>.forEach()
  - JsArray<T>.filter()
  - JsArray<T>.map()
  - JsArray<T>.every()
  - JsArray<T>.some()
  - JsArray<T>.reduce()
  - JsArray<T>.reduceRight()
  - JsArray<T>.sort()
```kotlin
// 使用匿名对象的方式：
jsArray.forEach(object : TypedIteratorCallback<Int?, Unit> {
  override fun call(currentValue: Int?, index: Int, total: Int?, arr: Any?) {
    println("jsArray[$index]=$currentValue")
  }
})

// 现在可以这样简写了
jsArray.forEach{ index, value->
  println("jsArray[$index]=$value")
}
```
- 重载了reduce()和reduceRight()，现在可以设定初始值了:
  - JSArray<T>.reduce(initialValue: T?, callback: ...)
  - JSArray<T>.reduceRight(initialValue: T?, callback: ...)

### Breaking Changes
- 更改了JsArrayIteratorCallback接口中call函数的签名，将其total参数的类型由R?转变为T?
- 更改了TypedCallback3接口中的函数签名，同样将total参数的类型由R?转变为T?
- 去除了reduce()与reduceRight()函数类型参数R，现在这两个函数的返回值类型将与数组类型一致（即为T？）
- 更改了由js到java的类型映射逻辑，统一将传递给java的undefined值转换为null
- 由于java中没有undefined的等价物，因此对于一些函数，undefined和null被认为具有相同的逻辑含义，比如：
  - (java) include(null) <=> (js) includes(null) && indexOf(null) ||  includes(undefined) 
  - (java) indexOf(null) <=> (js) {let __tmp=indexOf(null);__tmp!=-1? __tmp : indexOf(undefined)}
  - (java) lastIndexOf(null) <=> (js) {let __tmp=lastIndexOf(null);__tmp!=-1? __tmp : lastIndexOf(undefined)}

##  v0.1.2-SNAPSHOT（2021/8/15）
### Bug Fixes
- 解决了一个小bug，将findIndex()函数参数泛型类型中忘记添加的?补上了

> 之前：fun findIndex(callback: JsArrayIteratorCallback<T, Boolean>): Int
> 之后：fun findIndex(callback: JsArrayIteratorCallback<T?, Boolean>): Int


### Features
- 无

### Breaking Changes
- findIndex()函数参数类型由**JsArrayIteratorCallback<T, Boolean>** 改为**JsArrayIteratorCallback<T?, Boolean>**, 这是编码上的一处遗漏，现在已解决。