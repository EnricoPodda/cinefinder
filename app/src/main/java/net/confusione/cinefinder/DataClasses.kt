package net.confusione.cinefinder

import java.util.*
import kotlin.collections.ArrayList

//This file contains only data classes (aka structures)

data class Movie(private val _title: String, val description: String, val releaseDate: String, val image: String) {

    val title : String
    var cast: ArrayList<String> = ArrayList()
    var length = ""

    init {
        title = sanitizeInput(_title)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Movie)
            return preHashTitle(title) == preHashTitle(other.title)
        else
            return false
    }

    override fun hashCode(): Int {
        return preHashTitle(title).hashCode()
    }

    fun sanitizeInput(_string: String): String {
        var string = _string
        string = string.replace("–","-")
        return string
    }
}

data class Show(val cinema: String, val timeSchedule: Date, val movie: Movie, val standard: Boolean)