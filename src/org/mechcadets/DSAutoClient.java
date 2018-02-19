package org.mechcadets;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.EntryListenerFlags;

public class DSAutoClient {
	
	public static void main(String[] args) {
		
		NetworkTableInstance inst = NetworkTableInstance.getDefault();
		
		NetworkTable table = inst.getTable("AutoRecordings");
		
	}
	
}
