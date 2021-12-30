from numpy import *
import matplotlib.pyplot as plt
import numpy.polynomial.polynomial as poly
from scipy.interpolate import CubicSpline as CS

files = ["graphs/poolpassword.txt", "graphs/onepassword.txt", "graphs/simplebruteforce.txt", "graphs/printfile_pass_opti.txt", "graphs/printfile.txt"]
titles = ["Server response time", "Bruteforce execution time"]
x_axis = 'Number of clients'
X_axis2 = 'Password length'
y_axis = 'Execution time (ms)'
names = ["graphs/server.png", "graphs/server-client-delay.png", "graphs/simplebruteforce.png", "graphs/password.png", "graphs/password_opti.png"]

def sortarrays(sorted,ret,dic):
    for i in sorted:
        for k in dic.keys():
            if dic[k] == i:
                ret[k] = dic[k]
                break

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

def get_data2(dictionary, file):
    with open(file) as f:
        measures = f.read().splitlines()

    for m in measures:
        tim, pl = m.split(", ")
        time = int(tim)
        password_length = int(pl)

        l = dictionary.get(password_length, time)
        dictionary[password_length] = l

d1 = {}
d2 = {}
d3 = {}
d4 = {}
x3 = linspace(1, 100, 10000)

get_data(d1, files[0])
get_data(d2, files[1])
get_data2(d3, files[2])
#get_data(d4, files[3])

#sort before showing in graphs
sorted_values = sorted(d3.values()) # Sort the values
sorted_dict = {}
sortarrays(sorted_values,sorted_dict,d3)

def make_plot(table, title,name,lab):
    x = list(table.keys())
    y = []
    for key in table.keys():
        length = 0
        sum = 0
        for measure in table.get(key):
            (fs, tim, pl) = measure
            sum += tim
            length += 1
        y.append(sum/length)
    plt.plot(x, y, label=lab)
    plt.title(title)
    plt.ylabel(y_axis)
    plt.xlabel(x_axis)
    plt.ylim(bottom=0)
    plt.grid()
    plt.legend(loc='best', fontsize='small')
    plt.savefig(name)
    plt.close()

def make_plot2(table, title,name,lab):
    x = []
    y = []
    for e in table:
        x.append(e)
        y.append(table[e])
    plt.plot(x, y, label=lab, color='red')
    plt.title(title)
    plt.ylabel(y_axis)
    plt.xlabel('Password length')
    plt.ylim(bottom=0)
    plt.grid()
    plt.legend(loc='best', fontsize='small')
    plt.savefig(name)
    plt.close()

make_plot(d1,titles[0],names[0],'Clients sending requests at the same time')
make_plot(d2,titles[0],names[1],'Clients requests delayed')
make_plot2(d3,titles[1],names[2],'Basic bruteforce')
#make_plot2(d4,titles[0],names[3],'Optimized server')

print("Graphs generated in graphs directory")