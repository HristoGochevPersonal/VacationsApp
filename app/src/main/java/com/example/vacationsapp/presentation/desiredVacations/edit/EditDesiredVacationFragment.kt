package com.example.vacationsapp.presentation.desiredVacations.edit

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.vacationsapp.databinding.FragmentEditDesiredVacationBinding
import com.example.vacationsapp.presentation.utils.collectLatestLifecycleFlow
import com.example.vacationsapp.presentation.utils.uriIsValid
import com.google.android.material.snackbar.Snackbar

class EditDesiredVacationFragment : Fragment() {

    private var _binding: FragmentEditDesiredVacationBinding? = null
    private val binding get() = _binding!!
    private val args: EditDesiredVacationFragmentArgs by navArgs()
    private lateinit var viewModel: EditDesiredVacationViewModel

    // Image picker contract
    private val imagePickerContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // If image was successfully selected
            if (it.resultCode == Activity.RESULT_OK) {
                // Get it's permanent uri
                val imageUri = it.data?.data ?: return@registerForActivityResult
                requireActivity().contentResolver.takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                // Add it to the view model
                viewModel.setImageUri(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creating the view model factory for the view model
        val viewModelFactory = EditDesiredVacationViewModelFactory(args.desiredVacationModel)

        // Creating the view model
        viewModel =
            ViewModelProvider(this, viewModelFactory)[EditDesiredVacationViewModel::class.java]

        // Initializing the view model observers
        initViewModelObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDesiredVacationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // Updates the view with info from the model
        with(args.desiredVacationModel) {
            binding.editName.setText(name, TextView.BufferType.EDITABLE)
            binding.editHotelName.setText(hotelName, TextView.BufferType.EDITABLE)
            binding.editLocation.setText(location, TextView.BufferType.EDITABLE)
            binding.editCost.setText(cost.toString(), TextView.BufferType.EDITABLE)
            binding.editDescription.setText(description, TextView.BufferType.EDITABLE)
        }

        binding.deleteButton.setOnClickListener {
            deleteDesiredVacation()
        }
        binding.editImagePickerHolder.setOnClickListener {
            // Opens an image picker browser
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"

            imagePickerContract.launch(intent)
        }

        binding.cancelButton.setOnClickListener {
            // Navigates back
            findNavController().popBackStack()
        }

        binding.saveButton.setOnClickListener {
            // Attempts updating the current model with new data
            val name = binding.editName.text.toString()
            val hotelName = binding.editHotelName.text.toString()
            val location = binding.editLocation.text.toString()
            val cost = binding.editCost.text.toString()
            val description = binding.editDescription.text.toString()
            viewModel.editDesiredVacation(name, hotelName, location, cost, description)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Setting up the view model observers
    private fun initViewModelObservers() {

        // Observes the image uri updates
        collectLatestLifecycleFlow(viewModel.imagePathStateFlow) {
            if (requireActivity().uriIsValid(it)) {
                binding.editImage.setImageURI(it)
            }
        }

        // Observes the edit shared flow
        collectLatestLifecycleFlow(viewModel.editSharedFlow) {
            if (it) {
                // Navigating back if the editing succeeded
                findNavController().popBackStack()
            } else {
                // Displaying a snackbar if the editing failed
                val text = "Could not update desired vacation"
                Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Observing the deletion shared flow
        collectLatestLifecycleFlow(viewModel.deletionSharedFlow) {
            if (it) {
                // Navigating back if the deletion succeeded
                findNavController().popBackStack()
                findNavController().popBackStack()
            } else {
                // Displaying a snackbar if the deletion failed
                val text = "Could not delete desired vacation"
                Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // Displays the delete vacation confirmation dialog
    private fun deleteDesiredVacation() {
        val builder = AlertDialog.Builder(requireActivity()).apply {
            setTitle("Confirm")
            setMessage("Delete ${args.desiredVacationModel.name}?")
            setCancelable(false)
            setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                viewModel.deleteDesiredVacation()
            }
            setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }
}