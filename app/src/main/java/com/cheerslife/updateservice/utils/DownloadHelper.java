package com.cheerslife.updateservice.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.cheerslife.updateservice.App;

import java.io.File;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class DownloadHelper {

    private DownloadManager mDownloadManager;
    private long mDownloadId;
    private String mFilePath;
    private String mAPKName;

    private CallBack mCallBack;
    private static DownloadHelper mHelper;

    private DownloadHelper() {

    }

    public static DownloadHelper instance() {
        if (mHelper == null) {
            mHelper = new DownloadHelper();
        }
        return mHelper;
    }

    //下载apk
    public void downloadAPK(String url, String name, CallBack back) {
        mCallBack = back;
        if (FileUtils.delete(new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + name))) {
            Log.e("Tag", "downloadAPK: 删除成功");
        } else {
            Log.e("Tag", "downloadAPK: 删除失败");
        }
        mAPKName = name;
        Log.e("Tag", "downloadAPK: 开始下载");
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(true);

        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setMimeType("application/vnd.android.package-archive");
        request.setTitle("新版本Apk");
        request.setDescription("正在下载安装包...");
        request.setVisibleInDownloadsUi(true);

        mFilePath = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();
        //设置下载的路径
        request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, mAPKName);

        //获取DownloadManager
        mDownloadManager = (DownloadManager) App.getApp().getSystemService(Context.DOWNLOAD_SERVICE);
        if (mDownloadManager != null) {
            //将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
            mDownloadId = mDownloadManager.enqueue(request);

            //注册广播接收者，监听下载状态
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
            App.getApp().registerReceiver(receiver, filter);
        }

        /*Uri uri = Uri.parse("content://downloads/all_downloads/" + mDownloadId);;
        VLog.e("Tag", "uri: " + uri.toString());
        mActivity.getContentResolver().registerContentObserver(uri, true, new DownloadContentObserver());*/
    }

    //广播监听下载的各个状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
                Log.e("Tag", "onReceive: Clicked");
            } else {
                checkStatus();
            }
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Log.e("Tag", "当前进度: " + ((int) (msg.arg1 * 100f / msg.arg2)) + "%" + "status: " + msg.obj);
            }
        }
    };

    class DownloadContentObserver extends ContentObserver {
        DownloadContentObserver() {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateView();
        }

    }

    private void updateView() {
        int[] bytesAndStatus = getBytesAndStatus(mDownloadId);
        int currentSize = bytesAndStatus[0];//当前大小
        int totalSize = bytesAndStatus[1];//总大小
        int status = bytesAndStatus[2];//下载状态
        Message.obtain(handler, 0, currentSize, totalSize, status).sendToTarget();
    }

    private int[] getBytesAndStatus(long downloadId) {
        int[] bytesAndStatus = new int[]{-1, -1, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        try (Cursor c = mDownloadManager.query(query)) {
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } catch (Exception ignored) {
        }
        return bytesAndStatus;
    }


    //检查下载状态
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        //通过下载的id查找
        query.setFilterById(mDownloadId);
        Cursor c = mDownloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                //下载暂停
                case DownloadManager.STATUS_PAUSED:
                    Log.e("Tag", "checkStatus: 下载暂停");
                    break;
                //下载延迟
                case DownloadManager.STATUS_PENDING:
                    Log.e("Tag", "checkStatus: 下载延迟");
                    break;
                //正在下载
                case DownloadManager.STATUS_RUNNING:
                    Log.e("Tag", "checkStatus: 下载中");
                    break;
                //下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.e("Tag", "checkStatus: 下载完成");
                    downloadApkSuccess();
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    Log.e("Tag", "checkStatus: 下载失败");
                    downloadApkFail();
                    break;
            }
        }
        c.close();
    }

    private void downloadApkSuccess() {
        mCallBack.downApkSuccess(mFilePath, mAPKName);
    }

    private void downloadApkFail() {
        mCallBack.downApkFail();
    }

    public interface CallBack {
        void downApkSuccess(String path, String apkName);

        void downApkFail();
    }


}