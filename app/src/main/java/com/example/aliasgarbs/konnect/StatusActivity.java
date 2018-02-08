package com.example.aliasgarbs.konnect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText StatusInput;
    private Button SaveChangesButton;

    private DatabaseReference changeStatusRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        changeStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SaveChangesButton = (Button) findViewById(R.id.save_status_change_button);
        StatusInput = (EditText) findViewById(R.id.status_input);

        loadingBar = new ProgressDialog(this);

        String old_status = getIntent().getExtras().get("user_status").toString();

        StatusInput.setText(old_status);

        SaveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newStatus = StatusInput.getText().toString();

                ChangeProfileStatus(newStatus);
            }
        });

    }

    private void ChangeProfileStatus(String newStatus) {
        if(TextUtils.isEmpty(newStatus)){
            Toast.makeText(StatusActivity.this, "Please Enter New Status", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Change Profile Status");
            loadingBar.setMessage("Please wait while we are updating profile status");
            loadingBar.show();
            changeStatusRef.child("user_status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        loadingBar.dismiss();
                        Intent settingsIntent = new Intent(StatusActivity.this,SettingsActivity.class);
                        startActivity(settingsIntent);
                        Toast.makeText(StatusActivity.this, "Profile Status Updated!!!", Toast.LENGTH_LONG).show();

                    }
                    else {
                        Toast.makeText(StatusActivity.this, "Error Occurred!!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
