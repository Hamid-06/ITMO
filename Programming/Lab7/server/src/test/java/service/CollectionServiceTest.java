package service;

import db.PersistenceException;
import models.*;
import org.junit.jupiter.api.Test;
import support.MemoryRouteRepository;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class CollectionServiceTest {
    private static RouteData data(Long distance) {
        return new RouteData("route", new Coordinates(0f, 1L), new Location(1, 2.0, 3L), null, distance);
    }

    @Test
    void failedWritesLeaveMemoryUnchanged() {
        var repo = new MemoryRouteRepository();
        var service = new CollectionService(repo);
        Route original = service.add(data(5L), 1, false).orElseThrow();
        repo.fail = true;
        assertThrows(PersistenceException.class, () -> service.add(data(6L), 1, false));
        assertThrows(PersistenceException.class, () -> service.update(original.getId(), data(7L), 1));
        assertThrows(PersistenceException.class, () -> service.clear(1));
        assertEquals(List.of(original), service.snapshot());
        assertEquals(5L, service.snapshot().get(0).getDistance());
    }

    @Test
    void uncertainCommitStopsServingPossiblyStaleCache() {
        var repo = new MemoryRouteRepository();
        var service = new CollectionService(repo);
        repo.fail = true;
        repo.uncertain = true;
        assertThrows(PersistenceException.class, () -> service.add(data(5L), 1, false));
        assertThrows(IllegalStateException.class, service::snapshot);
        assertThrows(IllegalStateException.class, service::info);
    }

    @Test
    void allOwnersAreVisibleButOnlyOwnObjectsCanBeModified() {
        var repo = new MemoryRouteRepository();
        var service = new CollectionService(repo);
        Route mine = service.add(data(10L), 1, false).orElseThrow();
        Route other = service.add(data(20L), 2, false).orElseThrow();
        assertEquals(2, service.snapshot().size());
        assertThrows(IllegalArgumentException.class, () -> service.update(other.getId(), data(8L), 1));
        assertThrows(IllegalArgumentException.class, () -> service.removeById(other.getId(), 1));
        assertEquals(0, service.removeAnyByDistance(20L, 1));
        assertEquals(1, service.removeCompared(data(5L), 1, true));
        assertEquals(List.of(other), service.snapshot());
        assertEquals(0, service.clear(1));
        assertFalse(repo.rows.containsKey(mine.getId()));
        assertTrue(repo.rows.containsKey(other.getId()));
    }

    @Test
    void conditionalAddUsesGlobalMinimumAndStrictDistanceComparison() {
        var service = new CollectionService(new MemoryRouteRepository());
        assertTrue(service.add(data(10L), 2, true).isPresent());
        assertTrue(service.add(data(10L), 1, true).isEmpty());
        assertTrue(service.add(data(11L), 1, true).isEmpty());
        assertTrue(service.add(data(9L), 1, true).isPresent());
        assertTrue(service.add(data(null), 1, true).isPresent());
        assertTrue(service.add(data(null), 1, true).isEmpty());
    }

    @Test
    void snapshotsAndQueriesDoNotReloadDatabase() {
        var repo = new MemoryRouteRepository();
        var service = new CollectionService(repo);
        service.add(data(2L), 1, false);
        for (int i = 0; i < 20; i++) {
            service.snapshot();
            service.info();
        }
        assertEquals(1, repo.loads.get());
        assertThrows(UnsupportedOperationException.class, () -> service.snapshot().clear());
    }

    @Test
    void concurrentConditionalAddsAreAtomic() throws Exception {
        var service = new CollectionService(new MemoryRouteRepository());
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            List<Callable<Boolean>> jobs = new ArrayList<>();
            for (int i = 0; i < 40; i++) jobs.add(() -> service.add(data(10L), 1, true).isPresent());
            int accepted = 0;
            for (Future<Boolean> future : pool.invokeAll(jobs)) if (future.get()) accepted++;
            assertEquals(1, accepted);
            assertEquals(1, service.snapshot().size());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void readerCannotObserveUncommittedInsert() throws Exception {
        CountDownLatch insideInsert = new CountDownLatch(1);
        CountDownLatch commit = new CountDownLatch(1);
        var repo = new MemoryRouteRepository() {
            @Override public Route insert(RouteData data, int owner) {
                insideInsert.countDown();
                try {
                    if (!commit.await(3, TimeUnit.SECONDS)) throw new AssertionError("Commit timeout");
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
                return super.insert(data, owner);
            }
        };
        var service = new CollectionService(repo);
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            Future<?> write = pool.submit(() -> service.add(data(2L), 1, false));
            assertTrue(insideInsert.await(2, TimeUnit.SECONDS));
            Future<List<Route>> read = pool.submit(service::snapshot);
            assertThrows(TimeoutException.class, () -> read.get(100, TimeUnit.MILLISECONDS));
            commit.countDown();
            write.get(3, TimeUnit.SECONDS);
            assertEquals(1, read.get(3, TimeUnit.SECONDS).size());
        } finally {
            commit.countDown();
            pool.shutdownNow();
        }
    }
}
