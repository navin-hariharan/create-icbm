package com.createicbm.lib.tracker;

@FunctionalInterface
public interface IEventTrackerListener {

    void accept(EventTrackerEntry entry);

    default boolean consumes(EventTrackerType type) {
        return true;
    }
}
