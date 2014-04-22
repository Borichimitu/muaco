package ru.ifmo.optimization.instance.fsm.algorithm;

import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;

public abstract class AbstractAutomataOptimizationAlgorithm {
	protected AbstractAutomatonTask task;
	
	public AbstractAutomataOptimizationAlgorithm(AbstractAutomatonTask task) {
		this.task = task;
	}
	
	public void setTask(AbstractAutomatonTask task) {
		this.task = task;
	}
	
	public abstract InstanceMetaData<FSM> run();
	public InstanceMetaData<FSM> run(int maxNumberOfAttempts) {
		return null;
	}
	
	public InstanceMetaData<FSM> run(int maxNumberOfAttempts, FSM startSolution) {
		return null;
	}
	
}
