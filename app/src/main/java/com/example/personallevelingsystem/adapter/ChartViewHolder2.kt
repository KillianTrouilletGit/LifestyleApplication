package com.example.personallevelingsystem.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.SleepTimeDao
import com.example.personallevelingsystem.data.WaterDao
import com.example.personallevelingsystem.model.Sleep
import com.example.personallevelingsystem.model.Water
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class ChartViewHolder2(itemView: View, private val sleepDao: SleepTimeDao) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.findViewById(R.id.title2)
    private val chart: LineChart = itemView.findViewById(R.id.chart2)

    fun bind() {
        title.text = "Temps de sommeil"
        setupChart()
    }

    private fun setupChart() {
        CoroutineScope(Dispatchers.Main).launch {
            val data = withContext(Dispatchers.IO) {
                val sleepDao = sleepDao
                getLast30DaysDataAggregated(sleepDao)
            }
            val lineData = convertDataToEntries(data)
            chart.data = lineData
            configureChartAxes()
            chart.invalidate() // Refresh the chart
        }
    }

    private fun configureChartAxes() {
        val xAxis: XAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = DateValueFormatter()
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(true)
        xAxis.setDrawAxisLine(true)
        xAxis.textColor = Color.WHITE
        xAxis.axisLineColor = Color.WHITE

        val yAxisLeft: YAxis = chart.axisLeft
        yAxisLeft.granularity = 0.01f
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setDrawAxisLine(true)
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.axisLineColor = Color.WHITE

        val yAxisRight: YAxis = chart.axisRight
        yAxisRight.isEnabled = false

        val legend = chart.legend
        legend.isEnabled = true
        legend.form = Legend.LegendForm.LINE
        legend.textColor = Color.WHITE
    }

    private suspend fun getLast30DaysDataAggregated(sleepDao: SleepTimeDao): List<Sleep> {
        val calendar = Calendar.getInstance()
        val endOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startOfDay = calendar.timeInMillis
        val data = sleepDao.getSleepForDay(startOfDay, endOfDay)


        val aggregatedData = data.groupBy {
            // Normaliser la date à minuit pour chaque jour
            TimeUnit.MILLISECONDS.toDays(it.date)
        }.map { entry ->
            val totalAmount = entry.value.map { convertTimeToFloat(it.duration) }.sum()

            val date = entry.value.first().date // Any date within the same day works
            Sleep(date = date, duration = convertFloatToTime(totalAmount))
        }.sortedBy { it.date }

        // Log the aggregated data to verify
        Log.d("AggregatedSleepData", "Aggregated sleep data: $aggregatedData")

        return aggregatedData
    }

    private fun convertDataToEntries(sleepList: List<Sleep>): LineData {
        val entries = mutableListOf<Entry>()
        for (sleep in sleepList) {

            entries.add(Entry(sleep.date.toFloat(), convertTimeToFloat(sleep.duration)))

        }
        if (entries.isEmpty()) {
            // Ajouter une entrée factice pour éviter les problèmes de taille de tableau négative
            entries.add(Entry(0f, 0f))
        }

        // Log the entries to verify
        Log.d("ChartEntries", "Chart entries: $entries")

        val dataSet = LineDataSet(entries, "Sleep Time").apply { color = Color.WHITE}
        return LineData(dataSet)
    }
    private fun convertTimeToFloat(time: String): Float {
        // Split the input time string into hours and minutes
        val parts = time.split(":")
        // Convert hours and minutes to integers
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        Log.d("Temps181002", "Heures: $hours")
        Log.d("Temps181002", "minutes: $minutes")
        // Calculate the float value
        return hours + minutes / 60.0f

    }
    @SuppressLint("DefaultLocale")
    private fun convertFloatToTime(value: Float): String {
        // Extraire les heures
        val hours = value.toInt()
        // Calculer les minutes
        val minutes = ((value - hours) * 60).roundToInt()
        // Formater en "HH:mm"
        val formattedTime = String.format("%02d:%02d", hours, minutes)
        Log.d("Temps181002", "formattedTime: $formattedTime")
        return formattedTime
    }

}