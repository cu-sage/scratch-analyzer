
All the csv files are generated from one project: completeSE/Face Morphing.se.

1. We use read_se.py to generate some fake projects, these projects are classified into two types: success, failure. They are located in mockSE.

* Success: We shuffle the order of some random blocks to generate fake successful se data from completeSE.
* Failure: We delete some blocks to generate fake failure se data from completeSE.

2. From mockSE files, we use mockdataProducer.py to imitate the operations of four kinds of students: extremeMover(1), mover(2), stopper(3), tinker(4).
Each type students have 25 mock data.
3. Each csv file represent the timestamps of a student completing one project. 

* Columns: representing 138 kinds of operations. 
* Rows: representing timestamps


### original:

This folder contains the original operation of students.


### differential:
This folder contains the differential between two snapshots of operation of students.
There are 138 columns representing 138 kinds of operations. 
1: this operation is added in this snapshot, -1: this operation is deleted in this snapshot
