import hashlib
import pandas as pd
import random
import glob
import os
import sys
import numpy as np


# Hash string into 8 digits numbers
def string2numeric_hash(text):
    return int(hashlib.md5(text).hexdigest()[:8], 16)


def read_se(input_file):
    with open(input_file, encoding='utf-8') as f:
        content = f.readlines()

    output_content = []
    for line in content:
        line = line.strip('\t\n ')
        if (line == "" or line[0] == '<'):
            continue
        output_content.append(operation[line])

    return output_content


# Convert se file to csv file
def convert_se_to_csv_diff(directory, student_type, student_id):
    diff = []
    n = len(glob.glob(directory + '/*.se'))
    print(glob.glob('*.se'))
    for i in range(1, n):
        list1 = read_se(os.path.join(directory, str(i-1) + ".se"))
        list2 = read_se(os.path.join(directory, str(i) + ".se"))
        diff.append(list(set(list2) - set(list1)))
    df = pd.DataFrame({'diff': diff})
    print(df)
    try:
        os.mkdir("csv_file/diff/" + str(student_type))
    except OSError as e:
        print(e)

    df.to_csv("csv_file/" + str(student_type) + "/" + str(student_id) + "output1.csv")

def convert_se_to_csv(directory, student_type, student_id):
    diff = []
    n = len(glob.glob(directory + '/*.se'))
    print(glob.glob('*.se'))
    for i in range(n):
        list2 = read_se(os.path.join(directory, str(i) + ".se"))
        diff.append(list2)
    df = pd.DataFrame({'operation': diff})
    try:
        os.mkdir("csv_file/operation/success/" + str(student_type))
    except OSError as e:
        print(e)

    df.to_csv("csv_file/operation/success/" + str(student_type) + "/" + str(student_id) + "output1.csv")


# List all the operations exists in completeSE, and save them into operations.csv
def calculate(dir):
    all_operation = set()
    for root, dirs, files in os.walk(dir):
        for filename in files:
            print(filename)
            all_operation = all_operation.union(read_se(os.path.join(dir, filename)))
    df = pd.DataFrame({'operation': list(all_operation)})
    df.to_csv("operations.csv")
    # return all_operation


# shuffle some blocks in the compleseSE file
def generate_mock_se_success(input_file, n):
    with open("completeSE/" + input_file, encoding='utf-8') as f:
        content = f.readlines()

    blocks = []
    prev = 0
    index = 0
    for i in range(len(content)):
        if count_indent(content[i]) == 2 and content[i].lstrip()[0] == '<':
            index += 1
            if index == 1:
                prev = i
                continue
            blocks.append((prev + 1, i))
            prev = i
    # the beginning block
    begin = random.randint(1, len(blocks) - 5)
    old_index = list(range(begin, begin + 5))
    new_index = old_index[:]
    np.random.shuffle(new_index)
    new_content = content[:]
    # shuffle the blocks between begin, begin + 5
    for t in range(5):
        new_content[blocks[old_index[t]][0]:blocks[old_index[t]][1]] \
            = content[blocks[new_index[t]][0]:blocks[new_index[t]][1]]

    output_file = "mockSE/success/" + input_file.split('.')[0] + str(n) + "." + input_file.split('.')[1]
    fo = open(output_file, "w")
    fo.writelines(new_content)


# Delete some blocks in the compleseSE file
def generate_mock_se_failure(input_file, n):
    with open("completeSE/" + input_file, encoding='utf-8') as f:
        content = f.readlines()

    blocks = []
    prev = 0
    index = 0
    for i in range(len(content)):
        if count_indent(content[i]) == 2 and content[i].lstrip()[0] == '<':
            index += 1
            if index == 1:
                prev = i
                continue
            blocks.append((prev + 1, i))
            prev = i
    # the beginning block
    begin = random.randint(1, len(blocks) - 5)
    l = random.randint(1, 5)
    print(blocks)
    print(begin)

    # delete the blocks between begin, begin + l
    del content[blocks[begin][0]:blocks[begin + l][1]]

    output_file = "mockSE/failure/" + input_file.split('.')[0] + str(n) + "." + input_file.split('.')[1]
    fo = open(output_file, "w")
    fo.writelines(content)


def count_indent(line):
    count = 0
    for i in range(len(line)):
        if line[i] == '\t':
            count += 1
    return count

def read_operation(opt_file):
    df = pd.read_csv(opt_file, index_col="operation")
    return df.to_dict()['Unnamed: 0']

if __name__ == "__main__":
    # read_se("sage-frontend/machine_learning/sample_data/0/CTG-22.se")
    # convert_se_to_csv("")
    # calculate("completeSE")
    for i in range(5):
        generate_mock_se_success("Face Morphing.se", i + 1)
        generate_mock_se_failure("Face Morphing.se", i + 1)

    operation = read_operation("operations.csv")
    input_file = "../mockData/failure/1/1/100.se"
    sys.exit()
    # print(read_se(input_file, operation))

    for i in range(1, 5):
        for j in range(25):
            convert_se_to_csv("../mockData/success/" + str(i) + "/" + str(j) + "/", i, j)