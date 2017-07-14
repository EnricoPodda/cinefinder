package net.confusione.cinefinder

import android.net.Uri
import android.util.Log
import java.io.*
import java.net.URL
import java.nio.charset.StandardCharsets

//This file contains package-level functions used by all the classes

/**
 * Returns a normalized version of the title of a movie
 *
 * @param _title Title of the movie
 * @return preHash of _title
 */
fun preHashTitle(_title: String) : String {
    //TODO: improve pre-hashing function
    var title = _title
    val regex = Regex("[^a-z]")
    title = title.toLowerCase()
    title = regex.replace(title,"")
    return title
}


/**
 * Returns an html-free version of the param string.
 * Removes only what is needed for the project
 *
 * @param _html Html to purge
 * @return Html-free version of _html
 */
fun decodeCommonHtmlEntities(_html: String): String {
    var html = _html
    html = html.replace("&nbsp;"," ")
    return html
}


fun downloadASCIIFile(_url: String): String {
    //val url: URL = URL("https://www.google.it/search?q="+ Uri.encode(cinema.name+" "+ location)+"+film")
    val url: URL = URL(_url)
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
    val responseString = String(response,StandardCharsets.UTF_8)
    return responseString
}
