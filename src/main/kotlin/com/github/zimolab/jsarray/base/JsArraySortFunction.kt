package com.github.zimolab.jsarray.base

interface JsArraySortFunction<in T> {
    fun compare(a: T, b: T): Boolean
}

interface TypedSortFunction<T>: JsArraySortFunction<T>
interface UnTypedSortFunction: JsArraySortFunction<Any?>