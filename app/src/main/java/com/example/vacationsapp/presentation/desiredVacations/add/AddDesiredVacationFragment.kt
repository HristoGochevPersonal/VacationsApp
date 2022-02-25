package com.example.vacationsapp.presentation.desiredVacations.add

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.vacationsapp.databinding.FragmentAddDesiredVacationBinding
import com.example.vacationsapp.presentation.utils.collectLatestLifecycleFlow
import com.google.android.material.snackbar.Snackbar

class AddDesiredVacationFragment : Fragment() {
    private var _binding: FragmentAddDesiredVacationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddDesiredVacationViewModel by viewModels()

    // Image picker contract
    private val imagePickerContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // If image was successfully selected
            if (it.resultCode == RESULT_OK) {
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

        // Initializing the view model observers
        initViewModelObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDesiredVacationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.imagePickerHolder.setOnClickListener {
            // Opens an image picker browser
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"

            imagePickerContract.launch(intent)
        }

        binding.createButton.setOnClickListener {
            // Attempts creating of a new vacation with the given data
            val name = binding.inputName.text.toString()
            val hotelName = binding.inputHotelName.text.toString()
            val location = binding.inputLocation.text.toString()
            val cost = binding.inputCost.text.toString()
            val description = binding.inputDescription.text.toString()
            viewModel.createDesiredVacation(name, hotelName, location, cost, description)
        }
        binding.cancelButton.setOnClickListener {
            // Navigates back
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViewModelObservers() {
        // Observes the image uri updates
        collectLatestLifecycleFlow(viewModel.imagePathStateFlow) {
            it?.let { uri ->
                binding.inputImage.setImageURI(uri)
            }
        }

        // Observes the vacation creation shared flow
        collectLatestLifecycleFlow(viewModel.creationSharedFlow) {
            if (it) {
                // Navigating back if the creation succeeded
                findNavController().popBackStack()
            } else {
                // Displaying a snackbar if the creation failed
                val text = "Could not create desired vacation"
                Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}