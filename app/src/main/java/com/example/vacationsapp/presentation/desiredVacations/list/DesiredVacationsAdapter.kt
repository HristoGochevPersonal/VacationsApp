package com.example.vacationsapp.presentation.desiredVacations.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.vacationsapp.databinding.DesiredVacationPreviewBinding
import com.example.vacationsapp.presentation.desiredVacations.DesiredVacationModel

// The DesiredVacations recycler view adapter
// Requires 2 interactions, for single tap and long press
class DesiredVacationsAdapter(
    private val onSingleTap: ((Int, DesiredVacationModel) -> Unit)?,
    private val onLongPress: ((Int, DesiredVacationModel) -> Unit)?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Item difference callback for checking the differences in a newly inserted list and an old one
    private val diffCallback = object : DiffUtil.ItemCallback<DesiredVacationModel>() {
        override fun areItemsTheSame(
            oldItem: DesiredVacationModel,
            newItem: DesiredVacationModel
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: DesiredVacationModel,
            newItem: DesiredVacationModel
        ): Boolean {
            return when {
                oldItem.name != newItem.name -> false
                oldItem.hotelName != newItem.hotelName -> false
                oldItem.location != newItem.location -> false
                oldItem.cost != newItem.cost -> false
                oldItem.description != newItem.description -> false
                oldItem.imagePath != newItem.imagePath -> false
                else -> true
            }
        }
    }

    // Allows for the execution of the item difference callback on a background thread
    private val differ = AsyncListDiffer(this, diffCallback)

    // Standard adapter binding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DesiredVacationPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DesiredVacationViewHolder(binding, onSingleTap, onLongPress)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DesiredVacationViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<DesiredVacationModel>) {
        differ.submitList(list)
    }

    // DesiredVacations recycler view item view holder
    class DesiredVacationViewHolder constructor(
        private var binding: DesiredVacationPreviewBinding,
        private val onSingleTap: ((Int, DesiredVacationModel) -> Unit)?,
        private val onLongPress: ((Int, DesiredVacationModel) -> Unit)?
    ) :
        RecyclerView.ViewHolder(binding.root) {

        // Contains a model it should be associated it
        private lateinit var desiredVacationModel: DesiredVacationModel

        // Function for binding
        fun bind(desiredVacationModel: DesiredVacationModel) {
            this.desiredVacationModel = desiredVacationModel
            with(desiredVacationModel){
                binding.nameTextView.text = name
                binding.locationTextView.text = location
                binding.vacationImage.setImageURI(imagePath)
            }
            binding.root.clipToOutline = true
            binding.root.setOnClickListener {
                onSingleTap?.let { it(adapterPosition, desiredVacationModel) }
            }
            binding.root.setOnLongClickListener {
                onLongPress?.let { it(adapterPosition, desiredVacationModel) }
                true
            }
        }
    }
}