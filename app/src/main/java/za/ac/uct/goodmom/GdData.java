package za.ac.uct.goodmom;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GdData implements Comparable<GdData> {
    private double mGlucose, mActivityTime, mWeight;
    private long mDateTime;
    private String mLocation, mMeal, mActivityDescription, mMedication;
    private int mCarbs;

    public GdData() {}

    public GdData(double glucose, double activityTime, double weight, long dateTime, String location,
                  String meal, String activityDescription, String medication, int carbs) {
        mGlucose = glucose;
        mActivityTime = activityTime;
        mWeight = weight;
        mDateTime = dateTime;
        mLocation = location;
        mMeal = meal;
        mActivityDescription = activityDescription;
        mMedication = medication;
        mCarbs = carbs;
    }

    public String getMedication() {
        return mMedication;
    }

    public String getMeal() {
        return mMeal;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getActivityDescription() {
        return mActivityDescription;
    }

    public long getDateTime() {
        return mDateTime;
    }

    public int getCarbs() {
        return mCarbs;
    }

    public double getWeight() {
        return mWeight;
    }

    public double getGlucose() {
        return mGlucose;
    }

    public double getActivityTime() {
        return mActivityTime;
    }

    public void setMeal(String meal) {
        mMeal = meal;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public void setGlucose(double glucose) {
        this.mGlucose = glucose;
    }

    public void setDateTime(long dateTime) {
        mDateTime = dateTime;
    }

    public void setCarbs(int carbs) {
        mCarbs = carbs;
    }

    public void setActivityTime(double activityTime) {
        mActivityTime = activityTime;
    }

    public void setActivityDescription(String activityDescription) {
        mActivityDescription = activityDescription;
    }

    public void setMedication(String mMedication) {
        this.mMedication = mMedication;
    }

    public void setWeight(double mWeight) {
        this.mWeight = mWeight;
    }

    public Date getDateObject(long dateTime) {
        Date dateObject = new Date(dateTime);
        return dateObject;
    }

    public int getHoursOfMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hours = Integer.parseInt(sdf.format(new Date(mDateTime)));
        sdf = new SimpleDateFormat("dd");
        hours += (Integer.parseInt(sdf.format(new Date(mDateTime))) - 1) * 24;
        return hours;
    }

    public int getMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        return Integer.parseInt(sdf.format(new Date(mDateTime)));
    }

    @Override
    public int compareTo(GdData comparePost) {

        return getDateObject(mDateTime).compareTo(getDateObject(comparePost.getDateTime()));
    }
}
