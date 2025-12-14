package com.example.personallevelingsystem.adapter

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.FlexibilityTrainingDao
import com.example.personallevelingsystem.model.FlexibilityTraining
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
import java.util.*
import java.util.concurrent.TimeUnit

class ChartViewHolder5(itemView: View, private val flexDao: FlexibilityTrainingDao) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.findViewById(R.id.title5)
    private val chart: LineChart = itemView.findViewById(R.id.chart5)

    fun bind() {
        title.text = "Flexibility training time"
        setupChart()
    }

    private fun setupChart() {
        CoroutineScope(Dispatchers.Main).launch {
            val data = withContext(Dispatchers.IO) {
                val enduranceDao = flexDao
                getLast30DaysTimeAggregated(flexDao)
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

    private suspend fun getLast30DaysTimeAggregated(flexDao: FlexibilityTrainingDao): List<FlexibilityTraining> {
        val calendar = Calendar.getInstance()
        val endOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startOfDay = calendar.timeInMillis
        val rawData = flexDao.getFlexForDay(startOfDay, endOfDay)

        // Agréger les données par jour
        val aggregatedData = rawData.groupBy {
            // Normaliser la date à minuit pour chaque jour
            TimeUnit.MILLISECONDS.toDays(it.date)
        }.map { entry ->
            val totalAmount = entry.value.sumOf { it.duration }
            val amountInMinutes = totalAmount / 60000
            val date = entry.value.first().date // Any date within the same day works
            FlexibilityTraining(date = date, duration = amountInMinutes)
        }.sortedBy { it.date }

        // Log the aggregated data to verify
        Log.d("AggregatedFlexData", "Aggregated flex data: $aggregatedData")

        return aggregatedData
    }

    private fun convertDataToEntries(flexList: List<FlexibilityTraining>): LineData {
        val entries = mutableListOf<Entry>()
        for (flex in flexList) {
            if (flex.duration >= 0) {
                entries.add(Entry(flex.date.toFloat(), flex.duration.toFloat()))
            }
        }
        if (entries.isEmpty()) {
            // Ajouter une entrée factice pour éviter les problèmes de taille de tableau négative
            entries.add(Entry(0f, 0f))
        }

        // Log the entries to verify
        Log.d("ChartEntries", "Chart entries: $entries")

        val dataSet = LineDataSet(entries, "Flexibility training time").apply { color = Color.WHITE}
        return LineData(dataSet)
    }
}


// Similar classes for ChartViewHolder2, ChartViewHolder3, etc.
