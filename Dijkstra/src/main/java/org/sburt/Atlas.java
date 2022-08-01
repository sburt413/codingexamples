package org.sburt;

import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collector;

public class Atlas {
    private final Map<Location, Collection<Route>> routesBySource;

    public Atlas(Map<Location, Collection<Route>> routesBySource) {
        this.routesBySource = routesBySource;
    }

    public static Atlas from(Collection<Route> routes) {
        return new Atlas(Multimaps.index(routes, Route::getSource).asMap());
    }

    public Collection<Route> getRoutes(Location from) {
        return routesBySource.getOrDefault(from, Collections.emptyList());
    }

    public Set<Location> allLocations() {
        return routesBySource.values().stream().flatMap(Collection::stream).map(route -> Sets.newHashSet(route.getSource(), route.getDestination())).collect(
                Collector.of(HashSet<Location>::new, AbstractCollection::addAll, (lhs, rhs) -> {
                    lhs.addAll(rhs);
                    return lhs;
                }));
    }
}
