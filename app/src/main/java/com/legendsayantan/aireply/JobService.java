package com.legendsayantan.aireply;

import android.app.job.JobParameters;
import android.content.Intent;
import android.os.Build;

public class JobService extends android.app.job.JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        System.out.println("JobService started");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) startForegroundService(new Intent(getApplicationContext(), PythonService.class));
        else startService(new Intent(getApplicationContext(),PythonService.class));
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Intent broadcastIntent = new Intent();
        sendBroadcast(broadcastIntent);
        return true;
    }
}


