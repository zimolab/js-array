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
    fun getAny(index: Int): Any?
    fun concat(other: JsArrayInterface<T>): JsArrayInterface<T>
    fun concatAny(other: JsArrayInterface<T>): JsArrayInterface<Any?>
    fun join(separator: String = ","): String
    fun reverse(): JsArrayInterface<T>
    fun pop(): T?
    fun popAny(): Any?
    fun push(vararg elements: T?): Int
    fun shift(): T?
    fun shiftAny(): Any?
    fun unshift(vararg elements: T?): Int
    fun slice(start: Int, end: Int? = null): JsArrayInterface<T>
    fun sliceAny(start: Int, end: Int? = null): JsArrayInterface<Any?>
    fun splice(index: Int, count: Int, vararg items: T?): JsArrayInterface<T>
    fun spliceAny(index: Int, count: Int, vararg items: T?): JsArrayInterface<Any?>
    fun fill(value: T?, start: Int = 0, end: Int? = null): JsArrayInterface<T>
    fun find(callback: JsArrayIteratorCallback<T?, Boolean>): T?
    fun findAny(callback: UnTypedIteratorCallback<Boolean>): Any?
    fun findIndex(callback: JsArrayIteratorCallback<T?, Boolean>): Int
    fun includes(element: T?, start: Int = 0): Boolean
    fun indexOf(element: T?, start: Int = 0): Int
    fun lastIndexOf(element: T?, start: Int = -1): Int
    fun forLoop(callback: JsArrayIteratorCallback<T?, Boolean>, startIndex: Int = 0, stopIndex: Int = -1, step: Int = 1)
    fun forEach(callback: JsArrayIteratorCallback<T?, Unit>)
    fun filter(callback: JsArrayIteratorCallback<T?, Boolean>): JsArrayInterface<T>
    fun filterAny(callback: UnTypedIteratorCallback<Boolean>): JsArrayInterface<Any?>
    fun map(callback: JsArrayIteratorCallback<T?, T?>): JsArrayInterface<T>
    fun mapAny(callback: JsArrayIteratorCallback<Any?, Any?>): JsArrayInterface<Any?>
    fun every(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean
    fun some(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean
    fun reduce(initialValue: T?, callback: JsArrayIteratorCallback<T?, T?>): T?
    fun reduce(callback: JsArrayIteratorCallback<T?, T?>): T?
    fun reduceRight(initialValue: T?, callback: JsArrayIteratorCallback<T?, T?>): T?
    fun reduceRight(callback: JsArrayIteratorCallback<T?, T?>): T?
    fun sort(sortFunction: JsArraySortFunction<T?>? = null): JsArrayInterface<T>
}