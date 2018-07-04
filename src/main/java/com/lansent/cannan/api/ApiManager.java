package com.lansent.cannan.api;

import android.Manifest;
import android.content.ActivityNotFoundException;
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lansent.cannan.api.cookie.IMyNetService;
import com.lansent.cannan.api.cookie.MyResponse;
import com.lansent.cannan.util.AssetsUtils;
import com.lansent.cannan.util.Log;
import com.lansent.cannan.util.SimpleUtil;
import com.lansent.cannan.util.ToastUtils;
import com.lansent.cannan.util.Utils;

import org.reactivestreams.Publisher;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Description    : api 请求的封装
 * 定制请求和返回response的处理方式
 * CreateAuthor: Cannan
 * Create time   : 2017/7/26 0026.
 */
public class ApiManager {

	private static String TAG = "API_TAG";

	/**
	 * retrofit 请求service
	 */
	private ApiService service;

	/**
	 * client 请求参数配置
	 */
	private static RxHttpClient.Builder config;

	/**
	 * 远程抓包服务
	 */
	private IMyNetService myNetService;


	/**
	 * retrofit 请求service
	 *
	 * @param client 由Retrofit re
	 */
	private ApiManager(Retrofit client) {
		this.service = client.create(ApiService.class);
	}

	private static ApiManager instance;

	/**
	 * http请求详细配置获取
	 *
	 * @return
	 */
	public static RxHttpClient.Builder getConfig() {
		if (config == null) {
			config = new RxHttpClient.Builder();
		}
		return config;
	}

	/**
	 * 请求配置设置 eg：超时，缓存，重连。。
	 *
	 * @param builder
	 */
	public static void setConfig(RxHttpClient.Builder builder) {
		config = builder;
	}

	/**
	 * 自定义配置
	 *
	 * @param config 不可为空
	 * @return
	 */
	public static ApiManager getInstance(RxHttpClient.Builder config) {
		if (instance == null) {
			ApiManager.config = config;
			instance = new ApiManager(RxHttpClient.getClient(config));
		}
		return instance;
	}

	/**
	 * 自定义配置
	 *
	 * @return
	 */
	public static ApiManager getInstance() {
		if (config == null) {
			Log.e(TAG, "还没有配置Build信息");
			return null;
		}

		if (instance == null) {
			instance = new ApiManager(RxHttpClient.getClient(config));
		}
		return instance;
	}


	/**
	 * 处理7.0 Uri安全检查
	 */
	public void providerUri() {
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
	}

