package net.confusione.cinefinder

import org.joda.time.DateTime
import java.util.*

class Cinema(val name: String, val location: String) {
    private val shows = Hashtable<Movie,ArrayList<Show>>()

    fun addShow(show: Show) {
        if (shows.containsKey(show.movie))
            shows[show.movie]!!.add(show)
        else {
            shows[show.movie] = ArrayList<Show>()
            shows[show.movie]!!.add(show)
        }
    }

    fun getAllMovies() : ArrayList<Movie> {
        val result = ArrayList<Movie>()
        for (movie in shows.keys)
            result.add(movie)
        return result
    }

    fun getShows(movie: Movie) : ArrayList<Show> {
        val result = shows[movie]
        if (result != null)
            return result
        else
            return ArrayList<Show>()
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