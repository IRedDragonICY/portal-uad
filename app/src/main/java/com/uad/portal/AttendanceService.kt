import android.app.IntentService
import android.content.Intent
import com.uad.portal.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttendanceService : IntentService("AttendanceService") {

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
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.markAttendance(klsdtId, presklsId)
        }
    }
}
