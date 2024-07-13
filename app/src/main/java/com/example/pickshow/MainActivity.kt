package com.example.pickshow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.pickshow.data.models.Theatre
import com.example.pickshow.ui.activities.GetCinemaActivities
import com.example.pickshow.ui.theme.PickShowTheme
import org.json.JSONObject
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerTheater: Spinner
    private lateinit var spinnerMovie: Spinner
    private lateinit var spinnerDate: Spinner
    private lateinit var spinnerTime: Spinner
    private lateinit var buttonBook: Button

    private lateinit var movies: List<Movie>
    private lateinit var schedules: List<Schedule>
    private lateinit var theatres: List<Theatre>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerTheater = findViewById(R.id.spinner_theater)
        spinnerMovie = findViewById(R.id.spinner_movie)
        spinnerDate = findViewById(R.id.spinner_date)
        spinnerTime = findViewById(R.id.spinner_time)
        buttonBook = findViewById(R.id.button_book)

        val json = loadJSONFromAsset()
        parseJSON(json)

        val theaterNames = theatres.map { it.theatreName }
        val theaterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, theaterNames)
        spinnerTheater.adapter = theaterAdapter

        val movieNames = movies.map { it.filmName }
        val movieAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, movieNames)
        spinnerMovie.adapter = movieAdapter

        val hintMovie = "Select a movie"
        movieAdapter.insert(hintMovie, 0)
        spinnerMovie.adapter = movieAdapter
        spinnerMovie.setSelection(0)

        spinnerMovie.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    spinnerDate.setSelection(0)
                    spinnerTime.setSelection(0)
                    spinnerDate.visibility = View.GONE
                    spinnerTime.visibility = View.GONE
                } else {
                    spinnerDate.visibility = View.VISIBLE
                    spinnerTime.visibility = View.VISIBLE
                    val selectedMovie = movies[position - 1]
                    val availableDates = getAvailableDates(selectedMovie.filmcommonId)
                    val dateAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, availableDates)
                    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerDate.adapter = dateAdapter
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDate = spinnerDate.selectedItem as String
                val selectedMovie = movies[spinnerMovie.selectedItemPosition - 1] // Adjust for hint
                val availableTimes = getAvailableTimes(selectedMovie.filmcommonId, selectedDate)
                val timeAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, availableTimes)
                timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTime.adapter = timeAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                buttonBook.isEnabled = true
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        buttonBook.setOnClickListener {
            val selectedMoviePosition = spinnerMovie.selectedItemPosition
            val selectedMovie = movies[selectedMoviePosition - 1]
                val selectedDate = spinnerDate.selectedItem as String
                val selectedTime = spinnerTime.selectedItem as String
                val selectedTheatre = spinnerTheater.selectedItem as String

                val intent = Intent(this, GetCinemaActivities::class.java)
                intent.putExtra("MOVIE_NAME", selectedMovie.filmName)
                intent.putExtra("THEATRE_NAME", selectedTheatre)
                intent.putExtra("SELECTED_DATE", selectedDate)
                intent.putExtra("SELECTED_TIME", selectedTime)
                startActivity(intent)
        }
    }

    private fun loadJSONFromAsset(): String {
        val inputStream: InputStream = assets.open("getcinema.json")
        return inputStream.bufferedReader().use { it.readText() }
    }

    private fun parseJSON(json: String) {
        val jsonObject = JSONObject(json)
        val movieDetailsArray = jsonObject.getJSONArray("movieDetails")
        movies = (0 until movieDetailsArray.length()).map { i ->
            val movieObject = movieDetailsArray.getJSONObject(i)
            Movie(
                filmcommonId = movieObject.getString("filmcommonId"),
                filmName = movieObject.getString("filmName")
            )
        }

        val schedulesArray = jsonObject.getJSONArray("schedules")
        schedules = (0 until schedulesArray.length()).map { i ->
            val scheduleObject = schedulesArray.getJSONObject(i)
            val day = scheduleObject.getString("day")
            val showTimingsArray = scheduleObject.getJSONArray("showTimings")
            val showTimings = (0 until showTimingsArray.length()).map { j ->
                val showTimingArray = showTimingsArray.getJSONArray(j)
                ShowTiming(
                    filmCommonId = showTimingArray.getString(1),
                    date = showTimingArray.getString(2),
                    time = showTimingArray.getString(2).substring(11, 16)
                )
            }
            Schedule(day = day, showTimings = showTimings)
        }

        val theatresArray = jsonObject.getJSONArray("theatres")
        theatres = (0 until theatresArray.length()).map { i ->
            val theatreObject = theatresArray.getJSONObject(i)
            Theatre(
                id = theatreObject.getString("_id"),
                theatreName = theatreObject.getString("TheatreName")
            )
        }
    }

    private fun getAvailableDates(filmcommonId: String): List<String> {
        return schedules.filter { schedule ->
            schedule.showTimings.any { it.filmCommonId == filmcommonId }
        }.map { it.day }.distinct()
    }

    private fun getAvailableTimes(filmcommonId: String, date: String): List<String> {
        return schedules.find { it.day == date }?.showTimings?.filter {
            it.filmCommonId == filmcommonId
        }?.map { it.time } ?: emptyList()
    }

    data class Movie(val filmcommonId: String, val filmName: String)
    data class Schedule(val day: String, val showTimings: List<ShowTiming>)
    data class ShowTiming(val filmCommonId: String, val date: String, val time: String)
}