package me.spire.yodeldemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NotificationRegistrar {

    public static final String PROPERTY_REG_ID = "gcm_registration_id";
    public static final int RETRY_COUNT = 2;
    public static final int INITIAL_RETRY_DELAY_MS = 200;

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

        mRegId = getRegistrationId(mContext);

        if (mRegId == null || mRegId.isEmpty()) {
            performRegistrationTask(RegistrationAction.REGISTER);
        }
    }

    public void unregister() {
        performRegistrationTask(RegistrationAction.UNREGISTER);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        return registrationId;
    }

    private void storeRegistrationId(String regId, Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.commit();
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(context.getClass().getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private void performRegistrationTask(final RegistrationAction action) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String errorMessage = "";

                // Allow exponential backoff to be called <RETRY_COUNT> times
                // before sending an error message

                for (int attempt = 0; attempt <= RETRY_COUNT; attempt++) {

                    if (attempt > 0) {
                        // Start exponential backoff
                        try {
                            Thread.sleep(attempt * INITIAL_RETRY_DELAY_MS);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    try {

                        if (action == RegistrationAction.REGISTER) {
                            Log.i(TAG, "Sender ID: " + mSenderId);
                            mRegId = mGcm.register(mSenderId);
                            storeRegistrationId(mRegId, mContext);

                        } else if (action == RegistrationAction.UNREGISTER) {
                            mGcm.unregister();
                            mRegId = null;
                            storeRegistrationId(mRegId, mContext);
                        }

                        errorMessage = "";
                        break;

                    } catch (IOException ex) {
                        errorMessage = "Error :" + ex.getMessage();
                    }
                }

                return errorMessage;
            }

            @Override
            protected void onPostExecute(String errorMessage) {
                // Maybe do something with that message

                if (mRegistrationUpdateListener != null) {
                    if (errorMessage != null && errorMessage.length() > 0) {
                        mRegistrationUpdateListener.onError(errorMessage);
                    } else {
                        mRegistrationUpdateListener.onUpdate(action, mRegId);
                    }
                }
            }

        }.execute(null, null, null);
    }

    public interface OnRegistrationUpdateListener {
        public void onUpdate(RegistrationAction action, String registrationId);
        public void onError(String errorMessage);
    }

}
