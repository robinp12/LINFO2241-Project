import numpy as np
import numpy.polynomial.polynomial as poly
from matplotlib import pyplot as plt
from scipy.interpolate import CubicSpline as CS

#!/usr/bin/env python3

from numpy import *
import matplotlib.pyplot as plt

files = ["data_thinkers.csv", "data_producersconsumers.csv", "data_readerswriters.csv"]
titles = ["Thinkers' algorithm\nExecution time", "Producers-consumers' algorithm\nExecution time", "Readers-writers' algorithm\nExecution time"]
x_axis = 'Thread number'
y_axis = 'Execution time (ms)'
names = ["thinkers", "prodcons", "readwrite"]


Argument_processing = 1000*array([0.000113, 0.000106, 0.000050])
Socket_creation = 1000*array([0.000351, 0.000374, 0.000295])
Poll_initialization = 1000*array([0.000001, 0.000001, 0.000000])
Process_packet_time = 1000*array([0.000013, 0.000016, 0.000003,  0.000002, 0.000002, 0.000025, 0.000027, 0.000007, 0.000022, 0.000006, 0.000015, 0.000017, 0.000004, 0.000004, 0.000009])
Reception_window_time = 1000*array([0.000028, 0.000060, 0.000017, 0.000014, 0.000057, 0.000085, 0.000057, 0.000062, 0.000041, 0.000062, 0.000036, 0.000028])
Send_loop_time = 1000*array([0.000260, 0.000600, 0.000369])
Receiver_time = 1000*array([0.000774, 0.001252, 0.000807])
name = ["Argument processing", "Socket creation", "Poll initialization", "Process packet_time", "Reception window time", "Send_loop time", "Receiver time"]
tit = "Receiver performances on truncating network"
toprint = [Argument_processing, Socket_creation, Poll_initialization, Process_packet_time, Reception_window_time, Send_loop_time, Receiver_time]

def graph(title, y_axis, name, file):
    with open(file) as f:
        measures = f.read().splitlines()[1:]
    
    data = {}
    
    for m in measures:
        core, tim = m.split(" : ")
        cores = int(core)
        time = float(tim)
    
        l = data.get(cores, [])
        l.append(time)
        data[cores] = l
    
    x = list(data.keys())
    y = [sum(v) / len(v) for v in data.values()]

    n = 0
    for v in toprint:
        stand = round(std(v), 6)
        maxi = round(amax(v), 6)
        mini = round(amin(v), 6)
        moy = round(mean(v), 6)
        print("{}: {}\nσ={}ms, mean={}ms, max={}ms, min={}ms".format(n, name[n], stand, moy, maxi, mini))

        # plt.stem([n_thread], [moy], linefmt="w-", markerfmt="w", basefmt="w", label="${}\ thread:$\n $\sigma={}s,\ mean={}s,\ max={}s,\ min={}s$".format(n_thread, stand, moy, maxi, mini))
        n += 1

    # plt.plot(x, y)
    plt.boxplot(toprint, positions=[0, 1, 2, 3, 4, 5, 6])
    plt.title(title)
    # plt.xlabel(x_axis)
    plt.ylabel(y_axis)
    plt.ylim(bottom=0)
    plt.grid()
    plt.legend(loc='best', ncol=2, fontsize='xx-small', labelspacing=0)
    plt.savefig(tit + '.png', bbox_inches='tight')
    plt.show()


# V = [ 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5]
# I = [0, 4.4, 7.3, 10, 12.7, 15, 17.3, 19.1, 19, 24.3]
# x = np.linspace(1, 6, 10000)
# plt.title("Buzzer's impedance")
# plt.xlabel("V")
# plt.ylabel("I (mA)")
# type = int(input ("Do you wanna approximate (1) or interpolate (2) the data ?\n"))
# if type == 1:
#     level = int(input("With a polynom of which degree ? (1 to 9)\n"))
    
#     if level == 1:
#         trend1 = poly.polyfit(V, I, 1)
#         trendpoly1 = poly.polyval(x, trend1) 
#         plt.plot(x, trendpoly1, 'pink', label='linear approximation')
#     if level == 2:
#         trend2 = poly.polyfit(V, I, 2)
#         trendpoly2 = poly.polyval(x, trend2) 
#         plt.plot(x, trendpoly2, 'purple', label='quadratic approximation')
#     if level == 3:
#         trend3 = poly.polyfit(V, I, 3)
#         trendpoly3 = poly.polyval(x, trend3) 
#         plt.plot(x, trendpoly3, 'cyan', label='cubic approximation')
#     if level == 4:
#         trend4 = poly.polyfit(V, I, 4)
#         trendpoly4 = poly.polyval(x, trend4) 
#         plt.plot(x, trendpoly4, 'yellow', label='4th power approximation')
#     if level == 5:
#         trend5 = poly.polyfit(V, I, 5)
#         trendpoly5 = poly.polyval(x, trend5) 
#         plt.plot(x, trendpoly5, 'silver', label='5th power approximation')
#     if level == 6:
#         trend6 = poly.polyfit(V, I, 6)
#         trendpoly6 = poly.polyval(x, trend6) 
#         plt.plot(x, trendpoly6, 'orange', label='6th power approximation')
#     if level == 7:
#         trend7 = poly.polyfit(V, I, 7)
#         trendpoly7 = poly.polyval(x, trend7) 
#         plt.plot(x, trendpoly7, 'magenta', label='7th power approximation')
#     if level == 8:
#         trend8 = poly.polyfit(V, I, 8)
#         trendpoly8 = poly.polyval(x, trend8) 
#         plt.plot(x, trendpoly8, 'green', label='8th power approximation')
#     if level == 9:
#         trend9 = poly.polyfit(V, I, 9)
#         trendpoly9 = poly.polyval(x, trend9) 
#         plt.plot(x, trendpoly9, 'red', label='9th power approximation')
    
# if type == 2:
#     cs = CS(V, I)
#     plt.plot(x, cs(x), 'black', label='CubicSpline interpolation')
    
# plt.plot (V, I, '.b', markersize = 10)
# plt.legend(loc='upper left', ncol = 3)
# plt.plot()