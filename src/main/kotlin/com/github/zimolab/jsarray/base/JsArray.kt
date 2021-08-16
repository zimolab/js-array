package com.github.zimolab.jsarray.base

import com.github.zimolab.jsarray.*
import com.github.zimolab.jsarray.base.JsAPIs.Array.CONCAT
import com.github.zimolab.jsarray.base.JsAPIs.Array.EVERY
import com.github.zimolab.jsarray.base.JsAPIs.Array.FILL
import com.github.zimolab.jsarray.base.JsAPIs.Array.FILTER
import com.github.zimolab.jsarray.base.JsAPIs.Array.FIND
import com.github.zimolab.jsarray.base.JsAPIs.Array.FIND_INDEX
import com.github.zimolab.jsarray.base.JsAPIs.Array.FOR_EACH
import com.github.zimolab.jsarray.base.JsAPIs.Array.INCLUDES
import com.github.zimolab.jsarray.base.JsAPIs.Array.INDEX_OF
import com.github.zimolab.jsarray.base.JsAPIs.Array.JOIN
import com.github.zimolab.jsarray.base.JsAPIs.Array.LAST_INDEX_OF
import com.github.zimolab.jsarray.base.JsAPIs.Array.LENGTH
import com.github.zimolab.jsarray.base.JsAPIs.Array.MAP
import com.github.zimolab.jsarray.base.JsAPIs.Array.POP
import com.github.zimolab.jsarray.base.JsAPIs.Array.PUSH
import com.github.zimolab.jsarray.base.JsAPIs.Array.REDUCE
import com.github.zimolab.jsarray.base.JsAPIs.Array.REDUCE_RIGHT
import com.github.zimolab.jsarray.base.JsAPIs.Array.REVERSE
import com.github.zimolab.jsarray.base.JsAPIs.Array.SHIFT
import com.github.zimolab.jsarray.base.JsAPIs.Array.SLICE
import com.github.zimolab.jsarray.base.JsAPIs.Array.SOME
import com.github.zimolab.jsarray.base.JsAPIs.Array.SORT
import com.github.zimolab.jsarray.base.JsAPIs.Array.SPLICE
import com.github.zimolab.jsarray.base.JsAPIs.Array.UNSHIFT
import com.github.zimolab.jsarray.base.JsAPIs.UNDEFINED
import javafx.scene.web.WebEngine
import netscape.javascript.JSObject

