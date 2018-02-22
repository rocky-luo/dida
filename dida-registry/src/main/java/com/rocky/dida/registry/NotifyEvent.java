package com.rocky.dida.registry;

/**
 * Created by rocky on 17/11/1.
 */
public class NotifyEvent {
    private Event event;
    private String nodePath;
    private String data;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public enum Event {
        ADD_NODE,
        MODIFY_NODE,
        DELETE_NODE
    }

    @Override
    public String toString() {
        return "NotifyEvent{" +
                "event=" + event +
                ", nodePath='" + nodePath + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
