package com.rocky.dida.client.job;

/**
 * Created by rocky on 18/2/22.
 */
public class JobContext {
    private String group;
    private String name;
    private String cron;
    private SimpleJob job;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public SimpleJob getJob() {
        return job;
    }

    public void setJob(SimpleJob job) {
        this.job = job;
    }
}
