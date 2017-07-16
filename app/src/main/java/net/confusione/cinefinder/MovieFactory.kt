package net.confusione.cinefinder

import android.content.Context
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.util.*

class MovieFactory {
    companion object {
        private val searchUrl = "https://api.themoviedb.org/3/search/movie"
        private val apiKey = "b10ba5c83d0dc1b22753cd6767ac0987"
        val baseImageUrl= "http://image.tmdb.org/t/p/w185/"
        private val movies = Hashtable<String,Movie>()
        private var weakContext: WeakReference<AppCompatActivity>? = null

        fun init(weakContext: WeakReference<AppCompatActivity>) {
            this.weakContext = weakContext
            val context : Context? = weakContext.get()
            if (context != null) {
                val jsonCache = JsonCache(context)
                val cache = jsonCache.readMovies()
                for (movie in cache)
                    movies[preHashTitle(movie.title)] = movie
            }
        }

        private fun buildSearchUrl(_title: String): String {
            var title = _title.toLowerCase()
            val regex = Regex("[^a-z 1-9:]")
            title = regex.replace(title,"")
            title = Uri.encode(title)
            return "$searchUrl?api_key=$apiKey&language=it-IT&query=$title&page=1&include_adult=false"
        }

        fun getAllMovies() : ArrayList<Movie> {
            return ArrayList<Movie>(movies.values)
        }

        fun getMovieInfo(title: String): Movie {
            val preHash = preHashTitle(title)
            if (movies.containsKey(preHash))
                return movies[preHash]!!

            val document : String
            try {
                document  = downloadASCIIFile(buildSearchUrl(title))
            }
            catch (ex: java.io.FileNotFoundException) {
                val movie = Movie(title,"","","")
                movies[preHash] = movie
                return movie
            }


            if (JSONObject(document).getJSONArray("results").length() == 0)
                return Movie(title,"","","")

            val jsonObject = JSONObject(document).getJSONArray("results")[0] as JSONObject

            val description = jsonObject.getString("overview")
            val releaseDate = jsonObject.getString("release_date")
            val image = getImageAsString(baseImageUrl+jsonObject.getString("poster_path"))

            movies[preHash] = Movie(title,description,releaseDate,image)

            val context : Context? = weakContext?.get()
            if (context != null) {
                val jsonCache = JsonCache(context)
                jsonCache.writeMovie(movies[preHash]!!)
            }
            return movies[preHash]!!
        }

        fun extendMovieInfo(movie: Movie) {
            val url = "https://www.google.it/search?q="+Uri.encode(movie.title)+"+length"
            val document = Jsoup.connect(url).get()
            movie.length = getMovieLength(document)
            movie.cast = getMovieCast(document)
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


        private fun getImageAsString(_url: String): String {
            val url = URL(_url)
            val inputStream = BufferedInputStream(url.openStream())
            val out = ByteArrayOutputStream()
            val buf = ByteArray(1024)
            var n = inputStream.read(buf)
            while (-1 != n) {
                out.write(buf, 0, n)
                n = inputStream.read(buf)
            }
            out.close()
            inputStream.close()
            val response = out.toByteArray()
            val base64String = android.util.Base64.encodeToString(response,android.util.Base64.NO_WRAP)
            return base64String
        }
    }
}


