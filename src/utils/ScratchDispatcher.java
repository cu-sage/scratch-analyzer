package utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * @author JBender
 * Scratch Dispatcher transforms extracted blocks into simple CSVs compatible 
 * with recommendation engines built using a variety of big data technologies.
 */
public class ScratchDispatcher {

	private Path seDir;
	private Path outputDir;
	private TreeMap<Integer, ArrayList<Tree<Block>>> userProjects; 
	private TreeMap<Integer, TreeMap<String, Integer>> userBlocks;
	private TreeMap<Integer, TreeMap<Integer, TreeMap<String, Integer>>> userBlocksPerProject; //map of userID-ProjectID-BlockName-Counts
	private TreeMap<String, Integer> numericBlocks;
	private static final String OUTPUT_FILE = "dispatch_text.csv";
	private static final String OUTPUT_NUMERIC = "dispatch_numeric.csv";
	private static final String OUTPUT_LKUP = "dispatch_lookup.csv";
	private static final String OUTPUT_PERPROJECT = "dispatch_perProject.csv";
	private static int blockID = 0;
	
	public ScratchDispatcher(Path seDir, Path outputDir)
	{
		this.seDir = seDir;
		this.outputDir = outputDir;
	    this.userProjects = new TreeMap<Integer, ArrayList<Tree<Block>>>();
	    this.userBlocks = new TreeMap<Integer, TreeMap<String, Integer>>();
	    this.numericBlocks = new TreeMap<String, Integer>();
	    this.userBlocksPerProject = new TreeMap<Integer, TreeMap<Integer, TreeMap<String, Integer>>>();
	}
	
	/**
	 * Translate between text and numeric representation of blocks
	 */
	private int getNum(String blockName) {
		if(numericBlocks.containsKey(blockName))
			return numericBlocks.get(blockName);
		else {
			numericBlocks.put(blockName, ++blockID);
			return blockID;
		}
	}
	
	/**
	 * Aggregate the blocks used by each user in all projects
	 */
	private void aggregateUserBlocks() {
		Set<Entry<Integer, ArrayList<Tree<Block>>>> set = userProjects.entrySet(); //tree of projects into sets
		Iterator<Map.Entry<Integer, ArrayList<Tree<Block>>>> iterator = set.iterator();
		TreeMap<String, Integer> blockAggregate;
		//iterate through each tree of projects/user
		while (iterator.hasNext())
		{
			blockAggregate = new TreeMap<String, Integer>(); 
			Map.Entry<Integer, ArrayList<Tree<Block>>> me = (Map.Entry<Integer, ArrayList<Tree<Block>>>)iterator.next(); //returns a tree of projects
			ArrayList<Tree<Block>> al = me.getValue(); //get a list of projects
			//go through projects of the list
			for(Tree<Block> project : al) {
				aggregateProject(project, blockAggregate);
				userBlocks.put(me.getKey(), blockAggregate); //put treeMap of blockNames with their counts into userBLocks
				//me.getKey() returns user ID 
			}
		}
	}
	
	/**
	 * Duplicates functionality of aggregateUserBlocks, but also keeps track of projectID in addition to userID.
	 * ProjectID currently depends on the order of projects ScratchExtractor extracts. UserID still depends on 
	 * directory.
	 * @author Tim K. 
	 * POSTCONDITION: userBlocksPerProject gets a map containing block names with their counts according to User and Project ID. 
	 */
	private void aggregateUserBlocksPerProject() {
		Set<Entry<Integer, ArrayList<Tree<Block>>>> set = userProjects.entrySet(); //tree of projects into sets
		Iterator<Map.Entry<Integer, ArrayList<Tree<Block>>>> iterator = set.iterator();
		TreeMap<Integer, TreeMap<String, Integer>> userMapProject;
		TreeMap<String, Integer> blockAggregate;
		//iterate through each tree of projects/user
		while (iterator.hasNext())
		{
			int projectID = 1; //resets project ID
			userMapProject = new TreeMap<Integer, TreeMap<String, Integer>>(); //map of projects the user has
			Map.Entry<Integer, ArrayList<Tree<Block>>> me = (Map.Entry<Integer, ArrayList<Tree<Block>>>)iterator.next(); //returns a tree of projects
			ArrayList<Tree<Block>> al = me.getValue(); //get a list of projects
			//go through projects of the list
			for(Tree<Block> project : al) {
				blockAggregate = new TreeMap<String, Integer>(); 
				aggregateProject(project, blockAggregate);
				userMapProject.put(projectID, blockAggregate);
				userBlocksPerProject.put(me.getKey(), userMapProject); //put treeMap of blockNames with their counts into userBLock
				projectID++;
			}
		}
	}
	/**
	 * Aggregate block counts while performing a pre-order traversal of the project
	 * @param project Tree containing blocks of the project
	 * @param blockAggregate TreeMap that represents the total aggregate blocks of the user
	 */
	private void aggregateProject(Tree<Block> project, TreeMap<String, Integer> blockAggregate) {
		Block head = project.getHead();
		//if project has blocks
		if (head!=null && head.getBlockName()!=null)
			updateAggregate(blockAggregate, head.getBlockName()); 
		Collection<Block> blocks = project.getSuccessors(head);
		//pre-order traversal
		for (Block block : blocks) 
			aggregateProject(project.getTree(block), blockAggregate);
		
	}
	
