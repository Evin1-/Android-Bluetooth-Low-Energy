package com.loopcupcakes.examples.bluetoothleexample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import icepick.Icepick;
import icepick.State;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10 * 1000;
    private static final String TAG = "MainActivityTAG_";

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private BluetoothLeScanner mBluetoothScanner;
    private BluetoothLEAdapter mArrayAdapter;

    private ArrayList<BluetoothDevice> mBluetoothDevices;

    @Bind(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    @Bind(R.id.list_view_1)
    RecyclerView mListView;

    @Bind(R.id.text_view1)
    TextView mTextView;

    @Bind(R.id.scan_le_button)
    Button mScanLEButton;

    ViewPropertyAnimator mViewPropertyAnimator;

    public CountingIdlingResource idlingResource = new CountingIdlingResource("BlueToothLEButton");

    @State boolean bleButtonEnabled;
    @State String bleInfoText;
    @State int orientation;
    @State boolean orientationChanged;
    @State boolean isRefreshing;

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "onScanResult: " + result.toString());
            BluetoothDevice bluetoothDevice = result.getDevice();
            mArrayAdapter.notifyDataSetChanged();
            mBluetoothDevices.add(bluetoothDevice);
            updateViews(true, "Received scan result");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "onBatchScanResults: " + results.toString());
            updateViews(true, "Received batch scan results");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "onScanFailed: " + errorCode);
            updateViews(true, "Scan failed");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationChanged  = true;
        orientation = newConfig.orientation;
        init();
    }

    private void init() {
        setContentView(R.layout.activity_main);

        Fabric.with(this, new Crashlytics());

        ButterKnife.bind(this);

        mHandler = new Handler();
        mBluetoothDevices = new ArrayList<>();

        checkIfBleSupported();
        initializeBluetoothManager();
        checkIfBluetoothEnabled();

        setListView();

        setBleInfoText(bleInfoText);

        setupRefreshView();

        setupScanLEButton();

        if (!orientationChanged) {
            scanLeDevice(true);
        }
    }

    private void setListView() {
        mArrayAdapter = new BluetoothLEAdapter(mBluetoothDevices,
                new BluetoothLEAdapter.OnBluetoothItemClickHandler() {

                    @Override
                    public void onBluetoothItemClicked(int position) {
                        MainActivity.this.onBluetoothItemClicked(position);
                    }
                });
        mListView.setAdapter(mArrayAdapter);
    }

    private void scanLeDevice(final boolean enable) {
        if (!isRefreshing) {
            if (enable) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothScanner.stopScan(mLeScanCallback);
                        Toast.makeText(MainActivity.this, "Stopped scanning", Toast.LENGTH_SHORT).show();
                        updateViews(true, "No results");
                    }
                }, SCAN_PERIOD);

                mBluetoothScanner.startScan(mLeScanCallback);
                Toast.makeText(this, "Scanning for LE Bluetooth", Toast.LENGTH_SHORT).show();
                updateViews(false, "Scanning...");
            } else {
                mBluetoothScanner.startScan(mLeScanCallback);
            }
        }
    }

    private void checkIfBluetoothEnabled() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void initializeBluetoothManager() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void checkIfBleSupported() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void onBluetoothItemClicked(int position) {
        Intent intent = new Intent(this, DeviceActivity.class);

        intent.putExtra(DeviceActivity.EXTRA_DEVICE_KEY, mBluetoothDevices.get(position));

        startActivity(intent);
    }

    private void setupRefreshView() {
        mRefreshLayout.setRefreshing(isRefreshing);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainActivity.this.scanLeDevice(true);
            }
        });
    }

    private void setupScanLEButton() {
        mScanLEButton.setEnabled(bleButtonEnabled);
        mViewPropertyAnimator = mScanLEButton.animate();
        mScanLEButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleScanLEButtonClick(v, event);
            }
        });
    }

    private boolean handleScanLEButtonClick(View view, MotionEvent event) {
        long translationZDuration =
                getResources().getInteger(R.integer.custombutton_translationz_duration);
        int translationZ = getResources().getInteger(R.integer.custombutton_translationz);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mScanLEButton.setPressed(true);
            mViewPropertyAnimator
                    .setDuration(translationZDuration)
                    .translationZBy(translationZ)
                    .start();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mScanLEButton.setPressed(false);
            mViewPropertyAnimator
                    .setDuration(translationZDuration)
                    .translationZBy(-translationZ)
                    .start();
            scanLeDevice(true);
        }
        return false;
    }

    private void updateViews(boolean enabled, String status) {
        setBleInfoText(status);
        bleButtonEnabled = enabled;
        mScanLEButton.setEnabled(enabled);
        mRefreshLayout.setRefreshing(!enabled);
        if (enabled) {
            if (!idlingResource.isIdleNow()) {
                idlingResource.decrement();
            }
            isRefreshing = false;
        } else {
            idlingResource.increment();
            isRefreshing = true;
        }
    }

    private void setBleInfoText(String bleInfoText) {
        this.bleInfoText = bleInfoText;
        mTextView.setText(bleInfoText);
    }
}
