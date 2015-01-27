package me.spire.stentordemo;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import me.spire.stentordemo.NotificationRegistrar.*;


public class MainActivity extends ActionBarActivity {

    private final Context CONTEXT = this;

    private Button mRegisterButton;
    private Button mUnregisterButton;
    private Button mNotifyButton;

    private NotificationRegistrar.OnRegistrationUpdateListener mRegistrationUpdateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRegisterButton = (Button) findViewById(R.id.register_button);
        mUnregisterButton = (Button) findViewById(R.id.unregister_button);
        mNotifyButton = (Button) findViewById(R.id.notify_button);

        mRegistrationUpdateListener = new OnRegistrationUpdateListener() {
            @Override
            public void onRegistrationUpdate(RegistrationAction action, String message) {

                if (action == RegistrationAction.REGISTER && message.isEmpty()) {
                    Toast.makeText(CONTEXT, "Successfully registered!", Toast.LENGTH_SHORT).show();

                } else if (action == RegistrationAction.UNREGISTER && message.isEmpty()) {
                    Toast.makeText(CONTEXT, "Successfully unregistered!", Toast.LENGTH_SHORT).show();

                } else if (!message.isEmpty()) {
                    Toast.makeText(CONTEXT, "Error! " + action.toString() + " : " + message, Toast.LENGTH_SHORT).show();
                }
            }
        };

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationRegistrar.register(CONTEXT, mRegistrationUpdateListener);
            }
        });

        mUnregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationRegistrar.unregister(CONTEXT, mRegistrationUpdateListener);
            }
        });
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
}
