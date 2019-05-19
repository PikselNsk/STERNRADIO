package info.javaway.sternradio.model;

import java.util.Calendar;

import info.javaway.sternradio.App;
import info.javaway.sternradio.handler.PrefManager;

public class Alarm {

    private int hour;
    private int minute;

    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;


    public boolean isSingleAlarm() {
        return (!isMonday() &&
                !isTuesday() &&
                !isWednesday() &&
                !isThursday() &&
                !isFriday() &&
                !isSaturday() &&
                !isSunday());
    }

    public int checkEnableDay(int nowDay, int countDay) {

        if (nowDay == 8) nowDay = 1;
        switch (nowDay){
            case 2:{
                if(monday) return countDay;
                countDay++;
                break;
            }
            case 3:{
                if(tuesday) return countDay;
                countDay++;
                break;
            }
            case 4:{
                if(wednesday) return countDay;
                countDay++;
                break;
            }
            case 5:{
                if(thursday) return countDay;
                countDay++;
                break;
            }
            case 6:{
                if(friday) return countDay;
                countDay++;
                break;
            }
            case 7:{
                if(saturday) return countDay;
                countDay++;
                break;
            }
            case 1:{
                if(sunday) return countDay;
                countDay++;
                break;
            }
        }
        nowDay++;
        return checkEnableDay(nowDay, countDay);
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public boolean isSunday() {
        return sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }
}
