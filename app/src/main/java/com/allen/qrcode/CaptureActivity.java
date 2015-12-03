package com.allen.qrcode;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.myqrcode.R;
import com.allen.qrcode.db.DatabaseUtil;
import com.allen.qrcode.db.GreenDaoUtils;
import com.allen.qrcode.sweetalertdemo.sweetalert.SweetAlertDialog;
import com.allen.qrcode.zxing.camera.CameraManager;
import com.allen.qrcode.zxing.control.AmbientLightManager;
import com.allen.qrcode.zxing.control.BeepManager;
import com.allen.qrcode.zxing.decode.BitmapDecoder;
import com.allen.qrcode.zxing.decode.CaptureActivityHandler;
import com.allen.qrcode.zxing.decode.InactivityTimer;
import com.allen.qrcode.zxing.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;

public final class CaptureActivity extends Activity implements
		SurfaceHolder.Callback {
	private Button btn_back;
	private TextView button_lamp;
	private LinearLayout btn_lamp;
	private LinearLayout btn_history;
	private LinearLayout btn_picture;

	private boolean isTorchOn = false;
	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	private InactivityTimer inactivityTimer;

	private BeepManager beepManager;
	private AmbientLightManager ambientLightManager;
	private boolean vibrate;// 震动
	private DatabaseUtil dbUtil;

	private Button setBtn;
	private static final int REQUEST_CODE = 100;
	private static final int PARSE_BARCODE_FAIL = 300;
	private static final int PARSE_BARCODE_SUC = 200;
	private static final int PHOTO_REQUEST_GALLERY = 400;
	private static final int PHOTO_REQUEST_CUT = 500;

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	/**
	 * 图片的路径
	 */
	private String photoPath;

	private Handler mHandler = new MyHandler(this);
	GreenDaoUtils greenDaoUtils;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 取消标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 全屏
		// Window window = getWindow();
		// window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.capture);
		greenDaoUtils = new GreenDaoUtils(this);


		setBtn = (Button) findViewById(R.id.btn_set);
		setBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(CaptureActivity.this, SetingActivity.class);
				startActivity(intent);
			}
		});
		btn_lamp = (LinearLayout) findViewById(R.id.linearlayout_lamp);
		btn_history = (LinearLayout) findViewById(R.id.linearlayout_history);
		btn_picture = (LinearLayout) findViewById(R.id.linearlayout_picture);

		btn_lamp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playVibrate();
				// 开关灯
				if (isTorchOn) {
					isTorchOn = false;
					cameraManager.setTorch(false);
				} else {
					isTorchOn = true;
					cameraManager.setTorch(true);
				}
			}
		});
		btn_history.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				playVibrate();
				Intent intent = new Intent();
				intent.setClass(CaptureActivity.this, HistoryActivity.class);
				startActivity(intent);
			}
		});

		btn_picture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				playVibrate();
				// 打开手机中的相册
				Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
				innerIntent.setType("image/*");
				Intent wrapperIntent = Intent.createChooser(innerIntent,
						"选择二维码图片");
				CaptureActivity.this.startActivityForResult(wrapperIntent,
						PHOTO_REQUEST_GALLERY);
			}
		});

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);
		ambientLightManager = new AmbientLightManager(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		handler = null;
		resetStatusView();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		beepManager.updatePrefs();
		ambientLightManager.start(cameraManager);

		inactivityTimer.onResume();
		vibrate = true;
		decodeFormats = null;
		characterSet = null;

	}

	private void playVibrate() {

		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(VIBRATE_DURATION);

	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		viewfinderView.recycleLineDrawable();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
			case KeyEvent.KEYCODE_CAMERA:// 拦截相机键
				return true;
			case KeyEvent.KEYCODE_BACK:
				AnimationUtil.finishActivityAnimation(CaptureActivity.this);
				;

				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler,
						R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {

	}

	/** 结果处理 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();
		beepManager.playBeepSoundAndVibrate();

		String msg = rawResult.getText();
		int type;
		if (msg == null || "".equals(msg)) {
			msg = "无法识别";
		}
		if (Utils.isURL(msg)) {
			type = 1;
		} else {
			type = 0;
		}
		playBeepSoundAndVibrate();// 扫描后震动提示

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
		String time = simpleDateFormat.format(new Date());

		greenDaoUtils.insertGreenDao(msg, type, time);

		Intent intent = new Intent(CaptureActivity.this, ShowActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("msg", msg);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	/**
	 * 扫描后震动提示
	 */
	private static final long VIBRATE_DURATION = 50;

	private void playBeepSoundAndVibrate() {

		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			return;
		}
		if (cameraManager.isOpen()) {
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		// AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// builder.setTitle("警告");
		// builder.setCancelable(false);
		// builder.setMessage(R.string.waring);
		// builder.setPositiveButton("确定", new FinishListener(this));
		// builder.setOnCancelListener(new FinishListener(this));
		// builder.show();
		// new SweetAlertDialog(this).show();
		new SweetAlertDialog(this)
				.setContentText(getResources().getString(R.string.waring))
				.setConfirmClickListener(
						new SweetAlertDialog.OnSweetClickListener() {

							@Override
							public void onClick(
									SweetAlertDialog sweetAlertDialog) {
								// TODO Auto-generated method stub
								finish();
							}
						}).show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
		viewfinderView.setVisibility(View.VISIBLE);
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
									Intent intent) {

		if (resultCode == RESULT_OK) {
			final ProgressDialog progressDialog;
			switch (requestCode) {
				case PHOTO_REQUEST_GALLERY:
					// 从相册返回的数据
					if (intent != null) {
						// 得到图片的全路径
						Uri uri = intent.getData();
						crop(uri);
					}
					break;

				case PHOTO_REQUEST_CUT:
					// 从剪切图片返回的数据
					try {
						if (intent != null) {
							progressDialog = new ProgressDialog(this);
							progressDialog.setMessage("正在扫描...");
							progressDialog.setCancelable(false);
							progressDialog.show();

							Bitmap img = intent.getParcelableExtra("data");
							// Bitmap img =
							// BitmapUtils.getCompressedBitmap(photoPath);

							BitmapDecoder decoder = new BitmapDecoder(
									CaptureActivity.this);
							Result result = decoder.getRawResult(img);

							if (result != null) {
								Message m = mHandler.obtainMessage();
								m.what = PARSE_BARCODE_SUC;
								m.obj = ResultParser.parseResult(result).toString();
								Success(m);
								// mHandler.sendMessage(m);
							} else {
								// new
								// SweetAlertDialog(CaptureActivity.this).setContentText("解析图片失败").show();
								Message m = mHandler.obtainMessage();
								m.what = PARSE_BARCODE_FAIL;
								mHandler.sendMessage(m);
							}

							progressDialog.dismiss();

						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case REQUEST_CODE:

					// 获取选中图片的路径
					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor cursor = getContentResolver().query(intent.getData(),
							proj, null, null, null);
					if (cursor.moveToFirst()) {
						photoPath = cursor.getString(cursor
								.getColumnIndex(MediaStore.Images.Media.DATA));
					}
					cursor.close();

					progressDialog = new ProgressDialog(this);
					progressDialog.setMessage("正在扫描...");
					progressDialog.setCancelable(false);
					progressDialog.show();

					new Thread(new Runnable() {

						@Override
						public void run() {

							Bitmap img = BitmapUtils.getCompressedBitmap(photoPath);

							BitmapDecoder decoder = new BitmapDecoder(
									CaptureActivity.this);
							Result result = decoder.getRawResult(img);

							if (result != null) {
								Message m = mHandler.obtainMessage();
								m.what = PARSE_BARCODE_SUC;
								m.obj = ResultParser.parseResult(result).toString();
								Success(m);
								// mHandler.sendMessage(m);
							} else {
								// new
								// SweetAlertDialog(CaptureActivity.this).setContentText("解析图片失败").show();
								Message m = mHandler.obtainMessage();
								m.what = PARSE_BARCODE_FAIL;
								mHandler.sendMessage(m);
							}

							progressDialog.dismiss();

						}
					}).start();

					break;

			}
		}

	}

	public static class MyHandler extends Handler {

		private WeakReference<Activity> activityReference;

		public MyHandler(Activity activity) {
			activityReference = new WeakReference<Activity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case PARSE_BARCODE_SUC: // 解析图片成功
					Toast.makeText(activityReference.get(), "解析成功，结果为：" + msg.obj,
							Toast.LENGTH_SHORT).show();

					break;

				case PARSE_BARCODE_FAIL:// 解析图片失败
					new SweetAlertDialog(activityReference.get()).setContentText(
							"所选图片中未检出有二维码信息").show();
					// Toast.makeText(activityReference.get(), "解析图片失败",
					// Toast.LENGTH_SHORT).show();
					break;

				default:
					break;
			}

			super.handleMessage(msg);
		}

	}

	public void Success(Message msg) {
		int type = 0;
		String msgString = msg.obj.toString();
		if (Utils.isURL(msgString)) {
			type = 1;
		} else {
			type = 0;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
		String time = simpleDateFormat.format(new Date());
		greenDaoUtils.insertGreenDao(msgString, type, time);
		Intent intent = new Intent(CaptureActivity.this, ShowActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("msg", msgString);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	/*
	 * 剪切图片
	 */
	private void crop(Uri uri) {
		// 裁剪图片意图
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// 裁剪框的比例，1：1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);

		intent.putExtra("outputFormat", "JPEG");// 图片格式
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
		intent.putExtra("return-data", true);
		// 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
		startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

}
