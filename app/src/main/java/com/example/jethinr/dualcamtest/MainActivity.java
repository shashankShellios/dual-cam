package com.example.jethinr.dualcamtest;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Picture;
import android.graphics.SurfaceTexture;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import 	android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.view.View.OnTouchListener;
import 	android.view.InputEvent;
import android.media.MediaRecorder;
import android.widget.Toast;
//import android.util.Size;
import 	java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import 	java.io.FileOutputStream;






import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.hardware.Camera.Parameters.ANTIBANDING_50HZ;
import static android.hardware.Camera.Parameters.ANTIBANDING_AUTO;
import static android.hardware.Camera.Parameters.ANTIBANDING_OFF;
import static android.hardware.Camera.Parameters.EFFECT_AQUA;
import static android.hardware.Camera.Parameters.EFFECT_MONO;
import static android.hardware.Camera.Parameters.EFFECT_NEGATIVE;
import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_FIXED;
import static android.hardware.Camera.Parameters.WHITE_BALANCE_AUTO;
import static android.hardware.Camera.Parameters.WHITE_BALANCE_INCANDESCENT;
import static android.hardware.Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT;
import static android.os.SystemClock.sleep;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity  extends AppCompatActivity  implements View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, BluetoothAdapter.LeScanCallback {

    /** BLE Section **/
    private static final String TAG = "MainActivity";

    private static final String DEVICE_NAME1 = "SHELLIOS-HBR";
    private static final String DEVICE_NAME = "RDL51822";
    private static final String DEVICE_NAME2 = "";

    /* Beacon Service */ //TODO: for notify
    private static final UUID HUMIDITY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private static final UUID HUMIDITY_DATA_CHAR = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private static final UUID HUMIDITY_CONFIG_CHAR = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    /* Barometric Pressure Service */
    private static final UUID PRESSURE_SERVICE = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private static final UUID PRESSURE_DATA_CHAR = UUID.fromString("00002A00-0000-1000-8000-00805f9b34fb");
    private static final UUID PRESSURE_CONFIG_CHAR = UUID.fromString("00002A00-0000-1000-8000-00805f9b34fb");
    private static final UUID PRESSURE_CAL_CHAR = UUID.fromString("00002A00-0000-1000-8000-00805f9b34fb");
    /* Client Configuration Descriptor */
    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private BluetoothAdapter mBluetoothAdapter;
    private SparseArray<BluetoothDevice> mDevices;

    private BluetoothGatt mConnectedGatt;

    private TextView mConnected, mCommands ;

    private ProgressDialog mProgress;

    /** Cam section **/
    public static final int CAMERA_MODE_BOTH = 0;
    public static final int CAMERA_MODE_REAR = 1;
    public static final int CAMERA_MODE_FRONT = 2;
    public static final int CAMERA_MODE_BOTH_RESET = 3;
    public static final int CAMERA_MODE_FRONT_RESET = 4;
    public static final int CAMERA_MODE_RESET = 5;

    public static final int BACK_CAM = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static final int FRONT_CAM = Camera.CameraInfo.CAMERA_FACING_FRONT;

    List focus_mode;

    TextureView[] mTextureView = new TextureView[2];
    int surfaceCounter = 0;
    SurfaceTexture[] Surface = new SurfaceTexture[2];

    private Camera[] mCamera = new Camera[2];/*BACK_CAM = 0, FRONT_CAM = 1 */
    int camid[] = {0,1};
    int CameraModeIdx = 0;

    int ev_max = 0;
    int ev = 0;
    int ev_min = 0;
    int antibanding_count = 0;
    int whitebalance_count = 0;

    private  Context context;
    private PictureCallback[] mPicture = new PictureCallback[2];

    Button camera_switch, anti_banding;
    Button EVn, EVp;
    ImageButton recordButton, wb;
    Button Text;
    TextView cam_name [] = new TextView[2];

    private byte[] callbackBuffer;

    public MediaRecorder[] mrec = new MediaRecorder[2];

    String CameraMode[] = {"Both","Rear","Front"};
    String antibanding_values[] = {"auto","50hz","60hz","off"};
    String whitebalance_values[] = {"auto","cloudy-daylight","daylight","fluorescent","incandescent","shade"};
//    BleCommFragment fragment;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * This is the first callback and called when the activity is first created.
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("");
        setSupportProgressBarIndeterminateVisibility(true);

        mConnected = findViewById(R.id.connected);
        mConnected.setText("Disconnected");
        mCommands = findViewById(R.id.logView);

       // if (savedInstanceState == null) {
            /** Disabled Bluetooth Classic Control Fragment **/
            /*FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment = new BleCommFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();*/
       // }

       /** BluetoothLE Section  **/
        /*
         * Bluetooth in Android 4.3 is accessed via the BluetoothManager, rather than
         * the old static BluetoothAdapter.getInstance()
         */
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mDevices = new SparseArray<BluetoothDevice>();

        /*
         * A progress dialog will be needed while the connection process is
         * taking place
         */
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);

        /** Cam Section **/

        ImageButton captureButton = findViewById(R.id.imageButton);
        captureButton.setOnClickListener(this);
        recordButton = findViewById(R.id.videorec);
        recordButton.setOnClickListener(this);
        camera_switch = findViewById(R.id.switch_camera);
        camera_switch.setOnClickListener(this);
        cam_name [0] = findViewById(R.id.tv_rear);
        cam_name [1] = findViewById(R.id.tv_front);
        CameraModeIdx = CAMERA_MODE_RESET;

     /*   wb = (ImageButton) findViewById(R.id.wb);
        wb.setOnClickListener(this);
        anti_banding = (Button) findViewById(R.id.antiBanding);
        anti_banding.setOnClickListener(this);
        EVp = (Button) findViewById(R.id.EVp);
        EVp.setOnClickListener(this);
        EVn = (Button) findViewById(R.id.EVn);
        EVn.setOnClickListener(this);
        Text = (Button) findViewById(R.id.EV);
        Text.setOnClickListener(this);  */
    }
    /***
     * BluetoothLowEnergy section starts from here
     ***/

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private void startScan() {
        mBluetoothAdapter.startLeScan(this);
        setProgressBarIndeterminateVisibility(true);

        mHandler.postDelayed(mStopRunnable, 2500);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
        /*
         * We are looking for Specific devices only, so validate the name
         * that each device reports before adding it to our collection
         */
        if ( DEVICE_NAME.equals(device.getName()) || DEVICE_NAME1.equals(device.getName()) || true) {//todo: Device Filtering
            mDevices.put(device.hashCode(), device);
            //Update the overflow menu
            invalidateOptionsMenu();
        }
    }

    /*
     * In this callback, we've created a bit of a state machine to enforce that only
     * one characteristic be read or written at a time until all of our sensors
     * are enabled and we are registered to get notifications.
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /* State Machine Tracking */
        private int mState = 0;

        private void reset() { mState = 0; }

        private void advance() { mState++; }

        /*
         * Send an enable command to each sensor by writing a configuration
         * characteristic.  This is specific to the SensorTag to keep power
         * low by disabling sensors you aren't using.
         */
        private void enableNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Enabling pressure cal");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CONFIG_CHAR);
                    characteristic.setValue("Shellios");
                    break;
                /*case 1:
                    Log.d(TAG, "Enabling pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x01});
                    break;
                case 2:
                    Log.d(TAG, "Enabling humidity");
                    characteristic = gatt.getService(HUMIDITY_SERVICE)

                            .getCharacteristic(HUMIDITY_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x01});
                    break;*/
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            gatt.writeCharacteristic(characteristic);
        }

        /*
         * Read the data characteristic's value for each sensor explicitly
         */
        private void readNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Reading pressure cal");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CAL_CHAR);
                    break;
                /*case 1:
                    Log.d(TAG, "Reading pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_DATA_CHAR);
                    break;
                case 2:
                    Log.d(TAG, "Reading humidity");
                    characteristic = gatt.getService(HUMIDITY_SERVICE)
                            .getCharacteristic(HUMIDITY_DATA_CHAR);
                    break;*/
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            gatt.readCharacteristic(characteristic);

        }

        /*
         * Enable notification of changes on the data characteristic for each sensor
         * by writing the ENABLE_NOTIFICATION_VALUE flag to that characteristic's
         * configuration descriptor.
         */
        private void setNotifyNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                /*case 0:
                    Log.d(TAG, "Set notify pressure cal");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CAL_CHAR);
                    break;
                case 1:
                    Log.d(TAG, "Set notify pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_DATA_CHAR);
                    break;*/
                case 0:
                    Log.d(TAG, "Set notify humidity");
                    characteristic = gatt.getService(HUMIDITY_SERVICE)
                            .getCharacteristic(HUMIDITY_DATA_CHAR);
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Notify Sensors Enabled");
                    return;
            }

            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);
            //Enabled remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: "+status+" -> "+connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
                gatt.discoverServices();
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
                mHandler.sendEmptyMessage(MSG_CLEAR);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
                gatt.disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services Discovered: "+status);
            mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Fetching Commands..."));
            /*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             */
            reset();
