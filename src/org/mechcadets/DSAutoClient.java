package org.mechcadets;

import java.util.Scanner;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class DSAutoClient {
	
	private static final Scanner SCAN = new Scanner(System.in);
	
	private static NetworkTableInstance inst;
	
	private NetworkTable autonomousData;
	private NetworkTable robotData;
	private NetworkTable bufferData;
	
	private NetworkTableEntry pingEntry;
	private NetworkTableEntry robotTickEntry;
	private NetworkTableEntry robotTickTimerEntry;
	
	private static int tick;
	
	public static void main(String[] args) { new DSAutoClient().run(); }
	
	private void run() {
		
		inst = NetworkTableInstance.getDefault();
		
		autonomousData = inst.getTable("AutonomousData");
		robotData = autonomousData.getSubTable("RobotState");
		bufferData = autonomousData.getSubTable("BufferData");
		
		pingEntry = robotData.getEntry("ping");
		robotTickEntry = robotData.getEntry("tick");
		robotTickTimerEntry = robotData.getEntry("tickTimer");
		
		inst.startClientTeam(4456);
		
		waitForConnection(); // maybe replace with inst.addConnectionListener(...);
		
		int timerListenerHandle = robotTickTimerEntry.addListener(event -> {
			onTimer(event.value.getDouble());
		}, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);
		
		do {
			System.out.println("Enter 'quit' to exit.");
		} while (!SCAN.nextLine().toLowerCase().equals("quit"));
		
	}
	
	private void waitForConnection() {
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
	
	private void onTimer(double timerVal) {
		System.out.println("Timer val: " + timerVal);
		if (!pingEntry.getBoolean(true)) {
			System.out.println("Robot->Client ping received! Sending pong...");
			pingEntry.setBoolean(true);
		}
	}
	
}
