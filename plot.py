from numpy import *
import matplotlib.pyplot as plt
import numpy.polynomial.polynomial as poly
from scipy.interpolate import CubicSpline as CS

files = ["graphs/NetworkTimeSimple.txt", "graphs/NetworkTimePool.txt", "graphs/CPUTimeSimple.txt", "graphs/CPUTimeImproved.txt"]
names = ["graphs/NetworkTime.png", "graphs/server-client-delay.png", "graphs/CPUTime.png"]
x_axis = 'Number of clients'
X_axis2 = 'Password length'
y_axis = 'Execution time (ms)'

def get_data(dictionary,dictionary1, file):
    with open(file) as f:
        measures = f.read().splitlines()
    i = 0
    a = 0
    for m in measures:
        client, size, time, pwdlength = m.split(", ")

        #Divise le tableau en 2 (premiere partie mesure simple et 2eme partie amelioré)
        if(i<=6):
        #Simple perf
            dictionary[i] = [client, size, time, pwdlength]
        if(size!='2681188'):
        # Mettre les mesures de taille de fichier dans un tableau spécifique
            dictionary1[a] = [client, size, time, pwdlength]
            a=a+1
        i=i+1

def get_data2(dictionary, file):
    with open(file) as f:
        measures = f.read().splitlines()

    for m in measures:
        tim, pl = m.split(", ")
        time = int(tim)
        password_length = int(pl)

        l = dictionary.get(password_length, time)
        dictionary[password_length] = l

def make_plotDelay(table, table2, title, name):
    plt.figure()
    xsimple = []
    ysimple = []
    for measure in table:

        #client = int(table[measure][0])
        filesize = table[measure][1]
        # Calcul du temps sur le reseau (temps total - temps CPU)
        # pour le server simple
        time = int(table[measure][2])-table2[measure+1]
        #password_length = int(table[measure][3])
        xsimple.append(filesize)
        ysimple.append(time)


    plt.plot([e[:-3] for e in xsimple], ysimple, label="Transfer time", color='orange')

    plt.title(title)
    plt.ylabel(y_axis)
    plt.xlabel('File size (Kb)')
    plt.ylim(bottom=0)
    plt.grid()
    plt.legend(loc='best', fontsize='small')
    plt.savefig(name)
    plt.close()

def make_plotCPUTime(table,table1, title,name):
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

NetworkSimple = {}
NetworkImproved = {}
NetworkPool = {}
CPUSimple = {}
CPUSimproved = {}

#Make graph server delay
get_data(NetworkSimple,NetworkImproved, files[0])
get_data2(CPUSimproved, files[3])
get_data2(CPUSimple, files[2])

#get_data(NetworkPool, files[1])
#make_plot(NetworkPool,"Server response time",names[1],'Clients requests delayed')

## Make graph bruteforce time
# Décommenter pour modifier le graph du temps en fonction de la taille des fichiers
#make_plotDelay(NetworkImproved,CPUSimple,"Time to transfer file through network",names[0])

# Décommenter pour modifier le graph du temps CPU en fonction de la taille du mdp
#make_plotCPUTime(CPUSimple,CPUSimproved,"Bruteforce execution time on CPU",names[2])

print("Graphs are generated in graphs directory")