package za.ac.uct.goodmom;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostData implements Comparable<PostData> {

    /** Tag for the log messages */
    public static final String LOG_TAG = PostData.class.getSimpleName();

    private String mUrl;
    private String mTitle;
    private String mDate;
    private String mThumbUrl;

    public PostData(){}

    public PostData(String url, String title, String date, String thumbUrl){
        mUrl = url;
        mTitle = title;
        mDate = date;
        mThumbUrl = thumbUrl;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDate() {
        return mDate;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public void setUrl(String thumbUrl) {
        mUrl = thumbUrl;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setThumbUrl(String thumbUrl) {
        mThumbUrl = thumbUrl;
    }

    public Date getDateObject(String date) {
        Date dateObject = null;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy");
        try {
            dateObject = sdf.parse(date);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error with pull parse", e);
        }
        return dateObject;
    }

    @Override
    public int compareTo(PostData comparePost) {

        return getDateObject(comparePost.getDate()).compareTo(getDateObject(mDate));
    }
}
