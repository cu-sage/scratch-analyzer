# -*- coding: utf-8 -*-
"""
Created on Mon Nov 26 20:09:42 2018

@author: Alina.Ying
"""
#import numpy as np
from numpy import random
import os
import shutil
#from random import randint
#import time



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
    #print(l)
    while i < len(l):
        interval = abs(int(random.normal(1,1)))
        t = 0
        while t < interval:
            ouf = open(url+str(count)+'_new.se', 'w')
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

#INTERRUPTIONS
#def extremeMover(url, l):
#     if not os.path.isdir(url):
#         os.makedirs(url)
#     w=l.copy();
#     count=0
#     while (count<1):
#         index1 = randint(0, len(w)-1)
# #           index2 = randint(0, len(l)-1)
#         if ("Object" not in w[index1]):
#                 w[index1]="stuck #"+str(count)+" "+w[index1]
#         count+=1
#     ouf = open(url+str(count)+'_new.se', 'w')
#     ouf.writelines(w)
#     ouf.close()
#
#
#def stopper (url,l):
#     if not os.path.isdir(url):
#         os.makedirs(url)
#     w=l.copy()
#     count=0
#     while (count<4):
#         index1 = randint(0, len(w)-1)
# #       index2 = randint(0, len(temp)-1)
#         if ("Object" not in w[index1]):
#                 w[index1]="stuck #"+str(count)+" "+w[index1]
#         count+=1
#     ouf = open(url+str(count)+'_new.se', 'w')
#     ouf.writelines(w)
#     ouf.close()
#
#def tinkerer (url, l):
#     if not os.path.isdir(url):
#         os.makedirs(url)
#     w=l.copy()
#     count=0
#     while (count<3):
#         index1 = randint(0, len(l)-1)
# #       index2 = randint(0, len(temp)-1)
#         if ("Object" not in w[index1]):
#                 w[index1]="stuck #"+str(count)+" "+w[index1]
#         count+=1
#     ouf = open(url+str(count)+'_new.se', 'w')
#     ouf.writelines(w)
#     ouf.close()
#
#
#def mover (url, l):
#     if not os.path.isdir(url):
#         os.makedirs(url)
#     w=l.copy()
#     count=0
#     while (count<2):
#         index1 = randint(0, len(w)-1)
# #       index2 = randint(0, len(temp)-1)
#         if ("Object" not in w[index1]):
#                 w[index1]="stuck #"+str(count)+" "+w[index1]
# #               l[index1]="#"+str(count)+" "+l[index2]
# #               l[index2]=pivot
#
#         count+=1
#     ouf = open(url+str(count)+'_new.se', 'w')
#     ouf.writelines(w)
#     ouf.close()

def mockPerProject(n, se):
    url='mock/'
    if os.path.exists(url):
        shutil.rmtree(url)
    i=0
    for l in se:
        i+=1
        print("num of SE file"+str(i))
#       l = random.choice(se)
        stopper(url + str(i)+'/'+'stopper/', l)
        extremeMover(url +str(i)+'/'+ 'extremeMover/', l)
        tinkerer(url + str(i)+'/'+'tinkerer/', l)
        mover(url +str(i)+'/'+ 'mover/', l)

if __name__ == '__main__':
    url='completeSE'
    #url='SEdata'
    #url='SEdatasimple'
    se = []
    for file in os.listdir(url):
        if not file.endswith(".se"):
            continue
        inf = open(os.path.join(url, file), 'r')
        se.append(inf.readlines())
        inf.close()
    print(se[0])
    # mockPerProject(1, se)
    # print('Completed!')
# =============================================================================
#     print("original file\n")
#     print(se)
#     print("end of original file\n")
# =============================================================================
#    mockPerProject(25, se)
