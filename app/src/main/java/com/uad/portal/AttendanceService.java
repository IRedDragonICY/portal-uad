package com.uad.portal;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.uad.portal.API.portal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceService extends IntentService {
    private SessionManager sessionManager;
    private ExecutorService executorService;

    public AttendanceService() {
        super("AttendanceService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = new SessionManager(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;

        String klsdtId = intent.getStringExtra("klsdtId");
        if (klsdtId == null) return;

        String presklsId = intent.getStringExtra("presklsId");
        if (presklsId == null) return;


        portal portal = new portal(sessionManager);
        Log.i("AttendanceService", "AttendanceService is running");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                portal.markAttendance(klsdtId, presklsId);
            }
        });
    }
}
