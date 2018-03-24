import pandas as pd
import random
import glob
import os
from util import *


def read_se(input_file):
    with open(input_file, encoding='utf-8') as f:
        content = f.readlines()

    output_content = []
    for line in content:
        line = line.strip('\t\n ')
        if (line == "" or line[0] == '<'):
            continue
        try:
            output_content.append(operation[line])
        except KeyError as e:
            print(e)

    return output_content


def convert_se_to_csv_diff(file_path, student_type, student_id):
    '''
    Convert se file to csv file. 1 represent append operation, -1 represent delete operation
    :param file_path:
    :param student_type:
    :param student_id:
    :return:
    '''
    diff = []
    n = len(glob.glob(file_path + '/*.se'))
    print(glob.glob('*.se'))
    one_hot_data = np.zeros((n - 1, len(operation)))
    for i in range(n - 1):
        list1 = read_se(os.path.join(file_path, str(i) + ".se"))
        list2 = read_se(os.path.join(file_path, str(i + 1) + ".se"))
        operations_append = list(set(list2) - set(list1))
        operations_delete = list(set(list1) - set(list2))
        for opt in operations_append:
            one_hot_data[i][opt] = 1
        for opt in operations_delete:
            one_hot_data[i][opt] = -1

    one_hot_data = one_hot_data.astype(int)
    df = pd.DataFrame(one_hot_data)
    try:
        os.mkdir("csv_file/differential/success/" + str(student_type))
    except OSError as e:
        print(e)

    df.to_csv("csv_file/differential/success/" + str(student_type) + "/output" + str(student_id) + ".csv")


def convert_se_to_csv(file_path, student_type, student_id):
    diff = []
    n = len(glob.glob(file_path + '/*.se'))
    one_hot_data = np.zeros((n, len(operation)))
    for i in range(n):
        list2 = read_se(os.path.join(file_path, str(i) + ".se"))
        for opt in list2:
            one_hot_data[i][opt] += 1

    one_hot_data = one_hot_data.astype(int)
    df = pd.DataFrame(one_hot_data)

    try:
        os.mkdir("csv_file/original/success/" + str(student_type))
    except OSError as e:
        print(e)

    df.to_csv("csv_file/original/success/" + str(student_type) + "/output" + str(student_id) + ".csv")


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

    blocks = calculate_blocks(content)
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

    blocks = calculate_blocks(content)
    # the beginning block
    begin = random.randint(1, len(blocks) - 5)
    l = random.randint(1, 5)

    # delete the blocks between begin, begin + l
    del content[blocks[begin][0]:blocks[begin + l][1]]

    output_file = "mockSE/failure/" + input_file.split('.')[0] + str(n) + "." + input_file.split('.')[1]
    fo = open(output_file, "w")
    fo.writelines(content)


def count_indent(line):
    '''
    Count indentation in front of the line
    :param line:
    :return:
    '''
    count = 0
    for i in range(len(line)):
        if line[i] == '\t':
            count += 1
    return count


def read_operation(opt_file):
    '''
    Read all the operation dict from file
    :param opt_file: operation file
    :return: dict
    '''
    df = pd.read_csv(opt_file, index_col="operation")
    return df.to_dict()['Unnamed: 0']


if __name__ == "__main__":
    # read_se("sage-frontend/machine_learning/sample_data/0/CTG-22.se")
    # convert_se_to_csv("")
    # calculate("completeSE")
    # for i in range(5):
    #     generate_mock_se_success("Face Morphing.se", i + 1)
    #     generate_mock_se_failure("Face Morphing.se", i + 1)

    operation = read_operation("operations.csv")
    # input_file = "../mockData/failure/1/1/100.se"
    # print(read_se(input_file, operation))

    for i in range(1, 5):
        for j in range(25):
            convert_se_to_csv("../../mockData/success/" + str(i) + "/" + str(j) + "/", i, j)