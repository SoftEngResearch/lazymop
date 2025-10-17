package edu.lazymop.types;

public class Event {
    public String eventName;
    public int eventLocation; // TODO: not sure if we should use int or string, need to measure performance
    public int eventFrequency;

    public Event(String eventName, int eventLocation, int eventFrequency) {
        this.eventName = eventName;
        this.eventLocation = eventLocation;
        this.eventFrequency = eventFrequency;
    }

    @Override
    public String toString() {
        return eventName + "~" + eventLocation + "x" + eventFrequency;
    }
}
