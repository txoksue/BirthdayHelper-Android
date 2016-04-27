package com.example.joseramonparreno.birthdayhelper;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by txoksue on 4/4/16.
 */
public class DBActions {

    public static final String DBACTIONS_LOG = "DBActions";
    public static final String SELECT_ALL_CONTACTS = "SELECT * FROM miscumples ORDER BY ID";
    public static final String INSERT_NEW_CONTACT = "INSERT INTO miscumples (ID, Telefono, Nombre, Noti) VALUES(";
    public static final String DELETE_CONTACT = "DELETE FROM miscumples WHERE ID=";

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_NOTIF_ID = 1;
    public static final int COLUMN_MESSAGE = 2;
    public static final int COLUMN_PHONE = 3;
    public static final int COLUMN_DOB = 4;
    public static final int COLUMN_NAME = 5;

    Actions actions;
    SQLiteDatabase dataBaseApp;

     
    /**************
     * Constructor
     **************/

    public DBActions(SQLiteDatabase dataBaseApp) {
        this.dataBaseApp = dataBaseApp;

    }


    /********************************************************************
     * Método que actualiza la BBDD con los contactos de la agenda.
     * @param contentResolver - Content Resolver para llamar a la agenda.
     ********************************************************************/

    public void updateContactsDB (ContentResolver contentResolver) {

        ArrayList<Contact> listAllContactsMobile = new ArrayList<>(actions.getAllContactsMobile(contentResolver));
        ArrayList<ContactDB> listAllContactsDB = new ArrayList<>(this.getAllContactsDB(this.dataBaseApp, contentResolver));

        this.searchNewContactsMobile(listAllContactsMobile, listAllContactsDB);
        this.searchContactsMobileDeleted(listAllContactsMobile, listAllContactsDB);
        this.searchAnyContactDataUpdated(listAllContactsMobile, listAllContactsDB);

        Log.i(DBACTIONS_LOG,"Contacts database has been updated successfuly.");

    }


    /**********************************************************************
     * Método para buscar contactos nuevos en la agenda y añadir a la BBDD.
     * @param contactsMobile - Contactos de la agenda.
     * @param contactsDB - Contactos de la BBDD.
     **********************************************************************/

    public void searchNewContactsMobile(ArrayList<Contact> contactsMobile, ArrayList<ContactDB> contactsDB) {

        int numContactsMobile = contactsMobile.size();
        int numContactsDB = contactsDB.size();

        boolean found;

        try {

            for (int i=0; i<numContactsMobile; i++){

                found = false;

                int idContactMobile = contactsMobile.get(i).getID();

                for(int j=0; j<numContactsDB; j++){

                    int idContactDB = contactsDB.get(j).getID();

                    if (idContactDB == idContactMobile){

                        found = true;
                    }
                }

                if (!found){

                    Log.i(DBACTIONS_LOG, "New contact to added to database found.");

                    this.insertContact(contactsMobile.get(i));

                    Log.i(DBACTIONS_LOG, "New contact has been added successfuly.");
                }

            }

        } catch (SQLException e) {

            Log.e(DBACTIONS_LOG, "ERROR. New contact has not been added. " + e.getMessage());

            e.printStackTrace();

        }

    }


 
    /**********************************************************************
     * Método para buscar contactos borrados de la agenda para borralos 
     * también de la BBDD.
     * @param contactsMobile - Contactos de la agenda.
     * @param contactsDB - Contactos de la BBDD.
     **********************************************************************/

    public void searchContactsMobileDeleted(ArrayList<Contact> contactsMobile, ArrayList<ContactDB> contactsDB) {

        int numContactsMobile = contactsMobile.size();
        int numContactsDB = contactsDB.size();

        boolean found;

        try {

            for (int i=0; i<numContactsDB; i++) {

                found = false;

                int idContactDB = contactsDB.get(i).getID();

                int j = 0;

                while (j < numContactsMobile && !found) {

                    int idContactMobile = contactsMobile.get(j).getID();

                    if (idContactMobile == idContactDB) {

                        found = true;

                    } else {

                        j++;
                    }
                }

                if (!found) {

                    Log.i(DBACTIONS_LOG, "Contact to delete in database found.");

                    this.deleteContact(contactsDB.get(i));

                    Log.i(DBACTIONS_LOG, "Contact has been deleted successfuly.");

                }

            }

        } catch (SQLException e) {

            Log.e(DBACTIONS_LOG, "ERROR. Contact has not been deleted. " + e.getMessage());

            e.printStackTrace();

        }

    }

    
    /**********************************************************************
     * Método para buscar datos actualizados de los contactos de la agenda
     * y actualizar la BBDD con ellos.
     * @param contactsMobile - Contactos de la agenda.
     * @param contactsDB - Contactos de la BBDD.
     **********************************************************************/

