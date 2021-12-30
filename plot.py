from numpy import *
import matplotlib.pyplot as plt
import numpy.polynomial.polynomial as poly
from scipy.interpolate import CubicSpline as CS

files = ["graphs/perfPoolTime.txt", "graphs/perfPwdLen.txt", "graphs/simplebruteforce.txt", "graphs/improvedbruteforce.txt", "graphs/printfile_pass_opti.txt", "graphs/printfile.txt"]
titles = ["Server response time", "Password bruteforce execution time"]
x_axis = 'Number of clients'
X_axis2 = 'Password length'
y_axis = 'Execution time (ms)'
names = ["graphs/server.png", "graphs/server-client-delay.png", "graphs/bruteforcePerformance.png", "graphs/password.png", "graphs/password_opti.png"]

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
        client, size, time, length = m.split(", ")

        thread_number = int(client)
        file_length = int(size)
        time = int(time)
        password_length = int(length)

        dictionary[thread_number] = [file_length,time,password_length]

def get_data2(dictionary, file):
    with open(file) as f:
        measures = f.read().splitlines()

    for m in measures:
        tim, pl = m.split(", ")
        time = int(tim)
        password_length = int(pl)

        l = dictionary.get(password_length, time)
        dictionary[password_length] = l

def make_plot(table, title,name,lab):
    plt.figure()
    x = list(table.keys())
    y = []
    length = 0
    sum = 0
    for measure in table:
        #print(measure)
        #print(table[measure])
        fs = table[measure][0]
        tim = table[measure][1]
        pl = table[measure][2]

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

def make_plot2(table,table1, title,name):
    plt.figure()
    x = []
    y = []
    x1 = []
    y1 = []
    for e in table:
        x.append(e)
        y.append(table[e])

    for e in table1:
            x1.append(e)
            y1.append(table1[e])

    plt.plot(x, y, label='Basic bruteforce', color='orange')
    plt.plot(x1, y1, label='Improved bruteforce', color='blue')
    plt.title(title)
    plt.ylabel(y_axis)
    plt.xlabel('Password length')
    plt.ylim(bottom=0)
    plt.grid()
    plt.legend(loc='best', fontsize='small')
    plt.savefig(name)
    plt.close()

d1 = {}
d2 = {}
d3 = {}
d4 = {}

#Make graph server delay
get_data(d1, files[0])
get_data(d2, files[1])
make_plot(d1,titles[0],names[0],'Clients sending requests at the same time')
make_plot(d2,titles[0],names[1],'Clients requests delayed')

## Make graph bruteforce time
get_data2(d4, files[3])
get_data2(d3, files[2])
make_plot2(d3,d4,titles[1],names[2])

print("Graphs are generated in graphs directory")