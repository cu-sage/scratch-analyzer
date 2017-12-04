package utils;



public class Data {
		String firstTimeStamp;
		String secondTimeStamp;
		String blockName;
		String blockID;
		boolean change;
		
		public Data(String first,String second, String name, boolean change,String blockID) {
			firstTimeStamp = first;
			secondTimeStamp = second;
			blockName = name;
			this.change = change;
			this.blockID = blockID;
		}
		
		
		public String getFirstTimeStamp() {
			return firstTimeStamp;
		}


		public String getSecondTimeStamp() {
			return secondTimeStamp;
		}
		
		public String getBlockID() {
			return blockID;
		}


		public String getBlockName() {
			return blockName;
		}


		public int change() {
			return change==true?1:0;
		}
		
		
}
	