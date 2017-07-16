package net.confusione.cinefinder

import android.net.Uri
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Chronometer
import org.joda.time.DateTime
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class GoogleMovies(weakContext: WeakReference<AppCompatActivity>) {

    val movieTheaters = ArrayList<Cinema>()
    val chrono : Chronometer = Chronometer(weakContext.get())

    init {
        MovieFactory.init(weakContext)
        chrono.base = SystemClock.elapsedRealtime();
    }

    /**
     *  Given a location (like Cagliari, Pisa etc) returns a populated ArrayList<Cinema>
     *  @param _location Biggest city around the current position
     *  @return All movie theaters organized with an ArrayList<Cinema>
     */
    fun downloadMoviesData(location: String): ArrayList<Cinema> {
        chrono.start()
        populateMovieTheaters(movieTheaters)
        val threads = ArrayList<Thread>()

        Log.d("Performance","downloadMovies: Starting Threads - "+((SystemClock.elapsedRealtime() - chrono.base)/1000).toString()+"s")

        for (cinema in movieTheaters) {
            threads.add(Thread { populateCinema(cinema, location) })
            threads.last().start()
        }

        Log.d("Performance","downloadMovies: Ended Threads - "+((SystemClock.elapsedRealtime() - chrono.base)/1000).toString()+"s")
        for (thread in threads) {
            if (thread.isAlive)
                thread.join()
        }

        Log.d("Performance","downloadMovieDate: End - "+((SystemClock.elapsedRealtime() - chrono.base)/1000).toString()+"s")
        return movieTheaters
    }

    fun populateCinema(cinema: Cinema, _location: String) {
        val location = Uri.encode(_location)
        val url: String = "https://www.google.it/search?q=" + Uri.encode(cinema.name) + "$location+film"
        val dayClass = "class=\"tb_c\""

        val html = downloadASCIIFile(url)
        var tmp = html
        val days = ArrayList<String>()
        while (tmp.contains(dayClass)) {
            tmp = tmp.substring(tmp.indexOf(dayClass) + dayClass.length)
            if (tmp.contains(dayClass))
                days.add(tmp.substring(0, tmp.indexOf(dayClass)))
            else
                days.add(tmp)
        }
        Log.d("Performance","populateCinema: Day started - "+((SystemClock.elapsedRealtime() - chrono.base)/1000).toString()+"s")
        for ((index,day) in days.withIndex()) {
            val document = Jsoup.parse(day)
            val elements : Elements = document.getElementsByClass("lr_c_fcb")
            for (element in elements) {
                val title = element.getElementsByClass("lr-s-din")[0].html()
                val movie = MovieFactory.getMovieInfo(title)
                val standard = element.getElementsByClass("lr_c_vn")[0].html()
                val showType : ShowType = standardToShowType(standard)
                val dates = element.getElementsByClass("lr_c_stnl")
                for (date in dates) {
                    val dateString = date.html()
                    val hour = dateString.substring(0,dateString.indexOf(":")).toInt()
                    val minutes = dateString.substring(dateString.indexOf(":")+1).toInt()
                    val baseDate = DateTime.now().plusDays(index)
                    val dateTime = DateTime(baseDate.year,baseDate.monthOfYear,baseDate.dayOfMonth,hour,minutes)
                    cinema.addShow(Show(cinema.name,dateTime,movie,showType))
                }
            }
            Log.d("Performance","populateCinema: Day ended - "+((SystemClock.elapsedRealtime() - chrono.base)/1000).toString()+"s")
        }
    }

    fun getTimeSchedule(movie: Movie) : ArrayList<Show> {
        val shows = ArrayList<Show>()
        for (cinema in movieTheaters) {
            val tmp = cinema.getShows(movie)
            for (show in tmp)
                shows.add(show)
        }
        return shows
    }


    private fun populateMovieTheaters(movieTheaters: ArrayList<Cinema>) {
        //TODO: Need to populate that with movie_theaters.json, using a workaround for testing purposes
        movieTheaters.add(Cinema("the space quartucciu","cagliari"))
        movieTheaters.add(Cinema("the space sestu","cagliari"))
        movieTheaters.add(Cinema("uci","cagliari"))
    }

}