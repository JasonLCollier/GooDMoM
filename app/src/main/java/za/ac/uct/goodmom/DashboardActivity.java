package za.ac.uct.goodmom;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    // Current activity
                    return true;
                case R.id.navigation_reminders:
                    Intent remindersIntent = new Intent(DashboardActivity.this, RemindersActivity.class);
                    startActivity(remindersIntent);
                    return true;
                case R.id.navigation_messenger:
                    Intent messengerIntent = new Intent(DashboardActivity.this, MessengerActivity.class);
                    startActivity(messengerIntent);
                    return true;
                case R.id.navigation_tips:
                    Intent tipsIntent = new Intent(DashboardActivity.this, TipsActivity.class);
                    startActivity(tipsIntent);
                    return true;
            }
            return false;
        }
    };

    private FloatingActionButton mFab;
    private LineChart mChart;
    private LineDataSet mDataSet;
    private LineData mLineData;

    private List<Entry> mEntries = new ArrayList<>();
    private ArrayList<GdData> mGdDataList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private EventAdapter mDataAdapter;

    private String mUsername, mUserId;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mGdDataDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mValueEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialise firebase components
        mFirebasedatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            mUsername = user.getDisplayName();
            mUserId = user.getUid();
        }
        else {
            mUsername =  "Unauthorized";
            mUserId = "Unauthorized";
        }
        mGdDataDatabaseReference = mFirebasedatabase.getReference().child("gdData").child(mUserId);

        // Assign variables to views
        mFab = findViewById(R.id.fab);
        mChart = findViewById(R.id.chart);

        //Initialise chart
        initialiseChart();

        // floating action button click listener
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newEventIntent = new Intent(DashboardActivity.this, AddDataActivity.class);
                startActivity(newEventIntent);
            }
        });

        // Authorization state listener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // za.ac.uct.goodmom.User is signed in

                } else {
                    // za.ac.uct.goodmom.User is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.PhoneBuilder().build()))
                                    .setTheme(R.style.LoginTheme)
                                    //.setLogo(R.drawable.pregnant)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        // Attach the database read listener and check when all data has been synchronised
        attachDatabaseReadListener();

        // Set up bottom navigation view
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Set up menu
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

    }

    private void initialiseChart() {
        // add entries and styling to dataset
        mDataSet = new LineDataSet(mEntries, "Glucose Data");
        mDataSet.setValueTextColor(R.color.primary_text);
        mDataSet.setValueTextSize(16);
        mDataSet.setColor(R.color.primary);
        mDataSet.setLineWidth(4);
        mDataSet.setCircleColor(R.color.primary);
        mDataSet.setCircleRadius(8);
        mDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        mDataSet.setCubicIntensity(0.1f);
        mDataSet.setDrawFilled(true);
        mDataSet.setFillColor(R.color.primary_light);

        // add dataset to linedata object to be displayed on chart
        mLineData = new LineData(mDataSet);
        mChart.setData(mLineData);
        mChart.invalidate(); // refresh

        // no data text
        mChart.setNoDataText("No data available - add a new entry");
        mChart.setNoDataTextColor(R.color.primary);

        // description formatting
        Description description = new Description();
        description.setText("Glucose");
        description.setTextSize(16);
        description.setTextColor(R.color.primary);
        description.setPosition(110, 45);
        mChart.setDescription(description);

        // axis formatting
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);
        YAxis left = mChart.getAxisLeft();
        left.setDrawGridLines(false);
        left.setDrawLabels(false);
        left.setDrawAxisLine(false);
        YAxis right = mChart.getAxisRight();
        right.setDrawGridLines(false);
        right.setDrawLabels(false);
        right.setDrawAxisLine(false);

        // legend formatting
        Legend legend = mChart.getLegend();
        legend.setEnabled(false);

        // modify the viewport

    }

    public void addDummyData() {

        mEntries.add(new Entry(0, 1));
        mEntries.add(new Entry(1, 1));
        mEntries.add(new Entry(2, 4));
        mEntries.add(new Entry(3, 9));
        mEntries.add(new Entry(4, 10));
        mEntries.add(new Entry(5, 9));
        mEntries.add(new Entry(6, 4));
        mEntries.add(new Entry(7, 5));
        mEntries.add(new Entry(8, 6));
        mEntries.add(new Entry(9, 8));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signout_menu:
                AuthUI.getInstance().signOut(this);
                Intent logoutIntent = new Intent(DashboardActivity.this, LandingActivity.class);
                startActivity(logoutIntent);
                return true;
            case R.id.settings_menu:
                Intent settingsIntent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded - check whether the user is a first time or returning user
                FirebaseUserMetadata metadata = mFirebaseAuth.getCurrentUser().getMetadata();
                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    // The user is new, take them to the sign up activity for additional info
                    Intent signUpIntent = new Intent(DashboardActivity.this, PersonalInfoActivity.class);
                    startActivity(signUpIntent);
                } else {
                    // This is an existing user, show them the dashboard.
                    Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
                }

            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();

                Intent landingIntent = new Intent(DashboardActivity.this, LandingActivity.class);
                startActivity(landingIntent);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        mGdDataList.clear();
        detachDatabaseReadListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    GdData data = dataSnapshot.getValue(GdData.class);
                    mGdDataList.add(data);
                    Collections.sort(mGdDataList);
                    //mDataAdapter.notifyDataSetChanged();

                    mEntries.add(new Entry((float) data.getDateTime(), (float) data.getGlucose()));
                    //mChart.notifyDataSetChanged();
                    //mChart.invalidate();

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
            mGdDataDatabaseReference.addChildEventListener(mChildEventListener);

            if (mValueEventListener == null) {
                mValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Value events are always triggered last
                        // and are guaranteed to contain updates from any other events
                        // which occurred before that snapshot was taken
                        initialiseChart();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
            }
            mGdDataDatabaseReference.addListenerForSingleValueEvent(mValueEventListener);

        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mGdDataDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mValueEventListener != null) {
            mGdDataDatabaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

}
