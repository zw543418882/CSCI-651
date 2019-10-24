package com.weizeng.project2Redo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RIPv2 {
	static byte[] setRequestRIPv2Packet() {
		byte[] RIPv2 = new byte[24];
		/*set RIPv2 Header
		 * */
		RIPv2[0] = (byte) (1 & 0xff);//command
		RIPv2[1] = (byte) (2 & 0xff);//version
		RIPv2[2] = (byte) (0 & 0xff);//unused
		RIPv2[3] = (byte) (0 & 0xff);//unused
		/*load information from routing table
		 * */
		//protocol tag
		RIPv2[4] = (byte) ((2 >>> 8) & 0xff);
		RIPv2[5] = (byte) ((2 >>> 0) & 0xff);
		//router tag
		RIPv2[6] = (byte) ((2 >>> 8) & 0xff);
		RIPv2[7] = (byte) ((2 >>> 0) & 0xff);
		//destination IP address 
		RIPv2[8] = (byte) (10 & 0xff);
		RIPv2[9] = (byte) (0 & 0xff);
		RIPv2[10] = (byte) (2& 0xff);
		RIPv2[11] = (byte) (0 & 0xff);
		//subnet mask
		RIPv2[12] = (byte) (255 & 0xff);
		RIPv2[13] = (byte) (255 & 0xff);
		RIPv2[14] = (byte) (255 & 0xff);
		RIPv2[15] = (byte) (0 & 0xff);
		//next hop 
		RIPv2[16] = (byte) (224 & 0xff);
		RIPv2[17] = (byte) (0 & 0xff);
		RIPv2[18] = (byte) (0 & 0xff);
		RIPv2[19] = (byte) (232& 0xff);
		//Metric
		RIPv2[20] = (byte) ((1 >>> 24) & 0xff);
		RIPv2[21] = (byte) ((1 >>> 16) & 0xff);
		RIPv2[22] = (byte) ((1 >>> 8) & 0xff);
		RIPv2[23] = (byte) ((1 >>> 0) & 0xff);
		
		return RIPv2;
	}
	
	

	public static void main(String[] args) {
		try {
			DatagramSocket socket = new DatagramSocket();
			byte[] RIPv2 = setRequestRIPv2Packet();
			DatagramPacket RIPv2Packet = new DatagramPacket(RIPv2, RIPv2.length, InetAddress.getByName("224.0.0.232"), 63001);
			socket.send(RIPv2Packet);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
