package com.beust.klaxon

import java.util.ArrayList
import java.util.HashMap

trait JsonBase {
    fun valueToString(v: Any) : String {
        val result = StringBuilder()

        when (v) {
            is JsonObject -> result.append(v.toJsonString())
            is JsonArray<*> -> result.append(v.toJsonString())
            is String -> result.append("\"").append(v).append("\"")
            else -> result.append(v)
        }

        return result.toString()
    }
}

data open public class JsonObject(val map: MutableMap<String, Any>
                             = HashMap<String, Any>())
        : JsonBase, MutableMap<String, Any> by map {

    override fun put(key: String, value: Any) : JsonObject {
        map.put(key, value)
        return this
    }

    open fun getArray() : ArrayList<JsonObject>? {
        return null
    }

    fun obj(fieldName: String) : JsonObject? {
        return map.get(fieldName) as JsonObject
    }

    fun <T> array(fieldName: String) : JsonArray<T>? {
        return map.get(fieldName) as JsonArray<T>
    }

    fun long(fieldName: String) : Long? {
        return map.get(fieldName) as Long?
    }

    fun string(fieldName: String) : String? {
        return map.get(fieldName) as String
    }

    fun double(fieldName: String) : Double? {
        return map.get(fieldName) as Double
    }

    fun boolean(fieldName: String) : Boolean? {
        return map.get(fieldName) as Boolean
    }

    open fun asString() : String {
        throw RuntimeException("Not a String")
    }

    open fun toJsonString() : String {
        val result = StringBuilder();
        result.append("{ ")
        var comma = false
        for ((k, v) in map) {
            if (comma) {
                result.append(", ")
            } else {
                comma = true
            }
            result.append("\"").append(k).append("\" : ")
            result.append(valueToString(v)).append(" ")
        }
        result.append("} ")

        return result.toString()
    }
}

// Because of http://youtrack.jetbrains.com/issue/KT-3546, I need to do some
// manual delegation here
data public class JsonArray<T>(val value : MutableList<T> = ArrayList<T>())
    : JsonBase {

    fun add(a: T) : JsonArray<T> {
        value.add(a)
        value.forEach {  }
        return this
    }

    public fun filter(predicate: (T) -> Boolean) : List<T> {
        return value.filter(predicate)
    }

    public fun <R> flatMap(transform: (T)-> Iterable<R>) : List<R> {
        return value.flatMap(transform)
    }

    public fun <R> map(transform: (T)-> R) : List<R> {
        return value.map(transform)
    }

    fun string(id: String) : JsonArray<String>? {
        var result = JsonArray<String>()
        value.forEach {
            val obj = (it as JsonObject).string(id)
            result.add(obj!!)
        }
        return result
    }

    fun obj(id: String) : JsonArray<JsonObject> {
        var result = JsonArray<JsonObject>()
        value.forEach {
            val obj = (it as JsonObject).obj(id)
            result.add(obj!!)
        }
        return result
    }

    fun long(id: String) : JsonArray<Long> {
        var result = JsonArray<Long>()
        value.forEach {
            val obj = (it as JsonObject).long(id)
            result.add(obj!!)
        }
        return result
    }

    open fun toJsonString() : String {
        val result = StringBuilder();
        result.append("[ ")
        var comma = false
        value.forEach {
            if (comma) {
                result.append(", ")
            } else {
                comma = true
            }
            result.append(valueToString(it)).append(" ")
        }
        result.append("] ")

        return result.toString()
    }

    public fun forEach(field: String, operation: (JsonObject) -> Unit) : Unit {
        for (element in value) operation(element as JsonObject)
    }

    fun find(predicate: (T) -> Boolean) : T? {
        return value.find(predicate)
    }
}