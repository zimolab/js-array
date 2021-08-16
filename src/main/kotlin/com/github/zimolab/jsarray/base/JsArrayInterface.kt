package com.github.zimolab.jsarray.base

import netscape.javascript.JSObject

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
    fun includes(element: T?, start: Int = 0): Boolean
    fun indexOf(element: T?, start: Int = 0): Int
    fun lastIndexOf(element: T?, start: Int = -1): Int
    fun forLoop(callback: JsArrayIteratorCallback<T?, Boolean>, startIndex: Int = 0, stopIndex: Int = -1, step: Int = 1)
    fun forEach(callback: JsArrayIteratorCallback<T?, Unit>)
    fun filter(callback: JsArrayIteratorCallback<T?, Boolean>): JsArrayInterface<T>
    fun map(callback: JsArrayIteratorCallback<T?, T?>): JsArrayInterface<T>
    fun every(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean
    fun some(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean
    fun reduce(initialValue: T?, callback: JsArrayIteratorCallback<T?, T?>): T?
    fun reduce(callback: JsArrayIteratorCallback<T?, T?>): T?
    fun reduceRight(initialValue: T?, callback: JsArrayIteratorCallback<T?, T?>): T?
    fun reduceRight(callback: JsArrayIteratorCallback<T?, T?>): T?
    fun sort(sortFunction: JsArraySortFunction<T?>? = null): JsArrayInterface<T>
    fun toJsAnyArray(): JsArrayInterface<Any?>

    // Any版的API
    // Any版的API旨在减少由于类型转换而可能引发的异常。这些异常一般发生在以下两个场景中：
    // 1、从js回调java函数时，如果js传递的参数不能由WebEngine自动映射为java回调函数所声明的参数类型则引发异常
    // 2、"xxx as T"语句调用失败时，有一部分函数的返回值为T?，因此可能需要编写诸如”return result as T“，如果不能转换则会引发异常
    fun reduceRightAny(callback: UnTypedIteratorCallback<Any?>): Any?
    fun reduceRightAny(initialValue: Any?, callback: UnTypedIteratorCallback<Any?>): Any?
    fun reduceAny(callback: UnTypedIteratorCallback<Any?>): Any?
    fun reduceAny(initialValue: Any?, callback: JsArrayIteratorCallback<Any?, Any?>): Any?
    fun mapAny(callback: UnTypedIteratorCallback<Any?>): JsArrayInterface<Any?>
    fun filterAny(callback: UnTypedIteratorCallback<Boolean>): JsArrayInterface<Any?>
    fun findAny(callback: UnTypedIteratorCallback<Boolean>): Any?
    fun shiftAny(): Any?
    fun popAny(): Any?
    fun concatAny(other: JsArrayInterface<T>): JsArrayInterface<Any?>
    fun getAny(index: Int): Any?
    fun includesAny(element: Any?, start: Int=0): Boolean
    fun indexOfAny(element: Any?, start: Int=0): Int
    fun lastIndexOfAny(element: Any?, start: Int=0): Int

}