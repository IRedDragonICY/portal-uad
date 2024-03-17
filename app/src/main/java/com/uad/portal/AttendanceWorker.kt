package com.uad.portal

import AttendanceService
import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uad.portal.API.portal
import com.uad.portal.views.Attendance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttendanceWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    private lateinit var sessionManager: SessionManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        sessionManager = SessionManager(applicationContext)
        val portal = portal(sessionManager)
        val mainViewModel = MainViewModel()
        mainViewModel.initSessionManager(applicationContext)

        if (!mainViewModel.isNetworkAvailable.value!!) {
            return@withContext Result.retry()
        }

        val attendanceInfo = portal.getAttendanceInfo()

        if (attendanceInfo.isNotEmpty()) {
            sendNotification(attendanceInfo)
        }

        Result.success()
    }



    private fun sendNotification(attendanceInfo: List<Attendance>) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channelId = "attendance_notifications"

        for ((index, attendance) in attendanceInfo.withIndex()) {
            if (attendance.attendanceStatus == "Not Marked") {
                val attendanceIntent = Intent(applicationContext, AttendanceService::class.java).apply {
                    putExtra("klsdtId", attendance.klsdtId)
                    putExtra("presklsId", attendance.presklsId)
                }

                val attendancePendingIntent: PendingIntent = PendingIntent.getService(applicationContext, index, attendanceIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(applicationContext, channelId)
                    .setSmallIcon(R.drawable.logo_uad)
                    .setContentTitle(attendance.courseClass)
                    .setContentText(attendance.meetingDate)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .addAction(R.drawable.logo_uad, "Absen", attendancePendingIntent)
                    .build()

                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                notificationManager.notify(index, notification)
            }
        }
    }






    companion object {
        const val ACTION_REQUEST_PERMISSION = "com.uad.portal.ACTION_REQUEST_PERMISSION"
        const val EXTRA_PERMISSION = "com.uad.portal.EXTRA_PERMISSION"
        const val EXTRA_NOTIFICATION = "com.uad.portal.EXTRA_NOTIFICATION"
        const val EXTRA_NOTIFICATION_ID = "com.uad.portal.EXTRA_NOTIFICATION_ID"
        const val REQUEST_CODE = 0
    }
}


class PermissionRequestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AttendanceWorker.ACTION_REQUEST_PERMISSION) return

        val permission = intent.getStringExtra(AttendanceWorker.EXTRA_PERMISSION) ?: return
        val notification = intent.getParcelableExtra<Notification>(AttendanceWorker.EXTRA_NOTIFICATION) ?: return
        val notificationId = intent.getIntExtra(AttendanceWorker.EXTRA_NOTIFICATION_ID, 0)

        if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, notification)
        } else {
            Toast.makeText(context, "Aplikasi memerlukan izin untuk mengirim notifikasi. Silakan berikan izin dalam pengaturan.", Toast.LENGTH_LONG).show()
        }
    }
}
