package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {
    this ?: return mutableListOf()
    if(substr.isEmpty())  return mutableListOf()
    val str = if (ignoreCase) {
        substr.toLowerCase()
    } else {
        substr
    }
    val regex = "$str"
    val resul = if (ignoreCase) {
        regex.toRegex().findAll(this.toLowerCase())
    } else {
        regex.toRegex().findAll(this)
    }
    val out: MutableList<Int> = mutableListOf()
    resul.forEach { out.add(it.range.start) }
    return out
}