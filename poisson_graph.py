from numpy import *
import matplotlib.pyplot as plt
import numpy.polynomial.polynomial as poly
from scipy.interpolate import CubicSpline as CS

files = "graphs/NetworkTimePool_new.txt"
name = "graphs/Poisson.png"
title = "Server response time by number of requests"
x_axis = 'Number of threads'
y_axis = 'Elapsed time(ms)'
y_axis2 = 'Number of rejected requests'

dictionary = {}
dictionary1 = {}

with open(files) as f1:
    l1 = f1.read().splitlines()
for i in range(len(l1)):
    n1, size1, time1, thread1, error = l1[i].split(", ")
    if dictionary.get(int(n1)) is None:
        dictionary[int(n1)] = []
        dictionary1[int(n1)] = []
    list = dictionary.get(int(n1))
    list.append(int(time1))
    dictionary[int(n1)] = list
    list = dictionary1.get(int(n1))
    list.append(int(error))
    dictionary1[int(n1)] = list

plt.figure()
x = [key for key in dictionary]
y = []
y1 = []
# y2 = []
for i in range(len(x)):
    y.append(sum(dictionary[x[i]])/len(dictionary[x[i]]))
    y1.append(sum(dictionary1[x[i]])/len(dictionary1[x[i]]))
    # y2.append()

print(y)
print(y1)
# print(y2)
fig, ax1 = plt.subplots()

ax1.set_xlabel(x_axis)
ax1.set_ylabel(y_axis, color='magenta')
ax1.plot(x, y, color='magenta')
ax1.tick_params(axis='y', labelcolor='magenta')

# Adding Twin Axes

ax2 = plt.twinx()
ax2.set_ylabel(y_axis2, color='turquoise')
ax2.plot(x, y1, color='turquoise')
ax2.tick_params(axis='y', labelcolor='turquoise')
# plt.plot(x, y2, label='Server response (network and computation)', color='purple')
plt.title(title)
plt.ylim(bottom=0)
plt.grid()
# plt.legend(loc='best', fontsize='small')
plt.savefig(name)
plt.close()
