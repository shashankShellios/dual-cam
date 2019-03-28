package com.example.jethinr.dualcamtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RoleSelectionActivity extends AppCompatActivity implements View.OnClickListener {

    Button mCam, mCotroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        mCam = findViewById(R.id.btn_cam);
        mCam.setOnClickListener(this);
        mCotroller = findViewById(R.id.btn_controller);
        mCotroller.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_cam : {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.btn_controller : {
                Intent intent = new Intent(this, BleControllerActivity.class);
                startActivity(intent);
                finish();
                break;
            }
        }
    }
}
