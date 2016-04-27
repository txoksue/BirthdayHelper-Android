package com.example.joseramonparreno.birthdayhelper;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;

public class ContactDetailActivity extends AppCompatActivity implements CheckBox.OnCheckedChangeListener, Spinner.OnItemSelectedListener{

    public ContactDB contactSelected;
    public EditText txtMessage, txtName, txtDOB;
    public Spinner spnPhone;
    public ImageView photoContact;
    public CheckBox chbSMS;
    SQLiteDatabase dataBaseApp;
    DBActions actionsDB;
    Actions actions;
    ContentResolver contentResolver;
    ArrayList<String> listContactPhones;

    public static final String CONTACT_DETAIL_ACTIVITY_LOG= "ContactDetailActivity";

    public static final String ID = "ID";
    public static final String NAME = "Name";
    public static final String PHONE = "Phone";
    public static final String DOB = "DOB";
    public static final String NOTIFICATION_ID = "NotificationId";
    public static final String MESSAGE = "Message";
    public static final String PHOTO = "Photo";

    public static final String SEND_SMS = "1";
    public static final String JUST_NOTIFICATION = "2";

    public static final int CONTACTS_APP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        this.contentResolver = getContentResolver();
        
        //Abrimos la BBDD.

        this.dataBaseApp = openOrCreateDatabase("BirthDayHelper", Context.MODE_PRIVATE, null);

        Log.i(CONTACT_DETAIL_ACTIVITY_LOG, "Database open.");

        if (actionsDB == null) {

            actionsDB = new DBActions(this.dataBaseApp);
        }

        if (actions == null){

            actions = new Actions();

        }

        Intent intentMainActivity = getIntent();

        this.contactSelected = this.getContactFromIntent(intentMainActivity);

        this.txtName = (EditText)findViewById(R.id.txtName);
        this.txtMessage = (EditText) findViewById(R.id.txtMessage);
        this.txtDOB = (EditText) findViewById(R.id.txtDOB);
        this.spnPhone = (Spinner)findViewById(R.id.spnPhone);
        this.photoContact = (ImageView)findViewById(R.id.photoContact);
        this.chbSMS = (CheckBox)findViewById(R.id.chbSendSMS);

        this.chbSMS.setOnCheckedChangeListener(this);
        this.spnPhone.setOnItemSelectedListener(this);

