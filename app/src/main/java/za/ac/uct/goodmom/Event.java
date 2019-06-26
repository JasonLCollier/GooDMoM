package za.ac.uct.goodmom;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Event implements Parcelable, Comparable<Event> {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = Event.class.getSimpleName();
    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }

        @Override
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }
    };
    private long mStartDateTime;
    private long mEndDateTime;
    private int mEventType;
    private String mTitle;
    private String mLocation;
    private String mDescription;

    public Event() {
    }

    public Event(int reminderType, String title, String location, String description, long startDateTime, long endDateTime) {
        mEventType = reminderType;
        mTitle = title;
        mLocation = location;
        mDescription = description;
        mStartDateTime = startDateTime;
        mEndDateTime = endDateTime;
    }

    public Event(Parcel source) {
        mEventType = source.readInt();
        mTitle = source.readString();
        mLocation = source.readString();
        mDescription = source.readString();
        mStartDateTime = source.readLong();
        mEndDateTime = source.readLong();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getEventType() {
        return mEventType;
    }

    public void setEventType(int mEventType) {
        this.mEventType = mEventType;
    }

    public long getStartDateTime() {
        return mStartDateTime;
    }

    public void setStartDateTime(long mStartDateTime) {
        this.mStartDateTime = mStartDateTime;
    }

    public long getEndDateTime() {
        return mEndDateTime;
    }

    public void setEndDateTime(long mEndDateTime) {
        this.mEndDateTime = mEndDateTime;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String mLocation) {
        this.mLocation = mLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mEventType);
        dest.writeString(mTitle);
        dest.writeString(mLocation);
        dest.writeString(mDescription);
        dest.writeLong(mStartDateTime);
        dest.writeLong(mEndDateTime);
    }

    public Date getDateObject(long dateTime) {
        Date dateObject = new Date(dateTime);
        return dateObject;
    }

    @Override
    public int compareTo(Event comparePost) {
        return getDateObject(comparePost.getStartDateTime()).compareTo(getDateObject(mStartDateTime));
    }

}
