package za.ac.uct.goodmom;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GdData implements Comparable<GdData> {
    private double mGlucose, mActivityTime, mWeight;
    private long mDateTime;
    private String mLocation, mMeal, mActivityDescription, mMedication, mGlucoseTime, mBloodPressure, mSymptoms;
    private int mCarbs;

    public GdData() {
    }

    public GdData(double glucose, String glucoseTime, double activityTime, double weight, long dateTime, String location,
                  String meal, String activityDescription, String medication, int carbs, String bloodPressure, String symptoms) {
        mGlucose = glucose;
        mGlucoseTime = glucoseTime;
        mActivityTime = activityTime;
        mWeight = weight;
        mDateTime = dateTime;
        mLocation = location;
        mMeal = meal;
        mActivityDescription = activityDescription;
        mMedication = medication;
        mCarbs = carbs;
        mBloodPressure = bloodPressure;
        mSymptoms = symptoms;
    }

    public String getMedication() {
        return mMedication;
    }

    public void setMedication(String mMedication) {
        this.mMedication = mMedication;
    }

    public String getMeal() {
        return mMeal;
    }

    public void setMeal(String meal) {
        mMeal = meal;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public String getActivityDescription() {
        return mActivityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        mActivityDescription = activityDescription;
    }

    public long getDateTime() {
        return mDateTime;
    }

    public void setDateTime(long dateTime) {
        mDateTime = dateTime;
    }

    public int getCarbs() {
        return mCarbs;
    }

    public void setCarbs(int carbs) {
        mCarbs = carbs;
    }

    public double getWeight() {
        return mWeight;
    }

    public void setWeight(double mWeight) {
        this.mWeight = mWeight;
    }

    public double getGlucose() {
        return mGlucose;
    }

    public void setGlucose(double glucose) {
        this.mGlucose = glucose;
    }

    public double getActivityTime() {
        return mActivityTime;
    }

    public void setActivityTime(double activityTime) {
        mActivityTime = activityTime;
    }

    public String getGlucoseTime() {
        return mGlucoseTime;
    }

    public void setGlucoseTime(String glucoseTime) {
        mGlucoseTime = glucoseTime;
    }

    public String getBloodPressure() {
        return mBloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        mBloodPressure = bloodPressure;
    }

    public String getSymptoms() {
        return mSymptoms;
    }

    public void setSymptoms(String symptoms) {
        mSymptoms = symptoms;
    }

    public Date dateObject(long dateTime) {
        Date dateObject = new Date(dateTime);
        return dateObject;
    }

    public int hoursOfMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hours = Integer.parseInt(sdf.format(new Date(mDateTime)));
        sdf = new SimpleDateFormat("dd");
        hours += (Integer.parseInt(sdf.format(new Date(mDateTime))) - 1) * 24;
        return hours;
    }

    public int month() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        return Integer.parseInt(sdf.format(new Date(mDateTime)));
    }

    @Override
    public int compareTo(GdData comparePost) {

        return dateObject(mDateTime).compareTo(dateObject(comparePost.getDateTime()));
    }
}
