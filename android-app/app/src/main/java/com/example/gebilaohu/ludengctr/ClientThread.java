package com.example.gebilaohu.ludengctr;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

import android.util.Log;
import android.widget.Toast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

@SuppressLint("HandlerLeak")
public class ClientThread extends Thread {

	
	private OutputStream outputStream = null;
	private InputStream inputStream = null;
	private Socket socket;
	private SocketAddress socketAddress;
	public static Handler childHandler;
	private boolean RxFlag = true;
	private RxThread rxThread;
	final int TEXT_INFO = 12;
	static final int RX_EXIT = 11;
	static final int TX_DATA = 10;
	Context mainContext;
	Message msg;
	private String strIP;
	private int SERVER_PORT;

	
	
	public ClientThread(String ip,int port) {
		strIP = ip;
		SERVER_PORT = port;
		Log.d("MainActivity","ClientThread" );
	}	


	void connect() {
		RxFlag = true;
		socketAddress = new InetSocketAddress(strIP, SERVER_PORT);
		socket = new Socket();
		
		try {
			
			Log.d("MainActivity","socket = new Socket();" );
			socket.connect(socketAddress, SERVER_PORT);

			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			
			
			msg = MainActivity.mainHandler.obtainMessage(MainActivity.TIPS_UPDATE_UI, "连接成功");
			MainActivity.mainHandler.sendMessage(msg);

			rxThread = new RxThread();
			rxThread.start();

		} catch (IOException e) {
			try {
				sleep(10);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			msg = MainActivity.mainHandler.obtainMessage(MainActivity.TIPS_UPDATE_UI, "无法连接");
			MainActivity.mainHandler.sendMessage(msg);
			e.printStackTrace();
		} catch (NumberFormatException e) {

		}
	}

	void initChildHandler() {
		
		Looper.prepare();

		childHandler = new Handler() {
			//���߳���Ϣ��������
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case TX_DATA:;				
					try {
						//outputStream.write((byte [])msg.obj, 0, len);
						String str = (String) msg.obj;
						outputStream.write(str.getBytes("utf-8"));
						outputStream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;

				case RX_EXIT:
					RxFlag = false;
					try {
						if (socket.isConnected()) {
							inputStream.close();
							outputStream.close();
							socket.close();
						}
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					childHandler.getLooper().quit();

					break;

				default:
					break;
				}

			}
		};

		Looper.loop();

	}

	public void run() {
		connect();
		initChildHandler();
		msg = MainActivity.mainHandler.obtainMessage(MainActivity.TIPS_UPDATE_UI, "断开连接");
		MainActivity.mainHandler.sendMessage(msg);
	}
		
	//socket
	public class RxThread extends Thread {
		public void run() {
			try {
				while (socket.isConnected() && RxFlag) {
					
					InputStreamReader isr = new InputStreamReader(inputStream);
					BufferedReader br = new BufferedReader(isr);
					
					Log.d("MainActivity","RxThread" );
					
					MainActivity.recv = br.readLine();//readline
					
					
					msg = MainActivity.mainHandler.obtainMessage(MainActivity.RX_DATA_UPDATE_UI,"Connect");
					MainActivity.mainHandler.sendMessage(msg);

				}
				
				if (socket.isConnected())
					socket.close();
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
