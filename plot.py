from numpy import *
import matplotlib.pyplot as plt
import numpy.polynomial.polynomial as poly
from scipy.interpolate import CubicSpline as CS

files = ["printfile.txt", "printfile_wait.txt", "printfile_pass.txt", "printfile_pass_opti.txt"]
titles = ["Server response time"]
x_axis = 'Number of clients'
X_axis2 = 'Password length'
y_axis = 'Execution time (ms)'
names = ["server", "server-client-delay", "password"]


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
print(d1.values())
for x in d1.values():
    print(x)
y = [sum(tim) / len(tim) for (fs, tim, pl) in [c for c in d1.values()]]
plt.stem(x, y, label='Clients sending requests at the same time')


plt.title(titles[0])
plt.ylabel(y_axis)
plt.ylim(bottom=0)
plt.grid()
# plt.legend(loc='best', ncol=2, fontsize='xx-small', labelspacing=0)
plt.savefig(names[0] + '.png', bbox_inches='tight')
plt.show()

x = list(d2.keys())
y = [sum(tim) / len(tim) for (fs, tim, pl) in d2.values()]
plt.stem(x, y, label='Clients requests delayed')


plt.title(titles[0])
plt.ylabel(y_axis)
plt.ylim(bottom=0)
plt.grid()
# plt.legend(loc='best', ncol=2, fontsize='xx-small', labelspacing=0)
plt.savefig(names[1] + '.png', bbox_inches='tight')
plt.show()

y = [pl for (fs, tim, pl) in d3.values()]
y = [sum(tim) / len(tim) for (fs, tim, pl) in d3.values()]
plt.plot(x, y, label='Basic server')
y = [pl for (fs, tim, pl) in d4.values()]
y = [sum(tim) / len(tim) for (fs, tim, pl) in d4.values()]
plt.plot(x, y, label='Optimized server')

plt.title(titles[0])
plt.ylabel(y_axis)
plt.ylim(bottom=0)
plt.grid()
# plt.legend(loc='best', ncol=2, fontsize='xx-small', labelspacing=0)
plt.savefig(names[2] + '.png', bbox_inches='tight')
plt.show()