    public void searchAnyContactDataUpdated(ArrayList<Contact> contactsMobile, ArrayList<ContactDB> contactsDB){

        String nameContactMobile, nameContactDB, dobContactMobile, dobContactDB;
        HashMap <String, String> valuesToUpdated = new HashMap<>();
        int idContactDB, idContactMobile, j=0;
        boolean found;

        int numContactsMobile = contactsMobile.size();
        int numContactsDB = contactsDB.size();

        try {

            for (int i=0; i<numContactsDB; i++){

                found = false;

                idContactDB = contactsDB.get(i).getID();
                nameContactDB = contactsDB.get(i).getName();
               //dobContactDB = contactsDB.get(i).getDOB();

                while (j < numContactsMobile && !found){

                    idContactMobile = contactsMobile.get(j).getID();

                    if (idContactMobile == idContactDB){

                        found = true;

                        nameContactMobile = contactsMobile.get(j).getName();
                       // dobContactMobile = contactsMobile.get(j).getDOB();


                        if (!nameContactMobile.equals(nameContactDB)){

                            valuesToUpdated.put("Name", nameContactMobile);

                        }

                        /*
                        if(!dobContactMobile.equals(dobContactDB)){

                            valuesToUpdated.put("DOB", dobContactMobile);

                        }*/


                    }else{

                        j++;

                    }

                }

                if (!valuesToUpdated.isEmpty()) {

                    this.updateContact(valuesToUpdated, contactsDB.get(i));
                    valuesToUpdated.clear();
                }

            }


        } catch (SQLException e) {

            Log.e(DBACTIONS_LOG, "ERROR. Contact has not been updated. " + e.getMessage());

            e.printStackTrace();

        }

    }


    /*******************************************************
     * Método para recuperar todos los contactos de la BBDD.
     * @param dataBaseApp - Base de datos 
     * @param contentResolver - Content Resolver
     * @return - Lista con todos los contactos de la BBDD.
     *******************************************************/

    public ArrayList<ContactDB> getAllContactsDB (SQLiteDatabase dataBaseApp, ContentResolver contentResolver){

        ArrayList<ContactDB> listContactsDB = new ArrayList<>();

        Cursor cursorContactsDB = dataBaseApp.rawQuery(SELECT_ALL_CONTACTS, null);

        if (cursorContactsDB.getCount() > 0){

            while(cursorContactsDB.moveToNext()){

                ContactDB contact = new ContactDB();

                contact.setID(Integer.parseInt(cursorContactsDB.getString(COLUMN_ID)));
                contact.setNotificationId(cursorContactsDB.getString(COLUMN_NOTIF_ID));
                contact.setMessage(cursorContactsDB.getString(COLUMN_MESSAGE));
                contact.setPhone(cursorContactsDB.getString(COLUMN_PHONE));
                contact.setDOB(cursorContactsDB.getString(COLUMN_DOB));
                contact.setName(cursorContactsDB.getString(COLUMN_NAME));
                contact.setPhoto(actions.getImageContactMobile(contentResolver, contact));

                listContactsDB.add(contact);


            }

            cursorContactsDB.close();
        }

        return listContactsDB;

    }


    /**************************************************
     * Método que inserta un contacto nuevo en la BBDD.
     * @param contact - Contacto para insertar.
     * @throws SQLException
     **************************************************/

    public void insertContact (Contact contact) throws SQLException{

        this.dataBaseApp.execSQL(INSERT_NEW_CONTACT + contact.getID() + ",'" + contact.getPhone() + "','" + contact.getName() + "', TipoNotif = '2')");

    }


    /******************************************
     * Método que borra un contacto de la BBDD.
     * @param contact - Contacto a borrar.
     * @throws SQLException
     ******************************************/

    public void deleteContact (Contact contact) throws SQLException{

        this.dataBaseApp.execSQL(DELETE_CONTACT + contact.getID());

    }


    /********************************************************
     * Método que actualiza datos de un contacto en la BBDD, 
     * si estos han sido modificados en la agenda.
     * @param values - Valores para actualizar.
     * @param contact - Contacto a actualizar.
     * @throws SQLException
     ********************************************************/

    public void updateContact (HashMap <String, String> values, Contact contact) throws SQLException{

            String name, DOB;

            name = values.get("Name");
           // DOB = values.get("DOB");

            if (name != null){

                this.dataBaseApp.execSQL("UPDATE miscumples SET Nombre = '" + name + "' WHERE ID = " + contact.getID());
                Log.i(DBACTIONS_LOG, "Contact's name updated successfuly.");
            }

           /* if (DOB != null){

                this.dataBaseApp.execSQL("UPDATE miscumples SET FechaNacimiento = '" + DOB + "' WHERE ID = " + contact.getID());
                Log.i(DBACTIONS_LOG,"Contact's date of birth updated succesfuly.");
            }*/


    }


    /**********************************************************
     * Método para guardar los datos de un contacto en la BBDD.
     * @param contact -  Contacto para guardar.
     * @throws SQLException
     ***********************************************************/

    public void saveContactDB (ContactDB contact) throws SQLException{

        this.dataBaseApp.execSQL("UPDATE miscumples SET Nombre = '" + contact.getName() + "', Telefono = '" + contact.getPhone() + "', TipoNotif = '" + contact.getNotificationId() + "', FechaNacimiento = '" + contact.getDOB() + "', Mensaje = '" + contact.getMessage() + "' WHERE ID = " + contact.getID());

        Log.i(DBACTIONS_LOG, "Contact details have been saved successfuly.");

    }


}




