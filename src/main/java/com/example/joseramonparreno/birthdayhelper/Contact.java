package com.example.joseramonparreno.birthdayhelper;

import android.graphics.Bitmap;

/**
 * Created by txoksue on 2/4/16.
 */
 
/*************************
 * Clase para un contacto
 * de la agenda.
 *************************/
 
public class Contact {

    public int ID;
    public String name;
    public String phone;
    public Bitmap photo;

    public int getID() {
        return ID;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
