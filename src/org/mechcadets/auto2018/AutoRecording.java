package org.mechcadets.auto2018;

import java.util.Map;
import java.util.HashMap;

public class AutoRecording {
	
	private String recordingName;
	
	private int bufferSize;
	private int tickIntervalMs;
	
	private Map<String, String> talonModeMap; /* <talonName, talonMode> */
	private Map<Integer, Map<String, Double>> tickMap; /* <tick, <talonName, value>> */
	
	public AutoRecording() {
		bufferSize = 0;
		tickIntervalMs = 0;
		tickMap = new HashMap<>();
	}
	
	public AutoRecording(int recordingBufferSize, int recordingTickIntervalMs) {
		bufferSize = recordingBufferSize;
		tickIntervalMs = recordingTickIntervalMs;
		tickMap = new HashMap<>();
	}
	
	public void addTick(int tick, Map<String, Double> tickValues) {
		tickMap.put(tick, tickValues);
	}
	
	public Map<String, Double> getTickValues(int tick) {
		return tickMap.get(tick);
	}
	
	public void setTalonModeMap(Map<String, String> map) {
		talonModeMap = map;
	}
	
	public Map<String, String> getTalonModeMap() {
		return talonModeMap;
	}
	
	public void setRecordingName(String name) {
		recordingName = name;
	}
	
	public String getRecordingName() {
		return recordingName;
	}
	
	public void setBufferSize(int size) {
		bufferSize = size;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public void setTickIntervalMs(int intervalMs) {
		tickIntervalMs = intervalMs;
	}
	
	public int getTickIntervalMs() {
		return tickIntervalMs;
	}
	
}
