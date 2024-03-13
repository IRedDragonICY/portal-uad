

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.uad.portal.MainViewModel
import com.uad.portal.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttendanceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val klsdtId = intent.getStringExtra("klsdtId")
        val presklsId = intent.getStringExtra("presklsId")

        val sessionManager = SessionManager(context)
        val viewModel = MainViewModel().apply {
            initSessionManager(context)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val isAttendanceMarked = viewModel.markAttendance(klsdtId!!, presklsId!!)
            (context as Activity).runOnUiThread {
                if (isAttendanceMarked) {
                    Toast.makeText(context, "Attendance marked!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to mark attendance!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}



