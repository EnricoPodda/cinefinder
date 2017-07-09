package net.confusione.cinefinder

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import java.util.*
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ListView
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainToolbar : Toolbar = findViewById(R.id.mainToolbar)
        setSupportActionBar(mainToolbar)

        GetMoviesTask(WeakReference(this)).execute(GoogleMovies(WeakReference(this)))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

    //IGNORE THIS CLASS
    class GetMoviesTask(val weakActivity : WeakReference<AppCompatActivity>): AsyncTask<GoogleMovies, Void , GoogleMovies>()  {

        override fun doInBackground(vararg _googleMovies: GoogleMovies):  GoogleMovies {
            val googleMovies = _googleMovies[0]
            //TODO: Find location with GPS
            googleMovies.downloadMoviesData("cagliari")
            return googleMovies
        }

        override fun onPostExecute(googleMovies: GoogleMovies) {

            /*for (cinema in result) {
                Log.d("Debug", cinema.name)
                val movies = cinema.getAllMovies()
                for (movie in movies) {
                    Log.d("Debug","    "+movie.title)
                    val dates = cinema.getDateForMovie(movie)
                    for (date in dates)
                        Log.d("Debug",date.toString())
                    Log.d("Debug","----------------------------")
                }
            }*/
            val tmp = weakActivity.get()
            if (tmp != null) {
                val activity : AppCompatActivity = tmp
                val listView : ListView = activity.findViewById<ListView>(R.id.list_view)
                val arrayAdapter = MyAdapter(activity.baseContext, googleMovies)
                listView.adapter = arrayAdapter
            }
        }

    }

    class MyAdapter (val context : Context, val googleMovies: GoogleMovies) : BaseAdapter(){

        private var layoutInflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        private val movieList = ArrayList<Movie> (googleMovies.movies.values)

        override fun getCount(): Int {
            return googleMovies.movies.size
        }

        override fun getItem(i: Int): Any {
            return movieList[i]
        }

        override fun getItemId(p0: Int): Long {
            return movieList[p0].hashCode().toLong()
        }

        override fun getView(i: Int, _convertView: View?, parentViews : ViewGroup?): View {

            val convertView : View

            if(_convertView == null)
                convertView = layoutInflater.inflate(R.layout.list_item, parentViews, false)
            else
                convertView = _convertView

            val titleView = convertView.findViewById<TextView>(R.id.title)
            titleView.text = movieList[i].title

            return convertView

        }

    }

}