package com.github.zimolab.jsarray

import com.github.zimolab.jsarray.base.JsArray
import com.github.zimolab.jsarray.base.TypedIteratorCallback
import com.github.zimolab.jsarray.base.TypedSortFunction

typealias TypedCallback1<T> = (a: T?, b:T?)->Boolean
typealias TypedCallback2<T, R> = (index: Int, value: T?)->R
typealias TypedCallback3<T, R> = (index: Int, value: T?, total: R?)->R

inline fun <reified T> JsArray<T>.every(crossinline callback: TypedCallback2<T, Boolean>) {
    this.every(object : TypedIteratorCallback<T?, Boolean> {
        override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
            return callback(index, currentValue)
        }
    })
}

inline fun <reified T> JsArray<T>.some(crossinline callback: TypedCallback2<T, Boolean>) {
    this.some(object : TypedIteratorCallback<T?, Boolean> {
        override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
            return callback(index, currentValue)
        }
    })
}

inline fun <reified T> JsArray<T>.forEach(crossinline callback: TypedCallback2<T, Unit>) {
    this.forEach(object : TypedIteratorCallback<T?, Unit> {
        override fun call(currentValue: T?, index: Int, total: Unit?, arr: Any?) {
            callback(index, currentValue)
        }
    })
}

inline fun <reified T> JsArray<T>.find(crossinline callback: TypedCallback2<T, Boolean>): T? {
    return this.find(object : TypedIteratorCallback<T?, Boolean> {
        override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
            return callback(index, currentValue)
        }
    })
}

inline fun <reified T> JsArray<T>.findIndex(crossinline callback: TypedCallback2<T, Boolean>): Int {
    return this.findIndex(object : TypedIteratorCallback<T?, Boolean> {
        override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
            return callback(index, currentValue)
        }
    })
}

inline fun <reified T> JsArray<T>.forLoop(
    crossinline callback: TypedCallback2<T, Boolean>,
    startIndex: Int = 0,
    stopIndex: Int = -1,
    step: Int = 1
) {
    return this.forLoop(object : TypedIteratorCallback<T?, Boolean> {
        override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
            return callback(index, currentValue)
        }
    }, startIndex, stopIndex, step)
}

inline fun <reified T> JsArray<T>.filter(crossinline callback: TypedCallback2<T, Boolean>): JsArray<T> {
    return this.filter(object : TypedIteratorCallback<T?, Boolean> {
        override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
            return callback(index, currentValue)
        }
    })
}

inline fun <reified T> JsArray<T>.map(crossinline callback: TypedCallback2<T, T>): JsArray<T> {
    return this.map(object : TypedIteratorCallback<T?, T?> {
        override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
            return callback(index, currentValue)
        }
    })
}

inline fun <reified T, R> JsArray<T>.reduce(crossinline callback: TypedCallback3<T, R>): R? {
    return this.reduce(object : TypedIteratorCallback<T?, R?> {
        override fun call(currentValue: T?, index: Int, total: R?, arr: Any?): R? {
            return callback(index, currentValue, total)
        }
    })
}

inline fun <reified T, R> JsArray<T>.reduceRight(crossinline callback: TypedCallback3<T, R>): R? {
    return this.reduceRight(object : TypedIteratorCallback<T?, R?> {
        override fun call(currentValue: T?, index: Int, total: R?, arr: Any?): R? {
            return callback(index, currentValue, total)
        }
    })
}

inline fun <reified T> JsArray<T>.sort(crossinline callback: TypedCallback1<T>): JsArray<T> {
    return this.sort(object : TypedSortFunction<T?>{
        override fun compare(a: T?, b: T?): Boolean {
            return callback(a, b)
        }
    })
}



//UnTypedIteratorCallback系列API
//inline fun <reified T> JsArray<T>.untypedForEach(crossinline callback: UnTypedCallback2<Unit>) {
//    this.forEach(object : UnTypedIteratorCallback<Unit> {
//        override fun call(currentValue: Any?, index: Int, total: Unit?, arr: Any?) {
//            callback(index, currentValue)
//        }
//    })
//}
//
//inline fun <reified T> JsArray<T>.untypedSome(crossinline callback: UnTypedCallback2<Boolean>) {
//    this.some(object : UnTypedIteratorCallback<Boolean> {
//        override fun call(currentValue: Any?, index: Int, total: Boolean?, arr: Any?): Boolean {
//            return callback(index, currentValue)
//        }
//    })
//}
//
//inline fun <reified T> JsArray<T>.untypedEvery(crossinline callback: UnTypedCallback2<Boolean>) {
//    this.every(object : UnTypedIteratorCallback<Boolean> {
//        override fun call(currentValue: Any?, index: Int, total: Boolean?, arr: Any?): Boolean {
//            return callback(index, currentValue)
//        }
//    })
//}