package com.project.medikare.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.medikare.HistoryActivity
import com.project.medikare.database.PrescriptionEntity
import com.project.medikare.databinding.ItemListBinding
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator

class HistoryAdapter(
        private val context: HistoryActivity,
        private val items: ArrayList<PrescriptionEntity>
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>()
{

    // Track the expansion state for each item
    private val expandedStates = HashMap<Int, Boolean>()

    // Function to update RecyclerView data dynamically
    fun updateData(newList: ArrayList<PrescriptionEntity>)
    {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged() // Notify the adapter to refresh
    }

    inner class ViewHolder(binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)
    {
        val date = binding.tvDate
        val patientName = binding.tvPatientName
        val doctorName = binding.tvDoctorName
        val additionalInfo = binding.tvAdditionalInfo
        val expandableSection = binding.expandableSection
        val btnExpand = binding.btnExpand
        val table=binding.medTable
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[items.size - 1 - position]

        holder.date.text = "Consultaion Date: ${item.consultationDate}"
        holder.doctorName.text = "Doctor: ${item.doctorName}"
        holder.patientName.text = "Patient: ${item.patientName}"
        holder.additionalInfo.text = "Prescription ID: ${item.prescriptionID}"
        MedicationTableBuilder.build(context,holder.table,item.medications)

        val isExpanded = expandedStates[position] ?: false

        // Set initial state without animation
        holder.expandableSection.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.expandableSection.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.btnExpand.rotation = if (isExpanded) 180f else 0f

        holder.btnExpand.setOnClickListener {
            val currentlyExpanded = expandedStates[position] ?: false
            expandedStates[position] = !currentlyExpanded
            toggleExpansion(holder, !currentlyExpanded)
        }
    }


    override fun getItemCount(): Int = items.size

    private fun toggleExpansion(holder: ViewHolder, expand: Boolean) {
        val expandableView = holder.expandableSection

        if (expand) {
            expandableView.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val targetHeight = expandableView.measuredHeight
            expandableView.layoutParams.height = 0
            expandableView.visibility = View.VISIBLE

            val animator = ValueAnimator.ofInt(0, targetHeight)
            animator.addUpdateListener { animation ->
                expandableView.layoutParams.height = animation.animatedValue as Int
                expandableView.requestLayout()
            }
            animator.duration = 300
            animator.start()
        } else {
            val initialHeight = expandableView.height
            val animator = ValueAnimator.ofInt(initialHeight, 0)
            animator.addUpdateListener { animation ->
                expandableView.layoutParams.height = animation.animatedValue as Int
                expandableView.requestLayout()
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    expandableView.visibility = View.GONE
                }
            })
            animator.duration = 300
            animator.start()
        }

        ObjectAnimator.ofFloat(holder.btnExpand, "rotation", if (expand) 180f else 0f).apply {
            duration = 300
            start()
        }
    }
}
