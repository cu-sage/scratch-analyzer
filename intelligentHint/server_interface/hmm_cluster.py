# add file folders to system path
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath("server_interface"))))
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath("csv_file"))))

# global imports
from sklearn.metrics.pairwise import pairwise_distances
from sklearn.cluster import SpectralClustering
from server_interface import kmedoids
from hmmlearn import hmm
import numpy as np
import pandas as pd
from os import listdir
from os.path import join
import warnings

# local imports
import data_flow

# settings / global variables
warnings.filterwarnings('ignore')
k = 6       # number of milestones


class Snapshot:
    def __init__(self, group, order, success, csv_file):
        self.group = group
        self.order = order
        self.success = success
        self.csv_file = csv_file

        self.snapshot = pd.read_csv(csv_file).values[:,1:]


def get_student_type(snapshot_list):
    student_type = 1 # 0, 1, 2, 3
    # todo: get students' type according to his snapshot list in this game.
    # hmm cluster result is saved in cluster.txt which will be used in hint generation
    return student_type


def batch_load(directory, group, success):
    """
    Not generalized, only use for batch loading success .csv files
    """
    lst = []
    csvs = [f for f in listdir(directory) if f.endswith('.csv')][:6]
    print('Loading snapshots...')
    print(csvs)
    idx = 0
    idx_lb = 0
    for f in csvs:
        snap = Snapshot(group, idx, success, join(directory, f))
        lst.append(snap)
        snap.start_idx = idx_lb
        idx_lb += snap.snapshot.shape[0]
        snap.end_idx = idx_lb
        idx += 1
    return lst


def do_cluster(puzzle_id):
    """
    Creates what was original "cluster.txt" for the game id passed in
    Saved as cluster_file/<puzzle_id>.txt

    Args:
        puzzle_id: (string) game identifier, used for loading and saving data
    """

    snaps = data_flow.get_diff_success_snaps(puzzle_id)

    data = np.concatenate(np.array([x.snapshot for x in snaps]))
    print(data.shape)

    from sklearn.decomposition import PCA
    pca = PCA(n_components=20)
    principalComponents = pca.fit_transform(data)

    print('PCA dimension=', principalComponents.shape)

    sampled = principalComponents[::20, :]
    print(sampled.shape)

    D = pairwise_distances(sampled, metric='euclidean')
    print(D.shape)
    print('K-Medoids')
    M, C = kmedoids.kMedoids(D, k)
    num_routes = len(snaps)

    def log_likelihood_space_metric(model, X):
        return model.score(X)

    def symetric_score(i, j):
    	score = log_likelihood_space_metric(hmm_models[i], principalComponents[snaps[j].start_idx : snaps[j].end_idx])
    	score += log_likelihood_space_metric(hmm_models[j], principalComponents[snaps[i].start_idx : snaps[i].end_idx])
    	return score

    hmm_models = []
    means = np.array(sampled[M]).astype(np.float)

    print('HMM prior=', means)
    print(snaps)
    from tqdm import(trange)
    for i in trange(num_routes):
        model = hmm.GaussianHMM(n_components=k, covariance_type="diag", means_prior = means, n_iter=100)
        model.fit(principalComponents[snaps[i].start_idx : snaps[i].end_idx])
        hmm_models.append(model)

    dist = np.zeros((num_routes, num_routes))
    for i in range(num_routes):
        for j in range(num_routes):
            dist[i, j] = symetric_score(i, j)
            sc = SpectralClustering(n_clusters=4, affinity='precomputed')
    xmin, xmax = dist.min(), dist.max()
    dist = (dist - xmin) / (xmax - xmin)
    y_pred = sc.fit_predict(dist)

    # get cluster info for saving
    cluster_info = []
    for i in range(4):
        val = ['/'.join(str.split(snaps[x].csv_file, '/')[-2:]) for x in range(num_routes) if y_pred[x] == i]
        cluster_info.append(val)

    # save cluster info
    data_flow.save_cluster_file(puzzle_id, cluster_info)

if __name__ == "__main__":

    do_cluster("face_morphing")
