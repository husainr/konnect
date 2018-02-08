package com.example.aliasgarbs.konnect;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId;
    private String messageReceiverName;

    private Toolbar chatToolbar;
    private TextView usernameTitle;
    private TextView userlastseen;
    private CircleImageView userchatprofileimg;

    private DatabaseReference rootref;
    private FirebaseAuth mAuth;

    private ImageButton sendmsgbtn;
    private ImageButton selectimgbtn;
    private EditText inputmsgtxt;
    private String messageSenderId;

    private RecyclerView userMessagesList;
    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private static int Gallery_Pick = 1;

    private StorageReference MessageImageStorageRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().getString("user_name").toString();

        MessageImageStorageRef = FirebaseStorage.getInstance().getReference().child("Messages_Pictures");

        chatToolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chatToolbar);

        loadingBar = new ProgressDialog(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflator = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflator.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        usernameTitle = (TextView) findViewById(R.id.custom_profile_name);
        userlastseen = (TextView) findViewById(R.id.custom_user_last_seen);
        userchatprofileimg = (CircleImageView) findViewById(R.id.custom_profile_image);

        sendmsgbtn = (ImageButton) findViewById(R.id.send_message_btn);
        selectimgbtn = (ImageButton) findViewById(R.id.select_image_btn);
        inputmsgtxt = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messageList);

        userMessagesList = (RecyclerView) findViewById(R.id.messages_list_users);

        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        fetchMessages();


        usernameTitle.setText(messageReceiverName);
        rootref.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String online = dataSnapshot.child("online").getValue().toString();
                final String userthumb = dataSnapshot.child("user_thumb_image").getValue().toString();

                Picasso.with(ChatActivity.this).load(userthumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                        .into(userchatprofileimg, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(ChatActivity.this).load(userthumb).placeholder(R.drawable.default_profile).into(userchatprofileimg);

                            }
                        });

                if (online.equals("true")){
                    userlastseen.setText("online");
                }
                else
                {
                    LastSeenTime getTime = new LastSeenTime();
                    long last_seen = Long.parseLong(online);
                    String last_seen_display = getTime.getTimeAgo(last_seen, getApplicationContext()).toString();
                    userlastseen.setText(last_seen_display);

                }




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendmsgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        selectimgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });





    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            loadingBar.setTitle("Sending Image");
            loadingBar.setMessage("Please wait while your image is sending");
            loadingBar.show();

            Uri ImageUri = data.getData();
            final String message_sender_ref = "Messages/" + messageSenderId + "/" + messageReceiverId;
            final String message_receiver_ref = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference user_message_key = rootref.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();
            final String message_push_id = user_message_key.getKey();

            StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){

                        final String downloadUrl = task.getResult().getDownloadUrl().toString();

                        Map messagetxtbody = new HashMap();
                        messagetxtbody.put("message",downloadUrl);
                        messagetxtbody.put("seen",false);
                        messagetxtbody.put("type","image");
                        messagetxtbody.put("time", ServerValue.TIMESTAMP);
                        messagetxtbody.put("from",messageSenderId);

                        Map messagebodydetails = new HashMap();
                        messagebodydetails.put(message_sender_ref + "/" + message_push_id, messagetxtbody);
                        messagebodydetails.put(message_receiver_ref + "/" + message_push_id, messagetxtbody);

                        rootref.updateChildren(messagebodydetails, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null){
                                    Log.d("Chat_Log", databaseError.getMessage().toString());
                                }

                                inputmsgtxt.setText("");
                                loadingBar.dismiss();
                            }
                        });

                        Toast.makeText(ChatActivity.this, "Picture sent successfully", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                    else {
                        Toast.makeText(ChatActivity.this, "Picture not sent. Try again", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }




    private void fetchMessages() {
        rootref.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messageList.add(messages);
                        messageAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }




    private void SendMessage() {
        String msg = inputmsgtxt.getText().toString();
        if (TextUtils.isEmpty(msg)){
            Toast.makeText(ChatActivity.this, "please enter your message", Toast.LENGTH_SHORT).show();
        }
        else{
            String message_sender_ref = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String message_receiver_ref = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference user_message_key = rootref.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();
            String message_push_id = user_message_key.getKey();

            Map messagetxtbody = new HashMap();
            messagetxtbody.put("message",msg);
            messagetxtbody.put("seen",false);
            messagetxtbody.put("type","text");
            messagetxtbody.put("time", ServerValue.TIMESTAMP);
            messagetxtbody.put("from",messageSenderId);

            Map messagebodydetails = new HashMap();
            messagebodydetails.put(message_sender_ref + "/" + message_push_id, messagetxtbody);
            messagebodydetails.put(message_receiver_ref + "/" + message_push_id, messagetxtbody);

            rootref.updateChildren(messagebodydetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null){
                        Log.d("chat_log",databaseError.getMessage().toString());
                    }

                    inputmsgtxt.setText("");
                }
            });




        }
    }
}
