package nemosofts.vox.radio.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import nemosofts.vox.radio.activity.PlayerService;

public class TimeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPref sharedPref = new SharedPref(context);
        try {
            if (sharedPref.getIsSleepTimeOn()) {
                sharedPref.setSleepTime(false, 0, 0);
                if (PlayerService.getInstance() != null) {
                    intent = new Intent(context, PlayerService.class);
                    intent.setAction(PlayerService.ACTION_STOP);
                    context.startService(intent);
                }
                Toast.makeText(context, "Time End Stop Audio", Toast.LENGTH_SHORT).show();
                AudioRecorder.onStopRecord();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}