@Suppress("UNCHECKED_CAST")
class JsArray<T>
private constructor(override val reference: JSObject) : JsArrayInterface<T> {

    init {
        if (!JsArrayInterface.isJsArray(reference)) {
            throw IllegalArgumentException("the reference is point to an javascript Array object.")
        }
    }

    companion object {
        fun stringArrayOf(reference: JSObject): JsStringArray {
            return JsArray(reference)
        }

        fun intArrayOf(reference: JSObject): JsIntArray {
            return JsArray(reference)
        }

        fun doubleArrayOf(reference: JSObject): JsDoubleArray {
            return JsArray(reference)
        }

        fun jsObjectArrayOf(reference: JSObject): JsObjectArray {
            return JsArray(reference)
        }

        fun booleanArrayOf(reference: JSObject): JsBooleanArray {
            return JsArray(reference)
        }

        fun anyArrayOf(reference: JSObject): JsAnyArray {
            return JsArray(reference)
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
        execution: (callbackName: String) -> Any?
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
        get() = execute("this.$LENGTH") as Int

    override fun toString(): String {
        return "[${join(separator = ",")}]"
    }

    override operator fun set(index: Int, value: T?) {
        reference.setSlot(index, value)
    }

    override operator fun get(index: Int): T? {
        return getAny(index)?.let {
            it as T
        }
    }

    override fun getAny(index: Int): Any? {
        val result = execute("{let __tmp = this[$index];__tmp==$UNDEFINED?null:__tmp;}")
        if (result is Throwable)
            throw JsArrayExecutionError("failed to get value at index=$index")
        return result
    }

    override fun concat(other: JsArrayInterface<T>): JsArrayInterface<T> {
        val result = invoke(CONCAT, other.reference)
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke $CONCAT() function.")
    }

    override fun concatAny(other: JsArrayInterface<T>): JsArrayInterface<Any?> {
        val result = invoke(CONCAT, other.reference)
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke $CONCAT() function.")
    }

    override fun join(separator: String): String {
        val result = invoke(JOIN, separator)
        if (result is String)
            return result
        throw JsArrayExecutionError("failed to invoke $JOIN() function.")
    }

    override fun reverse(): JsArrayInterface<T> {
        return if (invoke(REVERSE) is JSObject) {
            this
        } else {
            throw JsArrayExecutionError("failed to invoke $REVERSE() function.")
        }
    }

    override fun pop(): T? {
        return popAny()?.let {
            it as T
        }
    }

    override fun popAny(): Any? {
        val result = execute("{let __tmp = this.$POP();__tmp==$UNDEFINED?null:__tmp;}")
        if (result is Throwable)
            throw JsArrayExecutionError("failed to invoke $POP() function.")
        return result
    }

    override fun push(vararg elements: T?): Int {
        return when (val result = invoke(PUSH, *elements)) {
            is Int -> result
            else -> throw JsArrayExecutionError("failed to invoke $PUSH() function.")
        }
    }

    override fun shift(): T? {
        return shiftAny()?.let {
            it as T
        }
    }

    override fun shiftAny(): Any? {
        val result = execute("{let __tmp = this.$SHIFT();__tmp==$UNDEFINED?null:__tmp;}")
        if (result is Throwable)
            throw JsArrayExecutionError("failed to invoke $SHIFT() function.")
        return result
    }

    override fun unshift(vararg elements: T?): Int {
        return when (val result = invoke(UNSHIFT, *elements)) {
            is Int -> result
            else -> throw JsArrayExecutionError("failed to invoke $UNSHIFT() function.")
        }
    }

    override fun slice(start: Int, end: Int?): JsArrayInterface<T> {
        val result = if (end == null) {
            invoke(SLICE, start)
        } else {
            invoke(SLICE, start, end)
        }
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke $SLICE() function.")
    }

//    override fun sliceAny(start: Int, end: Int?): JsArrayInterface<Any?> {
//        val result = if (end == null) {
//            invoke(SLICE, start)
//        } else {
//            invoke(SLICE, start, end)
//        }
//        if (result is JSObject)
//            return JsArray(result)
//        throw JsArrayExecutionError("failed to invoke $SLICE() function.")
//    }

    override fun splice(index: Int, count: Int, vararg items: T?): JsArrayInterface<T> {
        return when (val result = invoke(SPLICE, index, count, *items)) {
            is JSObject -> JsArray(result)
            else -> throw JsArrayExecutionError("failed to invoke $SPLICE() function.")
        }
    }

//    override fun spliceAny(index: Int, count: Int, vararg items: T?): JsArrayInterface<Any?> {
//        return when (val result = invoke(SPLICE, index, count, *items)) {
//            is JSObject -> JsArray(result)
//            else -> throw JsArrayExecutionError("failed to invoke $SPLICE() function.")
//        }
//    }

    override fun fill(value: T?, start: Int, end: Int?): JsArrayInterface<T> {
        val result = if (end == null) {
            invoke(FILL, value, start)
        } else {
            invoke(FILL, value, start, end)
        }
        return if (result is JSObject) {
            this
        } else {
            throw JsArrayExecutionError("failed to invoke $FILL() function.")
        }
    }

    override fun includes(element: Any?, start: Int): Boolean {
        val result = if (element == null) {
            execute("this.$INCLUDES(null, $start) || this.$INCLUDES($UNDEFINED, $start)")
        } else {
            invoke(INCLUDES, element, start)
        }
        return when (result) {
            is Boolean -> result
            else -> throw JsArrayExecutionError("failed to invoke $INCLUDES() function.")
        }
    }

    override fun indexOf(element: Any?, start: Int): Int {
        val result = if (element == null) {
            execute(
                "{" +
                        "let __tmp=this.$INDEX_OF(null, $start);" +
                        "__tmp!=-1?__tmp:this.$INDEX_OF($UNDEFINED, $start);" +
                        "}"
            )
        } else {
            invoke(INDEX_OF, element, start)
        }
        return when (result) {
            is Int -> result
            else -> throw JsArrayExecutionError("failed to invoke $INDEX_OF() function.")
        }
    }

    override fun lastIndexOf(element: Any?, start: Int): Int {
        val result = if (element == null) {
            execute(
                "" +
                        "{" +
                        "let __tmp=this.$LAST_INDEX_OF(null, $start);" +
                        "__tmp!=-1?__tmp:this.$LAST_INDEX_OF($UNDEFINED, $start);" +
                        "}"
            )
        } else {
            invoke(LAST_INDEX_OF, element, start)
        }
        return when (result) {
            is Int -> result
            else -> throw JsArrayExecutionError("failed to invoke $LAST_INDEX_OF() function.")
        }
    }

    // BugFix #1
    private fun undefine2Null(name: String): String {
        return "${name}==$UNDEFINED?null:$name"
    }

    override fun find(callback: JsArrayIteratorCallback<T?, Boolean>): T? {
        return with("__find_cb__", callback) { callback_: String ->
            // BugFix #1
            execute(
                "{" +
                        "let __tmp = this.$FIND((item, index, arr)=>{ return $callback_(${undefine2Null("item")}, index, null, arr); });" +
                        "__tmp==$UNDEFINED?null:__tmp;" +
                        "}"
            )
        }?.let {
            it as T
        }
    }

    override fun findAny(callback: UnTypedIteratorCallback<Boolean>): Any? {
        return with("__find_cb__", callback) { callback_: String ->
            // BugFix #1
            execute(
                "{" +
                        "let __tmp = this.$FIND((item, index, arr)=>{ return $callback_(${undefine2Null("item")}, index, null, arr); });" +
                        "__tmp==$UNDEFINED?null:__tmp;" +
                        "}"
            )
        }
    }

    override fun findIndex(callback: JsArrayIteratorCallback<T?, Boolean>): Int {
        val result = with("__find_index_cb__", callback) { callback_ ->
            // BugFix #1
            execute("this.$FIND_INDEX((item, index, arr)=>{ return $callback_(${undefine2Null("item")}, index, null, arr); })")
        }
        if (result is Int)
            return result
        throw JsArrayExecutionError("failed to invoke $FIND_INDEX() function.")
    }

    override fun forLoop(callback: JsArrayIteratorCallback<T?, Boolean>, startIndex: Int, stopIndex: Int, step: Int) {
        this.with("__for_cb__", callback) { callback_ ->
            val stop = if (stopIndex <= 0) length else stopIndex
            // BugFix #1
            execute(
                "" +
                        "for(let i=${startIndex}; i < ${stop}; i = i + $step){" +
                        "   let _continue=$callback_(${undefine2Null("this[i]")}, i, null, this);" +
                        "   if(!_continue) " +
                        "       break;" +
                        "}"
            )
        }
    }


    override fun forEach(callback: JsArrayIteratorCallback<T?, Unit>) {
        this.with("__forEach_cb__", callback) { callback_ ->
            // BugFix #1
            execute("this.$FOR_EACH((item, index, arr)=>{ $callback_(${undefine2Null("item")}, index, null, arr); })")
        }
    }

    override fun filter(callback: JsArrayIteratorCallback<T?, Boolean>): JsArrayInterface<T> {
        val result = this.with("__filter_cb__", callback) { callback_ ->
            // BugFix #1
            execute("this.$FILTER((item, index, arr)=>{ return $callback_(${undefine2Null("item")}, index, null, arr); })")
        }
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke $FILTER() function.")
    }

    override fun filterAny(callback: UnTypedIteratorCallback<Boolean>): JsArrayInterface<Any?> {
        val result = this.with("__filter_cb__", callback) { callback_ ->
            // BugFix #1
            execute("this.$FILTER((item, index, arr)=>{ return $callback_(${undefine2Null("item")}, index, null, arr); })")
        }
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke $FILTER() function.")
    }

    override fun map(callback: JsArrayIteratorCallback<T?, T?>): JsArrayInterface<T> {
        val result = this.with("__map_cb__", callback) { callback_ ->
            // BugFix #1
            execute("this.$MAP((item, index, arr)=>{ return $callback_(${undefine2Null("item")}, index, null, arr); })")
        }
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke $MAP() function.")
    }

    override fun mapAny(callback: UnTypedIteratorCallback<Any?>): JsArrayInterface<Any?> {
        val result = this.with("__map_cb__", callback) { callback_ ->
            // BugFix #1
            execute("this.$MAP((item, index, arr)=>{ return $callback_(${undefine2Null("item")}, index, null, arr); })")
        }
        if (result is JSObject)
            return JsArray(result)
        throw JsArrayExecutionError("failed to invoke $MAP() function.")
    }

    override fun every(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean {
        val result = this.with("__every_cb__", callback) { callback_ ->
            // BugFix #1
            execute("this.$EVERY((item, index, arr)=>{ return $callback_(${undefine2Null("item")}, index, null, arr); })")
        }
        if (result is Boolean)
            return result
        throw JsArrayExecutionError("failed to invoke $EVERY() function.")
    }

    override fun some(callback: JsArrayIteratorCallback<T?, Boolean>): Boolean {
        val result = this.with("__some_cb__", callback) { callback_ ->
            // BugFix #1
            execute("this.$SOME((item, index, arr)=>{ return $callback_(${undefine2Null("item")}, index, null, arr); })")
        }
        if (result is Boolean)
            return result
        throw JsArrayExecutionError("failed to invoke $SOME() function.")
    }

    override fun reduce(initialValue: T?, callback: JsArrayIteratorCallback<T?, T?>): T? {
        return this.with("__reduce_cb__", callback) { callback_ ->
            // BugFix #1
            execute(
                "{" +
                        "let __tmp=this.$REDUCE((total, item, index, arr)=>{ " +
                        "return $callback_(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) }, $initialValue);" +
                        "__tmp==$UNDEFINED?null:__tmp;" +
                        "}"
            )
        }?.let {
            it as T
        }
    }

    override fun reduceAny(initialValue: Any?, callback: JsArrayIteratorCallback<Any?, Any?>): Any? {
        return this.with("__reduce_cb__", callback) { callback_ ->
            // BugFix #1
            execute(
                "{" +
                        "let __tmp=this.$REDUCE((total, item, index, arr)=>{ " +
                        "return $callback_(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) }, $initialValue);" +
                        "__tmp==$UNDEFINED?null:__tmp;" +
                        "}"
            )
        }
    }

    override fun reduce(callback: JsArrayIteratorCallback<T?, T?>): T? {
        return this.with("__reduce_cb__", callback) { callback_ ->
            // BugFix #1
            execute(
                "{" +
                        "let __tmp=this.$REDUCE((total, item, index, arr)=>{ " +
                        "return $callback_(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) });" +
                        "__tmp==$UNDEFINED?null:__tmp;" +
                        "}"
            )
        }?.let {
            it as T
        }
    }

    override fun reduceAny(callback: UnTypedIteratorCallback<Any?>): Any? {
        return this.with("__reduce_cb__", callback) { callback_ ->
            // BugFix #1
            execute(
                "{" +
                        "let __tmp=this.$REDUCE((total, item, index, arr)=>{ " +
                        "return $callback_(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) });" +
                        "__tmp==$UNDEFINED?null:__tmp;" +
                        "}"
            )
        }
    }

    override fun reduceRight(initialValue: T?, callback: JsArrayIteratorCallback<T?, T?>): T? {
        return this.with("__reduce_cb__", callback) { callback_ ->
            // BugFix #1
            execute(
                "{" +
                        "let __tmp=this.${REDUCE_RIGHT}((total, item, index, arr)=>{ " +
                        "return $callback_(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) }, $initialValue);" +
                        "__tmp==$UNDEFINED?null:__tmp;" +
                        "}"
            )
        }?.let {
            it as T
        }
    }

    override fun reduceRightAny(initialValue: Any?, callback: UnTypedIteratorCallback<Any?>): Any? {
        return this.with("__reduce_cb__", callback) { callback_ ->
            // BugFix #1
            execute(
                "{" +
                        "let __tmp=this.${REDUCE_RIGHT}((total, item, index, arr)=>{ " +
                        "return $callback_(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) }, $initialValue);" +
                        "__tmp==$UNDEFINED?null:__tmp;" +
                        "}"
            )
        }
    }

    override fun reduceRight(callback: JsArrayIteratorCallback<T?, T?>): T? {
        return this.with("__reduce_cb__", callback) { callback_ ->
            // BugFix #1
            execute(
                "{" +
                        "let __tmp=this.$REDUCE_RIGHT((total, item, index, arr)=>{ " +
                        "return $callback_(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) });" +
                        "__tmp==$UNDEFINED?null:__tmp;" +
                        "}"
            )
        }?.let {
            it as T
        }
    }

    override fun reduceRightAny(callback: UnTypedIteratorCallback<Any?>): Any? {
        return this.with("__reduce_cb__", callback) { callback_ ->
            // BugFix #1
            execute(
                "{" +
                        "let __tmp=this.$REDUCE_RIGHT((total, item, index, arr)=>{ " +
                        "return $callback_(${undefine2Null("item")}, index, ${undefine2Null("total")}, arr) });" +
                        "__tmp==$UNDEFINED?null:__tmp;" +
                        "}"
            )
        }
    }

    override fun sort(sortFunction: JsArraySortFunction<T?>?): JsArrayInterface<T> {
        val result = if (sortFunction == null)
            invoke(SORT)
        else {
            this.with("__sort_cb__", sortFunction) { sortFunc_ ->
                // BugFix #1
                execute("this.$SORT((a, b)=>{ return $sortFunc_(${undefine2Null("a")}, ${undefine2Null("b")}) });")
            }
        }
        if (result is JSObject)
            return this
        throw JsArrayExecutionError("failed to invoke $SORT() function.")
    }

    override fun toJsAnyArray(): JsArrayInterface<Any?> {
        return JsArray(this.reference)
    }

    // 扩展API（基于核心API）
    // TypedXxx：限定类型的处理器
    // UnTypedXxx: 任意类型（Any?）的处理器
    inline fun every(crossinline callback: TypedCallback2<T, Boolean>) =
        this.every(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })

    inline fun every(crossinline callback: UnTypedCallback2<Boolean>) =
        this.every(object : UnTypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to currentValue)
            }
        })

    inline fun some(crossinline callback: TypedCallback2<T, Boolean>) =
        this.some(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })

    inline fun some(crossinline callback: UnTypedCallback2<Boolean>) =
        this.some(object : UnTypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to currentValue)
            }
        })


    inline fun forEach(crossinline callback: TypedCallback2<T, Unit>) =
        this.forEach(object : TypedIteratorCallback<T?, Unit> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?) {
                callback(index, currentValue)
            }
        })

    inline fun forEach(crossinline callback: UnTypedCallback2<Unit>) =
        this.forEach(object : UnTypedIteratorCallback<Unit> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?) {
                callback(index to currentValue)
            }
        })

    inline fun find(crossinline callback: TypedCallback2<T, Boolean>) =
        this.find(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })

    inline fun find(crossinline callback: UnTypedCallback2<Boolean>) =
        this.find(object : UnTypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to currentValue)
            }
        })

    inline fun findAny(crossinline callback: UnTypedCallback2<Boolean>) =
        this.findAny(object : UnTypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to currentValue)
            }
        })


    inline fun findIndex(crossinline callback: TypedCallback2<T, Boolean>) =
        this.findIndex(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })

    inline fun findIndex(crossinline callback: UnTypedCallback2<Boolean>) =
        this.findIndex(object : UnTypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to currentValue)
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

    inline fun forLoop(
        startIndex: Int = 0,
        stopIndex: Int = -1,
        step: Int = 1,
        crossinline callback: UnTypedCallback2<Boolean>
    ) =
        this.forLoop(object : UnTypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to currentValue)
            }
        }, startIndex, stopIndex, step)


    inline fun filter(crossinline callback: TypedCallback2<T, Boolean>) =
        this.filter(object : TypedIteratorCallback<T?, Boolean> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): Boolean {
                return callback(index, currentValue)
            }
        })

    inline fun filter(crossinline callback: UnTypedCallback2<Boolean>) =
        this.filter(object : UnTypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to currentValue)
            }
        })

    inline fun filterAny(crossinline callback: UnTypedCallback2<Boolean>) =
        this.filterAny(object : UnTypedIteratorCallback<Boolean> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Boolean {
                return callback(index to currentValue)
            }
        })

    inline fun map(crossinline callback: TypedCallback2<T, T>) =
        this.map(object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue)
            }
        })

    inline fun map(crossinline callback: UnTypedCallback2<T>) =
        this.map(object : UnTypedIteratorCallback<T?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): T? {
                return callback(index to currentValue)
            }
        })

    inline fun mapAny(crossinline callback: UnTypedCallback2<Any?>) =
        this.mapAny(object : UnTypedIteratorCallback<Any?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return callback(index to currentValue)
            }
        })

    inline fun reduce(initialValue: T?, crossinline callback: TypedCallback3<T, T>) =
        this.reduce(initialValue, object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue, total)
            }
        })

    inline fun reduce(initialValue: T?, crossinline callback: UnTypedCallback3<T>) =
        this.reduce(initialValue, object : UnTypedIteratorCallback<T?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): T? {
                return callback(Triple(index, currentValue, total))
            }
        })

    inline fun reduceAny(initialValue: Any?, crossinline callback: UnTypedCallback3<Any?>) =
        this.reduceAny(initialValue, object : UnTypedIteratorCallback<Any?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return callback(Triple(index, currentValue, total))
            }
        })

    inline fun reduce(crossinline callback: TypedCallback3<T, T>) =
        this.reduce(object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue, total)
            }
        })

    inline fun reduce(crossinline callback: UnTypedCallback3<T>) =
        this.reduce(object : UnTypedIteratorCallback<T?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): T? {
                return callback(Triple(index, currentValue, total))
            }
        })

    inline fun reduceAny(crossinline callback: UnTypedCallback3<Any?>) =
        this.reduceAny(object : UnTypedIteratorCallback<Any?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return callback(Triple(index, currentValue, total))
            }
        })

    inline fun reduceRight(initialValue: T?, crossinline callback: TypedCallback3<T, T>) =
        this.reduceRight(initialValue, object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue, total)
            }
        })

    inline fun reduceRight(initialValue: T?, crossinline callback: UnTypedCallback3<T>) =
        this.reduce(initialValue, object : UnTypedIteratorCallback<T?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): T? {
                return callback(Triple(index, currentValue, total))
            }
        })

    inline fun reduceRightAny(initialValue: Any?, crossinline callback: UnTypedCallback3<Any?>) =
        this.reduceRightAny(initialValue, object : UnTypedIteratorCallback<Any?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return callback(Triple(index, currentValue, total))
            }
        })

    inline fun reduceRight(crossinline callback: TypedCallback3<T, T>) =
        this.reduceRight(object : TypedIteratorCallback<T?, T?> {
            override fun call(currentValue: T?, index: Int, total: T?, arr: Any?): T? {
                return callback(index, currentValue, total)
            }
        })

    inline fun reduceRight(crossinline callback: UnTypedCallback3<T>) =
        this.reduce(object : UnTypedIteratorCallback<T?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): T? {
                return callback(Triple(index, currentValue, total))
            }
        })

    inline fun reduceRightAny(crossinline callback: UnTypedCallback3<Any?>) =
        this.reduceRightAny(object : UnTypedIteratorCallback<Any?> {
            override fun call(currentValue: Any?, index: Int, total: Any?, arr: Any?): Any? {
                return callback(Triple(index, currentValue, total))
            }
        })

    inline fun sort(crossinline callback: TypedSortComparator<T>) =
        this.sort(object : TypedSortFunction<T?> {
            override fun compare(a: T?, b: T?): Boolean {
                return callback(a, b)
            }
        })

    inline fun sort(crossinline callback: UntypedSortComparator) =
        this.sort(object : UnTypedSortFunction {
            override fun compare(a: Any?, b: Any?): Boolean {
                return callback(a to b)
            }
        })
}

// BugFix记录
//#1: 2021/8/13: 由于Array中某些方法（例如find()、findIndex()等）方法会遍历数组中的所有值，包括undefined
// 而在js->java的类型映射中，undefined会被映射成一个字符串形式的"undefined"，这些值可能会作为java回调函数的参数使用，这对于非字符串类型的参数而言显然会引发类型错误
// 为了避免这一错误，需要在js层面就将undefined值转换为null值，我们定义了一个undefined2Null()函数，用于生成将指定js变量的值从undefined转换为null的代码判断
