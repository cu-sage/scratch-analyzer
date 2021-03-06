package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Templates;

import java.util.Set;
import java.util.TreeMap;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.core.ZipFile;

/**
 * @author JBender
 * Scratch Extractor identifies Scratch blocks serialized as JSON and strips irrelevant 
 * syntax while preserving hierarchical structure.
 */
public class ScratchExtractor {
	private Path scratchDir;
	private Path scratchJsonDir;
	private Path outputDir;
	//private static TreeMap<Integer, ArrayList<Tree<Block>>> userTimeStampedProjects = new TreeMap<>();
	private static TreeMap<Integer, TreeMap<String, ArrayList<Tree<Block>>>> userTimeStampedProjects = new TreeMap<>();//Maps user id to a map that maps project name to projects
	private static TreeMap<Integer , ArrayList<Tree<Block>>> userProjects = new TreeMap<>();
	private static final String OBJECT = "\"objName\": \"";
	private static final String CHILDREN = "\"children\": ";
	private static final String SCRIPTS = "\"scripts\": ";
	private static final char O_SCOPE = '[';
	private static final char C_SCOPE = ']';
	private static ArrayList<String> blockList;
	private static Helper helper;

	public ScratchExtractor(Path scratchDir, Path outputDir) throws IOException {
		this.scratchDir = scratchDir;
		this.outputDir = outputDir;
		this.scratchJsonDir = Paths.get(scratchDir + "-temp");
		helper = new Helper();
		blockList = helper.getBlockNames();
		Files.createDirectories(scratchJsonDir);
	}

	/*
	 * Only use this constructor for methods that do not rely on local file system:
	 * extractJsonStr and jsonToSe.
	 * yli - 11/2018
	 */
	public ScratchExtractor() throws IOException {
		helper = new Helper();
		blockList = helper.getBlockNames();
	}
	
	/*
	 * Parses JSON string to SE string.
	 * yli - 11/2018
	 */
	public String jsonToSe(String jsonStr, boolean printId) {
		int dummyUserId = 1;
		String dummyProjectName = "project1";
		String dummyTimeStamp = "now";
		userTimeStampedProjects.clear();
		extractJsonStr(jsonStr, dummyUserId, dummyProjectName, dummyTimeStamp, printId);
		Iterator<Entry<Integer, TreeMap<String, ArrayList<Tree<Block>>>>> iterator = userTimeStampedProjects.entrySet().iterator();
		Set<Entry<String, ArrayList<Tree<Block>>>> projectSet = userTimeStampedProjects.get(iterator.next().getKey()).entrySet();
		Iterator<Entry<String, ArrayList<Tree<Block>>>> projectIterator  = projectSet.iterator();
		Tree<Block> project = projectIterator.next().getValue().get(0);
		return project.printTree(1);
	}
	
	/*
	 * Parses the json files if we want to analyze timeStamped projects
	 */

