import numpy as np


def calculate_blocks(content):
    """
    calculate the indent blocks of the given file
    :param content: content of file, list
    :return: list of tuple, [(begin_idx, end_idx)]
    """
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
    return blocks


def generate_new_order(length, block_len=10):
    """
    shuffle every 10 elements
    :param length: the length of the input
    :return:
    eg: generate_new_order(15, 10): 8,6,3,7,5,1,2,9,4,0,11,12,13,14,15
    """
    j = block_len
    new_order = []
    while j < length:
        block_range = list(range(j - block_len, j))
        np.random.shuffle(block_range)
        new_order = new_order + block_range
        j = j + block_len
    new_order = new_order + list(range(j - block_len, length))
    return new_order


def write_to_file(file_name, content_idx, content):
    """
    Write the content indexed by content_idx to file_name

    :param file_name:
    :param content_idx: list of index in content
    :param content: content of the original se file
    :return:
    """
    content_idx.sort()
    new_content = []
    for idx in content_idx:
        new_content.append(content[idx])
    ouf = open(file_name, 'w')
    ouf.writelines(new_content)
    ouf.close()