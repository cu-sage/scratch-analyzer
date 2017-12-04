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
	private TreeMap<String, Integer> numericBlocks;
	private static final String OUTPUT_FILE = "dispatch_text.csv";
	private static final String OUTPUT_NUMERIC = "dispatch_numeric.csv";
	private static final String OUTPUT_LKUP = "dispatch_lookup.csv";
	private static int blockID = 0;
	
	public ScratchDispatcher(Path seDir, Path outputDir)
	{
		this.seDir = seDir;
		this.outputDir = outputDir;
	    this.userProjects = new TreeMap<Integer, ArrayList<Tree<Block>>>();
	    this.userBlocks = new TreeMap<Integer, TreeMap<String, Integer>>();
	    this.numericBlocks = new TreeMap<String, Integer>();
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
		Set<Entry<Integer, ArrayList<Tree<Block>>>> set = userProjects.entrySet();
		Iterator<Map.Entry<Integer, ArrayList<Tree<Block>>>> iterator = set.iterator();
		TreeMap<String, Integer> blockAggregate;
		while (iterator.hasNext())
		{
			blockAggregate = new TreeMap<String, Integer>();
			Map.Entry<Integer, ArrayList<Tree<Block>>> me = (Map.Entry<Integer, ArrayList<Tree<Block>>>)iterator.next();
			ArrayList<Tree<Block>> al = me.getValue();
			for(Tree<Block> project : al) {
				aggregateProject(project, blockAggregate);
				userBlocks.put(me.getKey(), blockAggregate);
			}
		}
	}
	
	/**
	 * Aggregate block counts while performing a pre-order traversal of the project
	 */
	private void aggregateProject(Tree<Block> project, TreeMap<String, Integer> blockAggregate) {
		Block head = project.getHead();
		if (head!=null && head.getBlockName()!=null)
			updateAggregate(blockAggregate, head.getBlockName());
		Collection<Block> blocks = project.getSuccessors(head);
		for (Block block : blocks) 
			aggregateProject(project.getTree(block), blockAggregate);
		
	}
	
	/**
	 * Insert or update the per-user, per-block aggregate
	 */
	private void updateAggregate(TreeMap<String, Integer> blockAggregate, String blockName) {
		int aggregate = 1;
		if (blockAggregate.containsKey(blockName)) 
			aggregate = blockAggregate.get(blockName).intValue() + 1;
		blockAggregate.put(blockName, aggregate);
	}
	
	/**
	 * Output the aggregates for all users to 3 CSV files
	 * dispatch_numeric.csv format: userID, blockID, count
	 * dispatch_lookup.csv format: blockID, blockName
	 * dispatch_text.csv format: userID, blockName, count
	 */
	private void writeAggregates() throws IOException {
		Writer writer = null;
		Writer writerNum = null;
		Writer writerLkup = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(Paths.get(outputDir.toString(), OUTPUT_FILE).toString()), "utf-8"));
			writerNum = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(Paths.get(outputDir.toString(), OUTPUT_NUMERIC).toString()), "utf-8"));
			writerLkup = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(Paths.get(outputDir.toString(), OUTPUT_LKUP).toString()), "utf-8"));
			int userID = 0;
			String blockName = "";
			int blockCount = 0;
			Set<Entry<Integer, TreeMap<String, Integer>>> set = userBlocks.entrySet();
			Iterator<Map.Entry<Integer, TreeMap<String, Integer>>> iterator = set.iterator();
			while (iterator.hasNext())
			{
				Map.Entry<Integer, TreeMap<String, Integer>> me = (Map.Entry<Integer, TreeMap<String, Integer>>)iterator.next();
				userID = me.getKey();
				TreeMap<String, Integer> tm = me.getValue();
				Set<Entry<String, Integer>> setBlocks = tm.entrySet();
				Iterator<Map.Entry<String, Integer>> iBlocks = setBlocks.iterator();
				while (iBlocks.hasNext())
				{
					Map.Entry<String, Integer> meBlocks = (Map.Entry<String, Integer>)iBlocks.next();
					blockName = meBlocks.getKey();
					blockCount = meBlocks.getValue();
					writer.write(userID + "," + blockName + "," + blockCount + "\n");
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
		} catch (IOException ex) {
			
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		   try {writerNum.close();} catch (Exception ex) {}
		   try {writerLkup.close();} catch (Exception ex) {}
		}
	}
	
	/**
	 * Primary entry. Load the per-user projects into memory from the extracted .se files.
	 * Aggregate the per-user blocks. Write the aggregates to 3 CSVs.
	 */
	public void dispatch() throws IOException {
		IOUtils.LoadSEDirectory(seDir, userProjects);
		aggregateUserBlocks();
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
	    IOUtils.rm(finalDir);
	    ScratchDispatcher dispatcher = new ScratchDispatcher(seDir, outputDir);
	    dispatcher.dispatch();
	    Files.move(outputDir, finalDir, StandardCopyOption.ATOMIC_MOVE);
	}
	
	/**
	 * Print usage instructions
	 */
	private static void usage(String msg) {
	    System.err.println("Usage: "+msg+" :: java -classpath <...> utils.ScratchDispatcher <Path to extracted .se files> <Output Path>");
	  }
}