        new getContactPhonesAsyncTask().execute();

    }



    @Override
    public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {

        if (isChecked){

            this.txtMessage.setEnabled(true);

        }else{

            this.txtMessage.setEnabled(false);
        }
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        this.contactSelected.setPhone(parent.getItemAtPosition(pos).toString());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACTS_APP) {

            new updateContactDisplayedAsyncTask().execute();

        }
    }


    /*********************************************************
     * Método que abre la agenda con el contacto seleccionado.
     * @param v - Botón pulsado
     *********************************************************/

    public void openContactToView (View v){

        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactSelected.getID()));
        Intent contactIntent = new Intent(Intent.ACTION_VIEW, uri);

        startActivityForResult(contactIntent, CONTACTS_APP);
    }


    /**********************************************************************
     * Método para recuperar los datos del contacto selecionado que vienen
     * dentro del Intent enviado desde BirthdayHelpeMainActivity.
     * @param intent - Intent enviado desde BirthdayHelperMainActivity.
     * @return - Una instancia de ContactDB con sus datos.
     **********************************************************************/
    
    public ContactDB getContactFromIntent (Intent intent){

        ContactDB contactDB = new ContactDB();

        Log.i(CONTACT_DETAIL_ACTIVITY_LOG,"Getting contact from intent.");

        contactDB.setID(intent.getIntExtra(ID, 0));
        contactDB.setName(intent.getStringExtra(NAME));
        contactDB.setPhone(intent.getStringExtra(PHONE));
        contactDB.setDOB(intent.getStringExtra(DOB));
        contactDB.setNotificationId(intent.getStringExtra(NOTIFICATION_ID));
        contactDB.setMessage(intent.getStringExtra(MESSAGE));
        contactDB.setPhoto((Bitmap) intent.getParcelableExtra(PHOTO));

        return contactDB;
    }


    /*********************************************
     * Método que muestra los datos del contacto.
     *********************************************/

    public void displayContact (){

        this.photoContact.setImageBitmap(contactSelected.getPhoto());
        this.txtName.setText(contactSelected.getName());
        this.txtMessage.setText(contactSelected.getMessage());
        this.txtDOB.setText(contactSelected.getDOB());

        if (contactSelected.getNotificationId() != null && contactSelected.getNotificationId().equals(SEND_SMS)){

            this.chbSMS.setChecked(true);

        }else{

            this.chbSMS.setChecked(false);
        }

        ArrayAdapter<String> adapterPhones = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.listContactPhones);
        adapterPhones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spnPhone.setAdapter(adapterPhones);

        this.spnPhone.setSelection(this.listContactPhones.indexOf(contactSelected.getPhone()));

        Log.i(CONTACT_DETAIL_ACTIVITY_LOG, "Contact displayed.");

    }


    /*******************************************************
     * Método que guarda los datos del contacto en la BBDD.
     * @param view - Botón guardar
     *******************************************************/

    public void saveContactDisplayed (View view){

        this.contactSelected.setDOB(txtDOB.getText().toString());
        this.contactSelected.setMessage(txtMessage.getText().toString());
        this.contactSelected.setPhone(spnPhone.getSelectedItem().toString());

        if (chbSMS.isChecked()){

            this.contactSelected.setNotificationId(SEND_SMS);

        }else{

            this.contactSelected.setNotificationId(JUST_NOTIFICATION);

        }

        try {

            actionsDB.saveContactDB(this.contactSelected);

            Toast.makeText(getApplicationContext(), R.string.contactSaveSuccess, Toast.LENGTH_SHORT).show();

            Log.i(CONTACT_DETAIL_ACTIVITY_LOG, "Contact has been sent successfuly.");

        } catch (SQLException e) {

            Toast.makeText(getApplicationContext(), R.string.errorSavingContact, Toast.LENGTH_SHORT).show();

            Log.e(CONTACT_DETAIL_ACTIVITY_LOG, "ERROR. Contact has not been sent." + e.getMessage());

            e.printStackTrace();


        }


    }


    /************************************************************
     * Método llamado asicronamente para recuperar los teléfonos 
     * de la agenda del contacto si este tuviera más de uno.
     **********************************************************/

    private class getContactPhonesAsyncTask extends AsyncTask<Void,Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(CONTACT_DETAIL_ACTIVITY_LOG,"Asynchronous call to get contact's phones from contacts app.");

            Cursor cursorPhones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{String.valueOf(contactSelected.getID())}, null);

            if (cursorPhones.getCount() > 0) {

                listContactPhones = new ArrayList<>();

                while (cursorPhones.moveToNext()) {

                    String phoneNumber = cursorPhones.getString(cursorPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    listContactPhones.add(phoneNumber);
                }


            }

            cursorPhones.close();

            return true;
        }



        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if(result) {

                displayContact();

            }
        }

    }


    /**********************************************************
     * Método llamado asicronamente para recuperar el contacto 
     * seleccionado de la agenda, comprobar si ha sido 
     * modificado y actualizar los datos mostrados.
     **********************************************************/

    private class updateContactDisplayedAsyncTask extends AsyncTask<Void,Void, Boolean> {

        String name;

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.i(CONTACT_DETAIL_ACTIVITY_LOG,"Asynchronous call to get contact update from contacts app.");

            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts._ID + " = ?", new String[]{String.valueOf(contactSelected.getID())}, null);

            cursor.moveToNext();

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            if (!name.equals(contactSelected.getName())){

                contactSelected.setName(name);

            }

            cursor.close();

            return true;

        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if(result) {

                txtName.setText(name);

            }
        }

    }

}