	private void extractJson(Path path,int userId) throws IOException {
		String projectName = path.getFileName().toString();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path,"*.json")) {
			for(Path jsonFile : stream) {
				String timeStamp  = jsonFile.getFileName().toString();
				extractFile(jsonFile, userId, projectName,timeStamp);
			}
		}
	}

	/**
	 * Decompress all .sb2 files, identify JSON files, and initiate transformation to .se.
	 */
	private void extractDirectory(Path path) throws IOException {
		long count = 0;
		Path outputPath = Paths.get(scratchJsonDir.toString(), "/", path.getFileName().toString());
		Path filePath = null;
		String projectName;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.sb2")) {
			for (Path sb2File : stream) {
				if (count == 0)
					Files.createDirectories(outputPath);
				projectName = sb2File.getFileName().toString();
				filePath = Paths.get(outputPath.toString(), projectName);
				Files.createDirectories(filePath);
				unzip(sb2File, filePath);
				++count;
				try (DirectoryStream<Path> streamJson = Files.newDirectoryStream(filePath, "*.json")) {
					for (Path jsonFile : streamJson) {
						extractFile(jsonFile, Integer.parseInt(outputPath.getFileName().toString()), projectName);
					}
				}	
			}
		}
	}

	/**
	 * Parse JSON to build an in-memory representation of Scratch objects and blocks
	 */
	protected void extractFile(Path jsonFile, int userID, String projectName) {	
		try (BufferedReader reader = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8)) {
			Tree<Block> project = null;
			String objName = null;
			Block parentObject = null;
			Block parentBlock = null;
			Block currentBlock = null;
			Boolean isInScript = false;
			StringBuilder objScript = new StringBuilder();
			int sOpen;
			int sClose;
			String line = null;

			while ((line = reader.readLine()) != null) {
				if (line.contains(OBJECT))
				{
					objName = line.substring(line.indexOf(OBJECT) + OBJECT.length(), 
							line.lastIndexOf('"'));
					currentBlock = new Block(objName, null);
					if (project == null) 
						project = new Tree<Block>(currentBlock, projectName);
					else
						project.addLeaf(parentObject, currentBlock);
					parentBlock = currentBlock;
					objScript = new StringBuilder();
				}
				else if (line.contains(CHILDREN))
					parentObject = currentBlock;
				else if (line.contains(SCRIPTS))
				{
					isInScript = true;
					objScript.append(line.substring(line.indexOf(SCRIPTS) + SCRIPTS.length(),
							line.length()));
				}
				
				if (isInScript)
				{
					if(!line.contains(SCRIPTS)) {
						objScript.append(line);
					}

					Iterator<Integer> iterator = objScript.chars().iterator();
					sOpen = sClose = 0;
					char c[];
					// search for matching count of [] chars, indicating end of scripts
					while (iterator.hasNext())
					{
						c = Character.toChars(iterator.next());
						if (c[0] == O_SCOPE)
							++sOpen;
						else if (c[0] == C_SCOPE)
							++sClose;
					}
					if (sOpen != 0 && sOpen == sClose)
					{
						addBlocks(project, parentBlock, objScript);
						isInScript = false;
					}
				}  	    	  
			}

			if(project != null)
				addProject(userID, project);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Parse JSON file to build an in-memory representation of Scratch objects and blocks
	 * Overloaded version of ExtractFile to incorporate the timeStamp
	 */
	protected void extractFile(Path jsonFile, int userID, String projectName, String timeStamp) {

		try (BufferedReader reader = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8)) {
			// Does not print block ID.
			extractFile(reader, userID, projectName, timeStamp, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parse JSON string to build an in-memory representation of Scratch objects and blocks
	 * Overloaded version of ExtractFile to incorporate the timeStamp
	 */
	protected void extractJsonStr(String jsonStr, int userID, String projectName, String timeStamp, boolean printId) {

		try (BufferedReader reader = new BufferedReader(new StringReader(jsonStr))) {
			extractFile(reader, userID, projectName, timeStamp, printId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Read JSON from the given BufferedReader and parse JSON to build an in-memory representation
	 * of Scratch objects and blocks.
	 * Overloaded version of ExtractFile to incorporate the timeStamp.
	 * Method extracted from the original extractFile - yli 11/2018 
	 * @param printId whether print the ID when print the block.
	 */
	protected void extractFile(BufferedReader reader, int userID, String projectName, String timeStamp, boolean printId) {

		try {
			Tree<Block> project = null;
			String objName = null;
			Block parentObject = null;
			Block parentBlock = null;
			Block currentBlock = null;
			Boolean isInScript = false;
			StringBuilder objScript = new StringBuilder();
			int sOpen;
			int sClose;
			String line = null;


			while ((line = reader.readLine()) != null) {
				if (line.contains(OBJECT))
				{
					objName = line.substring(line.indexOf(OBJECT) + OBJECT.length(), 
							line.lastIndexOf('"'));
					currentBlock = new Block(objName, null);
					if (project == null) 
						project = new Tree<Block>(currentBlock, timeStamp);
					else
						project.addLeaf(parentObject, currentBlock);
					parentBlock = currentBlock;
					objScript = new StringBuilder();
				}
				else if (line.contains(CHILDREN))
					parentObject = currentBlock;
				else if (line.contains(SCRIPTS))
				{
					isInScript = true;
					objScript.append(line.substring(line.indexOf(SCRIPTS) + SCRIPTS.length(),
							line.length()));
				}

				if (isInScript)
				{
					if(!line.contains(SCRIPTS))
						objScript.append(line);
					Iterator<Integer> iterator = objScript.chars().iterator();
					sOpen = sClose = 0;
					char c[];
					// search for matching count of [] chars, indicating end of scripts
					while (iterator.hasNext())
					{
						c = Character.toChars(iterator.next());
						if (c[0] == O_SCOPE)
							++sOpen;
						else if (c[0] == C_SCOPE)
							++sClose;
					}
					if (sOpen != 0 && sOpen == sClose)
					{
						addBlocksTimeStamped(project, parentBlock, objScript, printId);
						isInScript = false;
					}
				} 

			}
			reader.close();

			if(project != null)
				addProject(userID, timeStamp, project, projectName);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parse and load into memory the blocks specified in JSON "Scripts:" sections
	 * Modified to accommodate for block ID's.
	 * @param printId whether print the ID when print the block.
	 */
	private void addBlocksTimeStamped(Tree<Block> project, Block parentBlock, StringBuilder objScript, boolean printId)
	{
		StringBuilder blockID = null;
		StringBuilder blockName = null;
		Boolean isPrimitive = false;
		int blockOpenCount = 0;
		Iterator<Integer> iterator = objScript.chars().iterator();
		int sOpen, sClose;
		sOpen = sClose = 0;
		Block currentBlock = null;

		char c[];
		char prevChar = 0;
		// search for matching count of [] chars, indicating end of scripts
		while (iterator.hasNext())
		{
			c = Character.toChars(iterator.next());
			switch (c[0]) {
			case O_SCOPE:
				++sOpen;
				// place primitives such as '=' on same level
				isPrimitive = parentBlock != null && parentBlock.getBlockName() != null && parentBlock.getBlockName().length() > 1;
				if (blockOpenCount > 0 && !isPrimitive)
					parentBlock = currentBlock;
				break;
			case C_SCOPE:
				++sClose;
				if (blockOpenCount > 0 && !isPrimitive)
				{
					if(currentBlock.getBlockName() != null) // already at Object level
						currentBlock = project.getTree(currentBlock).getParent().getHead();
					if(currentBlock.getBlockName() != null) // already at Object level
						parentBlock = project.getTree(currentBlock).getParent().getHead();
					--blockOpenCount;
				}
				else
					isPrimitive = false;
				break;
			case '\"':
				if (prevChar == O_SCOPE) {
					++blockOpenCount;
				}
				blockID = new StringBuilder();
				c = Character.toChars(iterator.next());
				while(c[0] != '\"') {
					blockID.append(c[0]);
					c = Character.toChars(iterator.next());
				}

				if(blockID.length()==36 && helper.checkIfID(blockID.toString())) {
					blockName = new StringBuilder();
					c = Character.toChars(iterator.next());
					while(c[0]!='\"')
						c = Character.toChars(iterator.next());
					c = Character.toChars(iterator.next());

					while(iterator.hasNext() && c[0]!='\"') {
						blockName.append(c[0]);
						c = Character.toChars(iterator.next());
					}
					if(blockList.contains(blockName.toString())) {
						currentBlock = new Block(null, blockName.toString(), blockID.toString(), printId);
						project.addLeaf(parentBlock, currentBlock);
						blockName = null;
						blockID = null;
					}
				}

				break;
			}

			if(sOpen != 0 & sOpen == sClose)
				break;
			prevChar = c[0];
		}

	}

	/**
	 * Parse and load into memory the blocks specified in JSON "Scripts:" sections
	 */

	private void addBlocks(Tree<Block> project, Block parentBlock, StringBuilder objScript)
	{
		StringBuilder blockName = null;
		Boolean isInBlockName = false;
		Boolean isPrimitive = false;
		int blockOpenCount = 0;
		Iterator<Integer> iterator = objScript.chars().iterator();
		int sOpen, sClose;
		sOpen = sClose = 0;
		Block currentBlock = null;

		char c[];
		char prevChar = 0;
		// search for matching count of [] chars, indicating end of scripts
		while (iterator.hasNext())
		{
			c = Character.toChars(iterator.next());
			switch (c[0]) {
			case O_SCOPE:
				++sOpen;
				// place primitives such as '=' on same level
				isPrimitive = parentBlock != null && parentBlock.getBlockName() != null && parentBlock.getBlockName().length() > 1;
				if (blockOpenCount > 0 && !isPrimitive)
					parentBlock = currentBlock;
				break;
			case C_SCOPE:
				++sClose;
				if (blockOpenCount > 0 && !isPrimitive)
				{
					if(currentBlock.getBlockName() != null) // already at Object level
						currentBlock = project.getTree(currentBlock).getParent().getHead();
					if(currentBlock.getBlockName() != null) // already at Object level
						parentBlock = project.getTree(currentBlock).getParent().getHead();
					--blockOpenCount;
				}
				else
					isPrimitive = false;
				break;
			case '\"': 
				if (prevChar == O_SCOPE)
				{
					blockName = new StringBuilder();
					isInBlockName = true;
					++blockOpenCount;
				}
				else if (isInBlockName)
				{
					isInBlockName = false;
					currentBlock = new Block(blockName.toString());
					project.addLeaf(parentBlock, currentBlock);
				}
				break;
			default:
				if(isInBlockName)
					blockName.append(c[0]);
				break;
			}

			if(sOpen != 0 & sOpen == sClose)
				break;
			prevChar = c[0];
		}

	}

	/**
	 * Add a project to the per-user ArrayList of projects in memory
	 */
	private void addProject(int userID, String timeStamp,Tree<Block> project,String projectName)
	{
		if(userTimeStampedProjects.containsKey(userID)) {
			if(userTimeStampedProjects.get(userID).containsKey(projectName))
				userTimeStampedProjects.get(userID).get(projectName).add(project);
			else {
				ArrayList<Tree<Block>> al = new ArrayList<>();
				al.add(project);
				userTimeStampedProjects.get(userID).put(projectName, al);
			}
		}
		else
		{
			ArrayList<Tree<Block>> al = new ArrayList<Tree<Block>>();
			al.add(project);
			TreeMap<String, ArrayList<Tree<Block>>> temp = new TreeMap<>();
			temp.put(projectName,al);
			userTimeStampedProjects.put(userID,temp);
		}
	}

	private void addProject(int userID, Tree<Block> project)
	{
		if(userProjects.containsKey(userID))
			userProjects.get(userID).add(project);
		else
		{
			ArrayList<Tree<Block>> al = new ArrayList<Tree<Block>>();
			al.add(project);
			userProjects.put(userID, al);
		}
	}

	/**
	 * Decompress a .sb2 file
	 */
	private void unzip(Path sb2File, Path outputPath) {

		try {
			ZipFile zipFile = new ZipFile(sb2File.toString());
			zipFile.extractAll(outputPath.toString());
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Transform the per-user ArrayList of projects into .se files written to per-project files in 
	 * per-user subdirectories in the file system
	 * @param howToPrint determines whether output to output the files
	 */
	private void writeProjects(boolean howToPrint) throws IOException {
		if(howToPrint) {
			Writer writer = null;
			Set<Entry<Integer, TreeMap<String, ArrayList<Tree<Block>>>>> set = userTimeStampedProjects.entrySet();
			Iterator<Entry<Integer, TreeMap<String, ArrayList<Tree<Block>>>>> iterator = set.iterator();
			while (iterator.hasNext())
			{
				Map.Entry<Integer, TreeMap<String, ArrayList<Tree<Block>>>> me = (Entry<Integer, TreeMap<String,ArrayList<Tree<Block>>>>)iterator.next();
				Path userPath = Paths.get(outputDir.toString(), "/", me.getKey().toString());
				Files.createDirectories(userPath);
				Set<Entry<String, ArrayList<Tree<Block>>>> projectSet = userTimeStampedProjects.get(me.getKey()).entrySet();
				Iterator<Entry<String, ArrayList<Tree<Block>>>> projectIterator  = projectSet.iterator();

				while(projectIterator.hasNext()) {
					Map.Entry<String, ArrayList<Tree<Block>>> userProject = (Entry<String, ArrayList<Tree<Block>>>)projectIterator.next();
					ArrayList<Tree<Block>> al = userProject.getValue();
					Path projectPath = Paths.get(userPath.toString(), "/",userProject.getKey().toString());
					Files.createDirectories(projectPath);
					for (Tree<Block> project : al)
					{
						try {
							writer = new BufferedWriter(new OutputStreamWriter(
									new FileOutputStream(Paths.get(projectPath.toString(), project.getName()).toString().replaceFirst(".json", ".se")), "utf-8"));
							writer.write(project.printTree(1));
						} catch (IOException ex) {

						} finally {
							try {writer.close();} catch (Exception ex) {}
						}

					}
				}

			}
		}
		else {
			Writer writer = null;
			Set<Entry<Integer, ArrayList<Tree<Block>>>> set = userProjects.entrySet();
			Iterator<Map.Entry<Integer, ArrayList<Tree<Block>>>> iterator = set.iterator();
			while (iterator.hasNext())
			{
				Map.Entry<Integer, ArrayList<Tree<Block>>> me = (Map.Entry<Integer, ArrayList<Tree<Block>>>)iterator.next();
				Path userPath = Paths.get(outputDir.toString(), "/", me.getKey().toString());
				Files.createDirectories(userPath);
				ArrayList<Tree<Block>> al = me.getValue();
				for (Tree<Block> project : al)
				{
					try {
						writer = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream(Paths.get(userPath.toString(), project.getName()).toString().replaceFirst(".sb2", ".se")), "utf-8"));
						writer.write(project.printTree(1));
					} catch (IOException ex) {

					} finally {
						try {writer.close();} catch (Exception ex) {}
					}

				}
			}
		}



	}

	/**
	 * Test method used for debugging tree-building
	 * @param howToPrint determines the format of the output
	 */

	private static void printProjects(boolean howToPrint) {
		if(howToPrint) {
			Set<Entry<Integer, TreeMap<String, ArrayList<Tree<Block>>>>> set = userTimeStampedProjects.entrySet();
			Iterator<Entry<Integer, TreeMap<String, ArrayList<Tree<Block>>>>> iterator = set.iterator();

			while(iterator.hasNext()) {

				Map.Entry<Integer, TreeMap<String, ArrayList<Tree<Block>>>> me = (Entry<Integer, TreeMap<String,ArrayList<Tree<Block>>>>)iterator.next();
				Set<Entry<String, ArrayList<Tree<Block>>>> projectSet = userTimeStampedProjects.get(me.getKey()).entrySet();
				Iterator<Entry<String, ArrayList<Tree<Block>>>> projectIterator  = projectSet.iterator();

				while(projectIterator.hasNext()) {
					Map.Entry<String, ArrayList<Tree<Block>>> entry = (Map.Entry<String, ArrayList<Tree<Block>>>)projectIterator.next();
					ArrayList<Tree<Block>> al = entry.getValue();
					for(Tree<Block> project: al) {
						System.out.println(project.printTree(1) + "\n\n");
					}

				}
			}
		}
		else {
			Set<Entry<Integer, ArrayList<Tree<Block>>>> set = userProjects.entrySet();
			Iterator<Map.Entry<Integer, ArrayList<Tree<Block>>>> iterator = set.iterator();
			while (iterator.hasNext())
			{
				Map.Entry<Integer, ArrayList<Tree<Block>>> me = (Map.Entry<Integer, ArrayList<Tree<Block>>>)iterator.next();
				ArrayList<Tree<Block>> al = me.getValue();
				for (Tree<Block> project : al)
				{
					System.out.print(project.printTree(1) + "\n\n");
				}
			}
		}


	}

	/**
	 * Primary entry. Extract each subdirectory.
	 * @param howToExtract determines whether input is in TimeStamped format
	 * or whether it is in the normal, original format
	 */
	public void extract(boolean howToExtract) throws IOException {
		if(howToExtract) {
			DirectoryStream.Filter<Path> filt = new DirectoryStream.Filter<Path>() {
				@Override
				public boolean accept(Path file) throws IOException {
					return (Files.isDirectory(file));
				}
			};

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(scratchDir, filt)) {
				for (Path path : stream) {
					int userId = Integer.parseInt(path.getFileName().toString());
					try (DirectoryStream<Path> stream1 = Files.newDirectoryStream(path, filt)) {
						for(Path project : stream1) {
							extractJson(project, userId);
						}
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			writeProjects(howToExtract);
		}
		else {
			DirectoryStream.Filter<Path> filt = new DirectoryStream.Filter<Path>() {
				@Override
				public boolean accept(Path file) throws IOException {
					return (Files.isDirectory(file));
				}
			};

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(scratchDir, filt)) {
				for (Path path : stream) {
					extractDirectory(path);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			writeProjects(howToExtract);
		}

	}


	/**
	 * Program entry.
	 * Usage: java -classpath <Path to zip4j;...> utils.ScratchExtractor <Path to Scratch sb2 files> <Output Path>
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			usage("Wrong number of arguments ("+args.length+")");
			return;
		}
		Path scratchDir = Paths.get(args[0]);
		if (!Files.exists(scratchDir)) {
			usage("Cannot find Path to Scratch sb2 files ("+scratchDir+")");
			return;
		}
		Path outputDir = Paths.get(args[1] + "-tmp");
		Path finalDir = Paths.get(args[1]);
		Path dir = Paths.get(args[1] + "CSV");
		Files.createDirectories(dir);
		Files.createDirectories(outputDir);
		System.out.println("Deleting all files in " + finalDir);
		IOUtils.rm(finalDir);
		ScratchExtractor extractor = new ScratchExtractor(scratchDir, outputDir);
		boolean howToProceed = false;//Value of this determines whether we are parsing timeStamped or a normal set of projects
		extractor.extract(howToProceed);
		Files.move(outputDir, finalDir, StandardCopyOption.ATOMIC_MOVE);
		IOUtils.rm(extractor.scratchJsonDir);
		if(howToProceed) {
			IOUtils.rm(dir);
			Files.createDirectories(dir);
			analyzeData(dir); 
		}
	}

	/*
	 * Analyzes all the projects per user and outputs the analysis in a 
	 */
	public static void analyzeData(Path dir) throws IOException {
		ReadTree file;
		Writer writer = makeFile(dir);
		for(int user : userTimeStampedProjects.keySet()) {

			for(String project : userTimeStampedProjects.get(user).keySet()) {
				
				Path output = Paths.get(dir.toString(),"/",Integer.toString(user),"/",project);
				Files.createDirectories(output);
				file = new ReadTree(output,userTimeStampedProjects.get(user).get(project));
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
	public static Writer makeFile(Path dir) {
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
			
			writer.write(user + "," + project + "," + data.changePerBlock_mean + "," + data.changePerBlock_sd+ "," + data.changePerBlock_max + ","+ data.changePerBlock_min + "," + data.changePerBlock_range + "," + data.changePerBlock_skewness + "," + data.changeInterval_mean + "," 
			+ data.changeInterval_sd + ","  + data.changeInterval_max + ","  + data.changeInterval_min + ","  + data.changeInterval_range + ","  + data.changeInterval_skewness + "," + data.blockInterval_mean + ", " + data.blockInterval_sd + "," + data.blockInterval_max + "," + data.blockInterval_min + "," + data.blockInterval_range + "," + data.blockInterval_skewness + ","+ data.changePerInterval_mean + ","
					+ data.changePerInterval_sd + "," + data.changePerInterval_max + ","+ data.changePerInterval_min + ","+ data.changePerInterval_range + ","+ data.changePerInterval_skewness +  " \n");
		}
		catch(IOException e) {
			System.out.println(e);
		}
		return writer;
	}
	
	

	/**
	 * Print usage instructions
	 */
	private static void usage(String msg) {
		System.err.println("Usage: "+msg+" :: java -classpath <...> utils.ScratchExtractor <Path to Scratch sb2 files> <Output Path>");
	}
}
