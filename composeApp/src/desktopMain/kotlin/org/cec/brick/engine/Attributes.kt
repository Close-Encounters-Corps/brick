package org.cec.brick.engine

import java.util.concurrent.ConcurrentHashMap

public class AttributeKey<out T : Any>(
    public val name: String,
    private val type: String
) {
    init {
        require(name.isNotBlank()) { "Name can't be blank" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is AttributeKey<*>) return false
        return name == other.name && type == other.type
    }

    override fun toString(): String {
        return "AttributeKey[$name]"
    }

    override fun hashCode(): Int = name.hashCode()
}

public inline fun <reified T : Any> attributeKeyOf(name: String): AttributeKey<T> =
    AttributeKey(name, T::class.toString())

public class Attributes {
    private val map = ConcurrentHashMap<AttributeKey<*>, Any?>()

    public operator fun <T : Any> get(key: AttributeKey<T>): T {
        return getOrNull(key) ?: error("No value for $key")
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> getOrNull(key: AttributeKey<T>): T? = map[key] as T?

    public fun <T : Any> put(key: AttributeKey<T>, value: T) {
        map[key] = value
    }

    public fun <T : Any> remove(key: AttributeKey<T>) {
        map.remove(key)
    }
}