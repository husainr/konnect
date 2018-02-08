package com.example.aliasgarbs.konnect;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartPageActivity extends AppCompatActivity {

    private Button NeedNewAccountButton;
    private Button AlreadyHaveAccountButton;
    private TextView logoname;
    private TextView slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        NeedNewAccountButton = (Button) findViewById(R.id.need_account_button);
        AlreadyHaveAccountButton = (Button) findViewById(R.id.already_have_account_button);
        slogan = (TextView) findViewById(R.id.slogan);
        logoname = (TextView) findViewById(R.id.logoname);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        slogan.setTypeface(face);
        logoname.setTypeface(face);


        NeedNewAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(StartPageActivity.this,RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        AlreadyHaveAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(StartPageActivity.this,LoginAcitivity.class);
                startActivity(loginIntent);
            }
        });

    }
}
