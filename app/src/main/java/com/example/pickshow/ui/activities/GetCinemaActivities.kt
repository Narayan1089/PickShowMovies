package com.example.pickshow.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pickshow.R

class GetCinemaActivities : AppCompatActivity() {
    private lateinit var textViewMovie: TextView
    private lateinit var textViewDate: TextView
    private lateinit var textViewTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_cinema_activities)
        textViewMovie = findViewById(R.id.textViewMovie)
        textViewDate = findViewById(R.id.textViewDate)
        textViewTime = findViewById(R.id.textViewTime)

        val movieName = intent.getStringExtra("MOVIE_NAME")
        val selectedDate = intent.getStringExtra("SELECTED_DATE")
        val selectedTime = intent.getStringExtra("SELECTED_TIME")
        val theatreName = intent.getStringExtra("THEATRE_NAME")

        supportActionBar?.title = theatreName ?: "Theatre"

        "Selected Movie: $movieName".also { textViewMovie.text = it }
        "Selected Date: $selectedDate".also { textViewDate.text = it }
        textViewTime.text = "Selected Time: $selectedTime"
    }
}