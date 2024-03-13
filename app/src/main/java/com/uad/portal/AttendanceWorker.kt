package com.uad.portal

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttendanceWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val mainViewModel = MainViewModel()

        val attendanceInfo = mainViewModel.getAttendanceInfo()

        if (attendanceInfo.isNotEmpty()) {
            sendNotification(attendanceInfo)
        }

        Result.success()
    }

    private fun sendNotification(attendanceInfo: List<Attendance>) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)

        // Ubah ini menjadi ID channel Anda
        val channelId = "attendance_notifications"

        // Membuat notifikasi untuk setiap presensi
        for ((index, attendance) in attendanceInfo.withIndex()) {
            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.logo_uad)
                .setContentTitle(attendance.courseClass)
                .setContentText(attendance.meetingDate)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            notificationManager.notify(index, notification)
        }
    }

}
