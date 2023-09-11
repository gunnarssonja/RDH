package se.cgi.android.rdh.notifications;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

import se.cgi.android.rdh.BuildConfig;
import se.cgi.android.rdh.R;
import se.cgi.android.rdh.utils.Logger;

/**
 * This class is a singleton class that handles different sounds.
 * To play a sound file the Android media player is used.
 * Note: It is where important to release the media player when the sound is finished.
 */
public final class Sound {
    private static final String APP_RAW_URI_PATH_1 = String.format("android.resource://%s/raw/", BuildConfig.APPLICATION_ID);
    private static final String TAG = Sound.class.getSimpleName();
    private static final String successTone = String.valueOf(R.raw.chimes);
    private static final String failureTone = String.valueOf(R.raw.error);
    private static final String notificationTone = String.valueOf(R.raw.notify);
    private static final String exclamationTone = String.valueOf(R.raw.exclamation);

    private static volatile Sound sInstance = null;
    private MediaPlayer mp;
    private final Context context;  // Note: Application context not activity context here

    private Sound(Context context) {
        this.context = context;
    }

    /**
     * Get singleton object of Sound
     * @param context Only application context here
     * @return sInstance
     */
    public static synchronized Sound getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Sound(context);
        }
        return sInstance;
    }

    /**
     * Play a sound in the media player
     * @param fileName if sound name is "sound.mp3" then pass fileName as "sound" only.
     */
    public synchronized void playSound(String fileName) {
        if (sInstance.mp == null) {
            sInstance.mp = new MediaPlayer();
        } else {
            sInstance.mp.reset();
        }
        try {
            sInstance.mp.setDataSource(context, Uri.parse(APP_RAW_URI_PATH_1 + fileName));
            sInstance.mp.prepare();
            sInstance.mp.setVolume(100f, 100f);
            sInstance.mp.setLooping(false);
            sInstance.mp.start();
            sInstance.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (sInstance.mp != null) {
                        sInstance.mp.reset();
                        sInstance.mp = null;
                    }
                }
            });
        } catch (IOException e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    public synchronized void stopSound() {
        if (sInstance.mp != null) {
            sInstance.mp.stop();
            sInstance.mp.release();
        }
    }

    public synchronized void pauseSound() {
        if (sInstance.mp != null) {
            sInstance.mp.pause();
        }
    }

    public synchronized void restartSound() {
        if (sInstance.mp != null) {
            sInstance.mp.start();
        }
    }

    public synchronized void playSuccess() {
        playSound(successTone);
    }

    public synchronized void playFailureTone() {
        playSound(failureTone);
    }

    public synchronized void playNotificationTone() {
        playSound(notificationTone);
    }

    public synchronized void playExclamationTone() {
        playSound(exclamationTone);
    }
}
