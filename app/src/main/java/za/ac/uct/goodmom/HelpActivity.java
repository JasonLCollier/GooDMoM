package za.ac.uct.goodmom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    private TextView mEmailUsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        mEmailUsText = findViewById(R.id.email_us);

        // Email us click listener
        mEmailUsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "clljas@myuct.ac.za", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "GooDMoM - Help & Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear Admin,");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
    }
}
