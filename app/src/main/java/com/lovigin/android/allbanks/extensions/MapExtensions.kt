package com.lovigin.android.allbanks.extensions

inline fun <K, V, R : Any> Map<K, V>.mapKeysTo(transform: (K) -> R): Map<R, V> {
    val result = LinkedHashMap<R, V>(this.size)
    for ((k, v) in this) {
        result[transform(k)] = v
    }
    return result
}