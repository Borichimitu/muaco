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

default_params = { 'muaco.stagnation-parameter' : 50,
                   'muaco.big-stagnation-parameter' : 4,
                   'muaco.output-period' : 1,
                   'muaco.number-of-ants' : 5,
                   'muaco.evaporation-rate' : 0.7,
                   'muaco.max-number-of-nodes' : 2000000,
                   'muaco.mutators' : "CHANGE_DEST,EFSM_ADD_DELETE_TRANSITIONS",
                   'muaco.path-selector': "HEURISTIC",
                   'muaco.start-nodes-selector' : "BEST",
                   'muaco.ant-type' : "CURRENT",
                   'muaco.ant-colony-type' : "STEP_BY_STEP",
                   'muaco.pheromone-updater' : "GLOBAL_ELITIST_MIN_BOUND",
                   'muaco.use-rising-paths' : "true",
                   'muaco.heuristic-distance' : "ABS_DIFF",
                   'muaco.instance-generator' : "PLAIN",
                   'muaco.construction-graph-type' : "PLAIN",
                   'muaco.add-delete-transition-probability' : 0.05,
                   'muaco.max-evals-till-stagnation' : 10000000,
                   'muaco.number-of-threads' : 1,
                   'path_selector.numberOfMutationsPerStep' : 15,
                   'path_selector.newMutationProbability' : 0.5,
                   'path_selector.alpha' : 1.0,
                   'path_selector.beta' : 1.0,
                   'path_selector.use-heuristic-distance' : "true",
                   'path_selector.edge-selector' : "SINGLE_OBJECTIVE"
                 }

def read_param_names():
    param_names = []
    for line in open("parameters-muaco.txt"):
        param = line.split("\t")[1]
        param = param.replace("\"", "")
        param = param.replace("--", "")
        param = param.replace(" ", "")
        param_names += [param]
    return param_names

def write_muaco_properties(params, sandbox_path):
    out_muaco = open("%s/muaco.properties" % sandbox_path, 'w')
    out_path_selector = open("%s/heuristic-path-selector.properties" % sandbox_path, 'w')

    for param_name in default_params.keys():
        param_value = 0
        if param_name in params:
            param_value = params[param_name]
        else: 
            param_value = default_params[param_name]

        short_param_name = param_name[param_name.find(".") + 1:]
        if "muaco" in param_name:
            print >> out_muaco, "%s=%s" % (short_param_name, param_value)
        else:
            if "path_selector" in param_name:
                print >> out_path_selector, "%s=%s" % (short_param_name, param_value) 
    out_muaco.close()
    out_path_selector.close()

def launch(params, instance_path):
    cwd = os.getcwd()
    sandbox_path = tempfile.mkdtemp(prefix="sandbox-", dir=cwd)
    os.system("chmod 777 %s" % sandbox_path)
    #os.mkdir(sandbox_path)
    os.system("cp -r default/* %s" % sandbox_path)
    true_instance_path = open(instance_path).readlines()[0][:-1]
    print "True instance path = %s" % true_instance_path
    os.system("cp %s/* %s/" % (true_instance_path, sandbox_path))
    write_muaco_properties(params, sandbox_path)
    os.chdir(sandbox_path)
    os.system("java -jar -Xms2g -Xmx2g muaco.jar > log.log 2>>errlog.log")
    os.chdir(cwd)
 
    fitness = float(open(glob.glob("%s/attempts/attempt0/*_metadata" % sandbox_path)[0]).readlines()[0].split()[-1][:-1])
    time = float(open(glob.glob("%s/attempts/attempt0/*_metadata" % sandbox_path)[0]).readlines()[2].split()[-1][:-1])

    print "Best %f %f" % (3.0 - fitness, time)
    os.system("rm -r %s" % sandbox_path)

#    mean_steps = get_stats(sandbox_path)[0]
#    os.system("rm -r %s" % sandbox_path)
#    print "Best %d" % mean_steps

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
