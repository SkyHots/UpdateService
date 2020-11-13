package com.cheerslife.updateservice.updateservice;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cheerslife.updateservice.App;
import com.cheerslife.updateservice.C;
import com.cheerslife.updateservice.R;
import com.cheerslife.updateservice.utils.HttpUtil;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText mIp;
    private EditText mPort;
    private Intent mService;

    private String[] data = {"床头屏", "门口屏", "走廊屏"};
    private TextView mDeviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "onCreate: MainActivity");

        initView();
        mService = new Intent(this, ApkService.class);
        startService(mService);

        int i = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (i == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    private void initView() {
        mIp = findViewById(R.id.ip_input);
        mPort = findViewById(R.id.port_input);
        mDeviceType = findViewById(R.id.spinner);

        mIp.setText(getIpPort(C.IP, C.DEFAULT_IP));
        mPort.setText(getIpPort(C.PORT, C.DEFAULT_PORT));

        int indexPicked = SPUtils.getInstance().getInt(C.DEVICE_TYPE);
        if (indexPicked != -1) {
            mDeviceType.setText(data[indexPicked - 1]);
        }
    }

    private String getIpPort(String ip2, String defaultIp) {
        String ip = SPUtils.getInstance().getString(ip2);
        ip = TextUtils.isEmpty(ip) ? defaultIp : ip;
        return ip;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void save(View view) {
        if (TextUtils.isEmpty(mDeviceType.getText().toString())){
            ToastUtils.showLong("请选择设备类型");
            return;
        }

        String ip = mIp.getText().toString();
        String port = mPort.getText().toString();
        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)) {
            ToastUtils.showShort(getString(R.string.inputIpOrPort));
        } else {
            SPUtils.getInstance().put(C.IP, ip);
            SPUtils.getInstance().put(C.PORT, port);
            ToastUtils.showShort(getString(R.string.saveSuccess));
        }

        startService(mService);
        App.initRabbit();
    }

    public void finish(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    public void test(View view) {
        String url = "http://" + getIpPort(C.IP, C.DEFAULT_IP) + ":" + getIpPort(C.PORT, C.DEFAULT_PORT) + "/api/iotCenter" +
                "/Screen/GetServerTime";
        HttpUtil.sendHttpRequest(url, new HttpUtil.HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                ToastUtils.showLong("连接" + getIpPort(C.IP, C.DEFAULT_IP) + "成功");
            }

            @Override
            public void onError(Exception e) {
                ToastUtils.showLong("连接" + getIpPort(C.IP, C.DEFAULT_IP) + "失败");
            }
        });

    }

    public void pickDeviceType(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int deviceType = SPUtils.getInstance().getInt(C.DEVICE_TYPE);
        builder.setSingleChoiceItems(data, deviceType != -1 ? deviceType - 1 : -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDeviceType.setText(data[which]);
                SPUtils.getInstance().put(C.DEVICE_TYPE, which + 1);
                dialog.dismiss();
            }
        });
        builder.show();

    }
}
