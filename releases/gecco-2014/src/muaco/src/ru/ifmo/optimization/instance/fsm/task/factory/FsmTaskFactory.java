package ru.ifmo.optimization.instance.fsm.task.factory;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.testsmodelchecking.TestsModelCheckingTask;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.TestsWithLabelingTask;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class FsmTaskFactory extends AbstractTaskFactory {
	private static enum TaskName {
		TESTS,
		MODEL_CHECKING
	}
	
	
	public FsmTaskFactory(AbstractTaskConfig config) {
		super(config);
	}
	
	public AbstractOptimizationTask<FSM> createTask() {
		TaskName taskName = TaskName.valueOf(config.getTaskName());
		AbstractOptimizationTask<FSM> fsmTask = null;
		switch (taskName) {
		case TESTS:
			fsmTask = new TestsWithLabelingTask(config);
			break;
		case MODEL_CHECKING:
			fsmTask = new TestsModelCheckingTask(config);
			break;
		default:
			throw new IllegalStateException();
		}
		
		FSM.setEvents(((AbstractAutomatonTask)fsmTask).getEvents());
		return fsmTask;
	}
}
