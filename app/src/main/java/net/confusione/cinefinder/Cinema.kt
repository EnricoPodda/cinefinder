package net.confusione.cinefinder

import java.util.*
import java.util.*

class Cinema(val name: String) {
    private val shows = Hashtable<Movie,ArrayList<Date>>()

    fun addShow(show: Show) {
        if (shows.containsKey(show.movie))
            shows[show.movie]!!.add(show.date)
        else {
            shows[show.movie] = ArrayList<Date>()
            shows[show.movie]!!.add(show.date)
        }
    }

    fun getAllMovies() : ArrayList<Movie> {
        val result = ArrayList<Movie>()
        for (movie in shows.keys)
            result.add(movie)
        return result
    }

    fun getTimeSchedule(movie: Movie) : ArrayList<Date> {
        val result = shows[movie]
        if (result != null)
            return result
        else
            return ArrayList<Date>()
    }

    override fun toString(): String {
        return name
    }
}