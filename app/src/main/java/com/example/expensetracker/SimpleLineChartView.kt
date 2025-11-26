package com.example.expensetracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SimpleLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var incomeData: List<Float> = emptyList()
    private var expenseData: List<Float> = emptyList()

    private val incomePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val expensePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F44336")
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 30f
    }

    fun setData(income: List<Float>, expense: List<Float>) {
        incomeData = income
        expenseData = expense
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {  // <-- make Canvas non-null
        super.onDraw(canvas)
        if (incomeData.isEmpty() && expenseData.isEmpty()) return

        val padding = 50f
        val width = width.toFloat() - 2 * padding
        val height = height.toFloat() - 2 * padding
        val maxVal = maxOf(
            incomeData.maxOrNull() ?: 0f,
            expenseData.maxOrNull() ?: 0f,
            1f
        )

        // Draw axes
        canvas.drawLine(padding, height + padding, width + padding, height + padding, axisPaint) // X
        canvas.drawLine(padding, padding, padding, height + padding, axisPaint) // Y

        // Draw income line
        for (i in 1 until incomeData.size) {
            val x1 = padding + (i - 1) * width / (incomeData.size - 1)
            val y1 = padding + height - (incomeData[i - 1] / maxVal) * height
            val x2 = padding + i * width / (incomeData.size - 1)
            val y2 = padding + height - (incomeData[i] / maxVal) * height
            canvas.drawLine(x1, y1, x2, y2, incomePaint)
        }

        // Draw expense line
        for (i in 1 until expenseData.size) {
            val x1 = padding + (i - 1) * width / (expenseData.size - 1)
            val y1 = padding + height - (expenseData[i - 1] / maxVal) * height
            val x2 = padding + i * width / (expenseData.size - 1)
            val y2 = padding + height - (expenseData[i] / maxVal) * height
            canvas.drawLine(x1, y1, x2, y2, expensePaint)
        }

        // Optional: draw max label
        canvas.drawText("Max: $maxVal", padding, padding - 10f, textPaint)
    }
}
