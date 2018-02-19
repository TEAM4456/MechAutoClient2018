package org.mechcadets;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.EntryListenerFlags;

public class DSAutoClient {
	
	private static NetworkTableInstance inst;
	
	private static NetworkTable autonomousData;
	private static NetworkTable robotData;
	private static NetworkTable bufferData;
	
	public static void main(String[] args) {
		
		inst = NetworkTableInstance.getDefault();
		
		autonomousData = inst.getTable("AutonomousData");
		robotData = autonomousData.getSubTable("RobotState");
		bufferData = autonomousData.getSubTable("BufferData");
		
		inst.startClientTeam(4456);
		
		NetworkTableEntry testEntry = bufferData.getEntry("test");
		
		try {
			while (true) {
				System.out.println(testEntry.getDouble(0));
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted!");
			return;
		}
		
	}
	
}
