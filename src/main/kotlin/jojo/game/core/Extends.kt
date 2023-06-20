package jojo.game.core

fun <T> List<T>.rotate(rotationPointPredicate: (T) -> Boolean): List<T> {
    val index = indexOfFirst(rotationPointPredicate)
    if (index < 0) return this
    return subList(index, size) + subList(0, index)
}