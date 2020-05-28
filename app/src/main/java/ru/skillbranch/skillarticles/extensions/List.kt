package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> =
        bounds.map { bound ->
            this.filter { it.second > bound.first && it.first < bound.second }
                    .map {
                        when {
                            it.first < bound.first -> Pair(bound.first, it.second)
                            it.second > bound.second -> Pair(it.first, bound.second)
                            else -> it
                        }
                    }
        }