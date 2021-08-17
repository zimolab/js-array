package com.github.zimolab.jsarray.base

interface JsArrayIteratorCallback<in T, out R> {
    fun call(currentValue: T, index: Int, total: T?, arr: Any?): R
}

interface TypedIteratorCallback<in T, out R> : JsArrayIteratorCallback<T, R>

interface UntypedIteratorCallback<out R> : JsArrayIteratorCallback<Any?, R> {
    override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): R
}