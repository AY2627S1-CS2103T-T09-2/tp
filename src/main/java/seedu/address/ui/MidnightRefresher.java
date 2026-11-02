package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javafx.application.Platform;
import seedu.address.commons.core.LogsCenter;

/**
 * Runs a refresh action when the local date changes, so that date-dependent displays such as the
 * overdue marker on contact cards stay correct when the app is left open past midnight.
 *
 * <p>The date is checked just after each local midnight, and at least once a minute in between, so a
 * computer that sleeps through midnight or a clock that is changed is also picked up promptly.
 * The waiting is done on a background daemon thread, so the JavaFX animation timer is not kept running,
 * and the refresh action itself runs on the JavaFX application thread.
 */
public class MidnightRefresher {

    /** How long after midnight the check runs, so a timer that fires slightly early still sees the new day. */
    static final Duration MIDNIGHT_MARGIN = Duration.ofSeconds(1);

    /** The longest wait between two date checks. */
    static final Duration MAX_CHECK_INTERVAL = Duration.ofMinutes(1);

    private static final Logger logger = LogsCenter.getLogger(MidnightRefresher.class);

    private static final String THREAD_NAME = "midnight-refresher";

    private final Clock clock;
    private final Runnable refreshAction;
    private final Supplier<ScheduledExecutorService> schedulerFactory;
    private final Executor uiThread;
    private LocalDate lastSeenDate;
    private ScheduledExecutorService scheduler;
    private boolean isStopped;

    /**
     * Creates a {@code MidnightRefresher} that runs {@code refreshAction} on the JavaFX application thread
     * whenever the date given by {@code clock} changes. Call {@link #start()} to begin checking.
     */
    public MidnightRefresher(Clock clock, Runnable refreshAction) {
        this(clock, refreshAction, MidnightRefresher::createDaemonScheduler, Platform::runLater);
    }

    /**
     * Creates a {@code MidnightRefresher} that waits using a scheduler from {@code schedulerFactory}
     * and runs each date check through {@code uiThread}.
     */
    MidnightRefresher(Clock clock, Runnable refreshAction, Supplier<ScheduledExecutorService> schedulerFactory,
            Executor uiThread) {
        this.clock = requireNonNull(clock);
        this.refreshAction = requireNonNull(refreshAction);
        this.schedulerFactory = requireNonNull(schedulerFactory);
        this.uiThread = requireNonNull(uiThread);
        lastSeenDate = LocalDate.now(clock);
    }

    /**
     * Returns the time from now until the start of the next day in the clock's time zone.
     * Daylight saving changes are followed, including days whose midnight is skipped.
     */
    public static Duration delayUntilNextMidnight(Clock clock) {
        requireNonNull(clock);
        ZoneId zone = clock.getZone();
        Instant now = clock.instant();
        Instant nextMidnight = LocalDate.ofInstant(now, zone).plusDays(1).atStartOfDay(zone).toInstant();
        return Duration.between(now, nextMidnight);
    }

    /**
     * Returns how long to wait before the next date check: just after the next midnight,
     * or {@link #MAX_CHECK_INTERVAL}, whichever is sooner.
     */
    Duration getNextCheckDelay() {
        Duration untilJustAfterMidnight = delayUntilNextMidnight(clock).plus(MIDNIGHT_MARGIN);
        return untilJustAfterMidnight.compareTo(MAX_CHECK_INTERVAL) < 0 ? untilJustAfterMidnight : MAX_CHECK_INTERVAL;
    }

    /**
     * Runs the refresh action if the date has changed since the last check.
     *
     * @return true if the refresh action was run.
     */
    boolean refreshIfDateChanged() {
        LocalDate today = LocalDate.now(clock);
        if (today.equals(lastSeenDate)) {
            return false;
        }

        logger.info("Date changed from " + lastSeenDate + " to " + today + "; refreshing the contact list.");
        lastSeenDate = today;
        refreshAction.run();
        return true;
    }

    /**
     * Starts checking the date. Calling this again, or after {@link #stop()}, has no effect.
     */
    public synchronized void start() {
        if (scheduler != null || isStopped) {
            return;
        }

        scheduler = schedulerFactory.get();
        scheduleNextCheck();
    }

    /**
     * Stops checking the date and ends the background thread. Safe to call more than once.
     */
    public synchronized void stop() {
        isStopped = true;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private synchronized void scheduleNextCheck() {
        if (isStopped) {
            return;
        }

        scheduler.schedule(this::runCheck, getNextCheckDelay().toMillis(), TimeUnit.MILLISECONDS);
    }

    private void runCheck() {
        try {
            uiThread.execute(this::refreshIfDateChanged);
        } catch (RuntimeException e) {
            logger.warning("Could not check the date for the contact list: " + e);
        } finally {
            scheduleNextCheck();
        }
    }

    private static ScheduledExecutorService createDaemonScheduler() {
        return Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        });
    }
}
