package com.example.eventer2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eventer2.GoogleMapAndPlaces.InfoMapActivity;
import com.example.eventer2.R;
import com.example.eventer2.adapters.GuestRecyclerAdapter;
import com.example.eventer2.models.Guest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventInfoActivity extends AppCompatActivity {

    private static final String TAG = "EventInfoActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    final Date today = new Date();

    private TextView mName;
    private TextView mTheme;
    private TextView mLocation;
    private TextView mStartDate, mEndDate;
    private TextView mStartTime, mEndTime;
    private ImageView mImageView;
    private ImageView mInviteBtn;
    private ImageView mExportBtn;
    private Button mEventerBtn, mAdminBtn;


    private String mEventId;
    private String currentUserId;
    private String mAuthorId;
    private String location;

    private List<Guest> guest_list;
    private RecyclerView guest_recycler_view;
    private GuestRecyclerAdapter guest_adapter;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        init();

        //guest recycler
        guest_recycler_view = findViewById(R.id.guest_list_view);
        guest_list = new ArrayList<>();
        guest_recycler_view.setLayoutManager(new LinearLayoutManager(this));

        mEventId = getIntent().getStringExtra("eventId");
        currentUserId = mAuth.getCurrentUser().getUid();




        mFirestore.collection("Events").document(mEventId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().exists()){
                    final String name = task.getResult().getString("name");
                    String theme = task.getResult().getString("theme");
                    location = task.getResult().getString("eventLocation");
                    final String startDate = task.getResult().getString("startDate");
                    final String startTime = task.getResult().getString("startTime");
                    final String endDate = task.getResult().getString("endDate");
                    final String endTime = task.getResult().getString("endTime");
                    String image = task.getResult().getString("image_url");
                    mAuthorId = task.getResult().getString("authorId");

                    mName.setText(name);
                    mTheme.setText(theme);
                    mLocation.setText(location);
                    mStartDate.setText(startDate);
                    mEndDate.setText(endDate);
                    mStartTime.setText(startTime);
                    mEndTime.setText(endTime);
                    Glide.with(EventInfoActivity.this).load(image).into(mImageView);

                    if(mAuthorId.equals(currentUserId)) {
                        try {
                            Date eventEndDate = format.parse(endDate);
                            if (today.after(eventEndDate)) {
                                mInviteBtn.setVisibility(View.INVISIBLE);
                                mExportBtn.setVisibility(View.INVISIBLE);
                            } else if (mAuthorId.equals(currentUserId) && !today.after(eventEndDate)) {
                                mInviteBtn.setVisibility(View.VISIBLE);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

//                        mExportBtn.setOnClickListener(v -> {
//                            Intent intent = new Intent(Intent.ACTION_EDIT);
//                            intent.setType("vnd.android.cursor.item/event");
////                                intent.putExtra(CalendarContract.Events.DTSTART, "21/5/2019 15:26:33");
//                            intent.putExtra(CalendarContract.Events.DTEND, endDate);
//                            intent.putExtra(CalendarContract.Events.TITLE, name);
//                            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
//                            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, "26.06.2019 15:26:33");
//                            startActivity(intent);
//                        });
                }
            }else {
                Toast.makeText(this, "Something was wrong: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });

        //prikazivanje gostiju
        onLoadGuests();

        mEventerBtn.setOnClickListener(v -> {
            guest_list.clear();
            mFirestore.collection("Events/" + mEventId + "/Guests").addSnapshotListener((queryDocumentSnapshots, e) -> {
                if(queryDocumentSnapshots != null){
                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){

                            Guest guests = doc.getDocument().toObject(Guest.class);
                            String guestId = guests.getUserId();

                            if(guestId != null){
                                guest_list.add(guests);
                                guest_adapter.notifyDataSetChanged();
                            }

                        }
                    }
                }
            });

            mEventerBtn.setEnabled(false);
            mAdminBtn.setEnabled(true);

            mEventerBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            mEventerBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
            mEventerBtn.setTextSize(16);
            mAdminBtn.setTextSize(12);
            mAdminBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            mAdminBtn.setTextColor(getResources().getColor(R.color.colorAccent));

        });

        mAdminBtn.setOnClickListener(v -> {
            guest_list.clear();
            mFirestore.collection("Events/" + mEventId + "/Guests").addSnapshotListener((queryDocumentSnapshots, e) -> {
                if(queryDocumentSnapshots != null){
                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){

                            Guest guests = doc.getDocument().toObject(Guest.class);
                            String guestId = guests.getUserId();
                            if(guestId == null){
                                guest_list.add(guests);
                                guest_adapter.notifyDataSetChanged();
                            }
                        }
                    }

                }
            });
            mEventerBtn.setEnabled(true);
            mAdminBtn.setEnabled(false);

            mAdminBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            mAdminBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
            mAdminBtn.setTextSize(16);
            mEventerBtn.setTextSize(12);
            mEventerBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            mEventerBtn.setTextColor(getResources().getColor(R.color.colorAccent));
        });


        guest_adapter = new GuestRecyclerAdapter(guest_list);
        guest_recycler_view.setAdapter(guest_adapter);
        guest_adapter.notifyDataSetChanged();


        mInviteBtn.setOnClickListener(v -> {
            Intent inviteIntent = new Intent(EventInfoActivity.this, InviteActivity.class);
            inviteIntent.putExtra("eventId", mEventId);
            startActivity(inviteIntent);
            finish();
        });

        mLocation.setOnClickListener(v -> {
            onLocation(location);
        });
    }

    private void init(){
        mName = findViewById(R.id.event_info_name);
        mTheme = findViewById(R.id.event_info_theme);
        mLocation = findViewById(R.id.info_location);
        mStartDate = findViewById(R.id.info_start_date);
        mEndDate = findViewById(R.id.info_end_date);
        mStartTime = findViewById(R.id.info_start_time);
        mEndTime = findViewById(R.id.info_end_time);
        mInviteBtn = findViewById(R.id.info_invate_friends);
        mImageView = findViewById(R.id.event_info_bg);
        mExportBtn = findViewById(R.id.export_event_btn);

        mEventerBtn = findViewById(R.id.event_info_base_btn);
        mAdminBtn = findViewById(R.id.event_info_admin_btn);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

    }

    private void onLocation(String location){
        Intent infoMap = new Intent(this, InfoMapActivity.class);
        infoMap.putExtra("location", location);
        startActivity(infoMap);
    }


    private List<Guest> onLoadGuests(){
        guest_list.clear();
        mFirestore.collection("Events/" + mEventId + "/Guests").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if(queryDocumentSnapshots != null){
                for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){

                        Guest guests = doc.getDocument().toObject(Guest.class);
                        String guestId = guests.getUserId();

                        if(guestId != null){
                            guest_list.add(guests);
                            guest_adapter.notifyDataSetChanged();
                        }

                    }
                }
            }
        });

        return guest_list;
    }


    //google maps service
    public boolean isServiceOK(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(EventInfoActivity.this);

        if(available == ConnectionResult.SUCCESS){
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(EventInfoActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}