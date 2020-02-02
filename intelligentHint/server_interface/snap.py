import numpy as np
import pandas as pd

from os import listdir
from os.path import isfile, join

class Student:
    def __init__(self, group, order, success, csv_file):
        self.group = group
        self.order = order
        self.success = success
        
        self.csv_file = csv_file
        self.snapshot = pd.read_csv(csv_file).values[:, 1:]

def batch_load(directory, group, success):
    lst = []
    csvs = [f for f in listdir(directory) if f.endswith('.csv')]
    idx = 0
    for f in csvs:
        lst.append(Student(group, idx, success, join(directory, f)))
        idx += 1
    return lst

def load(filename, group, success):
    idx = filter(str.isdigit, filename)
    return Student(group, idx, success, filename)
