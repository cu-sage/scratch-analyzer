# from numpy import random
import os
import shutil
import random
import numpy as np


def mover(url, l):
    if not os.path.isdir(url):
        os.makedirs(url)
    i = 1
    count = 0
    while i < len(l):
        interval = abs(int(np.random.normal(1,1)))
        t = 0
        while t < interval:
            ouf = open(url+str(count)+'.se', 'w')
            ouf.writelines(l[:i])
            ouf.close()
            t += 1
            count += 1
        i += 1


def stopper(url, l):
    if not os.path.isdir(url):
        os.makedirs(url)
    i = 1
    count = 0
    while i < len(l):
        interval = abs(int(np.random.normal(5,3)))
        t = 0
        while t < interval:
            ouf = open(url+str(count)+'.se', 'w')
            ouf.writelines(l[:i])
            ouf.close()
            t += 1
            count += 1
        i += 1


def extremeMover(url, l):
    if not os.path.isdir(url):
        os.makedirs(url)
    i = 1
    count = 0
    while i < len(l):
        interval = abs(int(np.random.normal(1,1)))
        t = 0
        while t < interval:
            ouf = open(url+str(count)+'.se', 'w')
            ouf.writelines(l[:i])
            ouf.close()
            t += 1
            count += 1
        if random.random() > 0.8:
            i -= 1
        else:
            i += 1


def tinkerer(url, l):
    if not os.path.isdir(url):
        os.makedirs(url)
    i = 1
    count = 0
    while i < len(l):
        interval = abs(int(np.random.normal(3,1)))
        t = 0
        while t < interval:
            ouf = open(url+str(count)+'.se', 'w')
            ouf.writelines(l[:i])
            ouf.close()
            t += 1
            count += 1
        if random.random() > 0.7:
            i -= 1
        else:
            i += 1


def mockPerProject(n, se):
    url = 'mockData/success/'
    if os.path.exists(url):
        shutil.rmtree(url)
            
    for i in range(n):
        #select random .se file
        index = random.randrange(len(se))
        # l = random.choice(se)
        # print("he")
        mover(url + '2/' + str(i) + '/', se[index])
        stopper(url + '3/' + str(i) + '/', se[index])
        extremeMover(url + '1/' + str(i) + '/', se[index])
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