//            enableNextSensor(gatt);//TODO: After the services get discovered, perform your read,write,notify call from here
            readNextSensor(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //For each read, pass the data up to the UI thread to update the display
            if (HUMIDITY_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
            }
            if (PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE, characteristic));
            }
            /*if (PRESSURE_CAL_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE_CAL, characteristic));
            }*/

            //After reading the initial value, next we enable notifications
            setNotifyNextSensor(gatt);//TODO: changed here
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //After writing the enable flag, next we read the initial value
            readNextSensor(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            /*
             * After notifications are enabled, all updates from the device on characteristic
             * value changes will be posted here.  Similar to read, we hand these up to the
             * UI thread to update the display.
             */
            if (HUMIDITY_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
            }
            /*if (PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE, characteristic));
            }
            if (PRESSURE_CAL_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE_CAL, characteristic));
            }*/
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            Log.e("Shellios", "Descriptor Written");
            advance();
            enableNextSensor(gatt);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "Remote RSSI: "+rssi);
        }

        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }
    };

    /*
     * We have a Handler to process event results on the main thread
     */
    private static final int MSG_HUMIDITY = 101;
    private static final int MSG_PRESSURE = 102;
    private static final int MSG_PRESSURE_CAL = 103;
    private static final int MSG_PROGRESS = 201;
    private static final int MSG_DISMISS = 202;
    private static final int MSG_CLEAR = 301;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BluetoothGattCharacteristic characteristic;
            switch (msg.what) {
                case MSG_HUMIDITY:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining humidity value");
                        return;
                    }
                    updateHumidityValues(characteristic);
                    break;
                case MSG_PRESSURE:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining pressure value");
                        return;
                    }
                    updatePressureValue(characteristic);
                    break;
                case MSG_PRESSURE_CAL:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining cal value");
                        return;
                    }
                    updatePressureCals(characteristic);
                    break;
                case MSG_PROGRESS:
                    mProgress.setMessage((String) msg.obj);
                    if (!mProgress.isShowing()) {
                        mProgress.show();
                    }
                    break;
                case MSG_DISMISS:
                    mProgress.hide();
                    break;
                case MSG_CLEAR:
                    //clearDisplayValues();
                    break;
            }
        }
    };

    /* Methods to extract sensor data and update the UI */

    private void updateHumidityValues(BluetoothGattCharacteristic characteristic) {
        String valueNotify = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0).toString();
        mCommands.setText(valueNotify);
        Log.e("Shellios Notify", valueNotify);
    }
    private void updatePressureCals(BluetoothGattCharacteristic characteristic) {
        //mPressureCals = SensorTagData.extractCalibrationCoefficients(characteristic);
        Log.e("Shellios", "Pr Calc");
    }

    private void updatePressureValue(BluetoothGattCharacteristic characteristic) {
        /*if (mPressureCals == null) return;
        double pressure = SensorTagData.extractBarometer(characteristic, mPressureCals);
        double temp = SensorTagData.extractBarTemperature(characteristic, mPressureCals);*/

        String valueName1 = characteristic.getStringValue(0);
        Log.e("Shellios Name",valueName1);

        mConnected.setText("Connected to " + valueName1);
        mProgress.dismiss();
    }


    //TODO: Cam Section
    static boolean is_recording = false;

    public void videoRec(){
        if(!is_recording){
            is_recording = true;
            StartRecorder();
        } else {
            StopRecorder();
        }
    }
    public void mPicture(){
        takePicture(CameraModeIdx);
    }


    /**
     * Called when a view has been clicked. It reads the value from the activity_main.xml
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
          /*   case R.id.EVp: {
                 IncreaseExposure();
                 break;
            }
            case R.id.EVn: {
                DecreaseExposure();
                break;
            }   */
            case R.id.videorec: {
                videoRec();
                break;
            }
            case R.id.imageButton: {
                mPicture();
                    break;
            }
            case R.id.switch_camera: {
                SwitchtoNextCameraMode();
                break;
            }
         /*   case R.id.antiBanding: {
                SwitchAntibanding();
                break;
            }
            case R.id.wb: {
                SwitchWhiteBalance();
                break;
            }   */
        }
    }

    /**
     * onStart() is called when the activity becomes visible to the user.
     */

    @Override
    protected void onStart(){
        super.onStart();


        surfaceCounter = 0;
        mTextureView[1] = findViewById(R.id.textureView_rear);
        mTextureView[1].setSurfaceTextureListener(mSurfaceTextureListenerRear);
        mTextureView[0] = findViewById(R.id.textureView_front);
        mTextureView[0].setSurfaceTextureListener(mSurfaceTextureListenerFront);
    }

    /**
     * onResume() called when the user starts interacting with the application.
     */
    @Override
    protected void onResume() {
        super.onResume();

        /** BluetoothLE Section  **/
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /** Cam Section  **/
        switch (CameraModeIdx) {
            case CAMERA_MODE_BOTH:
                startCameraMode(CAMERA_MODE_BOTH_RESET);
                break;
            case CAMERA_MODE_RESET:
                CameraModeIdx = CAMERA_MODE_BOTH;
                break;
                default:
                    startCameraMode(CameraModeIdx);
                    break;
        }
    }

    /**
     * onPause() is called when the current activity is being paused
     */
    protected void onPause() {
        switch (CameraModeIdx) {
            case CAMERA_MODE_FRONT:
                stopCamera(CAMERA_MODE_FRONT_RESET);
                break;
              //  default:stopCamera(CameraModeIdx);
              //  break;
        }
        if (mCamera[camid[0]] != null) {
            mCamera[camid[0]].release();
            mCamera[camid[0]] = null;
        }
        if (mCamera[camid[1]] != null) {
            mCamera[camid[1]].release();
            mCamera[camid[1]] = null;
        }
        super.onPause();

        /** BluetoothLE Section **/
        //Make sure dialog is hidden
        mProgress.dismiss();
        //Cancel any scans in progress
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Disconnect from any active tag connection
        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the "scan" option to the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Add any device elements we've discovered to the overflow menu
        for (int i=0; i < mDevices.size(); i++) {
            BluetoothDevice device = mDevices.valueAt(i);
            menu.add(0, mDevices.keyAt(i), 0, device.getName());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                mDevices.clear();
                startScan();
                return true;
            default:
                //Obtain the discovered device to connect with
                BluetoothDevice device = mDevices.get(item.getItemId());
                Log.i(TAG, "Connecting to "+device.getName());
                /*
                 * Make a connection with the device using the special LE-specific
                 * connectGatt() method, passing in a callback for GATT events
                 */
                mConnectedGatt = device.connectGatt(this, false, mGattCallback);
                //Display progress UI
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to "+device.getName()+"..."));
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     *This listener can be used to be notified when the surface texture associated with this texture view is available.
     */

    private final TextureView.SurfaceTextureListener mSurfaceTextureListenerFront
            = new TextureView.SurfaceTextureListener() {

        int idx = 0;

        /**
         * Invoked when a TextureView's SurfaceTexture is ready for use.
         * @param surface
         * @param width
         * @param height
         */
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            /*Increase the counter and store information to an array*/
            /*Trigger Start Camera once all surface available by checking the surface counter*/
            Surface[FRONT_CAM] = surface;
            surfaceCounter++;
            if (surfaceCounter >= 2) { //TODO: change here done
                initializeCamera(BACK_CAM,90,90);
                initializeCamera(FRONT_CAM,90,270);//TODO: orientation 90 -> 270
            }


        }

        /**
         * Invoked when the SurfaceTexture's buffers size changed.
         * @param texture
         * @param width
         * @param height
         */
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            // Ignored, Camera does all the work for us
        }

        /**
         * Invoked when the specified SurfaceTexture is about to be destroyed.
         * @param surface
         * @return
         */
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mCamera[FRONT_CAM].stopPreview();
            mCamera[FRONT_CAM].release();
            return true;
        }

        /**
         *Invoked when the specified SurfaceTexture is updated through SurfaceTexture.updateTexImage().
         * @param surface
         */
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // Invoked every time there's a new Camera preview frame
        }

    };

    /**
     *This listener can be used to be notified when the surface texture associated with this texture view is available.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListenerRear
            = new TextureView.SurfaceTextureListener() {

        int idx = 0;

        /**
         * Invoked when a TextureView's SurfaceTexture is ready for use.
         * @param surface
         * @param width
         * @param height
         */

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            /*Increase the counter and store information to an array*/
            /*Trigger Start Camera once all surface available by checking the surface counter*/
            Surface[BACK_CAM] = surface;
            surfaceCounter++;
            if (surfaceCounter >= 2) { //TODO: change here (orientation adjustment)
                initializeCamera(BACK_CAM,90,90);
                initializeCamera(FRONT_CAM,90,270);
            }
        }

        /**
         * Invoked when the SurfaceTexture's buffers size changed.
         * @param texture
         * @param width
         * @param height
         */
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            // Ignored, Camera does all the work for us
        }

        /**
         * Invoked when the specified SurfaceTexture is about to be destroyed.
         * @param surface
         * @return
         */
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            // stopPreview();
            mCamera[BACK_CAM].stopPreview();
            mCamera[BACK_CAM].release();
            return true;
        }

        /**
         *Invoked when the specified SurfaceTexture is updated through SurfaceTexture.updateTexImage().
         * @param surface
         */
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // Invoked every time there's a new Camera preview frame
        }

    };

    /**
     * Initialize and Start the Camera based on the CamIdz
     * @param CamIdx
     * @param orientation
     * @param rotation
     * @return
     */
    public long initializeCamera(int CamIdx,int orientation, int rotation) {


        //setContentView(R.layout.activity_camera);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
        while(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED) {

        }
        mCamera[CamIdx] = Camera.open(CamIdx);
        mPicture[CamIdx] = getPictureCallback();
        Camera.Size result = null;

        try {
            Camera.Parameters param = mCamera[CamIdx].getParameters();
            List sizes = param.getSupportedPictureSizes();
            focus_mode = param.getSupportedFocusModes();
            result = (Size) sizes.get(0);
            param.setRotation(rotation);
            param.setPreviewSize(320,240);
            String str = mCamera[CamIdx].getParameters().flatten();
            Log.e("DualCam", str);
            ev_max = param.getMaxExposureCompensation();
            ev_min = param.getMinExposureCompensation();
            param.setAntibanding("auto");
            param.setPictureSize(result.width,result.height);
            if(focus_mode.contains("FOCUS_MODE_CONTINUOUS_PICTURE")) //TODO: changes here done
                param.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera[CamIdx].setParameters(param);
            /**Preview Callback for YUV Dump*/
            int dataBufferSize=(int)(320*240*
                    (ImageFormat.getBitsPerPixel(param.getPreviewFormat())/8.0));

            for(int i=0; i<10;i++) {
                callbackBuffer =new byte[dataBufferSize];
                mCamera[CamIdx].addCallbackBuffer(callbackBuffer);
            }
            mCamera[CamIdx].setPreviewCallbackWithBuffer(previewCallback);
            /*Code for Yuv Dump ends here */

            mCamera[CamIdx].setDisplayOrientation(orientation);
            mCamera[CamIdx].setPreviewTexture(Surface[CamIdx]);
            mCamera[CamIdx].startPreview();
            } catch (IOException ioe) {
            // Something bad happened
        }

        return 0;
    }

    /**
     * Returns the First Camera That should  to be controlled
     * @return
     */
    public int ControlStateMachineinSwitchingCamera()
    {
        /*This return info of first camera to be controlled*/
        return BACK_CAM;
    }

    /**
     * Starts the  Video Recording
     * @param CamIdx
     * @param path
     * @param filename
     */
        private void StartRecordeing(int CamIdx, File path, String filename){
            mrec[CamIdx] = new MediaRecorder();


            mCamera[CamIdx].lock();
            mCamera[CamIdx].unlock();


            // Please maintain sequence of following code.

            // If you change sequence it will not work.
            mrec[CamIdx].setCamera(mCamera[CamIdx]);
            mrec[CamIdx].setVideoSource(MediaRecorder.VideoSource.CAMERA);
            if(CamIdx == 0)
                mrec[CamIdx].setOrientationHint(90);
            else
                mrec[CamIdx].setOrientationHint(270);
            //  mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
            mrec[CamIdx].setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mrec[CamIdx].setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            //     mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {

                mrec[CamIdx].setOutputFile(path + filename);
                mrec[CamIdx].prepare();
                mrec[CamIdx].start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    /**
     * Fucntion that sets the name and location for the recorded video file
     * @param CamIdx
     */
    private  void videoRecorder(int CamIdx) {
            String filename;
            File path;
            path = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

            Date date0 = new Date();
            if(CamIdx == 0)
                filename = "/Cam" + date0.toString().replace(" ", "_").replace(":", "_") + "Rear"+".mp4";
            else
                filename = "/Cam" + date0.toString().replace(" ", "_").replace(":", "_") + "Front"+".mp4";

            //create empty file it must use
            File file = new File(path, filename);

            StartRecordeing(CamIdx,path,filename);

        }

    /**
     * Calls the video recorder based on the Current mode of operation
     * @return
     */
    public long StartRecorder(){ //TODO: Changes Here Done
       /* if(CameraModeIdx != CAMERA_MODE_FRONT) {
            Camera.Parameters params = mCamera[BACK_CAM].getParameters();
            params.setFocusMode(FOCUS_MODE_CONTINUOUS_VIDEO);
            mCamera[BACK_CAM].setParameters(params);
        }*/
        recordButton.setImageResource(R.drawable.record_stop_2); //To update record symbol to Stop Symbol
        //fragment.sendMessage("Recording Started");
        switch(CameraModeIdx) {
            case CAMERA_MODE_BOTH:
                videoRecorder(BACK_CAM);
                videoRecorder(FRONT_CAM);
                break;
            case CAMERA_MODE_REAR:
                videoRecorder(BACK_CAM);
                break;
            case CAMERA_MODE_FRONT:
                videoRecorder(FRONT_CAM);
                break;
        }
        return 0;
    }

    /**
     *Stops the Video Recording
     * @param CamIdx
     * @return
     */
    public long stopRecording(int CamIdx)
    {
        if (mrec[CamIdx] != null) {
            mrec[CamIdx].stop();
            mrec[CamIdx].release();
        }
        return 0;
    }

    /**
     * Chooses the Camera in which recording need to be stopped
     * @return
     */
    public  long StopRecorder(){
        recordButton.setImageResource(R.drawable.record_start_2); //To update Stop symbol to record Symbol
        //fragment.sendMessage("Recording Stopped");
        is_recording = false;
        switch(CameraModeIdx) {
            case CAMERA_MODE_BOTH:
                stopRecording(BACK_CAM);
                stopRecording(FRONT_CAM);
                break;
            case CAMERA_MODE_REAR:
                stopRecording(BACK_CAM);
                break;
            case CAMERA_MODE_FRONT:
                stopRecording(FRONT_CAM);
                break;
        }
      /*  if(CameraModeIdx != CAMERA_MODE_FRONT) { //TODO: Changes Here Done
            Camera.Parameters params = mCamera[BACK_CAM].getParameters();
            params.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera[BACK_CAM].setParameters(params);
        } */
        return 0;
    }

    /**
     * Set the Directory for Storing the Image
     * @return
     */

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "CameraAPIDemo");
    }

    /**
     * Storing the YUV image in /storage/emulated/0/Pictures/CameraAPIDemo/
     * @param data
     * @param name
     * @return
     */
    public long storeYUVImage(byte[] data, String name)
    {
        File pictureFileDir = getDir();
        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(TAG, "Can't create directory to save image."); // Define proper tag "DUALCAM"
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return -1;

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());

        /*removing date to duplicate YUV file names*/
        String photoFile = "Picture_"+ name + /* date +*/ ".yuv";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        //Use this function here to write writeFileToStorage()

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (Exception error) {
            Log.d(TAG, "File" + filename + "not saved: "
                    + error.getMessage());
        }

        return 0;
    }

    /**
     * Preview Call Back Function and stores the YUV data of every 50th frame
     */
    private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        int counter = 0;
        @Override public void onPreviewFrame(byte[] data, Camera camera) {
            counter++;
            if (counter == 50) {
                counter = 0;
                if(camera == mCamera[BACK_CAM])
                    storeYUVImage(data,"Rear");
                else
                    storeYUVImage(data,"Front");
            }
            camera.addCallbackBuffer(data);
        }
    };

    /**
     * Stores the JPEG image in the given location
     * @param data
     * @param numBytes
     * @param name
     * @return
     */
    public long storeJPEGImage(byte[] data, long numBytes, String name)
    {
        File pictureFileDir = getDir();
        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(TAG, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return -1;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());

        String photoFile = "Picture_"+ name +  date  +".jpg";
        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        //Use this function here to write writeFileToStorage()

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(getApplicationContext(), "New Image saved:" + filename, Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Log.d(TAG, "File" + filename + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
        return 0;
    }

    /**
     * Snapshot Capture Call back function
     * @return
     */
    private PictureCallback getPictureCallback() {
        this.context = context;
        PictureCallback picture = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //make a new picture file
                String LensPosition = "Front";
                if (camera == mCamera[BACK_CAM]) {
                    LensPosition = "Rear";
                }
                storeJPEGImage(data, data.length, LensPosition);
                camera.startPreview();
            }
        };
        return picture;
    }

    /**
     * Initiate  take picture call back based on the Mode
     * @param mode
     * @return
     */
    public long takePicture(int mode)
    {
        switch (mode) {
            case CAMERA_MODE_BOTH: {
                mCamera[BACK_CAM].takePicture(null, null, mPicture[0]);
                mCamera[FRONT_CAM].takePicture(null, null, mPicture[1]);
                break;
            }
            case CAMERA_MODE_REAR: {
                mCamera[BACK_CAM].takePicture(null, null, mPicture[0]);
                break;
            }
            case CAMERA_MODE_FRONT: {
                mCamera[FRONT_CAM].takePicture(null, null, mPicture[1]);
                break;
            }
        }
        return 0;
    }

    /**
     * Stops the Preview from the camera
     * @param camera
     * @return
     */
    public long stopPreview(Camera camera)
    {
        camera.stopPreview();
        camera.release();
        return 0;
    }

    /**
     * Stops the Camera Based on the mode of operation
     * @param option
     * @return
     */
    public long stopCamera(int option)
    {
        /*In Recording*/
        if(is_recording)
            StopRecorder();

        switch (option)
        {
            case CAMERA_MODE_BOTH:
                int CamIdx = ControlStateMachineinSwitchingCamera();
                /*Stop Rear and Front Camera*/
                stopPreview(mCamera[CamIdx]);
                CamIdx = CamIdx == BACK_CAM? FRONT_CAM:BACK_CAM;
                stopPreview(mCamera[CamIdx]);
                break;
            case CAMERA_MODE_REAR:

                /*Stop Rear Camera*/
                stopPreview(mCamera[BACK_CAM]);
                break;
            case CAMERA_MODE_FRONT:
                /*Do nothing */
                  break;
            case CAMERA_MODE_FRONT_RESET:
                stopPreview(mCamera[FRONT_CAM]);
                break;
        }
        //CameraModeIdx = CAMERA_MODE_INVALID;
        return 0;
    }

    /**
     * Starts the Camera with the provided parameters
     * @param CamIdx
     * @param Surface
     * @param orientation
     * @param rotation
     * @return
     */
    public long startCamera(int CamIdx,SurfaceTexture Surface ,int orientation,int rotation ) {
        /*Start Camera*/

        Camera camera = Camera.open(CamIdx);
        try {
            Camera.Parameters params = camera.getParameters();
            params.setRotation(rotation);

            camera.setDisplayOrientation(orientation);
            camera.setPreviewTexture(Surface);
            if(focus_mode.contains("FOCUS_MODE_CONTINUOUS_PICTURE")) //TODO: changes here done
                params.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);

        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        mCamera[CamIdx] = camera;


        return 0;
    }

    /**
     * Selects the startCamera based on the mode
     * @param cameraMode
     * @return
     */
    public long startCameraMode(int cameraMode)
    {
        /*Start Camera*/
        switch (cameraMode) {
            case CAMERA_MODE_BOTH:
                /*Start Rear Camera*/
                startCamera(BACK_CAM,Surface[BACK_CAM],90,90);
                break;
            case CAMERA_MODE_REAR:
                startCamera(BACK_CAM,Surface[BACK_CAM],90,90);
                break;
            case CAMERA_MODE_FRONT:
                // startCamera(BACK_CAM,Surface[BACK_CAM],90,90);
                startCamera(FRONT_CAM,Surface[FRONT_CAM],270,270);//TODO: orientation 90 -> 270
                startCamera(FRONT_CAM,Surface[FRONT_CAM],270,270);//TODO: orientation 90 -> 270
                break;
            case CAMERA_MODE_BOTH_RESET:
                startCamera(BACK_CAM,Surface[BACK_CAM],90,90);
                startCamera(FRONT_CAM,Surface[FRONT_CAM],90,270);
        }
        return 0;
    }

    /*-----Comments-----*/

    /**
     * This function is used to select next mode of operation
     * @return
     */
    public long SwitchtoNextCameraMode()
    {
        stopCamera(CameraModeIdx);
        int mode = CameraModeIdx;
        if(++mode >=3) {
            mode =0;
        }
        camera_switch.setText(CameraMode[mode]);
        startCameraMode(mode);
        CameraModeIdx = mode;

        // To notify about active Camera Texture on UI.
        switch (CameraModeIdx){
            case 0 : {      //Both
                cam_name[0].setVisibility(View.VISIBLE);
                cam_name[1].setVisibility(View.VISIBLE);
                //fragment.sendMessage("Switched to Both");
                break;
            }
            case 1 : {      //Rear
                cam_name[0].setVisibility(View.VISIBLE);
                cam_name[1].setVisibility(View.INVISIBLE);
                //fragment.sendMessage("Switched to Rear");
                break;
            }
            case 2 : {      //Front
                cam_name[0].setVisibility(View.INVISIBLE);
                cam_name[1].setVisibility(View.VISIBLE);
                //fragment.sendMessage("Switched to Front");
                break;
            }
        }
        return CameraModeIdx;
    }

    /**
     * Set the given Anti Banding Mode on the given camera
     * @param camera
     * @param antiBanding_value
     * @return
     */
    public long setAntibanding(Camera camera,String antiBanding_value)
    {
        Camera.Parameters params = camera.getParameters();
        params.setAntibanding(antiBanding_value);
        camera.setParameters(params);
        return 0;
    }

    /**
     * Switch Between Anti Banding modes
     * @return
     */
    public long SwitchAntibanding() {
        antibanding_count++;
        if (antibanding_count > 3) {
            antibanding_count = 0;
        }
        anti_banding.setText(antibanding_values[antibanding_count]);
        String abVal = antibanding_values[antibanding_count];
        switch (CameraModeIdx) {
            case CAMERA_MODE_BOTH:
                setAntibanding(mCamera[BACK_CAM], abVal);
                setAntibanding(mCamera[FRONT_CAM], abVal);
                break;
            case CAMERA_MODE_REAR:
                setAntibanding(mCamera[BACK_CAM], abVal);
                break;
            case CAMERA_MODE_FRONT:
                setAntibanding(mCamera[FRONT_CAM], abVal);
                break;
        }
        return 0;
    }

    /**
     * Set the given White Balance  on the given camera
     * @param camera
     * @param whiteBalance
     * @return
     */
    public long setWhiteBalance(Camera camera, String whiteBalance)
    {
        Camera.Parameters params = camera.getParameters();
        params.setWhiteBalance(whiteBalance);
        camera.setParameters(params);
        return 0;
    }

    /**
     * Switch between White Balance modes
     * @return
     */
    public long SwitchWhiteBalance() {
        whitebalance_count++;
        if (whitebalance_count > 5) {
            whitebalance_count = 0;
        }
        String wbVal = whitebalance_values[whitebalance_count];
        switch (CameraModeIdx) {
            case CAMERA_MODE_BOTH:
                setWhiteBalance(mCamera[BACK_CAM], wbVal);
                setWhiteBalance(mCamera[FRONT_CAM], wbVal);
                break;
            case CAMERA_MODE_REAR:
                setWhiteBalance(mCamera[BACK_CAM], wbVal);
                break;
            case CAMERA_MODE_FRONT:
                setWhiteBalance(mCamera[FRONT_CAM], wbVal);
                break;
        }
        switch (whitebalance_count) { //Use proper name for wb_c
            case 0: wb.setImageResource(R.mipmap.auto); break;
            case 1: wb.setImageResource(R.mipmap.cloudy); break;
            case 2: wb.setImageResource(R.mipmap.sun); break;
            case 3: wb.setImageResource(R.mipmap.flu); break;
            case 4: wb.setImageResource(R.mipmap.tungstun); break;
            case 5: wb.setImageResource(R.mipmap.shade); break;
        }
        return 0;
    }

    /**
     * Sets the Exposure compensation Value on given camera.
     * @param camera
     * @param value
     * @return
     */
    public long setExposure(Camera camera, int value)
    {
        Camera.Parameters params = camera.getParameters();
        params.setExposureCompensation(value);
        camera.setParameters(params);
        return 0;
    }

    /**
     * Set Exposure Compensation Values based on the mode
     * @param evVal
     * @return
     */
    public long SetExposureCompensation(int evVal) {

          switch (CameraModeIdx) {
            case CAMERA_MODE_BOTH:
                setExposure(mCamera[BACK_CAM], evVal);
                setExposure(mCamera[FRONT_CAM], evVal);
                break;
            case CAMERA_MODE_REAR:
                setExposure(mCamera[BACK_CAM], evVal);
                break;
            case CAMERA_MODE_FRONT:
                setExposure(mCamera[FRONT_CAM], evVal);
                break;
        }
        return 0;
    }

    /**
     * Increases the Exposure Compensation value.
     * @return
     */
    public  long IncreaseExposure(){

        EVn.setText("-");

        ev++;
        if(ev < ev_max) {
            SetExposureCompensation(ev);
        }else {
            ev = ev_max;
            EVp.setText("X");
        }
        Text.setText("EV"+String.valueOf(ev));
        return 0;
    }
    /**
     * Decreases the Exposure Compensation value.
     * @return
     */
    public long DecreaseExposure(){
        EVp.setText("+");
        ev--;
        if(ev > ev_min) {
            SetExposureCompensation(ev);
        }else {
            ev = ev_min;
            EVn.setText("X");
        }
        Text.setText("EV"+String.valueOf(ev));
        return 0;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
