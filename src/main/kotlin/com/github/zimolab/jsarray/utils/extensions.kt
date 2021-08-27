package com.github.zimolab.jsarray

import javafx.scene.web.WebEngine
import netscape.javascript.JSException
import netscape.javascript.JSObject

private val ID_REGEXP = """^[a-zA-Z_$][a-zA-Z0-9_$]*$""".toRegex()
private val ES_KEYWORDS = setOf(
    "break", "case", "catch", "continue", "default", "delete", "do",
    "else", "finally", "for", "function", "if", "in", "instanceof",
    "new", "return", "switch", "this", "throw", "try", "typeof", "var",
    "void", "while", "with", "abstract", "boolean", "byte", "char",
    "class", "const", "debugger", "double", "enum", "export", "extends",
    "final", "float", "goto", "implements", "import", "int", "interface",
    "long", "native", "package", "private", "protected", "public", "short",
    "static", "super", "synchronized", "throws", "transient", "volatile"
)

fun JSObject.inject(nameInJs: String, javaObject: Any) {
    if (!ID_REGEXP.matches(nameInJs) || nameInJs in ES_KEYWORDS)
        throw IllegalArgumentException("Illegal javascript name of java object")
    this.setMember(nameInJs, javaObject)
}

fun JSObject.uninject(nameInJs: String) {
    this.removeMember(nameInJs)
}

fun JSObject.invoke(methodName: String, vararg args: Any?, silently: Boolean = true, printlnStackTrace: Boolean = true): Any? {
    return if (silently) {
        try {
            this.call(methodName, *args)
        } catch (e: JSException) {
            if (printlnStackTrace) e.printStackTrace()
            e
        }
    } else {
        this.call(methodName, args)
    }
}

fun WebEngine.execute(jsCode: String, silently: Boolean = true, printlnStackTrace: Boolean = true): Any {
    return if (silently) {
        try {
           this.executeScript(jsCode)
        } catch (e: JSException) {
          if (printlnStackTrace) e.printStackTrace()
          e
        }
    } else {
        this.executeScript(jsCode)
    }
}

fun JSObject.execute(jsExp: String, silently: Boolean = true, printlnStackTrace: Boolean = true): Any? {
    return if (silently) {
        try {
            this.eval(jsExp)
        } catch (e: JSException) {
            if (printlnStackTrace) e.printStackTrace()
            e
        }
    } else {
        this.eval(jsExp)
    }
}