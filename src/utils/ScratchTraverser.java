package utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
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
 * Scratch Traverser follows the hierarchical structure of extracted 
 * Scratch blocks and executes a user-supplied method (Operator.operate) for each block.
 */
public class ScratchTraverser {

	private Path seDir;
	private Path outputDir;
	private String outputFile;
	private Path operatorDir;
	private TreeMap<Integer, ArrayList<Tree<Block>>> userProjects;
	private StringBuilder operateOut;
	private Class<?> operator;
	
	public ScratchTraverser(Path seDir, Path outputDir, String outputFile,
			Path operatorDir) throws ClassNotFoundException, IOException
	{
		this.seDir = seDir;
		this.outputDir = outputDir;
		this.outputFile = outputFile;
		this.operatorDir = operatorDir;
	    this.userProjects = new TreeMap<Integer, ArrayList<Tree<Block>>>();
	    operateOut = new StringBuilder();
	    this.operator = IOUtils.loadClass(this.operatorDir);
	}
	
	/**
	 * Traverse the projects for each user, and finalize the per-user Operator.operate functionality
	 * using a sentinel for end-of-processing
	 */
	private void traverseUserProjects() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Set<Entry<Integer, ArrayList<Tree<Block>>>> set = userProjects.entrySet();
		Iterator<Map.Entry<Integer, ArrayList<Tree<Block>>>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, ArrayList<Tree<Block>>> me = (Map.Entry<Integer, ArrayList<Tree<Block>>>)iterator.next();
			ArrayList<Tree<Block>> al = me.getValue();
			for(Tree<Block> project : al) {
				traverseProject(project, 0);
			}
			//Operator.operate(null, 0, operateOut);
			Method opMethod = operator.getMethod("operate", Tree.class, int.class, StringBuilder.class);
			opMethod.invoke(null, null, 0, operateOut);
			writeUserProjects(me.getKey());
			operateOut.setLength(0);
		}
	}
	
	/**
	 * Pre-order traverse a project, invoking the user-supplied Operator.operate method at each node.
	 * Increment sequence numbers during processing to aid any discrete-entity needs
	 * of the Operator.operate implementation
	 */
	private void traverseProject(Tree<Block> project, int sequence) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Block head = project.getHead();
		if (head!=null) {
			//Operator.operate(project, sequence, operateOut);
			Method opMethod = operator.getMethod("operate", Tree.class, int.class, StringBuilder.class);
			opMethod.invoke(null, project, sequence, operateOut);
		}
		Collection<Block> blocks = project.getSuccessors(head);
		int objSequence = 0;
		int blockSequence = 0;
		for (Block block : blocks) {
			if(block.getObjectName() != null)
				traverseProject(project.getTree(block), ++objSequence);
			else
				traverseProject(project.getTree(block), ++blockSequence);
		}
	}
	
	/**
	 * Ouput a parameter-driven per-user output file in per-user subdirectories.
	 * The example parameters and Operate.operator implementation output for each user
	 * a traversed.cypher file containing Cypher scripts for instantiating Neo4j graph databases
	 * representative of the Scratch projects created.
	 */
	private void writeUserProjects(Integer userID) {
		Writer writer = null;
		try {
			Path userPath = Paths.get(outputDir.toString(), "/", userID.toString());
			Files.createDirectories(userPath);
			writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(Paths.get(userPath.toString(), outputFile).toString()), "utf-8"));
			writer.write(operateOut.toString());
		} catch (IOException ex) {
			
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
	}
	
	/**
	 * Primary entry. Load the per-user projects into memory from the extracted .se files.
	 * Traverse the projects for each user while invoking Operator.operate on each node.
	 */
	public void traverse() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		IOUtils.LoadSEDirectory(seDir, userProjects);
		traverseUserProjects();
	}

	/**
	 * Program entry.
	 * Usage: java -classpath <...> utils.ScratchTraverser <Path to extracted .se files> 
	 * <Output Path> <Output File> <Operator Path>
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (args.length != 4) {
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
	    Path operatorDir = Paths.get(args[3]);
	    
	    ScratchTraverser traverser = new ScratchTraverser(seDir, outputDir, args[2], operatorDir);
	    traverser.traverse();
	    Files.move(outputDir, finalDir, StandardCopyOption.ATOMIC_MOVE);
	}
	
	/**
	 * Print usage instructions
	 */
	private static void usage(String msg) {
	    System.err.println("Usage: "+msg+" :: java -classpath <...> utils.ScratchTraverser <Path to extracted .se files> <Output Path> <Output File> <Operator Path>");
	  }
}
