package za.ac.uct.goodmom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button logIn = findViewById(R.id.login_button);
        Button signUp = findViewById(R.id.signup_button);

        // Set a click listener on that button
        logIn.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Log In button is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link LoginActivity}
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);

                // Start the new activity
                startActivity(loginIntent);

            }
        });

        // Set a click listener on that button
        signUp.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Log In button is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link DashboardActivity}
                Intent signUpIntent = new Intent(MainActivity.this, DashboardActivity.class);

                // Start the new activity
                startActivity(signUpIntent);

            }
        });

    }
}
