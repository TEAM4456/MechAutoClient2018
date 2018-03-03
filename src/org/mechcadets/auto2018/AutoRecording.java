package org.mechcadets.auto2018;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class AutoRecording implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private double tickIntervalMs;
	private int stopTick;
	
	private Map<String, String> talonModeMap; /* <talonName, talonMode> */
	private Map<Integer, Map<String, Double>> tickMap; /* <tick, <talonName, value>> */
	
	public AutoRecording(String recordingName, double recordingTickIntervalMs, Map<String, String> modes) {
		name = recordingName;
		tickIntervalMs = recordingTickIntervalMs;
		stopTick = 0;
		tickMap = new HashMap<>();
		talonModeMap = modes;
	}
	
	public static void saveRecording(AutoRecording recording) {
		FileOutputStream fileOut = null;
		ObjectOutputStream objectOut = null;
		try {
			String filepath = "[FILEPATH REDACTED]";
			fileOut = new FileOutputStream(filepath + recording.getName() + ".arf");
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(recording);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			if (objectOut != null) {
				try {
					objectOut.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public void setStopTick(int tick) {
		stopTick = tick;
	}
	
	public int getStopTick() {
		return stopTick;
	}
	
	public void addTick(int tick, Map<String, Double> tickValues) {
		tickMap.put(tick, tickValues);
	}
	
	public Map<String, Double> getTickValues(int tick) {
		return tickMap.get(tick);
	}
	
	public Map<String, String> getTalonModeMap() {
		return talonModeMap;
	}
	
	public String getName() {
		return name;
	}
	
	public double getTickIntervalMs() {
		return tickIntervalMs;
	}
	
}
