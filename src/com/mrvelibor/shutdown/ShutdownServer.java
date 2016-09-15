package com.mrvelibor.shutdown;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ShutdownServer extends Thread {
	
	public static final int PORT = 12345;
	
	private ServerSocket mServer = null;
	private Socket mClient = null;
	
	private static boolean sRunning = false;
	
	public ShutdownServer() {
		super(ShutdownServer.class.getName());
	}
	
	@Override
	public synchronized void start() {
		if(sRunning) throw new IllegalStateException("Already running!");
		else super.start();
	}
	
	@Override
	public void run() {
		sRunning = true;
		try {
			mServer = new ServerSocket(PORT, 1);
			while(sRunning) {
				mClient = mServer.accept();
				System.out.println("Another instance started!");
				mClient.close();
			}
			mServer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		sRunning = false;
	}
	
	public void close() {
		if(mServer != null) {
			sRunning = false;
			try {
				mServer.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}