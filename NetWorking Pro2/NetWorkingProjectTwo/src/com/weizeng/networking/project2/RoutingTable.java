//package com.weizeng.networking.project2;
/*
 * Wei Zeng
 * CSCI651-Project2
 * 10/18/2018
 * */
import java.util.ArrayList;

public class RoutingTable {
	private ArrayList<String> address;
	private ArrayList<Integer> nextHop;
	private ArrayList<Integer> cost;

	public void setAddress(ArrayList<String> address) {
		this.address = address;
	}

	public ArrayList<String> getAddress() {
		return address;
	}

	public void setNextHop(ArrayList<Integer> nextHop) {
		this.nextHop = nextHop;
	}

	public ArrayList<Integer> getNextHop() {
		return nextHop;
	}

	public void setCost(ArrayList<Integer> cost) {
		this.cost = cost;
	}

	public ArrayList<Integer> getCost() {
		return cost;
	}

	public void updateTable(String[][] routingData, int routerNum) {
		/*
		 * After unpacking the receive packets then update the routing table
		 * */
		for (int i = 0; i < routingData.length; i++) {
			String address = routingData[i][0];
			int cost = Integer.valueOf(routingData[i][2]);
			if (this.address.contains(address)) {
				int addIndex = this.address.indexOf(address);
				if (cost < 15 && cost + 1 < this.cost.get(addIndex)) {
					this.nextHop.set(addIndex, routerNum);
					this.cost.set(addIndex, cost + 1);
				}
			} else {
				if (cost < 15) {
					this.address.add(address);
					this.nextHop.add(routerNum);
					this.cost.add(cost + 1);
				}
			}
		}
	}

	@Override
	public String toString() {
		String routingTableInfo = "";
		for (int i = 0; i < this.address.size(); i++) {
			routingTableInfo = routingTableInfo + this.address.get(i) + "          " + this.nextHop.get(i)
					+ "          " + this.cost.get(i) + "\n";
		}
		return routingTableInfo;
	}

}
