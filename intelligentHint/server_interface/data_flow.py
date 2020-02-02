import hmm_cluster
import ast
from read_se import read_operation
import pickle


def get_diff_success_snaps(puzzle_id):
    snaps = []
    for i in range(1, 5):
        snaps += hmm_cluster.batch_load('../csv_file/'+str(puzzle_id)+'/differential/success/%d/' % i, i, 1)
    return snaps


def save_cluster_file(puzzle_id, cluster_info):
    with open('../cluster_file/'+str(puzzle_id)+'.txt', 'w') as f:
        f.write(str(cluster_info))

def load_cluster_file(puzzle_id):
    with open('../cluster_file/'+str(puzzle_id)+'.txt', 'r') as f:
        cluster_info = ast.literal_eval(f.read())
    return cluster_info


def save_graph_data(puzzle_id, sb_type, data):
    """sb_type == student behavior type"""
    path = '../dat_file/' + str(puzzle_id) + '/graph' + str(sb_type) + '.dat'
    with open('graph{}.dat'.format(i), 'wb') as f:
        pickle.dump(data, f)
    return g

def load_graph_data(puzzle_id, sb_type):
    """sb_type == student behavior type"""
    path = '../dat_file/' + str(puzzle_id) + '/graph' + str(sb_type) + '.dat'
    with open(path, 'rb') as f:
        g = pickle.load(f)
    return g


def save_student_data(puzzle_id, sb_type, data):
    """sb_type == student behavior type"""
    path = '../dat_file/' + str(puzzle_id) + '/student' + str(sb_type) + '.dat'
    with open('graph{}.dat'.format(i), 'wb') as f:
        pickle.dump(data, f)

def load_student_data(puzzle_id, sb_type):
    """sb_type == student behavior type"""
    path = '../dat_file/' + str(puzzle_id) + '/student' + str(sb_type) + '.dat'
    # with open(os.path.abspath(os.path.join(os.path.dirname(__file__)))+'/graph{}.dat'.format(type), 'rb') as f:
    with open(path, 'rb') as f:
        g = pickle.load(f)


def load_operations_list():
    # operation = read_operation(os.path.abspath(os.path.join(os.path.dirname(__file__)))+"/operations.csv")
    operation = read_operation('./operations.csv')

    pool = []
    for key, value in sorted(operation.items(), key=lambda kv: (kv[1],kv[0])):
        pool.append(key)
    return pool
