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
    private int mUserId;

    private OnRegistrationUpdateListener mRegistrationUpdateListener;

    public static void register(Context context, int userId, OnRegistrationUpdateListener listener) {
        NotificationRegistrar registrar = new NotificationRegistrar(context, userId);
        registrar.setOnRegistrationUpdateListener(listener);
        registrar.register();
    }

    public static void unregister(Context context, int userId, OnRegistrationUpdateListener listener) {
        NotificationRegistrar registrar = new NotificationRegistrar(context, userId);
        registrar.setOnRegistrationUpdateListener(listener);
        registrar.unregister();
    }

    public NotificationRegistrar(Context context, int userId) {
        if (context == null) return; // should technically throw error

        mContext = context;
        mSenderId = mContext.getResources().getString(R.string.gcm_sender_id);
        mGcm = GoogleCloudMessaging.getInstance(mContext);
        mUserId = userId;
    }

    public void setOnRegistrationUpdateListener(OnRegistrationUpdateListener listener) {
        mRegistrationUpdateListener = listener;
    }

    public void setUserId(int userId) {
        mUserId = userId;
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

    private static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        return registrationId;
    }

    private static void storeRegistrationId(String regId, Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.commit();
    }

    private static SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(context.getClass().getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private void subscribeToDemoServer() {
        new DemoApi().getService().subscribe(new DemoApi.Device(mUserId, getRegistrationId(mContext)),
                new Callback<DemoApi.StatusResponse>() {
            @Override
            public void success(DemoApi.StatusResponse statusResponse, Response response) {
                Toast.makeText(mContext, "Subscription Successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(mContext, "Failed Subscription: " +
                        (error != null ? error.getMessage() : "<no error>"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unsubscribeToDemoServer() {
        new DemoApi().getService().unsubscribe(new DemoApi.Device(mUserId, getRegistrationId(mContext)),
                new Callback<DemoApi.StatusResponse>() {
            @Override
            public void success(DemoApi.StatusResponse statusResponse, Response response) {
                Toast.makeText(mContext, "Unsubscription Successful", Toast.LENGTH_SHORT).show();
                mRegId = "";
                storeRegistrationId(mRegId, mContext);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(mContext, "Failed Unsubscription: " +
                        (error != null ? error.getMessage() : "<no error>"), Toast.LENGTH_SHORT).show();
            }
        });
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
                        storeRegistrationId(mRegId, mContext);
                        subscribeToDemoServer();

                    } else if (action == RegistrationAction.UNREGISTER) {
                        unsubscribeToDemoServer();
                    }

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
