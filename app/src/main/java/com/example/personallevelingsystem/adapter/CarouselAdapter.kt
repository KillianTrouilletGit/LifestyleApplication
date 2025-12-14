package com.example.personallevelingsystem.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.personallevelingsystem.R
import com.example.personallevelingsystem.data.EnduranceTrainingDao
import com.example.personallevelingsystem.data.FlexibilityTrainingDao
import com.example.personallevelingsystem.data.MealDao
import com.example.personallevelingsystem.data.SleepTimeDao
import com.example.personallevelingsystem.data.TrainingSessionDao
import com.example.personallevelingsystem.data.WaterDao
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

// Formatteur pour afficher les dates sur l'axe X
class DateValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val date = Date(value.toLong())
        val format = SimpleDateFormat("dd MMM", Locale.getDefault())
        return format.format(date)
    }
}

class CarouselAdapter(private val waterDao: WaterDao,private val sleepDao: SleepTimeDao,private val enduranceDao: EnduranceTrainingDao,private val sessionDao: TrainingSessionDao,private val mealDao: MealDao,private val flexDao: FlexibilityTrainingDao) :  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_CHART1 = 0
        const val VIEW_TYPE_CHART2 = 1
        const val VIEW_TYPE_CHART3 = 2
        const val VIEW_TYPE_CHART4 = 3
        const val VIEW_TYPE_CHART5 = 4
        const val VIEW_TYPE_CHART6 = 5
        const val VIEW_TYPE_CHART7 = 6
    }
    override fun getItemViewType(position: Int): Int {
        return position % 7 // Modulo 7 for seven different charts
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CHART1 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel_chart1, parent, false)
                ChartViewHolder1(view, waterDao)
            }
            VIEW_TYPE_CHART2 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel_chart2, parent, false)
                ChartViewHolder2(view, sleepDao)
            }
            VIEW_TYPE_CHART3 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel_chart3, parent, false)
                ChartViewHolder3(view, enduranceDao)
            }
            VIEW_TYPE_CHART4 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel_chart4, parent, false)
                ChartViewHolder4(view, sessionDao)
            }
            VIEW_TYPE_CHART5 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel_chart5, parent, false)
                ChartViewHolder5(view, flexDao)
            }
            VIEW_TYPE_CHART6 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel_chart6, parent, false)
                ChartViewHolder6(view, mealDao)
            }
            VIEW_TYPE_CHART7 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel_chart7, parent, false)
                ChartViewHolder7(view, mealDao)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_CHART1 -> (holder as ChartViewHolder1).bind()
            VIEW_TYPE_CHART2 -> (holder as ChartViewHolder2).bind()
            VIEW_TYPE_CHART3 -> (holder as ChartViewHolder3).bind()
            VIEW_TYPE_CHART4 -> (holder as ChartViewHolder4).bind()
            VIEW_TYPE_CHART5 -> (holder as ChartViewHolder5).bind()
            VIEW_TYPE_CHART6 -> (holder as ChartViewHolder6).bind()
            VIEW_TYPE_CHART7 -> (holder as ChartViewHolder7).bind()
        }
    }


    override fun getItemCount(): Int {
        return Integer.MAX_VALUE // Rendre le carrousel infini
    }


}
