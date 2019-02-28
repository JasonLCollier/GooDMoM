package za.ac.uct.goodmom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Assign buttons to variables
        Button logIn = findViewById(R.id.login_button);
        Button exit = findViewById(R.id.exit_button);

        // Set a click listener on that button
        logIn.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Log In button is clicked on.
            @Override
            public void onClick(View view) {

                // Create a new intent to open the activity
                Intent loginIntent = new Intent(LandingActivity.this, DashboardActivity.class);
                startActivity(loginIntent);
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Log In button is clicked on.
            @Override
            public void onClick(View view) {
                finish();
                moveTaskToBack(true);
            }
        });
    }
}



