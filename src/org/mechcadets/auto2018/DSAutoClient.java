package org.mechcadets.auto2018;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class DSAutoClient {
	
	/*
	This program was not made as robustly as the robot's portion.
	It's not necessarily spaghetti, but definitely not as robust.
	
	Known bugs:
		- if playback is stopped too quickly after starting, client will never start playback again until restart
			- possible causes:
				- playback is stopped before client gets another ping from robot (onPing() isn't called yet)
				- playback is stopped within n < bufferSize ticks
	*/
	
	private final int TIMEOUT_S = 2;
	private final int LOOP_WAIT_MS = 100;
	
	private String lastMessage;
	private long messageRepeatCounter;
	
	private boolean recordingRunning;
	private boolean playbackRunning;
	
	private int clientTick;
	private double bufferSize;
	
	private long timeoutCounter;
	
	private List<String> talonList;
	
	private AutoRecording recording;
	
	private NetworkTableInstance inst;
	
	private NetworkTable autonomousData;
	private NetworkTable robotData;
	private NetworkTable bufferData;
	
	private NetworkTableEntry robotEnabledEntry;
	private NetworkTableEntry pingEntry;
	private NetworkTableEntry robotTickEntry;
	private NetworkTableEntry syncStopTickEntry;
	private NetworkTableEntry intervalEntry;
	private NetworkTableEntry bufferSizeEntry;
	private NetworkTableEntry talonModesEntry;
	private NetworkTableEntry managerModeEntry;
	private NetworkTableEntry recordingNameEntry;
	
	private NetworkTableEntry[][] talonBufferArray;
	
	public static void main(String[] args) { new DSAutoClient().run(); }
	
	private void run() {
		
		lastMessage = "";
		messageRepeatCounter = 1;
		
		recordingRunning = false;
		playbackRunning = false;
		
		clientTick = 0;
		bufferSize = 0;
		
		talonList = new ArrayList<>();
		
		inst = NetworkTableInstance.getDefault();
		
		autonomousData = inst.getTable("AutonomousData");
		robotData = autonomousData.getSubTable("RobotState");
		bufferData = autonomousData.getSubTable("BufferData");
		
		robotEnabledEntry = robotData.getEntry("enabled");
		pingEntry = robotData.getEntry("ping");
		robotTickEntry = robotData.getEntry("tick");
		syncStopTickEntry = robotData.getEntry("syncStopTick");
		intervalEntry = robotData.getEntry("interval");
		bufferSizeEntry = robotData.getEntry("bufferSize");
		talonModesEntry = robotData.getEntry("talonModes");
		managerModeEntry = robotData.getEntry("managerMode");
		recordingNameEntry = robotData.getEntry("recordingName");
		
		inst.startClientTeam(4456);
		
		waitForConnection();
		
		int robotEnabledListenerHandle = robotEnabledEntry.addListener(event -> {
			onEnabledUpdate(event.value.getBoolean());
		}, EntryListenerFlags.kNew | EntryListenerFlags.kImmediate | EntryListenerFlags.kUpdate);
		int pingListenerHandle = pingEntry.addListener(event -> {
			onPing(event.value.getBoolean());
		}, EntryListenerFlags.kNew | EntryListenerFlags.kImmediate | EntryListenerFlags.kUpdate);
		
		while (true) {
			if (timeoutCounter > (TIMEOUT_S * 1000) / LOOP_WAIT_MS) {
				if (recordingRunning) {
					printMessage("WARNING: timeout time exceeded while recording! Cancelling recording...");
					stopRecording(false);
				} else if (playbackRunning) {
					printMessage("WARNING: timeout time exceeded during playback! Stopping playback.");
					stopPlayback();
				} else {
					printMessage("WARNING: timeout time exceeded while idle!");
				}
			}
			timeoutCounter++;
			try {
				Thread.sleep(LOOP_WAIT_MS);
			} catch (InterruptedException ex) {
				printMessage("Interrupted during main loop.\nExiting...");
				robotEnabledEntry.removeListener(robotEnabledListenerHandle); // just to be safe
				pingEntry.removeListener(pingListenerHandle); // just to be safe
				System.exit(0);
			}
		}
		
	}
	
	private void printMessage(String message) {
		if (!message.equals(lastMessage)) {
			lastMessage = message;
			System.out.println(message);
			messageRepeatCounter = 1;
		} else {
			messageRepeatCounter++;
			System.out.println(message + " (x" + messageRepeatCounter + ")");
		}
	}
	
	private void waitForConnection() {
		try {
			while (!inst.isConnected()) {
				printMessage("Waiting for connection...");
				Thread.sleep(LOOP_WAIT_MS);
			}
			printMessage("Connected!");
		} catch (InterruptedException ex) {
			printMessage("Interrupted while waiting for connection.\nExiting...");
			System.exit(0);
		}
	}
	
	private void onEnabledUpdate(boolean enabled) {
		if (!enabled) {
			if (recordingRunning) {
				printMessage("WARNING: robot disabled while recording! Cancelling recording...");
				stopRecording(false);
			} else if (playbackRunning) {
				printMessage("WARNING: robot disabled during playback! Stopping playback.");
				stopPlayback();
			}
		}
	}
	
	private void onPing(boolean ping) {
		
		String managerMode = managerModeEntry.getString("");
		double robotTick = robotTickEntry.getDouble(0);
		double syncStopTick = syncStopTickEntry.getDouble(-1);
		
		if (managerMode.equals("RECORD_RUNNING") && !recordingRunning &&
				(robotTick <= syncStopTick - bufferSize || syncStopTick == -1)) {
			recording = startRecording();
			
			printMessage("Recording started.");
			printMessage("Recording name: "+ recording.getName());
			printMessage("Interval: " + recording.getInterval());
			printMessage("Talon modes: " + recording.getTalonModeMap().toString());
		}
		
		if (managerMode.equals("PLAYBACK_RUNNING") && !playbackRunning &&
				(robotTick <= syncStopTick - bufferSize || syncStopTick == -1)) {
			recording = startPlayback();
			
			printMessage("Playback started.");
			printMessage("Recording name: " + recording.getName());
			printMessage("Interval: " + recording.getInterval());
			printMessage("Talon modes: " + recording.getTalonModeMap().toString());
		}
		
		if (recordingRunning) {
			if (syncStopTick == -1) {
				while (clientTick < robotTick) {
					recording.addTick(clientTick, getValuesForTick(clientTick));
					clientTick++;
				}
			} else {
				while (clientTick <= syncStopTick) {
					recording.addTick(clientTick, getValuesForTick(clientTick));
					clientTick++;
				}
				boolean save = !recordingNameEntry.getString("").equals("CLIENT::CANCEL_RECORDING");
				stopRecording(save);
			}
		}
		
		if (playbackRunning) {
			if (clientTick >= syncStopTick) {
				printMessage("STOPPING PLAYBACK.");
				stopPlayback();
			}
			while (clientTick < robotTick + bufferSize - 1) {
				if (clientTick >= syncStopTick) {
					break;
				}
				Map<String, Double> tickValues = recording.getTickValues(clientTick);
				for (Map.Entry<String, Double> entry : tickValues.entrySet()) {
					writeToTalonBuffer(entry.getKey(), clientTick, entry.getValue());
				}
				clientTick++;
			}
		}
		
		if (!ping) {
			printMessage("Ping received! Sending pong...");
			pingEntry.setBoolean(true);
		}
		
		timeoutCounter = 0;
	}
	
	private NetworkTableEntry[] getBufferEntriesForTalon(String talonName) {
		NetworkTableEntry[] entries = new NetworkTableEntry[(int)bufferSize];
		for (int i = 0; i < bufferSize; i++) {
			NetworkTableEntry entry = bufferData.getEntry(talonName + "-" + i);
			entry.setDefaultDouble(0); // sets if doesn't exist
			entries[i] = entry;
		}
		return entries;
	}
	
	private Map<String, Double> getValuesForTick(int tick) {
		Map<String, Double> tickValues = new HashMap<>();
		for (String talonName : talonList) {
			tickValues.put(talonName, readFromTalonBuffer(talonName, tick));
		}
		return tickValues;
	}
	
	private double readFromTalonBuffer(String talonName, int tick) {
		return talonBufferArray[talonList.indexOf(talonName)][tick % (int)bufferSize].getDouble(0);
	}
	
	private void writeToTalonBuffer(String talonName, int tick, double value) {
		talonBufferArray[talonList.indexOf(talonName)][tick % (int)bufferSize].setDouble(value);
	}
	
	private AutoRecording startRecording() {
		if (recordingRunning) {
			System.err.println("ERROR: startRecording() called while recording is running!");
			System.exit(1);
		}
		if (playbackRunning) {
			System.err.println("ERROR: startRecording() called while playback is running!");
			System.exit(1);
		}
		
		String recordingName = recordingNameEntry.getString("");
		double tickIntervalMs = intervalEntry.getDouble(0);
		String talonModes = talonModesEntry.getString("");
		
		talonList.clear();
		Map<String, String> modes = new HashMap<>();
		for (String talonAndMode : talonModes.split("\\|")) {
			String[] talonAndModeArray = talonAndMode.split(":");
			talonList.add(talonAndModeArray[0]);
			modes.put(talonAndModeArray[0], talonAndModeArray[1]);
		}
		
		clientTick = 0;
		bufferSize = bufferSizeEntry.getDouble(0);
		recordingRunning = true;
		
		talonBufferArray = new NetworkTableEntry[talonList.size()][];
		for (int i = 0; i < talonList.size(); i++) {
			talonBufferArray[i] = getBufferEntriesForTalon(talonList.get(i));
		}
		
		return new AutoRecording(recordingName, tickIntervalMs, modes);
	}
	
	private AutoRecording startPlayback() {
		if (playbackRunning) {
			System.err.println("ERROR: startPlayback() called while playback is running!");
			System.exit(1);
		}
		if (recordingRunning) {
			System.err.println("ERROR: startPlayback() called while recording is running!");
			System.exit(1);
		}
		
		String recordingName = recordingNameEntry.getString("");
		
		recording = AutoRecording.loadRecording(recordingName);
		
		intervalEntry.setDouble(recording.getInterval());
		syncStopTickEntry.setDouble(recording.getStopTick());
		
		Map<String, String> modes = recording.getTalonModeMap();
		String modesString = "";
		talonList.clear();
		for (Map.Entry<String, String> mode : modes.entrySet()) {
			talonList.add(mode.getKey());
			modesString += mode.getKey() + ":" + mode.getValue() + "|";
		}
		modesString = modesString.substring(0, modesString.length() - 1); // remove trailing delimiter
		talonModesEntry.setString(modesString);
		
		clientTick = 0;
		bufferSize = bufferSizeEntry.getDouble(0);
		playbackRunning = true;
		
		talonBufferArray = new NetworkTableEntry[talonList.size()][];
		for (int i = 0; i < talonList.size(); i++) {
			talonBufferArray[i] = getBufferEntriesForTalon(talonList.get(i));
		}
		
		return recording;
	}
	
	private void stopRecording(boolean saveRecording) {
		if (!recordingRunning) {
			System.err.println("ERROR: stopRecording() called while recording is not running!");
			System.exit(1);
		} else if (playbackRunning) {
			System.err.println("ERROR: stopRecording() called while playback is running!");
			System.exit(1);
		}
		
		recording.setStopTick(clientTick - 1);
		
		if (saveRecording) {
			AutoRecording.saveRecording(recording);
			printMessage("Recording saved.");
		} else {
			printMessage("Recording cancelled.");
		}
		
		recordingRunning = false;
	}
	
	private void stopPlayback() {
		if (!playbackRunning) {
			System.err.println("ERROR: stopPlayback() called while playback is not running!");
			System.exit(1);
		}
		if (recordingRunning) {
			System.err.println("ERROR: stopPlayback() called while recording is running!");
			System.exit(1);
		}
		
		playbackRunning = false;
	}
	
}
