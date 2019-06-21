package za.ac.uct.goodmom;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    private TextView mNameText, mIDText, mEmailText, mPhoneText, mAddressText, mDOBText,
            mHeightText, mWeightText, mBMIText, mDiabetesTypeText, mDueDateText, mHPText,
            mGlucoseRangeText, mWeightRangeText, mActivityGoalText, mMedicationText;

    private String mUsername, mUserId;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mUsersDatabaseReference;
    private ChildEventListener mChildEventListener;
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
        mIDText.setText("No data");
        mEmailText.setText("No data");
        mPhoneText.setText("No data");
        mAddressText.setText("No data");
        mDOBText.setText("No data");
        mHeightText.setText("No data");
        mWeightText.setText("No data");
        mBMIText.setText("No data");
        mDiabetesTypeText.setText("No data");
        mDueDateText.setText("No data");
        mHPText.setText("No data");
        mGlucoseRangeText.setText("No data");
        mWeightRangeText.setText("No data");
        mActivityGoalText.setText("No data");
        mMedicationText.setText("No data");
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
        mIDText.setText(user.getId());
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String key = dataSnapshot.getKey();
                    if (key == "id")
                        mIDText.setText(dataSnapshot.getValue(String.class));
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mUsersDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mUsersDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

}
