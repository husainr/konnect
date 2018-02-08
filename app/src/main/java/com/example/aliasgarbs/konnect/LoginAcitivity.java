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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginAcitivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button LoginButton;
    private EditText LoginEmail;
    private EditText LoginPassword;
    //private ProgressDialog loadingBar;
    private ProgressBar mbar;

    private DatabaseReference userRef;

    private FirebaseAuth mAuth;
    private TextView logoname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_acitivity);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LoginButton = (Button) findViewById(R.id.login_button);
        LoginEmail = (EditText) findViewById(R.id.login_email);
        LoginPassword = (EditText) findViewById(R.id.login_password);
        //loadingBar = new ProgressDialog(this);
        mbar = (ProgressBar) findViewById(R.id.progressBar2);
        mbar.setVisibility(View.GONE);
        logoname = (TextView) findViewById(R.id.logo_name1);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        logoname.setTypeface(face);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = LoginEmail.getText().toString();
                String password = LoginPassword.getText().toString();

                LoginUserAccount(email,password);
            }
        });
    }

    private void LoginUserAccount(String email, String password) {
            if(TextUtils.isEmpty(email)){
                Toast.makeText(LoginAcitivity.this,"enter the email-id",Toast.LENGTH_LONG).show();
            }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(LoginAcitivity.this,"enter the password",Toast.LENGTH_LONG).show();
        }

        else{
            //loadingBar.setTitle("Login Account");
            //loadingBar.setMessage("please wait while we are verifying your credentials");
            //loadingBar.show();

            mbar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        String online_user_id = mAuth.getCurrentUser().getUid();
                        String device_token = FirebaseInstanceId.getInstance().getToken();

                        userRef.child(online_user_id).child("device_token").setValue(device_token)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mbar.setVisibility(View.GONE);
                                        Intent mainIntent = new Intent(LoginAcitivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();

                                    }
                                });



                    }
                    else{
                        mbar.setVisibility(View.GONE);
                        Toast.makeText(LoginAcitivity.this,"Please check your credentials",Toast.LENGTH_LONG).show();
                    }
                    //loadingBar.dismiss();

                }
            });

        }
    }
}
