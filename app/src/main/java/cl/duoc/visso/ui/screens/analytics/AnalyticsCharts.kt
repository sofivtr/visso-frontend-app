package cl.duoc.visso.ui.screens.analytics

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cl.duoc.visso.data.model.ProductSales
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/* ===================================================
   ANALYTICS CHARTS – MPAndroidChart
=================================================== */

@Composable
fun TopProductsChart(data: List<ProductSales>) {
    if (data.isEmpty()) return

    val chartData = data.take(5)
    val entries = chartData.mapIndexed { index, item ->
        BarEntry(index.toFloat(), item.quantity.toFloat())
    }

    val labels = chartData.map { it.productName }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Top productos por cantidad", style = MaterialTheme.typography.titleMedium)
            Text("Unidades vendidas (Top 5)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        setDrawValueAboveBar(true)
                        description.isEnabled = false
                        legend.isEnabled = false
                        setPinchZoom(false)

                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        xAxis.setDrawGridLines(false)
                        xAxis.granularity = 1f
                        xAxis.labelRotationAngle = -30f

                        axisRight.isEnabled = false
                        axisLeft.axisMinimum = 0f
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                update = { chart ->
                    val dataSet = BarDataSet(entries, "Cantidad")
                    dataSet.color = AndroidColor.parseColor("#4F46E5") // Indigo
                    dataSet.valueTextColor = AndroidColor.BLACK
                    dataSet.valueTextSize = 12f

                    val barData = BarData(dataSet)
                    barData.barWidth = 0.6f

                    chart.data = barData
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun SalesByProductChart(data: List<ProductSales>) {
    if (data.isEmpty()) return

    val chartData = data.take(5)
    val entries = chartData.mapIndexed { index, item ->
        BarEntry(index.toFloat(), item.totalSales.toFloat())
    }

    val labels = chartData.map { it.productName }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ventas por producto", style = MaterialTheme.typography.titleMedium)
            Text("Monto total vendido (Top 5)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        setDrawValueAboveBar(true)
                        description.isEnabled = false
                        legend.isEnabled = false
                        setPinchZoom(false)

                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        xAxis.setDrawGridLines(false)
                        xAxis.granularity = 1f
                        xAxis.labelRotationAngle = -30f

                        axisRight.isEnabled = false
                        axisLeft.axisMinimum = 0f
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                update = { chart ->
                    val dataSet = BarDataSet(entries, "Ventas")
                    dataSet.color = AndroidColor.parseColor("#22C55E") // Verde
                    dataSet.valueTextColor = AndroidColor.BLACK
                    dataSet.valueTextSize = 12f

                    val barData = BarData(dataSet)
                    barData.barWidth = 0.6f

                    chart.data = barData
                    chart.invalidate()
                }
            )
        }
    }
}
