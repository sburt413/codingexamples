package org.sburt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Path implements Comparable<Path> {
    public static final Comparator<Path> COMPARATOR = Path::compareTo;

    private final Location origin;
    private final Location destination;
    private final List<Route> steps;

    public Path(Location origin, Location destination, List<Route> steps) {
        this.origin = origin;
        this.destination = destination;
        this.steps = steps;
    }

    public static Path from(Location initialLocation) {
        return new Path(initialLocation, initialLocation, Lists.newArrayList());
    }

    public static Path of(Route initialRoute) {
        return new Path(initialRoute.getSource(), initialRoute.getDestination(), Lists.newArrayList(initialRoute));
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getDestination() {
        return destination;
    }

    public List<Route> getSteps() {
        return steps;
    }

    public int calculateTotalTime() {
        return steps.stream().mapToInt(Route::getTravelTime).sum();
    }

    public Path append(Route nextStep) {
        if (!nextStep.getSource().equals(destination)) {
            throw new IllegalArgumentException("Cannot append next step: " + nextStep + ", route does not continue from end of current path: " + destination);
        }

        List<Route> nextSteps = ImmutableList.<Route>builder().addAll(steps).add(nextStep).build();
        return new Path(origin, nextStep.getDestination(), nextSteps);
    }

    @Override
    public String toString() {
        return steps.stream().map(Route::toString).collect(Collectors.joining("\n"));
    }

    @Override
    public int compareTo(Path rhs) {
        return Integer.compare(this.calculateTotalTime(), rhs.calculateTotalTime());
    }
}
