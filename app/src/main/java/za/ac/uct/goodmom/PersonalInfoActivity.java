package za.ac.uct.goodmom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PersonalInfoActivity extends AppCompatActivity {

    private Button mSaveButton;
    private EditText mIdText;
    private EditText mEmailText;
    private EditText mHouseText;
    private EditText mStreetText;
    private EditText mCityText;
    private EditText mProvText;
    private EditText mPostText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        // Initialize references to views
        mSaveButton = findViewById(R.id.save_button);
        mIdText = findViewById(R.id.id_number);
        mEmailText = findViewById(R.id.email);
        mHouseText = findViewById(R.id.house_number);
        mStreetText = findViewById(R.id.street_name);
        mCityText = findViewById(R.id.city_name);
        mProvText = findViewById(R.id.province_name);
        mPostText = findViewById(R.id.postal_code);

        // Set a click listener on that button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Log In button is clicked on.
            @Override
            public void onClick(View view) {

                // Create User object
                User newUser = new User();
                newUser.setId(mIdText.getText().toString());
                newUser.setEmail(mEmailText.getText().toString());
                newUser.setAddress(Integer.parseInt(mHouseText.getText().toString()), mStreetText.getText().toString(), mCityText.getText().toString(), mProvText.getText().toString(), Integer.parseInt(mPostText.getText().toString()));

                // Write the user data to Firebase
                //mUsersDatabaseReference.setValue(newUser);

                // Create a new intent to open the activity
                Intent userInfoIntent = new Intent(PersonalInfoActivity.this, ClinicalInfoActivity.class);
                userInfoIntent.putExtra("userObject", newUser);
                startActivity(userInfoIntent);

            }
        });
    }
}
