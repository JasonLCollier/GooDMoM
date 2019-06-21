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
    private DatabaseReference mUsersDatabaseReference;
    private ValueEventListener mValueEventListener;
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

        // Initialise variables
        mNameText.setText(mUsername);
        mIDText.setText(R.string.no_data);
        mEmailText.setText(R.string.no_data);
        mPhoneText.setText(R.string.no_data);
        mAddressText.setText(R.string.no_data);
        mDOBText.setText(R.string.no_data);
        mHeightText.setText(R.string.no_data);
        mWeightText.setText(R.string.no_data);
        mBMIText.setText(R.string.no_data);
        mDiabetesTypeText.setText(R.string.no_data);
        mDueDateText.setText(R.string.no_data);
        mHPText.setText(R.string.no_data);
        mGlucoseRangeText.setText(R.string.no_data);
        mWeightRangeText.setText(R.string.no_data);
        mActivityGoalText.setText(R.string.no_data);
        mMedicationText.setText(R.string.no_data);
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


    private void updateData(User user) {
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
        //mGlucoseRangeText.setText("");
        //mWeightRangeText.setText("");
        //mActivityGoalText.setText("");
        //mMedicationText.setText("");

    }

    private void attachDatabaseReadListener() {
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    updateData(user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
        mUsersDatabaseReference.addListenerForSingleValueEvent(mValueEventListener);
    }

    private void detachDatabaseReadListener() {
        if (mValueEventListener != null) {
            mUsersDatabaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

}
