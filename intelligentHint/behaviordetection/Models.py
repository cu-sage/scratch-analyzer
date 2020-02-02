from pandas import read_csv
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis as LDA
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn import svm
import pickle
import os


def train_model(X, y, lda):
    """Train the models and return a list of models."""
    model_dict = dict()                                     # model dictionary
    dc_model = DecisionTreeClassifier(criterion='entropy', max_depth=5, random_state=0) # decision tree classifer
    model_dict['DecisionTreeClassifier'] = dc_model         # saved models
    knc_model = KNeighborsClassifier(n_neighbors=3)         # KNN classifier
    model_dict['KNeighborsClassifier'] = knc_model
    svm_model = svm.SVC(C=1,kernel='linear')                # SVM
    model_dict['svm'] = svm_model
    lr_model = LogisticRegression(C=30, random_state=0)     # Logistic
    model_dict['LogisticRegression'] = lr_model
    for key, model in model_dict.items():                   # iterate though all the model and train them
        try:
            model.fit(X,y)
            print("Accuracy for " + key + ": " + str(model.score(X,y) * 100) + "%")
        except:
            print("Error when training model: ", key)
    model_dict['lda'] = lda                                 # save the LDA (was already trained)
    pickle.dump(model_dict,open( "save_model.pkl", "wb" ))    # saved those models locally


def predict(file):
    """Predict student behavior type given the file path"""
    load_data = read_csv(file, delimiter=",", skiprows=0)   # load the csv file locally
    load_data = load_data.as_matrix()
    X = load_data[:, 2:]                                    # retrieve input
    dir_path = os.path.dirname(os.path.realpath(__file__))  # get the current directory path
    with open(dir_path + "/save_model.pkl", "rb" ) as f:    # load saved model pickle file
        saved_model = pickle.load(f)
    lda = saved_model['lda']                                # get LDA model
    X = lda.transform(X)                                    # transform X

    '''Change the following such that get most votes from all the models******'''
    y = saved_model['DecisionTreeClassifier'].predict(X)    # predict using the first model and output
    '''**********************************************************************'''

    y = y.tolist()
    return y


if __name__ == '__main__':
    dir_path = os.path.dirname(os.path.realpath(__file__))  # get the current directory path
    data = read_csv(dir_path + '/StatisticalAnalysis.csv',delimiter=",",skiprows = 0)
    data = data.as_matrix()
    X = data[:, 2:]
    y = data[:, 0].astype(int)
    lda = LDA(n_components=2)                               # dimension reduction via LDA
    lda = lda.fit(X, y)
    X = lda.transform(X)
    train_model(X, y, lda)                                  # train models and save
