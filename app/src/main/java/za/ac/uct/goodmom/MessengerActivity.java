package za.ac.uct.goodmom;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MessengerActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final String CHANNEL_ID = "channel_01";


    private ArrayList<Message> mMessagetList;
    private RecyclerView mMessageRecyclerView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername, mUserId;
    private boolean mNewMessageNotifications;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mValueEventListener;
    private FirebaseAuth mFirebaseAuth;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    Intent messengerIntent = new Intent(MessengerActivity.this, DashboardActivity.class);
                    startActivity(messengerIntent);
                    return true;
                case R.id.navigation_reminders:
                    Intent remindersIntent = new Intent(MessengerActivity.this, RemindersActivity.class);
                    startActivity(remindersIntent);
                    return true;
                case R.id.navigation_messenger:
                    // Current activity
                    return true;
                case R.id.navigation_tips:
                    Intent tipsIntent = new Intent(MessengerActivity.this, TipsActivity.class);
                    startActivity(tipsIntent);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        // Initialise Firebase components
        mFirebasedatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mUsername = user.getDisplayName();
        mUserId = user.getUid();
        mMessagesDatabaseReference = mFirebasedatabase.getReference().child("patients").child(mUserId).child("messages");

        // Initialize references to views
        mProgressBar = findViewById(R.id.progress_bar);
        mMessageRecyclerView = findViewById(R.id.recycler_view);
        mMessageEditText = findViewById(R.id.message_edit_text);
        mSendButton = findViewById(R.id.send_button);

        // Initialise variables
        mNewMessageNotifications = false;
        mMessagetList = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(mMessagetList);

        // Set up the recycler view
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mMessageRecyclerView.setLayoutManager(mLayoutManager);
        mMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mMessageRecyclerView.addItemDecoration(new CustomDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));

        // set the adapter
        mMessageRecyclerView.setAdapter(mMessageAdapter);

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message(mMessageEditText.getText().toString(), mUsername);

                mMessagesDatabaseReference.push().setValue(message);

                // Clear input box
                mMessageEditText.setText("");
            }
        });

        // Attach the database read listener
        attachDatabaseReadListener();

        // Set up progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // Set up bottom navigation view
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //set up menu
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent dashboardIntent = new Intent(MessengerActivity.this, DashboardActivity.class);
        startActivity(dashboardIntent);
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
                Intent profileIntent = new Intent(MessengerActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.signout_menu:
                onSignedOutCleanup();
                AuthUI.getInstance().signOut(this);
                Intent logoutIntent = new Intent(MessengerActivity.this, LandingActivity.class);
                startActivity(logoutIntent);
                return true;
            case R.id.settings_menu:
                Intent settingsIntent = new Intent(MessengerActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.help_menu:
                Intent helpIntent = new Intent(MessengerActivity.this, HelpActivity.class);
                startActivity(helpIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMessagetList.clear();
        detachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mMessagetList.clear();
        detachDatabaseReadListener();
        stopService(new Intent(this, NotificationService.class));
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Message message = dataSnapshot.getValue(Message.class);
                    mMessagetList.add(message);

                    mMessageRecyclerView.smoothScrollToPosition(mMessagetList.size() - 1);

                    mMessageAdapter.notifyDataSetChanged();
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
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }

        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Value events are always triggered last
                    // and are guaranteed to contain updates from any other events
                    // which occurred before that snapshot was taken

                    // Enable new message notifications
                    mNewMessageNotifications = true;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
        mMessagesDatabaseReference.addValueEventListener(mValueEventListener);
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

}
