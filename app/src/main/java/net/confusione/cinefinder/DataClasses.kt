package net.confusione.cinefinder

import java.util.*

//This file contains only data classes (aka structures)

data class Movie(val title: String, val description: String, val cast: ArrayList<String>, val releaseDate: String, val length: String, val standard: Boolean = true) {

    override fun equals(other: Any?): Boolean {
        if (other is Movie)
            return preHashTitle(title) == preHashTitle(other.title) && standard == other.standard
        else
            return false
    }

    override fun hashCode(): Int {
        return preHashTitle(title).hashCode()+standard.hashCode()
    }
}

data class Show(val cinema: String, val date: Date, val movie: Movie)