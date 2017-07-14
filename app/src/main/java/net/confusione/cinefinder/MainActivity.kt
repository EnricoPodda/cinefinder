package net.confusione.cinefinder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.list_schedule.view.*
import java.util.*
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList
import org.joda.time.DateTime
import java.util.zip.Inflater
import kotlin.concurrent.timer


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
            val tmp = weakActivity.get()
            if (tmp != null) {
                val activity : AppCompatActivity = tmp
                val listView : ListView = activity.findViewById<ListView>(R.id.list_view)
                val arrayAdapter = MoviesAdapter(activity.baseContext, googleMovies)
                listView.adapter = arrayAdapter
            }
        }

    }

    class MoviesAdapter(val context : Context, val googleMovies: GoogleMovies) : BaseAdapter(){

        private var layoutInflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        private val movieList = ArrayList<Movie> (MovieFactory.getAllMovies())

        override fun getCount(): Int {
            return movieList.size
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

            val imageView = convertView.findViewById<ImageView>(R.id.image)
            var bMap : Bitmap? = null          //TODO : Implement Blank Image
            if (movieList[i].image != "") {
                val binaryImage = android.util.Base64.decode(movieList[i].image, android.util.Base64.NO_WRAP)
                bMap = BitmapFactory.decodeByteArray(binaryImage, 0, binaryImage.size)
            }
            if (bMap != null)
                imageView.setImageBitmap(bMap)

            val titleView = convertView.findViewById<TextView>(R.id.title)
            titleView.text = movieList[i].title

            val lengthView = convertView.findViewById<TextView>(R.id.length)
            lengthView.text = movieList[i].length


            //TODO: WeakReference(this)
            val timeScheduleListView = convertView.list_schedule
            timeScheduleListView.adapter = ScheduleAdapter(context, googleMovies.getTimeSchedule(movieList[i]))

            convertView.minimumHeight = 350

            return convertView

        }

    }

    class ScheduleAdapter(val context : Context, val shows : ArrayList<Show>) : BaseAdapter(){
        //TODO: think about context

        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return shows.size
        }

        override fun getItem(i : Int): Any {
            return shows[i]
        }

        override fun getItemId(i : Int): Long {
            return shows[i].hashCode().toLong()
        }

        override fun getView(i: Int, _view: View?, _viewGroup: ViewGroup?): View {

            val view : View

            if(_view != null)
                view = _view
            else
                view = layoutInflater.inflate(R.layout.list_schedule , _viewGroup, false)


            //TODO: date everywhere
            val time : DateTime = DateTime(shows[i].timeSchedule)
            view.time_schedule.text = time.hourOfDay.toString() + ":" + time.minuteOfHour.toString()

            return view

        }
    }

}