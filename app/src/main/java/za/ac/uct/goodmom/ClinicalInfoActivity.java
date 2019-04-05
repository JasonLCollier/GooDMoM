package za.ac.uct.goodmom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ClinicalInfoActivity extends AppCompatActivity {

    private String mUsername;
    private String mUserId;

    private Button mSaveButton;
    private EditText mHpSurnameText;
    private EditText mHpNumberText;
    private EditText mDueDateText;

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
        mDueDateText = findViewById(R.id.due_date_display_text_view);

        // Initialise Firebase components
        mFirebasedatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        mUsername = firebaseUser.getDisplayName();
        mUserId = firebaseUser.getUid();
        mUsersDatabaseReference = mFirebasedatabase.getReference().child("users").child(mUserId);

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
                newUser.setDueDate(mDueDateText.getText().toString());


                // Write the user data to Firebase
                mUsersDatabaseReference.setValue(newUser);

                // Create a new intent to open the activity
                Intent dashboardIntent = new Intent(ClinicalInfoActivity.this, DashboardActivity.class);
                startActivity(dashboardIntent);

            }
        });
    }
}
