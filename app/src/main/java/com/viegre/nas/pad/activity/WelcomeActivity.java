package com.viegre.nas.pad.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.util.ZxingUtils;

import java.util.Arrays;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private final UUID UUID_SERVICE = UUID.fromString("10000000-0000-0000-0000-000000000000"); //自定义UUID
    private final UUID UUID_CHAR_READ_NOTIFY = UUID.fromString("11000000-0000-0000-0000-000000000000");
    private final UUID UUID_DESC_NOTITY = UUID.fromString("11100000-0000-0000-0000-000000000000");
    private final UUID UUID_CHAR_WRITE = UUID.fromString("12000000-0000-0000-0000-000000000000");
    private final String TAG = WelcomeActivity.class.getSimpleName();
    private AppCompatTextView welcomeSkip;
    private ConstraintLayout Layout1;
    private ConstraintLayout Layout2;
    //    private BlueToothValueReceiver blueToothValueReceiver;
    private BluetoothGattServer mBluetoothGattServer; // BLE服务端
    private TextView next_to;
    private int id;
    private LinearLayout layout3;
    private BluetoothAdapter mBluetoothAdapter;
    // BLE广播Callback
    @SuppressLint("NewApi")
    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            CommonUtils.showToast("BLE广播开启成功");
//            Toast.makeText(MainActivity2.this,"BLE广播开启成功",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            CommonUtils.showToast("BLE广播开启失败,错误码:" + errorCode);

//            Toast.makeText(MainActivity2.this,"BLE广播开启失败,错误码:" + errorCode,Toast.LENGTH_SHORT).show();
        }
    };
    // BLE服务端Callback
    @SuppressLint("NewApi")
    private final BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.i(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", device.getName(), device.getAddress(), status, newState));
            CommonUtils.showToast(String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status), device));
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.i(TAG, String.format("onServiceAdded:%s,%s", status, service.getUuid()));
//            CommonUtils.showToast(String.format(status == 0 ? "添加服务[%s]成功" : "添加服务[%s]失败,错误码:" + status, service.getUuid()));
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG,
                    String.format("onCharacteristicReadRequest:%s,%s,%s,%s,%s",
                            device.getName(),
                            device.getAddress(),
                            requestId,
                            offset,
                            characteristic.getUuid()));
