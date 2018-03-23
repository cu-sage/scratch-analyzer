import os
import shutil
import random
from util import *

def write_to_file(file_name, content_idx, content):
    '''

    :param file_name:
    :param content_idx:
    :param content:
    :return:
    '''
    content_idx.sort()
    new_content = []
    for idx in content_idx:
        new_content.append(content[idx])
    ouf = open(file_name, 'w')
    ouf.writelines(new_content)
    ouf.close()

def mover(file_path, content):
    if not os.path.isdir(file_path):
        os.makedirs(file_path)
    i = 0
    count = 0

    new_order = generate_new_order(len(content))
    content_idx = []
    while i < len(content):
        interval = abs(int(np.random.normal(1, 1)))
        t = 0
        while t < interval:
            write_to_file(file_path+str(count)+'.se', content_idx, content)
            t += 1
            count += 1
        content_idx.append(new_order[i])
        if random.random() < 0.2:
            # remove the last
            if len(content_idx) != 0:
                content_idx.pop()
            i -= 1
        else:
            content_idx.append(new_order[i])
            i += 1


def stopper(file_path, content):
    if not os.path.isdir(file_path):
        os.makedirs(file_path)
    i = 1
    count = 0

    new_order = generate_new_order(len(content))
    content_idx = []
    while i < len(content):
        interval = abs(int(np.random.normal(5,3)))
        t = 0
        while t < interval:
            write_to_file(file_path + str(count) + '.se', content_idx, content)
            t += 1
            count += 1
        if random.random() < 0.2:
            # remove the last
            if len(content_idx) != 0:
                content_idx.pop()
            i -= 1
        else:
            content_idx.append(new_order[i])
            i += 1

def extreme_mover(file_path, content):
    if not os.path.isdir(file_path):
        os.makedirs(file_path)
    i = 1
    count = 0

    new_order = generate_new_order(len(content))
    content_idx = []
    while i < len(content):
        interval = abs(int(np.random.normal(1,1)))
        t = 0
        while t < interval:
            write_to_file(file_path + str(count) + '.se', content_idx, content)
            t += 1
            count += 1
        if random.random() > 0.8:
            # remove the last
            if len(content_idx) != 0:
                content_idx.pop()
            i -= 1
        else:
            content_idx.append(new_order[i])
            i += 1


def tinkerer(file_path, content):
    if not os.path.isdir(file_path):
        os.makedirs(file_path)
    i = 1
    count = 0

    new_order = generate_new_order(len(content))
    content_idx = []
    while i < len(content):
        interval = abs(int(np.random.normal(3,1)))
        t = 0
        while t < interval:
            write_to_file(file_path + str(count) + '.se', content_idx, content)
            t += 1
            count += 1
        if random.random() < 0.3:
            # remove the last
            if len(content_idx) != 0:
                content_idx.pop()
            i -= 1
        else:
            content_idx.append(new_order[i])
            i += 1


def mockPerProject(n, se):
    url = '../../mockData/success/'
    if os.path.exists(url):
        shutil.rmtree(url)
            
    for i in range(n):
        #select random .se file
        index = random.randrange(len(se))

        mover(url + '2/' + str(i) + '/', se[index])
        stopper(url + '3/' + str(i) + '/', se[index])
        extreme_mover(url + '1/' + str(i) + '/', se[index])
        tinkerer(url + '4/' + str(i) + '/', se[index])


if __name__ == '__main__':
    url = 'mockSE/success'
    se = []
    for file in os.listdir(url):
        if not file.endswith(".se"):
            continue
        inf = open(os.path.join(url, file), 'r')
        #the list se contains all .se file
        se.append(inf.readlines())
        inf.close()

    for line in se:
        print(line)
    mockPerProject(25, se)
    print('Completed!')
