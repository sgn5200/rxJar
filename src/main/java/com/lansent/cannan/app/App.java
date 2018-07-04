package com.lansent.cannan.app;

import android.app.Application;
import android.os.StrictMode;

import com.lansent.cannan.api.ApiManager;
import com.lansent.cannan.util.Utils;

/**
 * Created by Cannan on 2017/9/26 0026.
 * 全局 Application
 * <p>
 * 初始化 bugly 、Stetho
 */

public class App extends Application {

	private static App mContext;
	private String TAG = getClass().getSimpleName();

	public static App getInstance() {
		return mContext;
	}


	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;

		Utils.init(this, ApiManager.getConfig().setStetho(true));  //初始化工具包
		providerUri();         //不检查Uri暴露
	}

	/**
	 * 处理7.0 Uri安全检查
	 */
	private void providerUri() {
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
	}
}
