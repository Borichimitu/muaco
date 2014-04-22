import glob
from numpy import *
def getAttemptInfoForACO(dirname):
    filename = glob.glob(dirname + '/*metadata')[0]
    f = open(filename)
    i = 0
    fitness = 0.0
    steps = 0
    time = 0.0
    for line in f:
        if i == 0:
            fitness = float(line.split(" ")[-1])
            i += 1
            continue
        if i == 1:
            steps = int(line.split(" ")[-1])
            i += 1
            continue
        if i == 2:
            time = float(line.split(" ")[-1])
            i += 1
            continue
    return [fitness, time, steps]

def summ(data):
    result = zeros(len(data))
    for i in range(len(data)):
        for j in range(len(data[i])):
            result[i] += data[i][j]
    return result

def get_standard_deviation(data, ex):
    sigma = 0 
    for i in range(len(data)):
        sigma += (data[i] - ex) * (data[i] - ex)
    return sqrt(sigma / float(len(data) - 1))

def get_stats(path):
    AcoAttemptsInfo = []
    for dirname in glob.glob('%s/attempts/attempt*' % path):
        try:
            AcoAttemptsInfo += [getAttemptInfoForACO(dirname)]
        except Exception:
            continue
    AcoAttemptsInfo = sorted(AcoAttemptsInfo)
    acoTime = array([v[1] for v in AcoAttemptsInfo])
    acoSteps = array([v[2] for v in AcoAttemptsInfo])

    mean_steps = sum(acoSteps) / len(acoSteps) 


    if len(acoSteps) > 0:
        return mean_steps, sum(acoTime) / len(acoTime), len(acoSteps), get_standard_deviation(acoSteps, mean_steps)
    return "-1", "-1", "-1", "-1"

