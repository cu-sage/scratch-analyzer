package utils;

/**
 * @author JBender
 * A simple class to represent either a Scratch object or block
 * Generally, Scratch projects contain many more blocks than objects
 */
public class Block {
	private String objName;
	private String blockName;
	private String id;
	private boolean printId = false;
	private static final String SE_OBJ_OPEN = "<<";
	private static final String SE_OBJ_CLOSE = ">>";
	
	public Block(String blockName)
	{
		this.blockName = blockName;
	}
	
	public Block(String objectName, String blockName)
	{
		this.objName = objectName;
		this.blockName = blockName;
	}
	
	public Block(String objectName, String blockName, String id) {
		this.objName = objectName;
		this.blockName = blockName;
		this.id = id;
	}
	
	public Block(String objectName, String blockName, String id, boolean printId) {
		this.objName = objectName;
		this.blockName = blockName;
		this.id = id;
		this.printId = printId;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id != null ? id: null;
	}
	
	public void setObjectName(String objName) {
		this.objName = objName;
	}
	
	public String getObjectName()
	{
		return this.objName != null ? this.objName.replace("Object ", "").trim(): null;
	}
	
	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}
	
	public String getBlockName()
	{
		return this.blockName;
	}
	
	public String toString()
	{
		String value = "";
		if(objName != null)
			value += SE_OBJ_OPEN + "Object " + objName + SE_OBJ_CLOSE;
		else {
			if (printId) value += id + ", ";
			value += blockName;	
		}
		return value;
	}
}
