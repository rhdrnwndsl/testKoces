package com.jiw.testkoces;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.jiw.testkoces.databinding.ActivityMainBinding;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AppToAppActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        if (binding.appBarMain.fab != null) {
            binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show());
        }
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationView navigationView = binding.navView;
        if (navigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_transform, R.id.nav_reflow, R.id.nav_slideshow, R.id.nav_settings)
                    .setOpenableLayout(binding.drawerLayout)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        }

        BottomNavigationView bottomNavigationView = binding.appBarMain.contentMain.bottomNavView;
        if (bottomNavigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_transform, R.id.nav_reflow, R.id.nav_slideshow)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        ///////////////
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        // Using findViewById because NavigationView exists in different layout files
        // between w600dp and w1240dp
        NavigationView navView = findViewById(R.id.nav_view);
        if (navView == null) {
            // The navigation drawer already has the items including the items in the overflow menu
            // We only inflate the overflow menu if the navigation drawer isn't visible
            getMenuInflater().inflate(R.menu.overflow, menu);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_settings);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    ///////////////////////

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private TextView display1, display2;
    private EditText editText1, editText2;
    private Button button1, button2;
    private MyHandler mHandler;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private void init() {
        //텍스트뷰나 이런 것들 체크처리
//        display1 = findViewById(R.id.textView1);
//        display2 = findViewById(R.id.textView2);
//
//        editText1 = findViewById(R.id.editText1);
//        editText2 = findViewById(R.id.editText2);
//
//        button1 = findViewById(R.id.buttonSend1);
//        button2 = findViewById(R.id.buttonSend2);

        button1.setOnClickListener((View v) -> {
            byte[] data = editText1.getText().toString().getBytes();
            usbService.write(data, 0);

        });

        button2.setOnClickListener((View v) -> {
            byte[] data = editText2.getText().toString().getBytes();
            usbService.write(data, 1);
        });

        mHandler = new MyHandler(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<AppToAppActivity> mActivity;

        public MyHandler(AppToAppActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.SYNC_READ:
                    String buffer = (String) msg.obj;
                    if(msg.arg1 == 0){
                        mActivity.get().display1.append(buffer);
                    }else if(msg.arg1 == 1){
                        mActivity.get().display2.append(buffer);
                    }

                    break;
            }
        }
    }

    //////////////////////////////
    //================== [블루투스 퍼미션 결과 확인] ==================
    public static final int PERMISSIONS_REQUEST_CODE = 1234;

    //================== [블루투스 상태 확인 위한 전역 변수 선언 실시] ==================
    public static final String BLUETOOTH_DISABLE = "DISABLE";
    public static final String BLUETOOTH_ACTIVE = "ACTIVE";
    public static final String BLUETOOTH_INACTIVE = "INACTIVE";

    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private Handler handler = new Handler();
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public ArrayList<BluetoothDevice> mLeDevices = new ArrayList();
    private LeDeviceListAdapter mLeDeviceListAdapter = new LeDeviceListAdapter();
    final HashMap<String, String> bleList = new HashMap<String, String>();
    Set key;
    private Context mCtx;

    private BluetoothGatt mBluetoothGatt = null;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    //================== [블루투스 상태 확인 메소드] ==================
    public String getBluetoothState() {
        try {
            Log.d("---", "---");
            Log.d("//===========//", "================================================");
            Log.d("", "\n" + "[A_Blutooth > getBluetoothState() 메소드 : 블루투스 지원 여부 및 활성, 비활성 확인 실시]");
            Log.d("//===========//", "================================================");
            Log.d("---", "---");
            /**
             * [확인 방법]
             * 1. 블루투스 상태 권한을 획득 실시 - AndroidManifest.xml : <uses-permission android:name="android.permission.BLUETOOTH"/>
             * 2. BluetoothAdapter 객체를 사용해 블루투스 지원 여부 및 활성, 비활성 확인 실시
             * 3. 블루투스 지원하지 않는 기기 리턴 값 - DISABLE
             * 4. 블루투스 비활성 경우 리턴 값 - INACTIVE
             * 5. 블루투스 활성인 경우 리턴 값 - ACTIVE
             */
//            BluetoothAdapter mBluetoothAdapter = null;
//            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            // [객체 생성 실시]
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) { //TODO 블루투스를 지원하지 않는 기기인지 확인
                Log.d("---", "---");
                Log.e("//===========//", "================================================");
                Log.d("", "\n" + "[A_Blutooth > 블루투스 지원 기기 확인 : 지원하지 않는 모바일 기기]");
                Log.e("//===========//", "================================================");
                Log.d("---", "---");
                return BLUETOOTH_DISABLE;
            } else { //TODO 블루투스가 켜져있는지 확인 [블루투스 지원 기기]
                Log.d("---", "---");
                Log.w("//===========//", "================================================");
                Log.d("", "\n" + "[A_Blutooth > 블루투스 지원 기기 확인 : 지원하는 모바일 기기]");
                Log.w("//===========//", "================================================");
                Log.d("---", "---");
                if (mBluetoothAdapter.isEnabled() == true) {
                    Log.d("---", "---");
                    Log.w("//===========//", "================================================");
                    Log.d("", "\n" + "[A_Blutooth > 블루투스 기능 활성 확인 : 활성 상태]");
                    Log.w("//===========//", "================================================");
                    Log.d("---", "---");
                    return BLUETOOTH_ACTIVE;
                } else {
                    Log.d("---", "---");
                    Log.e("//===========//", "================================================");
                    Log.d("", "\n" + "[A_Blutooth > 블루투스 기능 활성 확인 : 비활성 상태]");
                    Log.e("//===========//", "================================================");
                    Log.d("---", "---");
                    return BLUETOOTH_INACTIVE;
                }
            }
        } catch (Exception e) {
            Log.d("---", "---");
            Log.e("//===========//", "================================================");
            Log.d("", "\n" + "[A_Blutooth > 블루투스 지원 기기 확인 : 지원하지 않는 모바일 기기]");
            Log.d("", "\n" + "[Catch 메시지 : " + String.valueOf(e.getMessage()) + "]");
            Log.e("//===========//", "================================================");
            Log.d("---", "---");
            return BLUETOOTH_DISABLE;
        }
    }

    private void checkPermssion() {
        String[] permissions;
        if (Build.VERSION.SDK_INT > 30) {

            permissions = new String[]{android.Manifest.permission.CAMERA,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.ACCESS_MEDIA_LOCATION,
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.RECORD_AUDIO
            };
        } else {
            permissions = new String[]{android.Manifest.permission.CAMERA,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.RECORD_AUDIO
            };
        }

        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
        } else {
            //퍼미션을 모두 받아옴
            Log.d("---", "---");
            Log.e("//===========//", "================================================");
            Log.d("", "\n" + "[A_Blutooth > 블루투스 퍼미션 확인 : 모든 퍼미션 승인 완료]");
            Log.e("//===========//", "================================================");
            Log.d("---", "---");
            switch (getBluetoothState()) {
                case BLUETOOTH_DISABLE:
                    break;
                case BLUETOOTH_ACTIVE:
                    scanLeDevice();
                    break;
                case BLUETOOTH_INACTIVE:
                    break;
                default:
                    break;
            }
        }
    }

    //권한체크 함수
    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void scanLeDevice() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (ActivityCompat.checkSelfPermission(mCtx, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            if (!scanning) {
                // Stops scanning after a predefined scan period.
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanning = false;
                        if (ActivityCompat.checkSelfPermission(mCtx, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                            bluetoothLeScanner.stopScan(leScanCallback);
                            if (mLeDevices.size() > 0) {
//                                mTextToSpeech.speak(mLeDevices.get(0).getName() + " 연결을 시도합니다", TextToSpeech.QUEUE_FLUSH, null, "id");
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        connect(mLeDevices.get(0));
                                    }
                                },7000);
                            }

                            return;
                        }

                    }
                }, SCAN_PERIOD);

                scanning = true;
                String SERVICE_STRING_C1_OLD_NOT_PRINT = "49535343-FE7D-4AE5-8FA9-9FAFD205E455";
                String SERVICE_STRING_C1_OLD_PRINT = "49535343-FE7D-4AE5-8FA9-9FAFD205E455";
                String SERVICE_STRING_C1_NEW_PRINT = "49324541-5211-FA30-4301-48AFD205E400";
                String SERVICE_STRING_ZOA = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
                String SERVICE_STRING_KWANGWOO = "49535343-FE7D-4AE5-8FA9-9FAFD205E455";
                UUID SERVICE_UUID_C1_OLD_NOT_PRINT = UUID.fromString(SERVICE_STRING_C1_OLD_NOT_PRINT);
                UUID SERVICE_UUID_C1_OLD_PRINT = UUID.fromString(SERVICE_STRING_C1_OLD_PRINT);
                UUID SERVICE_UUID_C1_NEW_PRINT = UUID.fromString(SERVICE_STRING_C1_NEW_PRINT);
                UUID SERVICE_UUID_ZOA = UUID.fromString(SERVICE_STRING_ZOA);
                UUID SERVICE_UUID_KWANGWOO = UUID.fromString(SERVICE_STRING_KWANGWOO);

                String[] SERVICE_STRING = new String[]{
                        "49535343-FE7D-4AE5-8FA9-9FAFD205E455",
                        "49324541-5211-FA30-4301-48AFD205E400",
                        "6E400001-B5A3-F393-E0A9-E50E24DCCA9E",
                        "49535343-FE7D-4AE5-8FA9-9FAFD205E455",
                };
                UUID[] SERVICE_UUID = new UUID[]{
                        UUID.fromString(SERVICE_STRING[0]),
                        UUID.fromString(SERVICE_STRING[1]),
                        UUID.fromString(SERVICE_STRING[2]),
                        UUID.fromString(SERVICE_STRING[3]),
                };

                List<ScanFilter> filters = new ArrayList<>();
                ScanFilter[] scan_filter = new ScanFilter[4];
                for (int i = 0; i < 4; i++) {
                    scan_filter[i] = new ScanFilter.Builder()
                            .setServiceUuid(new ParcelUuid(SERVICE_UUID[i]))
                            .build();
                    filters.add(scan_filter[i]);
                }
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .build();
                key = bleList.keySet();
                bluetoothLeScanner.startScan(filters, settings, leScanCallback);
            } else {
                scanning = false;
                bluetoothLeScanner.stopScan(leScanCallback);
            }
        }
    }

    public void writeDevice() {
        if (ActivityCompat.checkSelfPermission(mCtx, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        BluetoothGattCharacteristic writableChar = null;
        byte[] data = new byte[]{0x02,0x00,0x02,0x50,0x03,0x51};

        List<BluetoothGattService> services = mBluetoothGatt.getServices();
        for (BluetoothGattService service : services) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                //"Found characteristic : " + characteristic.getUuid()
                UUID C1NEW = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");
                UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
                if (characteristic.getUuid().equals(RX_CHAR_UUID)) {
                    writableChar = characteristic;
//                    writableChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    writableChar.setValue(data);
                }
            }
        }

        mBluetoothGatt.writeCharacteristic(writableChar);
    }
    /**
     * Connecting Device
     */
    public void connect(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(mCtx, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        final int[] count = {0};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBluetoothGatt = device.connectGatt(mCtx, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    if (ActivityCompat.checkSelfPermission(mCtx, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        return;
                    }
                    if (status == BluetoothGatt.GATT_FAILURE) {
                        Log.d("---", "---");
                        Log.e("//===========//", "================================================");
                        Log.d("", "\n" + "[A_Blutooth > 블루투스 onConnectionStateChange BluetoothGatt.GATT_FAILURE]");
                        Log.d("", "\n" + "[onConnectionStateChange newState : " + newState + "]");
                        Log.d("", "\n" + "[onConnectionStateChange status : " + status + "]");
                        Log.e("//===========//", "================================================");
                        Log.d("---", "---");
                        gatt.disconnect();
                        gatt.close();
//                        mTextToSpeech.speak("연결에 실패하였습니다", TextToSpeech.QUEUE_FLUSH, null, "id");
                        mLeDeviceListAdapter.removeDevice(gatt.getDevice());
                        return;
                    }
                    if (status == 133) // Unknown Error
                    {
                        Log.d("---", "---");
                        Log.e("//===========//", "================================================");
                        Log.d("", "\n" + "[A_Blutooth > 블루투스 onConnectionStateChange 133에러]");
                        Log.d("", "\n" + "[onConnectionStateChange newState : " + newState + "]");
                        Log.d("", "\n" + "[onConnectionStateChange status : " + status + "]");
                        Log.e("//===========//", "================================================");
                        Log.d("---", "---");
                        gatt.disconnect();
                        gatt.close();
//                        mTextToSpeech.speak("연결에 실패하였습니다", TextToSpeech.QUEUE_FLUSH, null, "id");
                        mLeDeviceListAdapter.removeDevice(gatt.getDevice());
                        return;
                    }
                    if (newState == BluetoothGatt.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("---", "---");
                        Log.e("//===========//", "================================================");
                        Log.d("", "\n" + "[A_Blutooth > 블루투스 onConnectionStateChange]");
                        Log.d("", "\n" + "[onConnectionStateChange newState : " + newState + "]");
                        Log.d("", "\n" + "[onConnectionStateChange status : " + status + "]");
                        Log.e("//===========//", "================================================");
                        Log.d("---", "---");
                        gatt.discoverServices();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    Log.d("---", "---");
                    Log.e("//===========//", "================================================");
                    Log.d("", "\n" + "[A_Blutooth > 블루투스 onServicesDiscovered]");
                    Log.d("", "\n" + "[onServicesDiscovered characteristic : " + gatt.getServices() + "]");
                    Log.d("", "\n" + "[onServicesDiscovered status : " + status + "]");
                    Log.e("//===========//", "================================================");
                    Log.d("---", "---");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        List<BluetoothGattService> services = gatt.getServices();
                        for (BluetoothGattService service : services) {
                            // "Found service : " + service.getUuid()
                            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                                //"Found characteristic : " + characteristic.getUuid()
                                if (hasProperty(characteristic, BluetoothGattCharacteristic.PROPERTY_READ)) {
                                    // "Read characteristic : " + characteristic.getUuid());
                                    if (ActivityCompat.checkSelfPermission(mCtx, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        return;
                                    }
                                    gatt.readCharacteristic(characteristic);
                                }

                                if( hasProperty(characteristic, BluetoothGattCharacteristic.PROPERTY_NOTIFY))
                                {
                                    if (ActivityCompat.checkSelfPermission(mCtx, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        return;
                                    }
                                    // "Register notification for characteristic : " + characteristic.getUuid());
                                    gatt.setCharacteristicNotification(characteristic, true);

                                    UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    gatt.writeDescriptor(descriptor);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
                    super.onCharacteristicRead(gatt, characteristic, value, status);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onCharacteristicRead]");
                    Log.d("","\n"+"[onCharacteristicRead characteristic : "+characteristic+"]");
                    Log.d("","\n"+"[onCharacteristicRead value : "+bytesToHex_0xType(value)+"]");
                    Log.d("","\n"+"[onCharacteristicRead status : "+status+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");

//                    if (count[0] > 0) {
//                        return;
//                    }
//                    count[0]++;
//                    writeDevice();
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onCharacteristicWrite]");
                    Log.d("","\n"+"[onCharacteristicWrite characteristic : "+characteristic+"]");
                    Log.d("","\n"+"[onCharacteristicWrite status : "+status+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                }

                @Override
                public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
                    super.onCharacteristicChanged(gatt, characteristic, value);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onCharacteristicChanged]");
                    Log.d("","\n"+"[onCharacteristicChanged characteristic : "+characteristic+"]");
                    Log.d("","\n"+"[onCharacteristicChanged value : "+bytesToHex_0xType(value)+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
//                if( onNotifyValueListener == null ) return;
//                // This is Background Thread
//                mainThreadHandler.post(
//                        ()->onNotifyValueListener.onValue(gatt.getDevice(), onNotifyValueListener.formatter(characteristic))
//                );
                }

                @Override
                public void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
                    super.onDescriptorRead(gatt, descriptor, status, value);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onDescriptorRead]");
                    Log.d("","\n"+"[onDescriptorRead mtu : "+descriptor+"]");
                    Log.d("","\n"+"[onDescriptorRead status : "+status+"]");
                    Log.d("","\n"+"[onDescriptorRead value : "+bytesToHex_0xType(value)+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onDescriptorWrite]");
                    Log.d("","\n"+"[onDescriptorWrite mtu : "+descriptor+"]");
                    Log.d("","\n"+"[onDescriptorWrite status : "+status+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                }

                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    super.onMtuChanged(gatt, mtu, status);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onMtuChanged]");
                    Log.d("","\n"+"[onMtuChanged mtu : "+mtu+"]");
                    Log.d("","\n"+"[onMtuChanged status : "+status+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                }

                @Override
                public void onServiceChanged(@NonNull BluetoothGatt gatt) {
                    super.onServiceChanged(gatt);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onServiceChanged]");
                    Log.d("","\n"+"[onServiceChanged : "+gatt.getServices()+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                }
            }, BluetoothDevice.TRANSPORT_LE);
        }
        else {
            mBluetoothGatt = device.connectGatt(mCtx, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    if (ActivityCompat.checkSelfPermission(mCtx, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        return;
                    }
                    if (status == BluetoothGatt.GATT_FAILURE) {
                        Log.d("---", "---");
                        Log.e("//===========//", "================================================");
                        Log.d("", "\n" + "[A_Blutooth > 블루투스 onConnectionStateChange BluetoothGatt.GATT_FAILURE]");
                        Log.d("", "\n" + "[onConnectionStateChange newState : " + newState + "]");
                        Log.d("", "\n" + "[onConnectionStateChange status : " + status + "]");
                        Log.e("//===========//", "================================================");
                        Log.d("---", "---");
                        gatt.disconnect();
                        gatt.close();
                        mLeDeviceListAdapter.removeDevice(gatt.getDevice());
                        return;
                    }
                    if (status == 133) // Unknown Error
                    {
                        Log.d("---", "---");
                        Log.e("//===========//", "================================================");
                        Log.d("", "\n" + "[A_Blutooth > 블루투스 onConnectionStateChange 133에러]");
                        Log.d("", "\n" + "[onConnectionStateChange newState : " + newState + "]");
                        Log.d("", "\n" + "[onConnectionStateChange status : " + status + "]");
                        Log.e("//===========//", "================================================");
                        Log.d("---", "---");
                        gatt.disconnect();
                        gatt.close();
                        mLeDeviceListAdapter.removeDevice(gatt.getDevice());
                        return;
                    }
                    if (newState == BluetoothGatt.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("---", "---");
                        Log.e("//===========//", "================================================");
                        Log.d("", "\n" + "[A_Blutooth > 블루투스 onConnectionStateChange]");
                        Log.d("", "\n" + "[onConnectionStateChange newState : " + newState + "]");
                        Log.d("", "\n" + "[onConnectionStateChange status : " + status + "]");
                        Log.e("//===========//", "================================================");
                        Log.d("---", "---");
                        gatt.discoverServices();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    Log.d("---", "---");
                    Log.e("//===========//", "================================================");
                    Log.d("", "\n" + "[A_Blutooth > 블루투스 onServicesDiscovered]");
                    Log.d("", "\n" + "[onServicesDiscovered characteristic : " + gatt.getServices() + "]");
                    Log.d("", "\n" + "[onServicesDiscovered status : " + status + "]");
                    Log.e("//===========//", "================================================");
                    Log.d("---", "---");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        List<BluetoothGattService> services = gatt.getServices();
                        for (BluetoothGattService service : services) {
                            // "Found service : " + service.getUuid()
                            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                                //"Found characteristic : " + characteristic.getUuid()
                                if (hasProperty(characteristic, BluetoothGattCharacteristic.PROPERTY_READ)) {
                                    // "Read characteristic : " + characteristic.getUuid());
                                    if (ActivityCompat.checkSelfPermission(mCtx, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        return;
                                    }
                                    gatt.readCharacteristic(characteristic);
                                }

                                if( hasProperty(characteristic, BluetoothGattCharacteristic.PROPERTY_NOTIFY))
                                {
                                    if (ActivityCompat.checkSelfPermission(mCtx, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        return;
                                    }
                                    // "Register notification for characteristic : " + characteristic.getUuid());
                                    gatt.setCharacteristicNotification(characteristic, true);

                                    UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    gatt.writeDescriptor(descriptor);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
                    super.onCharacteristicRead(gatt, characteristic, value, status);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onCharacteristicRead]");
                    Log.d("","\n"+"[onCharacteristicRead characteristic : "+characteristic+"]");
                    Log.d("","\n"+"[onCharacteristicRead value : "+bytesToHex_0xType(value)+"]");
                    Log.d("","\n"+"[onCharacteristicRead status : "+status+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
//                if( status == BluetoothGatt.GATT_SUCCESS) {
//                    if( onReadValueListener == null ) return;
//                    // This is Background Thread
//                    mainThreadHandler.post(
//                            () ->onReadValueListener.onValue(gatt.getDevice(), onReadValueListener.formatter(characteristic))
//                    );
//                }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onCharacteristicWrite]");
                    Log.d("","\n"+"[onCharacteristicWrite characteristic : "+characteristic+"]");
                    Log.d("","\n"+"[onCharacteristicWrite status : "+status+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                }

                @Override
                public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
                    super.onCharacteristicChanged(gatt, characteristic, value);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onCharacteristicChanged]");
                    Log.d("","\n"+"[onCharacteristicChanged characteristic : "+characteristic+"]");
                    Log.d("","\n"+"[onCharacteristicChanged value : "+bytesToHex_0xType(value)+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
//                if( onNotifyValueListener == null ) return;
//                // This is Background Thread
//                mainThreadHandler.post(
//                        ()->onNotifyValueListener.onValue(gatt.getDevice(), onNotifyValueListener.formatter(characteristic))
//                );
                }

                @Override
                public void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
                    super.onDescriptorRead(gatt, descriptor, status, value);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onDescriptorRead]");
                    Log.d("","\n"+"[onDescriptorRead mtu : "+descriptor+"]");
                    Log.d("","\n"+"[onDescriptorRead status : "+status+"]");
                    Log.d("","\n"+"[onDescriptorRead value : "+bytesToHex_0xType(value)+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onDescriptorWrite]");
                    Log.d("","\n"+"[onDescriptorWrite mtu : "+descriptor+"]");
                    Log.d("","\n"+"[onDescriptorWrite status : "+status+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                }

                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    super.onMtuChanged(gatt, mtu, status);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onMtuChanged]");
                    Log.d("","\n"+"[onMtuChanged mtu : "+mtu+"]");
                    Log.d("","\n"+"[onMtuChanged status : "+status+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                }

                @Override
                public void onServiceChanged(@NonNull BluetoothGatt gatt) {
                    super.onServiceChanged(gatt);
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 onServiceChanged]");
                    Log.d("","\n"+"[onServiceChanged : "+gatt.getServices()+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                }
            });
        }

    }

    public static boolean hasProperty(BluetoothGattCharacteristic characteristic, int property) {
        int prop = characteristic.getProperties() & property;
        return prop == property;
    }

    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Log.d("---", "---");
                    Log.e("//===========//", "================================================");
                    Log.d("", "\n" + "[A_Blutooth > 블루투스 스캔 확인]");
                    if (ActivityCompat.checkSelfPermission(mCtx, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("", "\n" + "[" + result.getDevice() + " : " + result.getDevice().getName() + "]");
                        bleList.put(result.getDevice().getAddress(), result.getDevice().getName());
                    } else {
                        Log.d("","\n"+"[블루투스 퍼미션 없음 : Manifest.permission.BLUETOOTH_CONNECT]");
                    }

                    for (Iterator iterator = key.iterator(); iterator.hasNext();) {
                        String addr = (String) iterator.next();
                        String name = (String) bleList.get(addr);
                        if (result.getDevice().getAddress().equals(addr)){
                            if (!mLeDevices.contains(result.getDevice())) {
                                Log.v("err", "스캐닝된 Device를 리스트에 추가");
//                                mTextToSpeech.speak(result.getDevice().getName() + "을 찾았습니다", TextToSpeech.QUEUE_FLUSH,null,"id");
                                mLeDeviceListAdapter.addDevice(result.getDevice());
                            }

                        }

                    }
                    Log.e("//===========//","================================================");
                    Log.d("---","---");

                }
            };

    /** checkPermission() 를 통해 권한을 요청하고 이곳에서 그 결과를 받는다 */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //사용자가 권한 거절시. 일단 읽고 쓰기 권한을 거절해도 다른 권한(메시지박스를 다른 앱들 위에 띄우기)을 물어봐야한다.
                            Log.d("---","---");
                            Log.e("//===========//","================================================");
                            Log.d("","\n"+"[A_Blutooth > 블루투스 퍼미션 확인 : 퍼미션 승인 실패]");
                            Log.d("","\n"+"["+permissions[i]+" : "+String.valueOf(grantResults[i])+"]");
                            Log.e("//===========//","================================================");
                            Log.d("---","---");
//                            mPermissionCheckListener.onResult(false);
                            return;
                        }
                    }
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 퍼미션 확인 : 퍼미션 승인 완료]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
                    switch (getBluetoothState()) {
                        case BLUETOOTH_DISABLE:
                            break;
                        case BLUETOOTH_ACTIVE:
                            scanLeDevice();
                            break;
                        case BLUETOOTH_INACTIVE:
                            break;
                        default:
                            break;
                    }
                    //권한 허용 선택시
                    //오레오부터 꼭 권한체크내에서 파일 만들어줘야함
                    //권한을 확인(파일 읽고 쓰기문제. 로그파일 만들기 위해서는 권한을 확인해 줘야 한다
