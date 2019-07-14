package za.ac.uct.goodmom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class PersonalInfoActivity extends AppCompatActivity {

    String mProvince;
    private Button mSaveButton;
    private EditText mIdText, mPhoneText, mHouseText, mStreetText, mCityText, mPostText;
    private Spinner mProvSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        // Initialize references to views
        mSaveButton = findViewById(R.id.save_button);
        mIdText = findViewById(R.id.id_number);
        mPhoneText = findViewById(R.id.phone);
        mHouseText = findViewById(R.id.house_number);
        mStreetText = findViewById(R.id.street_name);
        mCityText = findViewById(R.id.city_name);
        mProvSpinner = findViewById(R.id.province_name_spinner);
        mPostText = findViewById(R.id.postal_code);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.province_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mProvSpinner.setAdapter(adapter);

        mProvSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mProvince = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set a click listener on that button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Log In button is clicked on.
            @Override
            public void onClick(View view) {

                // Create User object
                User newUser = new User();
                newUser.setId(mIdText.getText().toString());
                newUser.setPhone(mPhoneText.getText().toString());
                newUser.setAddress(Integer.parseInt(mHouseText.getText().toString()), mStreetText.getText().toString(), mCityText.getText().toString(), mProvince, Integer.parseInt(mPostText.getText().toString()));

                // Write the user data to Firebase
                //mUsersDatabaseReference.setValue(newUser);

                // Create a new intent to open the activity
                Intent userInfoIntent = new Intent(PersonalInfoActivity.this, ClinicalInfoActivity.class);
                userInfoIntent.putExtra("userObject", newUser);
                startActivity(userInfoIntent);

            }
        });
    }

    @Override
    public void onBackPressed() {
        // Do nothing
        Toast toast = Toast.makeText(getApplicationContext(), "You cannot go back - please finish signing up", Toast.LENGTH_SHORT);
        toast.show();
    }
}
