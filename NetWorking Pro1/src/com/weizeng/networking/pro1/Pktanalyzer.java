//package com.weizeng.networking.pro1;
/*
 * Wei Zeng
 * NetWorking Project1
 * Date: 09/14/2018
 * */

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;

public class Pktanalyzer {
	public String fileName;
	public File packetFile;
	
	public Pktanalyzer(String fileName) {
		this.fileName = fileName;
		try {
			this.packetFile = new File(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void printEtherHeader(byte[] dataA) {
		System.out.println("ETHER:  ----- Ether Header ----- ");
		System.out.println("ETHER: ");
		System.out.println("ETHER:  Packet size = " + dataA.length + " bytes");
		System.out.print("ETHER:  Destination = ");
		for(int i = 0; i < 6; i++) {
			if(i != 5) {
				System.out.format("%02x :", dataA[i]);
			}else {
				System.out.format("%02x,\n", dataA[i]);
			}
		}
		System.out.print("ETHER:  Source      = ");
		for(int i = 6; i < 12; i++) {
			if(i != 11) {
				System.out.format("%02x :", dataA[i]);
			}else {
				System.out.format("%02x,\n", dataA[i]);
			}
		}
		System.out.format("Ether:  type        = %02x%02x (IP)\n", dataA[12], dataA[13]);
		System.out.println("ETHER: ");
	}

	public static void printIPHeader(byte[] dataA, String typeName) {
		System.out.println("IP:   ----- IP Header ----- ");
		System.out.println("IP:");
		String temp = String.format("%02x", dataA[14]).substring(0, 1);
		System.out.println("IP:  Version =  " + temp);
		String temp2 = String.format("%02x", dataA[14]).substring(1, 2);
		int tempV2 = Integer.valueOf(temp2);
		System.out.println("IP:  Header length = " + 4 * tempV2 + " bytes");
		System.out.format("IP:  Type of service = 0x%02x\n", dataA[15]);
		System.out.println("IP:        xxx. .... = 0 (precedence)");
		int tempServicesV = Integer.parseInt(String.format("%02x", dataA[15]), 16);
		String tempServiceS = Integer.toBinaryString(tempServicesV);
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < 8 - tempServiceS.length(); i++) {
		sb.append("0");
		}
		sb.append(tempServiceS);
		System.out.println(sb);
		if (sb.charAt(3)  == '0') {
			System.out.println("IP:        ...0 .... = normal delay");
		}
		if (sb.charAt(4)  == '0') {
			System.out.println("IP:        .... 0... = normal throughput");
		}
		if (sb.charAt(5)  == '0') {
			System.out.println("IP:        .... .0.. = normal reliability");
		}
		String tempLengthS = String.valueOf(Integer.valueOf(String.format("%02x", dataA[17]), 16));
		System.out.println("IP:  Total length = " + tempLengthS);
		String tempIdenS = String.valueOf(
				Long.valueOf(String.format("%02x", dataA[18]) + String.format("%02x", dataA[19]), 16));
		System.out.println("IP:  Identification = " + tempIdenS);
		System.out.format("IP:  Flags = 0x%s\n", String.format("%02x", dataA[20]).substring(0, 1));
		String tempFlagS = new BigInteger(String.format("%02x", dataA[22]), 16).toString(2);
		if (tempFlagS.charAt(1) == '1') {
			System.out.println("IP:         .1.. .... = do not fragment");
		}
		if (tempFlagS.charAt(1) == '0') {
			System.out.println("IP:         .0.. .... = OK to fragment");
		}
		if (tempFlagS.charAt(2) == '0') {
			System.out.println("IP:         ..0. .... = last fragment");
		}
		String tempOffSetS = String.valueOf(Integer.parseInt(String.format("%02x", dataA[21]), 16));
		System.out.println("IP:  Fragment offset = " +tempOffSetS);
		String tempTimeS = String.valueOf(Integer.parseInt(String.format("%02x", dataA[22]), 16));
		System.out.println("IP:  Time to live = " + tempTimeS + " seconds/hops");
		System.out.format("IP:  Protocol = %d (%s)\n", Integer.parseInt(String.format("%02x", dataA[23]), 16), typeName);
		System.out.println("IP:  Header checksum = 0x" + String.format("%02x", dataA[24])
				+ String.format("%02x", dataA[25]));
		String sourceS = String.valueOf(Integer.parseInt(String.format("%02x", dataA[26]), 16)) + "."
				+ String.valueOf(Integer.parseInt(String.format("%02x", dataA[27]), 16)) + "."
				+ String.valueOf(Integer.parseInt(String.format("%02x", dataA[28]), 16)) + "."
				+ String.valueOf(Integer.parseInt(String.format("%02x", dataA[29]), 16));
		System.out.println("IP:  Source address = " + sourceS);
		String destS = String.valueOf(Integer.parseInt(String.format("%02x", dataA[30]), 16)) + "."
				+ String.valueOf(Integer.parseInt(String.format("%02x", dataA[31]), 16)) + "."
				+ String.valueOf(Integer.parseInt(String.format("%02x", dataA[32]), 16)) + "."
				+ String.valueOf(Integer.parseInt(String.format("%02x", dataA[33]), 16));
		System.out.println("IP:  Destination address = " + destS);
		System.out.println("IP:  No options\nIP:");
		
		
		
	}
	
	public static void printTCP(byte[] dataA) {
		System.out.println("TCP:  ----- TCP Header -----");
		System.out.println("TCP:");
		System.out.format("TCP:  Source port = %s\n", String.valueOf(Long.valueOf(
				String.format("%02x", dataA[34]) + String.format("%02x", dataA[35]), 16)));
		System.out.format("TCP:  Destination port = %s\n", String.valueOf(Long.valueOf(
				String.format("%02x", dataA[36]) + String.format("%02x", dataA[37]), 16)));
		System.out.format("TCP:  Sequence number = %s\n", String.valueOf(Long.valueOf(String.format("%02x", dataA[38])
				+ String.format("%02x", dataA[39]) + String.format("%02x", dataA[40])
				+ String.format("%02x", dataA[41]), 16)));
		System.out.format("TCP:  Acknowledgement number = %s\n", String.valueOf(Long.valueOf(String.format("%02x", dataA[42])
				+ String.format("%02x", dataA[43]) + String.format("%02x", dataA[44])
				+ String.format("%02x", dataA[45]), 16)));
		System.out.format("TCP:  Data offset = %s bytes\n", String.valueOf(Integer.parseInt((String.format("%02x", dataA[46])).substring(0, 1), 16)));
		System.out.format("TCP:  Flags = 0x%s\n", String.format("%02x", dataA[47]));
		String tempFlagS = new BigInteger(String.format("%02x", dataA[47]), 16).toString(2);
		StringBuffer tempsS = new StringBuffer();
		for(int i = 0; i < 8 - tempFlagS.length(); i++) {
			tempsS.append("0");
		}
		tempsS.append(tempFlagS);
		if (tempsS.charAt(2) == '1') {
			System.out.println("TCP:        ..1. .... = Urgent pointer");
		} else {
			System.out.println("TCP:        ..0. .... = No urgent pointer");
		}
		if (tempsS.charAt(3) == '1') {
			System.out.println("TCP:        ...1 .... = Acknowledgement");
		} else {
			System.out.println("TCP:        ...0 .... = No Acknowledgement");
		}
		if (tempsS.charAt(4) == '1') {
			System.out.println("TCP:        .... 1... = Push");
		} else {
			System.out.println("TCP:        .... 0... = No Push");
		}
		if (tempsS.charAt(5) == '1') {
			System.out.println("TCP:        .... .1.. = Reset");
		} else {
			System.out.println("TCP:        .... .0.. = No Reset");
		}
		if (tempsS.charAt(6) == '1') {
			System.out.println("TCP:        .... ..1. = Syn");
		} else {
			System.out.println("TCP:        .... ..0. = No Syn");
		}
		if (tempsS.charAt(7) == '1') {
			System.out.println("TCP:        .... ...1 = Fin");
		} else {
			System.out.println("TCP:        .... ...0 = No Fin");
		}
		System.out.format("TCP:  Window = %s\n", String.valueOf(Long.valueOf(
				String.format("%02x", dataA[48]) + String.format("%02x", dataA[49]), 16)));
		System.out.format("TCP:  Checksum = 0x%s%s\n", String.format("%02x", dataA[50]), String.format("%02x", dataA[51]));
		System.out.format("TCP:  Urgent pointer = %s\n", String.valueOf(Long.valueOf(
				String.format("%02x", dataA[52]) + String.format("%02x", dataA[53]), 16)));
		System.out.println("TCP:  No options \nTCP:");
		
	}
	
	public static void printData(byte[] dataA, String typeName) {
		System.out.format("%s:  Data: (first 64 bytes)\n", typeName);
		int index = 0;
		byte[] dataArray;
		int j = 0;
		for(int i = 0; i < 4 && 34 + (16 * i) < dataA.length; i++) {
			dataArray = new byte[64];
			System.out.format("%s:  ", typeName);
			for(j = 0; j < 16 && j + 34 + (16 * i) < dataA.length; j++) {
				System.out.print(String.format("%02x", dataA[j + 34 + (16 * i)]));
				dataArray[index] = dataA[j + 34 + (16 * i)];
				index++;
				if (j % 2 != 0) {
					System.out.print(" ");
				}
			}
			System.out.print("    '");
			for(int k = 0; k < 16; k++) {
				int tempC = Integer.parseInt(String.format("%02x", dataArray[k + 16 * i]), 16);
				if (tempC >= 36 && tempC <= 126) {
					System.out.print((char) tempC);
				}else {
					System.out.print(".");
				}
			}
			System.out.print("'");
			System.out.println("");
		}
		
	}
	
	public static void printICMP(byte[] dataA, String typeName) {
		System.out.println("ICMP:  ----- ICMP Header -----\nICMP:");
		System.out.format("ICMP:  Type = %s (Echo request)\n", String.valueOf(Integer.parseInt(String.format("%02x", dataA[34]), 16)));
		System.out.format("ICMP:  Code = %s\n", String.valueOf(Integer.valueOf(String.format("%02x", dataA[35]), 16)));
		System.out.format("ICMP:  Checksum =  0x%s%s\n", String.format("%02x", dataA[36]), String.format("%02x", dataA[37]));
		System.out.println("TCMP:  ");
	}
	
	public static void printUDP(byte[] dataA, String typeName) {
		System.out.println("UDP:  ----- UDP Header -----\nUDP:");
		System.out.format("UDP:  Source port = %s\n", String.valueOf(Integer.valueOf(
				String.format("%02x", dataA[34]) + String.format("%02x", dataA[35]), 16)));
		System.out.format("UDP:  Destination port = %s\n", String.valueOf(Integer.valueOf(
				String.format("%02x", dataA[36]) + String.format("%02x", dataA[37]), 16)));
		System.out.format("UDP:  Length = %s\n", String.valueOf(Integer.valueOf(
				String.format("%02x", dataA[38]) + String.format("%02x", dataA[39]), 16)));
		System.out.format("UDP:  Checksum = 0x%s%s\n", String.format("%02x", dataA[40]), String.format("%02x", dataA[41]));
		System.out.println("UDP:");
	}
	
	public static void main(String[] args) {
		Pktanalyzer pktA = new Pktanalyzer(args[0]);
		InputStream packetStream = null;
		try {
			 packetStream = new FileInputStream(pktA.packetFile);
			 byte[] dataA = new byte[packetStream.available()];
			 packetStream.read(dataA);
			 printEtherHeader(dataA);
			 int type = Integer.parseInt(String.format("%02x", dataA[23]), 16);
			 String typeName = null;
			 if (type == 1) {
					typeName = "ICMP";
					printIPHeader(dataA, typeName);
					printICMP(dataA, typeName);
				}
			 if(type == 6) {
				 typeName = "TCP";
				 printIPHeader(dataA, typeName);
				 printTCP(dataA);
				 printData(dataA, typeName);
			 }
			 if (type == 17) {
				typeName = "UDP";
				printIPHeader(dataA, typeName);
				printUDP(dataA, typeName);
				printData(dataA, typeName);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				packetStream.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		
		

	}

}
