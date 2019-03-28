package com.example.jethinr.dualcamtest;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class BleControllerActivity extends AppCompatActivity implements View.OnClickListener {

    Button camera_switch;
    ImageButton recording, cam_click;
    String mode2 [] = {"Both", "Rear", "Front"};
    private BleCommService mChatService = null;
    private StringBuffer mOutStringBuffer;
    private EditText mOutEditText;
    BleCommFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_controller);
        getSupportActionBar().show();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment = new BleCommFragment();
        transaction.replace(R.id.sample_content_fragment2, fragment);
        transaction.commit();

        camera_switch = findViewById(R.id.switch_camera);
        camera_switch.setOnClickListener(this);
        cam_click = findViewById(R.id.click_cam_2);
        cam_click.setOnClickListener(this);
        recording = findViewById(R.id.imageButton);
        recording.setOnClickListener(this);
    }

    //TODO: Read the received mesg & do things accordingly.

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_camera :{
                Toast.makeText(this, "Switch Camera", Toast.LENGTH_SHORT).show();
                fragment.sendMessage("switch");
                break;
            }
            case R.id.click_cam_2 :{
                Toast.makeText(this, "Screenshot", Toast.LENGTH_SHORT).show();
                fragment.sendMessage("shot");
                break;
            }
            case R.id.imageButton :{
                Toast.makeText(this, "Record Button", Toast.LENGTH_SHORT).show();
                fragment.sendMessage("recording");
                break;
            }
        }
    }

    public void receivedMessage(String msg){
        // To notify about active Camera Texture on UI.
        switch (msg){
            case "Switched to Front" : {
                camera_switch.setText("Front");
                break;
            }
            case "Switched to Rear" : {
                camera_switch.setText("Rear");
                break;
            }
            case "Switched to Both" : {
                camera_switch.setText("Both");
                break;
            }
            case "Recording Stopped" : {
                recording.setImageResource(R.drawable.record_start_2);
                break;
            }
            case "Recording Started" : {
                recording.setImageResource(R.drawable.record_stop_2);
                break;
            }
        }
    }
}
