from numpy import *
import matplotlib.pyplot as plt
import numpy.polynomial.polynomial as poly
from scipy.interpolate import CubicSpline as CS

files = ["printfile.txt", "printfile_wait.txt", ]
titles = ["Server response time"]
x_axis = 'Number of clients'
y_axis = 'Execution time (ms)'
names = ["server"]


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
        l.append((thread_number, file_length, time, password_length))
        dictionary[thread_number] = l

d1 = {}
d2 = {}
d3 = {}
d4 = {}
x3 = linspace(1, 100, 10000)

get_data(d1, files[0])
get_data(d2, files[1])
get_data(d3, files[2])
get_data(d4, files[4])


x = list(d1.keys())
y = [sum(v) / len(v) for v in d1.values()]
plt.stem(x, y, label='Clients sending requests at the same time')


plt.title(titles[0])
plt.ylabel(y_axis)
plt.ylim(bottom=0)
plt.grid()
# plt.legend(loc='best', ncol=2, fontsize='xx-small', labelspacing=0)
plt.savefig(names[0] + '.png', bbox_inches='tight')
plt.show()