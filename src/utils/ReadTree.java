/*
 * Sambhav Anand
 * Adds functionality to Jeff Bender's ScratchExtractor and builds on ScratchDispatcher 
 * Make sure to import apache.commons.math3 jar file from the required files folder
 */
package utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


public class ReadTree {
	private Path outputDir;
	private ArrayList<Tree<Block>> userProject;//Stores a set of TimeStamps. Each tree in the list is one version of the project

	private static final String OUTPUT_FILE = "output.csv"; //Output File per Project
	private ArrayList<Data> values;//Stores the result of analysis. Data from here is used to output the csv file
	private HashMap<String, Integer> changesPerBlock;//keeps track of the changes per block. Maps the id of a block to the number of changes
	private ArrayList<Integer> intervals;//Stores time index where change happens
	private ArrayList<Integer> blockAddOrDelete;
	private ArrayList<Integer> changePerInterval;
	private static final int INTERVAL = 10;
	private int countTS = 0;
	private int countChange = 0;

	/**
	 * Constructor
	 * @param outputDir where to output the analysis data
	 * @param userProject is one complete project. Holds the timesStamped projects 
	 */
	public ReadTree(Path outputDir, ArrayList<Tree<Block>> userProject) {

		this.outputDir = outputDir;
		this.userProject = userProject;
		values = new ArrayList<>();
		changesPerBlock = new HashMap<>();
		intervals = new ArrayList<>();
		blockAddOrDelete = new ArrayList<>();
		changePerInterval = new ArrayList<>();

	}
	
	/**
	 * Compares each successive timeStamped version of the project and modifies block changes accordingly
	 * @throws IOException
	 */
	public void aggregateTimeStampedProjects() throws IOException {
		
		for(int i=0;i<userProject.size()-1;i++) {
			Tree<Block> instance1 = userProject.get(i);
			Tree<Block> instance2 = userProject.get(i+1);

			makeComparison(instance1,instance2,instance1.getName(),instance2.getName(), i+1);
				
		}
		updateCSV();
	}
	/**
	 * 
	 * @param block id of the block who's value is to be modified
	 * @param increment the ammount by which the value is to be modified
	 */
	private void updateChanges(String block,int increment) {
		if(changesPerBlock.containsKey(block))
			changesPerBlock.put(block, changesPerBlock.get(block)+increment);
		else
			changesPerBlock.put(block, 1);//Adding a new block ammounts to one change

	}
	
