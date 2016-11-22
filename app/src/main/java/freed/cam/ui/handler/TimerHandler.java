package freed.cam.ui.handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.TextView;

import com.troop.freedcam.R;
import com.troop.freedcam.R.id;

import freed.cam.ActivityFreeDcamMain;
import freed.cam.apis.KEYS;
import freed.cam.apis.basecamera.modules.RecordingStates;

/**
 * Created by troop on 26.11.2014.
 */
public class TimerHandler
{
    private final TextView timerText;

    private final ActivityFreeDcamMain activityFreeDcamMain;
    private final MyTimer timer;

    public TimerHandler(ActivityFreeDcamMain activityFreeDcamMain)
    {
        this.activityFreeDcamMain = activityFreeDcamMain;
        timerText = (TextView) activityFreeDcamMain.findViewById(id.textView_RecCounter);
        timer = new MyTimer(timerText);
        activityFreeDcamMain.getContext().registerReceiver(new ModuleChangedReciever(), new IntentFilter(activityFreeDcamMain.getResources().getString(R.string.INTENT_MODULECHANGED)));
        activityFreeDcamMain.getContext().registerReceiver(new RecordingStateReciever(), new IntentFilter(activityFreeDcamMain.getResources().getString(R.string.INTENT_RECORDSTATECHANGED)));
        timerText.setVisibility(View.GONE);
    }

    private class ModuleChangedReciever extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            String module = intent.getStringExtra(activityFreeDcamMain.getResources().getString(R.string.INTENT_EXTRA_MODULECHANGED));
            if (module.equals(KEYS.MODULE_VIDEO))
                timerText.setVisibility(View.VISIBLE);
            else
                timerText.setVisibility(View.GONE);
        }
    }

    private class RecordingStateReciever extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(activityFreeDcamMain.getString(R.string.INTENT_EXTRA_RECORDSTATECHANGED), RecordingStates.STATUS_RECORDING_STOP);
            switch (status) {
                case RecordingStates.STATUS_RECORDING_STOP:
                    timer.Stop();
                    break;
                case RecordingStates.STATUS_RECORDING_START :
                    timer.Start();
                    break;
                default:
                    timer.Stop();
                    break;
            }
        }
    }
}
