package com.tomer.alwayson.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.format.DateFormat;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.receivers.BatteryReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TTS implements TextToSpeech.OnInitListener, ContextConstatns {

    private boolean speaking;
    private Context context;
    private TextToSpeech tts;
    private boolean toStopTTS;
    private BatteryReceiver batteryReceiver;

    public TTS(Context context) {
        this.context = context;
        batteryReceiver = new BatteryReceiver();
    }

    public void sayCurrentStatus() {
        tts = new TextToSpeech(context, TTS.this);
        tts.setLanguage(Locale.getDefault());
        tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onInit(int status) {
        if (toStopTTS) {
            try {
                tts.speak(" ", TextToSpeech.QUEUE_FLUSH, null);
            } catch (NullPointerException ignored) {
            }
            return;
        }
        if (status == TextToSpeech.SUCCESS) {
            if (!speaking) {
                SimpleDateFormat sdf;
                sdf = DateFormat.is24HourFormat(context) ? new SimpleDateFormat("HH mm", Locale.getDefault()) : new SimpleDateFormat("h mm aa", Locale.getDefault());
                String time = sdf.format(new Date());
                time = time.charAt(0) == '0' ? time.substring(1, time.length()) : time;

                tts.speak("The time is " + time, TextToSpeech.QUEUE_FLUSH, null);
                if (Globals.notifications.size() > 0)
                    tts.speak("You have " + Globals.notifications.size() + " Notifications", TextToSpeech.QUEUE_ADD, null);
                tts.speak("Battery is at " + batteryReceiver.getBatteryLevel(context) + " percent", TextToSpeech.QUEUE_ADD, null);
                speaking = true;
                new Handler().postDelayed(() -> speaking = false, 4000);
            } else
                Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Still speaking..");
        }
    }

    public void destroy() {
        toStopTTS = true;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        tts = null;
    }
}
