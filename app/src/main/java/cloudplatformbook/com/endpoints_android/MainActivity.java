package com.cloudplatformbook.endpoints_android;

import java.io.IOException;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.appspot.lunch__mates__endpoints.lunchmates.Lunchmates;
import com.appspot.lunch__mates__endpoints.lunchmates.model.MeetingCollection;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

public class MainActivity extends ActionBarActivity {

    private SharedPreferences sharedPreferences;
    private static final String ACCOUNT_NAME = "account_name";

    private static final int ACCOUNT_PICKER_REQUEST_CODE = 2;

    private static final String ANDROID_AUDIENCE = "server:client_id:99886669718-6q66jlok7oej4bbgfn1nhu5g5gdgak4n" +
            ".apps.googleusercontent.com";

    private Lunchmates lunchmatesApi;
    private GoogleAccountCredential credential;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

        case ACCOUNT_PICKER_REQUEST_CODE:
            if (data != null && data.getExtras() != null) {
                String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                if (accountName != null) {
                    checkSelectedAccount(accountName);
                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        credential = GoogleAccountCredential.usingAudience(this, ANDROID_AUDIENCE);

        checkSelectedAccount(sharedPreferences.getString(ACCOUNT_NAME, null));
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

    private void checkSelectedAccount(String accountName) {

        if (accountName == null) {
            startActivityForResult(credential.newChooseAccountIntent(), ACCOUNT_PICKER_REQUEST_CODE);
        } else {
            setupLunchmatesApi(credential, accountName);
            fetchMeetingsList();
        }
    }

    private void setupLunchmatesApi(GoogleAccountCredential credential, String accountName) {

        credential.setSelectedAccountName(accountName);
        storeAccountName(accountName);

        Lunchmates.Builder lunchmates = new Lunchmates.Builder(
                new NetHttpTransport(),
                new JacksonFactory(),
                credential);

        lunchmatesApi = lunchmates.build();
    }

    private void fetchMeetingsList() {

        Thread activityThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MeetingCollection meetings = lunchmatesApi.meeting().meetings().list().execute();

                    final String result;
                    if (meetings.get("items") != null) {
                        result = meetings.getItems().size() + " meetings";
                    } else {
                        result = "0 meetings";
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        activityThread.start();
    }

    private void storeAccountName(String accountName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCOUNT_NAME, accountName);
        editor.apply();
    }
}
