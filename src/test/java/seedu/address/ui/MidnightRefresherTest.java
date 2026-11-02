package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Tests the date checks behind {@code MidnightRefresher} with a controllable clock, so no display is needed.
 */
public class MidnightRefresherTest {

    private static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");
    private static final ZoneId SANTIAGO = ZoneId.of("America/Santiago");
    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new MidnightRefresher(null, () -> { }));
        assertThrows(NullPointerException.class, () -> new MidnightRefresher(Clock.systemUTC(), null));
    }

    @Test
    public void delayUntilNextMidnight_ordinaryTimes_returnsTimeLeftInDay() {
        // EP: times on an ordinary day; the delay is the time left until the next local midnight.
        assertEquals(Duration.ofSeconds(30),
                MidnightRefresher.delayUntilNextMidnight(clockAt(SINGAPORE, "2026-10-29T23:59:30")));
        assertEquals(Duration.ofHours(12),
                MidnightRefresher.delayUntilNextMidnight(clockAt(SINGAPORE, "2026-10-29T12:00:00")));
    }

    @Test
    public void delayUntilNextMidnight_exactlyMidnight_returnsWholeDay() {
        // Boundary: at midnight itself, the next midnight is a full day away, not now.
        assertEquals(Duration.ofHours(24),
                MidnightRefresher.delayUntilNextMidnight(clockAt(SINGAPORE, "2026-10-30T00:00:00")));
    }

    @Test
    public void delayUntilNextMidnight_daylightSavingDays_followsLocalCalendar() {
        // EP: the day clocks go forward is 23 hours long, and the day they go back is 25 hours long.
        assertEquals(Duration.ofHours(23),
                MidnightRefresher.delayUntilNextMidnight(clockAt(NEW_YORK, "2026-03-08T00:00:00")));
        assertEquals(Duration.ofHours(25),
                MidnightRefresher.delayUntilNextMidnight(clockAt(NEW_YORK, "2026-11-01T00:00:00")));
    }

    @Test
    public void delayUntilNextMidnight_transitionAtMidnight_findsStartOfNextDay() {
        // EP: in Santiago the clocks change at midnight. When midnight is skipped, the next day
        // starts at 01:00, 30 minutes after 23:30. When midnight repeats, it is 90 minutes away.
        assertEquals(Duration.ofMinutes(30),
                MidnightRefresher.delayUntilNextMidnight(clockAt(SANTIAGO, "2026-09-05T23:30:00")));
        assertEquals(Duration.ofMinutes(90),
                MidnightRefresher.delayUntilNextMidnight(clockAt(SANTIAGO, "2026-04-04T23:30:00")));
    }

    @Test
    public void delayUntilNextMidnight_sameInstantInDifferentZones_usesClockZone() {
        // EP: the same moment is 23:30 in Kiritimati (UTC+14) and 22:30 the day before in Pago Pago (UTC-11).
        Instant instant = Instant.parse("2026-10-29T09:30:00Z");
        assertEquals(Duration.ofMinutes(30), MidnightRefresher.delayUntilNextMidnight(
                Clock.fixed(instant, ZoneId.of("Pacific/Kiritimati"))));
        assertEquals(Duration.ofMinutes(90), MidnightRefresher.delayUntilNextMidnight(
                Clock.fixed(instant, ZoneId.of("Pacific/Pago_Pago"))));
    }

    @Test
    public void getNextCheckDelay_midnightFarAway_waitsAtMostOneMinute() {
        // EP: long before midnight, the date is still checked every minute, which also covers
        // a computer that slept through midnight.
        MidnightRefresher refresher = new MidnightRefresher(clockAt(SINGAPORE, "2026-10-29T12:00:00"), () -> { });
        assertEquals(MidnightRefresher.MAX_CHECK_INTERVAL, refresher.getNextCheckDelay());
    }

    @Test
    public void getNextCheckDelay_midnightClose_firesJustAfterMidnight() {
        // EP: within the last minute of the day, the check lands just after midnight.
        MidnightRefresher refresher = new MidnightRefresher(clockAt(SINGAPORE, "2026-10-29T23:59:30"), () -> { });
        assertEquals(Duration.ofSeconds(30).plus(MidnightRefresher.MIDNIGHT_MARGIN), refresher.getNextCheckDelay());
    }

    @Test
    public void refreshIfDateChanged_sameDay_doesNotRefresh() {
        MutableClock clock = new MutableClock(SINGAPORE, "2026-10-29T08:00:00");
        int[] refreshCount = {0};
        MidnightRefresher refresher = new MidnightRefresher(clock, () -> refreshCount[0]++);

        clock.setTo("2026-10-29T23:59:59");
        assertFalse(refresher.refreshIfDateChanged());
        assertEquals(0, refreshCount[0]);
    }

    @Test
    public void refreshIfDateChanged_afterMidnight_refreshesOncePerNewDay() {
        MutableClock clock = new MutableClock(SINGAPORE, "2026-10-29T23:59:59");
        int[] refreshCount = {0};
        MidnightRefresher refresher = new MidnightRefresher(clock, () -> refreshCount[0]++);

        clock.setTo("2026-10-30T00:00:01");
        assertTrue(refresher.refreshIfDateChanged());
        assertEquals(1, refreshCount[0]);

        // Later checks on the same new day do nothing more.
        clock.setTo("2026-10-30T00:01:01");
        assertFalse(refresher.refreshIfDateChanged());
        assertEquals(1, refreshCount[0]);

        // Waking up several days later still refreshes once.
        clock.setTo("2026-11-02T09:00:00");
        assertTrue(refresher.refreshIfDateChanged());
        assertEquals(2, refreshCount[0]);
    }

    @Test
    public void refreshIfDateChanged_dateMovesBack_refreshes() {
        // EP: the clock is set back to the previous day, so overdue markers may need to disappear.
        MutableClock clock = new MutableClock(SINGAPORE, "2026-10-30T00:10:00");
        int[] refreshCount = {0};
        MidnightRefresher refresher = new MidnightRefresher(clock, () -> refreshCount[0]++);

        clock.setTo("2026-10-29T23:50:00");
        assertTrue(refresher.refreshIfDateChanged());
        assertEquals(1, refreshCount[0]);
    }

    @Test
    public void start_schedulesFirstCheckJustAfterMidnight() {
        RecordingScheduler scheduler = new RecordingScheduler();
        Clock clock = clockAt(SINGAPORE, "2026-10-29T23:59:30");
        MidnightRefresher refresher = new MidnightRefresher(clock, () -> { }, () -> scheduler, Runnable::run);

        refresher.start();
        assertEquals(List.of(Duration.ofSeconds(31)), scheduler.delays);
    }

    @Test
    public void start_calledTwice_schedulesOnce() {
        RecordingScheduler scheduler = new RecordingScheduler();
        int[] schedulersCreated = {0};
        Supplier<ScheduledExecutorService> schedulerFactory = () -> {
            schedulersCreated[0]++;
            return scheduler;
        };
        Clock clock = clockAt(SINGAPORE, "2026-10-29T12:00:00");
        MidnightRefresher refresher = new MidnightRefresher(clock, () -> { }, schedulerFactory, Runnable::run);

        refresher.start();
        refresher.start();
        assertEquals(1, schedulersCreated[0]);
        assertEquals(1, scheduler.tasks.size());
    }

    @Test
    public void scheduledCheck_afterMidnight_refreshesOnUiThreadAndReschedules() {
        MutableClock clock = new MutableClock(SINGAPORE, "2026-10-29T23:59:30");
        RecordingScheduler scheduler = new RecordingScheduler();
        List<String> events = new ArrayList<>();
        Executor uiThread = action -> {
            events.add("ui");
            action.run();
        };
        Runnable refreshAction = () -> events.add("refresh");
        MidnightRefresher refresher = new MidnightRefresher(clock, refreshAction, () -> scheduler, uiThread);
        refresher.start();

        clock.setTo("2026-10-30T00:00:01");
        scheduler.tasks.get(0).run();
        assertEquals(List.of("ui", "refresh"), events);
        assertEquals(List.of(Duration.ofSeconds(31), MidnightRefresher.MAX_CHECK_INTERVAL), scheduler.delays);

        // The next check on the same day reschedules again without refreshing.
        scheduler.tasks.get(1).run();
        assertEquals(List.of("ui", "refresh", "ui"), events);
        assertEquals(3, scheduler.tasks.size());
    }

    @Test
    public void stop_afterStart_shutsDownAndStopsRescheduling() {
        RecordingScheduler scheduler = new RecordingScheduler();
        Clock clock = clockAt(SINGAPORE, "2026-10-29T12:00:00");
        MidnightRefresher refresher = new MidnightRefresher(clock, () -> { }, () -> scheduler, Runnable::run);
        refresher.start();

        refresher.stop();
        assertTrue(scheduler.isShutdown());

        // A check that was already running when stop() was called does not schedule another.
        scheduler.tasks.get(0).run();
        assertEquals(1, scheduler.tasks.size());

        // Starting again after stopping has no effect.
        refresher.start();
        assertEquals(1, scheduler.tasks.size());
    }

    @Test
    public void stop_beforeStart_createsNoScheduler() {
        int[] schedulersCreated = {0};
        Supplier<ScheduledExecutorService> schedulerFactory = () -> {
            schedulersCreated[0]++;
            return new RecordingScheduler();
        };
        Clock clock = clockAt(SINGAPORE, "2026-10-29T12:00:00");
        MidnightRefresher refresher = new MidnightRefresher(clock, () -> { }, schedulerFactory, Runnable::run);

        refresher.stop();
        refresher.start();
        assertEquals(0, schedulersCreated[0]);
    }

    private static Clock clockAt(ZoneId zone, String localDateTime) {
        return Clock.fixed(toInstant(zone, localDateTime), zone);
    }

    private static Instant toInstant(ZoneId zone, String localDateTime) {
        return LocalDateTime.parse(localDateTime).atZone(zone).toInstant();
    }

    /**
     * A clock whose time a test can move, standing in for time passing while the app is open.
     */
    private static class MutableClock extends Clock {
        private final ZoneId zone;
        private Instant instant;

        MutableClock(ZoneId zone, String localDateTime) {
            this.zone = zone;
            setTo(localDateTime);
        }

        void setTo(String localDateTime) {
            instant = toInstant(zone, localDateTime);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId newZone) {
            return newZone.equals(zone) ? this : Clock.fixed(instant, newZone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    /**
     * A scheduler that records each scheduled check and its delay instead of running it, so a test can run
     * the checks itself without waiting or starting a thread.
     */
    private static class RecordingScheduler extends ScheduledThreadPoolExecutor {
        private final List<Runnable> tasks = new ArrayList<>();
        private final List<Duration> delays = new ArrayList<>();

        RecordingScheduler() {
            super(1);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
            tasks.add(task);
            delays.add(Duration.of(delay, unit.toChronoUnit()));
            return null;
        }
    }
}
