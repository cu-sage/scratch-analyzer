# Gameful Intelligent Tutoring For SAGE.

Last updated by Fall 2018 team, please continue to update as codebase evolves.

## How to Run hinting API on Localhost

1. Navigate to the `server_interface` directory in command line.
2. Run command `python application.py`
3. Can now test with sample input using Postman or other API tester

## Hinting API Request Format

The method(s) below are the fully functional / active requests. See Intelligent Hinting final report for Fall 2018 for a more detailed overview on the progress of both functional and nonfunctional API requests. Any new changes to request format should be noted here.

### /get_hints (GET)

Generates hints for a certain puzzle, current snapshot, and student type based on models built on mock / real game data.

Request format (with sample values):

{
  "seFiles": [
      {
          "content": "<<Object Stage>>\n",
          "timestamp": 1542350500031
      },
      {
          "content": "<<Object Stage>>\n                <<Object Sprite1>>\n",
          "timestamp": 1542350509935
      },
      {
          "content": "<<Object Stage>>\n                <<Object Sprite1>>\n                                whenKeyPressed\n",
          "timestamp": 1542350515652
      }
  ],
  "info": {
      "studentID": "stu2",
      "gameID": "game2",
      "objectiveID": "obj2",
      "studentType": 2,
      "puzzleID": "face_morphing"
  }
}

Currently, the model used for Intelligent Hinting only cares about the most recent snapshot, and does not factor in the timestamp. However, in the future these may be taken into consideration, which is why we include the previous snapshots in the request as well.

"studentID", "gameID", and "objectiveID" are not currently used by the model (but may be necessary in the future when generating hint logs).

"studentType" is the behavior type of the student that Behavior Detection has provided to Sage Node already. "puzzleID" is the id of the puzzle we are talking about, and is used to fetch files specific to that puzzle.

## Overview of Subdirectories and Files

Any files or subdirectories that exist in this directory but are not listed below are not being currently used as functional code, or the creator / most recent modifier has not provided a description (yet).

- behaviordetection/ : copies of some files from behaviorDetection repurposed for generating mock data needed for Intelligent Hint models
- cluster_file/ : output of hmm_cluster for each puzzle_id; to be stored in database to remove dependency on local file system
- completeSE/ : sample complete puzzles in .se format; puzzle_id for each puzzle in this directory is found by converting to lowercase and replacing " " with "\_" e.g. "Face Morphing.SE" --> "face_morphing"
- csv_file/ : contains .csv representations of mock data generated from .se files; to be stored in database to remove dependency on local file system
- dat_file/ : contains graphs for each puzzle--used for poisson path; to be stored in database to remove dependency on local file system
- server_interface/ : contains Intelligent Hint API code and algorithms used to generate responses
- sagenode_interface.py : contains functionality for communication with SAGE database in mlab

## server_interface Files

Please see Intelligent Hint report from Spring 2018 for high level purpose of algorithms, and refer to Intelligent Hint report from Fall 2018 for description of vision / future work. Below will discuss what each file does currently at a low level.

- application.py : run to initialize Hinting API on localhost
- data_flow.py : includes "save" and "load" functions for different data that the models in Intelligent Hint require; was created as part of the effort to decouple the algorithmic functionality from the data storage; allows us to change functionality or storage without changes in one affecting the other
- hmm_cluster.py : generates hmm cluster files currently stored in cluster_file/
- kmedoids.py : does K-medoids clustering--functionality used by hmm_cluster.py
- operations.csv : all currently considered SCRATCH operations
- poisson_path.py : uses output of hmm_cluster.py and poisson_path.build_graph() to generate hints
- read_se.py : contains functions for .se and .csv reading and conversions

## Todo

- have mock data generated for each puzzle, store in local with face_morphing example (see csv_file/face_morphing), or do alongside shift from local to database
- replace data_flow functions that point to local file structure with pulling / pushing to remote database
- fix one last dependency on local file structure in line 132 of poisson_path.py, can be dealt with alongside shift from local to database

## References
> Bauckhage C. Numpy/scipy Recipes for Data Science: k-Medoids Clustering[R]. Technical Report, University of Bonn, 2015.