	/**
	 * Insert or update the per-user, per-block aggregate
	 * @param blockAggregate TreeMap that represents the total aggregate blocks of the user
	 * @param blockName
	 */
	private void updateAggregate(TreeMap<String, Integer> blockAggregate, String blockName) {
		int aggregate = 1;
		if (blockAggregate.containsKey(blockName)) 
			//if the aggregate TreeMap already has that blockName, increase count by 1
			aggregate = blockAggregate.get(blockName).intValue() + 1;
		blockAggregate.put(blockName, aggregate); //put back blockName with its count
	}
	
	/**
	 * Output the aggregates for all users to 3 CSV files
	 * dispatch_numeric.csv format: userID, blockID, count
	 * dispatch_lookup.csv format: blockID, blockName
	 * dispatch_text.csv format: userID, blockName, count
	 * (Tim K.) Outputs block count for each user by project to a CSV file:
	 * dispatch_perProject.csv format: userID, projectID, blockName, count
	 */
	private void writeAggregates() throws IOException {
		Writer writer = null;
		Writer writerNum = null;
		Writer writerLkup = null;
		Writer writerPerProject = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(Paths.get(outputDir.toString(), OUTPUT_FILE).toString()), "utf-8"));
			writerNum = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(Paths.get(outputDir.toString(), OUTPUT_NUMERIC).toString()), "utf-8"));
			writerLkup = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(Paths.get(outputDir.toString(), OUTPUT_LKUP).toString()), "utf-8"));
			writerPerProject = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(Paths.get(outputDir.toString(), OUTPUT_PERPROJECT).toString()), "utf-8"));
			int userID = 0;
			String blockName = "";
			int blockCount = 0;
			Set<Entry<Integer, TreeMap<String, Integer>>> set = userBlocks.entrySet();
			Iterator<Map.Entry<Integer, TreeMap<String, Integer>>> iterator = set.iterator();
			//go through userBlocks TreeMaps
			while (iterator.hasNext())
			{
				//iterate through userBlock/users
				Map.Entry<Integer, TreeMap<String, Integer>> me = (Map.Entry<Integer, TreeMap<String, Integer>>)iterator.next();
				userID = me.getKey(); //get user ID (key)
				TreeMap<String, Integer> tm = me.getValue(); //get TreeMap of BlockNames and its counts
				Set<Entry<String, Integer>> setBlocks = tm.entrySet();
				Iterator<Map.Entry<String, Integer>> iBlocks = setBlocks.iterator();
				while (iBlocks.hasNext()) //iterate through the treeMap, through each blockName
				{
					Map.Entry<String, Integer> meBlocks = (Map.Entry<String, Integer>)iBlocks.next();
					//get names and number of blocks
					blockName = meBlocks.getKey(); 
					blockCount = meBlocks.getValue();
					writer.write(userID + "," + blockName + "," + blockCount + "\n"); //print blocks with name and count
					writerNum.write(userID + "," + getNum(blockName) + "," + blockCount + "\n");
				}
			}
			Set<Entry<String, Integer>> setLkup = numericBlocks.entrySet();
			Iterator<Map.Entry<String, Integer>> iNum = setLkup.iterator();
			while (iNum.hasNext())
			{
				Map.Entry<String, Integer> meNum = (Map.Entry<String, Integer>)iNum.next();
				writerLkup.write(meNum.getValue() + "," + meNum.getKey() + "\n");
			}
			//(TIM K.) writer function for dispatch_perProject
			int userID_PP = 0;
			int projectID_PP = 0;
			String blockNamePP = "";
			int blockCountPP = 0;
			Set<Entry<Integer, TreeMap<Integer, TreeMap<String, Integer>>>> setPerProject = userBlocksPerProject.entrySet();
			Iterator<Map.Entry<Integer, TreeMap<Integer, TreeMap<String, Integer>>>> iterPerUser = setPerProject.iterator();
			//iterate through each user
			while (iterPerUser.hasNext())
			{
				Map.Entry<Integer, TreeMap<Integer, TreeMap<String, Integer>>> mePerUser = (Map.Entry<Integer, TreeMap<Integer, TreeMap<String, Integer>>>)iterPerUser.next();
				userID_PP = mePerUser.getKey(); //get user ID (key)
				TreeMap<Integer, TreeMap<String, Integer>> tm = mePerUser.getValue(); //get TreeMap of BlockNames and its counts
				Set<Entry<Integer, TreeMap<String, Integer>>> setPerUser = tm.entrySet();
				Iterator<Map.Entry<Integer, TreeMap<String, Integer>>> iterPerProject = setPerUser.iterator();
				while (iterPerProject.hasNext()) {
					Map.Entry<Integer, TreeMap<String, Integer>> mePerProject  = (Map.Entry<Integer, TreeMap<String, Integer>>)iterPerProject.next();
					projectID_PP = mePerProject.getKey(); //get project ID
					TreeMap<String, Integer> tmPerProject = mePerProject.getValue(); //get TreeMap of BlockNames and its counts
					Set<Entry<String, Integer>> setBlocks = tmPerProject.entrySet();
					Iterator<Map.Entry<String, Integer>> iterBlocks = setBlocks.iterator();
					while (iterBlocks.hasNext()) {
						Map.Entry<String, Integer> meBlocks = (Map.Entry<String, Integer>)iterBlocks.next();
						//get names and number of blocks
						blockName = meBlocks.getKey(); 
						blockCount = meBlocks.getValue();
						writerPerProject.write(userID_PP + "," + projectID_PP + "," + blockName + "," + blockCount + "\n"); //print blocks with project ID, user ID, 
					}
					//iterate through the treeMap, 
				}
			}
			
		} catch (IOException ex) {
			
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		   try {writerNum.close();} catch (Exception ex) {}
		   try {writerLkup.close();} catch (Exception ex) {}
		   try {writerPerProject.close();} catch (Exception ex) {}
		}
	}
	
	/**
	 * Primary entry. Load the per-user projects into memory from the extracted .se files.
	 * Aggregate the per-user blocks. Write the aggregates to 3 CSVs.
	 */
	public void dispatch() throws IOException {
		IOUtils.LoadSEDirectory(seDir, userProjects); //sePath into userProjects
		aggregateUserBlocks();
		aggregateUserBlocksPerProject(); //overload for added functionality
		writeAggregates();
	}
	
	/**
	 * Program entry.
	 * Usage: java -classpath <...> utils.ScratchDispatcher <Path to extracted .se files> <Output Path>
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			usage("Wrong number of arguments ("+args.length+")");
		    return;
		}
		Path seDir = Paths.get(args[0]);
		if (!Files.exists(seDir)) {
		    usage("Cannot find Path to Scratch se files ("+seDir+")");
		    return;
		}
		Path outputDir = Paths.get(args[1] + "-tmp");
		Path finalDir = Paths.get(args[1]);
	    Files.createDirectories(outputDir);
	    System.out.println("Deleting all files in " + finalDir);
	    IOUtils.rm(finalDir); //deletes everything underneath it
	    ScratchDispatcher dispatcher = new ScratchDispatcher(seDir, outputDir);
	    dispatcher.dispatch();
	    Files.move(outputDir, finalDir, StandardCopyOption.ATOMIC_MOVE); //move files from outputDir to finalDir
	}
	
	/**
	 * Print usage instructions
	 */
	private static void usage(String msg) {
	    System.err.println("Usage: "+msg+" :: java -classpath <...> utils.ScratchDispatcher <Path to extracted .se files> <Output Path>");
	  }
}