//                    if (m_logfile == null) {
//                        m_logfile = new LogFile(this);
//                    }
//                    m_logfile.deleteLogFile();
//                    mPermissionCheckListener.onResult(true);
                    return;

                } else {
                    Log.d("---","---");
                    Log.e("//===========//","================================================");
                    Log.d("","\n"+"[A_Blutooth > 블루투스 퍼미션 확인 : 퍼미션 승인 실패]");
                    Log.d("","\n"+"[퍼미션 결과 없음 : "+String.valueOf(grantResults.length)+"]");
                    Log.e("//===========//","================================================");
                    Log.d("---","---");
//                    mPermissionCheckListener.onResult(false);
                    return;
                }

        }
    }

    public static String bytesToHex_0xType(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        String tmp = "";

        for(int i=0;i<hexChars.length;i+=2)
        {
            //tmp += "0x" + String.valueOf(hexChars[i])+String.valueOf(hexChars[i+1]) + " ";
            tmp += String.valueOf(hexChars[i])+String.valueOf(hexChars[i+1]) + " ";
        }
        return tmp;
    }


    class LeDeviceListAdapter {
        public LeDeviceListAdapter() {
            mLeDevices = new ArrayList();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                Log.v("err", "스캐닝된 Device를 리스트에 추가");
                mLeDevices.add(device);
            }

        }

        public BluetoothDevice getDevice(int position) {
            return (BluetoothDevice)mLeDevices.get(position);
        }

        public void removeDevice(BluetoothDevice device) {
            if (mLeDevices.equals(device)) {
                Log.v("err", "스캐닝된 Device를 리스트에서 제거");
                mLeDevices.remove(device);
            }
        }

        public void clear() {
            mLeDevices.clear();
        }

        public int getCount() {
            return mLeDevices.size();
        }

        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        public long getItemId(int i) {
            return (long)i;
        }
    }

}