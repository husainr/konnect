package com.example.aliasgarbs.konnect;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private Button Sendfriendrequest;
    private Button Declinefriendrequest;
    private TextView Profilename;
    private TextView Profilestatus;

    private ImageView Profileimage;

    private DatabaseReference UsersReference;

    private String CURRENT_STATE;

    private DatabaseReference friendreqref;
    private FirebaseAuth mAuth;
    String sender_user_id;
    String receiver_user_id;

    private DatabaseReference friendsref;

    private DatabaseReference NotificationsRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        friendreqref = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendreqref.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();

        friendsref = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsref.keepSynced(true);

        NotificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationsRef.keepSynced(true);


        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

         receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();



        Sendfriendrequest = (Button) findViewById(R.id.profile_visit_send_req_button);
        Declinefriendrequest = (Button) findViewById(R.id.profile_decline_req_btn);
        Profilename = (TextView) findViewById(R.id.profile_visit_username);
        Profilestatus = (TextView) findViewById(R.id.profile_visit_userstatus);
        Profileimage = (ImageView) findViewById(R.id.profile_visit_user_image);


        CURRENT_STATE = "not_friends";

        UsersReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();


                Profilename.setText(name);
                Profilestatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_profile).into(Profileimage);

                friendreqref.child(sender_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(receiver_user_id)){
                                String reqType = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                                if (reqType.equals("sent")){
                                    CURRENT_STATE = "request_sent";
                                    Sendfriendrequest.setText("Cancel Friend Request");
                                    Declinefriendrequest.setVisibility(View.INVISIBLE);
                                    Declinefriendrequest.setEnabled(false);
                                }

                                else if (reqType.equals("received")){
                                    CURRENT_STATE = "request_received";
                                    Sendfriendrequest.setText("Accept Friend Request");
                                    Declinefriendrequest.setVisibility(View.VISIBLE);
                                    Declinefriendrequest.setEnabled(true);

                                    Declinefriendrequest.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            DeclineFriendRequest();
                                        }
                                    });


                                }
                            }


                        else {
                            friendsref.child(sender_user_id).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiver_user_id)){
                                                CURRENT_STATE = "friends";
                                                Sendfriendrequest.setText("Unfriend this person");
                                                Declinefriendrequest.setVisibility(View.INVISIBLE);
                                                Declinefriendrequest.setEnabled(false);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Declinefriendrequest.setVisibility(View.INVISIBLE);
        Declinefriendrequest.setEnabled(false);


        if (!sender_user_id.equals(receiver_user_id)){
            Sendfriendrequest.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Sendfriendrequest.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")){
                        SendFriendRequest();
                    }

                    if (CURRENT_STATE.equals("request_sent")){
                        CancelFriendReq();
                    }

                    if (CURRENT_STATE.equals("request_received")){
                        AcceptFriendReq();
                    }

                    if (CURRENT_STATE.equals("friends")){
                        UnFriendsFriend();
                    }
                }
            });
        }
        else {
            Declinefriendrequest.setVisibility(View.INVISIBLE);
            Sendfriendrequest.setVisibility(View.INVISIBLE);
        }
    }

    private void DeclineFriendRequest() {

        friendreqref.child(sender_user_id).child(receiver_user_id)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendreqref.child(receiver_user_id).child(sender_user_id).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Sendfriendrequest.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        Sendfriendrequest.setText("Send Friend Request");

                                        Declinefriendrequest.setVisibility(View.INVISIBLE);
                                        Declinefriendrequest.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });

    }

    private void UnFriendsFriend() {
        friendsref.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendsref.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Sendfriendrequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                Sendfriendrequest.setText("Send Friend Request");

                                                Declinefriendrequest.setVisibility(View.INVISIBLE);
                                                Declinefriendrequest.setEnabled(false);

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendReq() {
        Calendar callForeDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurrentDate = currentDate.format(callForeDate.getTime());

        friendsref.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        friendsref.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        friendreqref.child(sender_user_id).child(receiver_user_id)
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    friendreqref.child(receiver_user_id).child(sender_user_id).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        Sendfriendrequest.setEnabled(true);
                                                                        CURRENT_STATE = "friends";
                                                                        Sendfriendrequest.setText("Unfriend this person");

                                                                        Declinefriendrequest.setVisibility(View.INVISIBLE);
                                                                        Declinefriendrequest.setEnabled(false);
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                                    }
                                });
                    }
                });
    }

    private void CancelFriendReq() {
        friendreqref.child(sender_user_id).child(receiver_user_id)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendreqref.child(receiver_user_id).child(sender_user_id).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Sendfriendrequest.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        Sendfriendrequest.setText("Send Friend Request");

                                        Declinefriendrequest.setVisibility(View.INVISIBLE);
                                        Declinefriendrequest.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void SendFriendRequest() {
        friendreqref.child(sender_user_id).child(receiver_user_id)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendreqref.child(receiver_user_id).child(sender_user_id)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                HashMap<String, String> notificationsData = new HashMap<String, String>();
                                                notificationsData.put("from", sender_user_id);
                                                notificationsData.put("type", "request");

                                                NotificationsRef.child(receiver_user_id).push().setValue(notificationsData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    Sendfriendrequest.setEnabled(true);
                                                                    CURRENT_STATE = "request_sent";
                                                                    Sendfriendrequest.setText("Cancel Friend Request");

                                                                    Declinefriendrequest.setVisibility(View.INVISIBLE);
                                                                    Declinefriendrequest.setEnabled(false);

                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

}
