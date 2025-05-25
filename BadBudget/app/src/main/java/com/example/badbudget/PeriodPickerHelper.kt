package com.example.badbudget

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*
// helper object for period picker functionality
object PeriodPickerHelper {

    data class Range(var start: String, var end: String) // stores selected date range

    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // hooks the period picker to the view
    fun hook(root: View, ctx: Context, initial: Range, onRange: (Range) -> Unit) {

        var pick = initial
        // Shows DatePickerDialog to choose a date, and updates start/end date values
        fun show(isStart: Boolean) {
            val cal = Calendar.getInstance()
            DatePickerDialog(ctx, { _, y, m, d ->
                cal.set(y, m, d)
                val date = fmt.format(cal.time)
                if (isStart) pick = pick.copy(start = date) else pick = pick.copy(end = date)
                val btnId = if (isStart) R.id.btnStart else R.id.btnEnd
                root.findViewById<MaterialButton>(btnId).text = date
            },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // initialize the Start Date button
        root.findViewById<MaterialButton>(R.id.btnStart).apply {
            text = initial.start
            setOnClickListener { show(true) }
        }
        root.findViewById<MaterialButton>(R.id.btnEnd).apply {
            text = initial.end
            setOnClickListener { show(false) }
        }
        root.findViewById<View>(R.id.btnApply).setOnClickListener { onRange(pick) }
    }
}
