//package com.weizeng.networking.project2;
/*
 * Wei Zeng
 * CSCI651-Project2
 * 10/18/2018
 * */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalTime;

public class Send implements Runnable {
	private DatagramSocket datagramSocket = null;
	private int port;
	private String IP;
	private String data;

	public Send(DatagramSocket datagramSocket, int port, String IP, String data) {
		this.datagramSocket = datagramSocket;
		this.port = port;
		this.IP = IP;
		this.data = data;
	}

	@Override
	public void run() {
		try {
			this.data = this.data + LocalTime.now().toSecondOfDay();
			byte[] dataB = this.data.getBytes();
			DatagramPacket datagramPacket = new DatagramPacket(dataB, dataB.length, InetAddress.getByName(this.IP),
					this.port);
			this.datagramSocket.send(datagramPacket);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				this.datagramSocket.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

	}

}
