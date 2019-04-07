package za.ac.uct.goodmom;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
    private EditText mHpSurnameText;
    private EditText mHpNumberText;
    private TextView mDueDateText;
    private DatePickerDialog mDatePickerDialog;

    private int mYear, mMonth, mDay;
    private String mDueDateString;

    private String mUsername, mUserId;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mUsersDatabaseReference;
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

        // Initialise Firebase components
        mFirebasedatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        mUsername = firebaseUser.getDisplayName();
        mUserId = firebaseUser.getUid();
        mUsersDatabaseReference = mFirebasedatabase.getReference().child("users").child(mUserId);

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

        // Set a click listener on that button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Log In button is clicked on.
            @Override
            public void onClick(View view) {

                // Update user object
                Intent userInfo = getIntent();
                User newUser = (User) userInfo.getSerializableExtra("userObject");
                newUser.setName(mUsername);
                newUser.setPhone("0000000000");
                newUser.setHpSurname(mHpSurnameText.getText().toString());
                newUser.setHpNumber(mHpNumberText.getText().toString());
                newUser.setDueDate(convertDateToMillis(mDueDateString));


                // Write the user data to Firebase
                mUsersDatabaseReference.setValue(newUser);

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
