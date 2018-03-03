package org.mechcadets.auto2018;

import java.util.Scanner;
import java.util.Map;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class RecordingReader {
	
	public static final Scanner SCAN = new Scanner(System.in);
	
	public static void main(String[] args) {
	
		System.out.print("Please enter the filename of the recording: ");
		String filename = SCAN.nextLine();
		
		AutoRecording recording = null;
		
		FileInputStream fileIn = null;
		ObjectInputStream objectIn = null;
		
		try {
			
			fileIn = new FileInputStream(filename);
			objectIn = new ObjectInputStream(fileIn);
			
			recording = (AutoRecording)objectIn.readObject();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fileIn != null) {
				try {
					fileIn.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			if (objectIn != null) {
				try {
					objectIn.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		System.out.println("Recording name: " + recording.getName());
		System.out.println("Tick interval (ms): " + recording.getTickIntervalMs());
		System.out.println("Stop tick: " + recording.getStopTick());
		System.out.println("Talon modes: " + recording.getTalonModeMap().toString());
		
		System.out.println("TICK VALUES:");
		int stopTick = recording.getStopTick();
		for (int i = 0; i <= stopTick; i++) {
			Map<String, Double> tickValues = recording.getTickValues(i);
			System.out.println("Tick: " + i + ", values: " + tickValues.toString());
		}
	
	}
	
}
