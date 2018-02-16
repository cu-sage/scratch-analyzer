FILE AND DIRECTORY DESCRIPTIONS

The following are details/descriptions concerning directories and files in pect_analysis:
outputCSV: directory containing CSV results of analyses.
outputPlots: directory containing graphical results of analyses.
wild_analysis: directory containing code and results for wild Scratch analyses.
blockListSAGE.csv: block names that appear in the newer version of Scratch. If new names appear, ev_keys.csv should be edited as necessary
blockListWild.csv: block names that the current version of pect_analysis takes account of
ev_keys.csv: Look-up table for evidence variable mapping
analysis.R: source code/main file to execute analysis of SAGE data
tidyverse_1.2.1.tar.gz: tar file to install R's tidy verse package

INSTALLATION INSTRUCTIONS:
For R, RStudio, and tidy verse, installation, refer to the final report regarding SAGE Analytics. 

INSTRUCTIONS FOR EXECUTING ANALYSIS:
Below are instructions of how to run analyses on both SAGE data and Wild Data and details concerning the R scripts.

For SAGE data:
All related files are primarily in the directory pect_analysis. 
Input CSV comes from scratch-analyzer/example_input_output/Output/RegularOutput/dispatched/dispatch_perProject.csv. 
If needed, input source can be changed in line 95 of analysis.R 
To run analyses:
1. In command line, go to the pect_analysis directory.
2. Type in: RScript analysis.R
3. Wait until script finishes, outputCSV and outputPlots directory should be created and contain graphical (bar graph for each project and box plot of entire class) and CSV output.

For Wild data: 
All related files are primarily in the directory pect_analysis. 
Input CSV comes from the CSV file project_input.csv. 
If needed, input source can be changed in line 108 of computeWild.R.
To run analyses:
1. In command line, go to the wild_analysis directory.
2. Type in: RScript computeWild.R
3. Wait until script finishes, outputCSV and outputPlots directory should be created and contain graphical and CSV output. 

TEST DESCRIPTIONS: 
Below are details concerning the test input, which is under user6 for SAGE data.  
1. Basic, all scores = 1
2. Developing, all scores = 2
3. Proficient, all scores = 3
4. ParallelOne, parallelization score = 1 (only one type of initialization)
5. ParallelMany, parallelization score = 1 (diverse type of initialization)
6. InLocation, initialize location score = 1
7. InLooks, initialize looks score = 1
8. None, all scores = 0
9. All, all scores = maximum
