package org.mechcadets;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class DSAutoClient {
	
	private static NetworkTableInstance inst;
	
	private static NetworkTable autonomousData;
	private static NetworkTable robotData;
	private static NetworkTable bufferData;
	
	private static NetworkTableEntry robotTickEntry;
	
	private static int tick;
	
	public static void main(String[] args) {
		
		inst = NetworkTableInstance.getDefault();
		
		autonomousData = inst.getTable("AutonomousData");
		robotData = autonomousData.getSubTable("RobotState");
		bufferData = autonomousData.getSubTable("BufferData");
		
		robotTickEntry = robotData.getEntry("tick");
		
		inst.startClientTeam(4456);
		
		waitForConnection(); // maybe replace with inst.addConnectionListener(...);
		
		tick = (int)robotTickEntry.getDouble(0);
		
		robotTickEntry.addListener(event -> {
			System.out.println("Tick changed: " + event.value.getDouble());
		}, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);
		
	}
	
	private static void waitForConnection() {
		try {
			while (!inst.isConnected()) {
				System.out.println("Waiting for connection...");
				Thread.sleep(100);
			}
			System.out.println("Connected!");
		} catch (InterruptedException ex) {
			System.out.println("Interrupted while waiting for connection.\nExiting...");
			System.exit(0);
		}
	}
	
}
