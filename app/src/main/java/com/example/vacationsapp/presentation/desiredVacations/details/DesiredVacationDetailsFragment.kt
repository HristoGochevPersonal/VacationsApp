package com.example.vacationsapp.presentation.desiredVacations.details

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.vacationsapp.R
import com.example.vacationsapp.databinding.FragmentDesiredVacationDetailsBinding
import com.example.vacationsapp.presentation.notifications.*
import com.example.vacationsapp.presentation.utils.collectLatestLifecycleFlow
import com.example.vacationsapp.presentation.utils.uriIsValid
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*


class DesiredVacationDetailsFragment : Fragment() {
    private var _binding: FragmentDesiredVacationDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: DesiredVacationDetailsFragmentArgs by navArgs()
    private val viewModel: DesiredVacationDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initializing the view model observers
        initViewModelObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDesiredVacationDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Making sure the information is kept up to date
        viewModel.refreshInfo(args.desiredVacationId)
        viewModel.setNotificationSet(notificationIsSet(args.desiredVacationId))

        // Setting up listener for the buttons
        binding.notificationButton.setOnClickListener {
            toggleNotification(args.desiredVacationId)
        }
        binding.editButton.setOnClickListener {
            val model = viewModel.vacationModelStateFlow.value ?: return@setOnClickListener
            val action =
                DesiredVacationDetailsFragmentDirections.actionDesiredVacationDetailsFragmentToEditDesiredVacationFragment(
                    model
                )
            findNavController().navigate(action)
        }

        binding.goBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Setting up the view model observers
    private fun initViewModelObservers() {

        collectLatestLifecycleFlow(viewModel.vacationModelStateFlow) {
            it?.let {
                binding.nameInfo.text = it.name
                binding.hotelInfo.text = it.hotelName
                binding.locationInfo.text = it.location
                binding.costInfo.text = it.cost.toString()
                binding.descriptionInfo.text = it.description
                if (requireActivity().uriIsValid(it.imagePath)) {
                    binding.imageInfo.setImageURI(it.imagePath)
                }
            }
        }

        collectLatestLifecycleFlow(viewModel.notificationSetStateFlow) {
            if (it) binding.notificationButton.setImageResource(R.drawable.ic_baseline_notifications_active_24)
            else binding.notificationButton.setImageResource(R.drawable.ic_baseline_notification_add_24)
        }
    }

    // Toggling the notification status
    private fun toggleNotification(vacationId: Int) {
        // Based on whether the notification is currently scheduled
        if (notificationIsSet(vacationId)) {
            displayCancelDialog(vacationId)
        } else {
            displayTimePicker(vacationId)
        }
    }

    // Displaying a time picker to schedule a reminder
    private fun displayTimePicker(vacationId: Int) {
        // Using the material time picker available in android
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Choose notification time")
            .build()

        // Showing it
        picker.show(parentFragmentManager, "vacations")

        // Adding a listener for when the user has selected time
        picker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, picker.hour)
                set(Calendar.MINUTE, picker.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // Scheduling a new notification with the time selected
            scheduleNotification(vacationId, calendar.timeInMillis)

            // Checking whether the notification has been successfully set
            with(notificationIsSet(vacationId)) {
                // Updating the view model
                viewModel.setNotificationSet(this)

                // Updating the user
                val text = if (this) "Scheduled a reminder for ${picker.hour}:${picker.minute}"
                else "Could not schedule a reminder"

                Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // Displays the cancel notification dialog
    private fun displayCancelDialog(vacationId: Int) {
        val builder = AlertDialog.Builder(requireActivity()).apply {
            setTitle("Confirm")
            setMessage("Cancel notification for ${viewModel.vacationModelStateFlow.value?.name}?")
            setCancelable(false)
            // If the user confirmed canceling of the notification
            setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                // Cancel the notification
                cancelNotification(vacationId)
                // Checking whether the notification has been successfully set
                val stillSet = notificationIsSet(vacationId)
                // Updating the view model
                viewModel.setNotificationSet(stillSet)
            }
            setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }

    // Schedules a notification in the future
    private fun scheduleNotification(vacationId: Int, timeInMillis: Long) {
        // Gets a reference to the alarm manager of the device
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), VacationNotificationReceiver::class.java)
        // Passing a vacation id to the pending intent
        intent.putExtra("vacationId", vacationId)
        val flags = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            vacationId,
            intent,
            flags
        )
        // Setting the alarm manager up with a new alarm that is essentially our notification
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

    // Cancels a scheduled notification
    private fun cancelNotification(vacationId: Int) {
        // Gets a reference to the alarm manager of the device
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Creating a pending intent that we can use to instruct the alarm to cancel based on its request code
        val intent = Intent(requireContext(), VacationNotificationReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            vacationId,
            intent,
            flags
        )
        // Canceling both the scheduled notification popup and the pending intent itself
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    // Checks whether a notification is scheduled
    private fun notificationIsSet(vacationId: Int): Boolean {
        // Creating a pending intent with information about the notification
        val intent = Intent(requireContext(), VacationNotificationReceiver::class.java)
        // Setting it's flags so that it does not change anything
        val flags = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        // If there's already a pending intent with the same request code that means the notification is set
        return PendingIntent.getBroadcast(
            requireContext(), vacationId,
            intent,
            flags
        ) != null
    }


}