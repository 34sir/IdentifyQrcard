package com.example.chukc.identifyqrcard.camera;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * 鎻忚堪: 鐩告満绠＄悊
 */
@SuppressWarnings("deprecation")
public final class CameraManager {
	private static CameraManager cameraManager;

	static final int SDK_INT;
	static {
		int sdkInt;
		try {
			sdkInt = android.os.Build.VERSION.SDK_INT;
		} catch (NumberFormatException nfe) {
			sdkInt = 10000;
		}
		SDK_INT = sdkInt;
	}

	private final CameraConfigurationManager configManager;
	private final CameraConfigurationManager_id configManager_id;

	private Camera camera;
	@SuppressWarnings("unused")
	private boolean initialized,initialized_id;
	private boolean previewing;
	private final boolean useOneShotPreviewCallback;
	private final PreviewCallback previewCallback;
	private final PreviewCallback_id previewCallback_id;
	private final AutoFocusCallback autoFocusCallback;
	private Parameters parameter;

	public static void init(Context context) {
		if (cameraManager == null) {
			cameraManager = new CameraManager(context);
		}
	}

	public static CameraManager get() {
		return cameraManager;
	}

	private CameraManager(Context context) {
		this.configManager = new CameraConfigurationManager(context);
		this.configManager_id=new CameraConfigurationManager_id(context);

		useOneShotPreviewCallback = SDK_INT > 3;
		previewCallback = new PreviewCallback(configManager, useOneShotPreviewCallback);
		previewCallback_id= new PreviewCallback_id(configManager_id, useOneShotPreviewCallback,context);
		autoFocusCallback = new AutoFocusCallback();
	}

	public boolean openDriver(SurfaceHolder holder) throws IOException {
		Log.i("mayicloud_recruit", "openDriver===============");
		boolean isopen = false;
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}

		if (camera == null) {

			try {
				camera = Camera.open();
				isopen=true;
				camera.setPreviewDisplay(holder);

				if (!initialized) {
					initialized = true;
					configManager.initFromCameraParameters(camera);
				}
				configManager.setDesiredCameraParameters(camera);
				FlashlightManager.enableFlashlight();
			} catch (Exception e) {
				// TODO: handle exception
				camera=null;
				isopen=false;
			}

//			camera = Camera.open();
//			isopen=true;
//			if (camera == null) {
//				isopen=false;
//				throw new IOException();
//			}
//			camera.setPreviewDisplay(holder);
//
//			if (!initialized) {
//				initialized = true;
//				configManager.initFromCameraParameters(camera);
//			}
//			configManager.setDesiredCameraParameters(camera);
//			FlashlightManager.enableFlashlight();
		}

		Log.i("mayicloud_recruit", "isopen==============="+isopen);
		return isopen;
	}

	public void idopenDriver(SurfaceHolder holder,Activity activity) throws IOException {
		if (camera == null) {
			System.out.println("openDriver=================");
			camera = Camera.open();
			if (camera == null) {
				throw new IOException();
			}
			if (!initialized) {
				initialized = true;
				camera.setPreviewDisplay(holder);
//				camera.setPreviewCallback((android.hardware.Camera.PreviewCallback) activity);

				configManager_id.initFromCameraParameters(camera);
			}
			configManager_id.setDesiredCameraParameters(camera);
			FlashlightManager.enableFlashlight();
		}
	}

	public Point getCameraResolution() {
		return configManager.getCameraResolution();
	}
	public Point getCameraResolution_id() {
		return configManager_id.getCameraResolution();
	}

	public void closeDriver() {
		if (camera != null) {
			//FlashlightManager.disableFlashlight();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
	}

	public void startPreview() {
		if (camera != null && !previewing) {
			camera.startPreview();
			previewing = true;
		}
	}

	public void stopPreview() {
		if (camera != null && previewing) {
			System.out.println("======================stopPreview");
			if (!useOneShotPreviewCallback) {
				camera.setPreviewCallback(null);
			}
			camera.stopPreview();
			previewCallback.setHandler(null, 0);
			autoFocusCallback.setHandler(null, 0);
			previewing = false;
			initialized=false;
		}
	}

	/**
	 * 浜岀淮鐮�
	 * @param handler
	 * @param message
	 */
	public void requestPreviewFrame(Handler handler, int message) {
		if (camera != null && previewing) {
			System.out.println("requestPreviewFrame================");
			previewCallback.setHandler(handler, message);
			if (useOneShotPreviewCallback) {
				camera.setOneShotPreviewCallback(previewCallback);
			} else {
				camera.setPreviewCallback(previewCallback);
			}
		}
	}


	/**
	 * 韬唤璇�
	 * @param handler
	 * @param message
	 */
	public void requestPreviewFrame_id(Handler handler, int message) {

		if (camera != null && previewing) {
			if (useOneShotPreviewCallback) {
				System.out.println("===========useOneShotPreviewCallback");
			    camera.setOneShotPreviewCallback(previewCallback_id);
		} else {
			camera.setPreviewCallback(previewCallback_id);
			System.out.println("================camera.setPreviewCallback");
		}
		}
		previewCallback_id.setHandler(handler, message);
	}

	public void requestAutoFocus(Handler handler, int message) {

		if (camera != null && previewing) {
			autoFocusCallback.setHandler(handler, message);
		camera.autoFocus(autoFocusCallback);
		}
	}

	public void openLight() {
		if (camera != null) {
			parameter = camera.getParameters();
//			parameter.setPreviewFrameRate(fps);
//			parameter.setPreviewFpsRange(min, max);
			parameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
			camera.setParameters(parameter);
		}
	}

	public void offLight() {
		if (camera != null) {
			parameter = camera.getParameters();
			parameter.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(parameter);
		}
	}
}
