package com.example.hirou;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    public static final String ACTION_GATT_CONNECTED = "com.example.hirou.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.example.hirou.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_DATA_AVAILABLE = "com.example.hirou.ACTION_DATA_AVAILABLE";

    public static String receivedMessage = "";
    public static Double readValue = 0d;
    private final IBinder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private Handler handler = new Handler();
    private boolean isScanning = false;
    private static final long SCAN_PERIOD = 10000;
    private static final String TARGET_MAC_ADDRESS = "20:33:91:BA:82:3F"; // Your Arduino Nano BLE MAC address, 76:05:96:D0:E9:3E, 1E:41:CF:5C:6F:4E, 20:33:91:BA:82:3F
    private static final UUID MY_SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_CHARACTERISTIC_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothGattCharacteristic characteristic;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            stopSelf();
            return;
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
            stopSelf();
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // Check if BluetoothLeScanner is initialized
        if (bluetoothLeScanner == null) {
            Log.e(TAG, "BluetoothLeScanner is not initialized");
            stopSelf();
            return;
        }

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        scanLeDevice(true, scanSettings);
    }

    @SuppressLint("MissingPermission")
    private void scanLeDevice(final boolean enable, ScanSettings scanSettings) {
        if (enable) {
            handler.postDelayed(() -> {
                isScanning = false;
                if (bluetoothLeScanner != null) {
                    bluetoothLeScanner.stopScan(leScanCallback);
                    Log.d(TAG, "Stopped scanning");
                }
            }, SCAN_PERIOD);

            isScanning = true;
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.startScan(new ArrayList<>(), scanSettings, leScanCallback);
                Log.d(TAG, "Started scanning");
            } else {
                Log.e(TAG, "BluetoothLeScanner is null, cannot start scan");
            }
        } else {
            isScanning = false;
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(leScanCallback);
                Log.d(TAG, "Stopped scanning");
            } else {
                Log.e(TAG, "BluetoothLeScanner is null, cannot stop scan");
            }
        }
    }

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();

            if (TARGET_MAC_ADDRESS.equals(device.getAddress())) {
                connectToDevice(device);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        Log.d(TAG, "Connecting to device: " + device.getAddress());
        if (ContextCompat.checkSelfPermission(BluetoothService.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetoothGatt = device.connectGatt(BluetoothService.this, false, gattCallback);
        } else {
            showToast("연결중입니다 ... ");
        }
    }

    public boolean isCharacteristicInitialized() {
        return (bluetoothGatt != null && characteristic != null);
    }

    public void sendData(String data) {
        if (bluetoothGatt != null && characteristic != null) {
            characteristic.setValue(data.getBytes(StandardCharsets.UTF_8));
            boolean success = bluetoothGatt.writeCharacteristic(characteristic);
            if (success) {
                Log.d(TAG, "Write initiated successfully.");
            } else {
                Log.e(TAG, "Write initiation failed.");
            }
        } else {
            showToast("Device not connected");
            Log.e(TAG, "sendData() failed: bluetoothGatt or characteristic is null");
        }
    }

    public void readData() {
        Log.d(TAG, "readData()");
        if (bluetoothGatt != null && characteristic != null) {
            Log.d(TAG, "readData() if");
            bluetoothGatt.readCharacteristic(characteristic);
        }
    }

    public final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "onConnectionStateChange");
                gatt.discoverServices();
                broadcastUpdate(ACTION_GATT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(MY_SERVICE_UUID);
                if (service != null) {
                    characteristic = service.getCharacteristic(MY_CHARACTERISTIC_UUID);
                    if (characteristic != null) {
                        Log.d(TAG, "onServicesDiscovered");
                        gatt.readCharacteristic(characteristic);
                        broadcastUpdate(ACTION_DATA_AVAILABLE);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            super.onCharacteristicRead(gatt, characteristic, value, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead");
                String receivedMessage = new String(value, StandardCharsets.UTF_8);
                Log.d(TAG, "data: " + receivedMessage);
                BluetoothService.receivedMessage = receivedMessage;
                try {
                    readValue = Double.parseDouble(receivedMessage);
                } catch (Exception e) {
                    Log.e(TAG, "Read value parsing error");
                }
            }
        }

        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            byte[] bytes = characteristic.getValue();
            String received = new String(bytes, StandardCharsets.UTF_8);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Write Success");
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    private void showToast(final String message) {
        handler.post(() -> Toast.makeText(BluetoothService.this, message, Toast.LENGTH_SHORT).show());
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}


// 변경 사항 요약

// 1. Bluetooth 지원 및 활성화 확인:
// - bluetoothAdapter 가 null 인지 확인하여 블루투스 지원 여부를 체크.
// - bluetoothAdapter.isEnabled()를 통해 블루투스 활성화 여부를 확인.

// 2. BluetoothLeScanner 초기화 확인:
// - bluetoothLeScanner가 null인지 확인하여 초기화가 제대로 되었는지 확인.
// - null일 경우 로그를 남기고 stopSelf()를 호출하여 서비스 종료.

// 3. BluetoothLeScanner null 체크:
// - scanLeDevice 메서드에서 bluetoothLeScanner가 null인지 체크하고 null일 경우 로그를 남기고 스캔을 시작하지 않음.

// 이렇게 하면 BluetoothLeScanner 가 null 상태에서 메서드를 호출하려고 해서 발생하는 문제를 해결할 수 있습니다.