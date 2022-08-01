package org.sburt;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TripPlanner {
    private final Atlas atlas;

    public TripPlanner(Atlas atlas) {
        this.atlas = atlas;
    }

    public Optional<Path> buildPlan(Location origin, Location destination) {
        Map<Location, Path> shortestKnownPaths = Maps.newHashMap();
        shortestKnownPaths.put(origin, Path.from(origin));

        planCalculator(destination, shortestKnownPaths, Sets.newHashSet(atlas.allLocations()), Path.from(origin));

        return Optional.ofNullable(shortestKnownPaths.get(destination));
    }

    private Optional<Path> planCalculator(Location target, Map<Location, Path> shortestKnownPaths, Set<Location> unvisitedLocations, Path currentPath) {
        Location currentLocation = currentPath.getDestination();
        if (currentLocation.equals(target)) {
            return Optional.of(currentPath);
        }

        Collection<Route> availableRoutes = atlas.getRoutes(currentLocation);

        availableRoutes.forEach(availableRoute -> {
            Location destination = availableRoute.getDestination();
            Path newPath = currentPath.append(availableRoute);
            if (!shortestKnownPaths.containsKey(destination)) {
                shortestKnownPaths.put(destination, newPath);
            } else {
                Path existingPath = shortestKnownPaths.get(destination);
                if (newPath.calculateTotalTime() < existingPath.calculateTotalTime()) {
                    shortestKnownPaths.put(destination, newPath);
                }
            }
        });

        unvisitedLocations.remove(currentLocation);


        Optional<Path> nextPath = shortestKnownPathToUnvisitedLocation(shortestKnownPaths.values(), unvisitedLocations);
        if (nextPath.isPresent()) {
            return planCalculator(target, shortestKnownPaths, unvisitedLocations, nextPath.get());
        } else {
            // No existing path to target (we checked everything)
            return Optional.empty();
        }
    }

    private static Optional<Path> shortestKnownPathToUnvisitedLocation(Collection<Path> shortestKnownPathsToDestination, Set<Location> unvisitedLocations) {
        return shortestKnownPathsToDestination.stream().filter(path -> unvisitedLocations.contains(path.getDestination())).min(Path.COMPARATOR);
    }

    public Optional<Path> buildBruteForcePlan(Location origin, Location destination) {
        Map<Location, Path> shortestKnownPaths = Maps.newHashMap();
        shortestKnownPaths.put(origin, Path.from(origin));

        bruteForcePlanCalculator(shortestKnownPaths, Path.from(origin));
        return Optional.ofNullable(shortestKnownPaths.get(destination));
    }

    private void bruteForcePlanCalculator(Map<Location, Path> shortestKnownPaths, Path currentPath) {
        Location currentLocation = currentPath.getDestination();
        Collection<Route> availableRoutes = atlas.getRoutes(currentLocation);

        availableRoutes.forEach(availableRoute -> {
            Location destination = availableRoute.getDestination();
            Path newPath = currentPath.append(availableRoute);
            if (!shortestKnownPaths.containsKey(destination)) {
                shortestKnownPaths.put(destination, newPath);
            } else {
                Path existingPath = shortestKnownPaths.get(destination);
                if (newPath.calculateTotalTime() < existingPath.calculateTotalTime()) {
                    shortestKnownPaths.put(destination, newPath);
                }
            }
        });

        availableRoutes.forEach(availableRoute -> bruteForcePlanCalculator(shortestKnownPaths,
                                                                           currentPath.append(availableRoute)));
    }
}
