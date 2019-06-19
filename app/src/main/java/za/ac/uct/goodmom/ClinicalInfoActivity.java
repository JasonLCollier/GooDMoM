package za.ac.uct.goodmom;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ClinicalInfoActivity extends AppCompatActivity {

    // Tag for the log messages
    public static final String LOG_TAG = ClinicalInfoActivity.class.getSimpleName();

    private Button mSaveButton;
    private EditText mHpSurnameText, mHpNumberText, mDoubleNumberText;
    private TextView mDueDateText, mHeightText, mPrepregWeightText, mCancelButton, mOkButton;
    private DatePickerDialog mDatePickerDialog;
    private Spinner mHpSpinner, mDiabetesSpinner;
    private LinearLayout mHeightContainer, mWeightContainer;

    private Dialog mDialog;
    private NumberPicker mNumberPicker;

    private int mYear, mMonth, mDay, mHeight;
    private double mPrepregWeight;
    private String mDueDateString, mHpTypeString, mDiabetesTypeString, mHeightString, mPrepregWeightString;

    private String mUsername, mUserId, mEmail;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mUsersDatabaseReference, mLinkedPatientsDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinical_info);

        // Initialize references to views
        mSaveButton = findViewById(R.id.save_button);
        mHpSurnameText = findViewById(R.id.hp_surname);
        mHpNumberText = findViewById(R.id.hp_number);
        mDueDateText = findViewById(R.id.due_date_display_text);
        mHpSpinner = findViewById(R.id.hp_type_spinner);
        mDiabetesSpinner = findViewById(R.id.diabetes_type_spinner);
        mHeightContainer = findViewById(R.id.height_container);
        mHeightText = findViewById(R.id.height_value);
        mWeightContainer = findViewById(R.id.weight_container);
        mPrepregWeightText = findViewById(R.id.prepreg_weight_value);

        // Initialise Height and weight
        mHeightText.setText("0");
        mPrepregWeightText.setText("00.0");

        // Initialise Firebase components
        mFirebasedatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        mUsername = firebaseUser.getDisplayName();
        mUserId = firebaseUser.getUid();
        mEmail = firebaseUser.getEmail();
        mUsersDatabaseReference = mFirebasedatabase.getReference().child("patients").child(mUserId).child("userData");

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.hp_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mHpSpinner.setAdapter(adapter);

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this,
                R.array.diabetes_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mDiabetesSpinner.setAdapter(adapter);

        mHpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mHpTypeString = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mDiabetesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDiabetesTypeString = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR); // current year
        mMonth = c.get(Calendar.MONTH); // current month
        mDay = c.get(Calendar.DAY_OF_MONTH); // current day

        // Update date and time views
        mDueDateString = mDay + "/" + (mMonth + 1) + "/" + mYear;
        mDueDateText.setText(formatDisplayDate(mDueDateString));

        // Set on click listener on due date text
        mDueDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // date picker dialog
                mDatePickerDialog = new DatePickerDialog(ClinicalInfoActivity.this, R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                mDueDateString = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                mDueDateText.setText(formatDisplayDate(mDueDateString));
                            }
                        }, mYear, mMonth, mDay);
                mDatePickerDialog.show();
            }
        });

        //On click listener for carbs value
        mHeightContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set up dialog
                mDialog = new Dialog(ClinicalInfoActivity.this);
                mDialog.setContentView(R.layout.dialog_number_picker);

                // Set up number picker
                mNumberPicker = mDialog.findViewById(R.id.number_picker);
                mNumberPicker.setMinValue(70);
                mNumberPicker.setMaxValue(250);

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
                        mHeight = mNumberPicker.getValue();
                        mHeightString = String.valueOf(mHeight);
                        mHeightText.setText(mHeightString);

                        // Exit dialog
                        mDialog.dismiss();
                    }
                });

                mDialog.show();
            }
        });

        // On click listener for weight value
        mWeightContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set up dialog
                mDialog = new Dialog(ClinicalInfoActivity.this);
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

                        mPrepregWeightString = mDoubleNumberText.getText().toString();
                        mPrepregWeight = Double.valueOf(mPrepregWeightString);
                        mPrepregWeightText.setText(mPrepregWeightString);

                        // Exit dialog
                        mDialog.dismiss();
                    }
                });

                mDialog.show();
            }
        });

        // Set a click listener on that button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Log In button is clicked on.
            @Override
            public void onClick(View view) {

                // Assign all user to given HP ID, except if HP ID is one
                String hpNumber = mHpNumberText.getText().toString();
                if (mHpNumberText.getText().toString() == "1")
                    hpNumber = "nvYiR62maGNho4avqSAQAvoE8wI2"; // Barnard's number


                // Update user object
                Intent userInfo = getIntent();
                User newUser = (User) userInfo.getSerializableExtra("userObject");
                newUser.setName(mUsername);
                newUser.setEmail(mEmail);
                newUser.setHpSurname(mHpSurnameText.getText().toString());
                newUser.setHpNumber(hpNumber);
                newUser.setHpType(mHpTypeString);
                newUser.setDiabetesType(mDiabetesTypeString);
                newUser.setHeight(mHeight);
                newUser.setPrepregWeight(mPrepregWeight);
                newUser.setDueDate(convertDateToMillis(mDueDateString));

                // Write the user data to Firebase
                mUsersDatabaseReference.setValue(newUser);

                // Create LinkedPatient object
                LinkedPatient newLinkedPatient = new LinkedPatient();
                newLinkedPatient.setPatientId(mUserId);

                // Write current users's ID to Linked HP's linked patients list
                mLinkedPatientsDatabaseReference = mFirebasedatabase.getReference().child("doctors").child(hpNumber).child("linkedPatients");
                mLinkedPatientsDatabaseReference.push().setValue(newLinkedPatient);

                // Create a new intent to open the activity
                Intent dashboardIntent = new Intent(ClinicalInfoActivity.this, DashboardActivity.class);
                startActivity(dashboardIntent);

            }
        });
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

    public long convertDateToMillis(String dateStr) {
        Date dateTime = null;
        SimpleDateFormat mSdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            dateTime = mSdf.parse(dateStr);
        } catch (ParseException e) {

            Log.e(LOG_TAG, "Error with pull parse", e);
        }
        return dateTime.getTime();
    }

}
