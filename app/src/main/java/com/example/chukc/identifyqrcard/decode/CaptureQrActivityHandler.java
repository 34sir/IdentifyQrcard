package com.example.chukc.identifyqrcard.decode;


import android.os.Handler;
import android.os.Message;

import com.example.chukc.identifyqrcard.CaptureQrActivity;
import com.example.chukc.identifyqrcard.R;
import com.example.chukc.identifyqrcard.camera.CameraManager;


public final class CaptureQrActivityHandler extends Handler {

	DecodeQrThread decodeThread = null;
	CaptureQrActivity activity = null;
	private State state;

	public enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureQrActivityHandler(CaptureQrActivity activity) {
		this.activity = activity;
		decodeThread = new DecodeQrThread(activity);
		decodeThread.start();
		state = State.SUCCESS;
		CameraManager.get().startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {

		switch (message.what) {
		case R.id.auto_focus:
			if (state == State.PREVIEW) {
				CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
			}
			break;
		case R.id.restart_preview:
			restartPreviewAndDecode();
			break;
		case R.id.decode_succeeded:
			state = State.SUCCESS;
			activity.handleDecode((String) message.obj);// 瑙ｆ瀽鎴愬姛锛屽洖璋�

			break;

		case R.id.decode_failed:
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
			break;
		}

	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
		removeMessages(R.id.decode);
		removeMessages(R.id.auto_focus);
	}

	public void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
			CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
		}
	}

}
