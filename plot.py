from numpy import *
import matplotlib.pyplot as plt
import numpy.polynomial.polynomial as poly
from scipy.interpolate import CubicSpline as CS

files = ["printfile.txt", "printfile_wait.txt", "printfile_pass.txt", "printfile_pass_opti.txt"]
titles = ["Server response time"]
x_axis = 'Number of clients'
X_axis2 = 'Password length'
y_axis = 'Execution time (ms)'
names = ["server", "server-client-delay", "password", "password_opti"]


def get_data(dictionary, file):
    with open(file) as f:
        measures = f.read().splitlines()

    for m in measures:
        tn, fs, tim, pl = m.split(", ")
        thread_number = int(tn)
        file_length = int(fs)
        time = int(tim)
        password_length = int(pl)

        l = dictionary.get(thread_number, [])
        l.append((file_length, time, password_length))
        # print(l)
        dictionary[thread_number] = l

d1 = {}
d2 = {}
d3 = {}
d4 = {}
x3 = linspace(1, 100, 10000)

get_data(d1, files[0])
get_data(d2, files[1])
get_data(d3, files[2])
get_data(d4, files[3])


x = list(d1.keys())
y = []
for key in d1.keys():
    length = 0
    sum = 0
    for measure in d1.get(key):
        (fs, tim, pl) = measure
        sum += tim
        length += 1
    y.append(sum/length)
plt.plot(x, y, label='Clients sending requests at the same time')


plt.title(titles[0])
plt.ylabel(y_axis)
plt.xlabel(x_axis)
plt.ylim(bottom=0)
plt.grid()
# plt.legend(loc='best', ncol=2, fontsize='xx-small', labelspacing=0)
plt.savefig(names[0] + '.png', bbox_inches='tight')
plt.show()

x = list(d2.keys())
y = []
for key in d2.keys():
    length = 0
    sum = 0
    for measure in d2.get(key):
        (fs, tim, pl) = measure
        sum += tim
        length += 1
    y.append(sum/length)
plt.plot(x, y, label='Clients requests delayed')


plt.title(titles[0])
plt.ylabel(y_axis)
plt.xlabel(x_axis)
plt.ylim(bottom=0)
plt.grid()
# plt.legend(loc='best', ncol=2, fontsize='xx-small', labelspacing=0)
plt.savefig(names[1] + '.png', bbox_inches='tight')
plt.show()

x = []
y = []
for measure in d4.get(1):
    (fs, tim, pl) = measure
    x.append(pl)
    y.append(tim)
plt.plot(x, y, label='Basic server', color='cyan')
plt.title(titles[0])
plt.ylabel(y_axis)
plt.xlabel('Password length')
plt.ylim(bottom=0)
plt.grid()
# plt.legend(loc='best', ncol=2, fontsize='xx-small', labelspacing=0)
plt.savefig(names[2] + '.png', bbox_inches='tight')
plt.show()
x = []
y = []
for measure in d4.get(1):
    (fs, tim, pl) = measure
    x.append(pl)
    y.append(tim)
plt.plot(x, y, label='Optimized server', color='magenta')

plt.title(titles[0])
plt.ylabel(y_axis)
plt.xlabel('Password length')
plt.ylim(bottom=0)
plt.grid()
# plt.legend(loc='best', ncol=2, fontsize='xx-small', labelspacing=0)
plt.savefig(names[3] + '.png', bbox_inches='tight')
plt.show()
