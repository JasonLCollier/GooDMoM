package za.ac.uct.goodmom;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class PersonalInformationFragment extends Fragment {

    public static final String ANONYMOUS = "anonymous";

    private Button mSaveButton;
    private EditText mIdText;
    private EditText mEmailText;
    private EditText mHouseText;
    private EditText mStreetText;
    private EditText mCityText;
    private EditText mProvText;
    private EditText mPostText;

    private String mUsername;

    // Firebase instance variables
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;


    public PersonalInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_personal_information, container, false);

        // Initialize references to views
        mSaveButton = rootView.findViewById(R.id.save_button);
        mIdText = rootView.findViewById(R.id.id_number);
        mEmailText = rootView.findViewById(R.id.email);
        mHouseText = rootView.findViewById(R.id.house_number);
        mStreetText = rootView.findViewById(R.id.street_name);
        mCityText = rootView.findViewById(R.id.city_name);
        mProvText = rootView.findViewById(R.id.province_name);
        mPostText = rootView.findViewById(R.id.postal_code);

        // Initialise Firebase components
        mFirebasedatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mMessagesDatabaseReference = mFirebasedatabase.getReference().child("users");

        // Initialise username
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mUsername = user.getDisplayName();

        // Set a click listener on that button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Log In button is clicked on.
            @Override
            public void onClick(View view) {

                mMessagesDatabaseReference.push().child(mUsername).child("phoneNo").setValue("0720001111");

            }
        });

        return rootView;
    }

}
