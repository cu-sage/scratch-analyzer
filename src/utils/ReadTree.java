/*
 * Adds functionality to Jeff Bender's ScratchExtractor and builds on ScratchDispatcher 
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
import java.util.Collections;
import java.util.TreeMap;

public class ReadTree {
	private Path outputDir;
	private ArrayList<Tree<Block>> userProject;
	private static final String OUTPUT_FILE = "output.csv";
	
	private ArrayList<Data> values;
	
	public ReadTree(Path outputDir, ArrayList<Tree<Block>> userProject) {

		this.outputDir = outputDir;
		this.userProject = userProject;
		values = new ArrayList<>();
	}
	
	public void aggregateTimeStampedProjects() throws IOException {
		
		for(int i=0;i<userProject.size()-1;i++) {
			Tree<Block> instance1 = userProject.get(i);
			Tree<Block> instance2 = userProject.get(i+1);
			
			makeComparison(instance1,instance2,i+1,i+2);
		}
		updateCSV();
	}
	
	private void makeComparison(Tree<Block> first, Tree<Block> second,int firstNo, int secondNo) throws IOException {
		Block firstHead = first.getHead();
		Block secondHead = second.getHead();
		if (firstHead!=null && firstHead.getBlockName()!=null) {
			if(secondHead!=null && secondHead.getBlockName()!=null) {
				if(firstHead.getBlockName().compareTo(secondHead.getBlockName())!=0)
					values.add(new Data(firstNo,secondNo,firstHead.getBlockName(),true));
				else
					values.add(new Data(firstNo,secondNo,firstHead.getBlockName(),false));
			}
		}

		ArrayList<Block> firstTemp = first.getSuccessors(firstHead);
		ArrayList<Block> secondTemp = second.getSuccessors(secondHead);
		ArrayList<Block> firstProject = new ArrayList<>();
		ArrayList<Block> secondProject = new ArrayList<>();
		
		for(int i=0;i<firstTemp.size();i++) {
			firstProject.addAll(first.getSuccessors(firstTemp.get(i)));
		}
		
		for(int i=0;i<secondTemp.size();i++) {
			secondProject.addAll(second.getSuccessors(secondTemp.get(i)));
		}
		
		//ArrayList<Block> firstProject = first.traverseTree(firstHead);
		//ArrayList<Block> secondProject = second.traverseTree(secondHead);
		
		
		int flag = firstProject.size()>secondProject.size()?1:0;
		if(firstProject.size()==secondProject.size())
			flag = 2;
		if(flag == 1) {
			for(int i=0;i<secondProject.size();i++) {
				String firstBlock = firstProject.get(i).getBlockName();
				String secondBlock = secondProject.get(i).getBlockName();
				
				if(firstBlock.compareTo(secondBlock)==0)
					values.add(new Data(firstNo, secondNo, firstBlock,false));
				else
					values.add(new Data(firstNo,secondNo,firstBlock,true));
			}
			values.add(new Data(firstNo,secondNo,"Blocks Deleted",true));
		}
		else {
			for(int i=0;i<firstProject.size();i++) {
				String firstBlock = firstProject.get(i).getBlockName();
				String secondBlock = secondProject.get(i).getBlockName();
				if(firstBlock.compareTo(secondBlock)==0) 
					values.add(new Data(firstNo, secondNo, firstBlock,false));
				else
					values.add(new Data(firstNo,secondNo,firstBlock,true));
				
			}
			if(flag==0)
				values.add(new Data(firstNo,secondNo,"Blocks Added", true));
		}
		
	}
	
	public void updateCSV() throws IOException{
		Writer writer = null;
		
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(Paths.get(outputDir.toString(), OUTPUT_FILE).toString()), "utf-8"));
			writer.write("FirstTimeStamp" + "," + "SecondTimeStamp" + "," + "BlockName" + "," + "Change" +" \n");
			for(Data entry:values) {
				writer.write("#"+entry.getFirstTimeStamp()+","+"#"+entry.getSecondTimeStamp()+","+entry.getBlockName()+","+entry.change+"\n");
			}
		}
		catch(IOException e) {
			System.out.println(e);
		}
		finally {
			try {writer.close();} catch (Exception ex) {}
		}
		
	}
	
	
}
