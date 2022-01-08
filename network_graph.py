from numpy import *
import matplotlib.pyplot as plt
import numpy.polynomial.polynomial as poly
from scipy.interpolate import CubicSpline as CS

files = ["graphs/CPUTimeSimple_network.txt", "graphs/NetworkTimeSimple_network_simple.txt"]
name = "graphs/NetworkTime_file.png"
title = "Network load related to file size"
x_axis = 'File Size (bits)'
y_axis = 'Elapsed time (ms)'

dictionary = {}
dictionary1 = {}
dictionary2 = {}

with open(files[0]) as f1:
    l1 = f1.read().splitlines()
with open(files[1]) as f2:
    l2 = f2.read().splitlines()
for i in range(int((len(l1)+len(l2))/2)):
    n1, size1, time1, thread1 = l1[i].split(", ")
    n2, size2, time2, thread2 = l2[i].split(", ")
    if dictionary.get(int(size2)) is None:
        dictionary[int(size2)] = []
        dictionary1[int(size2)] = []
        dictionary2[int(size2)] = []
    list = dictionary.get(int(size2))
    list.append(int(time2)-int(time1))
    dictionary[int(size2)] = list
    list = dictionary1.get(int(size2))
    list.append(int(time1))
    dictionary1[int(size2)] = list
    list = dictionary2.get(int(size2))
    list.append(int(time2))
    dictionary2[int(size2)] = list

plt.figure()
x = [key for key in dictionary]
y = []
y1 = []
y2 = []
for i in range(len(x)):
    y.append(sum(dictionary[x[i]])/len(dictionary[x[i]]))
    y1.append(sum(dictionary1[x[i]]) / len(dictionary1[x[i]]))
    y2.append(sum(dictionary2[x[i]]) / len(dictionary2[x[i]]))

print(y)
print(y1)
print(y2)
plt.plot(x, y, label='Network', color='magenta')
plt.plot(x, y1, label='Computation time', color='cyan')
plt.plot(x, y2, label='Server response (network and computation)', color='purple')
plt.title(title)
plt.ylabel(y_axis)
plt.xlabel(x_axis)
plt.ylim(bottom=0)
plt.grid()
plt.legend(loc='best', fontsize='small')
plt.savefig(name)
plt.close()
