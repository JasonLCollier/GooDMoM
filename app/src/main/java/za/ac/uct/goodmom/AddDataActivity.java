package za.ac.uct.goodmom;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddDataActivity extends AppCompatActivity {

    // Tag for the log messages
    public static final String LOG_TAG = AddDataActivity.class.getSimpleName();

    private EditText mMealDescrText, mActivityDescrText, mMedicationText, mDoubleNumberText;
    private TextView mGlucoseText, mDateTimeText, mLocationText, mCarbsText, mActivityTimeText, mWeightText, mCancelButton, mOkButton;
    private LinearLayout mGlucoseContainer, mTimeContainer, mLocationContainer, mCarbsContainer, mWeightContainer;
    private Button mAddDataButton;

    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private Dialog mDialog;
    private NumberPicker mNumberPicker;

    private String mMealDescr, mActivityDescr, mMedication, mDateTime, mLocation, mActivityTime, mGlucoseStr, mCarbsStr, mWeightStr;
    private double mGlucose, mWeight;
    private int  mCarbs, mYear, mMonth, mDay, mHour, mMinute, mEventType, mDayOfWeek;

    private String mUsername, mUserId;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        // Initialise Firebase components
        mFirebasedatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mUsername = user.getDisplayName();
        mUserId = user.getUid();
        mMessagesDatabaseReference = mFirebasedatabase.getReference().child("gdData").child(mUserId);

        // Initialise references to views
        mGlucoseText = findViewById(R.id.glucose);
        mDateTimeText = findViewById(R.id.date_time);
        mLocationText = findViewById(R.id.location);
        mMealDescrText = findViewById(R.id.meal_val);
        mCarbsText = findViewById(R.id.carbs_val);
        mActivityDescrText = findViewById(R.id.activity_description_val);
        mActivityTimeText = findViewById(R.id.activity_time_val);
        mWeightText = findViewById(R.id.body_weight_val);
        mMedicationText = findViewById(R.id.medication_val);
        mAddDataButton = findViewById(R.id.add_data_button);
        mGlucoseContainer = findViewById(R.id.glucose_container);
        mTimeContainer = findViewById(R.id.time_container);
        mLocationContainer = findViewById(R.id.location_container);
        mCarbsContainer = findViewById(R.id.carbs_container);
        mWeightContainer = findViewById(R.id.weight_container);

        // Calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR); // current year
        mMonth = c.get(Calendar.MONTH); // current month
        mDay = c.get(Calendar.DAY_OF_MONTH); // current day
        mHour = c.get(Calendar.HOUR_OF_DAY); // current hour
        mMinute = c.get(Calendar.MINUTE); // current minute

        // Initialise glucose view
        mGlucoseStr = "0.0";
        mGlucoseText.setText(mGlucoseStr);
        // Initialise date time view
        mDateTime = mDay + "/" + (mMonth + 1) + "/" + mYear + "/" + createTimeString(mHour, mMinute);
        mDateTimeText.setText(formatDisplayDate(mDateTime));
        // Initialise location view
        mLocation = "Click to find location";
        mLocationText.setText(mLocation);
        // Initialise carbs view
        mCarbsStr = "0";
        mCarbsText.setText(mCarbsStr);

       // On click listener for glucose value
        mGlucoseContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set up dialog
                mDialog = new Dialog(AddDataActivity.this);
                mDialog.setContentView(R.layout.dialog_edit_text);

                mDoubleNumberText = mDialog.findViewById(R.id.number_val);

                mCancelButton = mDialog.findViewById(R.id.cancel_button);
                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Exit dialog
                        mDialog.dismiss();
                    }
                });

                mOkButton = mDialog.findViewById(R.id.ok_button);
                mOkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get the glucose value
                        mGlucoseStr = mDoubleNumberText.getText().toString();
                        mGlucose = Double.valueOf(mGlucoseStr);
                        mGlucoseText.setText(mGlucoseStr);

                        // Exit dialog
                        mDialog.dismiss();
                    }
                });

                mDialog.show();
            }
        });

        // On click listener for date time value
        mTimeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // time picker dialog
                mTimePickerDialog = new TimePickerDialog(AddDataActivity.this, R.style.DialogTheme,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                // set hour of day and minute value in the edit text
                                mDateTime += createTimeString(selectedHour, selectedMinute);

                                mDateTimeText.setText(formatDisplayDate(mDateTime));
                            }
                        }, mHour, mMinute, true);// Yes 24 hour time
                mTimePickerDialog.show();

                // date picker dialog
                mDatePickerDialog = new DatePickerDialog(AddDataActivity.this, R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                mDateTime = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year + "/";
                            }
                        }, mYear, mMonth, mDay);
                mDatePickerDialog.show();
            }
        });

        // On click listener for location value


        // On click listener for weight value
        mWeightContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set up dialog
                mDialog = new Dialog(AddDataActivity.this);
                mDialog.setContentView(R.layout.dialog_edit_text);

                mDoubleNumberText = mDialog.findViewById(R.id.number_val);

                mCancelButton = mDialog.findViewById(R.id.cancel_button);
                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Exit dialog
                        mDialog.dismiss();
                    }
                });

                mOkButton = mDialog.findViewById(R.id.ok_button);
                mOkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get the glucose value
                        mWeightStr = mDoubleNumberText.getText().toString();
                        mWeight = Double.valueOf(mWeightStr);
                        mWeightText.setText(mWeightStr);

                        // Exit dialog
                        mDialog.dismiss();
                    }
                });

                mDialog.show();
            }
        });

        // On click listener for activity time value


        //On click listener for carbs value
        mCarbsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set up dialog
                mDialog = new Dialog(AddDataActivity.this);
                mDialog.setContentView(R.layout.dialog_number_picker);

                // Set up number picker
                mNumberPicker = mDialog.findViewById(R.id.number_picker);
                mNumberPicker.setMinValue(0);
                mNumberPicker.setMaxValue(1000);

                mCancelButton = mDialog.findViewById(R.id.cancel_button);
                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Exit dialog
                        mDialog.dismiss();
                    }
                });

                mOkButton = mDialog.findViewById(R.id.ok_button);
                mOkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get the carbs value
                        mCarbs = mNumberPicker.getValue();
                        mCarbsStr = String.valueOf(mCarbs);
                        mCarbsText.setText(mCarbsStr);

                        // Exit dialog
                        mDialog.dismiss();
                    }
                });

                mDialog.show();
            }
        });

        // On click listener for add data button
        mAddDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from edit text views
                mMealDescr = mMealDescrText.getText().toString();
                mActivityDescr = mActivityDescrText.getText().toString();
                mMedication = mMedicationText.getText().toString();

                // Create new GdData Object


                // Push new event to database
                //mMessagesDatabaseReference.push().setValue();

                // Return to RemindersActivity with updated event list
                Intent newEventIntent = new Intent(AddDataActivity.this, DashboardActivity.class);
                startActivity(newEventIntent);

            }
        });

    }

    public String formatDisplayDate(String srcDate) {
        String destString = null;

        SimpleDateFormat srcFormat = new SimpleDateFormat("d/M/yyyy/HH:mm");
        SimpleDateFormat destFormat = new SimpleDateFormat("EEE, d MMM yyyy 'at' HH:mm");

        try {
            Date dateObj = srcFormat.parse(srcDate);
            destString = destFormat.format(dateObj);
        } catch (ParseException e) {

            Log.e(LOG_TAG, "Error with pull parse", e);
        }
        return destString;

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

}
