from sklearn.model_selection import cross_val_score
import numpy as np
from pandas import read_csv
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis as LDA

def decisionTree(X, y):
    from sklearn.tree import DecisionTreeClassifier
    scores = cross_val_score(DecisionTreeClassifier(criterion='entropy', max_depth=5, random_state=0)
            , X, y, cv=5, scoring='accuracy')
#     print(scores)
    return scores.mean()



def knn(X, y):
    from sklearn.neighbors import KNeighborsClassifier
    scores = cross_val_score(KNeighborsClassifier(n_neighbors=3), X, y, cv=5, scoring='accuracy')
#     print(scores)
    return scores.mean()



def logistic(X, y):
    from sklearn.linear_model import LogisticRegression
    scores = cross_val_score(LogisticRegression(C=30, random_state=0), X, y, cv=5, scoring='accuracy')
#     print(scores)
    return scores.mean()



def svm(X, y):
    from sklearn import svm
    scores = cross_val_score(svm.SVC(C=1,kernel='linear'), X, y, cv=5, scoring='accuracy')
#     print(scores)
    return scores.mean()


if __name__ == '__main__':
	# load data
	data = read_csv('/Users/Bian/Desktop/mockData/StatiscalAnalysis.csv',delimiter=",",skiprows = 0)
	data = data.as_matrix()
	X = data[:, 2:]
	y = data[:, 0].astype(int)
	# dimension reduction via LDA
	lda = LDA(n_components=2)
	X = lda.fit_transform(X, y)
	# compute model accuracy
	print('Decision tree accuracy: {0}'.format(decisionTree(X, y)))
	print('KNN accuracy: {0}'.format(knn(X, y)))
	print('Logistic regression accuracy: {0}'.format(logistic(X, y)))
	print('SVM accuracy: {0}'.format(svm(X, y)))



