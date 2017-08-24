package org.xzd.javacode.event;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.CompositeService;
import org.apache.hadoop.service.Service;
import org.apache.hadoop.yarn.event.AsyncDispatcher;
import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.event.EventHandler;

/**
 * Created by xuzd on 17-7-31.
 */
public class SimpleMRAppMaster extends CompositeService {
    private Dispatcher dispatcher;
    private String jobID;
    private int taskNumber;
    private String[] taskIDs;

    public SimpleMRAppMaster(String name, String jobID, int taskNumber) {
        super(name);
        this.jobID = jobID;
        this.taskNumber = taskNumber;
        taskIDs = new String[taskNumber];

        for (int i = 0; i < taskNumber; i++){
            taskIDs[i] = new String(jobID + "_task_" + i);
        }
    }

    public void serviceInit(final Configuration configuration) throws Exception {
        dispatcher = new AsyncDispatcher();
        dispatcher.register(JobEventType.class, new JobEventDispatcher());
        dispatcher.register(TaskEventType.class, new TaskEventDispatcher());
        addService((Service) dispatcher);
        super.serviceInit(configuration);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    private class JobEventDispatcher implements EventHandler<JobEvent> {

        public void handle(JobEvent jobEvent) {
            if (jobEvent.getType() == JobEventType.JOB_KILL) {
                System.out.println("Receive JOB_KILL event, killing all the tasks!");

                for (int i = 0; i < taskNumber; i++) {
                    dispatcher.getEventHandler().handle(new TaskEvent(taskIDs[i], TaskEventType.T_KILL));
                }
            } else  if (jobEvent.getType() == JobEventType.JOB_INIT) {
                System.out.println("Receive JOB_INIT event, scheduling tasks!");

                for (int i = 0; i < taskNumber; i++) {
                    dispatcher.getEventHandler().handle(new TaskEvent(taskIDs[i], TaskEventType.T_SCHEDULE));
                }
            }
        }
    }

    private class TaskEventDispatcher implements EventHandler<TaskEvent> {

        public void handle(TaskEvent taskEvent) {
            if (taskEvent.getType() == TaskEventType.T_KILL) {
                System.out.println("Receive T_KILL event of task " + taskEvent.getTaskID());
            } else if (taskEvent.getType() == TaskEventType.T_SCHEDULE) {
                System.out.println("Receive T_SCHEDULE event of task " + taskEvent.getTaskID());
            }
        }
    }
}
