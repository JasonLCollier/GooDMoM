package za.ac.uct.goodmom;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationService extends Service {

    public static final String CHANNEL_ID = "channel_01";

    private String mUserId;
    private boolean mNewMessageNotifications;

    private ArrayList<Event> mEventList;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mMessagesDatabaseReference, mEventsDatabaseReference;
    private ChildEventListener mChildEventListenerForMessages, mChildEventListenerForEvents;
    private ValueEventListener mValueEventListener;
    private FirebaseAuth mFirebaseAuth;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialise Firebase components
        mFirebasedatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mUserId = user.getUid();
        mMessagesDatabaseReference = mFirebasedatabase.getReference().child("patients").child(mUserId).child("messages");
        mEventsDatabaseReference = mFirebasedatabase.getReference().child("patients").child(mUserId).child("events");

        // Attach the database read listener
        attachDatabaseReadListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListenerForMessages == null) {
            mChildEventListenerForMessages = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (mNewMessageNotifications) {
                        createNotificationChannel();
                        addNotification(message.getText());
                    }
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
            mMessagesDatabaseReference.addChildEventListener(mChildEventListenerForMessages);
        }

        if (mChildEventListenerForEvents == null) {
            mChildEventListenerForEvents = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Event event = dataSnapshot.getValue(Event.class);
                    mEventList.add(event);
                    Collections.sort(mEventList);
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
            mEventsDatabaseReference.addChildEventListener(mChildEventListenerForEvents);
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
        if (mChildEventListenerForMessages != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListenerForMessages);
            mChildEventListenerForMessages = null;
        }
        if (mChildEventListenerForMessages != null) {
            mMessagesDatabaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    private void addNotification(String content) {
        Intent notificationIntent = new Intent(this, MessengerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.baseline_notifications_24)
                        .setContentTitle("GooDMoM")
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "GooDMoM", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("New notification");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
