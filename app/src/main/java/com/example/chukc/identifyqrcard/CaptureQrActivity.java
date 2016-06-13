package com.example.chukc.identifyqrcard;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chukc.identifyqrcard.camera.CameraManager;
import com.example.chukc.identifyqrcard.decode.CaptureQrActivityHandler;

import java.io.IOException;


/**
 *
 * 鎻忚堪: 浜岀淮鐮佹壂鎻忕晫闈�
 */
public class CaptureQrActivity extends Activity implements Callback {

	private CaptureQrActivityHandler handler;
	private boolean hasSurface;
	// private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.50f;
	private boolean vibrate;
	private int x = 0;
	private int y = 0;
	private int cropWidth = 0;
	private int cropHeight = 0;
	private LinearLayout linear_capture = null;
	private RelativeLayout relative_container = null, rl_capture,rl_capturetop;
	private boolean isNeedCapture = false;

	private String job, complay, source, name, cardid;
	private int jobid, memberid, OrgLibID,LimitAppNum;

	private TextView tv_inputbyhand;
	private EditText et_code;
	private LinearLayout lin_code;

	private RelativeLayout rl_sure;

	private InputMethodManager imm;
	public boolean isNeedCapture() {
		return isNeedCapture;
	}

	public void setNeedCapture(boolean isNeedCapture) {
		this.isNeedCapture = isNeedCapture;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getCropWidth() {
		return cropWidth;
	}

	public void setCropWidth(int cropWidth) {
		this.cropWidth = cropWidth;
	}

	public int getCropHeight() {
		return cropHeight;
	}

	public void setCropHeight(int cropHeight) {
		this.cropHeight = cropHeight;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		hasSurface = false;
		setContentView(R.layout.qrcode_scan);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		System.out.println("==================onStart");

		// 鍒濆鍖� CameraManager
		CameraManager.init(getApplication());

		// inactivityTimer = new InactivityTimer(this);

		linear_capture = (LinearLayout) findViewById(R.id.linear_capture);
		relative_container = (RelativeLayout) findViewById(R.id.relative_container);
		rl_capture = (RelativeLayout) findViewById(R.id.rl_capture);
		rl_capturetop = (RelativeLayout) findViewById(R.id.rl_capturetop);
		tv_inputbyhand = (TextView) findViewById(R.id.tv_inputbyhand);
		et_code = (EditText) findViewById(R.id.et_code);
		lin_code = (LinearLayout) findViewById(R.id.lin_code);
		rl_sure = (RelativeLayout) findViewById(R.id.rl_sure);

		tv_inputbyhand.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				lin_code.setVisibility(View.VISIBLE);
				et_code.setFocusable(true);
				et_code.setFocusableInTouchMode(true);
				et_code.requestFocus();
				imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.showSoftInput(et_code,InputMethodManager.RESULT_SHOWN);
				imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
				v.setVisibility(View.GONE);
			}
		});



		ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
		TranslateAnimation mAnimation = new TranslateAnimation(
				TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
				0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.0f);
		mAnimation.setDuration(3000);
		mAnimation.setRepeatCount(-1);
		mAnimation.setRepeatMode(Animation.RESTART);
		mAnimation.setInterpolator(new LinearInterpolator());
		mQrLineView.setAnimation(mAnimation);

		getKeybordHeight();
	}

	boolean flag = true;

	protected void light() {
		if (flag == true) {
			flag = false;
			// 寮�闂厜鐏�
			CameraManager.get().openLight();
		} else {
			flag = true;
			// 鍏抽棯鍏夌伅
			CameraManager.get().offLight();
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("=====================onresume");
		System.out.println("hasSurface======================" + hasSurface);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		// continuePreview(surfaceView,surfaceHolder);
		if (hasSurface) {
			initCamera(surfaceHolder);
			System.out.println("onresume=========initCamera===========");
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;


	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
        if(imm!=null)
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	protected void onDestroy() {
		System.out.println("===========onDestroy");
		// inactivityTimer.shutdown();
		super.onDestroy();
	}

	public void handleDecode(String result) {
		// inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		new Toast(this).makeText(this, result, Toast.LENGTH_LONG).show();;
		// new ToastShow(this, application.getApplicationId()).show();
		// 杩炵画鎵弿锛屼笉鍙戦�佹娑堟伅鎵弿涓�娆＄粨鏉熷悗灏变笉鑳藉啀娆℃壂鎻�
		// handler.sendEmptyMessage(R.id.restart_preview);

	}



	/**
	 * 鍔犺浇鐩告満
	 *
	 * @param surfaceHolder
	 */
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {

			System.out.println("==================鍔犺浇鐩告満");
			boolean isOpen=CameraManager.get().openDriver(surfaceHolder);
			if(!isOpen){
				new Toast(this).makeText(this, "请开启相机权限", Toast.LENGTH_LONG).show();;
				CaptureQrActivity.this.finish();
				Log.i("mayicloud_recruit", "finish===============");
				return;
			}else{
				Point point = CameraManager.get().getCameraResolution();
				int width = point.y;
				int height = point.x;

				// int x = linear_capture.getLeft() * width
				// / relative_container.getWidth();
				int x = rl_capture.getLeft() * width
						/ relative_container.getWidth();
				int y = linear_capture.getTop() * height
						/ relative_container.getHeight();

				int cropWidth = linear_capture.getWidth() * width
						/ relative_container.getWidth();

				int cropHeight = linear_capture.getHeight() * height
						/ relative_container.getHeight();

				setX(x);
				setY(y);
				setCropWidth(cropWidth);
				setCropHeight(cropHeight);
				// 璁剧疆鏄惁闇�瑕佹埅鍥�
				setNeedCapture(true);
			}



		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureQrActivityHandler(CaptureQrActivity.this);
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// initCamera(holder);
		System.out.println("===========surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		System.out.println("=====================surfaceCreated");
		if (!hasSurface) {
			System.out.println("hasSurfacecreate==============hasSurface:::"
					+ hasSurface);
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println("111===========surfacedestroy");
		hasSurface = false;

	}

	public Handler getHandler() {
		return handler;
	}

	/**
	 * 鎵弿瀹屾垚鎻愮ず闊�
	 */
	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};


	public void getKeybordHeight() {
		relative_container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			/**
			 * the result is pixels
			 */
			@Override
			public void onGlobalLayout() {

				Rect rect = new Rect();
				// 鑾峰彇root鍦ㄧ獥浣撶殑鍙鍖哄煙
				relative_container.getWindowVisibleDisplayFrame(rect);
				// 鑾峰彇root鍦ㄧ獥浣撶殑涓嶅彲瑙嗗尯鍩熼珮搴�(琚叾浠朧iew閬尅鐨勫尯鍩熼珮搴�)
				int rootInvisibleHeight = relative_container.getRootView().getHeight() - rect.bottom;
				// 鑻ヤ笉鍙鍖哄煙楂樺害澶т簬100锛屽垯閿洏鏄剧ず
				if (rootInvisibleHeight > 100) {
					// 澶у皬瓒呰繃100鏃讹紝涓�鑸负鏄剧ず铏氭嫙閿洏浜嬩欢
					int[] location = new int[2];
					// 鑾峰彇scrollToView鍦ㄧ獥浣撶殑鍧愭爣
					rl_capturetop.getLocationInWindow(location);
					// 璁＄畻root婊氬姩楂樺害锛屼娇scrollToView鍦ㄥ彲瑙佸尯鍩�
					int srollHeight = (location[1] + rl_capturetop.getHeight()) - rect.bottom;
					rl_capturetop.scrollTo(0, srollHeight);
				} else {
					// 澶у皬灏忎簬100鏃讹紝涓轰笉鏄剧ず铏氭嫙閿洏鎴栬櫄鎷熼敭鐩橀殣钘�
					rl_capturetop.scrollTo(0, 0);
				}

			}
		});
	}

}