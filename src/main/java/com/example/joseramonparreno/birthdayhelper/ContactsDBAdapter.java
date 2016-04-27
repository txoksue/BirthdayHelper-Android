package com.example.joseramonparreno.birthdayhelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by txoksue on 31/3/16.
 */
 
 /***********************************************************
  * Custom adapter para poder mostrar los datos del contacto
  * en la fila del ListView tal como se muestran y para el 
  * que hemos creado un layout propio llamado row_style.
  ***********************************************************/
 
public class ContactsDBAdapter extends ArrayAdapter<ContactDB> {

        public ContactsDBAdapter(Context context, ArrayList<ContactDB> contactsBD) {
            super(context, 0, contactsBD);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            ContactDB contact = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_style, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.txvName);
            TextView tvPhone = (TextView) convertView.findViewById(R.id.txvPhone);
            TextView tvTypeNotif = (TextView) convertView.findViewById(R.id.txvNotificaciton);
            ImageView photoContact = (ImageView)convertView.findViewById(R.id.photoContact);
            // Populate the data into the template view using the data object
            tvName.setText(contact.getName());
            tvPhone.setText(contact.getPhone());
            photoContact.setImageBitmap(contact.getPhoto());

            if (contact.getNotificationId().equals("1")){

                tvTypeNotif.setText(R.string.label_send_sms);

            }else{

                tvTypeNotif.setText(R.string.lbl_notification);
            }


            // Return the completed view to render on screen
            return convertView;
        }
}
