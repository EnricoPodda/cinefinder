package net.confusione.cinefinder

import java.util.*

class Cinema(val name: String, val location: String) {
    private val shows = Hashtable<Movie,ArrayList<Date>>()

    fun addShow(show: Show) {
        if (shows.containsKey(show.movie))
            shows[show.movie]!!.add(show.timeSchedule)
        else {
            shows[show.movie] = ArrayList<Date>()
            shows[show.movie]!!.add(show.timeSchedule)
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

    override fun equals(other: Any?): Boolean {
        if (other is Cinema) {
            if (this.name != other.name)
                return false
            if (this.location != other.location)
                return false
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        return (this.name+this.location).hashCode()
    }
}