package net.confusione.cinefinder

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