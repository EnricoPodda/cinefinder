package net.confusione.cinefinder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
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
            Log.d("Performance","Download ended in 7s")
            val tmp = weakActivity.get()
            if (tmp != null) {
                val activity : AppCompatActivity = tmp
                val listView : ListView = activity.list_view
                val arrayAdapter = MyAdapter(activity.baseContext, googleMovies)
                listView.adapter = arrayAdapter
            }
        }

    }

    class MyAdapter (context : Context, val googleMovies: GoogleMovies) : BaseAdapter(){

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
                val binaryImage = android.util.Base64.decode(movieList[i].image,android.util.Base64.NO_WRAP)
                bMap = BitmapFactory.decodeByteArray(binaryImage, 0, binaryImage.size)
            }
            if (bMap != null)
                imageView.setImageBitmap(bMap)

            val titleView = convertView.findViewById<TextView>(R.id.title)
            titleView.text = movieList[i].title

            val lengthView = convertView.findViewById<TextView>(R.id.length)
            lengthView.text = movieList[i].length

            val timeSchedule : ArrayList<Show> = googleMovies.getTimeSchedule(movieList[i])
            val timeScheduleView = convertView.findViewById<TextView>(R.id.time_schedule)
            var temp : String = ""
            for (show in timeSchedule){
                val calendar : Calendar = Calendar.getInstance()
                calendar.time = show.timeSchedule
                temp += calendar.get(Calendar.HOUR_OF_DAY).toString()

                if (calendar.get(Calendar.MINUTE) < 10)
                    temp += ":0"+calendar.get(Calendar.MINUTE).toString()
                else
                    temp += ":"+calendar.get(Calendar.MINUTE).toString()

                if (show != timeSchedule.last())
                    temp += " -- "
            }
            timeScheduleView.text = temp

            convertView.minimumHeight = 350

            return convertView

        }

    }

}