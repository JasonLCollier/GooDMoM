package za.ac.uct.goodmom;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    public static final String LOG_TAG = DashboardActivity.class.getSimpleName();

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
    private Spinner mChartTypeSpinner, mPeriodSpinner, mMonthSpinner, mDaySpinner;
    private ProgressBar mProgressBar;
    private TextView mDueDateText, mGlucoseText, mCarbText, mActivityTimeText, mWeightText, mBPText;
    private Dialog mDialog;

    private LineDataSet mDataSet;
    private LineData mLineData;

    private List<Entry> mEntries = new ArrayList<>();
    private ArrayList<GdData> mGdDataList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private EventAdapter mDataAdapter;

    private User mUser;
    private HpSpecifiedRanges mRanges;

    private String mUsername, mUserId;
    private int mYear, mMonth, mDay, mMonthOfYear, mDayOfMonth, mPeriod, mChartType;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mGdDataDatabaseReference, mUsersDatabaseReference, mRangesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mValueEventListener, mValueEventListenerForDD, mValueEventListenerForRanges;
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
        } else {
            mUsername = "Unauthorized";
            mUserId = "Unauthorized";
        }
        mGdDataDatabaseReference = mFirebasedatabase.getReference().child("patients").child(mUserId).child("gdData");
        mUsersDatabaseReference = mFirebasedatabase.getReference().child("patients").child(mUserId).child("userData");
        mRangesDatabaseReference = mFirebasedatabase.getReference().child("patients").child(mUserId).child("userData").child("ranges");

        // Assign variables to views
        mFab = findViewById(R.id.fab);
        mChart = findViewById(R.id.chart);
        mChartTypeSpinner = findViewById(R.id.chart_type);
        mPeriodSpinner = findViewById(R.id.period);
        mMonthSpinner = findViewById(R.id.month);
        mProgressBar = findViewById((R.id.progress_bar));
        mDueDateText = findViewById(R.id.due_date_display_text);
        mGlucoseText = findViewById(R.id.glucose_display_text);
        mCarbText = findViewById(R.id.carb_display_text);
        mActivityTimeText = findViewById(R.id.activity_time_display_text);
        mWeightText = findViewById(R.id.weight_display_text);
        mBPText = findViewById(R.id.bp_display_text);
        mDaySpinner = findViewById(R.id.day_in_month);

        // Initialise chart, progress bar, display text
        initialiseProgressBar();
        initialiseChart();
        initialiseDisplayText();

        // Calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR); // current year
        mMonth = c.get(Calendar.MONTH); // current month
        mDay = c.get(Calendar.DAY_OF_MONTH); // current day

        // Initialise spinner default values
        mChartType = 0; // 0 = glucose; 1 = carbs; 2 = activity; 3 = weight; 4 = BP
        mPeriod = 1; // 0 = day; 1 = month; 2 = gestation
        mDayOfMonth = mDay;
        mMonthOfYear = mMonth;

        // Hide day spinner for default layout
        mDaySpinner.setVisibility(View.GONE);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.chart_type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mChartTypeSpinner.setAdapter(adapter);

        mChartTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mChartType = position;
                updateChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mChartType = 0;
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this,
                R.array.period_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mPeriodSpinner.setAdapter(adapter);
        // Default selection on month
        mPeriodSpinner.setSelection(1);

        mPeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPeriod = position;

                if (mPeriod == 0) {
                    mDaySpinner.setVisibility(View.VISIBLE);
                    mMonthSpinner.setVisibility(View.VISIBLE);
                } else if (mPeriod == 1) {
                    mDaySpinner.setVisibility(View.GONE);
                    mMonthSpinner.setVisibility(View.VISIBLE);
                } else if (mPeriod == 2) {
                    mMonthSpinner.setVisibility(View.INVISIBLE);
                    mDaySpinner.setVisibility(View.GONE);
                }

                updateChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPeriod = 1;
                mDaySpinner.setVisibility(View.GONE);
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this,
                R.array.day_in_month_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mDaySpinner.setAdapter(adapter);
        // Default selection on current month
        mDaySpinner.setSelection(mDayOfMonth - 1);

        mDaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDayOfMonth = position + 1;
                updateChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDayOfMonth = mMonth;
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this,
                R.array.month_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mMonthSpinner.setAdapter(adapter);
        // Default selection on current month
        mMonthSpinner.setSelection(mMonth);

        mMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMonthOfYear = position;
                updateChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mMonthOfYear = mMonth;
            }
        });

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
                                    .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .setTheme(R.style.LoginTheme)
                                    .setLogo(R.drawable.logo)
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

    private void updateChart() {
        mEntries.clear();

        // chart type
        float yVal = 0;

        // Initialisation of variables to set up full gestation graph
        long dueDate = 0;
        long conceptionDate = 0;
        long curDaysSinceConceptionInMillis = 0;
        long nextDaysSinceConceptionInMillis = 0;
        int curDaysSinceConception = 0;
        int nextDaysSinceConception = 0;
        int count = 0;
        int zeroCount = 0;
        float avgYVal = 0;
        // get due date in millis
        if (mUser != null)
            dueDate = mUser.getDueDate();
        // calculate conception date in millis
        conceptionDate = dueDate - 24192000000L; // due date - 9 months (in millis)

        for (int i = 0; i < mGdDataList.size(); i++) {
            switch (mChartType) {
                case 0:
                    yVal = (float) mGdDataList.get(i).getGlucose();
                    break;
                case 1:
                    yVal = (float) mGdDataList.get(i).getCarbs();
                    break;
                case 2:
                    yVal = (float) mGdDataList.get(i).getActivityTime();
                    break;
                case 3:
                    yVal = (float) mGdDataList.get(i).getWeight();
                    break;
                case 4:
                    String[] parts = mGdDataList.get(i).getBloodPressure().split("/");
                    yVal = Float.valueOf(parts[0]);
                    break;
            }

            // period = day (and check for no zero values)
            if (mPeriod == 0 && mGdDataList.get(i).month() - 1 == mMonthOfYear && mGdDataList.get(i).day() == mDayOfMonth && yVal != 0) {
                mEntries.add(new Entry((float) mGdDataList.get(i).hoursOfDay(), yVal));
            }
            // period = month (and check for no zero values)
            else if (mPeriod == 1 && mGdDataList.get(i).month() - 1 == mMonthOfYear && yVal != 0) {
                mEntries.add(new Entry((float) mGdDataList.get(i).hoursOfMonth(), yVal));
            }
            // full gestation
            else if (mPeriod == 2) {
                // check all entries except last
                if (i < mGdDataList.size() - 1) {
                    // calculate days since conception
                    nextDaysSinceConceptionInMillis = mGdDataList.get(i + 1).getDateTime() - conceptionDate;
                    curDaysSinceConceptionInMillis = mGdDataList.get(i).getDateTime() - conceptionDate;
                    nextDaysSinceConception = (int) (nextDaysSinceConceptionInMillis / (1000 * 60 * 60 * 24));
                    curDaysSinceConception = (int) (curDaysSinceConceptionInMillis / (1000 * 60 * 60 * 24));

                    // get average of all glucose levels on 1 day and add that day and average glucose as an entry
                    avgYVal += yVal;
                    if (yVal == 0)
                        zeroCount++;
                    count++;
                    if (curDaysSinceConception != nextDaysSinceConception) {
                        avgYVal = avgYVal / (count - zeroCount);

                        if (count - zeroCount != 0 && avgYVal != 0)
                            mEntries.add(new Entry((float) curDaysSinceConception, avgYVal));

                        count = 0;
                        zeroCount = 0;
                        avgYVal = 0;
                    }
                }
                // check last entry
                if (i == mGdDataList.size() - 1) {
                    avgYVal += yVal;
                    if (yVal == 0)
                        zeroCount++;
                    count++;

                    avgYVal = avgYVal / (count - zeroCount);

                    if (count - zeroCount != 0 && avgYVal != 0)
                        mEntries.add(new Entry((float) curDaysSinceConception, avgYVal));

                    count = 0;
                    zeroCount = 0;
                    avgYVal = 0;
                }
            }
        }

        initialiseChart();
        initialiseDisplayText();
    }

    private void initialiseChart() {
        // Prepare colors
        int primaryColor = getResources().getColor(R.color.primary);
        int primaryLightColor = getResources().getColor(R.color.primary_light);
        int primaryTextColor = getResources().getColor(R.color.primary_text);
        int greenColor = getResources().getColor(android.R.color.holo_green_dark);
        int redColor = getResources().getColor(android.R.color.holo_red_dark);
        int blueColor = getResources().getColor(android.R.color.holo_blue_dark);

        // find max and min values for y axis
        double maxY = 11;
        double minY = 4;
        double yVal = 0;
        if (mGdDataList.size() != 0) {
            maxY = Double.MIN_VALUE;
            minY = Double.MAX_VALUE;
            for (int i = 0; i < mGdDataList.size(); i++) {
                switch (mChartType) {
                    case 0:
                        yVal = mGdDataList.get(i).getGlucose();
                        break;
                    case 1:
                        yVal = (double) mGdDataList.get(i).getCarbs();
                        break;
                    case 2:
                        yVal = mGdDataList.get(i).getActivityTime();
                        break;
                    case 3:
                        yVal = mGdDataList.get(i).getWeight();
                        break;
                    case 4:
                        String[] parts = mGdDataList.get(i).getBloodPressure().split("/");
                        yVal = Float.valueOf(parts[0]);
                        break;
                }
                if (yVal > maxY)
                    maxY = yVal;
                if (yVal < minY && yVal >= 1)
                    minY = yVal;
            }
        }

        //Determine entry colours based on goals
        int[] circleColours = {primaryColor}; // manual color entry
        double goalMax = 0; // initialise upper glucose goal
        double goalMin = 0; // initialise lower glucose goal
        boolean rangesDefined = false;
        switch (mChartType) {
            case 0:
                if (mRanges != null && mRanges.getGlucMax() != null && mRanges.getGlucMin() != null) {
                    goalMax = Double.valueOf(mRanges.getGlucMax());
                    goalMin = Double.valueOf(mRanges.getGlucMin());
                    rangesDefined = true;
                }
                break;
            case 1:
                if (mRanges != null && mRanges.getCarbsMax() != null && mRanges.getCarbsMin() != null) {
                    goalMax = Double.valueOf(mRanges.getCarbsMax());
                    goalMin = Double.valueOf(mRanges.getCarbsMin());
                    rangesDefined = true;
                }
                break;
            case 2:
                if (mRanges != null && mRanges.getActMax() != null && mRanges.getActMin() != null) {
                    goalMax = Double.valueOf(mRanges.getActMax());
                    goalMin = Double.valueOf(mRanges.getActMin());
                    rangesDefined = true;
                }
                break;
            case 3:
                if (mRanges != null && mRanges.getWeightMax() != null && mRanges.getWeightMin() != null) {
                    goalMax = Double.valueOf(mRanges.getWeightMax());
                    goalMin = Double.valueOf(mRanges.getWeightMin());
                    rangesDefined = true;
                }
                break;
            case 4:
                if (mRanges != null && mRanges.getSystolicMax() != null && mRanges.getSystolicMin() != null) {
                    goalMax = Double.valueOf(mRanges.getSystolicMax());
                    goalMin = Double.valueOf(mRanges.getSystolicMin());
                    rangesDefined = true;
                }
                break;
        }
        if (rangesDefined && mEntries.size() != 0) {
            circleColours = new int[mEntries.size()];
            for (int i = 0; i < mEntries.size(); i++) {
                if (mEntries.get(i).getY() > goalMax)
                    circleColours[i] = redColor;
                else if (mEntries.get(i).getY() < goalMin)
                    circleColours[i] = blueColor;
                else
                    circleColours[i] = greenColor;
            }
        }

        // add entries and styling to dataset
        mDataSet = new LineDataSet(mEntries, "Data");
        mDataSet.setColor(primaryLightColor);
        mDataSet.setLineWidth(2);
        mDataSet.setCircleColors(circleColours);
        mDataSet.setCircleRadius(4);
        mDataSet.setDrawCircleHole(false);
        mDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        mDataSet.setCubicIntensity(0.1f);
        mDataSet.setDrawFilled(true);
        mDataSet.setFillColor(primaryLightColor);
        mDataSet.setDrawValues(false);

        mLineData = new LineData(mDataSet);
        mLineData.setValueFormatter(new CustomValueFormatter());
        mChart.setData(mLineData);
        mChart.invalidate(); // refresh

        // no data text
        mChart.setNoDataText("No data available - add a new entry");
        mChart.setNoDataTextColor(primaryTextColor);

        // description formatting
        Description description = new Description();
        description.setText(""); // Remove description
        mChart.setDescription(description);

        // axis formatting
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);
        xAxis.setAxisMinimum(0);
        if (mPeriod == 0) {
            xAxis.setValueFormatter(new DefaultAxisValueFormatter(1));
            xAxis.setAxisMaximum(24);
            xAxis.setGranularityEnabled(true);
            xAxis.setGranularity(2);
        } else if (mPeriod == 1) {
            xAxis.setValueFormatter(new CustomXAxisValueFormatter());
            xAxis.setAxisMaximum(31 * 24);
            xAxis.setGranularityEnabled(true);
            xAxis.setGranularity(5 * 24);
        } else if (mPeriod == 2) {
            xAxis.setValueFormatter(new DefaultAxisValueFormatter(1));
            xAxis.setAxisMaximum(280);
            xAxis.setGranularityEnabled(true);
            xAxis.setGranularity(14);
        }

        YAxis left = mChart.getAxisLeft();
        left.setDrawGridLines(false);
        left.setDrawAxisLine(true);
        left.setDrawLabels(true);
        left.setAxisMinimum((int) minY - 1);
        left.setAxisMaximum((int) maxY + 1);
        left.setGranularityEnabled(true);
        left.setGranularity(1);

        YAxis right = mChart.getAxisRight();
        right.setDrawGridLines(false);
        right.setDrawAxisLine(false);
        right.setDrawLabels(false);
        right.setAxisMinimum((int) minY - 1);
        right.setAxisMaximum((int) maxY + 1);
        right.setGranularityEnabled(true);
        right.setGranularity(1);

        // legend formatting
        Legend legend = mChart.getLegend();
        legend.setEnabled(false);

        // chart interaction
        mDataSet.setDrawHighlightIndicators(true);
        mDataSet.setHighLightColor(primaryColor);
        mChart.setPinchZoom(true);
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                displayDialog(e.getX(), e.getY());
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void displayDialog(double selectedX, double selectedY) {
        for (int i = 0; i < mGdDataList.size(); i++) {
            // get current y value
            double curY = 0;
            switch (mChartType) {
                case 0:
                    curY = mGdDataList.get(i).getGlucose();
                    break;
                case 1:
                    curY = (double) mGdDataList.get(i).getCarbs();
                    break;
                case 2:
                    curY = mGdDataList.get(i).getActivityTime();
                    break;
                case 3:
                    curY = mGdDataList.get(i).getWeight();
                    break;
                case 4:
                    String[] parts = mGdDataList.get(i).getBloodPressure().split("/");
                    curY = Float.valueOf(parts[0]);
                    break;
            }

            // get current x value
            double curX = 0;
            if (mPeriod == 0)
                curX = mGdDataList.get(i).hoursOfDay();
            else if (mPeriod == 1)
                curX = mGdDataList.get(i).hoursOfMonth();

            //check for equivalence
            if (curX == selectedX && (Math.abs(curY - selectedY) < 0.001)) {
                mDialog = new Dialog(DashboardActivity.this);
                mDialog.setContentView(R.layout.dialog_chart_marker);

                TextView glucText = mDialog.findViewById(R.id.glucose_val);
                TextView carbsText = mDialog.findViewById(R.id.carbs_val);
                TextView actText = mDialog.findViewById(R.id.act_val);
                TextView weightText = mDialog.findViewById(R.id.weight_val);
                TextView bpText = mDialog.findViewById(R.id.bp_val);
                TextView symptomsText = mDialog.findViewById(R.id.symptoms_val);

                glucText.setText(mGdDataList.get(i).getGlucose() + " mmol/L");
                carbsText.setText(mGdDataList.get(i).getCarbs() + " g");
                actText.setText(mGdDataList.get(i).getActivityTime() + " hrs");
                weightText.setText(mGdDataList.get(i).getWeight() + " Kg");
                bpText.setText(mGdDataList.get(i).getBloodPressure() + " mmHg");

                String[] parts = mGdDataList.get(i).getSymptoms().split(",");
                String symptoms = " ";
                for (int j = 0; j < parts.length - 1; j++) {
                    symptoms += parts[j] + ",\n";
                }
                symptomsText.setText(symptoms);

                mDialog.show();

            }
        }
    }

    private void initialiseProgressBar() {

        try {
            // get due date in millis
            long dueDate = 0;
            if (mUser != null)
                dueDate = mUser.getDueDate();

            // set due date text
            Date dueDateObj = new Date(dueDate);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy");
            String dateText = sdf.format(dueDateObj);
            mDueDateText.setText(dateText);

            // get current date in millis
            long curDate = System.currentTimeMillis();

            // max is 9 months in milliseconds
            long gestationPeriod = 24192000000L;

            // progress is due - current / 9 months
            float progress = ((float) (dueDate - curDate) / (float) gestationPeriod) * 100;
            mProgressBar.setProgress(100 - (int) progress);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Error with pull parse", e);
            mProgressBar.setProgress(0);
        }

    }

    private void initialiseDisplayText() {
        DecimalFormat df = new DecimalFormat("#.#");

        String glucoseStr, carbsStr, activityStr, weightStr, bpStr;

        double glucose = 0;
        int carbs = 0;
        double activityTime = 0;
        double weight = 0;
        int count = 0;
        double systolic = 0;
        double diastolic = 0;
        int zeroWeightCount = 0; //checks if weight is not entered and does not include it in avg calc
        int zeroGlucCount = 0; //checks if gluc is not entered and does not include it in avg calc
        int zeroBpCount = 0; //checks if BP is not entered and does not include it in avg calc

        // Day
        if (mPeriod == 0) {
            for (int i = 0; i < mGdDataList.size(); i++) {
                if (mGdDataList.get(i).month() - 1 == mMonthOfYear && mGdDataList.get(i).day() == mDayOfMonth) {
                    glucose += mGdDataList.get(i).getGlucose();
                    if (mGdDataList.get(i).getGlucose() == 0)
                        zeroGlucCount++;

                    carbs += mGdDataList.get(i).getCarbs();

                    activityTime += mGdDataList.get(i).getActivityTime();

                    weight += mGdDataList.get(i).getWeight();
                    if (mGdDataList.get(i).getWeight() == 0)
                        zeroWeightCount++;


                    String[] parts = mGdDataList.get(i).getBloodPressure().split("/");
                    systolic += Double.valueOf(parts[0]);
                    diastolic += Double.valueOf(parts[1]);
                    if (Double.valueOf(parts[0]) == 0)
                        zeroBpCount++;

                    count++;
                }
            }
        }

        // month
        if (mPeriod == 1) {
            for (int i = 0; i < mGdDataList.size(); i++) {
                if (mGdDataList.get(i).month() - 1 == mMonthOfYear) {
                    glucose += mGdDataList.get(i).getGlucose();
                    if (mGdDataList.get(i).getGlucose() == 0)
                        zeroGlucCount++;

                    carbs += mGdDataList.get(i).getCarbs();

                    activityTime += mGdDataList.get(i).getActivityTime();

                    weight += mGdDataList.get(i).getWeight();
                    if (mGdDataList.get(i).getWeight() == 0)
                        zeroWeightCount++;


                    String[] parts = mGdDataList.get(i).getBloodPressure().split("/");
                    systolic += Double.valueOf(parts[0]);
                    diastolic += Double.valueOf(parts[1]);
                    if (Double.valueOf(parts[0]) == 0)
                        zeroBpCount++;

                    count++;
                }
            }
        }

        // full gestation
        if (mPeriod == 2) {
            for (int i = 0; i < mGdDataList.size(); i++) {
                glucose += mGdDataList.get(i).getGlucose();
                if (mGdDataList.get(i).getGlucose() == 0)
                    zeroGlucCount++;

                carbs += mGdDataList.get(i).getCarbs();

                activityTime += mGdDataList.get(i).getActivityTime();

                weight += mGdDataList.get(i).getWeight();
                if (mGdDataList.get(i).getWeight() == 0)
                    zeroWeightCount++;

                String[] parts = mGdDataList.get(i).getBloodPressure().split("/");
                systolic += Double.valueOf(parts[0]);
                diastolic += Double.valueOf(parts[1]);
                if (Double.valueOf(parts[0]) == 0)
                    zeroBpCount++;

                count++;
            }
        }

        if (count != 0) {
            glucose = glucose / (count - zeroGlucCount);
            if (count - zeroGlucCount == 0)
                glucose = 0;

            weight = weight / (count - zeroWeightCount);
            if (count - zeroWeightCount == 0)
                weight = 0;

            systolic = systolic / (count - zeroBpCount);
            diastolic = diastolic / (count - zeroBpCount);
            if (count - zeroBpCount == 0) {
                systolic = 0;
                diastolic = 0;
            }
        }

        glucoseStr = df.format(glucose) + "\nmmol/L";
        carbsStr = carbs + "\ng";
        activityStr = df.format(activityTime) + "\nhrs";
        weightStr = df.format(weight) + "\nKg";
        bpStr = (int) systolic + "/" + (int) diastolic + "\nmmHg";

        mGlucoseText.setText(glucoseStr);
        mCarbText.setText(carbsStr);
        mActivityTimeText.setText(activityStr);
        mWeightText.setText(weightStr);
        mBPText.setText(bpStr);

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
            case R.id.profile_menu:
                Intent profileIntent = new Intent(DashboardActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.signout_menu:
                AuthUI.getInstance().signOut(this);
                Intent logoutIntent = new Intent(DashboardActivity.this, LandingActivity.class);
                startActivity(logoutIntent);
                return true;
            case R.id.settings_menu:
                Intent settingsIntent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.help_menu:
                Intent helpIntent = new Intent(DashboardActivity.this, HelpActivity.class);
                startActivity(helpIntent);
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
        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    GdData data = dataSnapshot.getValue(GdData.class);
                    mGdDataList.add(data);
                    Collections.sort(mGdDataList);

                    // add data to entries list if it is from the selected month
                    if (data.month() - 1 == mMonthOfYear)
                        mEntries.add(new Entry((float) data.hoursOfMonth(), (float) data.getGlucose()));

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

            if (mValueEventListenerForRanges == null) {
                mValueEventListenerForRanges = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mRanges = dataSnapshot.getValue(HpSpecifiedRanges.class);

                        // Value events are always triggered last
                        // and are guaranteed to contain updates from any other events
                        // which occurred before that snapshot was taken

                        // Initialise chart
                        initialiseChart();

                        // Initialise display text
                        initialiseDisplayText();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
            }
            mRangesDatabaseReference.addListenerForSingleValueEvent(mValueEventListenerForRanges);

            if (mValueEventListenerForDD == null) {
                mValueEventListenerForDD = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUser = dataSnapshot.getValue(User.class);

                        // Initialise progress bar
                        initialiseProgressBar();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
            }
            mUsersDatabaseReference.addListenerForSingleValueEvent(mValueEventListenerForDD);

        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mGdDataDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mValueEventListenerForDD != null) {
            mUsersDatabaseReference.removeEventListener(mValueEventListenerForDD);
            mValueEventListenerForDD = null;
        }
        if (mValueEventListenerForRanges != null) {
            mRangesDatabaseReference.removeEventListener(mValueEventListenerForRanges);
            mValueEventListenerForRanges = null;
        }
    }

}
