# add file folder to system path
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath("server_interface"))))

# global imports
from collections import defaultdict
import pickle
import difflib
import os
# local imports
from server_interface.snap import load
from server_interface.read_se import read_operation, read_se_string
import data_flow


class Graph:
    def __init__(self):
        self.nodes = set()
        self.edges = defaultdict(list)

    def add_node(self, v):
        self.nodes.add(v)

    def add_edge(self, u, v, w):
        self.edges[u].append((v, w))


def Dijkstra(graph, initial):
    # if the current snapshot is not exist in the graph
    # choose the max similarity as the initial

    delta = {initial: 0}
    nodes = set(graph.nodes)
    path = {}
    while nodes:
        min_node = None
        for node in nodes:
            if node in delta:
                if min_node is None or delta[node] < delta[min_node]:
                    min_node = node

        if min_node is None:
            break
        nodes.remove(min_node)
        d = delta[min_node]
        for v, w in graph.edges[min_node]:
            weight = d + w
            if v not in delta or weight < delta[v]:
                delta[v] = weight
                path[v] = min_node
    return path


def add_path(graph, path):
    last = None
    for snapshot in path:
        code = ','.join(str(e) for e in snapshot)
        graph.add_node(code)
        if last:
            graph.add_edge(last, code, 1)
        last = code

    if last:
        graph.add_edge(last, 'final', 1)


def add_test(graph, path):
    last = None
    for snapshot in path:
        graph.add_node(snapshot)
        if last:
            graph.add_edge(last, snapshot, 1)
        last = snapshot

    if last:
        graph.add_edge(last, 'final', 1)


def poi_reweight(graph):
    # Merge duplicate edges
    gprime = Graph()
    gprime.nodes = graph.nodes
    for node in graph.nodes:
        se = defaultdict(lambda: 0)
        for v, w in graph.edges[node]:
            se[v] += w
        # Reweight edges
        for v in se:
            se[v] = 1.0 / se[v]
            # print(se[v])
        gprime.edges[node] = list(se.items())
    return gprime


def subsample(snaps, k):
    for x in snaps:
        x.snapshot = x.snapshot[0::k]


def check_graph():
    g = Graph()
    test1 = [0,1,2,3,4,5,6]
    test2 = [2,3,6]
    add_test(g, test1)
    add_test(g, test2)
    g = poi_reweight(g)
    # print(g.edges)

    code = 3
    tree = Dijkstra(g, code)

    s = []
    u = 'final'

    while u is not code and u in tree:
        s.append(u)
        u = tree[u]

    # print(s[-1])


def build_graph(puzzle_id):
    pass
    files = data_flow.load_cluster_file(puzzle_id)
    
    i = 0
    for students in files:
        g = Graph()
        students_total = []
        for student in students:
            # NOTE: need more permanant fix later on, this should also be independent of local file system, but fix is more complicated than throwing functionality into data_flow.py
            students_total.append(load(os.path.abspath(os.path.join(os.path.join(os.path.dirname(__file__)), os.path.pardir))+'/csv_file/{}/original/success/'.format(puzzle_id) + student, i, 1))

        for current in students_total:
            add_path(g, current.snapshot.tolist())

        g = poi_reweight(g)

        data_flow.save_student_data(puzzle_id, sb_type, g)
        data_flow.save_graph_data(puzzle_id, sb_type, students_total)
        i += 1


def generate_hints(puzzle_id, code, type):
    """
    Args:
        puzzle_id (string): id of the puzzle
        code (string): snapshot of student's progress
        type (int): learning type (from `behaviordetection`)

    Returns:
        best next snapshot (string)
        distance from goal (int)
    """

    g = data_flow.load_graph_data(puzzle_id, type)
    target = code
    if code not in g.nodes:
        max_ratio = 0.0
        for node in g.nodes:
            seq = difflib.SequenceMatcher(None, node, code)
            # based on docs for SequenceMatcher, ratio() method should be
            # deterministic (and no randomness involved)
            ratio = seq.ratio()
            if ratio > max_ratio:
                max_ratio = ratio
                target = node

    tree = Dijkstra(g, target)
    # print(tree)

    s = []
    u = 'final'
    distance = 0
    while u is not code and u in tree:
        s.append(u)
        distance += 1
        u = tree[u]

    # print(s)
    return s[-1], distance


def get_hints(puzzle_id, se_string, type):
    """
    Given a snapshot and a type of learning behavior, return block(s) of interest

    Args:
        code (string): snapshot of student's progress
        type (int): learning type (from `behaviordetection`)
    Returns:
        blocks (list of strings)
    """

    # convert se string to code
    op_array = read_se_string(se_string)
    # convert op_array to code format:
    # sample ='0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,9,0,0,0,0,1,0,0,0,4,0,1,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,8,0,0,0,0,0,0,0,0,0,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0'
    code = [0 for _ in range(139)] # initialize each operation to 0
    for i in op_array:
        code[i] += 1 # for each operation encountered, add one (get cumulative, not differential)
    code = ",".join([str(i) for i in code]) # join with commas, legacy format

    pool = data_flow.load_operations_list()

    # generate hints
    hint, distance = generate_hints(puzzle_id, code, type)

    hint1 = hint.split(',')
    code1 = code.split(',')
    result = []

    # calculate the difference between current snapshot and the hints
    for idx in range(0, len(code1)):
        if hint1[idx] != code1[idx]:
            result.append((int(hint1[idx]), pool[idx]))

    # return the top 3 blocks if there are more than three dimension different.
    result.sort()
    blocks = [r[1] for r in result[-1:]]
    print("distance to the final snapshot: %d" % distance)
    print("generated hint: {0}".format(blocks))

    return blocks

if __name__ == '__main__':

    # build_graph("face_morphing")
    se_string = "<<Object Stage>>\n                <<Object Sprite1>>\n                                whenKeyPressed\n         doWaitUntil\n"
    type = 2
    puzzle_id = "face_morphing"

    get_hints(puzzle_id, se_string, type)

    # 139 columns
    # sample ='0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,9,0,0,0,0,1,0,0,0,4,0,1,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,8,0,0,0,0,0,0,0,0,0,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0'
    # blocks = get_hints(sample, 2)
    # print("")
    # print(blocks)