//            String response = "CHAR_" + (int) (Math.random() * 100); //模拟数据
            String macAddress = DeviceUtils.getMacAddress();//获取本地Mac地址
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, macAddress.getBytes());// 响应客户端
//            CommonUtils.showToast("客户端读取Characteristic[" + characteristic.getUuid() + "]:\n" + response);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
            // 获取客户端发过来的数据
            String requestStr = new String(requestBytes);
            Log.i(TAG,
                    String.format("onCharacteristicWriteRequest:%s,%s,%s,%s,%s,%s,%s,%s",
                            device.getName(),
                            device.getAddress(),
                            requestId,
                            characteristic.getUuid(),
                            preparedWrite,
                            responseNeeded,
                            offset,
                            requestStr));
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, requestBytes);// 响应客户端
//            CommonUtils.showToast("客户端写入Characteristic[" + characteristic.getUuid() + "]:\n" + requestStr);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.i(TAG,
                    String.format("onDescriptorReadRequest:%s,%s,%s,%s,%s",
                            device.getName(),
                            device.getAddress(),
                            requestId,
                            offset,
                            descriptor.getUuid()));
            String response = "DESC_" + (int) (Math.random() * 100); //模拟数据
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response.getBytes()); // 响应客户端
//            CommonUtils.showToast("客户端读取Descriptor[" + descriptor.getUuid() + "]:\n" + response);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            // 获取客户端发过来的数据
            String valueStr = Arrays.toString(value);
            Log.i(TAG,
                    String.format("onDescriptorWriteRequest:%s,%s,%s,%s,%s,%s,%s,%s",
                            device.getName(),
                            device.getAddress(),
                            requestId,
                            descriptor.getUuid(),
                            preparedWrite,
                            responseNeeded,
                            offset,
                            valueStr));
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);// 响应客户端
//            CommonUtils.showToast("客户端写入Descriptor[" + descriptor.getUuid() + "]:\n" + valueStr);

            // 简单模拟通知客户端Characteristic变化
            if (Arrays.toString(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE).equals(valueStr)) { //是否开启通知
                final BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 5; i++) {
                            SystemClock.sleep(3000);
                            String response = "CHAR_" + (int) (Math.random() * 100); //模拟数据
                            characteristic.setValue(response);
                            mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
                            CommonUtils.showToast("通知客户端改变Characteristic[" + characteristic.getUuid() + "]:\n" + response);
                        }
                    }
                }).start();
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            Log.i(TAG, String.format("onExecuteWrite:%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, execute));
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            Log.i(TAG, String.format("onNotificationSent:%s,%s,%s", device.getName(), device.getAddress(), status));
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            Log.i(TAG, String.format("onMtuChanged:%s,%s,%s", device.getName(), device.getAddress(), mtu));
        }
    };
    private ImageView mQRCodeImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initBle();
        initView(this);
    }

    @SuppressLint("MissingPermission")
    private void initBle() {
        // 检查蓝牙开关
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
//            APP.toast(MainActivity.this,"本机没有找到蓝牙硬件或驱动！", 0);
            CommonUtils.showToast("本机没有找到蓝牙硬件或驱动！");
//            finish();
            return;
        } else {
            if (!adapter.isEnabled()) {
                //直接开启蓝牙
                adapter.enable();
                //跳转到设置界面
                //startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 112);
            }
        }

        // 检查是否支持BLE蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            APP.toast(MainActivity.this,"本机不支持低功耗蓝牙！", 0);
            CommonUtils.showToast("本机不支持低功耗蓝牙！");
            finish();
            return;
        }

        // Android 6.0动态请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION};
            for (String str : permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, 111);
                    break;
                }
            }
        }
    }

    private void initView(WelcomeActivity welcomeActivity) {
        welcomeSkip = findViewById(R.id.welcomeSkip);
        Layout1 = findViewById(R.id.Layout1);
        Layout2 = findViewById(R.id.Layout2);
        layout3 = findViewById(R.id.Layout3);
        next_to = findViewById(R.id.next_to);
        mQRCodeImg = findViewById(R.id.imageView3);

        Layout1.setVisibility(View.VISIBLE);
        Layout2.setVisibility(View.GONE);
        layout3.setVisibility(View.GONE);

        welcomeSkip.setOnClickListener(this);
        next_to.setOnClickListener(this);
        createQRCode();
    }

    private void createQRCode() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sn", PhoneUtils.getSerial());
            jsonObject.put("deviceName", "GOV NAS 2020");
            Bitmap qweqweqweqweqweq = ZxingUtils.createQRCode(jsonObject.toString(), 500, 500, true);
            mQRCodeImg.setImageBitmap(qweqweqweqweqweq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        id = v.getId();
        if (id == R.id.welcomeSkip) {
            Layout1.setVisibility(View.GONE);
            Layout2.setVisibility(View.VISIBLE);
            layout3.setVisibility(View.GONE);
        } else if (id == R.id.next_to) {
            initLayout3();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        从蓝牙设置界面返回时判断是否打开蓝牙
        if (mBluetoothAdapter.isEnabled()) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            WelcomeActivity.this.finish();
        } else {
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        }
    }

    private void initLayout3() {
//        显示layout3视图 获取蓝牙权限  监控蓝牙
        Layout1.setVisibility(View.GONE);
        Layout2.setVisibility(View.GONE);
        layout3.setVisibility(View.VISIBLE);
        runBle();
    }

    @SuppressLint("NewApi")
    private void runBle() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ============启动BLE蓝牙广播(广告) =================================================================================
        //广播设置(必须)
        AdvertiseSettings settings = new AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //广播模式: 低功耗,平衡,低延迟
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) //发射功率级别: 极低,低,中,高
                .setConnectable(true) //能否连接,广播分为可连接广播和不可连接广播
                .build();
        //广播数据(必须，广播启动就会发送)
        AdvertiseData advertiseData = new AdvertiseData.Builder().setIncludeDeviceName(true) //包含蓝牙名称
                .setIncludeTxPowerLevel(true) //包含发射功率级别
                .addManufacturerData(1, new byte[]{23, 33}) //设备厂商数据，自定义
                .build();
        //扫描响应数据(可选，当客户端扫描时才发送)
        AdvertiseData scanResponse = new AdvertiseData.Builder().addManufacturerData(2, new byte[]{66, 66}) //设备厂商数据，自定义
                .addServiceUuid(new ParcelUuid(UUID_SERVICE)) //服务UUID
//                .addServiceData(new ParcelUuid(UUID_SERVICE), new byte[]{2}) //服务数据，自定义
                .build();
//        BluetoothLeAdvertiser mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        bluetoothAdapter.getBluetoothLeAdvertiser().startAdvertising(settings, advertiseData, scanResponse, mAdvertiseCallback);

        // 注意：必须要开启可连接的BLE广播，其它设备才能发现并连接BLE服务端!
        // =============启动BLE蓝牙服务端=====================================================================================
        BluetoothGattService service = new BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //添加可读+通知characteristic
        BluetoothGattCharacteristic characteristicRead = new BluetoothGattCharacteristic(UUID_CHAR_READ_NOTIFY,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        characteristicRead.addDescriptor(new BluetoothGattDescriptor(UUID_DESC_NOTITY, BluetoothGattCharacteristic.PERMISSION_WRITE));
        service.addCharacteristic(characteristicRead);
        //添加可写characteristic
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(UUID_CHAR_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(characteristicWrite);
        if (bluetoothManager != null) {
            mBluetoothGattServer = bluetoothManager.openGattServer(this, mBluetoothGattServerCallback);
        }
        mBluetoothGattServer.addService(service);
    }
}










