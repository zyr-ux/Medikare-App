package kiit.project.kimsmedicineapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.RecyclerView
import kiit.project.kimsmedicineapp.database.KIMSDao
import kiit.project.kimsmedicineapp.database.PatientEntity
import kiit.project.kimsmedicineapp.databinding.RvAdminItemViewBinding
import java.util.ArrayList
import java.util.Locale

class AdminPageAdapter(private val context:Context,private val items:ArrayList<PatientEntity>)
    :RecyclerView.Adapter<AdminPageAdapter.Viewholder>() {

    class Viewholder(binding: RvAdminItemViewBinding):RecyclerView.ViewHolder(binding.root)
    {
        val rv_id=binding.idNumber
        val rv_date=binding.date
        val rv_patient_name=binding.patientName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        return Viewholder(RvAdminItemViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
       return items.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item=items[position]
        holder.rv_id.text=String.format(Locale.getDefault(),item.id.toString())
        holder.rv_date.text=item.date
        holder.rv_patient_name.text=item.patientname
    }

    suspend fun deleteItem(kimsDao: KIMSDao,position:Int)
    {
        kimsDao.delete(items[position])
    }
}