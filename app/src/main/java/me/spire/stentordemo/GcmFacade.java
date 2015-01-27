package me.spire.stentordemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class GcmFacade {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";

    private final static String TAG = "GcmFacade";

    private Context mContext;
    private String mSenderId;
    private GoogleCloudMessaging mGcm;
    private AtomicInteger mMsgId = new AtomicInteger();
    private SharedPreferences mPrefs;
    private String mRegId;

    public GcmFacade(Context context) {
        if (context == null) return; // should technically throw error

        mContext = context;
        mSenderId = mContext.getResources().getString(R.string.gcm_sender_id);
        mGcm = GoogleCloudMessaging.getInstance(mContext);
    }

    public void ensureRegistration() {
        mRegId = getRegistrationId();

        if (mRegId == null || mRegId.isEmpty()) {
            registerInBackground();
        }
    }

    private String getRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        return registrationId;
    }

    private void storeRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, mRegId);
        editor.commit();
    }

    private SharedPreferences getGCMPreferences() {
        return mContext.getSharedPreferences(mContext.getClass().getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private void subscribeToDemoServer() {

    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {

                    mRegId = mGcm.register(mSenderId);
                    storeRegistrationId();

                    subscribeToDemoServer();

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            }

        }.execute(null, null, null);
    }

}
