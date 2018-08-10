package com.lansent.cannan.api;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.ParseException;
import android.os.NetworkOnMainThreadException;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonParseException;
import com.lansent.cannan.util.Log;
import com.lansent.cannan.util.Utils;
import com.lansent.cannan.watcher.WatcherActivity;

import org.json.JSONException;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import retrofit2.HttpException;

/**
 * Description    :RxAndroid 网络请求结果中转处理
 * CreateAuthor: Cannan
 * Create time   : 2017/9/30 0030     上午 10:41
 */

public abstract class BaseSub<T extends BaseResponse<?>>
		implements Subscriber<T>, IApiCallback<T> {


	/**
	 * 日志打印标签
	 */
	private String TAG = getClass().getSimpleName();

	/**
	 * 结果处理回调接口
	 */
	IApiCallback<T> iApiBack;
	private AlertDialog dialog;

	/**
	 * 构造方法，同时启动onStart方法，用于通知UI更新，表示请求已经发起
	 * 回调接口，需指定泛型参数，解析时以传入的泛型为参考依据
	 */
	public BaseSub() {
		iApiBack = this;
		iApiBack.callStart();
	}

	@Override
	public void onSubscribe(Subscription s) {
		s.request(Integer.MAX_VALUE);
	}


	@Override
	public void onNext(T response) {
		if (response.isSuccess()) {
			this.iApiBack.callSuccess(response);
		} else {
			this.iApiBack.CallFailure(response);
		}
	}

	@Override
	public void onError(Throwable t) {
		Log.i(TAG);
		if (t == null) {
			iApiBack.callError("未知错误");
		} else if (t instanceof NetworkOnMainThreadException) {
			iApiBack.callError("在主线程中发起网络请求，未指定rx订阅线程");
		} else if (t.getMessage().endsWith("No address associated with hostname")) {
			iApiBack.callError("服务器地址错误");
		} else {
			iApiBack.callError(getErrorMsg(t));
		}
		onComplete();
	}


	private WindowManager wm;
	private WindowManager.LayoutParams params;
	private Button button;

	@Override
	public final void onComplete() {
		this.iApiBack.callComplete();

		if (!ApiManager.getConfig().isStetho()) {
			return;
		}

		Log.i(TAG);
//		showDialog();
		showView();
	}

	private void showView() {
		Log.i(TAG);
		setWatcher();

//		if(Settings.canDrawOverlays(Utils.getApp())){
//			setWatcher();
//		}else{
//			Toast.makeText( Utils.getApp(),"未开启悬浮窗权限！", Toast.LENGTH_SHORT).show();
//			//没有悬浮窗权限m,去开启悬浮窗权限
//			try{
//				Intent  intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//				Utils.getApp().startActivity(intent);
//			}catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			//默认有悬浮窗权限  但是 华为, 小米,oppo等手机会有自己的一套Android6.0以下  会有自己的一套悬浮窗权限管理 也需要做适配
//		}
	}

	private void setWatcher() {
		if (wm == null) {
			wm = (WindowManager) Utils.getApp().getSystemService(Context.WINDOW_SERVICE);
			WindowManager.LayoutParams dialogParam = new WindowManager.LayoutParams();
			// 设置window type
			dialogParam.type = WindowManager.LayoutParams.TYPE_TOAST;
			dialogParam.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
			// 设置Window flag
			dialogParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			dialogParam.gravity = Gravity.RIGHT;
			dialogParam.width = WindowManager.LayoutParams.WRAP_CONTENT;
			dialogParam.height = WindowManager.LayoutParams.WRAP_CONTENT;
			params = dialogParam;
			button = new Button(Utils.getApp());
			button.setAlpha(0.5f);
			button.setText("抓包");
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					wm.removeView(button);
					Intent intent = new Intent(Utils.getApp(), WatcherActivity.class);
					intent.putParcelableArrayListExtra("response", ApiManager.getInstance().getList());
					Utils.getApp().startActivity(intent);
				}
			});
			wm.addView(button, dialogParam);
		} else {
			wm.updateViewLayout(button, params);
		}
		Toast.makeText( Utils.getApp(),"抓捕到新的网络请求！", Toast.LENGTH_SHORT).show();
	}

	private void showDialog() {
		if (dialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Utils.getApp());
			builder.setTitle("抓捕到新的网络请求")
					.setMessage("是否进入查看？")
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent(Utils.getApp(), WatcherActivity.class);
					intent.putParcelableArrayListExtra("response", ApiManager.getInstance().getList());
					Utils.getApp().startActivity(intent);
				}
			});
			dialog = builder.create();
			Window window = dialog.getWindow();
			window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			WindowManager.LayoutParams layoutParams = window.getAttributes();
			layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
			layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
			dialog.onWindowAttributesChanged(layoutParams);
			//设置弹出框失去焦点是否隐藏,即点击屏蔽其它地方是否隐藏
			dialog.setCanceledOnTouchOutside(true);
			//设置按钮是否可以按返回键取消,false则不可以取消
			dialog.setCancelable(true);
		}
		dialog.show();
	}

	//对应HTTP的状态码
	private final int UNAUTHORIZED = 401;
	private final int FORBIDDEN = 403;
	private final int NOT_FOUND = 404;
	private final int REQUEST_TIMEOUT = 408;
	private final int INTERNAL_SERVER_ERROR = 500;
	private final int BAD_GATEWAY = 502;
	private final int SERVICE_UNAVAILABLE = 503;
	private final int GATEWAY_TIMEOUT = 504;

	/**
	 * 获取请求错误处理后的信息，展示给UI
	 *
	 * @param throwable
	 * @return
	 */
	private String getErrorMsg(Throwable throwable) {
		if (throwable instanceof SocketTimeoutException ||
				throwable instanceof TimeoutException) {
			return "网络请求超时，请稍后重试";
		}
		if (throwable instanceof HttpException) {             //HTTP错误
			HttpException httpException = (HttpException) throwable;
			Log.e(TAG, "error code = " + httpException.code());
			switch (httpException.code()) {
				case UNAUTHORIZED:
					return "证书认证错误";
				case FORBIDDEN:
					return "网页禁止访问";
				case NOT_FOUND:
					return "网页找不到";
				case REQUEST_TIMEOUT:
					return "请求超时";
				case GATEWAY_TIMEOUT:
					return "请求超时";
				case INTERNAL_SERVER_ERROR:
					return "服务器异常";
				case BAD_GATEWAY:
					return "网关错误";
				case SERVICE_UNAVAILABLE:
					return "服务不可用";
				default:
					return "网络错误";  //均视为网络错误
			}
		}

		if (throwable instanceof JsonParseException
				|| throwable instanceof JSONException
				|| throwable instanceof ParseException) {
			return "解析错误";            //均视为解析错误
		}

		if (throwable instanceof ConnectException) {
			return "连接失败";  //均视为网络错误
		}
		return throwable.getMessage();          //未知错误
	}

	@Override
	public void callStart() {
	}


	@Override
	public void callComplete() {
	}

}
