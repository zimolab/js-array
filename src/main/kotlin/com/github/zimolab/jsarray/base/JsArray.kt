package com.github.zimolab.jsarray.base

import com.github.zimolab.jsarray.*
import com.github.zimolab.jsarray.base.JsAPIs.UNDEFINED
import javafx.scene.web.WebEngine
import netscape.javascript.JSObject

@Suppress("UNCHECKED_CAST")
class JsArray<T>
private constructor(
    override val reference: JSObject,
    private val undefineAsNull: Boolean = true) : JsArrayInterface<T> {

    init {
        if (!JsArrayInterface.isJsArray(reference)) {
            throw IllegalArgumentException("the reference is point to an javascript Array object.")
        }
    }

    companion object {
        fun stringArrayOf(reference: JSObject): JsStringArray {
            return JsArray(reference, false)
        }

        fun intArrayOf(reference: JSObject): JsIntArray {
            return JsArray(reference, true)
        }

        fun doubleArrayOf(reference: JSObject): JsDoubleArray {
            return JsArray(reference, true)
        }

        fun jsObjectArrayOf(reference: JSObject): JsObjectArray {
            return JsArray(reference, true)
        }

        fun booleanArrayOf(reference: JSObject): JsBooleanArray {
            return JsArray(reference, true)
        }

        fun anyArrayOf(reference: JSObject): JsAnyArray {
            return JsArray(reference, false)
        }

        fun newArray(env: WebEngine, initialSize: Int = 0): JSObject? {
            val result = env.execute("new Array($initialSize)")
            return if (result is JSObject && JsArrayInterface.isJsArray(result))
                result
            else
                null
        }

        fun newArray(env: JSObject, initialSize: Int = 0): JSObject? {
            val result = env.execute("new Array($initialSize)")
            return if (result is JSObject && JsArrayInterface.isJsArray(result))
                result
            else
                null
        }
    }

    private fun <M, R> with(
        nameInJs: String,
        callback: JsArrayIteratorCallback<M, R>,
        execution: (method: String) -> Any?
    ): Any? {
        reference.inject(nameInJs, callback)
        val result = execution("this.${nameInJs}.call")
        reference.uninject(nameInJs)
        if (result is Throwable) {
            throw JsArrayExecutionError("fail to execute JavaScript expression.")
        }
        return result
    }

    @Suppress("SameParameterValue")
    private fun <R> with(
        nameInJs: String,
        callback: JsArraySortFunction<R>,
        execution: (method: String) -> Any?
    ): Any? {
        reference.inject(nameInJs, callback)
        val result = execution("this.${nameInJs}.compare")
        reference.uninject(nameInJs)
        if (result is Throwable) {
            throw JsArrayExecutionError("fail to execute JavaScript expression.")
        }
        return result
    }

    private fun invoke(method: String, vararg args: Any?): Any? {
        return reference.invoke(method, *args)
    }

    private fun execute(jsExp: String): Any? {
        return reference.execute(jsExp)
    }

    override val length: Int
        get() = execute("this.${JsAPIs.Array.LENGTH}") as Int

    override fun toString(): String {
        return "[${join()}]"
    }

    override operator fun set(index: Int, value: T?) {
        reference.setSlot(index, value)
    }

    override operator fun get(index: Int): T? {
        val result = reference.getSlot(index)
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as T
    }

    override fun concat(other: JsArrayInterface<T>): JsArrayInterface<T> {
        val result = invoke(JsAPIs.Array.CONCAT, other.reference)
        if (result is JSObject)
            return JsArray(result, undefineAsNull)
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.CONCAT}() function.")
    }

    override fun join(separator: String): String {
        val result = invoke(JsAPIs.Array.JOIN, separator)
        if (result is String)
            return result
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.JOIN}() function.")
    }

    override fun reverse(): JsArrayInterface<T> {
        return if (invoke(JsAPIs.Array.REVERSE) is JSObject) {
            this
        } else {
            throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.REVERSE}() function.")
        }
    }

    override fun pop(): T? {
        val result = invoke(JsAPIs.Array.POP)
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as T
    }

    override fun push(vararg elements: T?): Int {
        return when (val result = invoke(JsAPIs.Array.PUSH, *elements)) {
            is Int -> result
            else -> throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.PUSH}() function.")
        }
    }

    override fun shift(): T? {
        val result = invoke(JsAPIs.Array.SHIFT)
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as T
    }

    override fun unshift(vararg elements: T?): Int {
        return when (val result = invoke(JsAPIs.Array.UNSHIFT, *elements)) {
            is Int -> result
            else -> throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.UNSHIFT}() function.")
        }
    }

    override fun slice(start: Int, end: Int?): JsArrayInterface<T> {
        val result = if (end == null) {
            invoke(JsAPIs.Array.SLICE, start)
        } else {
            invoke(JsAPIs.Array.SLICE, start, end)
        }
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.SLICE}() function.")
    }

    override fun splice(index: Int, count: Int, vararg items: T?): JsArray<T> {
        return when (val result = invoke(JsAPIs.Array.SPLICE, index, count, *items)) {
            is JSObject -> JsArray(result)
            else -> throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.SPLICE}() function.")
        }
    }

    override fun fill(value: T?, start: Int, end: Int?): JsArray<T> {
        val result = if (end == null) {
            invoke(JsAPIs.Array.FILL, value, start)
        } else {
            invoke(JsAPIs.Array.FILL, value, start, end)
        }
        return if (result is JSObject) {
            this
        } else {
            throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.FILL}() function.")
        }
    }

    override fun find(callback: JsArrayIteratorCallback<T?, Boolean>): T? {
        val result = with("__find_cb__", callback) { method: String ->
            execute("this.${JsAPIs.Array.FIND}((item, index, arr)=>{ return $method(item, index, null, arr); })")
        }
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as T
    }

    override fun findIndex(callback: JsArrayIteratorCallback<T, Boolean>): Int {
        val result = with("__find_index_cb__", callback) { method ->
            execute("this.${JsAPIs.Array.FIND_INDEX}((item, index, arr)=>{ return $method(item, index, null, arr); })")
        }
        if (result is Int)
            return result
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.FIND_INDEX}() function.")
    }

    override fun includes(element: T?, start: Int): Boolean {
        return when (val result = invoke(JsAPIs.Array.INCLUDES, element, start)) {
            is Boolean -> result
            else -> throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.INCLUDES}() function.")
        }
    }

    override fun indexOf(element: T?, start: Int): Int {
        return when (val result = invoke(JsAPIs.Array.INDEX_OF, element, start)) {
            is Int -> result
            else -> throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.INDEX_OF}() function.")
        }
    }

    override fun lastIndexOf(element: T?, start: Int): Int {
        return when (val result = invoke(JsAPIs.Array.LAST_INDEX_OF, element, start)) {
            is Int -> result
            else -> throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.LAST_INDEX_OF}() function.")
        }
    }

    override fun forLoop(callback: JsArrayIteratorCallback<T?, Boolean>, startIndex: Int, stopIndex: Int, step: Int) {
        this.with("__for_cb__", callback) { method ->
            val stop = if (stopIndex <= 0) length else stopIndex
            execute("for(let i=${startIndex}; i < ${stop}; i = i + $step){if(!$method(this[i], i, null, this)) break}")
        }
    }

    override fun forEach(callback: JsArrayIteratorCallback<T?, Unit>) {
        this.with("__forEach_cb__", callback) { method ->
            execute("this.${JsAPIs.Array.FOR_EACH}((item, index, arr)=>{ $method(item, index, null, arr); })")
        }
    }

    override fun filter(callback: JsArrayIteratorCallback<T?, Boolean>): JsArray<T> {
        val result = this.with("__filter_cb__", callback) { method ->
            execute("this.${JsAPIs.Array.FILTER}((item, index, arr)=>{ return $method(item, index, null, arr) })")
        }
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.FILTER}() function.")
    }

    override fun map(callback: JsArrayIteratorCallback<T?, T?>): JsArray<T> {
        val result = this.with("__map_cb__", callback) { method ->
            execute("this.${JsAPIs.Array.MAP}((item, index, arr)=>{ return $method(item, index, null, arr) })")
        }
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.MAP}() function.")
    }

    override fun every(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean {
        val result = this.with("__every_cb__", callback) { method ->
            execute("this.${JsAPIs.Array.EVERY}((item, index, arr)=>{ return $method(item, index, null, arr) })")
        }
        if (result is Boolean)
            return result
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.EVERY}() function.")
    }

    override fun some(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean {
        val result = this.with("__some_cb__", callback) { method ->
            execute("this.${JsAPIs.Array.SOME}((item, index, arr)=>{ return $method(item, index, null, arr) })")
        }
        if (result is Boolean)
            return result
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.SOME}() function.")
    }

    override fun <R> reduce(callback: JsArrayIteratorCallback<T?, R?>): R? {
        val result = this.with("__reduce_cb__", callback) { method ->
            execute("this.${JsAPIs.Array.REDUCE}((total, item, index, arr)=>{ return $method(item, index, total, arr) })")
        }
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as R
    }

    override fun <R> reduceRight(callback: JsArrayIteratorCallback<T?, R?>): R? {
        val result = this.with("__right_reduce_cb__", callback) { method ->
            execute("this.${JsAPIs.Array.REDUCE_RIGHT}((total, item, index, arr)=>{ return $method(item, index, total, arr) })")
        }
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as R
    }

    override fun sort(sortFunction: JsArraySortFunction<T?>?): JsArray<T> {
        val result = if (sortFunction == null)
            invoke(JsAPIs.Array.SORT)
        else {
            this.with("__sort_cb__", sortFunction) { method ->
                execute("this.${JsAPIs.Array.SORT}((a, b)=>{ return $method(a, b) })")
            }
        }
        if (result is JSObject)
            return this
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.SORT}() function.")
    }

    // 扩展API
    inline fun every(crossinline callback: TypedCallback2<T, Boolean>) {
        this.every(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })
    }

    inline fun some(crossinline callback: TypedCallback2<T, Boolean>) {
        this.some(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })
    }

    inline fun forEach(crossinline callback: TypedCallback2<T, Unit>) {
        this.forEach(object : TypedIteratorCallback<T?, Unit> {
            override fun call(currentValue: T?, index: Int, total: Unit?, arr: Any?) {
                callback(index, currentValue)
            }
        })
    }

    inline fun find(crossinline callback: TypedCallback2<T, Boolean>): T? {
        return this.find(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })
    }

    inline fun findIndex(crossinline callback: TypedCallback2<T, Boolean>): Int {
        return this.findIndex(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })
    }

    inline fun forLoop(
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

    inline fun filter(crossinline callback: TypedCallback2<T, Boolean>): JsArray<T> {
        return this.filter(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: Boolean?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })
    }

    inline fun map(crossinline callback: TypedCallback2<T, T>): JsArray<T> {
        return this.map(object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue)
            }
        })
    }

    inline fun <R> reduce(crossinline callback: TypedCallback3<T, R>): R? {
        return this.reduce(object : TypedIteratorCallback<T?, R?> {
            override fun call(currentValue: T?, index: Int, total: R?, arr: Any?): R? {
                return callback(index, currentValue, total)
            }
        })
    }

    inline fun <R> reduceRight(crossinline callback: TypedCallback3<T, R>): R? {
        return this.reduceRight(object : TypedIteratorCallback<T?, R?> {
            override fun call(currentValue: T?, index: Int, total: R?, arr: Any?): R? {
                return callback(index, currentValue, total)
            }
        })
    }

    inline fun sort(crossinline callback: TypedCallback1<T>): JsArray<T> {
        return this.sort(object : TypedSortFunction<T?>{
            override fun compare(a: T?, b: T?): Boolean {
                return callback(a, b)
            }
        })
    }
}