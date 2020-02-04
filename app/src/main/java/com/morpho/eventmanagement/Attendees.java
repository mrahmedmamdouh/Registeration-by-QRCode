package com.morpho.eventmanagement;

public class Attendees {

    private String VIP;
    private String CATEGORY;
    private String Last_Name;
    private String First_Name;
    private String Fonction;
    private String Entity;
    private String Email;
    private String Phone_Number;

    public Attendees(String VIP, String CATEGORY, String last_Name, String first_Name, String fonction, String entity, String email, String phone_Number) {
        this.VIP = VIP;
        this.CATEGORY = CATEGORY;
        Last_Name = last_Name;
        First_Name = first_Name;
        Fonction = fonction;
        Entity = entity;
        Email = email;
        Phone_Number = phone_Number;
    }

    public String getVIP() {
        return VIP;
    }

    public void setVIP(String VIP) {
        this.VIP = VIP;
    }

    public String getCATEGORY() {
        return CATEGORY;
    }

    public void setCATEGORY(String CATEGORY) {
        this.CATEGORY = CATEGORY;
    }

    public String getLast_Name() {
        return Last_Name;
    }

    public void setLast_Name(String last_Name) {
        Last_Name = last_Name;
    }

    public String getFirst_Name() {
        return First_Name;
    }

    public void setFirst_Name(String first_Name) {
        First_Name = first_Name;
    }

    public String getFonction() {
        return Fonction;
    }

    public void setFonction(String fonction) {
        Fonction = fonction;
    }

    public String getEntity() {
        return Entity;
    }

    public void setEntity(String entity) {
        Entity = entity;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhone_Number() {
        return Phone_Number;
    }

    public void setPhone_Number(String phone_Number) {
        Phone_Number = phone_Number;
    }
}
