package com.example.joseramonparreno.birthdayhelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by txoksue on 24/4/16.
 */
public class BirthdaysService extends Service{

    SQLiteDatabase databaseApp;
    ArrayList<String> listContactsNotifications;
    HashMap<String,String> listContactsSMS;

    public static final String BIRTHDAY_SERVICE_LOG = "BirthdaysService";
    
    public static final String SELECT_ALL_CONTACTS_SMS = "SELECT * FROM miscumples WHERE TipoNotif = 1";
    public static final String SELECT_ALL_CONTACTS_NOTIFICATION = "SELECT * FROM miscumples WHERE TipoNotif = 2";

    public static final int COLUMN_MESSAGE = 2;
    public static final int COLUMN_PHONE = 3;
    public static final int COLUMN_DOB = 4;
    public static final int COLUMN_NAME = 5;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        
        //Este es un servicio no vinculado, por eso devolvemos null
        
        Log.i(BIRTHDAY_SERVICE_LOG, "Birthdays service bound.");
        
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(BIRTHDAY_SERVICE_LOG, "Creating Birthdays service.");

        listContactsNotifications = new ArrayList<>();
        listContactsSMS = new HashMap<>();

        /*******************
         * Abrimos la BBDD.
         * *****************/
         
        this.databaseApp = openOrCreateDatabase("BirthDayHelper",MODE_PRIVATE, null);

        Log.i(BIRTHDAY_SERVICE_LOG, "Database open.");

        this.getContactsForNotification();
        this.getContactsForSMS();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(BIRTHDAY_SERVICE_LOG, "Birthdays service destroyed.");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(BIRTHDAY_SERVICE_LOG, "Birthdays service started.");

        if (this.listContactsNotifications.size() > 0) {

            this.createNotifications();
        }

        if (this.listContactsSMS.size() > 0){

            this.sendSMS();
        }

        Log.i(BIRTHDAY_SERVICE_LOG, "Stopping Birthdays service.");

        /**************************************
         * Una vez que se han enviado los SMS 
         * y que se ha mostrado la notificación
         * paramos el servicio.
         * ************************************/
         
        stopService(intent);

        return super.onStartCommand(intent, flags, startId);

    }


    /************************************************************************
     * Método que recupera de la BBDD el nombre de los contactos que cumplen
     * años hoy y que tienen la notificación como configuración.
     * **********************************************************************/

    public void getContactsForNotification(){

        Cursor cursorContacts = databaseApp.rawQuery(SELECT_ALL_CONTACTS_NOTIFICATION, null);
        
        //Cogemos la fecha actual para comparar día y mes con la fecha de cumpleaños del contacto
        
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateNow = dateFormat.format(currentDate.getTime());

        String dayNow = dateNow.substring(0,2);
        String monthNow = dateNow.substring(3,5);

        if (cursorContacts.getCount() > 0){

            while (cursorContacts.moveToNext()){

                String dayDOB = cursorContacts.getString(COLUMN_DOB).substring(0,2);
                String monthDOB = cursorContacts.getString(COLUMN_DOB).substring(3,5);

                if (dayDOB.equals(dayNow) &&  monthDOB.equals(monthNow)) {

                    this.listContactsNotifications.add(cursorContacts.getString(COLUMN_NAME));
                }
            }

        }

    }

    /*******************************************************
     * Método que recupera de la BBDD el teléfono y mensaje 
     * a enviar a los contactos que cumplen años hoy 
     * y que tienen SMS como configuración.
     * *****************************************************/

    public void getContactsForSMS(){

        Cursor cursorContacts = databaseApp.rawQuery(SELECT_ALL_CONTACTS_SMS, null);

        if (cursorContacts.getCount() > 0){

            while (cursorContacts.moveToNext()){

                String phone = cursorContacts.getString(COLUMN_PHONE);
                String message = cursorContacts.getString(COLUMN_MESSAGE);

                // Añadimos el teléfono como key de un HashMap y el mensaje como value.
                this.listContactsSMS.put(phone, message);

            }

        }

    }


    /**************************************************************
     * Método que envia la notificación a mostrar. Diferencia entre
     * si la notificación va a mostrar un solo contacto o si
     * va a mostrar varios, con lo que sería una expandible.
     * ************************************************************/

    public void createNotifications(){

        int notifId = 1;

        int sizelistContacts = this.listContactsNotifications.size();

        NotificationCompat.Builder constructorNotif = new NotificationCompat.Builder(this);

        constructorNotif.setSmallIcon(R.drawable.birthdayicon);

        if (sizelistContacts > 1){

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            inboxStyle.setBigContentTitle("Hoy es el cumpleaños de:");

            for (int i=0; i<sizelistContacts; i++){

                inboxStyle.addLine(this.listContactsNotifications.get(i).toString());

            }

            constructorNotif.setStyle(inboxStyle);


        }else{

            constructorNotif.setContentTitle(getString(R.string.titleNotification));

            constructorNotif.setContentText(this.listContactsNotifications.get(0).toString());

        }

       /* Intent resultIntent = new Intent(this, BirthHelperMainActivity.class);

        TaskStackBuilder pila = TaskStackBuilder.create(this);
        pila.addParentStack(BirthHelperMainActivity.class);

        pila.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = pila.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        constructorNotif.setContentIntent(resultPendingIntent);*/

        constructorNotif.setWhen(0);
        constructorNotif.setPriority(Notification.PRIORITY_MAX);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notifId, constructorNotif.build());

        Log.i(BIRTHDAY_SERVICE_LOG, "Notification has been sent.");

    }


    /*******************************************
     * Método que envia los SMS a los contactos.
     * *****************************************/

    public void sendSMS(){

        try{

            SmsManager smsManager = SmsManager.getDefault();

            Iterator it = listContactsSMS.keySet().iterator();

            while (it.hasNext()) {

                String contactMobile = (String) it.next();

                smsManager.sendTextMessage(contactMobile, null, listContactsSMS.get(contactMobile), null, null);

                Log.i(BIRTHDAY_SERVICE_LOG, "SMS has been sent to " + contactMobile + "mobile.");
            }


        } catch (Exception e) {

                Log.e(BIRTHDAY_SERVICE_LOG, "ERROR. SMS has not been sent. " + e.getMessage());

                e.printStackTrace();
        }
    }
}
