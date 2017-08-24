package org.apache.hadoop.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.xzd.javacode.event.JobEvent;
import org.xzd.javacode.event.JobEventType;
import org.xzd.javacode.event.SimpleMRAppMaster;

/**
 * Created by xuzd on 17-7-31.
 */
public class SimpleMRAppMasterTest {
    public static void main(String[] args) throws Exception {
        String jobID = "job_14382938893_11";
        SimpleMRAppMaster simpleMRAppMaster = new SimpleMRAppMaster("Test simple", jobID, 4);
        YarnConfiguration configuration = new YarnConfiguration(new Configuration());
        simpleMRAppMaster.serviceInit(configuration);
        simpleMRAppMaster.serviceStart();
        simpleMRAppMaster.getDispatcher().getEventHandler().handle(new JobEvent(jobID, JobEventType.JOB_KILL));
        simpleMRAppMaster.getDispatcher().getEventHandler().handle(new JobEvent(jobID, JobEventType.JOB_INIT));

    }
}
