For the latest Scratch Analyzer information, please visit:
http://scratchanalyzer.keotek.com/

How to Compile Scratch Analyzer

Scratch Analyzer is perhaps easiest compiled by establishing a Java project in Eclipse.  
One configuration required is the specification of the zip4j jar in the build path, 
as Scratch Extractor uses this component to decompress the input .sb2 files.  
This path can be set by selecting Project -> Properties in Eclipse. 
---Tim K.: An alternative way to set up the build path would be editing the .classpath file---
All should compile properly as-is; however, note that Scratch Traverser has a dependency on a user-supplied class, 
Operator.  An example Operator implementation is provided in the codebase for Scratch Analyzer, 
but any compiled Operator class with a method with the appropriate signature 
(public static void operate(Tree<Block>, int, StringBuilder)) should suffice.  
As noted in the following section, the path to the Operator class is specified as 
an input paramater to Scratch Traverser.

How to Run Scratch Analyzer

To run Scratch-Analyzer to parse 
Example command-line instructions for running each component of Scratch Analyzer are provided below.  
Scratch Dispatcher and Scratch Traverser depend on the outputs from Scratch Extractor, which requires 
as input a directory containing per-student subdirectories of Scratch .sb2 files; these directories must 
be named with integer student IDs.  The running of each module will generate additional directories with 
paths dependent upon the output path parameters supplied at the command-line.

The expected output of Scratch Extractor is a directory structure matching that of the input with .se files 
containing the simplified hierarchical representation of objects and blocks in place of the .sb2 files.  
Scratch Dispatcher outputs three files, dispatch_numeric.csv, dispatch_lookup.csv, and dispatch_text.csv, 
containing the sets of triplets of User ID, Block, and Count, with Block represented both in numeric and text formats.  
And Scratch Traverser, using the included Operator class, outputs a directory structure matching that of the Scratch Extractor 
output, with each subdirectory containing a single traversed.cypher file capable of generating a Neo4j graph database representative 
of the objects and blocks in the per-student sets of Scratch projects.  Note that since the Operator class is user-supplied, 
the output depends upon the user implementation.

Scratch Analyzer has been developed and tested in a Window 7 64-bit OS.  Its outputs function effectively as inputs to big
data infrastructures such as Hadoop, Mahout, and Neo4j operating in an Ubuntu Linux virtual environment within VMWare Player.
---Tim K.: Scratch Analyzer (mainly ScratchExtractor and ScratchDispatcher) was tested on a Mac OSX (macOS Sierra) and also works
effectively in extracting and dispatching sb2 files.---

Example command-line instructions:
---Tim K.: For Mac OSX users, on the argument after -classpath, users would need to use the colon symbol ':' rather than ';'. Example
command-line instructions for Mac users are also provided below. --

---
Scratch Extractor:
Usage/Syntax: java -classpath <Path to zip4j;...> utils.ScratchExtractor <Path to Scratch sb2 files> <Output Path>
Windows: java -classpath C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\scratch_analyzer\bin;C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\download\zip4j_1.3.2.jar utils.ScratchExtractor c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\in c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\extracted
Mac: java -classpath /Users/TimGimi/sage/scratch-analyzer/bin:/Users/TimGimi/sage/scratch-analyzer/FilesRequired/zip4j-1.3.2.jar utils.ScratchExtractor /Users/TimGimi/sage/scratch-analyzer/example_input_output/Input/Regular_Input /Users/TimGimi/sage/scratch-analyzer/example_input_output/Output/RegularOutput/extracted

Scratch Dispatcher:
Usage/Syntax: java -classpath <...> utils.ScratchDispatcher <Path to extracted .se files> <Output Path>
Windows: java -classpath C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\scratch_analyzer\bin;C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\download\zip4j_1.3.2.jar utils.ScratchDispatcher c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\extracted c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\dispatched
Mac: java -classpath /Users/TimGimi/sage/scratch-analyzer/bin:/Users/TimGimi/sage/scratch-analyzer/FilesRequired/zip4j-1.3.2.jar utils.ScratchDispatcher /Users/TimGimi/sage/scratch-analyzer/example_input_output/Output/RegularOutput/extracted /Users/TimGimi/sage/scratch-analyzer/xample_input_output/Output/RegularOutput/dispatched

Scratch Traverser:
Usage: java -classpath <...> utils.ScratchTraverser <Path to extracted .se files> <Output Path> <Output File> <Operator Path>
java -classpath C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\scratch_analyzer\bin;C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\download\zip4j_1.3.2.jar utils.ScratchTraverser c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\extracted c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\traversed traversed.cypher c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis

--Tim K.: An alternative way (and, in my opinion, easier way) to run Scratch Extractor and Scratch Dispatcher is to 
use Eclipse and run both java files as a Java Application with customized arguments (make sure your classpath is updated!). 
Below is a detailed step-by-step instruction for this alternative method:
1. Go to the file in the File Explorer (e.g. ScratchExtractor.java), right click, and click Run As -> Run Configurations.
2. In the Run Configurations window, create a new launch configuration (Upper Left button) and make sure its a Java Application.
3. Name your new launch configuration, then go to the Arguments Tab.
4. Click Variables, then Edit Variables button, and finally New.
5. Name the variable, and in Value, write in the last two arguments of the command line instruction above, which is usually the input path and output path.
Example for ScratchDispatcher: /Users/TimGimi/sage/scratch-analyzer/example_input_output/Output/RegularOutput/extracted /Users/TimGimi/sage/scratch-analyzer/example_input_output/Output/RegularOutput/dispatched
First argument is input (.se files) and ouput directory (.csv files)
6. Go back to the Select Variable window, and select the variable you just created. 
7. Finally, run the application (play button/right click on java file) and your ScratchDispatcher/ScratchExtractor should output files accordingly!
--

Copyright 2014 Jeff Bender