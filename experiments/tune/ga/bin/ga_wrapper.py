# This script is a wrapper around a MuACOsm launcher.
# The purpose is:
#    * read algorithm parameters passed by irace via command line,
#    * create a sandbox, write config files with set parameter values
#    * launch MuACOsm, retreive and return selected cost measure (fitness, computational time, etc)
# The output is the mean runtime of MuACOsm - that is what we are trying to minimize
# However, it could also be the max fitness value achieved by the algorithm (in the case where
# without training we cannot reach the desired fitness in a resonalble amount of fitness evaluations)

import sys
import tempfile
import os
from case_stats import *

default_params = { "pop-size" : 100,
                   "part-stay" : 0.1,
                   "time-small-mutation" : 50,
                   "time-big-mutation" : 100,
                   "mut-prob" : 0.01
                 }

def read_param_names():
    param_names = []
    for line in open("parameters-ga.txt"):
        param = line.split("\t")[1]
        param = param.replace("\"", "")
        param = param.replace("--", "")
        param = param.replace(" ", "")
        param_names += [param]
    return param_names

def write_ga_properties(params):
    os.system("m4 -D M4_POPULATION_SIZE=%s -D M4_PART_STAY=%s -D M4_TIME_SMALL_MUTATION=%s -D M4_TIME_BIG_MUTATION=%s -D M4_MUTATION_PROBABILITY=%s problem.xml.tmpl > problem.xml" %\
               (params["pop-size"], params["part-stay"], params["time-small-mutation"], params["time-big-mutation"], params["mut-prob"]))

def launch(params, instance_path):
    cwd = os.getcwd()
    sandbox_path = tempfile.mkdtemp(prefix="sandbox-", dir=cwd)
    os.system("chmod 777 %s" % sandbox_path)
    #os.mkdir(sandbox_path)
    os.system("cp -r default/* %s" % sandbox_path)
    true_instance_path = open(instance_path).readlines()[0][:-1]
    print "True instance path = %s" % true_instance_path
    os.system("cp %s/* %s/" % (true_instance_path, sandbox_path))
    os.chdir(sandbox_path)
    write_ga_properties(params)
    os.system("mkdir attempts && java -jar -Xms1g -Xmx1g gabp.jar problem.xml -1 10 1 > log.log 2>>errlog.log")
    os.chdir(cwd)
 
    fitness = float(open(glob.glob("%s/attempts/attempt0/*.metadata" % sandbox_path)[0]).readlines()[0].split()[-1][:-1])
    time = float(open(glob.glob("%s/attempts/attempt0/*.metadata" % sandbox_path)[0]).readlines()[2].split()[-1][:-1])

    print "Best %f %f" % (3.0 - fitness, time)
    os.system("rm -r %s" % sandbox_path)

def main():
   # print "Launching"
    param_names = read_param_names()
    params = dict()
    for param_name in param_names:
        irace_param = "--%s" % param_name
        if irace_param in sys.argv:
            params[param_name] = sys.argv[sys.argv.index(irace_param) + 1]
    instance_path = sys.argv[sys.argv.index("instance") + 1]
    launch(params, instance_path)
    pass

if __name__ == '__main__':
    main()
