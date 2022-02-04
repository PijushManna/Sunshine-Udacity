package com.example.sunshine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.example.sunshine.data.SunshinePreferences
import com.example.sunshine.utils.NetworkUtils
import com.example.sunshine.utils.OpenWeatherJsonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    lateinit var tvTest: TextView
    private lateinit var pbLoadingProgressBar:ProgressBar
    private lateinit var tvErrorMessage:TextView

    private val mockData = arrayListOf(
        "Good", "Sunny", "Rainy", "Cloudy"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTest = findViewById(R.id.tv_weather_data)
        pbLoadingProgressBar = findViewById(R.id.pbr_loading_data)
        tvErrorMessage = findViewById(R.id.tv_error_message)
        loadWeatherData()
    }

    private fun loadWeatherData(){
        updateUiForLoadingData()
        CoroutineScope(Dispatchers.IO).launch{
            val location = SunshinePreferences.getPreferredWeatherLocation(this@MainActivity)
            if (location.isEmpty()){
                return@launch
            }
            try {
                val weatherRequestUrl = NetworkUtils.buildUrl(location)
                val jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl)
                val simpleJsonWeatherData = OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(this@MainActivity,jsonWeatherResponse)
                Log.i(TAG, "fetchWeatherJob: $simpleJsonWeatherData")
                withContext(Dispatchers.Main){
                    simpleJsonWeatherData.forEach {
                        tvTest.append("$it\n\n\n")
                    }
                    updateUiOnSuccess()
                }
            } catch (e:Exception){
                withContext(Dispatchers.Main){
                    updateUiOnError(e.localizedMessage)
                }
                return@launch
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.forecast_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_refresh -> {
                refreshOldData()
            }
        }
        return true
    }

    private fun refreshOldData() {
        tvTest.text = ""
        loadWeatherData()
    }

    private fun updateUiForLoadingData(){
        pbLoadingProgressBar.visibility = View.VISIBLE
        tvTest.visibility = View.INVISIBLE
        tvErrorMessage.visibility = View.INVISIBLE
    }

    private fun updateUiOnSuccess(){
        pbLoadingProgressBar.visibility = View.INVISIBLE
        tvTest.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.INVISIBLE
    }

    private fun updateUiOnError(localizedMessage: String?) {
        pbLoadingProgressBar.visibility = View.INVISIBLE
        tvTest.visibility = View.INVISIBLE
        tvErrorMessage.visibility = View.VISIBLE
        tvErrorMessage.text = localizedMessage
    }

}