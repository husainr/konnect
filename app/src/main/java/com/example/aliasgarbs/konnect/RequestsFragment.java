package com.example.aliasgarbs.konnect;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView myrequestlist;

    private View myMainView;
    private FirebaseAuth mAuth;
    private DatabaseReference frndsreqref;
    String online_user_id;
    private DatabaseReference usersref;


    private DatabaseReference FriendsDatabaseref;
    private DatabaseReference FriendsReqDatabaseref;



    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        myMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        myrequestlist = (RecyclerView) myMainView.findViewById(R.id.requests_list);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();


        frndsreqref = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(online_user_id);
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");

        FriendsDatabaseref = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReqDatabaseref = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");


        myrequestlist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myrequestlist.setLayoutManager(linearLayoutManager);

        return myMainView;
    }




    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(
                        Requests.class,
                        R.layout.friend_request_all_user_layout,
                        RequestsFragment.RequestViewHolder.class,
                        frndsreqref
                ) {
                    @Override
                    protected void populateViewHolder(final RequestViewHolder viewHolder, Requests model, int position) {
                        final String list_users_id = getRef(position).getKey();
                        DatabaseReference get_type_ref = getRef(position).child("request_type").getRef();
                        get_type_ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String request_type = dataSnapshot.getValue().toString();

                                    if (request_type.equals("received")){

                                        Button req_sent_btn = (Button) viewHolder.mView.findViewById(R.id.request_accept_btn);
                                        req_sent_btn.setBackgroundColor(Color.RED);
                                        req_sent_btn.setText("Req received");

                                        viewHolder.mView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);

                                        usersref.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                final String userName = dataSnapshot.child("user_name").getValue().toString();
                                                final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                                final String userStatus = dataSnapshot.child("user_status").getValue().toString();

                                                viewHolder.setUserName(userName);
                                                viewHolder.setThumb_user_image(thumbImage, getContext());
                                                viewHolder.setUser_Status(userStatus);
                                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Accept Friend Request",
                                                                        "Cancel Friend Request"
                                                                };

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Friend Request options");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int position) {
                                                                if (position == 0){

                                                                    Calendar callForeDate = Calendar.getInstance();
                                                                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                                                    final String saveCurrentDate = currentDate.format(callForeDate.getTime());

                                                                    FriendsDatabaseref.child(online_user_id).child(list_users_id).child("date").setValue(saveCurrentDate)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    FriendsDatabaseref.child(list_users_id).child(online_user_id).child("date").setValue(saveCurrentDate)
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    FriendsReqDatabaseref.child(online_user_id).child(list_users_id)
                                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()){
                                                                                                                FriendsReqDatabaseref.child(list_users_id).child(online_user_id).removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if (task.isSuccessful()){
                                                                                                                                    Toast.makeText(getContext(), "Friend Req Accepted!!!", Toast.LENGTH_SHORT).show();
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

                                                                if (position == 1){

                                                                    FriendsReqDatabaseref.child(online_user_id).child(list_users_id)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                FriendsReqDatabaseref.child(list_users_id).child(online_user_id).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "Friend Req Cancelled!!!", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });


                                                                }

                                                            }
                                                        });
                                                        builder.show();

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }

                                    else if (request_type.equals("sent")){

                                        Button req_sent_btn = (Button) viewHolder.mView.findViewById(R.id.request_accept_btn);
                                        req_sent_btn.setText("Req sent");

                                        viewHolder.mView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);

                                        usersref.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                final String userName = dataSnapshot.child("user_name").getValue().toString();
                                                final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                                final String userStatus = dataSnapshot.child("user_status").getValue().toString();

                                                viewHolder.setUserName(userName);
                                                viewHolder.setThumb_user_image(thumbImage, getContext());
                                                viewHolder.setUser_Status(userStatus);
                                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Cancel Friend Request"
                                                                };

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Friend Request Sent");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int position) {


                                                                if (position == 0){

                                                                    FriendsReqDatabaseref.child(online_user_id).child(list_users_id)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                FriendsReqDatabaseref.child(list_users_id).child(online_user_id).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "Friend Req Cancelled!!!", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });


                                                                }

                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                };

        myrequestlist.setAdapter(firebaseRecyclerAdapter);


    }






    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserName(String userName) {
            TextView usernamedisplay = (TextView) mView.findViewById(R.id.request_profile_name);
            usernamedisplay.setText(userName);
        }

        public void setThumb_user_image(final String thumbImage, final Context ctx) {
            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.request_profile_image);




            Picasso.with(ctx).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.default_profile).into(thumb_image);

                        }
                    });
        }

        public void setUser_Status(String userStatus) {
            TextView usernamedisplay = (TextView) mView.findViewById(R.id.request_profile_status);
            usernamedisplay.setText(userStatus);

        }
    }

}
