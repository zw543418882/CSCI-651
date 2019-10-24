//package com.weizeng.networking.project2;
/*
 * Wei Zeng
 * CSCI651-Project2
 * 10/18/2018
 * */
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Recieve implements Runnable {
	private RoutingTable routingTable;
	private DatagramSocket datagramSocket = null;
	private String[] recTimeTable;

	public Recieve(RoutingTable routingTable, DatagramSocket datagramSocket, String[] recTimeTable) {
		this.routingTable = routingTable;
		this.datagramSocket = datagramSocket;
		this.recTimeTable = recTimeTable;
	}

	public void setRecTimeTable(String[] recTimeTable) {
		this.recTimeTable = recTimeTable;
	}

	public String[] getRecTimeTable() {
		return recTimeTable;
	}

	@Override
	public void run() {
		try {
			while (true) {
				/*
				 * make the receive socket persistent
				 * */
				byte[] data = new byte[1024];
				DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
				this.datagramSocket.receive(datagramPacket);
				String dataStr = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
				String[] dataStrArr = dataStr.split("\n");
				if (dataStr.length() > 36) {
					/*
					 * Unpack the receive packets
					 * */
					String[][] routingData = new String[dataStrArr.length - 2][3];
					int routerNum = Integer.valueOf(dataStrArr[0]);
					for (int i = 1; i < dataStrArr.length - 1; i++) {
						routingData[i - 1][0] = dataStrArr[i].split("          ")[0];
						routingData[i - 1][1] = dataStrArr[i].split("          ")[1];
						routingData[i - 1][2] = dataStrArr[i].split("          ")[2];
					}
					String recTime = dataStrArr[dataStrArr.length - 1];
					this.recTimeTable[routerNum] = recTime;
					this.routingTable.updateTable(routingData, routerNum);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.datagramSocket.close();
		}

	}

}
