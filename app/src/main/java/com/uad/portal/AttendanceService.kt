import android.app.IntentService
import android.content.Intent
import com.uad.portal.MainViewModel
import com.uad.portal.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttendanceService : IntentService("AttendanceService") {

    override fun onHandleIntent(intent: Intent?) {
        val klsdtId = intent?.getStringExtra("klsdtId")
        val presklsId = intent?.getStringExtra("presklsId")

        val sessionManager = SessionManager(this)
        val viewModel = MainViewModel().apply {
            initSessionManager(this@AttendanceService)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val isAttendanceMarked = viewModel.markAttendance(klsdtId!!, presklsId!!)
        }
    }
}
