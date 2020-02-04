package com.morpho.eventmanagement;

public class Attendance {
    private String Att_Flag;
    private String date;
    private String time;
    private String vip;
    private String last_name;
    private String first_name;
    private String Title;



    public Attendance(String att_Flag, String date, String time, String vip, String last_name, String first_name, String title) {
        this.Att_Flag = att_Flag;
        this.date = date;
        this.time = time;
        this.vip = vip;
        this.last_name = last_name;
        this.first_name = first_name;
        this.Title = title;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getAtt_Flag() {
        return Att_Flag;
    }

    public void setAtt_Flag(String att_Flag) {
        Att_Flag = att_Flag;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }
}
