package com.project.medikare.utils

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.project.medikare.R
import com.project.medikare.database.Medication

object MedicationTableBuilder
{

    fun build(context: Context, tableLayout: TableLayout, medications: List<Medication>)
    {
        tableLayout.removeAllViews()

        // Header
        val headerRow = TableRow(context)
        val headers = listOf("Medicine", "Dosage", "Time")
        headers.forEach { title ->
            headerRow.addView(createCell(context, title, isHeader = true))
        }
        tableLayout.addView(headerRow)

        // Rows
        medications.forEach { med ->
            val row = TableRow(context)
            val values = listOf(med.medicine, med.dosage, med.details1)
            values.forEach { value ->
                row.addView(createCell(context, value, isHeader = false))
            }
            tableLayout.addView(row)
        }
    }

    private fun createCell(context: Context, text: String, isHeader: Boolean): TextView
    {
        return TextView(context).apply {
            this.text = text
            gravity = Gravity.START
            setPadding(8, 6, 8, 6)
            background = ContextCompat.getDrawable(context, R.drawable.cell_border)

            // Material You text style
            setTextAppearance(
                if (isHeader)
                    com.google.android.material.R.style.TextAppearance_Material3_TitleSmall
                else
                    com.google.android.material.R.style.TextAppearance_Material3_BodySmall
            )

            // Auto-size config
            setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
    }
}
