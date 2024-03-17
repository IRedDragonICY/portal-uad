import android.app.IntentService
import android.content.Intent
import com.uad.portal.API.portal
import com.uad.portal.MainViewModel
import com.uad.portal.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttendanceService(private val sessionManager: SessionManager) : IntentService("AttendanceService") {

    override fun onHandleIntent(intent: Intent?) {
        val klsdtId = intent?.getStringExtra("klsdtId") ?: return
        val presklsId = intent?.getStringExtra("presklsId") ?: return

        val viewModel = MainViewModel().apply {
            initSessionManager(this@AttendanceService)
        }

        if (!viewModel.isNetworkAvailable.value!!) {
            stopSelf()
            return
        }

        val portal = portal(sessionManager)

        CoroutineScope(Dispatchers.IO).launch {
            portal.markAttendance(klsdtId, presklsId)
        }
    }

}
