package org.xzd.javacode.event;

import org.apache.hadoop.yarn.event.AbstractEvent;

/**
 * Created by xuzd on 17-7-31.
 */
public class JobEvent extends AbstractEvent {
    private String jobID;

    public JobEvent(String jobID, JobEventType type) {
        super(type);
        this.jobID = jobID;
    }

    public String getJobID() {
        return jobID;
    }
}
