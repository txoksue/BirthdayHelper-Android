package com.example.joseramonparreno.birthdayhelper;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.ArrayList;
import java.util.Calendar;


public class BirthHelperMainActivity extends AppCompatActivity implements ListView.OnItemClickListener {

    public ListView listViewContacts;
    public TextView txtSearchContact;
    public SQLiteDatabase dataBaseApp;
    public ContactsDBAdapter adapter;
    public ArrayList<ContactDB> listContactsDB;
    public ContentResolver contentResolver;
    public DBActions actionsDB;
    public Context mainActivityContext;

    public static final String BIRTHHELPER_MAIN_ACTIVITY_LOG = "BirthHelperMainActivity";

    public static final String CREATE_DATABASE_BIRTHDAYHELPER = "CREATE TABLE IF NOT EXISTS miscumples(ID INT PRIMARY KEY NOT NULL,TipoNotif CHAR(1),Mensaje VARCHAR(160),Telefono VARCHAR(15),FechaNacimiento VARCHAR(15),Nombre VARCHAR(128));";

    public static final String ID = "ID";
    public static final String NAME = "Name";
    public static final String PHONE = "Phone";
    public static final String DOB = "DOB";
    public static final String NOTIFICATION_ID = "NotificationId";
    public static final String MESSAGE = "Message";
    public static final String PHOTO = "Photo";

    public static final int CONTACT_DETAIL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birth_helper_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.i(BIRTHHELPER_MAIN_ACTIVITY_LOG, "Creating BirthHelperMainActivity");

        mainActivityContext = getApplicationContext();

        listViewContacts = (ListView) findViewById(R.id.listContacts);
        txtSearchContact = (TextView) findViewById(R.id.txtSearchContact);

        contentResolver = getContentResolver();

        dataBaseApp = openOrCreateDatabase("BirthDayHelper", Context.MODE_PRIVATE, null);
        dataBaseApp.execSQL(CREATE_DATABASE_BIRTHDAYHELPER);

        Log.i(BIRTHHELPER_MAIN_ACTIVITY_LOG, "Database open or created.");

        if (actionsDB == null) {

            actionsDB = new DBActions(this.dataBaseApp);
        }

        new updateContactsDBAsyncTask().execute();

    }

    /*
    @Override
    protected void onPostResume() {
        super.onPostResume();

        new updateContactsDBAsyncTask().execute();
        System.out.println("ON RESUME");
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_birth_helper_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_configurar_felicitaciones) {

            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getFragmentManager(), "timePicker");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intentContactDetails = new Intent(this, ContactDetailActivity.class);

        ContactDB contactDB = (ContactDB) parent.getItemAtPosition(position);

        intentContactDetails.putExtra(ID, contactDB.getID());
        intentContactDetails.putExtra(NAME, contactDB.getName());
        intentContactDetails.putExtra(PHONE, contactDB.getPhone());
        intentContactDetails.putExtra(DOB, contactDB.getDOB());
        intentContactDetails.putExtra(NOTIFICATION_ID, contactDB.getNotificationId());
        intentContactDetails.putExtra(MESSAGE, contactDB.getMessage());
        intentContactDetails.putExtra(PHOTO, contactDB.getPhoto());

        startActivityForResult(intentContactDetails, CONTACT_DETAIL);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_DETAIL) {

            new getAllContactsDBAsyncTask().execute();
        }
    }



    /**
     * @param listContactsDB
     */

    public void loadListViewContacts(ArrayList<ContactDB> listContactsDB) {

        this.adapter = new ContactsDBAdapter(this, listContactsDB);
        listViewContacts.setAdapter(this.adapter);
        listViewContacts.setOnItemClickListener(this);

    }



    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);


            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }


        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            AlarmManager alarmMgr;
            PendingIntent alarmIntent;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            Intent intent = new Intent(getActivity(), AlarmReceiver.class);

            alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

            alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

            Log.i(BIRTHHELPER_MAIN_ACTIVITY_LOG, "Alarm has been set.");

        }


    }

    /**
     *
     */

    private class updateContactsDBAsyncTask extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(BIRTHHELPER_MAIN_ACTIVITY_LOG, "Asynchronous call to update contacts database.");

            actionsDB.updateContactsDB(contentResolver);
            listContactsDB = actionsDB.getAllContactsDB(dataBaseApp, contentResolver);

            return true;

        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {

                loadListViewContacts(listContactsDB);

            }
        }

    }


    /**
     *
     */

    private class getAllContactsDBAsyncTask extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(BIRTHHELPER_MAIN_ACTIVITY_LOG, "Asynchronous call to get all contacts database.");

            listContactsDB = actionsDB.getAllContactsDB(dataBaseApp, contentResolver);

            return true;

        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {

                loadListViewContacts(listContactsDB);

            }
        }
    }


}