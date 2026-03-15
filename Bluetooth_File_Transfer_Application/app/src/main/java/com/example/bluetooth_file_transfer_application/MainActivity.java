package com.example.bluetooth_file_transfer_application;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bluetooth_file_transfer_application.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private DeviceListAdapter deviceListAdapter;
    private BluetoothService bluetoothService;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    sendFile(uri);
                }
            }
    );

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            binding.tvStatus.setText("Status: Connected");
                            binding.btnSendFile.setEnabled(true);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            binding.tvStatus.setText("Status: Connecting...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            binding.tvStatus.setText("Status: Not Connected");
                            binding.btnSendFile.setEnabled(false);
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_DEVICE_NAME:
                    String deviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
                    Toast.makeText(MainActivity.this, "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString(BluetoothService.TOAST), Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_PROGRESS:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.progressBar.setProgress(msg.arg1);
                    if (msg.arg1 == 100) {
                        new Handler().postDelayed(() -> binding.progressBar.setVisibility(View.GONE), 2000);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupRecyclerView();
        bluetoothService = new BluetoothService(this, mHandler);

        binding.btnScan.setOnClickListener(v -> startDiscovery());
        binding.btnMakeDiscoverable.setOnClickListener(v -> ensureDiscoverable());
        binding.btnSendFile.setOnClickListener(v -> openFilePicker());

        checkPermissions();
    }

    private void setupRecyclerView() {
        deviceListAdapter = new DeviceListAdapter(device -> {
            bluetoothAdapter.cancelDiscovery();
            bluetoothService.connect(device);
        });
        binding.rvDevices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDevices.setAdapter(deviceListAdapter);
    }

    private void checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }

        boolean allGranted = true;
        for (String p : permissions) {
            if (ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    private void startDiscovery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return;
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        deviceListAdapter.clear();
        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private void ensureDiscoverable() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return;
        }
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }

    private void sendFile(Uri uri) {
        try {
            File file = getFileFromUri(uri);
            if (file != null) {
                bluetoothService.sendFile(file);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error preparing file", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), "temp_file_to_send");
        FileOutputStream fos = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }
        fos.close();
        is.close();
        return tempFile;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    deviceListAdapter.addDevice(device);
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                startActivity(enableIntent);
            }
        } else if (bluetoothService != null) {
            if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
                bluetoothService.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) bluetoothService.stop();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            // ignore
        }
    }
}
