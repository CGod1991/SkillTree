package org.xzd.javacode.event;

import org.apache.hadoop.yarn.event.AbstractEvent;

/**
 * Created by xuzd on 17-7-31.
 */
public class TaskEvent extends AbstractEvent {
    private String taskID;

    public TaskEvent(String taskID, TaskEventType type) {
        super(type);
        this.taskID = taskID;
    }

    public String getTaskID() {
        return taskID;
    }
}
