package com.example.vacationsapp.presentation.notifications

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.vacationsapp.R
import com.example.vacationsapp.core.repository.VacationsRepository
import com.example.vacationsapp.presentation.MainActivity
import com.example.vacationsapp.presentation.desiredVacations.DesiredVacationModel
import kotlinx.coroutines.runBlocking


// Broadcast receiver that reminds the user to go to a location
class VacationNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Receives a vacation id and fetches a model from the repository
        val vacationId: Int = intent.getIntExtra("vacationId", -1)
        if (vacationId == -1) return
        val model = runBlocking {
            VacationsRepository.instance.vacationFetch(vacationId)?.let {
                DesiredVacationModel(
                    it.id,
                    it.name,
                    it.hotelName,
                    it.location,
                    it.cost,
                    it.description,
                    it.imagePath.toUri()
                )
            }
        } ?: return

        // Removes the pending intent from the alarm manager so it does not repeat
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val flags = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            vacationId,
            intent,
            flags
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        // Creates a new notification based on the passed model
        val notification = getNotification(context, model)

        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.notify(vacationId, notification)

    }

    // Creates a new notification based on a passed model
    private fun getNotification(context: Context, model: DesiredVacationModel): Notification {

        // Make sure the app is opened if the user presses the notification
        val openLocationIntent = Intent(context, MainActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val openLocationPendingIntent = PendingIntent.getActivity(
            context,
            model.id,
            openLocationIntent,
            flags
        )

        // Build the notification with the higher priorities and information about the vacation
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, "vacations")
                .setSmallIcon(R.drawable.ic_baseline_landscape_24)
                .setContentTitle("Vacation reminder")
                .setContentText("Go to the ${model.name}")
                .setAutoCancel(true)
                .setContentIntent(openLocationPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        return builder.build()
    }
}

