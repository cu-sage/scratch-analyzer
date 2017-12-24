package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class ReadFile {
	
	 public static void showDirectory(String filePath, Map<Integer, TreeMap<String, ArrayList<Tree<Block>>>> map){
		 	// create the root file with filePath
		 	File file = new File(filePath);
	        File[] files = file.listFiles();
			//traverse every user
	        for(File a : files){
	        if(a.getName().equals(".DS_Store"))
	        		continue;
	        int 	userID = Integer.valueOf(a.getName());
	        TreeMap<String, ArrayList<Tree<Block>>> user = new TreeMap<>();
	        File[] projects = a.listFiles();
	        //traverse every project per user
	        for(File p : projects) {
	        	if(p.getName().equals(".DS_Store"))
	        		continue;
	        	String projectID = p.getName();
	        	ArrayList<Tree<Block>> list = new ArrayList<>();
	        	File[] trees = p.listFiles();
	        	//traverse every .se file per project
	        	for(File tr : trees) {
	        		if(tr.getName().equals(".DS_Store"))
		        		continue;
	        		int preCount = 0;
	    			int count;
	    			Block preBlock = null;
	    			Tree<Block> t = null;
	        		try { 
	        		    BufferedReader reader = new BufferedReader(new FileReader(tr));
	        	    		String line = null;
	        	    		try {
	        	    			
	        	    			Stack<Block> stack = new Stack<>();
	        	    		while ((line = reader.readLine()) != null) {
//	        	    			System.out.println(line);
	        	    			count = 0;
	        	    			while (count < line.length() && line.charAt(count) == '\t') {
	        	    				count++;
	        	    			}
	        	    			if (count >= line.length())
	        	    				continue;
//	        	    			System.out.println(line + " " + stack.size()+" "+preCount + " " +count);
	        	    			String s = line.substring(count);
	        	    			Block newBlock = new Block(s);

	        	    			if (count == preCount) {
	        	    				if (stack.empty()) {
	        	    					t = new Tree(newBlock, projectID);
	        	    				}
	        	    				else {
	        	    					t.addLeaf(stack.peek(), newBlock);
	        	    				}
	        	    			}
	        	    			else if (count > preCount) {
	        	    				stack.push(preBlock);
	        	    				t.addLeaf(stack.peek(), newBlock);
	        	    			}
	        	    			else {
	        	    				while (preCount > count) {
	        	    					stack.pop();
	        	    					preCount-=2;
	        	    				}
	        	    				t.addLeaf(stack.peek(), newBlock);
	        	    			}

	        	    			preBlock = newBlock;
	        	    			preCount = count;
	        	    		}
	        	    		reader.close();
	        	    		//list.add(e);
	        	    		}catch (IOException e) {
	        	    			e.printStackTrace();
	        	    		}
	        			}catch(FileNotFoundException e) {
	        	                e.printStackTrace();
	        	            }
//	        		System.out.println(t.printTree(0));
	        		list.add(t);
	        	}
	        	user.put(projectID, list);
	        }
	        map.put(userID, user);
	        }
//	        for(Map.Entry<Integer, TreeMap<String, ArrayList<Tree<Block>>>> entry : map.entrySet()) {
//	        	set.add(entry);
//	        }
	    }
	 
	 /*
		 * Analyzes all the projects per user and outputs the analysis in a 
		 */
		public static void analyzeData(String dir, Map<Integer, TreeMap<String, ArrayList<Tree<Block>>>> map) throws IOException {
			ReadTreeMock file;
			Writer writer = makeFile(dir);
			for(int user : map.keySet()) {

				for(String project : map.get(user).keySet()) {
					
					Path output = Paths.get(dir.toString(),"/",Integer.toString(user),"/",project);
					Files.createDirectories(output);
					file = new ReadTreeMock(output,map.get(user).get(project));
					file.aggregateTimeStampedProjects();
					StatisticalData dataForThisUser = file.getStatisticalData();
					writer = writeForThisProject(user, project, dataForThisUser, writer);
				}
			}
			writer.close();
		}
		
		/*
		 Makes the basic skeletal file for the statistical analysis
		 */
		public static Writer makeFile(String dir) {
			Writer writer = null;
			try {
				writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(Paths.get(dir.toString(), "StatiscalAnalysis.csv").toString()), "utf-8"));
				writer.write("UserId" + "," + "ProjectName" + "," + "changePerBlock_mean" + "," + "changePerBlock_sd, changePerBlock_max, changePerBlock_min, changePerBlock_range, changePerBlock_skewness"+ ", changeInterval_mean, changeInterval_sd, changeInterval_max, changeInterval_min, changeInterval_range, changeInterval_skewness, "
						+ "blockInterval_mean, blockInterval_sd, blockInterval_max, blockInterval_min, blockInterval_range, blockInterval_skewness, " + "changePerInterval_mean, changePerInterval_sd, changePerInterval_max, changePerInterval_min, changePerInterval_range, changePerInterval_skewness" + " \n");
			}
			catch(IOException e) {
				System.out.println(e);
			}
			return writer;
		}
		/*
		 * Writes a per user per project statistical analysis to our output file
		 */
		public static Writer writeForThisProject(int user, String project, StatisticalData data,Writer writer) {
			try {
				if (!data.isValid()) return writer;
				writer.write(user + "," + project + "," + data.changePerBlock_mean + "," + data.changePerBlock_sd+ "," + data.changePerBlock_max + ","+ data.changePerBlock_min + "," + data.changePerBlock_range + "," + data.changePerBlock_skewness + "," + data.changeInterval_mean + "," 
				+ data.changeInterval_sd + ","  + data.changeInterval_max + ","  + data.changeInterval_min + ","  + data.changeInterval_range + ","  + data.changeInterval_skewness + "," + data.blockInterval_mean + ", " + data.blockInterval_sd + "," + data.blockInterval_max + "," + data.blockInterval_min + "," + data.blockInterval_range + "," + data.blockInterval_skewness + ","+ data.changePerInterval_mean + ","
						+ data.changePerInterval_sd + "," + data.changePerInterval_max + ","+ data.changePerInterval_min + ","+ data.changePerInterval_range + ","+ data.changePerInterval_skewness +  " \n");
			}
			catch(IOException e) {
				System.out.println(e);
			}
			return writer;
		}
	 
	 
	 public static void main(String[] args) throws IOException{
//		 Set<Entry<Integer, TreeMap<String, ArrayList<Tree<Block>>>>> set = new HashSet<>();
		 Map<Integer, TreeMap<String, ArrayList<Tree<Block>>>> map = new TreeMap<>();
		 String dir1 = "/Users/Bian/Desktop/Others/mockData";
		 String dir2 = "/Users/Bian/Desktop/mockData";
		 
		 showDirectory(dir1, map);
		 analyzeData(dir2, map); 
	} 
}

