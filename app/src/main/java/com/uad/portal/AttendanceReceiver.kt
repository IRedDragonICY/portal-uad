

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.uad.portal.MainViewModel
import com.uad.portal.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AttendanceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val klsdtId = intent.getStringExtra("klsdtId") ?: return
        val presklsId = intent.getStringExtra("presklsId") ?: return

        val sessionManager = SessionManager(context)
        val viewModel = MainViewModel().apply {
            initSessionManager(context)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val isAttendanceMarked = viewModel.markAttendance(klsdtId, presklsId)
            withContext(Dispatchers.Main) {
                val message = if (isAttendanceMarked) "Attendance marked!" else "Failed to mark attendance!"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}



