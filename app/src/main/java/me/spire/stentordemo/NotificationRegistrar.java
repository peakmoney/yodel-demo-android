package me.spire.stentordemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class NotificationRegistrar {

    public static final String PROPERTY_REG_ID = "registration_id";

    public enum RegistrationAction {
        REGISTER,
        UNREGISTER
    }

    private final static String TAG = "GcmFacade";

    private Context mContext;
    private String mSenderId;
    private GoogleCloudMessaging mGcm;
    private String mRegId;

    private OnRegistrationUpdateListener mRegistrationUpdateListener;

    public static void register(Context context, OnRegistrationUpdateListener listener) {
        NotificationRegistrar registrar = new NotificationRegistrar(context);
        registrar.setOnRegistrationUpdateListener(listener);
        registrar.register();
    }

    public static void unregister(Context context, OnRegistrationUpdateListener listener) {
        NotificationRegistrar registrar = new NotificationRegistrar(context);
        registrar.setOnRegistrationUpdateListener(listener);
        registrar.unregister();
    }

    public NotificationRegistrar(Context context) {
        if (context == null) return; // should technically throw error

        mContext = context;
        mSenderId = mContext.getResources().getString(R.string.gcm_sender_id);
        mGcm = GoogleCloudMessaging.getInstance(mContext);
    }

    public void setOnRegistrationUpdateListener(OnRegistrationUpdateListener listener) {
        mRegistrationUpdateListener = listener;
    }

    public void register() {

        mRegId = getRegistrationId();

        if (mRegId == null || mRegId.isEmpty()) {
            performRegistrationTask(RegistrationAction.REGISTER);
        }
    }

    public void unregister() {
        mRegId = null;
        performRegistrationTask(RegistrationAction.UNREGISTER);
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

    private void unsubscribeToDemoServer() {

    }

    private void performRegistrationTask(final RegistrationAction action) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {

                    if (action == RegistrationAction.REGISTER) {
                        Log.i(TAG, "Sender ID: " + mSenderId);
                        mRegId = mGcm.register(mSenderId);
                        subscribeToDemoServer();

                    } else if (action == RegistrationAction.UNREGISTER) {
                        mRegId = null;
                        unsubscribeToDemoServer();
                    }

                    storeRegistrationId();

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
                // Maybe do something with that message

                if (mRegistrationUpdateListener != null) {
                    mRegistrationUpdateListener.onRegistrationUpdate(action, msg);
                }
            }

        }.execute(null, null, null);
    }

    public interface OnRegistrationUpdateListener {
        public void onRegistrationUpdate(RegistrationAction action, String message);
    }

}