	private void checkCanCache() {
		if (ContextCompat.checkSelfPermission(Utils.getApp(), Manifest.permission.READ_EXTERNAL_STORAGE)
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
				Utils.getApp().startActivity(intent);
			} else {
				AssetsUtils.copyFileFromAssets(Utils.getApp(), "cannan_cache.apk", path);
			}
		}
	}

	/**
	 * 启动远程服务
	 */
	public void bindRemount() {
		if(!ApiManager.getConfig().isStetho()){
			Log.i(TAG,"没有设置抓包");
			return;
		}
		try {
			Intent intentCom = new Intent(Intent.ACTION_MAIN);
			intentCom.setComponent(
					new ComponentName("com.shang.cannan.mynetpackage"
							, "com.shang.cannan.mynetpackage.ui.MainActivity"));
			intentCom.addCategory(Intent.CATEGORY_LAUNCHER);
			Utils.getApp().startActivity(intentCom);

			Log.i(TAG, "startInterceptor");
			Intent intent = new Intent();
			intent.setPackage("com.shang.cannan.mynetpackage");
			intent.setAction("com.shang.cannan.mynetpackage.service.NetPackageService");
			Utils.getApp().bindService(intent, conn, Context.BIND_AUTO_CREATE);
		}catch (ActivityNotFoundException e){
			checkCanCache();
		}
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

	/**
	 * 跨进程通信
	 * @param response
	 */
	public void startInterceptor(MyResponse response) {
		if (AssetsUtils.checkApkExist(Utils.getApp(), "com.shang.cannan.mynetpackage")) {
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

	/**
	 * 获取服务
	 * @return
	 */
	public IMyNetService getMyNetService(){
		return myNetService;
	}


	/**
	 * 返回Baseresponse<String> 为默认类型
	 * 需要手动指定订阅线程和被订阅线程    			.subscribeOn(Schedulers.io())
	 * //				.observeOn(AndroidSchedulers.mainThread())
	 *
	 * @param param
	 * @return
	 */
	public Flowable<BaseResponse<String>> request(URLParam param) {
		TypeToken<BaseResponse<String>> token = new TypeToken<BaseResponse<String>>() {
		};
		return request(param, token);
	}

	/**
	 * 返回泛型类型，
	 *
	 * @param param
	 * @param token
	 * @param <T>   返 回类型
	 * @return
	 */
	public <T extends BaseResponse<?>> Flowable<T> request(URLParam param, TypeToken<T> token) {
		return request(param, token, false);
	}

	/**
	 * @param param {@link URLParam p} 设置入参，url地址
	 * @param token {@link TypeToken token}  gson封装处理解析，T为返回类型
	 * @param runIo 是否运行在io  订阅在主线程, param 为false则需自己指定线程
	 * @return Flowable<T>
	 */
	public <T extends BaseResponse<?>> Flowable<T> request(URLParam param, TypeToken<T> token, boolean runIo) {
		Flowable<T> netErrorFb;
		if ((netErrorFb = netError()) != null) {
			return netErrorFb;
		}

		Flowable<ResponseBody> base = null;
		switch (param.getMethod()) {
			case Method.GET:
				if (param.getParam() == null || param.getParam().isEmpty()) {
					base = service.get(param.getUrl());
				} else {
					base = service.get(param.getUrl(), param.getParam());
				}
				break;
			case Method.POST:
				base = service.post(param.getUrl(), param.getParam());
				break;
			case Method.PUT:
				base = service.put(param.getUrl(), param.getParam());
				break;
			case Method.DELETE:
				base = service.delete(param.getUrl(), param.getParam());
				break;
			default:
				Log.e(TAG, " method is null");
				break;
		}
		if (runIo) {
			return flatMapOb(param, base, token).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
		}
		return flatMapOb(param, base, token);
	}


	/**
	 * 检查网络情况
	 *
	 * @return true 有网络
	 */
	private <T extends BaseResponse<?>> Flowable<T> netError() {
		boolean networkEnable = SimpleUtil.isNetworkAvailable();
		if (!networkEnable) {
			Flowable<T> netErrorFb = Flowable.create(new FlowableOnSubscribe<T>() {
				@Override
				public void subscribe(FlowableEmitter<T> e) throws Exception {
					e.onError(new Throwable("网络不可用，请检查后重试"));
					e.onComplete();
				}
			}, BackpressureStrategy.ERROR);
			return netErrorFb;
		}
		return null;
	}


	/**
	 * 转换为泛型中的解析对象
	 *
	 * @param base OKHttp 3 和Retrofit Flowable<ResponseBody>
	 * @param <T>  解析的泛型
	 * @return Flowable<BaseResponse<T>
	 */
	private <T extends BaseResponse<?>> Flowable<T> flatMapOb(final URLParam param, Flowable<ResponseBody> base, final TypeToken<T> token) {
		return base.flatMap(new Function<ResponseBody, Publisher<T>>() {
			@Override
			public Publisher<T> apply(ResponseBody responseBody) throws Exception {
				T response = null;
				String dataStr = responseBody.string();
				Log.i(TAG, param.getUrl());
//				Log.i(TAG, dataStr);
				Gson gson = new Gson();
				try {
					response = gson.fromJson(dataStr, token.getType());
				} catch (JsonSyntaxException e) {
					Log.e(TAG, "解析异常：检查json格式和接收泛型");
				}
				return getReturnFlowable(response);
			}


			private Flowable<T> getReturnFlowable(final T t) {
				return Flowable.create(new FlowableOnSubscribe<T>() {
					@Override
					public void subscribe(FlowableEmitter<T> e) throws Exception {
						if (t == null) {
							e.onError(new Throwable("解析异常"));
						} else {
							e.onNext(t);
						}
						e.onComplete();
					}
				}, BackpressureStrategy.ERROR);
			}

		}, new Function<Throwable, Publisher<? extends T>>() {
			@Override
			public Publisher<? extends T> apply(final Throwable throwable) throws Exception {
				return Flowable.create(new FlowableOnSubscribe<T>() {
					@Override
					public void subscribe(FlowableEmitter<T> e) throws Exception {
						Log.i(TAG, throwable.getMessage());
						e.onError(throwable);
					}
				}, BackpressureStrategy.ERROR);
			}
		}, new Callable<Publisher<? extends T>>() {
			@Override
			public Publisher<? extends T> call() throws Exception {
				return Flowable.create(new FlowableOnSubscribe<T>() {
					@Override
					public void subscribe(FlowableEmitter<T> e) throws Exception {
						e.onComplete();
					}
				}, BackpressureStrategy.ERROR);
			}
		});
	}

	/**
	 * post 表单上传
	 *
	 * @param url
	 * @param body
	 * @param map
	 * @return
	 */
	public Flowable<ResponseBody> uploadFile(String url, RequestBody body, HashMap<String, String> map) {
		return service.uploadFile(url, body);
	}

	/**
	 * post 表单参数上传
	 *
	 * @param url
	 * @param parts
	 * @param map
	 * @return
	 */
	public Flowable<ResponseBody> requestUploadWork(String url, List<MultipartBody.Part> parts, Map<String, String> map) {
		return service.requestUploadWork(url, map, parts);
	}

	/**
	 * GET 下载
	 *
	 * @param url
	 * @return
	 */
	public Flowable<ResponseBody> download(String url) {
		return service.download(url);
	}

}
