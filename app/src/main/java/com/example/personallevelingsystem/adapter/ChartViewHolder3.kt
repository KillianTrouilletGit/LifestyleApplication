package com.example.personallevelingsystem.adapter

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.AppDatabase
import com.example.personallevelingsystem.data.EnduranceTrainingDao
import com.example.personallevelingsystem.data.WaterDao
import com.example.personallevelingsystem.model.EnduranceTraining
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ChartViewHolder3(itemView: View, private val enduranceDao: EnduranceTrainingDao) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.findViewById(R.id.title3)
    private val chart: LineChart = itemView.findViewById(R.id.chart3)

    fun bind() {
        title.text = "Distance de course"
        setupChart()
    }

    private fun setupChart() {
        CoroutineScope(Dispatchers.Main).launch {
            val data = withContext(Dispatchers.IO) {
                val enduranceDao = enduranceDao
                getLast30WeeksDistanceAggregated(enduranceDao)
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

    private suspend fun getLast30WeeksDistanceAggregated(enduranceDao: EnduranceTrainingDao): List<EnduranceTraining> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val endOfWeek = calendar.timeInMillis
        calendar.add(Calendar.WEEK_OF_YEAR, -30)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startOfWeek = calendar.timeInMillis
        val data = enduranceDao.getEnduranceForWeek(startOfWeek, endOfWeek)



        val aggregatedData = data.groupBy {
            // Normaliser la date à la semaine
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.WEEK_OF_YEAR)
        }.map { entry ->
            val totalAmount = entry.value.sumOf { it.distance.toDouble() }.toFloat()
            val date = entry.value.first().date // Any date within the same week works
            val duration = entry.value.first().duration
            EnduranceTraining(date = date, duration = duration, distance = totalAmount)
        }.sortedBy { it.date }

        // Log the aggregated data to verify
        Log.d("AggregatedEnduranceData", "Aggregated endurance data: $aggregatedData")

        return aggregatedData
    }

    private fun convertDataToEntries(enduranceList: List<EnduranceTraining>): LineData {
        val entries = mutableListOf<Entry>()
        for (endurance in enduranceList) {
            if (endurance.distance >= 0) {
                entries.add(Entry(endurance.date.toFloat(), endurance.distance))
            }
        }
        if (entries.isEmpty()) {
            // Ajouter une entrée factice pour éviter les problèmes de taille de tableau négative
            entries.add(Entry(0f, 0f))
        }

        // Log the entries to verify
        Log.d("ChartEntries", "Chart entries: $entries")

        val dataSet = LineDataSet(entries, "Run distance").apply { color = Color.WHITE}
        return LineData(dataSet)
    }
}


// Similar classes for ChartViewHolder2, ChartViewHolder3, etc.
