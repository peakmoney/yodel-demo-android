package me.spire.stentordemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import me.spire.stentordemo.NotificationRegistrar.*;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends ActionBarActivity {

    public static final String PROPERTY_USER_ID = "user_id";
    public static final String TAG = "MainActivity";

    private final Context CONTEXT = this;

    private Button mRegisterButton;
    private Button mUnregisterButton;
    private Button mNotifyButton;

    private EditText mUserIdField;
    private EditText mNotificationMessageField;

    private final int mDefaultUserId = 1;

    private NotificationRegistrar.OnRegistrationUpdateListener mRegistrationUpdateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRegisterButton = (Button) findViewById(R.id.register_button);
        mUnregisterButton = (Button) findViewById(R.id.unregister_button);
        mNotifyButton = (Button) findViewById(R.id.notify_button);

        mUserIdField = (EditText) findViewById(R.id.user_id_field);
        mNotificationMessageField = (EditText) findViewById(R.id.notification_message_field);

        mRegistrationUpdateListener = new OnRegistrationUpdateListener() {
            @Override
            public void onRegistrationUpdate(RegistrationAction action, String message) {

                if (action == RegistrationAction.REGISTER && message.isEmpty()) {
                    Toast.makeText(CONTEXT, "Successfully registered!", Toast.LENGTH_SHORT).show();
                    storeUserId(mUserIdField.getText().toString(), CONTEXT);

                } else if (action == RegistrationAction.UNREGISTER && message.isEmpty()) {
                    Toast.makeText(CONTEXT, "Successfully unregistered!", Toast.LENGTH_SHORT).show();
                    storeUserId("", CONTEXT);

                } else if (!message.isEmpty()) {
                    Toast.makeText(CONTEXT, "Error! " + action.toString() + " : " + message, Toast.LENGTH_SHORT).show();
                }

                renderUserIdField();
            }
        };

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserIdField.getText().length() == 0) {
                    Toast.makeText(CONTEXT, "No user ID entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                NotificationRegistrar.register(CONTEXT, getUserId(), mRegistrationUpdateListener);
            }
        });

        mUnregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationRegistrar.unregister(CONTEXT, getUserId(), mRegistrationUpdateListener);
            }
        });

        mNotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = mNotificationMessageField.getText().toString();

                String dataJson = "{\"message\": \"" + message + "\"}";

                new DemoApi().getService().notify(new DemoApi.Message(getUserId(), message, dataJson),
                        new Callback<DemoApi.StatusResponse>() {

                    @Override
                    public void success(DemoApi.StatusResponse statusResponse, Response response) {
                        Toast.makeText(CONTEXT, "Notification Successful", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(CONTEXT, "Failed Notification: " +
                                (error != null ? error.getMessage() : "<no error>"), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        renderUserIdField();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int getUserId() {
        String userIdString = mUserIdField.getText().toString();
        int userId = mDefaultUserId;

        if (userIdString.length() > 0) {
            userId = Math.max(Integer.valueOf(userIdString), 1);
        }

        return userId;
    }

    private void renderUserIdField() {
        String userId = getStoredUserId(CONTEXT);
        if (userId.isEmpty()) {
            mUserIdField.setEnabled(true);
        } else {
            mUserIdField.setEnabled(false);
            mUserIdField.setText(userId);
        }
    }

    private static String getStoredUserId(Context context) {
        final SharedPreferences prefs = getPreferences(context);
        String userId = prefs.getString(PROPERTY_USER_ID, "");
        if (userId.isEmpty()) {
            Log.i(TAG, "User ID not found.");
            return "";
        }

        return userId;
    }

    private static void storeUserId(String userId, Context context) {
        final SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_USER_ID, userId);
        editor.commit();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(context.getClass().getSimpleName(),
                Context.MODE_PRIVATE);
    }
}
