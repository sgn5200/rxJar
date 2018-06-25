package com.lansent.cannan.app;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;

import com.lansent.cannan.api.cookie.IMyNetService;
import com.lansent.cannan.api.cookie.MyResponse;
import com.lansent.cannan.util.AssetsUtils;
import com.lansent.cannan.util.Log;
import com.lansent.cannan.util.ToastUtils;
import com.lansent.cannan.util.Utils;

import java.io.File;

/**
 * Created by Cannan on 2017/9/26 0026.
 * 全局 Application
 * <p>
 * 初始化 bugly 、Stetho
 */

public class App extends Application {

	private static App mContext;
	private String TAG = getClass().getSimpleName();
	private IMyNetService myNetService;

	public static App getInstance() {
		return mContext;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;

		Utils.init(this);
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());

		bindRemout();
	}

	private void checkCanCache() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {  //没有权限
			ToastUtils.showLong("没有存储权限");
//			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 666);
		} else { //有权限
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
					+ "cannan" + File.separator + "cannan_cache.apk";
			Log.i(TAG, path);
			File file = new File(path);
			if (file.exists()) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.parse("file://" + path),
						"application/vnd.android.package-archive");
				mContext.startActivity(intent);
			} else {
				AssetsUtils.copyFileFromAssets(this, "cannan_cache.apk", path);
			}
		}
	}

	private void bindRemout() {
		Log.i(TAG, "startInterceptor");
		Intent intent = new Intent();
		intent.setPackage("com.shang.cannan.mynetpackage");
		intent.setAction("com.shang.cannan.mynetpackage.service.NetPackageService");
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}


	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myNetService = IMyNetService.Stub.asInterface(service);
			Log.i(TAG, "onServiceConnected");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "onServiceDisconnected");
			myNetService = null;

		}
	};

	public void startInterceptor(MyResponse response) {
		if (AssetsUtils.checkApkExist(this, "com.shang.cannan.mynetpackage")) {
			Log.i(TAG, "startInterceptor");
			if (myNetService != null) {
				try {
					myNetService.addResponse(response);
					myNetService.status(300);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		} else {
			checkCanCache();
		}
	}

}
