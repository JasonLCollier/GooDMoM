package za.ac.uct.goodmom;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    public static final String LOG_TAG = ProfileActivity.class.getSimpleName();

    private TextView mNameText, mIDText, mEmailText, mPhoneText, mAddressText, mDOBText,
            mHeightText, mWeightText, mBMIText, mDiabetesTypeText, mDueDateText, mHPText,
            mGlucoseRangeText, mWeightRangeText, mActivityGoalText, mMedicationText;

    private String mUsername, mUserId;

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

}
