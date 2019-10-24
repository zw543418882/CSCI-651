//package com.weizeng.networking.project4;
/*
 * Wei Zeng
 * Project 4
 * 12/02/2018
 * */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NTPClient {

	static byte[] setNTPPacket() {
		/*
		 * Construct NTP Packet According to RFC 5905
		 */
		byte[] NTPHeader = new byte[48];// Besides authenticator, a NTP packet has at least 48 bytes
		byte LI = 0;// 2 bits, warning of an impending leap second
		byte VN = 4;// 3 bits, the version of the NTP packet
		byte mod = 3;// 3 bits, the mode of the NTP packet
		byte poll = 4;// 1 byte, the maximum interval between successive messages, in seconds to the
						// nearest power of two
		NTPHeader[0] = (byte) (LI << 6 | VN << 3 | mod);
		for (int i = 1; i < 48; i++) {
			NTPHeader[i] = (byte) 0;
		}
		NTPHeader[2] = poll;
		// Root delay is about 1 second
		NTPHeader[4] = (byte) ((1000 >> 24) & 0xff);
		NTPHeader[5] = (byte) ((1000 >> 16) & 0xff);
		NTPHeader[6] = (byte) ((1000 >> 8) & 0xff);
		NTPHeader[7] = (byte) ((1000 >> 0) & 0xff);
		// Root dispersion is about 1 second
		NTPHeader[8] = (byte) ((1000 >> 24) & 0xff);
		NTPHeader[9] = (byte) ((1000 >> 16) & 0xff);
		NTPHeader[10] = (byte) ((1000 >> 8) & 0xff);
		NTPHeader[11] = (byte) ((1000 >> 0) & 0xff);
		return NTPHeader;
	}

	static short unsignedByteToShort(byte b) {
		/*
		 * Converts an unsigned byte to a short
		 */
		if ((b & 0x80) == 0x80) {
			return (short) (128 + (b & 0x7f));
		} else {
			return (short) b;
		}
	}

	static double getTimeStamp(byte[] data, int index) {
		/*
		 * Get time stamp from NTP response packet
		 */
		double result = 0.0;
		for (int i = 0; i < 8; i++) {
			result += unsignedByteToShort(data[index + i]) * Math.pow(2, (3 - i) * 8);
		}
		return result;
	}

	static String timestampToString(double timestamp) {
		/*
		 * convert time stamp of double to standard format of string
		 */
		if (timestamp == 0) {
			return "0";
		}
		double utc = timestamp - (2208988800.0);
		long ms = (long) (utc * 1000.0);
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(new Date(ms));
		return date;
	}

	static long converDateIntoLong(String date) {
		/*
		 * convert date of string to long
		 */
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
		long time = 0;
		try {
			time = simpleDateFormat.parse(date).getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return time;

	}

	static double NTPRequest(String serverName, long sentTimeLong) {
		/*
		 * 1. construct NTP header a. LI-VN-Mode: 0010011 (NTPv4, Client mode) b. Poll:
		 * 2^4 = 16, every 16 seconds sends a NTP request
		 * 
		 * 2. send NTP request a. send to us.pool.ntp.org, port 123 b. if time out which
		 * is 2 s, resends request at most 5 times c. record current system time t2
		 * 
		 * 3. receive NTP response a. record current system time t4
		 * 
		 * 4. process response packet a. retrieve receive time stamp t1 from the
		 * response packet b. retrieve transmit time stamp t3 from response packet c.
		 * offset = ((t1 - t2) + (t3 - t4)) /2 d. if offset < 100, the local clock is
		 * synchronized, otherwise it needs to plus offset to correct local clock
		 */
		double offset = 0.0;
		try {
			DatagramSocket socket = new DatagramSocket();
			byte[] NTPHeader = setNTPPacket();
			DatagramPacket packet = new DatagramPacket(NTPHeader, NTPHeader.length, InetAddress.getByName(serverName),
					123);
			SimpleDateFormat sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
			String sendTimeStr = sendTime.format(new Date(sentTimeLong));

			System.out.println("The Client Sent NTP Request at " + sendTimeStr + " (Client Clock)");

			byte[] recBytes = new byte[48];
			DatagramPacket recPacket = new DatagramPacket(recBytes, recBytes.length);

			socket.setSoTimeout(2000);
			int numOfSend = 0;
			while (numOfSend < 5) {
				try {
					socket.send(packet);
					socket.receive(recPacket);
					break;
				} catch (Exception e) {
					System.out.println("Time Out! Resend Request!");
					numOfSend++;
				}
			}

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
			String localTime = df.format(new Date());
			System.out.println("The Client Received NTP Response at " + localTime + " (Client Clock)");
			long recTimeLong = converDateIntoLong(localTime);

			double receiveTimeStamp = getTimeStamp(recBytes, 32);
			String receiveTimeStampStr = timestampToString(receiveTimeStamp);
			System.out.println("The Server Received The Request at " + receiveTimeStampStr + " (Server Clock)");

			double transmitTimeStamp = getTimeStamp(recBytes, 40);
			String sourceTime = timestampToString(transmitTimeStamp);
			System.out.println("The Server Sent The Response at " + sourceTime + " (Server Clock)");

			long receiveTimeLong = converDateIntoLong(receiveTimeStampStr);
			long transmitTimeLong = converDateIntoLong(sourceTime);
			offset = ((receiveTimeLong - sentTimeLong) + (transmitTimeLong - recTimeLong)) / 2;
			System.out.println("The Local Clock Offest is " + offset + " ms!");

			if (Math.abs(offset) < 100) {
				System.out.println("The Client Clock is Synchronized!");
			} else {
				System.out.println("The Client Clock is Not Synchronized!");
				System.out.println("The Client adjust clock in small steps(100 ms)!");
			}
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return offset;
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Please Input The IP Address Of NTP Server!");
			System.exit(0);// if IP address of NTP server is null, exit the program
		}

		final String serverName = args[0];

		double[] offset = new double[2];
		offset[0] = 0.0;//current offset
		offset[1] = 0.0;//the sum of offset that used to adjust client clock

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				SimpleDateFormat sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
				String sendTimeStr = sendTime.format(new Date());
				long sentTimeLong = converDateIntoLong(sendTimeStr);
				if (offset[0] >= 100) {
					offset[1] += 100;
					sentTimeLong += offset[1];
					offset[0] = NTPRequest(serverName, sentTimeLong);
					System.out.println("\n\n\n");
				} else if (offset[0] <= -100) {
					offset[1] -= 100;
					sentTimeLong += offset[1];
					offset[0] = NTPRequest(serverName, sentTimeLong);
					System.out.println("\n\n\n");
				}else {
					sentTimeLong += offset[1];
					offset[0] = NTPRequest(serverName, sentTimeLong);
					System.out.println("\n\n\n");
				}
				
			}
		}, 0, 2000);// every 2 s resends a NTP packet

	}

}
