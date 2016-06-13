package com.example.chukc.identifyqrcard.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 閹诲繗鍫�: 閹殿偅寮块煬顐″敜鐠囦胶娴夐張娲暕鐟欏牆娲栫拫锟�
 */
@SuppressWarnings("deprecation")
final  class PreviewCallback_id implements Camera.PreviewCallback {
	private static final String TAG = PreviewCallback.class.getSimpleName();
	int framecount = 0;
	String rootPath;
	private Handler previewHandler;
	private int previewMessage;
	private final boolean useOneShotPreviewCallback;
	private final CameraConfigurationManager_id configManager_id;
//	private CaptureIdActivity activity;

	PreviewCallback_id(CameraConfigurationManager_id configManager_id,
	    boolean useOneShotPreviewCallback, Context context) {
		this.configManager_id = configManager_id;
		this.useOneShotPreviewCallback = useOneShotPreviewCallback;
//        this.activity=(CaptureIdActivity) context;
	}

	void setHandler(Handler previewHandler, int previewMessage) {
		this.previewHandler = previewHandler;
		this.previewMessage = previewMessage;
	}



	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
		System.out.println("onPreviewFrame====================");
	    final Point cameraResolution = configManager_id.getCameraResolution();
	    if (!useOneShotPreviewCallback) {
	      camera.setPreviewCallback(null);
	    }
	    if (previewHandler != null) {

	    	 Message message = previewHandler.obtainMessage(previewMessage, cameraResolution.x,
			          cameraResolution.y, data);
			message.sendToTarget();

//	    	previewHandler.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//
//				}
//			}, 10);

//	      Message message = previewHandler.obtainMessage(previewMessage, activity.getWidth(),
//	          activity.getHeight(), data);

	      System.out.println("message.sendToTarget()====================");
	      previewHandler = null;
	    } else {
	      Log.d(TAG, "Got preview callback, but no handler for it");
	    }
	  }

}
