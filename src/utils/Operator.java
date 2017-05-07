package utils;

import utils.Block;
import utils.Tree;

/**
 * 
 * @author JBender
 * Example user-supplied class containing the operate method called in Scratch Traverser
 */
public class Operator {

	private static int entryID = 0;
	private static final String PF = "__";
	
	private Operator() {} // no instance

	/**
	 * User-supplied method containing functionality which will execute at each node
	 * of the Scratch block hierarchy during Scratch Traverser traversal.
	 * This example implementation outputs a Neo4j Cypher script capable of instantiating
	 * a graph database representative of the projects created. 
	 */
	public static void operate(Tree<Block> project, int sequence, StringBuilder operateOut) {
		if(project == null) { // sentinel for end-of-processing
			operateOut.append(';');
			return;
		}
		Block head = project.getHead();
		String objectName = head.getObjectName();
		String blockName = head.getBlockName();
		String objID = "";
		String blockID = "";
		
		// text augmentations to avoid Neo4j parsing problems and duplicate entries
		// although we might have duplicate blocks in the project, we must represent them discretely
		// while maintaining all proper parent/child relationships
		if (blockName!= null) {
			if(blockName.equals("\\/"))
				blockName = "V";
			blockID = blockName + PF + entryID++;
			head.setBlockName(blockID);
		} else {
			objID = objectName + PF + entryID++;
			head.setObjectName(objID);
		}
		
		// use back-ticks to avoid problems with `:` in block names
		// process objects and blocks
		if (objectName != null && objectName.equals("Stage"))
			operateOut.append((operateOut.length() != 0 ? ";\n\n" : "") + "CREATE (" + objID +  
					" { object_name: '`" + objectName + "`' })"); // first object in all Scratch files
		else if (objectName != null) 
			operateOut.append(",\n(`" + objID + "` { object_name: '`" + objectName +
					"`' })"); // subsequent objects
		else 
			operateOut.append(",\n(`" + blockID + "` { block_name: '`" + blockName +
					"`' })"); // blocks

		
		// process relationships
		Tree<Block> parentTree = project.getParent();
		if(parentTree == null || (objectName != null && objectName.equals("Stage")))
			return;		
		Block parent = parentTree.getHead();
		if(parent == null)
			return;
		if (objectName != null && parent.getObjectName() != null)
			operateOut.append(",\n(`" + objID + "`)-[:CHILD_OBJ_OF { obj_sequence: " + 
					sequence + " }]->(`" + parent.getObjectName() + "`)");
		else if (blockName != null) {
			if (parent.getBlockName()!=null)
				operateOut.append(",\n(`" + blockID + "`)-[:SUBBLOCK_OF { block_sequence: " +
						sequence + " }]->(`" + parent.getBlockName() + "`)");
			else if (parent.getObjectName() != null)
				operateOut.append(",\n(`" + blockID + "`)-[:BLOCK_OF { block_sequence: " +
						sequence + " }]->(`" + parent.getObjectName() + "`)");
		}		
		return;
	}
	
}
