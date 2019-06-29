package za.ac.uct.goodmom;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    public static final String LOG_TAG = ProfileActivity.class.getSimpleName();

    private TextView mNameText, mIDText, mEmailText, mPhoneText, mAddressText, mDOBText,
            mHeightText, mWeightText, mBMIText, mDiabetesTypeText, mDueDateText, mHPText,
            mGlucoseRangeText, mWeightRangeText, mActivityGoalText, mMedicationText;

    private String mUsername, mUserId;

    // Due date calculator
    private Dialog mDialog;
    private DatePickerDialog mDatePickerDialog;
    private TextView mOkButton, mCancelButton, mConceptionDateText, mLastPeriodText, mCycleLengthText, mCalculatedDueDateText;
    private Spinner mCalculationMethodSpinner, mCycleLengthSpinner;
    private int mCalculationMethod, mCycleLengthVal, mYear, mMonth, mDay;
    private String mChosenDateStr, mDueDateStr;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mUsersDatabaseReference, mRangesDatabaseReference;
    private ValueEventListener mValueEventListener, mValueEventListenerForRanges;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialise firebase components
        mFirebasedatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mUsername = user.getDisplayName();
        mUserId = user.getUid();
        mUsersDatabaseReference = mFirebasedatabase.getReference().child("patients").child(mUserId).child("userData");
        mRangesDatabaseReference = mFirebasedatabase.getReference().child("patients").child(mUserId).child("userData").child("ranges");

        // Assign variables to views
        mNameText = findViewById(R.id.name_val);
        mIDText = findViewById(R.id.id_val);
        mEmailText = findViewById(R.id.email_val);
        mPhoneText = findViewById(R.id.phone_val);
        mAddressText = findViewById(R.id.address_val);
        mDOBText = findViewById(R.id.dob_val);
        mHeightText = findViewById(R.id.height_val);
        mWeightText = findViewById(R.id.weight_val);
        mBMIText = findViewById(R.id.bmi_val);
        mDiabetesTypeText = findViewById(R.id.diabetes_type_val);
        mDueDateText = findViewById(R.id.due_date_val);
        mHPText = findViewById(R.id.hp_val);
        mGlucoseRangeText = findViewById(R.id.glucose_range_val);
        mWeightRangeText = findViewById(R.id.weight_range_val);
        mActivityGoalText = findViewById(R.id.activity_goal_val);
        mMedicationText = findViewById(R.id.medication_val);

        mDueDateText.setOnClickListener(new View.OnClickListener() {

            // Tme picker dialog
            @Override
            public void onClick(View v) {
                // Initialise dialog
                mDialog = new Dialog(ProfileActivity.this);
                mDialog.setContentView(R.layout.dialog_calculate_due_date);

                // Assign variables to views
                mCalculationMethodSpinner = mDialog.findViewById(R.id.method_spinner);
                mConceptionDateText = mDialog.findViewById(R.id.conception_date_text_view);
                mLastPeriodText = mDialog.findViewById(R.id.last_period_date_text_view);
                mCalculatedDueDateText = mDialog.findViewById(R.id.date_value);
                mCycleLengthText = mDialog.findViewById(R.id.cycle_length_text_view);
                mCycleLengthSpinner = mDialog.findViewById(R.id.cycle_length_spinner);
                mOkButton = mDialog.findViewById(R.id.ok_button);
                mCancelButton = mDialog.findViewById(R.id.cancel_button);

                // Default layout uses conception date
                mCalculationMethod = 0;
                mConceptionDateText.setVisibility(View.VISIBLE);
                mLastPeriodText.setVisibility(View.GONE);
                mCycleLengthText.setVisibility(View.GONE);
                mCycleLengthSpinner.setVisibility(View.GONE);

                // Get today's date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR); // current year
                mMonth = c.get(Calendar.MONTH); // current month
                mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                mChosenDateStr = mDay + "/" + (mMonth + 1) + "/" + mYear;
                mCalculatedDueDateText.setText(formatDisplayDate(mChosenDateStr));

                // Initialise calculation method spinner
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ProfileActivity.this,
                        R.array.calculation_method_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mCalculationMethodSpinner.setAdapter(adapter);

                // Initialise cycle length spinner
                adapter = ArrayAdapter.createFromResource(ProfileActivity.this,
                        R.array.cycle_length_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mCycleLengthSpinner.setAdapter(adapter);
                mCycleLengthSpinner.setSelection(7); // set default value to position 7 (28 day cycle)

                // On click listener for calculation method value
                mCalculationMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            mCalculationMethod = 0;
                            mConceptionDateText.setVisibility(View.VISIBLE);
                            mLastPeriodText.setVisibility(View.GONE);
                            mCycleLengthText.setVisibility(View.GONE);
                            mCycleLengthSpinner.setVisibility(View.GONE);
                        } else if (position == 1) {
                            mCalculationMethod = 1;
                            mConceptionDateText.setVisibility(View.GONE);
                            mLastPeriodText.setVisibility(View.VISIBLE);
                            mCycleLengthText.setVisibility(View.VISIBLE);
                            mCycleLengthSpinner.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        mCalculationMethod = 0;
                    }
                });

                // Get date value
                mCalculatedDueDateText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatePickerDialog = new DatePickerDialog(ProfileActivity.this, R.style.DialogTheme,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                        // set day of month , month and year value in the edit text
                                        mChosenDateStr = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                        mCalculatedDueDateText.setText(formatDisplayDate(mChosenDateStr));
                                    }
                                }, mYear, mMonth, mDay);
                        mDatePickerDialog.show();
                    }
                });

                // On click listener for cycle length value
                mCycleLengthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        mCycleLengthVal = Integer.valueOf((String) parent.getItemAtPosition(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        mCycleLengthVal = 28;
                    }
                });

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
                        // Calculate due date
                        if (mCalculationMethod == 0) {
                            Date date = createDateObject(mChosenDateStr);
                            date = addDays(date, 266); // 28 weeks from conception
                            mDueDateStr = convertDateToString(date);

                        } else if (mCalculationMethod == 1) {
                            Date date = createDateObject(mChosenDateStr);
                            date = addDays(date, (280 - (28 - mCycleLengthVal))); // 280 days from first day of last period
                            mDueDateStr = convertDateToString(date);
                        }
                        mDueDateText.setText(mDueDateStr);

                        // Exit dialog
                        mDialog.dismiss();
                    }
                });

                mDialog.show();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachDatabaseReadListener();
    }


    private void updateDataDisplay(User user) {
        // Calculate BMI
        int height = user.getHeight();
        double weight = user.getPrePregWeight();
        float heightInMeters = (float) height / 100;
        float bmi = ((float) weight) / ((heightInMeters) * (heightInMeters));

        // Set DOB and Due Date text
        String id = user.getId();
        String DOB = id.substring(0, 6);

        long dueDate = user.getDueDate();
        Date dueDateObj = new Date(dueDate);
        String dueDateText = String.valueOf(dueDateObj);

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyMMdd");
            Date date = inputFormat.parse(DOB);

            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, d MMM yyyy");
            DOB = outputFormat.format(date);

            dueDateText = outputFormat.format(dueDateObj);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error with pull parse", e);
        }

        // Assign values to views
        mNameText.setText(mUsername);
        mIDText.setText(id);
        mEmailText.setText(user.getEmail());
        mPhoneText.setText(user.getPhone());
        mAddressText.setText(user.getAddress());
        mDOBText.setText(DOB);
        mHeightText.setText(String.valueOf(height) + " cm");
        mWeightText.setText(String.valueOf(weight) + " Kg");
        mBMIText.setText(String.valueOf(bmi) + " Kg/m2");
        mDiabetesTypeText.setText(user.getDiabetesType());
        mDueDateText.setText(dueDateText);
        mHPText.setText(user.getHpSurname());

        if (user.getMedication() == null)
            mMedicationText.setText("Your HP has not set medication");
        else
            mMedicationText.setText(user.getMedication());
    }

    private void updateRangesDisplay(HpSpecifiedRanges ranges) {
        String noDataMessage = "Your HP has not set goals";

        if (ranges.getGlucMin() == null | ranges.getGlucMax() == null)
            mGlucoseRangeText.setText(noDataMessage);
        else
            mGlucoseRangeText.setText(ranges.getGlucMin() + " - " + ranges.getGlucMax() + " mmol/L");

        if (ranges.getWeightMin() == null | ranges.getWeightMax() == null)
            mWeightRangeText.setText(noDataMessage);
        else
            mWeightRangeText.setText(ranges.getWeightMin() + " - " + ranges.getWeightMax() + " Kg");

        if (ranges.getActMin() == null | ranges.getActMax() == null)
            mActivityGoalText.setText(noDataMessage);
        else
            mActivityGoalText.setText(ranges.getActMin() + " - " + ranges.getActMax() + " min / week");
    }

    private void attachDatabaseReadListener() {
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    updateDataDisplay(user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
        mUsersDatabaseReference.addListenerForSingleValueEvent(mValueEventListener);

        if (mValueEventListenerForRanges == null) {
            mValueEventListenerForRanges = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HpSpecifiedRanges ranges = dataSnapshot.getValue(HpSpecifiedRanges.class);
                    updateRangesDisplay(ranges);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
        mRangesDatabaseReference.addListenerForSingleValueEvent(mValueEventListenerForRanges);
    }

    private void detachDatabaseReadListener() {
        if (mValueEventListener != null) {
            mUsersDatabaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }

        if (mValueEventListenerForRanges != null) {
            mRangesDatabaseReference.removeEventListener(mValueEventListenerForRanges);
            mValueEventListenerForRanges = null;
        }
    }

    private String formatDisplayDate(String srcDate) {
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

    private Date createDateObject(String dateStr) {
        Date dateObj = null;
        SimpleDateFormat format = new SimpleDateFormat("d/M/yyyy");

        try {
            dateObj = format.parse(dateStr);
        } catch (ParseException e) {

            Log.e(LOG_TAG, "Error with pull parse", e);
        }
        return dateObj;
    }

    private Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    private String convertDateToString(Date date) {
        SimpleDateFormat destFormat = new SimpleDateFormat("EEE, d MMM yyyy");
        String dateStr = destFormat.format(date);
        return dateStr;
    }

}
