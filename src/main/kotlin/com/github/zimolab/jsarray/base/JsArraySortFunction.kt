package com.github.zimolab.jsarray.base

interface JsArraySortFunction<in T> {
    fun compare(a: T, b: T): Int
}

interface TypedSortFunction<in T>: JsArraySortFunction<T>
interface UnTypedSortFunction: JsArraySortFunction<Any?>