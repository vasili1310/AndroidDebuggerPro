package com.example.androiddebuggerpro;

import static androidx.constraintlayout.widget.ConstraintSet.VISIBLE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private boolean isFlashOn = false;
    private CameraManager cameraManager;
    private String camerald;
    private static final int PERMISSIONS_REQUEST_CODE = 1001;
    private static final int REQUEST_RECORD_AUDIO = 101;

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private TextView textViewSSID, textViewSignalStrength, textViewIP, textViewLinkSpeed, textViewSpeedTestResult;
    private ListView listViewNetworks;
    private Button btnRefreshInfo, btnScanNetworks, btnToggleWiFi, btnSpeedTest;
    private ArrayAdapter<String> adapter;
    private MediaRecorder recorder;
    private MediaPlayer player;
    private boolean isMuted = false;
    private TextView cpuInfoTextView;
    private ScheduledThreadPoolExecutor executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFlashlight();

        Button screenTest = findViewById(R.id.btnScreenTest);
        Button vibrateTest = findViewById(R.id.btnVibrate);
        Button sensorTest = findViewById(R.id.btnSensors);
        Button flashTest = findViewById(R.id.btnFlash);
        Button cameraTest = findViewById(R.id.btnCamera);

        screenTest.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenTestActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        vibrateTest.setOnClickListener(v -> triggerVibration());

        sensorTest.setOnClickListener(v -> {
            Intent intent = new Intent(this, SensorTestActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        flashTest.setOnClickListener(v -> toggleFlashlight());

        cameraTest.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, 100);
            } else {
                openCamera();
            }
        });

        // WiFI
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        textViewSSID = findViewById(R.id.textViewSSID);
        textViewSignalStrength = findViewById(R.id.textViewSignalStrength);
        textViewIP = findViewById(R.id.textViewIP);
        textViewLinkSpeed = findViewById(R.id.textViewLinkSpeed);
        textViewSpeedTestResult = findViewById(R.id.textViewSpeedTestResult);

        btnRefreshInfo = findViewById(R.id.btnRefreshInfo);
        btnScanNetworks = findViewById(R.id.btnScanNetworks);
        btnToggleWiFi = findViewById(R.id.btnToggleWiFi);
        btnSpeedTest = findViewById(R.id.btnSpeedTest);

        listViewNetworks = findViewById(R.id.listViewNetworks);

        if (!hasRequiredPermissions()) {
            requestPermissions();
        } else {
            setupListeners();
            updateWifiInfo();
            scanNetworks();
        }

        // SpeedTest
        TextView textViewSpeedResult = findViewById(R.id.textViewSpeedTestResult);
        Button btnSpeedTest = findViewById(R.id.btnSpeedTest);

        btnSpeedTest.setOnClickListener(v -> runSpeedTest(textViewSpeedResult));

        // Battery
        Button btnBatteryTest = findViewById(R.id.btnBatteryTest);
        TextView textBatteryStatus = findViewById(R.id.textBatteryStatus);

        btnBatteryTest.setOnClickListener(v -> showBatteryStatus(textBatteryStatus));

        // System Info
        Button btnSystemInfo = findViewById(R.id.btnSystemInfo);
        TextView systemInfoOutput = findViewById(R.id.systemInfoOutput);

        btnSystemInfo.setOnClickListener(v -> showSystemInfo(systemInfoOutput));

        Button btnGPS = findViewById(R.id.btnGPS);

        btnGPS.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GpsDebugActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Storage Info
        Button btnStorageInfo = findViewById(R.id.btnStorageInfo);
        btnStorageInfo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StorageDebugActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Sound

        // Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }

        Button testSpeaker = findViewById(R.id.btnSpeakers);
        Button testMic = findViewById(R.id.btnMic);
        Button showVolume = findViewById(R.id.btnVolume);
        Button toggleMute = findViewById(R.id.btnMute);
        Button audioDevices = findViewById(R.id.btnAudioDevices);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        TextView audioDeviceList = findViewById(R.id.audioDeviceList);

        // Speaker test
        testSpeaker.setOnClickListener(v -> {
            if (player != null && player.isPlaying()) {
                player.stop();
                player.release();
            }
            player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
            player.start();
        });

        // Mic test
        testMic.setOnClickListener(v -> {
            try {
                String filePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/mic_test.3gp";
                if (recorder == null) {
                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setOutputFile(filePath);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    recorder.prepare();
                    recorder.start();
                    Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
                } else {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                    Toast.makeText(this, "Recording stopped. Playing...", Toast.LENGTH_SHORT).show();
                    player = new MediaPlayer();
                    player.setDataSource(filePath);
                    player.prepare();
                    player.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Mic test failed.", Toast.LENGTH_SHORT).show();
            }
        });

        // Volume levels
        showVolume.setOnClickListener(v -> {
            int media = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int notif = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            int alarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

            Toast.makeText(this, "Media: " + media + ", Notification: " + notif +
                    ", Alarm: " + alarm, Toast.LENGTH_SHORT).show();
        });

        // Toggle mute
        toggleMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        isMuted ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE, 0);
            }
            Toast.makeText(this, isMuted ? "Muted" : "Unmuted", Toast.LENGTH_SHORT).show();
        });

        // Audio devices
        audioDevices.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
                StringBuilder sb = new StringBuilder();
                for (AudioDeviceInfo device : devices) {
                    sb.append(device.getProductName())
                            .append(" (Type: ")
                            .append(device.getType())
                            .append(")\n");
                }
                audioDeviceList.setText(sb.length() > 0 ? sb.toString() :
                        "No audio output devices found.");
            } else {
                audioDeviceList.setText("Not supported on this Android version.");
            }
            audioDeviceList.setAlpha(0f);
            audioDeviceList.setVisibility(View.VISIBLE);
            audioDeviceList.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(null);
        });

        // CPU
        cpuInfoTextView = findViewById(R.id.cpuInfoTextView);
        Button btnStartCpuMonitoring = findViewById(R.id.btnStartCpuMonitoring);

        btnStartCpuMonitoring.setOnClickListener(v -> {
            if (!isMonitoring) {
                startCpuMonitoring();
                btnStartCpuMonitoring.setText("Stop CPU Monitoring");
            } else {
                stopCpuMonitoring();
                btnStartCpuMonitoring.setText("Start CPU Monitoring");
            }
            isMonitoring = !isMonitoring;
        });
    }

    private boolean hasRequiredPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void updateWifiInfo() {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Enable Wi-Fi to refresh info", Toast.LENGTH_LONG).show();
            return;
        }

        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

        if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            String ssid = wifiInfo.getSSID();
            // Strip quotes if present
            if (ssid != null & ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }

            int signalLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 100);
            String ip = Formatter.formatIpAddress(wifiInfo.getIpAddress());
            int linkSpeed = wifiInfo.getLinkSpeed();

            textViewSSID.setText("SSID: " + ssid);
            textViewSignalStrength.setText("Signal Strength: " + signalLevel + "%");
            textViewIP.setText("IP Address: " + ip);
            textViewLinkSpeed.setText("Link Speed: " + linkSpeed + "Mbps");
        } else {
            textViewSSID.setText("SSID: N/A");
            textViewSignalStrength.setText("Signal Strength: N/A");
            textViewIP.setText("IP Address: N/A");
            textViewLinkSpeed.setText("Link Speed: N/A");
        }
        textViewSSID.setAlpha(0f);
        textViewSSID.setVisibility(View.VISIBLE);
        textViewSSID.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);
        textViewSignalStrength.setAlpha(0f);
        textViewSignalStrength.setVisibility(View.VISIBLE);
        textViewSignalStrength.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);
        textViewIP.setAlpha(0f);
        textViewIP.setVisibility(View.VISIBLE);
        textViewIP.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);
        textViewLinkSpeed.setAlpha(0f);
        textViewLinkSpeed.setVisibility(View.VISIBLE);
        textViewLinkSpeed.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);
    }

    private void scanNetworks() {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Enable Wi-Fi to scan networks", Toast.LENGTH_LONG).show();
            return;
        }

        boolean started = wifiManager.startScan();
        if (!started) {
            Toast.makeText(this, "Scan failed or unsupported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<ScanResult> results = wifiManager.getScanResults();
        if (results != null) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            for (ScanResult result : results) {
                adapter.add(result.SSID + " - " + WifiManager.calculateSignalLevel(result.level, 100) + "%");
            }
            listViewNetworks.setAdapter(adapter);
        }
    }

    private void runSpeedTest(TextView outputView) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Download
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            String testFileUrl = "https://download.thinkbroadband.com/5MB.zip";

            Request request = new Request.Builder().url(testFileUrl).build();

            double downloadSpeedMbps = 0.0;
            try (Response response = client.newCall(request).execute()) {
                if (response.body() == null) {
                    runOnUiThread(() -> outputView.setText("Speed: Failed (empty response)\n"));
                    return;
                }

                long startTime = System.nanoTime();
                long totalBytesRead = 0;
                byte[] buffer = new byte[8192];
                int bytesRead;

                try (InputStream inputStream = response.body().byteStream()) {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        totalBytesRead += bytesRead;
                    }
                }

                long endTime = System.nanoTime();
                double timeSeconds = (endTime - startTime) / 1_000_000_000.0;
                double fileSizeMB = totalBytesRead / (1024.0 * 1024.0);
                downloadSpeedMbps = (fileSizeMB * 8) / timeSeconds;

                double finalDownloadSpeedMbps = downloadSpeedMbps;
                runOnUiThread(() -> outputView.setText(String.format("Download Speed: %.2f Mbps\nTesting upload speed...", finalDownloadSpeedMbps)));
                outputView.setAlpha(0f);
                outputView.setVisibility(View.VISIBLE);
                outputView.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .setListener(null);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> outputView.setText("Speed: Error - " + e.getMessage() + "\n"));
            }

            // Upload
            String uploadUrl = "https://httpbin.org/post";
            // Generate ~5MB of data to upload
            byte[] uploadData = new byte[5 * 1024 * 1024];
            for (int i = 0; i < uploadData.length; i++) {
                uploadData[i] = (byte) (i % 256);
            }
            RequestBody requestBody = RequestBody.create(uploadData, MediaType.parse("application/octet-stream"));
            Request uploadRequest = new Request.Builder().url(uploadUrl).post(requestBody).build();

            double uploadSpeedMbps = 0.0;
            try {
                long startTime = System.nanoTime();
                Response response = client.newCall(uploadRequest).execute();
                long endTime = System.nanoTime();

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> outputView.setText("Upload: Failed with code " + response.code()));
                    return;
                }

                double timeSeconds = (endTime - startTime) / 1_000_000_000.0;
                double fileSizeMB = uploadData.length / (1024.0 * 1024.0);
                uploadSpeedMbps = (fileSizeMB * 8) / timeSeconds;
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> outputView.setText("Upload Speed: Error - " + e.getMessage()));
                return;
            }

            // Show results
            String result = String.format("Download Speed: %.2f Mbps\nUpload Speed: %.2f Mbps", downloadSpeedMbps, uploadSpeedMbps);
            outputView.setAlpha(0f);
            outputView.setVisibility(View.VISIBLE);
            outputView.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(null);
            runOnUiThread(() -> outputView.setText(result));
        });
        outputView.setAlpha(0f);
        outputView.setVisibility(View.VISIBLE);
        outputView.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);
    }

    private void setupListeners() {
        btnRefreshInfo.setOnClickListener(v -> updateWifiInfo());

        btnScanNetworks.setOnClickListener(v -> scanNetworks());

        btnToggleWiFi.setOnClickListener(v -> {
            Intent panelIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(panelIntent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                setupListeners();
                updateWifiInfo();
            } else {
                Toast.makeText(this, "Permissions denied. Cannot proceed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE
                }, PERMISSIONS_REQUEST_CODE);
    }

    private void triggerVibration() {
        VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        Vibrator vibrator = vibratorManager.getDefaultVibrator();
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    private void initFlashlight() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (hasFlash != null && hasFlash && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    camerald = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFlashlight() {
        if (cameraManager == null || camerald == null) {
            initFlashlight();
        }
        try {
            isFlashOn = !isFlashOn;
            cameraManager.setTorchMode(camerald, isFlashOn);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Unable to toggle flashlight", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBatteryStatus(TextView outputView) {
        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);

        // Battery level as percentage
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        // Charging status
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus == null) {
            outputView.setText("Battery status not available");
            return;
        }

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String chargingStatus;

        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                chargingStatus = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                chargingStatus = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                chargingStatus = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                chargingStatus = "Not Charging";
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            default:
                chargingStatus = "Unknown";
                break;
        }

        // Battery health
        int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        String healthStatus = null;
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthStatus = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthStatus = "Overheat";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthStatus = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthStatus = "Over Voltage";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                healthStatus = "Failure";
                break;
            case BatteryManager.BATTERY_HEALTH_COLD:
                healthStatus = "Cold";
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                healthStatus = "Unknown";
                break;
        }

        // Battery temperature
        int tempTenthsC = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        float tempC = tempTenthsC / 10f;

        String batteryInfo = "Battery Level: " + batteryLevel + "%\n" +
                "Charging Status: " + chargingStatus + "\n" +
                "Health: " + healthStatus + "\n" +
                "Temperature: " + tempC + "°C";

        outputView.setText(batteryInfo);
    }

    // System Info
    @SuppressLint("DefaultLocale")
    private void showSystemInfo(TextView outputView) {
        StringBuilder info = new StringBuilder();

        // 1. Device Model & Manufacturer
        info.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
        info.append("Model: ").append(Build.MODEL).append("\n");

        // 2. Android Version
        info.append("Android Version: ").append(Build.VERSION.RELEASE).append("\n");
        info.append("SDK: ").append(Build.VERSION.SDK_INT).append("\n");

        // 3. CPU Architecture
        info.append("CPU ABI: ").append(Build.SUPPORTED_ABIS[0]).append("\n");

        // 4. RAM Info
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalRam = memoryInfo.totalMem / (1024 * 1024);
        long availRam = memoryInfo.availMem / (1024 * 1024);
        info.append("RAM: ").append(availRam).append("MB available / ").append(totalRam).append("MB Total\n");

        // 5. Storage Info
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long totalStorage = (blockSize * blockSize) / (1024 * 1024);
        long availableStorage = (blockSize * availableBlocks) / (1024 * 1024);
        info.append("Internal Storage: ").append(availableStorage).append("MB Free / ").append(totalStorage).append("MB Total\n");

        // 6. Screen Resolution & Density
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        float density = metrics.density;
        info.append("Screen: ").append(width).append("x").append(height).append(" @ ")
                .append(String.format("%.1f", density)).append("x Density\n");

        // 7. Uptime
        long uptimeMillis = SystemClock.uptimeMillis();
        long uptimeSeconds = uptimeMillis / 1000;
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        info.append("Uptime: ").append(hours).append("h ").append(minutes).append("m\n");

        // 8. Battery Level
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int batteryLevel = -1;
        if (batteryStatus != null) {
            batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        }
        info.append("Battery Level: ").append(batteryLevel).append("%\n");

        // 9. Network Type
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
        String networkType = "None";
        if (caps != null) {
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                networkType = "Wi-Fi";
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                networkType = "Cellular";
            }
        }
        info.append("Network: ").append(networkType).append("\n");

        // 10. Build Info
        info.append("Build Fingerprint: ").append(Build.FINGERPRINT).append("\n");

        outputView.setText(info.toString());
    }

    // CPU Methods
    private void startCpuMonitoring() {
        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleWithFixedDelay(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("CPU ABI: ").append(Build.SUPPORTED_ABIS[0]).append("\n");
            sb.append("Cores: ").append(Runtime.getRuntime().availableProcessors()).append("\n");
            sb.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
            sb.append("Model: ").append(Build.MODEL).append("\n");

            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);

            sb.append("\nRAM:\n");
            sb.append("Available: ").append(memoryInfo.availMem / (1024 * 1024)).append(" MB\n");
            sb.append("Total: ").append(memoryInfo.totalMem / (1024 * 1024)).append(" MB\n");
            
            cpuInfoTextView.setAlpha(0f);
            cpuInfoTextView.setVisibility(View.VISIBLE);
            cpuInfoTextView.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(null);

            mainHandler.post(() -> cpuInfoTextView.setText(sb.toString()));
        }, 0, 3, TimeUnit.SECONDS); // refresh every 3 secs
    }

    private void stopCpuMonitoring() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCpuMonitoring(); // stop when activity is destroyed
    }
}