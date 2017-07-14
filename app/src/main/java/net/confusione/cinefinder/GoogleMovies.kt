package net.confusione.cinefinder

import android.content.Context
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.util.*

class GoogleMovies(weakContext: WeakReference<AppCompatActivity>) {

    val movieTheaters = ArrayList<Cinema>()

    init {
        MovieFactory.init(weakContext)
    }

    /**
     *  Given a location (like Cagliari, Pisa etc) returns a populated ArrayList<Cinema>
     *  @param _location Biggest city around the current position
     *  @return All movie theaters organized with an ArrayList<Cinema>
     */
    fun downloadMoviesData(_location: String): ArrayList<Cinema> {
        val location = Uri.encode(_location)
        populateMovieTheaters(movieTheaters)

        for (cinema in movieTheaters) {
            val url: String = "https://www.google.it/search?q="+Uri.encode(cinema.name)+"$location+film"
            val dayClassName = "tb_c"
            val movieFrameClassName = "lr_c_fcb"
            val titleClassName = "lr-s-din"
            val specialFrameClassName = "lr_c_vn"
            val dataClassName = "lr_c_s"
            val hourRegex = Regex("([0-9]{1,2}):([0-9]{2})")
            val document: Document
            var elements: Elements
            val days : Elements
            var counter = 0

            document = Jsoup.connect(url).get()

            //TODO: Organize this shit
            days = document.getElementsByClass(dayClassName)
            for (day in days) {
                elements = day.getElementsByClass(movieFrameClassName)
                var calendar: Calendar
                for (element in elements) {
                    val title = element.getElementsByClass(titleClassName)[0].html()
                    val rawDates = element.getElementsByClass(dataClassName)
                    val movie = MovieFactory.getMovieInfo(title)
                    for (rawDate in rawDates) {
                        val dateString = rawDate.html()
                        val matches = hourRegex.findAll(dateString)
                        for (match in matches) {
                            val hour = match.groupValues[1]
                            val minute = match.groupValues[2]
                            calendar = Calendar.getInstance()
                            calendar.set(Calendar.HOUR_OF_DAY, hour.toInt())
                            calendar.set(Calendar.MINUTE, minute.toInt())
                            calendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                            calendar.add(Calendar.DAY_OF_MONTH, counter)
                            //TODO: Difference between
                            cinema.addShow(Show(cinema.name, calendar.time, movie, true)) //TODO: Fix "true"
                        }
                    }
                }
                counter++
            }
        }

        return movieTheaters
    }

    fun getTimeSchedule(movie: Movie) : ArrayList<Show> {
        val shows = ArrayList<Show>()
        for (cinema in movieTheaters) {
            val dates = cinema.getTimeSchedule(movie)
            for (date in dates)
                shows.add(Show(cinema.name,date,movie,true)) //TODO: fix true
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