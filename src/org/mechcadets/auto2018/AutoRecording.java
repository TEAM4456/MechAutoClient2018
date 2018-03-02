package org.mechcadets.auto2018;

import java.util.Map;
import java.util.HashMap;

public class AutoRecording {
	
	private String name;
	
	private double bufferSize;
	private double tickIntervalMs;
	
	private Map<String, String> talonModeMap; /* <talonName, talonMode> */
	private Map<Integer, Map<String, Double>> tickMap; /* <tick, <talonName, value>> */
	
	public AutoRecording() {
		name = "";
		bufferSize = 0;
		tickIntervalMs = 0;
		tickMap = new HashMap<>();
		talonModeMap = new HashMap<>();
	}
	
	public AutoRecording(String recordingName, double recordingBufferSize, double recordingTickIntervalMs,
	                     Map<String, String> modes) {
		name = recordingName;
		bufferSize = recordingBufferSize;
		tickIntervalMs = recordingTickIntervalMs;
		tickMap = new HashMap<>();
		talonModeMap = modes;
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
	
	public void setName(String name) {
		name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setBufferSize(int size) {
		bufferSize = size;
	}
	
	public double getBufferSize() {
		return bufferSize;
	}
	
	public void setTickIntervalMs(int intervalMs) {
		tickIntervalMs = intervalMs;
	}
	
	public double getTickIntervalMs() {
		return tickIntervalMs;
	}
	
}
