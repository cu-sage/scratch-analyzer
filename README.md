scratch-analyzer
================

Social Science-Government Group: Scratch Analyzer

Copyright 2014 Jeff Bender

# How to Compile Scratch Analyzer


Scratch Analyzer is perhaps easiest compiled by establishing a Java project in Eclipse.  One configuration required is the specification of the zip4j jar in the build path, as Scratch Extractor uses this component to decompress the input .sb2 files.  This path can be set by selecting Project -> Properties in Eclipse.  All should compile properly as-is; however, note that Scratch Traverser has a dependency on a user-supplied class, Operator.  An example Operator implementation is provided in the codebase for Scratch Analyzer, but any compiled Operator class with a method with the appropriate signature (public static void operate(Tree<Block>, int, StringBuilder)) should suffice.  As noted in the following section, the path to the Operator class is specified as an input paramater to Scratch Traverser.

# How to Run Scratch Analyzer

To run Scratch-Analyzer to parse 
Example command-line instructions for running each component of Scratch Analyzer are provided below.  Scratch Dispatcher and Scratch Traverser depend on the outputs from Scratch Extractor, which requires as input a directory containing per-student subdirectories of Scratch .sb2 files; these directories must be named with integer student IDs.  The running of each module will generate additional directories with paths dependent upon the output path parameters supplied at the command-line.

The expected output of Scratch Extractor is a directory structure matching that of the input with .se files containing the simplified hierarchical representation of objects and blocks in place of the .sb2 files.  Scratch Dispatcher outputs three files, dispatch_numeric.csv, dispatch_lookup.csv, and dispatch_text.csv, containing the sets of triplets of User ID, Block, and Count, with Block represented both in numeric and text formats.  And Scratch Traverser, using the included Operator class, outputs a directory structure matching that of the Scratch Extractor output, with each subdirectory containing a single traversed.cypher file capable of generating a Neo4j graph database representative of the objects and blocks in the per-student sets of Scratch projects.  Note that since the Operator class is user-supplied, the output depends upon the user implementation.

Scratch Analyzer has been developed and tested in a Window 7 64-bit OS.  Its outputs function effectively as inputs to big data infrastructures such as Hadoop, Mahout, and Neo4j operating in an Ubuntu Linux virtual environment within VMWare Player.

# Example command-line instructions:

## Scratch Extractor:

### Windows
Usage: java -classpath <Path to zip4j;...> utils.ScratchExtractor <Path to Scratch sb2 files> <Output Path>
java -classpath C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\scratch_analyzer\bin;C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\download\zip4j_1.3.2.jar utils.ScratchExtractor c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\in c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\extracted

### Mac/Linux
In the following instruction, please replace <YOUR_SCRATCH_ANALYZER_HOME> with your scratch-analyzer folder directory.
After cloning the repository, please be aware that StatisticalData.class doesn't exist. Therefore the first step is to compile it.

```bash
$ cd src/utils
$ javac StatisticalData.java
$ chmod +x StatisticalData.class
$ cd ../..
$ cp src/utils/StatisticalData.class bin/utils
```

In the next step, we will construct our test output directory
``` bash
$ cd example_input_output/Output
$ mkdir TestOutput
$ cd TestOutput
$ mkdir extracted
$ mkdir extractedCSV
$ mkdir dispatched
$ mkdir traversed
$ cd <YOUR_SCRATCH_ANALYZER_HOME>
```

Usage: 
```bash
java -classpath <Path to zip4j;...> utils.ScratchExtractor <Path to Scratch sb2 files> <Output Path>
```

Example:
```bash
$ java -cp <YOUR_SCRATCH_ANALYZER_HOME>/bin:<YOUR_SCRATCH_ANALYZER_HOME>/bin/zip4j-1.3.2.jar utils.ScratchExtractor <YOUR_SCRATCH_ANALYZER_HOME>/example_input_output/Input <YOUR_SCRATCH_ANALYZER_HOME>/Output/TestOutput/extracted
```
This is just an example and you can run extractor on other input/output files.

## Scratch Dispatcher:

### Windows
Usage: java -classpath <...> utils.ScratchDispatcher <Path to extracted .se files> <Output Path>
java -classpath C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\scratch_analyzer\bin;C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\download\zip4j_1.3.2.jar utils.ScratchDispatcher c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\extracted c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\dispatched

### Mac/Linux
Usage:
```bash
Usage: java -classpath <...> utils.ScratchDispatcher <Path to extracted .se files> <Output Path>
```

Example:
```bash
$ java -cp <YOUR_SCRATCH_ANALYZER_HOME>/bin:<YOUR_SCRATCH_ANALYZER_HOME>/bin/zip4j-1.3.2.jar utils.ScratchDispatcher <YOUR_SCRATCH_ANALYZER_HOME>/example_input_output/Output/TestOutput/extracted <YOUR_SCRATCH_ANALYZER_HOME>/example_input_output/Output/TestOutput/dispatched
```

## Scratch Traverser:

### Windows
Usage: java -classpath <...> utils.ScratchTraverser <Path to extracted .se files> <Output Path> <Output File> <Operator Path>
java -classpath C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\scratch_analyzer\bin;C:\Users\jbender\Documents\docs\misc\cs\_bda\fp\download\zip4j_1.3.2.jar utils.ScratchTraverser c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\extracted c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis\data\s\traversed traversed.cypher c:\Users\jbender\Documents\docs\misc\cs\_bda\fp\scratch\analysis

### Mac/Linux

Usage:
```bash
java -classpath <...> utils.ScratchTraverser <Path to extracted .se files> <Output Path> <Output File> <Operator Path>
```

Example:
```bash
java -cp <YOUR_SCRATCH_ANALYZER_HOME>/bin:<YOUR_SCRATCH_ANALYZER_HOME>/bin/zip4j-1.3.2.jar utils.ScratchTraverser <YOUR_SCRATCH_ANALYZER_HOME>/example_input_output/Output/TestOutput/extracted <YOUR_SCRATCH_ANALYZER_HOME>/example_input_output/Output/TestOutput/traversed traversed.cypher /home/crossluna/6901/2/scratch-analyzer
```


<!---
For the latest Scratch Analyzer information, please visit:
http://scratchanalyzer.keotek.com/
-->