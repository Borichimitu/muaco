=====Building MuACO and GA=====
1. cd to src/gabp and execute: 
    ant && ./deploy

2. cd to src/muaco and execute:
    ant && ant -f build-jar.xml && ./deploy

=====Tuning Parameters=====
1. To generate training instances:
   cd to experiments/tune/instances and execute:
    ./generate 

Steps 2 and 3 can be done in parallel in separate terminals or using screen.
They will take 12 hours to complete.

2. cd to experiments/tune/ga and execute:
    ./make-instances.sh && ./train.sh

3. cd to experiments/tune/muaco and execute:
    ./make-instances.sh && ./train.sh

After tuning is complete, the best parameter values will be at the bottom of log.log files in tuning directories.
Copy parameter values to:
    * elevator.xml and clock.xml for GA
    * muaco.properties and heuristic-path-selector.properties for MuACO

=====Running experiments=====
To launch an experiment, cd to the experiment's directory and execute:
     ./launch

Experiments workdirs are as follows:
* experiments/clock/scenarios/ga
* experiments/clock/scenarios/muaco
* experiments/clock/scenarios+LTL/ga
* experiments/clock/scenarios+LTL/muaco
* experiments/elevator/scenarios/ga
* experiments/elevator/scenarios/muaco
* experiments/elevator/scenarios+LTL/ga
* experiments/elevator/scenarios+LTL/muaco

To calculate statistics, run "./stats" in the experiment's dir after the experiment is finished.

To do the Wilcoxon statistical test, go to, for example, experiments/clock/scenarios/wilcoxon-test and run "./compare".
