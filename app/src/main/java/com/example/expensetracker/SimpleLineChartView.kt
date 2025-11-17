package com.example.expensetracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SimpleLineChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var incomeData: List<Double> = emptyList()
    private var expenseData: List<Double> = emptyList()

    private val linePaintIncome = Paint().apply {
        color = Color.GREEN
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val linePaintExpense = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val axisPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 2f
    }

    fun setData(income: List<Double>, expense: List<Double>) {
        incomeData = income
        expenseData = expense
        invalidate() // redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 50f

        // Draw X and Y axes
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint)
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint)

        if (incomeData.isEmpty() || expenseData.isEmpty()) return

        // Find max value for scaling
        val maxIncome = incomeData.maxOrNull() ?: 0.0
        val maxExpense = expenseData.maxOrNull() ?: 0.0
        val maxY = maxOf(maxIncome, maxExpense, 1.0) // avoid divide by zero

        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding
        val pointGap = chartWidth / (incomeData.size - 1).coerceAtLeast(1)

        // Draw lines
        for (i in 0 until incomeData.size - 1) {
            val x1 = padding + i * pointGap
            val x2 = padding + (i + 1) * pointGap

            val y1Income = height - padding - (incomeData[i] / maxY * chartHeight).toFloat()
            val y2Income = height - padding - (incomeData[i + 1] / maxY * chartHeight).toFloat()
            canvas.drawLine(x1, y1Income, x2, y2Income, linePaintIncome)

            val y1Expense = height - padding - (expenseData[i] / maxY * chartHeight).toFloat()
            val y2Expense = height - padding - (expenseData[i + 1] / maxY * chartHeight).toFloat()
            canvas.drawLine(x1, y1Expense, x2, y2Expense, linePaintExpense)
        }
    }
}
