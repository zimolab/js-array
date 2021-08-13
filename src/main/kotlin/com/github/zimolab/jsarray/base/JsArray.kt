package com.github.zimolab.jsarray.base

import com.github.zimolab.jsarray.*
import com.github.zimolab.jsarray.base.JsAPIs.UNDEFINED
import javafx.scene.web.WebEngine
import netscape.javascript.JSObject

@Suppress("UNCHECKED_CAST")
class JsArray<T>
private constructor(
    override val reference: JSObject,
    private val undefineAsNull: Boolean = true
) : JsArrayInterface<T> {

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
            return if (result is JSObject && JsArrayInterface.isJsArray(jsObject = result))
                result
            else
                null
        }

        fun newArray(env: JSObject, initialSize: Int = 0): JSObject? {
            val result = env.execute("new Array($initialSize)")
            return if (result is JSObject && JsArrayInterface.isJsArray(jsObject = result))
                result
            else
                null
        }

        fun newJsStringArray(env: JSObject, initialSize: Int = 0): JsArray<String?>? {
            val raw = newArray(env, initialSize) ?: return null
            return stringArrayOf(raw)
        }

        fun newJsIntArray(env: JSObject, initialSize: Int = 0): JsArray<Int?>? {
            val raw = newArray(env, initialSize) ?: return null
            return intArrayOf(raw)
        }

        fun newJsDoubleArray(env: JSObject, initialSize: Int = 0): JsArray<Double?>? {
            val raw = newArray(env, initialSize) ?: return null
            return doubleArrayOf(raw)
        }

        fun newBooleanArray(env: JSObject, initialSize: Int = 0): JsArray<Boolean?>? {
            val raw = newArray(env, initialSize) ?: return null
            return booleanArrayOf(raw)
        }

        fun newJSObjectArray(env: JSObject, initialSize: Int = 0): JsArray<JSObject?>? {
            val raw = newArray(env, initialSize) ?: return null
            return jsObjectArrayOf(raw)
        }

        fun newJsAnyArray(env: JSObject, initialSize: Int = 0): JsArray<Any?>? {
            val raw = newArray(env, initialSize) ?: return null
            return anyArrayOf(raw)
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
        val result = execute("{let __tmp = this[$index];__tmp==$UNDEFINED?null:__tmp}")
        if (result is Throwable)
            throw JsArrayExecutionError("failed to get value at index=$index")
        if (result == null)
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

    override fun includes(element: T?, start: Int): Boolean {
        val result = if (element == null) {
            execute( "this.${JsAPIs.Array.INCLUDES}(null, $start) || this.${JsAPIs.Array.INCLUDES}($UNDEFINED, $start)")
        } else {
            invoke(JsAPIs.Array.INCLUDES, element, start)
        }
        return when (result) {
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

    // BugFix #1
    private fun undefine2Null(name: String): String {
        return "${name}==$UNDEFINED?null:$name"
    }

    override fun find(callback: JsArrayIteratorCallback<T?, Boolean>): T? {
        val result = with("__find_cb__", callback) { method: String ->
            // BugFix #1
            execute("this.${JsAPIs.Array.FIND}((item, index, arr)=>{ return $method(${undefine2Null("item")}, index, null, arr); })")
        }
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as T
    }

    override fun findIndex(callback: JsArrayIteratorCallback<T, Boolean>): Int {
        val result = with("__find_index_cb__", callback) { method ->
            // BugFix #1
            execute( "this.${JsAPIs.Array.FIND_INDEX}((item, index, arr)=>{ return $method(${undefine2Null("item")}, index, null, arr); })")
        }
        if (result is Int)
            return result
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.FIND_INDEX}() function.")
    }

    override fun forLoop(callback: JsArrayIteratorCallback<T?, Boolean>, startIndex: Int, stopIndex: Int, step: Int) {
        this.with("__for_cb__", callback) { method ->
            val stop = if (stopIndex <= 0) length else stopIndex
            // BugFix #1
            execute("for(let i=${startIndex}; i < ${stop}; i = i + $step){if(!$method(${undefine2Null("this[i]")}, i, null, this)) break}")
        }
    }


    override fun forEach(callback: JsArrayIteratorCallback<T?, Unit>) {
        this.with("__forEach_cb__", callback) { method ->
            // BugFix #1
            execute("this.${JsAPIs.Array.FOR_EACH}((item, index, arr)=>{ $method(${undefine2Null("item")}, index, null, arr); })")
        }
    }

    override fun filter(callback: JsArrayIteratorCallback<T?, Boolean>): JsArray<T> {
        val result = this.with("__filter_cb__", callback) { method ->
            // BugFix #1
            execute("this.${JsAPIs.Array.FILTER}((item, index, arr)=>{ return $method(${undefine2Null("item")}, index, null, arr) })")
        }
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.FILTER}() function.")
    }

    override fun map(callback: JsArrayIteratorCallback<T?, T?>): JsArray<T> {
        val result = this.with("__map_cb__", callback) { method ->
            // BugFix #1
            execute("this.${JsAPIs.Array.MAP}((item, index, arr)=>{ return $method(${undefine2Null("item")}, index, null, arr) })")
        }
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.MAP}() function.")
    }

    override fun every(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean {
        val result = this.with("__every_cb__", callback) { method ->
            // BugFix #1
            execute("this.${JsAPIs.Array.EVERY}((item, index, arr)=>{ return $method(${undefine2Null("item")}, index, null, arr) })")
        }
        if (result is Boolean)
            return result
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.EVERY}() function.")
    }

    override fun some(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean {
        val result = this.with("__some_cb__", callback) { method ->
            // BugFix #1
            execute("this.${JsAPIs.Array.SOME}((item, index, arr)=>{ return $method(${undefine2Null("item")}, index, null, arr) })")
        }
        if (result is Boolean)
            return result
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.SOME}() function.")
    }

    override fun reduce(initialValue: T?, callback: JsArrayIteratorCallback<T?, T?>): T? {
        val result = this.with("__reduce_cb__", callback) { method ->
            // BugFix #1
            execute("this.${JsAPIs.Array.REDUCE}((total, item, index, arr)=>{ return $method(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) }, $initialValue)")
        }
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as T
    }

    override fun reduce(callback: JsArrayIteratorCallback<T?, T?>): T? {
        val result = this.with("__reduce_cb__", callback) { method ->
            // BugFix #1
            execute("this.${JsAPIs.Array.REDUCE}((total, item, index, arr)=>{ return $method(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) })")
        }
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as T
    }

    override fun reduceRight(initialValue: T?, callback: JsArrayIteratorCallback<T?, T?>): T? {
        val result = this.with("__right_reduce_cb__", callback) { method ->
            // BugFix #1
            execute("this.${JsAPIs.Array.REDUCE_RIGHT}((total, item, index, arr)=>{ return $method(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) }, $initialValue)")
        }
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as T
    }

    override fun reduceRight(callback: JsArrayIteratorCallback<T?, T?>): T? {
        val result = this.with("__right_reduce_cb__", callback) { method ->
            // BugFix #1
            execute("this.${JsAPIs.Array.REDUCE_RIGHT}((total, item, index, arr)=>{ return $method(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) })")
        }
        if (result == null || (undefineAsNull && result == UNDEFINED))
            return null
        return result as T
    }

    override fun sort(sortFunction: JsArraySortFunction<T?>?): JsArray<T> {
        val result = if (sortFunction == null)
            invoke(JsAPIs.Array.SORT)
        else {
            this.with("__sort_cb__", sortFunction) { method ->
                // BugFix #1
                execute("this.${JsAPIs.Array.SORT}((a, b)=>{ return $method(${undefine2Null("a")}, ${undefine2Null("b")}) })")
            }
        }
        if (result is JSObject)
            return this
        throw JsArrayExecutionError("failed to invoke ${JsAPIs.Array.SORT}() function.")
    }

    // 扩展API
    inline fun every(crossinline callback: TypedCallback2<T, Boolean>) =
        this.every(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })


    inline fun some(crossinline callback: TypedCallback2<T, Boolean>) =
        this.some(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })


    inline fun forEach(crossinline callback: TypedCallback2<T, Unit>) =
        this.forEach(object : TypedIteratorCallback<T?, Unit> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?) {
                callback(index, currentValue)
            }
        })


    inline fun find(crossinline callback: TypedCallback2<T, Boolean>) =
        this.find(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })


    inline fun findIndex(crossinline callback: TypedCallback2<T, Boolean>) =
        this.findIndex(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })

    inline fun forLoop(
        startIndex: Int = 0,
        stopIndex: Int = -1,
        step: Int = 1,
        crossinline callback: TypedCallback2<T, Boolean>
    ) =
        this.forLoop(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        }, startIndex, stopIndex, step)


    inline fun filter(crossinline callback: TypedCallback2<T, Boolean>) =
        this.filter(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })

    inline fun map(crossinline callback: TypedCallback2<T, T>) =
        this.map(object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue)
            }
        })

    inline fun reduce(initialValue: T?, crossinline callback: TypedCallback3<T, T>) =
        this.reduce(initialValue, object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue, total)
            }
        })

    inline fun reduce(crossinline callback: TypedCallback3<T, T>) =
        this.reduce(object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue, total)
            }
        })



    inline fun reduceRight(initialValue: T?, crossinline callback: TypedCallback3<T, T>) =
        this.reduceRight(initialValue, object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue, total)
            }
        })

    inline fun reduceRight(crossinline callback: TypedCallback3<T, T>) =
        this.reduceRight(object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue, total)
            }
        })

    inline fun sort(crossinline callback: TypedCallback1<T>) =
        this.sort(object : TypedSortFunction<T?> {
            override fun compare(a: T?, b: T?): Boolean {
                return callback(a, b)
            }
        })
}

// BugFix记录
//#1: 2021/8/13: 由于Array中某些方法（例如find()、findIndex()等）方法会遍历数组中的所有值，包括undefined
// 而在js->java的类型映射中，undefined会被映射成一个字符串形式的"undefined"，这些值可能会作为java回调函数的参数使用，这对于非字符串类型的参数而言显然会引发类型错误
// 为了避免这一错误，需要在js层面就将undefined值转换为null值，我们定义了一个undefined2Null()函数，用于生成将指定js变量的值从undefined转换为null的代码判断
