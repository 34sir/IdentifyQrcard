package com.example.chukc.identifyqrcard.decode;

import android.os.Handler;
import android.os.Looper;

import com.example.chukc.identifyqrcard.CaptureQrActivity;

import java.util.concurrent.CountDownLatch;


/**
 * 描述: 解码线程
 */
final class DecodeQrThread extends Thread {

	CaptureQrActivity activity;
	private Handler handler;
	private final CountDownLatch handlerInitLatch;

	DecodeQrThread(CaptureQrActivity activity) {
		this.activity = activity;
		handlerInitLatch = new CountDownLatch(1);
	}

	Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler = new DecodeQrHandler(activity);
		handlerInitLatch.countDown();
		Looper.loop();
	}

}
