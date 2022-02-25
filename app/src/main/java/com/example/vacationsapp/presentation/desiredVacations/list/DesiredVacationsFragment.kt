package com.example.vacationsapp.presentation.desiredVacations.list

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vacationsapp.databinding.FragmentDesiredVacationsBinding
import com.example.vacationsapp.presentation.desiredVacations.DesiredVacationModel
import com.example.vacationsapp.presentation.utils.collectLatestLifecycleFlow
import com.google.android.material.snackbar.Snackbar

class DesiredVacationsFragment : Fragment() {
    private var _binding: FragmentDesiredVacationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DesiredVacationsViewModel by viewModels()
    private lateinit var adapter: DesiredVacationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initializing the notification channel
        createNotificationChannel()

        // Initializing the view model observers
        initViewModelObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDesiredVacationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // Initializing the recycler view
        initRecyclerView()



        // When the add button is pressed
        binding.vacationAddButton.setOnClickListener {
            // Navigate to the add desired vacation fragment
            val action =
                DesiredVacationsFragmentDirections.actionDesiredVacationsFragmentToAddDesiredVacationFragment()
            findNavController().navigate(action)
        }

        // Refreshes the recycler view on creation of the main view
        viewModel.refreshDesiredVacations()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Initializes the recycler view
    private fun initRecyclerView() {

        // When the user single taps on a vacation
        val onSingleTap = { _: Int, model: DesiredVacationModel ->
            // Navigate to a fragment containing the full information of the vacation
            val action =
                DesiredVacationsFragmentDirections.actionDesiredVacationsFragmentToDesiredVacationDetailsFragment(
                    model.id
                )
            findNavController().navigate(action)
        }

        // When the user long presses a vacation
        val onLongPress = { _: Int, model: DesiredVacationModel ->
            // Open prompt about the deletion of the vacation
            deleteDesiredVacation(model)
        }

        adapter = DesiredVacationsAdapter(onSingleTap, onLongPress)

        binding.vacationsRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(this.context)
            this.adapter = this@DesiredVacationsFragment.adapter
        }
    }

    // Setting up the view model observers
    private fun initViewModelObservers() {

        // Observing the vacations state flow
        collectLatestLifecycleFlow(viewModel.desiredVacationsStateFlow) {
            adapter.submitList(it)
        }

        // Observing the deletion shared flow
        collectLatestLifecycleFlow(viewModel.deletionSharedFlow) {
            // Displaying a snackbar whenever an update occurs
            val text = if (it.first) "Successfully deleted ${it.second.name}"
            else "Could not delete ${it.second.name}"

            Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()

            // Refreshing the recycler view with new data
            viewModel.refreshDesiredVacations()
        }
    }


    // Displays the delete vacation confirmation dialog
    private fun deleteDesiredVacation(desiredVacationModel: DesiredVacationModel) {
        val builder = AlertDialog.Builder(requireActivity()).apply {
            setTitle("Confirm")
            setMessage("Delete ${desiredVacationModel.name}?")
            setCancelable(false)
            setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                viewModel.deleteDesiredVacation(desiredVacationModel)
            }
            setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }

    // Creating a notification channel for the vacation reminders
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Vacation notifications channel"
            val description = "Channel for vacation notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("vacations", name, importance)
            channel.description = description

            val notificationManager: NotificationManager =
                ContextCompat.getSystemService(requireContext(), NotificationManager::class.java)
                    ?: return
            notificationManager.createNotificationChannel(channel)
        }
    }
}