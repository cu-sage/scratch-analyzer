from numpy import random
import os
import shutil


def mover(url, l):
    if not os.path.isdir(url):
        os.makedirs(url)
    i = 1
    count = 0
    while i < len(l):
        interval = abs(int(random.normal(1,1)))
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
        interval = abs(int(random.normal(5,3)))
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
        interval = abs(int(random.normal(1,1)))
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
        interval = abs(int(random.normal(3,1)))
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
    dir_path = os.path.dirname(os.path.realpath(__file__))
    url = dir_path + '/../../../mockData/'  # save mockdata outside of git folder so it doesn't get upload
    if os.path.exists(url):
        shutil.rmtree(url)
            
    for i in range(n):
        l = random.choice(se)
        mover(url + '2/' + str(i) + '/', l)
        stopper(url + '3/' + str(i) + '/', l)
        extremeMover(url + '1/' + str(i) + '/', l)
        tinkerer(url + '4/' + str(i) + '/', l)


if __name__ == '__main__':
    dir_path = os.path.dirname(os.path.realpath(__file__))
    url = dir_path + '/completeSE'
    se = []
    for file in os.listdir(url):
        if not file.endswith(".se"):
            continue
        inf = open(os.path.join(url, file), 'r')
        se.append(inf.readlines())
        inf.close()
    
    mockPerProject(25, se)                  # going to take a while
    print('Completed!')


