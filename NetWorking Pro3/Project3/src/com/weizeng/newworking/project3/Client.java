//package com.weizeng.newworking.project3;
/*
 * Wei Zeng
 * NetWorking Project3
 * 11/15/2018
 * */
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;



public class Client {
	static final int DATA_SIZE = 10 * 1024 * 1024;//The size of transmission file
	static final int PORT_NUMBER = 63001;//The port number of server
	static final int TIME_OUT = 1000;//The value of time out
	static final int RESENT_TIMES = 5;//How many times client can resent the detect packet
	
	public static int bytesToInt(byte first, byte second) {
		//Transfer bytes array to int
		int result = ((first & 0xff) << 8) | ((second & 0xff) << 0);
		return result;
	}
	
	public static byte[] getDataBuffer(String fileName) {
		//Transfer file to bytes array
		byte[] buffer = null;
		try {
			File dataFile = new File(fileName);
			FileInputStream inputStream = new FileInputStream(dataFile);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(DATA_SIZE);
			byte[] byteArr = new byte[DATA_SIZE];
			int length = -1;
			while((length = inputStream.read(byteArr)) != -1) {
				outputStream.write(byteArr, 0, length);
			}
			buffer = outputStream.toByteArray();
			inputStream.close();
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}
	
	public static byte[] intTOBytes(int number) {
		//Transfer int to bytes array
		byte[] result = new byte[2];
		result[0] = (byte) ((number >>> 8) & 0xff);
		result[1] = (byte) ((number >>> 0) & 0xff);
		return result;
	}
	
	public static DatagramPacket setHeader(int packetType, int fileType, InetAddress address, int seq, int ACK, byte[] data) {
		/*
		 * Set the header and data which is at most 450 bytes in message packets
		 * the first byte shows the type of message packet: 0 is command packet, 1 is data packet
		 * the second byte shows the type of file:  1 is jpg file, 2 is txt file
		 * the third and forth byte show the sequence number
		 * the fifth and sixth byte show the ACK number
		 * seventh and the rest bytes store the data
		 * */
		byte[] bytes = new byte[456];
		byte[] seqBytes = intTOBytes(seq);
		byte[] ACKBytes = intTOBytes(ACK);
		bytes[0] = (byte)packetType;
		bytes[1] = (byte)fileType;
		bytes[2] = seqBytes[0];
		bytes[3] = seqBytes[1];
		bytes[4] = ACKBytes[0];
		bytes[5] = ACKBytes[1];
		if (packetType != 0) {
			if (data.length - 450 * seq >= 450) {
				for(int i = 0; i < 450; i++) {
					bytes[i + 6] = data[seq * 450 + i];
				}
			}else {
				byte[] temp = new byte[data.length - 450 * seq + 6];
				temp[0] = (byte)packetType;
				temp[1] = (byte)fileType;
				temp[2] = seqBytes[0];
				temp[3] = seqBytes[1];
				temp[4] = ACKBytes[0];
				temp[5] = ACKBytes[1];
				for(int i = 0; i < temp.length - 6; i++) {
					temp[i + 6] = data[seq * 450 + i];
				}
				bytes = temp;
			}
		}
		DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, address, PORT_NUMBER);
		return datagramPacket;
	}

	public static void main(String[] args) {
		String serverIPStr = args[0];
		String fileStr = args[1];
		byte[] buffer = null;
		
		try {
			DatagramSocket datagramSocket = new DatagramSocket(PORT_NUMBER + 3);
			DatagramPacket firstPacket = setHeader(0, 0, InetAddress.getByName(serverIPStr), 0, 0, buffer);
			byte[] firstAckBytes = new byte[20];
			DatagramPacket firstACK = new DatagramPacket(firstAckBytes, firstAckBytes.length);
			
			int sentTimes = 0;
			boolean linkedOrNot = false;
			datagramSocket.setSoTimeout(TIME_OUT);
			while(!linkedOrNot && sentTimes < RESENT_TIMES) {
				/*
				 * send the detect packet, if the channel is clear then start transmission
				 * try at most five times
				 * */
				try {
					datagramSocket.send(firstPacket);
					datagramSocket.receive(firstACK);
					linkedOrNot = true;
				} catch (InterruptedIOException e) {
					sentTimes += 1;
				}
			}
			
			if (fileStr.equals("jpg")) {
				buffer = getDataBuffer("image.jpg");
			}
			if (fileStr.equals("txt")) {
				buffer = getDataBuffer("data.txt");
			}
			if (linkedOrNot) {
				System.out.println("The server is avaliable! Start Transmission!");
				int seq = 0;
				int ack = 0;
				int numberOfPacketsInt = buffer.length / 450;
				float numberOfPacketsFloat = (float)buffer.length / 450;
				if (numberOfPacketsFloat > numberOfPacketsInt) {
					numberOfPacketsInt += 1;
				}
				while(seq <= numberOfPacketsInt) {
					//start transmission 
					try {
						System.out.println(seq);
						DatagramPacket dataPacket = null;
						if (seq == numberOfPacketsInt) {
							if (args[1].equals("jpg")) {
								dataPacket = setHeader(0, 1, InetAddress.getByName(serverIPStr), seq, 0, buffer);
							}
							if (args[1].equals("txt")) {
								dataPacket = setHeader(0, 2, InetAddress.getByName(serverIPStr), seq, 0, buffer);
							}
							datagramSocket.send(dataPacket);
							break;
						}else {
							if (args[1].equals("jpg")) {
								dataPacket = setHeader(1, 1, InetAddress.getByName(serverIPStr), seq, 0, buffer);
							}
							if (args[1].equals("txt")) {
								dataPacket = setHeader(1, 2, InetAddress.getByName(serverIPStr), seq, 0, buffer);
							}
						}
						datagramSocket.send(dataPacket);
						byte[] receiveBytes = new byte[6];
						DatagramPacket receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);
						datagramSocket.receive(receivePacket);
						int ackNum = bytesToInt(receiveBytes[4], receiveBytes[5]);
						if (ackNum == seq) {
							//if receive the ack, send next packet
							seq += 1;
						}
					} catch (InterruptedIOException e) {
						System.out.println("Resend " + seq +" !");
					}
				}
				while(true) {
					byte[] lastBytes = new byte[1006];
					DatagramPacket lastPacket = new DatagramPacket(lastBytes, lastBytes.length);
					datagramSocket.receive(lastPacket);
					if (bytesToInt(lastPacket.getData()[4], lastPacket.getData()[5]) == seq + 1) {
						try {
							byte[] lastData = new byte[lastPacket.getData().length - 6];
							for(int i = 6; i < lastPacket.getData().length; i++) {
								lastData[i - 6] = lastPacket.getData()[i];
							}
							FileOutputStream outputStream = new FileOutputStream(new File("command.txt"));
							BufferedOutputStream bos = new BufferedOutputStream(outputStream);
							bos.write(lastData);
							bos.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					DatagramPacket lastSendPacket = setHeader(0, 0, InetAddress.getByName(serverIPStr), seq + 1, 0, new byte[0]);
					datagramSocket.send(lastSendPacket);
					break;
				}
			}else {
				System.out.println("The server is unavaliable. Please try later!");
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

}