	/**
	 * 
	 * @param first Tree structure of the first project
	 * @param second Tree structure of the second project
	 * @param firstNo name of the first time stamped project
	 * @param secondNo name of the second time stamped project
	 * @throws IOException
	 */
	private void makeComparison(Tree<Block> first, Tree<Block> second,String firstNo, String secondNo, int index) throws IOException {
		Block firstHead = first.getHead();
		Block secondHead = second.getHead();
		boolean changedNow = false;
		if (firstHead!=null && firstHead.getBlockName()!=null) {
			if(secondHead!=null && secondHead.getBlockName()!=null) {
				if(firstHead.getId().compareTo(secondHead.getId())!=0){
					values.add(new Data(firstNo, secondNo, firstHead.getBlockName(),true,firstHead.getId()));
					updateChanges(firstHead.getId(),1);
					changedNow = true;
					countChange++;
				}
				else {
					values.add(new Data(firstNo, secondNo, firstHead.getBlockName(),false,firstHead.getId()));
					updateChanges(firstHead.getId(),0);
				}
			}
		}

		ArrayList<Block> firstProject = traverseTimeStamp(first);
		ArrayList<Block> secondProject = traverseTimeStamp(second);
		/*
		 * The values for flag determine whether the sizes of the projects are the same or not
		 * if not same - either a block has been deleted or added
		 */
		int flag = firstProject.size()>secondProject.size()?1:0;
		if(firstProject.size()==secondProject.size())
			flag = 2;
		if(flag == 1) {
			for(int i=0;i<secondProject.size();i++) {
				String firstBlock = firstProject.get(i).getId();
				String secondBlock = secondProject.get(i).getId();

				if(firstBlock.compareTo(secondBlock)==0) {
					values.add(new Data(firstNo, secondNo, firstProject.get(i).getBlockName(),false,firstBlock));
					updateChanges(firstBlock, 0);
				}

				else {
					values.add(new Data(firstNo, secondNo, firstProject.get(i).getBlockName(),true,firstBlock));
					updateChanges(firstBlock, 1);
					changedNow = true;
					countChange++;
				}
			}
			for(int i=secondProject.size();i<firstProject.size();i++) {
				String firstBlock = firstProject.get(i).getId();

				values.add(new Data(firstNo, secondNo, firstProject.get(i).getBlockName()+ " deleted",true,firstBlock));
				updateChanges(firstBlock,1);
				changedNow = true;
				countChange++;
			}
		}

		else {
			for(int i=0;i<firstProject.size();i++) {
				String firstBlock = firstProject.get(i).getId();
				String secondBlock = secondProject.get(i).getId();
				if(firstBlock.compareTo(secondBlock)==0) {

					values.add(new Data(firstNo, secondNo, firstProject.get(i).getBlockName(),false,firstBlock));
					updateChanges(firstBlock,0);
				}

				else {
					values.add(new Data(firstNo, secondNo, firstProject.get(i).getBlockName(),true,firstBlock));
					updateChanges(firstBlock,1);
					changedNow = true;
					countChange++;
				}

			}
			if(flag==0) {
				for(int i=firstProject.size();i<secondProject.size();i++) {
					String secondBlock = secondProject.get(i).getId();

					values.add(new Data(firstNo, secondNo, secondProject.get(i).getBlockName()+ " added",true,secondBlock));
					updateChanges(secondBlock,1);
					changedNow = true;
					countChange++;
				}
			}
		}
		if(changedNow)
			intervals.add(index);
		if(flag != 2)
			blockAddOrDelete.add(index);
		if (++countTS == INTERVAL) {
			changePerInterval.add(countChange);
			countChange = 0;
			countTS = 0;
		}
		//System.out.println(countTS + " " + countChange);

	}


	
	/**
	 * Writes the data to the output file 
	 * @throws IOException
	 */
	public void updateCSV() throws IOException{
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Paths.get(outputDir.toString(), OUTPUT_FILE).toString()), "utf-8"));
			writer.write("FirstTimeStamp, SecondTimeStamp, BlockName, BlockID, NoOfChanges, Change" +" \n");
			for(Data entry:values) {

				writer.write(entry.getFirstTimeStamp()+","+entry.getSecondTimeStamp()+","+entry.getBlockName()+","+entry.getBlockID()+","+changesPerBlock.get(entry.getBlockID()) +","+ entry.change+"\n");
			}
		}
		catch(IOException e) {
			System.out.println(e);
		}
		finally {
			try {writer.close();} catch (Exception ex) {}
		}

	}

	/**
	 * returns all the blocks associated with a tree
	 * @param timeStamp tree versin of a project
	 * @return arraylist of blocks that represents a project
	 */
	private ArrayList<Block> traverseTimeStamp(Tree<Block> timeStamp) {
		if(timeStamp.getHead() == null)
			return new ArrayList<Block>();
		ArrayList<Block> project = new ArrayList<Block>();
		Block head = timeStamp.getHead();
		Collection<Block> blocks = timeStamp.getSuccessors(head);
		for(Block block: blocks) {
			if(block.getObjectName()!=null) {
				return traverseTimeStamp(timeStamp.getTree(block));
			}
			else {
				project.add(block);
				project.addAll(timeStamp.getSuccessors(block));
			}
		}
		return project;
	}
	
	public StatisticalData getStatisticalData() {
		Collection<Integer> values = changesPerBlock.values();
		ArrayList<Integer> valuesAsList = new ArrayList<>();
		valuesAsList.addAll(values);
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for(int i=0;i<values.size();i++) {
			stats.addValue((double) valuesAsList.get(i));
		}

		DescriptiveStatistics intervalStat = new DescriptiveStatistics();
		for (int i = 1; i < intervals.size(); i++) {
			intervalStat.addValue((double)(intervals.get(i)-intervals.get(i-1)));
		}
		//System.out.println(intervals.toString());
		
		DescriptiveStatistics blockMoveStat = new DescriptiveStatistics();
		for (int i = 1; i < blockAddOrDelete.size(); i++) {
			blockMoveStat.addValue((double)(blockAddOrDelete.get(i)-blockAddOrDelete.get(i-1)));
		}
		
		DescriptiveStatistics changePerIntervalStat = new DescriptiveStatistics();
		for (Integer i: changePerInterval) {
			changePerIntervalStat.addValue((double)i);
		}
		//System.out.println(changePerIntervalStat.toString());
		
		return new StatisticalData(stats.getMean(),stats.getStandardDeviation(), stats.getMax(), stats.getMin(), stats.getSkewness(), valuesAsList, intervalStat.getMean(), intervalStat.getStandardDeviation(), intervalStat.getMax(), intervalStat.getMin(), intervalStat.getSkewness(),
				blockMoveStat.getMean(), blockMoveStat.getStandardDeviation(), blockMoveStat.getMax(), blockMoveStat.getMin(), blockMoveStat.getSkewness(), changePerIntervalStat.getMean(), changePerIntervalStat.getStandardDeviation(), changePerIntervalStat.getMax(), changePerIntervalStat.getMin(), changePerIntervalStat.getSkewness());
	}

}
