package com.github.zimolab.jsarray

import com.github.zimolab.jsarray.base.JsArray
import netscape.javascript.JSObject

typealias JsAnyArray = JsArray<Any?>
typealias JsStringArray = JsArray<String?>
typealias JsBooleanArray = JsArray<Boolean?>
typealias JsIntArray = JsArray<Int?>
typealias JsDoubleArray = JsArray<Double?>
typealias JsObjectArray = JsArray<JSObject?>
typealias TypedSortComparator<T> = (a: T?, b:T?)->Boolean
typealias UntypedSortComparator = (ab: Pair<Any?, Any?>)->Boolean
typealias TypedCallback2<T, R> = (index: Int, value: T?)->R
typealias UntypedCallback2<R> = (indexValue: Pair<Int, Any?>)->R
typealias TypedCallback3<T, R> = (index: Int, value: T?, total: T?)->R
typealias UntypedCallback3<R> = (indexValueTotal: Triple<Int, Any?, Any?>)->R