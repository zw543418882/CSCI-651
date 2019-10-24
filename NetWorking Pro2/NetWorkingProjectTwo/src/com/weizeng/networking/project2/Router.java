//package com.weizeng.networking.project2;
/*
 * Wei Zeng
 * CSCI651-Project2
 * 10/18/2018
 * */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Router {
	private int routerNumber;// the number of buoy
	private int portNumber;// In this project, it is 63001
	private String address;// the address of the buoy
	private String[] neighbors;// the addresses of the buoy's neighbors
	private RoutingTable routingTable;// the routing table of the buoy
	private String[] netWork;// the addresses of the network linked with the buoy

	public void setRouterNumber(int routerNumber) {
		this.routerNumber = routerNumber;
	}

	public int getRouterNumber() {
		return routerNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setNeighbors(String[] neighbors) {
		this.neighbors = neighbors;
	}

	public String[] getNeighbors() {
		return neighbors;
	}

	public void setRoutingTable(RoutingTable routingTable) {
		this.routingTable = routingTable;
	}

	public RoutingTable getRoutingTable() {
		return routingTable;
	}

	public void setNetWork(String[] netWork) {

		this.netWork = netWork;
	}

	public String[] getNetWork() {
		return netWork;
	}

	public static Router initRouter(String fileName) {
		/*
		 *Read a configure file with the file name
		 *Initialize the router according to the configure file
		 * */
		File configFile = new File(fileName);
		BufferedReader reader = null;
		Router router = new Router();
		try {
			reader = new BufferedReader(new FileReader(configFile));
			String tempString = null;
			String[] neighbors = new String[1000];
			int neighborsIndex = 0;
			String[] netWork = new String[256];
			int netWorkIndex = 0;
			while ((tempString = reader.readLine()) != null) {
				String[] routerInfoArr = tempString.split(": ");
				if (routerInfoArr[0].equals("BUOY")) {
					router.setRouterNumber(Integer.valueOf(routerInfoArr[1]));
				}
				if (routerInfoArr[0].equals("PORT")) {
					router.setPortNumber(Integer.valueOf(routerInfoArr[1]));
				}
				if (routerInfoArr[0].equals("ADDRESS")) {
					router.setAddress(routerInfoArr[1]);
				}
				if (routerInfoArr[0].equals("NEIGHBOR")) {
					neighbors[neighborsIndex] = routerInfoArr[1];
					neighborsIndex++;
				}
				if (routerInfoArr[0].equals("NETWORK")) {
					netWork[netWorkIndex] = routerInfoArr[1].split(" ")[0];
					netWorkIndex++;
				}

			}
			router.setNeighbors(neighbors);
			router.setNetWork(netWork);
			RoutingTable routingTable = new RoutingTable();
			ArrayList<String> routingTableAdd = new ArrayList<String>();
			ArrayList<Integer> routingTableNextHop = new ArrayList<Integer>();
			ArrayList<Integer> routingTableCost = new ArrayList<Integer>();
			for (int i = 0; i < netWorkIndex; i++) {
				routingTableAdd.add(router.getNetWork()[i]);
				routingTableNextHop.add(Integer.valueOf(router.getRouterNumber()));
				routingTableCost.add(Integer.valueOf(0));
			}
			routingTable.setAddress(routingTableAdd);
			routingTable.setNextHop(routingTableNextHop);
			routingTable.setCost(routingTableCost);
			router.setRoutingTable(routingTable);
			System.out.println("Initialization Has Already Been Completed!");
			System.out.println("The Local Routing Table Will Print:\n");
			System.out.println("Address" + "           " + "Next Hop" + "      " + "Cost");
			System.out.println("======================================");
			System.out.println(routingTable.toString());
			System.out.println("\nRouting Table Will Be Updated For Every TEN Seconds By Using RIPv2!\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return router;
	}

	public void sendRoutingInfoToNei(Router router) {
		/*
		 * Use horizontal partition to pack the routing table information 
		 * */
		int tempLength = 0;
		for (int i = 0; i < router.getNeighbors().length; i++) {
			if (router.getNeighbors()[i] == null) {
				break;
			} else {
				tempLength++;
			}
		}

		for (int i = 0; i < tempLength; i++) {
			try {
				DatagramSocket sendSocket = new DatagramSocket();
				String data = String.valueOf(router.getRouterNumber()) + "\n";
				String lastNum = router.getNeighbors()[i].substring(router.getNeighbors()[i].length() - 1);
				for (int j = 0; j < router.getRoutingTable().getNextHop().size(); j++) {
					String nextHopStr = String.valueOf(router.getRoutingTable().getNextHop().get(j));
					if (!(lastNum.equals(nextHopStr))) {
						//if the next hop is the destination router, do not send the routing item
						data = data + router.getRoutingTable().getAddress().get(j) + "          "
								+ router.getRoutingTable().getNextHop().get(j) + "          "
								+ router.getRoutingTable().getCost().get(j) + "\n";
					}
				}
				Send send = new Send(sendSocket, router.getPortNumber(), router.getNeighbors()[i], data);
				new Thread(send).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void CIDR() {
		/*
		 * Use CIDR to manage IP addresses and then update routing table
		 * 
		 * */
		ArrayList<String> addArr = new ArrayList<>();
		ArrayList<Integer> nextHArr = new ArrayList<>();
		ArrayList<Integer> costArr = new ArrayList<>();
		for (int i = 0; i < this.getNetWork().length; i++) {
			if (this.getNetWork()[i] != null) {
				String address = this.getNetWork()[i];
				String[] netWorkAdds = address.split("/");
				String netWorkAdd = address.split("/")[0];
				String mask = address.split("/")[1];
				String[] netWorkNumArr = netWorkAdd.split("\\.");
				String netWorkNumStr = "";
				for (int j = 0; j < netWorkNumArr.length; j++) {
					int temp = Integer.valueOf(netWorkNumArr[j]);
					String tempS = Integer.toBinaryString(temp);
					String tempBinS = String.format("%08d", Integer.parseInt(tempS));
					netWorkNumStr += tempBinS;
				}
				String maskStr = "";
				for (int k = 0; k < Integer.valueOf(mask); k++) {
					maskStr += "1";
				}
				for (int k = 0; k < 32 - Integer.valueOf(mask); k++) {
					maskStr += "0";
				}
				String resultStr = "";
				for (int k = 0; k < 32; k++) {
					if (netWorkNumStr.charAt(k) == '1' && maskStr.charAt(k) == '1') {
						resultStr += "1";
					} else {
						resultStr += "0";
					}
				}
				String netResultStr = "";
				for (int k = 0; k < 32; k += 8) {
					String temp = resultStr.substring(k, k + 8);
					int tempInt = Integer.parseInt(temp, 2);
					if (k != 24) {
						netResultStr = netResultStr + String.valueOf(tempInt) + ".";
					} else {
						netResultStr = netResultStr + String.valueOf(tempInt);
					}
				}
				netResultStr = netResultStr + "/" + mask;
				if (!addArr.contains(netResultStr)) {
					addArr.add(netResultStr);
					nextHArr.add(this.getRouterNumber());
					costArr.add(0);
				}
			}
		}
		this.getRoutingTable().setAddress(addArr);
		this.getRoutingTable().setNextHop(nextHArr);
		this.getRoutingTable().setCost(costArr);
		System.out.println("");
		System.out.println("CIDR Has Been Used! Routing Table Has Been Updated!");
	}

	public static void main(String[] args) {
		String fileName = args[0];
		Router router = initRouter(fileName);// initialize router 

		router.CIDR();// manage local IP addresses

		try {
			/*
			 * Create the receive socket to monitor the port 63001
			 * */
			MulticastSocket recieveSocket = new MulticastSocket(router.getPortNumber());
			recieveSocket.joinGroup(InetAddress.getByName(router.getAddress()));
			String[] recTimeTable = new String[1000];
			Recieve recieve = new Recieve(router.getRoutingTable(), recieveSocket, recTimeTable);
			new Thread(recieve).start();
            
			/*
             * Use a thread to print routing table every 10 seconds
             * */
			Timer printTimer = new Timer();
			printTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					int netWorkL = 0;
					for (int i = 0; i < router.getNetWork().length; i++) {
						if (router.getNetWork()[i] != null) {
							netWorkL++;
						}
					}
					if (router.getRoutingTable().getAddress().size() > netWorkL) {
						LocalTime currTime = LocalTime.now();
						int currTimeV = currTime.toSecondOfDay();
						for (int i = 0; i < router.getRoutingTable().getNextHop().size(); i++) {
							if (router.getRoutingTable().getNextHop().get(i) != router.getRouterNumber()) {
								int recTimeV = Integer.valueOf(
										recieve.getRecTimeTable()[router.getRoutingTable().getNextHop().get(i)]);
								if (currTimeV - recTimeV > 10) {
									router.getRoutingTable().getCost().set(i, 16); // Poison Reverse 
								}
							}
						}
					}
					System.out.println("Address" + "           " + "Next Hop" + "      " + "Cost");
					System.out.println("======================================");
					System.out.println(router.routingTable.toString());
					System.out.println("======================================");
					System.out.println("\n\n\n\n");
				}
			}, 10000, 10000);
            /*
             * Use a thread to judge the state of neighbors every 5 seconds
             * if the router does not receive a packet from a specific router then set the router offline  
             * */
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					router.sendRoutingInfoToNei(router);
				}
			}, 5000, 5000);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
