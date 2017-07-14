package net.confusione.cinefinder
import android.content.Context
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.PrintWriter

class JsonCache(val context: Context) {

    private val cacheFile = "cacheFile.json"

    fun writeMovie(movie: Movie) {
        val movies = readMovies()
        movies.add(movie)
        writeMovies(movies)
    }

    fun writeMovies(movies: ArrayList<Movie>) {
        val outputStream : PrintWriter = PrintWriter(context.openFileOutput(cacheFile,Context.MODE_PRIVATE))
        val writer = JsonWriter(outputStream)
        writer.setIndent("  ")
        writeMovies_List(movies,writer)
        writer.close()
    }

    fun readMovie(title: String) : Movie? {
        val movies = readMovies()
        for (movie in movies)
            if (movie.title == title)
                return movie
        return null
    }

    fun readMovies() : ArrayList<Movie> {
        var output = ArrayList<Movie>()
        val file = context.getFileStreamPath(cacheFile)
        if (file != null && file.exists()) {
            val inputStream: InputStreamReader = InputStreamReader(context.openFileInput(cacheFile))
            val reader = JsonReader(inputStream)
            output = readMovies_List(reader)
            reader.close()
        }
        return output
    }

    fun deleteMovie(title: String) {
        //TODO: Implement
    }


    private fun writeMovies_List(movies: ArrayList<Movie>, writer: JsonWriter) {
        writer.beginArray()
        for (movie in movies)
            writeMovies_Movie(movie,writer)
        writer.endArray()
    }

    private fun writeMovies_Movie(movie: Movie, writer: JsonWriter) {
        writer.beginObject()
        writer.name("title")
        writer.value(movie.title)
        writer.name("description")
        writer.value(movie.description)
        writer.name("releaseDate")
        writer.value(movie.releaseDate)
        writer.name("length")
        writer.value(movie.length)
        writer.name("image")
        writer.value(movie.image)
        writer.name("cast")
        writeMovies_Cast(movie.cast,writer)
        writer.endObject()
    }

    private fun writeMovies_Cast(cast: ArrayList<String>, writer: JsonWriter) {
        writer.beginArray()
        for (actor in cast)
            writer.value(actor)
        writer.endArray()
    }

    private fun readMovies_List(reader: JsonReader) : ArrayList<Movie> {
        val movies = ArrayList<Movie>()
        reader.beginArray()
        while (reader.hasNext())
            movies.add(readMovies_Movie(reader))
        reader.endArray()
        return movies
    }

    private fun readMovies_Movie(reader: JsonReader) : Movie {
        var title : String = ""
        var description: String = ""
        var length: String = ""
        var standard: Boolean = true
        var releaseDate: String = ""
        var cast : ArrayList<String> = ArrayList<String>()
        var image = ""
        val movie : Movie
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "title")
                title = reader.nextString()
            else if (name == "description")
                description = reader.nextString()
            else if (name == "length")
                length = reader.nextString()
            else if (name == "standard")
                standard = reader.nextBoolean()
            else if (name == "releaseDate")
                releaseDate = reader.nextString()
            else if (name == "cast")
                cast = readMovies_Cast(reader)
            else if (name == "image")
                image = reader.nextString()
            else
                reader.skipValue()
        }
        reader.endObject()
        movie = Movie(title, description,releaseDate,image)
        movie.cast = cast
        movie.length = length
        return movie
    }

    private fun readMovies_Cast(reader: JsonReader) : ArrayList<String> {
        val cast = ArrayList<String>()
        reader.beginArray()
        while (reader.hasNext())
            cast.add(reader.nextString())
        reader.endArray()
        return cast
    }

}