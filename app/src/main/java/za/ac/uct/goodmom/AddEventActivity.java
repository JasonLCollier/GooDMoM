package za.ac.uct.goodmom;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class AddEventActivity extends AppCompatActivity {


    // Tag for the log messages
    public static final String LOG_TAG = AddEventActivity.class.getSimpleName();

    private EditText mTitleText, mRepeatPeriodText, mRepeatCountText, mLocationText, mDescriptionText;
    private TextView mStartDateText, mEndDateText, mStartTimeText, mEndTimeText;
    private Spinner mEventTypeSpinner, mDaySpinner;
    private Button mCreateEventButton;

    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;

    private int mYear, mMonth, mDay, mHour, mMinute, mEventType, mDayOfWeek;
    private String mTitleStr, mRepeatPeriodStr, mRepeatCountStr, mLocationStr,
            mDescriptionStr, mStartDateStr, mEndDateStr, mStartTimeStr, mEndTimeStr;

    private ArrayList<Event> mEventList = new ArrayList<>();
    private Event mNewEvent;

    private String mUsername, mUserId;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Initialise Firebase components
        mFirebasedatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mUsername = user.getDisplayName();
        mUserId = user.getUid();
        mMessagesDatabaseReference = mFirebasedatabase.getReference().child("events").child(mUserId);

        // Initialise references to views
        mTitleText = findViewById(R.id.title);
        mRepeatPeriodText = findViewById(R.id.repeat_period);
        mRepeatCountText = findViewById(R.id.repeat_count);
        mLocationText = findViewById(R.id.location);
        mDescriptionText = findViewById(R.id.description);
        mStartDateText = findViewById(R.id.start_date);
        mEndDateText = findViewById(R.id.end_date);
        mStartTimeText = findViewById(R.id.start_time);
        mEndTimeText = findViewById(R.id.end_time);
        mEventTypeSpinner = findViewById(R.id.event_type);
        mDaySpinner = findViewById(R.id.day);
        mCreateEventButton = findViewById(R.id.create_event_button);

        // Calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR); // current year
        mMonth = c.get(Calendar.MONTH); // current month
        mDay = c.get(Calendar.DAY_OF_MONTH); // current day
        mHour = c.get(Calendar.HOUR_OF_DAY); // current hour
        mMinute = c.get(Calendar.MINUTE); // current minute

        // Update date and time views
        mStartDateStr = mDay + "/" + (mMonth + 1) + "/" + mYear;
        mEndDateStr = mStartDateStr;
        mStartTimeStr = createTimeString(mHour, 0);
        mEndTimeStr = createTimeString(mHour + 1, 0);
        mStartDateText.setText(formatDisplayDate(mStartDateStr));
        mEndDateText.setText(formatDisplayDate(mEndDateStr));
        mStartTimeText.setText(mStartTimeStr);
        mEndTimeText.setText(mEndTimeStr);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mEventTypeSpinner.setAdapter(adapter);

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this,
                R.array.day_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mDaySpinner.setAdapter(adapter);


        mEventTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mEventType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mEventType = 0;
            }
        });

        mDaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDayOfWeek = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDayOfWeek = 0;
            }
        });

        mStartDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // date picker dialog
                mDatePickerDialog = new DatePickerDialog(AddEventActivity.this, R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                mStartDateStr = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                mStartDateText.setText(formatDisplayDate(mStartDateStr));

                                // update end date if start date is after end date
                                if (createDateObject(mStartDateStr).compareTo(createDateObject(mEndDateStr)) > 0)
                                    mEndDateText.setText(formatDisplayDate(mStartDateStr));

                            }
                        }, mYear, mMonth, mDay);
                mDatePickerDialog.show();
            }
        });

        mEndDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Date picker dialog
                mDatePickerDialog = new DatePickerDialog(AddEventActivity.this, R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                mEndDateStr = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                mEndDateText.setText(formatDisplayDate(mEndDateStr));

                            }
                        }, mYear, mMonth, mDay);
                mDatePickerDialog.show();
            }
        });

        mStartTimeText.setOnClickListener(new View.OnClickListener() {

            // Tme picker dialog
            @Override
            public void onClick(View v) {

                mTimePickerDialog = new TimePickerDialog(AddEventActivity.this, R.style.DialogTheme,
                        new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        // set hour of day and minute value in the edit text
                        mStartTimeStr = createTimeString(selectedHour, selectedMinute);
                        mStartTimeText.setText(mStartTimeStr);
                    }
                }, mHour, mMinute, true);// Yes 24 hour time
                mTimePickerDialog.show();

            }
        });

        mEndTimeText.setOnClickListener(new View.OnClickListener() {

            // Tme picker dialog
            @Override
            public void onClick(View v) {

                mTimePickerDialog = new TimePickerDialog(AddEventActivity.this, R.style.DialogTheme,
                        new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        // set hour of day and minute value in the edit text
                        mEndTimeStr = createTimeString(selectedHour, selectedMinute);
                        mEndTimeText.setText(mEndTimeStr);
                    }
                }, mHour, mMinute, true);// Yes 24 hour time
                mTimePickerDialog.show();

            }
        });

        mCreateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Assign EditText values to variables
                mTitleStr = mTitleText.getText().toString();
                mRepeatPeriodStr = mRepeatPeriodText.getText().toString();
                mRepeatCountStr = mRepeatCountText.getText().toString();
                mLocationStr = mLocationText.getText().toString();
                mDescriptionStr = mDescriptionText.getText().toString();

                // Create new event
                mNewEvent = new Event(mEventType, mTitleStr, mLocationStr, mDescriptionStr,
                        convertDateTimeToMillis(mStartDateStr, mStartTimeStr), convertDateTimeToMillis(mEndDateStr, mEndTimeStr));

                // Push new event to database
                mMessagesDatabaseReference.push().setValue(mNewEvent);

                // Return to RemindersActivity with updated event list
                Intent newEventIntent = new Intent(AddEventActivity.this, RemindersActivity.class);
                startActivity(newEventIntent);
            }
        });

    }

    public void createEventIntent(String title, String location, long begin, long end) {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                .putExtra(CalendarContract.Events.DESCRIPTION, "Created by GooDMoM App")
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
                .putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY=MO;INTERVAL=2;COUNT=5")
                .putExtra(Intent.EXTRA_EMAIL, "hp@mail.com")
                .putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public long convertDateTimeToMillis(String dateStr, String timeStr) {
        Date dateTime = null;
        SimpleDateFormat mSdf = new SimpleDateFormat("dd/MM/yyyy/hh:mm");
        try {
            dateTime = mSdf.parse(dateStr + "/" + timeStr);
        } catch (ParseException e) {

            Log.e(LOG_TAG, "Error with pull parse", e);
        }
        return dateTime.getTime();
    }

    public String createTimeString(int hour, int minute) {
        String timeStr;

        if (hour < 10)
            timeStr = "0" + hour;
        else
            timeStr = "" + hour;

        timeStr += ":";

        if (minute < 10)
            timeStr += ("0" + minute);
        else
            timeStr += minute;

        return timeStr;
    }

    public String formatDisplayDate(String srcDate) {
        String destString = null;

        SimpleDateFormat srcFormat = new SimpleDateFormat("d/M/yyyy");
        SimpleDateFormat destFormat = new SimpleDateFormat("EEE, d MMM yyyy");

        try {
            Date dateObj = srcFormat.parse(srcDate);
            destString = destFormat.format(dateObj);
        } catch (ParseException e) {

            Log.e(LOG_TAG, "Error with pull parse", e);
        }
        return destString;

    }

    public Date createDateObject(String dateStr) {
        Date dateObj = null;
        SimpleDateFormat format = new SimpleDateFormat("d/M/yyyy");

        try {
            dateObj = format.parse(dateStr);
        } catch (ParseException e) {

            Log.e(LOG_TAG, "Error with pull parse", e);
        }
        return dateObj;
    }

}