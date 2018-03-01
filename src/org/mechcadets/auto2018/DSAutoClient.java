package org.mechcadets.auto2018;

import java.util.Scanner;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class DSAutoClient {
	
	private static final Scanner SCAN = new Scanner(System.in);
	
	private static NetworkTableInstance inst;
	
	private static int tick;
	
	private NetworkTable autonomousData;
	private NetworkTable robotData;
	private NetworkTable bufferData;
	
	private NetworkTableEntry pingEntry;
	private NetworkTableEntry robotTickEntry;
	private NetworkTableEntry syncStopTickEntry;
	private NetworkTableEntry tickIntervalMsEntry;
	private NetworkTableEntry robotTickTimerEntry;
	private NetworkTableEntry bufferSizeEntry;
	private NetworkTableEntry talonModesEntry;
	private NetworkTableEntry managerModeEntry;
	private NetworkTableEntry robotReadyEntry;
	private NetworkTableEntry clientIsReadyEntry;
	private NetworkTableEntry recordingNameEntry;
	
	public static void main(String[] args) { new DSAutoClient().run(); }
	
	private void run() {
		
		tick = 0;
		
		inst = NetworkTableInstance.getDefault();
		
		autonomousData = inst.getTable("AutonomousData");
		robotData = autonomousData.getSubTable("RobotState");
		bufferData = autonomousData.getSubTable("BufferData");
		
		pingEntry = robotData.getEntry("ping");
		robotTickEntry = robotData.getEntry("tick");
		syncStopTickEntry = robotData.getEntry("syncStopTick");
		tickIntervalMsEntry = robotData.getEntry("tickIntervalMs");
		robotTickTimerEntry = robotData.getEntry("tickTimer");
		bufferSizeEntry = robotData.getEntry("bufferSize");
		talonModesEntry = robotData.getEntry("talonModes");
		managerModeEntry = robotData.getEntry("managerMode");
		robotReadyEntry = robotData.getEntry("robotReady");
		clientIsReadyEntry = robotData.getEntry("clientIsReady");
		recordingNameEntry = robotData.getEntry("recordingName");
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
		
		/* move to end later */
		if (!pingEntry.getBoolean(true)) {
			System.out.println("Robot->Client ping received! Sending pong...");
			pingEntry.setBoolean(true);
		}
		
		// testing
		if (managerModeEntry.getString("").equals("RECORD_RUNNING")) {
			System.out.println("RECORD_RUNNING");
		}
		
	}
	
}
