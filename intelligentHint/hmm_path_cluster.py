
# coding: utf-8
import csv
from sklearn.metrics.pairwise import pairwise_distances
import numpy as np
import kmedoids
import csv
import re
import numpy as np
from hmmlearn import hmm

filename1 = '/Users/dingyi/Documents/SAGE/sage-frontend/machine_learning/ml-output/gsp-features-0.csv'
content = {}
students = [[],[],[]]
operations = []
with open(filename1) as f:
    reader = csv.reader(f)
    for row in reader:
        if(row[28] == 'student'):
            continue
        row[0:27] = map(eval, row[0:27])
        students[int(row[27])].append(row[0:27])
        operations.append(row[0:27])


# 3 points in dataset
data = np.array(operations)

# distance matrix
D = pairwise_distances(data, metric='euclidean')

# split into 2 clusters
M, C = kmedoids.kMedoids(D, 4)

print('medoids:')
for point_idx in M:
    print( data[point_idx] )

# print('')
# print('clustering result:')
# for label in C:
#     for point_idx in C[label]:
#         print('label {0}:　{1}'.format(label, data[point_idx]))


filename = '/Users/dingyi/Documents/SAGE/sage-frontend/machine_learning/RapidMiner-output/clustered-set-cosine-4clusters.csv'
content = {}
observations = [[],[],[],[]]
with open(filename) as f:
    reader = csv.reader(f)
    for row in reader:
        if(row[142] == 'cluster'):
            continue
        row[0:140] = map(eval, row[0:140])
        content[row[140]] = row[0:140]
        observations[int(row[140].split('/')[0])].append(row[0:140])

states = []

milestone = '/Users/dingyi/Documents/SAGE/sage-frontend/machine_learning/ml-output/centroid-output.txt'
with open(milestone) as f:
    label = f.read()
    file_list = set(label.split(','))

for f in file_list:
    try:
        f = f.split('.')[0]
        states.append(content[f])
        del content[f]
    except:
        print ('no such file!')


# GaussianHMM: observation 140 dimensions，hidden states 4 
# Then our means parameter is a 4×140 matrix, “covars” parameter is a 4×140×140 tensor

startprob = np.array([0.25, 0.25, 0.25, 0.25])

transmat = np.array([[0.7, 0.2, 0.0, 0.1],
                     [0.3, 0.5, 0.2, 0.0],
                     [0.0, 0.3, 0.5, 0.2],
                     [0.2, 0.0, 0.2, 0.6]])

# The means of each component
means = np.array([states[0],
                  states[1],
                  states[2],
                  states[3]]).astype(np.float)

# The covariance of each component
covars = .5 * np.tile(np.identity(140), (4, 1, 1))

# Build an HMM instance and set parameters
model = hmm.GaussianHMM(n_components=4, covariance_type="diag", means_prior = means, n_iter=50)
print(model)


test = observations[0:2]
X = np.concatenate(test)
print(X.shape)
length = []
for item in test:
    length.append(len(item))
print (length)  
model.fit(X, length)



sample = observations[2]
Y = np.array(sample)
hidden_states = model.predict(Y)
print (hidden_states)

