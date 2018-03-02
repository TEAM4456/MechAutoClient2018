package org.mechcadets.auto2018;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class DSAutoClient {
	
	private boolean recordingRunning;
	private boolean playbackRunning;
	
	private int clientTick;
	
	private AutoRecording recording;
	
	private static NetworkTableInstance inst;
	
	private NetworkTable autonomousData;
	private NetworkTable robotData;
	private NetworkTable bufferData;
	
	private NetworkTableEntry pingEntry;
	private NetworkTableEntry robotTickEntry;
	private NetworkTableEntry syncStopTickEntry;
	private NetworkTableEntry tickIntervalMsEntry;
	private NetworkTableEntry tickTimerEntry;
	private NetworkTableEntry bufferSizeEntry;
	private NetworkTableEntry talonModesEntry;
	private NetworkTableEntry managerModeEntry;
	private NetworkTableEntry recordingNameEntry;
	
	private static final Scanner SCAN = new Scanner(System.in);
	
	public static void main(String[] args) { new DSAutoClient().run(); }
	
	private void run() {
		
		recordingRunning = false;
		playbackRunning = false;
		
		clientTick = 0;
		
		inst = NetworkTableInstance.getDefault();
		
		autonomousData = inst.getTable("AutonomousData");
		robotData = autonomousData.getSubTable("RobotState");
		bufferData = autonomousData.getSubTable("BufferData");
		
		pingEntry = robotData.getEntry("ping");
		robotTickEntry = robotData.getEntry("tick");
		syncStopTickEntry = robotData.getEntry("syncStopTick");
		tickIntervalMsEntry = robotData.getEntry("tickIntervalMs");
		tickTimerEntry = robotData.getEntry("tickTimer");
		bufferSizeEntry = robotData.getEntry("bufferSize");
		talonModesEntry = robotData.getEntry("talonModes");
		managerModeEntry = robotData.getEntry("managerMode");
		recordingNameEntry = robotData.getEntry("recordingName");
		
		inst.startClientTeam(4456);
		
		waitForConnection(); // maybe replace with inst.addConnectionListener(...);
		
		int timerListenerHandle = tickTimerEntry.addListener(event -> {
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
		
		String managerMode = managerModeEntry.getString("");
		
		if (managerMode.equals("IDLE")) {
			if (recordingRunning) {
				if (!recordingNameEntry.equals("CLIENT::CANCEL_RECORDING")) {
					System.out.println("Recording stopped. Saving as " + recording.getName() + "...");
					stopAndSaveRecording(recording);
				} else {
					System.out.println("Recording cancelled.");
					stopRecording();
				}
			} else if (playbackRunning) {
				System.out.println("Playback stopped");
				playbackRunning = false;
			}
		}
		
		if (managerMode.equals("RECORD_RUNNING") && !recordingRunning) {
			recording = startRecording();
			
			System.out.println("Recording started.");
			System.out.println("Recording name: "+ recording.getName());
			System.out.println("Buffer size: " + recording.getBufferSize());
			System.out.println("Tick interval (ms): " + recording.getTickIntervalMs());
			System.out.println("Talon modes: " + recording.getTalonModeMap().toString());
		}
		
		//if (managerMode.equals("PLAYBACK_RUNNING") && !playbackRunning) {}
		
		if (recordingRunning) {
		
		}
		
		//if (playbackRunning) {}
		
		pingRobot();
	}
	
	private AutoRecording startRecording() {
		// debugging
		if (recordingRunning) {
			System.err.println("DEBUG: startRecording() called while recording is running!");
			System.exit(1);
		} else if (playbackRunning) {
			System.err.println("DEBUG: startRecording() called while playback is running!");
			System.exit(1);
		}
		
		String recordingName = recordingNameEntry.getString("");
		double bufferSize = bufferSizeEntry.getDouble(0);
		double tickIntervalMs = tickIntervalMsEntry.getDouble(0);
		String talonModes = talonModesEntry.getString("");
		
		Map<String, String> modes = new HashMap<>();
		for (String talonAndMode : talonModes.split("\\|")) {
			String[] talonAndModeArray = talonAndMode.split(":");
			modes.put(talonAndModeArray[0], talonAndModeArray[1]);
		}
		
		recordingRunning = true;
		
		return new AutoRecording(recordingName, bufferSize, tickIntervalMs, modes);
	}
	
	private void stopAndSaveRecording(AutoRecording recording) {
		// debugging
		if (!recordingRunning) {
			System.err.println("DEBUG: stopAndSaveRecording() called while recording is not running!");
			System.exit(1);
		} else if (playbackRunning) {
			System.err.println("DEBUG: stopAndSaveRecording() called while playback is running!");
			System.exit(1);
		}
		
		recordingRunning = false;
	}
	
	private void stopRecording() {
		// debugging
		if (!recordingRunning) {
			System.err.println("DEBUG: stopRecording() called while recording is not running!");
			System.exit(1);
		} else if (playbackRunning) {
			System.err.println("DEBUG: stopRecording() called while playback is running!");
			System.exit(1);
		}
		
		recordingRunning = false;
	}
	
	private void pingRobot() {
		if (!pingEntry.getBoolean(true)) {
			System.out.println("Ping received! Sending pong...");
			pingEntry.setBoolean(true);
		}
	}
	
}
