package com.example.joseramonparreno.birthdayhelper;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by txoksue on 22/4/16.
 */
 
 /******************************************************
 * BroadcastReceiver para recibir la alarma establecida
 * por el usuario y poder lanzar el servicio que manda
 * los SMS y muestra la notificación.
 *******************************************************/
 
public class AlarmReceiver extends BroadcastReceiver {

    public static final String ALARM_RECEIVER_LOG = "AlarmReceiver";


    /**************
     * Constructor
     * ************/
    
    public AlarmReceiver() {
        super();
    }


    /***********************************************************
     * Método onReceive llamado cuando la alarma es establecida
     * desde BirthdayHelperMainActivity.
     * @param context - Contexto de la aplicación.
     * @param intent - Intent de la llamada.
     ************************************************************/
     
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(ALARM_RECEIVER_LOG, "onReceive method.");

        Intent birthdayService = new Intent(context, BirthdaysService.class);
        context.startService(birthdayService);

    }



}
