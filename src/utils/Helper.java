package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Helper {

	/*
	 * getList of Block names
	 */
	public  ArrayList<String> getBlockNames() throws FileNotFoundException {
		File file = new File("FilesRequired/BlockList.txt");
		Scanner sc = new Scanner(file);
		ArrayList<String> blockList = new ArrayList<>();
		while(sc.hasNextLine()) 
			blockList.add(sc.nextLine());
		sc.close();
		return blockList;
	}
	/*
	 * checks if the parameter passed in is an ID
	 */
	public  boolean checkIfID(String ID) {
		int count = 0;
		for(int i=0;i<ID.length();i++) {
			if(ID.charAt(i)=='-')
				count++;
		}
		return count==4?true:false;
	}
	
	
}
