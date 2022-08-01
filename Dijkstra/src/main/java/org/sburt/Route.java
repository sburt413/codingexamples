package org.sburt;

import java.util.Objects;

public class Route {
    private final String name;
    private final Location source;
    private final Location destination;
    private int travelTime;

    public Route(String name, Location source, Location destination, int travelTime) {
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.travelTime = travelTime;
    }

    public String getName() {
        return name;
    }

    public Location getSource() {
        return source;
    }

    public Location getDestination() {
        return destination;
    }

    public int getTravelTime() {
        return travelTime;
    }

    @Override
    public String toString() {
        return "[" + name + ", " + source.getName() + " -(" + travelTime + ")-> " + destination.getName() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Route route = (Route) o;
        return travelTime == route.travelTime && Objects.equals(name, route.name) && Objects.equals(source,
                                                                                                    route.source) && Objects
                .equals(destination, route.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, source, destination, travelTime);
    }
}
