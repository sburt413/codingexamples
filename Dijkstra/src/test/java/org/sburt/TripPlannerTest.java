package org.sburt;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TripPlannerTest {

    @Test
    public void testTrivialCase() {
        Location a = new Location("A");
        Location b = new Location("B");
        Location c = new Location("C");

        Route ab = new Route("AB", a, b, 1);
        Route bc = new Route("BC", b, c, 2);

        Atlas atlas = Atlas.from(Sets.newHashSet(ab, bc));
        TripPlanner tripPlanner = new TripPlanner(atlas);
        Path path = tripPlanner.buildPlan(a, c).orElseThrow(() -> new AssertionError("Could not find expected path!"));
        assertEquals(path.calculateTotalTime(), 3);
        assertEquals(a, path.getOrigin());
        assertEquals(c, path.getDestination());
        assertThat(path.getSteps(), contains(ab, bc));
    }

    @Test
    public void testUsesShortestPathByTime() {
        Location a = new Location("A");
        Location b = new Location("B");
        Location c = new Location("C");

        Route ab = new Route("AB", a, b, 1);
        Route bc = new Route("BC", b, c, 2);
        Route ac = new Route("AC", a, c, 4);

        Atlas atlas = Atlas.from(Sets.newHashSet(ab, bc, ac));
        TripPlanner tripPlanner = new TripPlanner(atlas);
        Path path = tripPlanner.buildPlan(a, c).orElseThrow(() -> new AssertionError("Could not find expected path!"));
        assertEquals(path.calculateTotalTime(), 3);
        assertEquals(a, path.getOrigin());
        assertEquals(c, path.getDestination());
        assertThat(path.getSteps(), contains(ab, bc));
    }

    @Test
    public void testDeadEnd() {
        // Worst case scenario of Dijkstra's Algorithm, a dead-end on the shortest path
        Location a = new Location("A");
        Location b = new Location("B");
        Location c = new Location("C");
        Location d = new Location("D");
        Location e = new Location("E");
        Location f = new Location("F");

        Route ab = new Route("AB", a, b, 1);
        Route bc = new Route("BC", b, c, 1);
        Route cd = new Route("CD", c, d, 1);
        Route ae = new Route("AE", a, e, 5);
        Route ef = new Route("EF", e, f, 5);

        Atlas atlas = Atlas.from(Sets.newHashSet(ab, bc, cd, ae, ef));
        TripPlanner tripPlanner = new TripPlanner(atlas);
        Path path = tripPlanner.buildPlan(a, f).orElseThrow(() -> new AssertionError("Could not find expected path!"));
        assertEquals(path.calculateTotalTime(), 10);
        assertEquals(a, path.getOrigin());
        assertEquals(f, path.getDestination());
        assertThat(path.getSteps(), contains(ae, ef));
    }

    @Test
    public void testNonExistentPath() {
        Location a = new Location("A");
        Location b = new Location("B");
        Location c = new Location("C");
        Location d = new Location("D");

        Route ab = new Route("AB", a, b, 1);
        Route cd = new Route("CD", c, d, 2);

        Atlas atlas = Atlas.from(Sets.newHashSet(ab, cd));
        TripPlanner tripPlanner = new TripPlanner(atlas);
        assertTrue(tripPlanner.buildPlan(a, d).isEmpty(), "Found unexpected path!");
    }

    @Test
    public void testMultipleConnections() {
        Location a = new Location("A");
        Location b = new Location("B");
        Location c = new Location("C");
        Location d = new Location("D");

        // Time(abLong + bc + cd) > Time(ad) > Time(abShort + bc + cd)
        // abShort -> bc -> cd should be the shortest
        Route abLong = new Route("AB (Long)", a, b, 5);
        Route abShort = new Route("AB (Short)", a, b, 2);
        Route bc = new Route("BC", b, c, 1);
        Route cd = new Route("CD", c, d, 2);
        Route ad = new Route("AD", a, d, 6);

        Atlas atlas = Atlas.from(Sets.newHashSet(abLong, abShort, bc, cd, ad));
        TripPlanner tripPlanner = new TripPlanner(atlas);
        Path path = tripPlanner.buildPlan(a, d).orElseThrow(() -> new AssertionError("Could not find expected path!"));
        assertEquals(path.calculateTotalTime(), 5);
        assertEquals(a, path.getOrigin());
        assertEquals(d, path.getDestination());
        assertThat(path.getSteps(), contains(abShort, bc, cd));
    }

    @Test
    public void testLoop() {
        Location a = new Location("A");
        Location b = new Location("B");
        Location c = new Location("C");
        Location d = new Location("D");

        Route ab = new Route("AB", a, b, 1);
        Route bc = new Route("BC", b, c, 1);
        Route cd = new Route("CD", c, d, 2);
        Route ca = new Route("CA", c, a, 1);

        Atlas atlas = Atlas.from(Sets.newHashSet(ab, bc, cd, ca));
        TripPlanner tripPlanner = new TripPlanner(atlas);
        Path path = tripPlanner.buildPlan(a, d).orElseThrow(() -> new AssertionError("Could not find expected path!"));
        assertEquals(path.calculateTotalTime(), 4);
        assertEquals(a, path.getOrigin());
        assertEquals(d, path.getDestination());
        assertThat(path.getSteps(), contains(ab, bc, cd));
    }

    @Test
    public void testDoubleDiamond() {
        Location a = new Location("A");
        Location b = new Location("B");
        Location c = new Location("C");
        Location d = new Location("D");
        Location e = new Location("E");
        Location f = new Location("F");
        Location g = new Location("G");

        // First Diamond (A->B->D is quickest)
        Route ab = new Route("AB", a, b, 3);
        Route ac = new Route("AC", a, c, 2);
        Route bd = new Route("BD", b, d, 1);
        Route cd = new Route("CD", c, d, 3);

        // Second Diamond (D->F->G is quickest)
        Route de = new Route("DE", d, e, 1);
        Route df = new Route("DF", d, f, 2);
        Route eg = new Route("EG", e, g, 4);
        Route fg = new Route("FG", f, g, 2);

        Atlas atlas = Atlas.from(Sets.newHashSet(ab, ac, bd, cd, de, df, eg, fg));
        TripPlanner tripPlanner = new TripPlanner(atlas);
        Path path = tripPlanner.buildPlan(a, g).orElseThrow(() -> new AssertionError("Could not find expected path!"));
        assertEquals(path.calculateTotalTime(), 8);
        assertEquals(a, path.getOrigin());
        assertEquals(g, path.getDestination());
        assertThat(path.getSteps(), contains(ab, bd, df, fg));
    }

    @Test
    public void testTiesFavorEarliestShorterPath() {
        // Dijkstra's Algorithm requires that you follow the shortest marked path first
        Location a = new Location("A");
        Location b = new Location("B");
        Location c = new Location("C");
        Location d = new Location("D");
        Location e = new Location("E");
        Location f = new Location("F");

        Route ab = new Route("AB", a, b, 2);
        Route bc = new Route("BC", b, c, 2);
        Route cd = new Route("CF", c, f, 2);

        Route ad = new Route("AD", a, d, 2);
        Route de = new Route("DE", d, e, 1);
        Route ef = new Route("EF", e, f, 3);

        // Both paths should produce a result of time 6, but `de` should be considered before `bc` because `de is shorter.

        Atlas atlas = Atlas.from(Sets.newHashSet(ab, bc, cd, ad, de, ef));
        TripPlanner tripPlanner = new TripPlanner(atlas);
        Path path = tripPlanner.buildPlan(a, f).orElseThrow(() -> new AssertionError("Could not find expected path!"));
        assertEquals(path.calculateTotalTime(), 6);
        assertEquals(a, path.getOrigin());
        assertEquals(f, path.getDestination());
        assertThat(path.getSteps(), contains(ad, de, ef));
    }
}
