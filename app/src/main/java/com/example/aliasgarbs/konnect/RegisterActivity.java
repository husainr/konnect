package com.example.aliasgarbs.konnect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;
    //private ProgressDialog loadingBar;
    private ProgressBar mbar;

    private EditText RegisterUserName;
    private EditText RegisterUserEmail;
    private EditText RegisterUserPassword;
    private Button CreateAccountButton;
    private TextView logoname;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RegisterUserName = (EditText) findViewById(R.id.register_name);
        RegisterUserEmail = (EditText) findViewById(R.id.register_email);
        RegisterUserPassword = (EditText) findViewById(R.id.register_password);
        CreateAccountButton = (Button) findViewById(R.id.create_account_button);
        //loadingBar = new ProgressDialog(this);
        mbar = (ProgressBar) findViewById(R.id.progressBar2);
        mbar.setVisibility(View.GONE);
        logoname = (TextView) findViewById(R.id.logo_name);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        logoname.setTypeface(face);


        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = RegisterUserName.getText().toString();
                String email = RegisterUserEmail.getText().toString();
                String password = RegisterUserPassword.getText().toString();


                RegisterAccount(name,email,password);
            }
        });

    }

    private void RegisterAccount(final String name, String email, String password) {
        if(TextUtils.isEmpty(name)){
            Toast.makeText(RegisterActivity.this,"Please enter your name",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this,"Please enter your email",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this,"Please enter your password",Toast.LENGTH_LONG).show();
        }
        else{

            /*loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("please wait,while we create account");
            loadingBar.show();*/
            mbar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        String device_token = FirebaseInstanceId.getInstance().getToken();
                        String current_user_id = mAuth.getCurrentUser().getUid();
                        storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

                        storeUserDefaultDataReference.child("user_name").setValue(name);
                        storeUserDefaultDataReference.child("user_status").setValue("Hey there, i am using Konnect app!!");
                        storeUserDefaultDataReference.child("user_image").setValue("default_profile");
                        storeUserDefaultDataReference.child("device_token").setValue(device_token);
                        storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    mbar.setVisibility(View.GONE);
                                    Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }

                            }
                        });
                    }
                    else{
                        mbar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this,"Error Occured, Try Again!!",Toast.LENGTH_LONG).show();
                    }

                    //loadingBar.dismiss();
                }
            });

        }
    }
}
