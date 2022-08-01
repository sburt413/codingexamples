package org.sburt;

import java.util.Optional;

public class TripPlanner {
    private final Atlas atlas;

    public TripPlanner(Atlas atlas) {
        this.atlas = atlas;
    }

    public Optional<Path> buildPlan(Location origin, Location destination) {
        throw new AssertionError("Method implementation needed");
    }
}
