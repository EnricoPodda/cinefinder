package net.confusione.cinefinder

import android.content.Context
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.util.JsonReader
import android.util.JsonWriter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.lang.ref.WeakReference
import java.util.*

class GoogleMovies(private val weakContext: WeakReference<AppCompatActivity>) {

    val movies = Hashtable<String,Movie>()
    val movieTheaters = ArrayList<Cinema>()

    init {
        val context : Context? = weakContext.get()
        if (context != null) {
            val jsonCache = JsonCache(context)
            val cache = jsonCache.readMovies()
            for (movie in cache)
                movies[preHashTitle(movie.title)] = movie
        }
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
            val url: String = "https://www.google.it/search?q=$cinema+$location+film"
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
            elements = document.getElementsByClass(titleClassName)
            //TODO: This for can be replaced with some high level function. Maybe.
            for (element in elements) { //Downloading info for the missing Movies
                if (element != elements[0] && elements[0].html() == element.html()) //TODO: Investigate that
                    break

                val movie_title = element.html()
                if (!movies.contains(preHashTitle(movie_title)))
                    movies[preHashTitle(movie_title)] = getMovieInfo(movie_title)
            }

            //TODO: Organize this shit
            days = document.getElementsByClass(dayClassName)
            for (day in days) {
                elements = day.getElementsByClass(movieFrameClassName)
                var calendar: Calendar
                for (element in elements) {
                    val title = element.getElementsByClass(titleClassName)[0].html()
                    val rawDates = element.getElementsByClass(dataClassName)
                    if (!movies.containsKey(preHashTitle(title)))
                        movies[preHashTitle(title)] = getMovieInfo(title)
                    val movie = movies[preHashTitle(title)]!!
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
                            cinema.addShow(Show(cinema.name, calendar.time, movie))
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
                shows.add(Show(cinema.name,date,movie))
        }
        return shows
    }

    fun getMovieInfo(movie_title: String) : Movie {
        val context : Context? = weakContext.get()
        if (movies[preHashTitle(movie_title)] != null)
            return movies[preHashTitle(movie_title)]!!

        val movie: Movie
        val detailsUrl = "https://www.google.it/search?q=$movie_title+durata"
        val document = Jsoup.connect(detailsUrl).get()

        val movie_length = getMovieLength(document)
        val movie_description = getMovieDescription(document)
        val movie_cast = getMovieCast(document)
        val movie_releaseDate = getMovieReleaseDate(document)

        movie = Movie(movie_title, movie_description, movie_cast, movie_releaseDate, movie_length)
        if (context != null) {
            val jsonCache = JsonCache(context)
            jsonCache.writeMovie(movie)
        }

        return movie
    }

    private fun getMovieLength(document: Document) : String {
        var length : String = ""
        val className = "_XWk"
        val elements = document.getElementsByClass(className)

        if (elements != null && elements.size > 0) {
            length = elements[0].html()
            length = decodeCommonHtmlEntities(length)
        }
        return length
    }

    private fun getMovieDescription(document: Document) : String {
        var description: String = ""
        val descriptionRegex = Regex("<span>(.*?)</span>",RegexOption.DOT_MATCHES_ALL)
        val className = "_RBg"
        val elements = document.getElementsByClass(className)

        if (elements != null && elements.size > 0) {
            var tmp = document.getElementsByClass(className)[0]
            tmp = tmp.child(0)
            val match = descriptionRegex.find(tmp.html())
            if (match != null)
                description = decodeCommonHtmlEntities(match.groupValues[1])
        }

        return description
    }

    private fun getMovieCast(document: Document) : ArrayList<String> {
        val cast = ArrayList<String>()
        val className = "kno-fb-ctx _Dnh kno-vrt-t"

        if (document.getElementsByClass(className).size > 0) {
            val rawElements = document.getElementsByClass(className)
            for (rawElement in rawElements)
                cast.add(rawElement.child(0).attr("title"))
        }

        return cast
    }

    private fun getMovieReleaseDate(document: Document) : String {
        var releaseDate: String = ""
        val releaseDateRegex = Regex("([0-9]{1,2} (gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre) [0-9]{4})")
        val yearRegex = Regex("([0-9]{4})")


        if (document.getElementsByClass("_Xbe kno-fv").size > 0) {
            val rawHtml = document.getElementsByClass("_Xbe kno-fv")[0].html()
            var match : MatchResult? = null
            if (releaseDateRegex.containsMatchIn(rawHtml))
                match = releaseDateRegex.find(rawHtml)
            else if (yearRegex.containsMatchIn(rawHtml))
                match = yearRegex.find(rawHtml)
            if (match != null)
                releaseDate = match.groupValues[0]
        }

        return releaseDate
    }

    private fun populateMovieTheaters(movieTheaters: ArrayList<Cinema>) {
        //TODO: Need to populate that with movie_theaters.json, using a workaround for testing purposes
        movieTheaters.add(Cinema("the space quartucciu"))
        movieTheaters.add(Cinema("the space sestu"))
        movieTheaters.add(Cinema("uci"))
    }

}