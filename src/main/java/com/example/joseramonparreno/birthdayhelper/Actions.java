package com.example.joseramonparreno.birthdayhelper;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by txoksue on 4/4/16.
 */
public class Actions{


    public static final String ACTIONS_LOG = "Actions";


    /*********************************************************************
     * Método que coge todos los contactos de la agenda del móvil
     * @param contentResolver - Content Resolver para acceder a la agenda 
     * @return - Lista de contactos
     *********************************************************************/


    public static ArrayList<Contact> getAllContactsMobile (ContentResolver contentResolver){

        ArrayList <Contact> contactsMobileList = new ArrayList<>();

        String[] proyeccion = {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_ID};

        Cursor cursorContactsMobile = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, proyeccion, null, null, null);

        if(cursorContactsMobile.getCount() > 0) {

            Log.i(ACTIONS_LOG, "Getting contact from mobile app.");

            while (cursorContactsMobile.moveToNext()) {

                Contact contactMobile = new Contact();

                String idContactMobile = cursorContactsMobile.getString(cursorContactsMobile.getColumnIndex(ContactsContract.Contacts._ID));
                String nameContactMobile = cursorContactsMobile.getString(cursorContactsMobile.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactHasPhone = cursorContactsMobile.getString(cursorContactsMobile.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                contactMobile.setID(Integer.parseInt(idContactMobile));
                contactMobile.setName(nameContactMobile);

                if (Integer.parseInt(contactHasPhone) > 0) {

                    Cursor cursorPhone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{idContactMobile}, null);

                    while(cursorPhone.moveToNext()) {

                        String phone = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                        contactMobile.setPhone(phone);

                    }

                    cursorPhone.close();
                }

                contactsMobileList.add(contactMobile);

            }

            cursorContactsMobile.close();

        }

        return contactsMobileList;
    }


    /******************************************************************
     * Método que recupera la foto que un contacto tiene en la agenda.
     * @param contentResolver - Content Resolver para acceder a la agenda
     * @param contact - Contacto para buscar su foto.
     * @return - Bitmap con la foto del contacto.
     ******************************************************************/

    public static Bitmap getImageContactMobile(ContentResolver contentResolver, Contact contact){

        Log.i(ACTIONS_LOG, "Getting contact's photo.");

        Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contact.getID()));

        InputStream photo_stream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver,contactUri);

        BufferedInputStream buffer = new BufferedInputStream(photo_stream);

        Bitmap myBtmp = BitmapFactory.decodeStream(buffer);

        return myBtmp;

    }



}
